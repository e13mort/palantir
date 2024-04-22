package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.CodeLinesResult
import com.e13mort.palantir.interactors.LinesCountReportItem
import com.e13mort.palantir.interactors.RepositoryReport
import com.e13mort.palantir.interactors.allKeys
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVCodeLinesCountReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryReport<CodeLinesResult>, String, Unit> {
    override fun render(value: RepositoryReport<CodeLinesResult>, params: Unit): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                groupedResult.result.forEach { (repo, reports: List<LinesCountReportItem>) ->
                    val allColumns = reports.allKeys()
                    append(repo)
                    append("\n")

                    append("Range")
                    append(",")
                    allColumns.forEach {
                        append(it)
                        append(",")
                    }
                    append("\n")
                    reports.forEach { report ->
                        append(report.range.asString(formatter))
                        append(",")
                        allColumns.forEach { columnName ->
                            append(report.lines[columnName]?.codeLines)
                            append(",")
                        }
                        append("\n")
                    }
                }
            }
        }.toString()
    }

}