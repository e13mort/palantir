package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.PrintMergeRequestInteractor
import com.e13mort.gitlab_report.interactors.ReportRender
import com.e13mort.gitlab_report.model.User
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
            value.assignees.let {
                if (it.isEmpty()) {
                    row {
                        cell("Empty assignees") {
                            columnSpan = 2
                        }
                    }
                } else {
                    row {
                        cell("Assignees") {
                            rowSpan = it.size
                        }
                        cell(format(it[0]))
                    }
                    it.forEachIndexed { index, user ->
                        if (index > 0) {
                            row(format(user))
                        }
                    }
                }

            }
        }.toString()
    }

    private fun format(user: User) = "${user.name()}<${user.userName()}>"
}