/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.SyncInteractor
import com.e13mort.palantir.render.ReportRender
import com.jakewharton.picnic.TableDsl
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
            value.projects.forEach { (id, project) ->
                renderProject(id, value, project)
            }
        }.toString()
    }

    private fun TableDsl.renderProject(
        id: Long,
        value: SyncInteractor.SyncResult,
        project: SyncInteractor.SyncResult.ProjectSyncState
    ) {
        row {
            cell("$id") {
                paddingTop = 1
            }
            cell(value.state.asString()) {
                paddingTop = 1
            }
        }
        if (!project.projectSyncState.isPending()) {
            row {
                cell("Branches") {
                    paddingLeft = 1
                }
                cell(project.branchesState.asString())
            }
            row {
                cell("MRs") {
                    paddingLeft = 1
                }
                cell(project.mrs.state.asString())
            }
            project.mrs.visitActiveMRs { id, state ->
                row {
                    cell("$id") {
                        paddingLeft = 2
                    }
                    cell("State: ${state.asString()}")
                }
            }
            project.mrs.state.onDone {
                row {
                    cell("Synced: ") {
                        paddingLeft = 2
                    }
                    cell(project.mrs.doneItemsCount())
                }
                row {
                    cell("Skipped: ") {
                        paddingLeft = 2
                    }
                    cell(project.mrs.skippedItemsCount())
                }
                row {
                    cell("Removed: ") {
                        paddingLeft = 2
                    }
                    cell(project.mrs.removedItemsCount())
                }
            }
        }
    }

    private fun SyncInteractor.SyncResult.State.asString(): String {
        return when (this) {
            is SyncInteractor.SyncResult.State.Done -> "Completed $itemsUpdated items"
            is SyncInteractor.SyncResult.State.InProgress -> when (state) {
                SyncInteractor.SyncResult.State.ProgressState.LOADING -> "Loading..."
                SyncInteractor.SyncResult.State.ProgressState.SAVING -> "Saving..."
                SyncInteractor.SyncResult.State.ProgressState.COMPLEX -> "Content syncing..."
            }

            SyncInteractor.SyncResult.State.Pending -> "Waiting..."
            SyncInteractor.SyncResult.State.Skipped -> "Skipped"
            SyncInteractor.SyncResult.State.Removed -> "Removed"
        }
    }

    private fun SyncInteractor.SyncResult.State.isPending(): Boolean {
        return this is SyncInteractor.SyncResult.State.Pending
    }

    private inline fun SyncInteractor.SyncResult.State.onDone(block: (SyncInteractor.SyncResult.State.Done) -> Unit) {
        if (this is SyncInteractor.SyncResult.State.Done) {
            block(this)
        }
    }

    private fun SyncInteractor.SyncResult.MrsSyncState.doneItemsCount(): Int {
        return this.mergeRequests.count { it.value is SyncInteractor.SyncResult.State.Done }
    }

    private fun SyncInteractor.SyncResult.MrsSyncState.skippedItemsCount(): Int {
        return this.mergeRequests.count { it.value is SyncInteractor.SyncResult.State.Skipped }
    }

    private fun SyncInteractor.SyncResult.MrsSyncState.removedItemsCount(): Int {
        return this.mergeRequests.count { it.value is SyncInteractor.SyncResult.State.Removed }
    }

    private inline fun SyncInteractor.SyncResult.MrsSyncState.visitActiveMRs(visitor: (Long, SyncInteractor.SyncResult.State) -> Unit) {
        mergeRequests.forEach {
            if (it.value is SyncInteractor.SyncResult.State.InProgress) visitor(it.key, it.value)
        }
    }
}