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
            when(val state = value.state) {
                is SyncInteractor.SyncResult.State.Done -> {
                    row("Projects updated")
                    row(state.itemsUpdated)
                }
                is SyncInteractor.SyncResult.State.InProgress -> {
                    when(state.state) {
                        SyncInteractor.SyncResult.State.ProgressState.LOADING -> {
                            row("Loading remote projects...")
                        }
                        SyncInteractor.SyncResult.State.ProgressState.SAVING -> {
                            row("Saving remote projects...")
                        }
                    }
                }
                SyncInteractor.SyncResult.State.Pending -> {
                    row("Waiting for sync start...")
                }
                SyncInteractor.SyncResult.State.Skipped -> {
                    row("Project sync skipped...")
                }
            }
        }.toString()
    }
}