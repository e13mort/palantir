package com.e13mort.palantir.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.AndRevFilter
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.util.io.NullOutputStream
import java.io.File
import java.io.FileReader

class RepositoryCodeIncrementInteractor :
    Interactor<Pair<String, List<Range>>, RepositoryCodeChangesReport> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryCodeChangesReport> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification =
                tryReadSpecFromFile(argPath)

            val result = mutableListOf<RepositoryCodeChangesReport.GroupedResults>()
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
        fullResult: MutableList<RepositoryCodeChangesReport.GroupedResults>
    ) {

        val reportResultPair = calculateReport(repoPath, ranges)
        val changesReportItem = RepositoryCodeChangesReport.CodeChangesReportItem(
            mapOf(reportResultPair)
        )
        fullResult += RepositoryCodeChangesReport.GroupedResults(
            "single",
            changesReportItem
        )
    }

    private fun createReportsForSpecification(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        fullResults: MutableList<RepositoryCodeChangesReport.GroupedResults>
    ) {
        specification.projects.forEach { (groupName, projects) ->
            val commitDiffs = mutableMapOf<String, List<RepositoryCodeChangesReport.DiffWithRanges>>()
            projects.forEach { projectSpec ->
                val localPath = projectSpec.localPath
                val report: Pair<String, List<RepositoryCodeChangesReport.DiffWithRanges>> = calculateReport(localPath, ranges, projectSpec.linesSpec)
                commitDiffs[report.first] = report.second
            }
            fullResults += RepositoryCodeChangesReport.GroupedResults(groupName, RepositoryCodeChangesReport.CodeChangesReportItem(commitDiffs))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun calculateReport(
        repoPath: String,
        ranges: List<Range>,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec? = null
    ): Pair<String, List<RepositoryCodeChangesReport.DiffWithRanges>> {

        val git: Git = Git.open(File(repoPath))
        val projectPath = git.firstRemoteUri()
        val commitsWithRanges = mapAllRangesToCommits(git, ranges)
        if (commitsWithRanges.size <= 1) {
            return projectPath to emptyList()
        }
        val formatter = DiffFormatter(NullOutputStream.INSTANCE)
        formatter.setRepository(git.repository)
        val resultMap = mutableMapOf<Range, List<RepositoryCodeChangesReport.CommitDiff>>()
        commitsWithRanges.forEach { (range, commits) ->
            val resultCommits = mutableListOf<RepositoryCodeChangesReport.CommitDiff>()
            commits.forEach { commit ->
                if (commit.parents.isNotEmpty()) {
                    val prevCommit = commit.parents[0]
                    val commitDiff = calculateDiff(formatter, prevCommit, commit)
                    resultCommits += commitDiff
                } else {
                    // TODO: track such commits
                }
            }
            resultMap[range] = resultCommits
        }
        return projectPath to resultMap.flatMap {
            listOf(RepositoryCodeChangesReport.DiffWithRanges(it.key, it.value))
        }
    }

    private fun calculateDiff(
        formatter: DiffFormatter,
        prevCommit: RevCommit,
        currentCommit: RevCommit
    ): RepositoryCodeChangesReport.CommitDiff {
        val diffEntries = formatter.scan(prevCommit, currentCommit)
        var localAdd = 0
        var localRemove = 0
        diffEntries.forEach { diffEntry ->
            val header = formatter.toFileHeader(diffEntry)
            val editList = header.toEditList()
            editList.forEach { edit ->
                localRemove += edit.lengthA
                localAdd += edit.lengthB
            }
        }
        return RepositoryCodeChangesReport.CommitDiff(
            prevCommit.name,
            currentCommit.name,
            localAdd,
            localRemove
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
        if (ranges.size <= 1) {
            throw IllegalArgumentException("Ranges $ranges should contain more than 1 value")
        }
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
        override val result: List<RepositoryCodeChangesReport.GroupedResults> = emptyList()
    ) : RepositoryCodeChangesReport

}