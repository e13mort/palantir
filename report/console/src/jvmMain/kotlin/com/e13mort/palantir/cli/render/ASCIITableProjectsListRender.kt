package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.AllProjectsResult
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.model.Project
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.table

class ASCIITableProjectsListRender : ReportRender<AllProjectsResult, String, Boolean> {

    override fun render(value: AllProjectsResult, showExtendedInfo: Boolean): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Id", "Project Name", "Synced")
                printProjects(value, true, showExtendedInfo)
                printProjects(value, false, showExtendedInfo)
            }
        }.toString()
    }

    private fun TableSectionDsl.printProjects(
        value: AllProjectsResult,
        synced: Boolean,
        showExtendedInfo: Boolean
    ) {
        value.projects(synced).forEach {
            printProject(it, synced, showExtendedInfo)
        }
    }

    private fun TableSectionDsl.printProject(
        project: Project,
        synced: Boolean,
        showExtendedInfo: Boolean
    ) {
        if (showExtendedInfo) {
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