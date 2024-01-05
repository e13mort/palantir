package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.SyncInteractor
import com.e13mort.palantir.render.ReportRender
import com.jakewharton.picnic.table

class ASCIIFullSyncProjectsRender : ReportRender<SyncInteractor.SyncResult, String, Unit> {
    override fun render(value: SyncInteractor.SyncResult, params: Unit): String {
        return table {
            cellStyle {
                border = true
            }
            row {
                cell("Sync projects") {
                    columnSpan = 2
                }
            }
            value.visitActiveProjects { id, activeProject ->
                row {
                    cell("Sync project $id: ")
                    cell(value.state)
                }
                activeProject.let {
                    row {
                        cell("Branches:")
                        cell(it.branchesState)
                    }
                    row {
                        cell("MRs:")
                        cell(it.mrs.state)
                    }
                    it.mrs.visitActiveMRs { id, activeMr ->
                        row {
                            cell("MR: $id")
                            cell("State: $activeMr")
                        }
                    }
                }
            }
        }.toString()
    }

    private inline fun SyncInteractor.SyncResult.visitActiveProjects(visitor: (Long, SyncInteractor.SyncResult.ProjectSyncState) -> Unit) {
        projects.forEach {
            if (it.value.projectSyncState is SyncInteractor.SyncResult.State.InProgress) {
                visitor(it.key, it.value)
            }
        }
    }

    private inline fun SyncInteractor.SyncResult.MrsSyncState.visitActiveMRs(visitor: (Long, SyncInteractor.SyncResult.State) -> Unit) {
        mergeRequests.forEach {
            if (it.value is SyncInteractor.SyncResult.State.InProgress) visitor(it.key, it.value)
        }
    }
}