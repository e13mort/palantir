package com.e13mort.gitlab_report.cli.render

import com.e13mort.gitlab_report.interactors.ReportRender
import com.e13mort.gitlab_report.interactors.SyncInteractor
import com.jakewharton.picnic.table

class ASCIISyncProjectsRender : ReportRender<SyncInteractor.SyncResult, String> {
    override fun render(value: SyncInteractor.SyncResult): String {
        return table {
            cellStyle {
                border = true
            }
            row("Projects updated")
            row(value.projectsUpdated)
        }.toString()
    }
}