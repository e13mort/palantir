package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeChangesReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.table

class ASCIICodeChangesReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeChangesReport, String, Unit> {
    override fun render(value: RepositoryCodeChangesReport, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                row {
                    cell(groupedResult.groupName) {
                        columnSpan = 5
                    }
                }
                groupedResult.result.commitDiffs.forEach { diff ->
                    row {
                        cell(diff.key) {
                            columnSpan = 5
                        }
                    }
                    row {
                        cell("Range")
                        cell("Added")
                        cell("Removed")
                        cell("Code increment")
                        cell("Total")
                    }
                    diff.value.forEach {
                        row {
                            cell(it.range.asString(formatter))
                            cell(it.totalAdded())
                            cell(it.totalRemoved())
                            cell(it.codeIncrement())
                            cell(it.totalChanged())
                        }
                    }
                }

            }
        }.toString()
    }

}