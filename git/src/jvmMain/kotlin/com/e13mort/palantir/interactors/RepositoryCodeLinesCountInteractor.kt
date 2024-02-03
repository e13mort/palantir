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
    Interactor<Pair<String, List<Range>>, RepositoryReport<CodeLinesResult>> {
    override fun run(arg: Pair<String, List<Range>>): Flow<RepositoryReport<CodeLinesResult>> {
        return flow {
            val argPath = arg.first
            val ranges = arg.second

            val specification = createSpec(argPath)
            val result = createReportsForSpecification(specification, ranges)
            emit(RepositoryReport(result))
        }
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
        ranges: List<Range>
    ): List<RepositoryReport.GroupedResults<CodeLinesResult>> {
        val fullResults = mutableListOf<RepositoryReport.GroupedResults<CodeLinesResult>>()
        specification.projects.forEach { (groupName, projects) ->
            val result = mutableMapOf<String, List<LinesCountReportItem>>()
            projects.forEach { projectSpec ->
                val localPath = projectSpec.localPath
                val report = calculateReport(localPath, ranges, projectSpec.linesSpec)
                result[report.first] = report.second
            }
            fullResults += RepositoryReport.GroupedResults(groupName, result)
        }
        return fullResults
    }

    private fun calculateReport(
        repoPath: String,
        ranges: List<Range>,
        linesSpec: RepositoryAnalysisSpecification.LinesSpec? = null
    ): Pair<String, MutableList<LinesCountReportItem>> {
        val result = mutableListOf<LinesCountReportItem>()

        val git: Git = Git.open(File(repoPath))
        val head = git.repository.findRef("HEAD").target.name
        val rangesToCommits = mapRangesToCommits(git, ranges)
        rangesToCommits.forEach { (range, rev) ->
            val calculateLinesCount = calculateLinesCount(git, rev, linesSpec).mapValues {
                LinesCountReportItem.LinesCountItem(it.value.code)
            }
            result += LinesCountReportItem(
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

    private suspend fun createSpec(filePath: String): RepositoryAnalysisSpecification {
        val file = File(filePath)
        return if (file.isDirectory) {
            RepositoryAnalysisSpecification(mapOf("single" to listOf(RepositoryAnalysisSpecification.ProjectSpecification(filePath))))
        } else {
            readSpec(file)
        }
    }

    private suspend fun readSpec(file: File): RepositoryAnalysisSpecification {
        val fileContent = withContext(Dispatchers.IO) {
            FileReader(file).readText()
        }
        return RepositoryAnalysisSpecification.fromString(fileContent) ?: throw IllegalArgumentException("Failed to create spec from file ${file.absolutePath}")
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
}