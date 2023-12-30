package com.e13mort.palantir.cli.render

import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.interactors.SyncInteractor
import com.jakewharton.picnic.table

class ASCIISyncProjectsRender : ReportRender<SyncInteractor.SyncResult, String, Unit> {
    override fun render(value: SyncInteractor.SyncResult, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            if (value.projectsUpdated == -1L) { //fixme
                row("Sync in process...")
            } else {
                row("Projects updated")
                row(value.projectsUpdated)
            }
        }.toString()
    }
}