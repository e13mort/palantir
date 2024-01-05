package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.repository.NotesRepository

class SyncMRCommentsPlan(
    private val mergeRequests: List<MergeRequest>,
    private val mergeRequestNotesRepository: NotesRepository
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.MrsSyncState> {
    private val pendingMRsForSync: MutableMap<Long, SyncInteractor.SyncResult.State> =
        mergeRequests.associate {
            it.id().toLong() to SyncInteractor.SyncResult.State.Pending
        }.toMutableMap()

    private var lastSyncState = SyncInteractor.SyncResult.MrsSyncState(
        state = SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.COMPLEX),
        mergeRequests = pendingMRsForSync.toMap()
    )

    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): SyncInteractor.SyncResult.MrsSyncState {
        callback(lastSyncState)
        mergeRequests.forEach { mr ->
            val mrId = mr.id().toLong()
            updateMRState(
                mrId,
                SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING),
                callback
            )
            val events = mr.events()
            updateMRState(
                mrId,
                SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING),
                callback
            )
            mergeRequestNotesRepository.saveMergeRequestEvents(
                mrId,
                events
            )
            updateMRState(mrId, SyncInteractor.SyncResult.State.Done(1), callback)
        }
        lastSyncState =
            lastSyncState.copy(state = SyncInteractor.SyncResult.State.Done(mergeRequests.size.toLong()))
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