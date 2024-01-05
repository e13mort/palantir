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
            .forEach { id ->
                updateMRState(
                    id,
                    SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING),
                    callback
                )
                val events = sourceNotesRepository.events(projectId, id)
                updateMRState(
                    id,
                    SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING),
                    callback
                )
                mergeRequestNotesRepository.saveMergeRequestEvents(
                    id,
                    events
                )
                updateMRState(id, SyncInteractor.SyncResult.State.Done(1), callback)
            }
        lastSyncState =
            lastSyncState.copy(state = SyncInteractor.SyncResult.State.Done(pendingMRsForSync.size.toLong()))
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