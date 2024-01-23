package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeChangesReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVCodeChangesReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeChangesReport, String, Set<CodeChangesReportParams>> {
    override fun render(value: RepositoryCodeChangesReport, params: Set<CodeChangesReportParams>): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                append(groupedResult.groupName)
                append(",")
                append("\n")
                groupedResult.result.commitDiffs.forEach { diff ->
                    append(diff.key)
                    append(",")
                    append("\n")
                    append("Range")
                    append(",")
                    append("Added")
                    append(",")
                    append("Removed")
                    append(",")
                    append("Code increment")
                    append(",")
                    append("Total")
                    append("\n")
                    diff.value.forEach { diffWithRanges ->
                        append(diffWithRanges.range.asString(formatter))
                        append(",")
                        append(diffWithRanges.totalAdded())
                        append(",")
                        append(diffWithRanges.totalRemoved())
                        append(",")
                        append(diffWithRanges.codeIncrement())
                        append(",")
                        append(diffWithRanges.totalChanged())
                        append("\n")
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
                                append("\n")
                            }
                        }
                    }
                }

            }
        }.toString()
    }

}