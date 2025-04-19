/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.CodeChangesReportItem
import com.e13mort.palantir.interactors.RepositoryReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVCodeAuthorsReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryReport<CodeChangesReportItem>, String, Unit> {
    override fun render(
        value: RepositoryReport<CodeChangesReportItem>,
        params: Unit
    ): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                appendGroupHeader(groupedResult.groupName)
                groupedResult.result.commitDiffs.forEach { diff ->
                    appendTitle(diff)
                    diff.value.forEach { diffWithRanges ->
                        appendDiffChanges(diffWithRanges)
                    }
                }
                appendGroupHeader("Summary: ${groupedResult.groupName}")
                groupedResult.result.summary.let { summary ->
                    summary.rangedData.values.forEach { diffWithRanges ->
                        appendDiffChanges(diffWithRanges)
                    }
                    appendDiffChanges(summary.total)
                    summary.total.uniqueAuthors().forEach { author ->
                        append(author)
                        append(",")
                        append("\n")
                    }
                }
            }
        }.toString()
    }

    private fun StringBuilder.appendGroupHeader(
        title: String
    ) {
        append(title)
        append(",")
        append("\n")
    }

    private fun StringBuilder.appendTitle(diff: Map.Entry<String, List<CodeChangesReportItem.DiffWithRanges>>) {
        append(diff.key)
        append(",")
        append("Authors Count")
        append("\n")
    }

    private fun StringBuilder.appendDiffChanges(diffWithRanges: CodeChangesReportItem.DiffWithRanges) {
        append(diffWithRanges.range.asString(formatter))
        append(",")
        append(diffWithRanges.uniqueAuthors().size)
        append("\n")
    }

}