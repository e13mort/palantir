package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.AllProjectsReport
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table

class ASCIITableProjectsListRender : ReportRender<AllProjectsReport, String> {
    override fun render(value: AllProjectsReport) : String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name")
                value.walk {
                    row(it.id(), it.name())
                }
            }
        }.toString()
    }

}