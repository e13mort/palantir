/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.cloc.ClocAdapter
import com.e13mort.palantir.cloc.LanguageReport
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import java.io.File

class CodeLinesReportCalculator(
    private val clocAdapter: ClocAdapter
) : RepositoryAnalyticsInteractor.RepositoryReportCalculator<CodeLinesResult> {
    override suspend fun calculateReport(
        specification: RepositoryAnalysisSpecification,
        ranges: List<Range>,
        singleGroupName: String?,
        itemIndexInGroup: Int?
    ): List<RepositoryReport.GroupedResults<CodeLinesResult>> {
        val fullResults = mutableListOf<RepositoryReport.GroupedResults<CodeLinesResult>>()
        specification.projects.filter {
                if (singleGroupName != null) {
                    singleGroupName == it.key
                } else true
            }.forEach { (groupName, projects) ->
                val result = mutableMapOf<String, List<LinesCountReportItem>>()
                projects.forEachIndexed { index, projectSpec ->
                    if (itemIndexInGroup == null || index == itemIndexInGroup) {
                        val localPath = projectSpec.localPath
                        val report = calculateReport(localPath, ranges, projectSpec.linesSpec)
                        result[report.first] = report.second
                    }
                }
                if (result.isNotEmpty()) {
                    fullResults += RepositoryReport.GroupedResults(groupName, result)
                }
            }
        return fullResults
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
        return clocAdapter.calculate(
            git.repository.workTree.path,
            linesSpec?.excludedPaths ?: emptyList()
        )
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