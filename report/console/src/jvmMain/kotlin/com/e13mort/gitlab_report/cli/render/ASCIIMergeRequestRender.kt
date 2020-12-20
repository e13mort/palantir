package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.PrintMergeRequestInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table

class ASCIIMergeRequestRender : ReportRender<PrintMergeRequestInteractor.MergeRequestsReport, String> {
    override fun render(value: PrintMergeRequestInteractor.MergeRequestsReport): String {
        return table {
            cellStyle {
                border = true
            }
            row("Id", value.id)
            row("State", value.state)
            row("From", value.from)
            row("To", value.to)
            row("Created", value.createdMillis.formatAsDate())
            value.closedMillis?.let {
                row("Closed", it.formatAsDate())
            }
        }.toString()
    }
}