/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.repository.NotesRepository

class SyncMRCommentsPlan(
    private val sourceNotesRepository: NotesRepository,
    private val mergeRequestNotesRepository: NotesRepository,
    private val pendingMRsForSync: MutableMap<Long, SyncInteractor.SyncResult.State>,
    private val projectId: Long
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.MrsSyncState> {

    private var lastSyncState = SyncInteractor.SyncResult.MrsSyncState(
        state = SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.COMPLEX),
        mergeRequests = pendingMRsForSync.toMap()
    )

    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): SyncInteractor.SyncResult.MrsSyncState {
        callback(lastSyncState)
        pendingMRsForSync
            .filter { it.value == SyncInteractor.SyncResult.State.Pending }
            .map { it.key }
            .forEach { mrId ->
                updateMRState(
                    mrId,
                    SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING),
                    callback
                )
                val events = sourceNotesRepository.events(projectId, mrId)
                updateMRState(
                    mrId,
                    SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING),
                    callback
                )
                mergeRequestNotesRepository.saveMergeRequestEvents(
                    projectId,
                    mrId,
                    events
                )
                updateMRState(mrId, SyncInteractor.SyncResult.State.Done(1), callback)
            }
        val itemsUpdated = pendingMRsForSync.count {
            it.value is SyncInteractor.SyncResult.State.Done
        }.toLong()
        lastSyncState =
            lastSyncState.copy(state = SyncInteractor.SyncResult.State.Done(itemsUpdated))
        return lastSyncState
    }

    private suspend fun updateMRState(
        mrId: Long,
        state: SyncInteractor.SyncResult.State,
        callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit
    ) {
        pendingMRsForSync[mrId] = state
        lastSyncState = lastSyncState.copy(mergeRequests = pendingMRsForSync.toMap())
        callback(lastSyncState)
    }
}