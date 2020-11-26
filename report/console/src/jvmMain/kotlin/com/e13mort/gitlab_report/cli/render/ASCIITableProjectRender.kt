package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.ProjectSummary
import com.e13mort.gitlab_report.interactors.ReportRender
import com.jakewharton.picnic.table

class ASCIITableProjectRender : ReportRender<ProjectSummary, String> {
    override fun render(value: ProjectSummary) : String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Branches", "MRs")
                value.project().let {
                    row(it.id(), it.name(), value.branchCount(), value.mergeRequestCount())
                }
            }
        }.toString()
    }

}