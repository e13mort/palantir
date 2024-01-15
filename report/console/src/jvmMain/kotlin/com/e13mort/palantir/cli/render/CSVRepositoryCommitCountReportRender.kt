package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.RepositoryCommitsReport
import com.e13mort.palantir.interactors.totalCommitsCount
import com.e13mort.palantir.interactors.uniqueAuthors
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVRepositoryCommitCountReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryCommitsReport, String, Unit> {
    override fun render(value: RepositoryCommitsReport, params: Unit): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                groupedResult.result.forEach { (repo, reports: List<RepositoryCommitsReport.RangeReportItem>) ->
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
                        append(report.uniqueAuthorsEmails.size)
                        append("\n")
                    }
                    if (reports.isEmpty())
                        append("\n")

                    append("Total")
                    append(",")
                    append(reports.totalCommitsCount())
                    append(",")
                    append(reports.uniqueAuthors().size)
                    append("\n")
                }
                append("Group: ${groupedResult.groupName}")
                append(",")
                append(groupedResult.totalCommitsCount())
                append(",")
                append(groupedResult.totalAuthors().size)
                append("\n")
            }
        }.toString()
    }
}