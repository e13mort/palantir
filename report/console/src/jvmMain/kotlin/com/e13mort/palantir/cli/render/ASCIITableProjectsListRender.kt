package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.AllProjectsResult
import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.model.Project
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.table

class ASCIITableProjectsListRender(
    private val showFullInfo: Boolean
) : ReportRender<AllProjectsResult, String> {
    override fun render(value: AllProjectsResult) : String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Synced")
                printProjects(value, true)
                printProjects(value, false)
            }
        }.toString()
    }

    private fun TableSectionDsl.printProjects(value: AllProjectsResult, synced: Boolean) {
        value.projects(synced).forEach {
            printProject(it, synced)
        }
    }

    private fun TableSectionDsl.printProject(
        project: Project,
        synced: Boolean
    ) {
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

    private fun renderFullProjectInfo(project: Project) =
        StringBuilder()
            .appendLine("name: ${project.name()}")
            .appendLine("ssh: ${project.clonePaths().ssh()}")
            .append("http: ${project.clonePaths().http()}")
            .toString()

}