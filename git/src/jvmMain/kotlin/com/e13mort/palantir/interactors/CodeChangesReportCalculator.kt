/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.git.MailMap
import com.e13mort.palantir.git.MailMapFactory
import com.e13mort.palantir.model.Percentile
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
import java.nio.file.Paths
import kotlin.math.ceil

class CodeChangesReportCalculator(
    private val calculationType: CalculationType = CalculationType.FULL,
    private val mailMapFactory: MailMapFactory
) :
    RepositoryAnalyticsInteractor.RepositoryReportCalculator<CodeChangesReportItem> {
    enum class CalculationType {
        FULL,
        AUTHORS
    }

    enum class PercentileColumn {
        TotalChanges, TotalIncrement, LinesAdded
    }

    override suspend fun calculateReport(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        singleGroupName: String?,
        itemIndexInGroup: Int?,
        explicitAuthors: Set<String>
    ): List<RepositoryReport.GroupedResults<CodeChangesReportItem>> {
        val fullResults =
            mutableListOf<RepositoryReport.GroupedResults<CodeChangesReportItem>>()
        specification.projects.filter {
            if (singleGroupName != null) {
                singleGroupName == it.key
            } else true
        }.forEach { (groupName, projects) ->
            val commitDiffs = mutableMapOf<String, List<CodeChangesReportItem.DiffWithRanges>>()
            projects.forEachIndexed { index, projectSpec ->
                if (itemIndexInGroup == null || index == itemIndexInGroup) {
                    val localPath = projectSpec.localPath
                    val report: Pair<String, List<CodeChangesReportItem.DiffWithRanges>> =
                        calculateReport(
                            localPath,
                            ranges,
                            projectSpec.linesSpec,
                            projectSpec.percentile,
                            projectSpec.mailMap,
                            explicitAuthors
                        )
                    commitDiffs[report.first] = report.second
                }
            }
            if (commitDiffs.isNotEmpty()) {
                val percentileForSummary = projects.firstOrNull()?.percentile ?: Percentile.P100
                fullResults += RepositoryReport.GroupedResults(
                    groupName, CodeChangesReportItem(
                        commitDiffs,
                        calculateSummary(commitDiffs, percentileForSummary),
                        specification.authorGroups
                    )
                )
            }
        }
        return fullResults
    }

    private fun calculateSummary(
        data: Map<String, List<CodeChangesReportItem.DiffWithRanges>>,
        percentile: Percentile
    ): CodeChangesReportItem.Summary {
        val map: List<Map<Range, CodeChangesReportItem.DiffWithRanges>> =
            data.values.map { diffWithRanges ->
                diffWithRanges.associateBy { it.range }
            }
        val resultMap = mutableMapOf<Range, MutableList<CodeChangesReportItem.DiffWithRanges>>()
        map.forEach { it: Map<Range, CodeChangesReportItem.DiffWithRanges> ->
            it.entries.forEach {
                val currentList: MutableList<CodeChangesReportItem.DiffWithRanges> =
                    resultMap.getOrPut(it.key) {
                        mutableListOf()
                    }
                currentList += it.value
            }
        }
        val result = mutableMapOf<Range, CodeChangesReportItem.DiffWithRanges>()
        resultMap.forEach { (range, list) ->
            val diffs: List<CodeChangesReportItem.CommitDiff> = list.map {
                it.diffs
            }.flatten()
            result[range] = CodeChangesReportItem.DiffWithRanges(
                range,
                diffs,
                calculatePercentileData(diffs, percentile)
            )
        }


        val totalDiffs: List<CodeChangesReportItem.CommitDiff> =
            result.values.map { it.diffs }.flatten()
        val totalPercentile = calculatePercentileData(totalDiffs, percentile)
        val totalRange = Range(
            result.values.first().range.start,
            result.values.last().range.end,
        )
        val totalData = CodeChangesReportItem.DiffWithRanges(
            totalRange,
            totalDiffs,
            totalPercentile
        )
        return CodeChangesReportItem.Summary(result, totalData)
    }

    private fun calculateReport(
        repoPath: String,
        ranges: List<Range>,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec? = null,
        percentile: Percentile,
        mailMapType: RepositoryAnalysisSpecification.MailMapType,
        explicitAuthors: Set<String>
    ): Pair<String, List<CodeChangesReportItem.DiffWithRanges>> {

        val regex = createRegexForFilesExclusions(linesSpec)
        val git: Git = Git.open(File(repoPath))
        val mailMap = when(mailMapType) {
            RepositoryAnalysisSpecification.MailMapType.Auto -> mailMapFactory.createMailMap(Paths.get(repoPath))
            RepositoryAnalysisSpecification.MailMapType.Disabled -> MailMap
        }
        val projectPath = git.firstRemoteUri()
        val commitsWithRanges = mapAllRangesToCommits(git, ranges)
        val formatter = DiffFormatter(NullOutputStream.INSTANCE)
        formatter.setRepository(git.repository)
        formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL)
        formatter.isDetectRenames = true
        val resultMap = mutableMapOf<Range, List<CodeChangesReportItem.CommitDiff>>()
        commitsWithRanges.forEach { (range, commits) ->
            val resultCommits = mutableListOf<CodeChangesReportItem.CommitDiff>()
            commits.forEach { commit ->
                if (commit.parents.isNotEmpty()) {
                    val prevCommit = commit.parents[0]
                    resultCommits += when (calculationType) {
                        CalculationType.FULL -> calculateFullDiff(
                            formatter,
                            prevCommit,
                            commit,
                            regex,
                            mailMap
                        )

                        CalculationType.AUTHORS -> calculateOnlyAuthor(commit, prevCommit, mailMap)
                    }

                } else {
                    // this is either the initial commit or an orphan one
                    // in both cases there's no need to track them
                    // initial commit doesn't count because we track increments
                    // orphans will be counted eventually when they are merged to target branch
                }
            }
            resultMap[range] = resultCommits
                .filter { explicitAuthors.isEmpty() || explicitAuthors.contains(it.authorEmailAddress) }
        }
        return projectPath to resultMap.flatMap {
            listOf(
                CodeChangesReportItem.DiffWithRanges(
                    it.key, it.value, statisticsData = calculatePercentileData(
                        it.value,
                        percentile
                    )
                )
            )
        }
    }

    private fun calculateOnlyAuthor(
        commit: RevCommit,
        prevCommit: RevCommit,
        mailMap: MailMap
    ): CodeChangesReportItem.CommitDiff {
        val (_, email) = mailMap.mapIdentity(
            commit.authorIdent.emailAddress,
            commit.authorIdent.name
        )

        return CodeChangesReportItem.CommitDiff(
            prevCommit.name,
            commit.name,
            0,
            0,
            0,
            0,
            email
        )
    }

    private fun calculatePercentileData(
        diffs: List<CodeChangesReportItem.CommitDiff>, percentile: Percentile
    ): CodeChangesReportItem.DiffWithRanges.StatisticsData {
        return CodeChangesReportItem.DiffWithRanges.StatisticsData(
            percentile = percentile,
            linesAdded = calculatePercentileByColumn(
                PercentileColumn.LinesAdded,
                diffs,
                percentile
            ),
            totalChanged = calculatePercentileByColumn(
                PercentileColumn.TotalChanges, diffs,
                percentile
            ),
            codeIncrement = calculatePercentileByColumn(
                PercentileColumn.TotalIncrement, diffs,
                percentile
            ),
            addedAvg = calculateAvgByColumn(PercentileColumn.LinesAdded, diffs)
        )
    }

    private fun calculateAvgByColumn(
        column: PercentileColumn,
        diffs: List<CodeChangesReportItem.CommitDiff>
    ): Int {
        return targetValues(column, diffs).average().toInt()
    }

    private fun calculatePercentileByColumn(
        column: PercentileColumn,
        diffs: List<CodeChangesReportItem.CommitDiff>,
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
        diffs: List<CodeChangesReportItem.CommitDiff>
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

    private fun calculateFullDiff(
        formatter: DiffFormatter,
        prevCommit: RevCommit,
        currentCommit: RevCommit,
        regex: Regex?,
        mailMap: MailMap
    ): CodeChangesReportItem.CommitDiff {
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

        val (_, email) = mailMap.mapIdentity(
            currentCommit.authorIdent.emailAddress,
            currentCommit.authorIdent.name
        )
        return CodeChangesReportItem.CommitDiff(
            prevCommit.name,
            currentCommit.name,
            localAdd,
            localRemove,
            ignoredAdd,
            ignoredRemove,
            email
        )
    }

    private fun Git.firstRemoteUri() = remoteList().call()[0].urIs[0].toString()

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

}