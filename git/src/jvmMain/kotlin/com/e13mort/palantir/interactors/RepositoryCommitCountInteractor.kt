package com.e13mort.palantir.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.eclipse.jgit.api.Git
import java.io.File

class RepositoryCommitCountInteractor :
    Interactor<Pair<String, List<Range>>, RepositoryCommitsReport> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryCommitsReport> {
        return flow {
            val repoPath = arg.first
            val git: Git = Git.open(File(repoPath))
            val result = mutableMapOf<String, List<RepositoryCommitsReport.RangeReportItem>>()
            val rangesReports = mutableListOf<RepositoryCommitsReport.RangeReportItem>()
            arg.second.forEach {
                val rangeReportItem = calculateReport(git, it)
                rangesReports += rangeReportItem
            }
            result[repoPath] = rangesReports
            emit(RepositoryCommitsReportImpl(result))
        }
    }

    private fun calculateReport(git: Git, range: Range): RepositoryCommitsReport.RangeReportItem {
        val log = git.log()
            .setRevFilter(
                org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter.between(
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