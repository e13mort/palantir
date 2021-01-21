package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.interactors.SyncInteractor
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