package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCodeLinesCountReport
import com.e13mort.palantir.interactors.allKeys
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.table

class ASCIICodeLinesCountReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCodeLinesCountReport, String, Unit> {
    override fun render(value: RepositoryCodeLinesCountReport, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                groupedResult.result.forEach { (repo, reports: List<RepositoryCodeLinesCountReport.LinesCountReportItem>) ->
                    val allColumns = reports.allKeys()
                    row {
                        cell(repo) {
                            columnSpan = allColumns.size + 1
                        }
                    }
                    row {
                        cell("Range")
                        allColumns.forEach {
                            cell(it)
                        }
                    }
                    reports.forEach { report ->
                        row {
                            cell(report.range.asString(formatter))
                            allColumns.forEach { columnName ->
                                cell(report.lines[columnName]?.codeLines ?: "0")
                            }
                        }
                    }
                }
            }
        }.toString()
    }

}