package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.AndRevFilter
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.util.io.NullOutputStream
import java.io.File
import java.io.FileReader
import kotlin.math.ceil

class RepositoryCodeIncrementInteractor :
    Interactor<Pair<String, List<Range>>, RepositoryCodeChangesReport> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryCodeChangesReport> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification =
                tryReadSpecFromFile(argPath)

            val result = mutableListOf<RepositoryReport.GroupedResults<RepositoryCodeChangesReport.CodeChangesResult>>()
            if (specification != null) {
                createReportsForSpecification(specification, ranges, result)
            } else {
                createReportsForSingleGitRepo(argPath, ranges, result)
            }
            emit(CodeChangesReportImpl(result))
        }
    }

    private fun createReportsForSingleGitRepo(
        repoPath: String,
        ranges: List<Range>,
        fullResult: MutableList<RepositoryReport.GroupedResults<RepositoryCodeChangesReport.CodeChangesResult>>
    ) {
        val defaultPercentile = Percentile.P100
        val reportResultPair = calculateReport(repoPath, ranges, percentile = defaultPercentile)
        val commitDiffs = mapOf(reportResultPair)
        fullResult += RepositoryReport.GroupedResults(
            "single",
            RepositoryCodeChangesReport.CodeChangesResult(
                commitDiffs,
                calculateSummary(commitDiffs, defaultPercentile)
            )
        )
    }

    private fun createReportsForSpecification(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        fullResults: MutableList<RepositoryReport.GroupedResults<RepositoryCodeChangesReport.CodeChangesResult>>
    ) {
        specification.projects.forEach { (groupName, projects) ->
            val commitDiffs = mutableMapOf<String, List<RepositoryCodeChangesReport.DiffWithRanges>>()
            projects.forEach { projectSpec ->
                val localPath = projectSpec.localPath
                val report: Pair<String, List<RepositoryCodeChangesReport.DiffWithRanges>> = calculateReport(
                    localPath,
                    ranges,
                    projectSpec.linesSpec,
                    projectSpec.percentile
                )
                commitDiffs[report.first] = report.second
            }
            val percentileForSummary = projects.firstOrNull()?.percentile ?: Percentile.P100
            fullResults += RepositoryReport.GroupedResults(
                groupName,
                RepositoryCodeChangesReport.CodeChangesResult(
                    commitDiffs,
                    calculateSummary(commitDiffs, percentileForSummary)
                )
            )
        }
    }

    private fun calculateSummary(
        data: Map<String, List<RepositoryCodeChangesReport.DiffWithRanges>>,
        percentile: Percentile
    ): RepositoryCodeChangesReport.Summary {
        val map: List<Map<Range, RepositoryCodeChangesReport.DiffWithRanges>> = data.values.map { diffWithRanges ->
            diffWithRanges.associateBy { it.range }
        }
        val resultMap = mutableMapOf<Range, MutableList<RepositoryCodeChangesReport.DiffWithRanges>>()
        map.forEach { it: Map<Range, RepositoryCodeChangesReport.DiffWithRanges> ->
            it.entries.forEach {
                val currentList: MutableList<RepositoryCodeChangesReport.DiffWithRanges> = resultMap.getOrPut(it.key) {
                    mutableListOf()
                }
                currentList += it.value
            }
        }
        val result = mutableMapOf<Range, RepositoryCodeChangesReport.DiffWithRanges>()
        resultMap.forEach { (range, list) ->
            val diffs: List<RepositoryCodeChangesReport.CommitDiff> = list.map {
                it.diffs
            }.flatten()
            result[range] = RepositoryCodeChangesReport.DiffWithRanges(
                range,
                diffs,
                calculatePercentileData(diffs, percentile)
            )
        }


        val totalDiffs: List<RepositoryCodeChangesReport.CommitDiff> = result.values.map { it.diffs }.flatten()
        val totalPercentile = calculatePercentileData(totalDiffs, percentile)
        val totalRange = Range(
            result.values.first().range.start,
            result.values.last().range.end,
        )
        val totalData = RepositoryCodeChangesReport.DiffWithRanges(
            totalRange,
            totalDiffs,
            totalPercentile
        )
        return RepositoryCodeChangesReport.Summary(result, totalData)
    }

    private fun calculateReport(
        repoPath: String,
        ranges: List<Range>,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec? = null,
        percentile: Percentile
    ): Pair<String, List<RepositoryCodeChangesReport.DiffWithRanges>> {

        val regex = createRegexForFilesExclusions(linesSpec)
        val git: Git = Git.open(File(repoPath))
        val projectPath = git.firstRemoteUri()
        val commitsWithRanges = mapAllRangesToCommits(git, ranges)
        val formatter = DiffFormatter(NullOutputStream.INSTANCE)
        formatter.setRepository(git.repository)
        formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL)
        formatter.isDetectRenames = true
        val resultMap = mutableMapOf<Range, List<RepositoryCodeChangesReport.CommitDiff>>()
        commitsWithRanges.forEach { (range, commits) ->
            val resultCommits = mutableListOf<RepositoryCodeChangesReport.CommitDiff>()
            commits.forEach { commit ->
                if (commit.parents.isNotEmpty()) {
                    val prevCommit = commit.parents[0]
                    val commitDiff = calculateDiff(formatter, prevCommit, commit, regex)
                    resultCommits += commitDiff
                } else {
                    // TODO: track such commits
                }
            }
            resultMap[range] = resultCommits
        }
        return projectPath to resultMap.flatMap {
            listOf(RepositoryCodeChangesReport.DiffWithRanges(it.key, it.value, statisticsData = calculatePercentileData(
                it.value,
                percentile
            )))
        }
    }

    enum class PercentileColumn {
        TotalChanges, TotalIncrement, LinesAdded
    }


    private fun calculatePercentileData(
        diffs: List<RepositoryCodeChangesReport.CommitDiff>, percentile: Percentile
    ): RepositoryCodeChangesReport.DiffWithRanges.StatisticsData {
        return RepositoryCodeChangesReport.DiffWithRanges.StatisticsData(
            percentile = percentile,
            linesAdded = calculatePercentileByColumn(PercentileColumn.LinesAdded, diffs, percentile),
            totalChanged = calculatePercentileByColumn(PercentileColumn.TotalChanges, diffs,
                percentile
            ),
            codeIncrement = calculatePercentileByColumn(PercentileColumn.TotalIncrement, diffs,
                percentile
            ),
            addedAvg = calculateAvgByColumn(PercentileColumn.LinesAdded, diffs)
        )
    }

    private fun calculateAvgByColumn(
        column: PercentileColumn,
        diffs: List<RepositoryCodeChangesReport.CommitDiff>
    ): Int {
        return targetValues(column, diffs).average().toInt()
    }

    private fun calculatePercentileByColumn(
        column: PercentileColumn,
        diffs: List<RepositoryCodeChangesReport.CommitDiff>,
        percentile: Percentile
    ): Int {
        if (diffs.isEmpty()) return 0
        val targetList = targetValues(column, diffs)
        val sortedList = targetList.sorted()
        val index = ceil(percentile.factor * (sortedList.size)).toInt()
        return sortedList[index - 1]
    }

    private fun targetValues(
        column: PercentileColumn,
        diffs: List<RepositoryCodeChangesReport.CommitDiff>
    ): List<Int> {
        return when (column) {
            PercentileColumn.TotalChanges -> diffs.map {
                it.totalChanges()
            }

            PercentileColumn.TotalIncrement -> diffs.map {
                it.codeIncrement()
            }

            PercentileColumn.LinesAdded -> diffs.map {
                it.linesAdded
            }

        }
    }

    private fun createRegexForFilesExclusions(linesSpec: RepositoryAnalysisSpecification.LinesSpec?): Regex? {
        return if (linesSpec?.excludedPaths?.isNotEmpty() == true) {
            linesSpec
                .excludedPaths
                .joinTo(StringBuilder(), "|", "(", ")")
                .toString()
                .toRegex()
        } else null
    }

    private fun calculateDiff(
        formatter: DiffFormatter,
        prevCommit: RevCommit,
        currentCommit: RevCommit,
        regex: Regex?
    ): RepositoryCodeChangesReport.CommitDiff {
        val diffEntries = formatter.scan(prevCommit, currentCommit)
        var localAdd = 0
        var localRemove = 0
        var ignoredAdd = 0
        var ignoredRemove = 0
        diffEntries.forEach { diffEntry ->
            val header = formatter.toFileHeader(diffEntry)
            if (header.changeType != DiffEntry.ChangeType.DELETE) {
                val fileNewPath = header.newPath
                val isFileIgnored = regex?.find(fileNewPath) != null
                val editList = header.toEditList()
                editList.forEach { edit ->
                    if (isFileIgnored) {
                        ignoredRemove += edit.lengthA
                        ignoredAdd += edit.lengthB
                    } else {
                        localRemove += edit.lengthA
                        localAdd += edit.lengthB
                    }
                }

            }
        }
        return RepositoryCodeChangesReport.CommitDiff(
            prevCommit.name,
            currentCommit.name,
            localAdd,
            localRemove,
            ignoredAdd,
            ignoredRemove,
            currentCommit.authorIdent.emailAddress
        )
    }

    private fun Git.firstRemoteUri() = remoteList().call()[0].urIs[0].toString()

    private suspend fun tryReadSpecFromFile(filePath: String): RepositoryAnalysisSpecification? {
        return try {
            RepositoryAnalysisSpecification.fromString(withContext(Dispatchers.IO) {
                FileReader(filePath).readText()
            })
        } catch (_: Exception) {
            null
        }
    }

    private fun mapAllRangesToCommits(git: Git, ranges: List<Range>): Map<Range, List<RevCommit>> {
        val result = mutableMapOf<Range, List<RevCommit>>()
        ranges.forEach { range ->
            val commits = git.log()
                .add(git.repository.findRef("HEAD").objectId)
                .setRevFilter(
                    AndRevFilter.create(
                        listOf(
                            CommitTimeRevFilter.between(range.start, range.end),
                            RevFilter.NO_MERGES
                        )
                    )
                )
                .call().toList()
            result[range] = commits
        }
        return result
    }

    data class CodeChangesReportImpl(
        override val result: List<RepositoryReport.GroupedResults<RepositoryCodeChangesReport.CodeChangesResult>> = emptyList()
    ) : RepositoryCodeChangesReport

}