package com.e13mort.palantir.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import java.io.File
import java.io.FileReader

class RepositoryCommitCountInteractor :
    Interactor<Pair<String, List<Range>>, RepositoryCommitsReport> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryCommitsReport> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification =
                tryReadSpecFromFile(argPath)

            val result = mutableListOf<RepositoryCommitsReport.GroupedResults>()
            if (specification != null) {
                createReportsForSpecification(specification, ranges, result)
            } else {
                createReportsForSingleGitRepo(argPath, ranges, result)
            }

            emit(RepositoryCommitsReportImpl(result))
        }
    }

    private fun createReportsForSingleGitRepo(
        repoPath: String,
        ranges: List<Range>,
        fullResult: MutableList<RepositoryCommitsReport.GroupedResults>
    ) {
        val git: Git = Git.open(File(repoPath))
        val rangesReports = mutableListOf<RepositoryCommitsReport.RangeReportItem>()
        val result = mutableMapOf<String, List<RepositoryCommitsReport.RangeReportItem>>()
        ranges.forEach {
            val rangeReportItem = calculateReport(git, it)
            rangesReports += rangeReportItem
        }
        result[git.firstRemoteUri()] = rangesReports
        fullResult += RepositoryCommitsReport.GroupedResults("single", result)
    }

    private fun createReportsForSpecification(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        fullResults: MutableList<RepositoryCommitsReport.GroupedResults>
    ) {
        specification.projects.forEach { (groupName, projects) ->
            val result = mutableMapOf<String, List<RepositoryCommitsReport.RangeReportItem>>()
            projects.forEach { projectSpec ->
                val rangesReports = mutableListOf<RepositoryCommitsReport.RangeReportItem>()
                val git: Git = Git.open(File(projectSpec.localPath))
                projectSpec.targetBranch?.let {
                    git
                        .checkout()
                        .setName(it)
                        .setCreateBranch(false)
                        .setForced(true)
                        .call()
                }
                ranges.forEach {
                    val rangeReportItem = calculateReport(git, it)
                    rangesReports += rangeReportItem
                }
                result[git.firstRemoteUri()] = rangesReports
            }
            fullResults += RepositoryCommitsReport.GroupedResults(groupName, result)
        }
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

    private fun calculateReport(git: Git, range: Range): RepositoryCommitsReport.RangeReportItem {
        val log = git.log()
            .setRevFilter(
                CommitTimeRevFilter.between(
                    range.start,
                    range.end
                )
            )
        val revCommits = log.call()
        val authors = mutableSetOf<String>()
        var commitsCount = 0
        revCommits.forEach {
            commitsCount++
            authors += it.authorIdent.emailAddress
        }
        return RepositoryCommitsReport.RangeReportItem(range, commitsCount, authors)
    }

    data class RepositoryCommitsReportImpl(
        override val result: List<RepositoryCommitsReport.GroupedResults> = emptyList()
    ) : RepositoryCommitsReport

}