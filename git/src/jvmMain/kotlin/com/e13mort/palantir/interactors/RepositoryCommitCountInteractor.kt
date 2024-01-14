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

            val result = mutableMapOf<String, List<RepositoryCommitsReport.RangeReportItem>>()
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
        result: MutableMap<String, List<RepositoryCommitsReport.RangeReportItem>>
    ) {
        val git: Git = Git.open(File(repoPath))
        val rangesReports = mutableListOf<RepositoryCommitsReport.RangeReportItem>()
        ranges.forEach {
            val rangeReportItem = calculateReport(git, it)
            rangesReports += rangeReportItem
        }
        result[git.firstRemoteUri()] = rangesReports
    }

    private fun createReportsForSpecification(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        result: MutableMap<String, List<RepositoryCommitsReport.RangeReportItem>>
    ) {
        specification.projects.forEach { (_, projects) ->
            projects.forEach { projectSpec ->
                val rangesReports = mutableListOf<RepositoryCommitsReport.RangeReportItem>()
                val git: Git = Git.open(File(projectSpec.localPath))
                ranges.forEach {
                    val rangeReportItem = calculateReport(git, it)
                    rangesReports += rangeReportItem
                }
                result[git.firstRemoteUri()] = rangesReports
            }
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
        return RepositoryCommitsReport.RangeReportItem(range, commitsCount, authors.size)
    }

    data class RepositoryCommitsReportImpl(
        override val result: Map<String, List<RepositoryCommitsReport.RangeReportItem>> = emptyMap()
    ) : RepositoryCommitsReport

}