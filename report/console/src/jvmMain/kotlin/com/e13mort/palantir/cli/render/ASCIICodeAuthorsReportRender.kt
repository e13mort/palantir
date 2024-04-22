package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.CodeChangesReportItem
import com.e13mort.palantir.interactors.RepositoryReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.RowDsl
import com.jakewharton.picnic.table

class ASCIICodeAuthorsReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryReport<CodeChangesReportItem>, String, Set<CodeChangesReportParams>> {
    override fun render(
        value: RepositoryReport<CodeChangesReportItem>,
        params: Set<CodeChangesReportParams>
    ): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                row {
                    cell(groupedResult.groupName) {
                        columnSpan = 2
                    }
                }
                groupedResult.result.commitDiffs.forEach { diff ->
                    row {
                        cell(diff.key) {
                            columnSpan = 2
                        }
                    }
                    row {
                        cell("Range")
                        cell("Authors count")
                    }
                    diff.value.forEach {
                        row {
                            appendDiffCells(it)
                        }
                    }
                }
                row {
                    cell("Summary: ${groupedResult.groupName}") {
                        columnSpan = 2
                    }
                }
                val summary = groupedResult.result.summary.rangedData.values
                summary.forEach {
                    row {
                        appendDiffCells(it)
                    }
                }
                val total = groupedResult.result.summary.total
                row {
                    appendDiffCells(total)
                }
                row {
                    cell("Authors") {
                        columnSpan = 2
                    }
                }
                total.uniqueAuthors().forEach { author ->
                    row {
                        cell(author) {
                            columnSpan = 2
                        }
                    }
                }

            }
        }.toString()
    }

    private fun RowDsl.appendDiffCells(it: CodeChangesReportItem.DiffWithRanges) {
        cell(it.range.asString(formatter))
        cell(it.uniqueAuthors().size)
    }

}