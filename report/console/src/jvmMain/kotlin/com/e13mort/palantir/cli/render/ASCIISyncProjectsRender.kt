package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.SyncInteractor.SyncResult
import com.e13mort.palantir.render.ReportRender
import com.jakewharton.picnic.table

class ASCIISyncProjectsRender : ReportRender<SyncResult, String, Unit> {
    override fun render(value: SyncResult, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            when (val state = value.state) {
                is SyncResult.State.Done -> {
                    row("Projects updated")
                    row(state.itemsUpdated)
                }

                is SyncResult.State.InProgress -> {
                    when (state.state) {
                        SyncResult.State.ProgressState.LOADING -> {
                            row("Loading remote projects...")
                        }

                        SyncResult.State.ProgressState.SAVING -> {
                            row("Saving remote projects...")
                        }

                        SyncResult.State.ProgressState.COMPLEX -> Unit
                    }
                }

                SyncResult.State.Pending -> {
                    row("Waiting for sync start...")
                }

                SyncResult.State.Skipped -> {
                    row("Project sync skipped...")
                }

                SyncResult.State.Removed -> {
                    row("Project removed")
                }
            }
        }.toString()
    }
}