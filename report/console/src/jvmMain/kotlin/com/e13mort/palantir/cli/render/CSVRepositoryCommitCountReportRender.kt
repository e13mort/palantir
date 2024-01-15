package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCommitsReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.jakewharton.picnic.table

class CSVRepositoryCommitCountReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCommitsReport, String, Unit> {
    override fun render(value: RepositoryCommitsReport, params: Unit): String {
        return StringBuilder().apply {
            value.result.forEach { (repo, reports: List<RepositoryCommitsReport.RangeReportItem>) ->
                append(repo)
                append(",")
                append(",")
                append("\n")

                append("Range")
                append(",")
                append("Commits count")
                append(",")
                append("Authors count")
                append("\n")

                reports.forEach { report ->
                    append(report.range.asString(formatter))
                    append(",")
                    append(report.commitsCount)
                    append(",")
                    append(report.uniqueAuthorsEmailsCount)
                    append("\n")
                }
                append("\n")
            }
        }.toString()
    }
}