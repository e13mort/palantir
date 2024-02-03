package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeChangesReport
import com.e13mort.palantir.interactors.firstItemPercentile
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVCodeChangesReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeChangesReport, String, Set<CodeChangesReportParams>> {
    override fun render(value: RepositoryCodeChangesReport, params: Set<CodeChangesReportParams>): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                appendGroupHeader(groupedResult.groupName)
                groupedResult.result.commitDiffs.forEach { diff ->
                    appendTitle(diff)
                    diff.value.forEach { diffWithRanges ->
                        appendDiffChanges(diffWithRanges)
                        appendAdditionalInfo(params, diffWithRanges)
                    }
                }
                appendGroupHeader("Summary: ${groupedResult.groupName}")
                groupedResult.result.summary.let {
                    it.rangedData.values.forEach { diffWithRanges ->
                        appendDiffChanges(diffWithRanges)
                    }
                    appendDiffChanges(it.total)
                }
            }
        }.toString()
    }

    private fun StringBuilder.appendGroupHeader(
        title: String
    ) {
        append(title)
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append("\n")
    }

    private fun StringBuilder.appendAdditionalInfo(
        params: Set<CodeChangesReportParams>,
        diffWithRanges: RepositoryCodeChangesReport.DiffWithRanges
    ) {
        if (params.contains(CodeChangesReportParams.ShowFullCommitsList)) {
            append("Commit")
            append(",")
            append("Added")
            append(",")
            append("Removed")
            append(",")
            append("Added(Ignored)")
            append(",")
            append("Removed(Ignored)")
            append(",")
            append("Effective code increment")
            append(",")
            append("Effective total changes")
            append(",")
            append("Author Email")
            append(",")
            append(",")
            append("\n")
            diffWithRanges.diffs.forEach {
                append(it.targetCommitSHA1)
                append(",")
                append(it.linesAdded)
                append(",")
                append(it.linesRemoved)
                append(",")
                append(it.ignoredLinesAdd)
                append(",")
                append(it.ignoredLinesRemove)
                append(",")
                append(it.codeIncrement())
                append(",")
                append(it.totalChanges())
                append(",")
                append(it.authorEmailAddress)
                append(",")
                append("\n")
            }
        }
    }

    private fun StringBuilder.appendTitle(diff: Map.Entry<String, List<RepositoryCodeChangesReport.DiffWithRanges>>) {
        val percentileTitle = diff.value.firstItemPercentile()?.name ?: "invalid"
        append(diff.key)
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append(",")
        append("\n")
        append("Range")
        append(",")
        append("Commits Count")
        append(",")
        append("Added")
        append(",")
        append("Removed")
        append(",")
        append("Code Increment")
        append(",")
        append("Total")
        append(",")
        append("Added ($percentileTitle)")
        append(",")
        append("Added (avg)")
        append(",")
        append("Authors Count")
        append("\n")
    }

    private fun StringBuilder.appendDiffChanges(diffWithRanges: RepositoryCodeChangesReport.DiffWithRanges) {
        append(diffWithRanges.range.asString(formatter))
        append(",")
        append(diffWithRanges.diffs.size)
        append(",")
        append(diffWithRanges.totalAdded())
        append(",")
        append(diffWithRanges.totalRemoved())
        append(",")
        append(diffWithRanges.codeIncrement())
        append(",")
        append(diffWithRanges.totalChanged())
        diffWithRanges.statisticsData.let { percentileData ->
            append(",")
            append(percentileData.linesAdded)
            append(",")
            append(percentileData.addedAvg)
        }
        append(",")
        append(diffWithRanges.uniqueAuthors().size)
        append("\n")
    }

}