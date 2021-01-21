package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.AllProjectsReport
import com.e13mort.palantir.interactors.ReportRender
import com.jakewharton.picnic.table

class ASCIITableProjectsListRender : ReportRender<AllProjectsReport, String> {
    override fun render(value: AllProjectsReport) : String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Synced")
                value.walk { project, synced ->
                    row(project.id(), project.name(), synced)
                }
            }
        }.toString()
    }

}