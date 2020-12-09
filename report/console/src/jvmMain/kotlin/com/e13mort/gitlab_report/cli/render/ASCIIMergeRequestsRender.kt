package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.PrintProjectMergeRequestsInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.*

class ASCIIMergeRequestsRender : ReportRender<PrintProjectMergeRequestsInteractor.MergeRequestsReport, String> {
    override fun render(value: PrintProjectMergeRequestsInteractor.MergeRequestsReport): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("ID", "From", "To", "Created", "State")
            }
            runBlocking {
                value.walk { id: String, sourceBranch: String, targetBranch: String, created: Long, state: String ->
                    val formattedDate = DateFormat.getInstance().format(Date(created))
                    row(id, sourceBranch, targetBranch, formattedDate, state)
                }
            }
        }.toString()
    }
}