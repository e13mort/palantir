package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCommitsReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.table

class ASCIIRepositoryCommitCountReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCommitsReport, String, Unit> {
    override fun render(value: RepositoryCommitsReport, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { (repo, reports: List<RepositoryCommitsReport.RangeReportItem>) ->
                row {
                    cell(repo) {
                        columnSpan = 3
                    }
                }
                row {
                    cell("Range")
                    cell("Commits count")
                    cell("Authors count")
                }
                reports.forEach { report ->
                    row {
                        cell(report.range.asString(formatter))
                        cell(report.commitsCount)
                        cell(report.uniqueAuthorsEmailsCount)
                    }
                }
            }
        }.toString()
    }

}