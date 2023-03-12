package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.AllProjectsReport
import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.model.Project
import com.jakewharton.picnic.table

class ASCIITableProjectsListRender(
    private val showFullInfo: Boolean
) : ReportRender<AllProjectsReport, String> {
    override fun render(value: AllProjectsReport) : String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Synced")
                value.walk { project, synced ->
                    if (showFullInfo) {
                        row(
                            project.id(),
                            renderFullProjectInfo(project),
                            synced
                        )
                    } else {
                        row(project.id(), project.name(), synced)
                    }
                }
            }
        }.toString()
    }

    private fun renderFullProjectInfo(project: Project) =
        StringBuilder()
            .appendLine("name: ${project.name()}")
            .appendLine("ssh: ${project.clonePaths().ssh()}")
            .append("http: ${project.clonePaths().http()}")
            .toString()

}