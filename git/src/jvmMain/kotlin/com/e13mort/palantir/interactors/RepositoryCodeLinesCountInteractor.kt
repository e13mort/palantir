package com.e13mort.palantir.interactors

import com.e13mort.palantir.cloc.ClocAdapter
import com.e13mort.palantir.cloc.LanguageReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import java.io.File
import java.io.FileReader

class RepositoryCodeLinesCountInteractor(
    private val clocAdapter: ClocAdapter
) :
    Interactor<Pair<String, List<Range>>, RepositoryCodeLinesCountReport> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryCodeLinesCountReport> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification =
                tryReadSpecFromFile(argPath)

            val result = mutableListOf<RepositoryReport.GroupedResults<CodeLinesResult>>()
            if (specification != null) {
                createReportsForSpecification(specification, ranges, result)
            } else {
                createReportsForSingleGitRepo(argPath, ranges, result)
            }

            emit(CodeLinesReportImpl(result))
        }
    }

    private fun createReportsForSingleGitRepo(
        repoPath: String,
        ranges: List<Range>,
        fullResult: MutableList<RepositoryReport.GroupedResults<CodeLinesResult>>
    ) {

        val reportResultPair = calculateReport(repoPath, ranges)
        fullResult += RepositoryReport.GroupedResults("single", mapOf(reportResultPair))
    }

    private fun calculateLinesCount(
        git: Git,
        revCommit: RevCommit?,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec?
    ): Map<String, LanguageReport> {
        if (revCommit == null) return emptyMap()
        git.checkout()
            .setForced(true)
            .setName(revCommit.name)
            .call()
        return clocAdapter.calculate(git.repository.workTree.path, linesSpec?.excludedPaths ?: emptyList())
    }

    private fun createReportsForSpecification(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        fullResults: MutableList<RepositoryReport.GroupedResults<CodeLinesResult>>
    ) {
        specification.projects.forEach { (groupName, projects) ->
            val result = mutableMapOf<String, List<RepositoryCodeLinesCountReport.LinesCountReportItem>>()
            projects.forEach { projectSpec ->
                val localPath = projectSpec.localPath
                val report = calculateReport(localPath, ranges, projectSpec.linesSpec)
                result[report.first] = report.second
            }
            fullResults += RepositoryReport.GroupedResults(groupName, result)
        }
    }

    private fun calculateReport(
        repoPath: String,
        ranges: List<Range>,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec? = null
    ): Pair<String, MutableList<RepositoryCodeLinesCountReport.LinesCountReportItem>> {
        val result = mutableListOf<RepositoryCodeLinesCountReport.LinesCountReportItem>()

        val git: Git = Git.open(File(repoPath))
        val head = git.repository.findRef("HEAD").target.name
        val rangesToCommits = mapRangesToCommits(git, ranges)
        rangesToCommits.forEach { (range, rev) ->
            val calculateLinesCount = calculateLinesCount(git, rev, linesSpec).mapValues {
                RepositoryCodeLinesCountReport.LinesCountItem(it.value.code)
            }
            result += RepositoryCodeLinesCountReport.LinesCountReportItem(
                range,
                calculateLinesCount
            )
        }
        git.checkout()
            .setForced(true)
            .setName(head)
            .call()

        return git.firstRemoteUri() to result
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

    private fun mapRangesToCommits(git: Git, ranges: List<Range>): Map<Range, RevCommit?> {
        val result = mutableMapOf<Range, RevCommit?>()
        ranges.forEachIndexed { index, range ->
            val revCommits = git.log()
                .setRevFilter(CommitTimeRevFilter.between(range.start, range.end))
                .call().toList()
            if (revCommits.isEmpty()) {
                result[range] = null
            } else {
                val revCommit = if (index == 0) revCommits.last() else revCommits.first()
                result[range] = revCommit
            }
        }
        return result
    }

    data class CodeLinesReportImpl(
        override val result: List<RepositoryReport.GroupedResults<CodeLinesResult>> = emptyList()
    ) : RepositoryCodeLinesCountReport

}