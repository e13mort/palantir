package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.Project
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.repository.NotesRepository
import kotlinx.coroutines.flow.toList

class SyncMRsPlan(
    private val projectId: Long,
    private val remoteProject: Project,
    private val mergeRequestRepository: MergeRequestRepository,
    private val mergeRequestNotesRepository: NotesRepository,
    private val remoteNotesRepository: NotesRepository
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.MrsSyncState> {
    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): SyncInteractor.SyncResult.MrsSyncState {
        val syncMRList = mergeRequests(callback)
        val pendingMRsForSync = prepareNotesForSync(syncMRList)
        return SyncMRCommentsPlan(
            remoteNotesRepository,
            mergeRequestNotesRepository,
            pendingMRsForSync,
            projectId
        ).sync(callback)
    }

    private suspend fun mergeRequests(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): List<MergeRequest> {
        callback(
            SyncInteractor.SyncResult.MrsSyncState(
                state = SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING)
            )
        )
        val mergeRequests = remoteProject.mergeRequests()
        val mergeRequestList = mergeRequests.values().toList()
        callback(
            SyncInteractor.SyncResult.MrsSyncState(
                state = SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING)
            )
        )
        mergeRequestRepository.deleteMergeRequestsForProject(projectId)
        mergeRequestRepository.saveMergeRequests(projectId, mergeRequestList)
        val mrsSyncState = SyncInteractor.SyncResult.MrsSyncState(
            state = SyncInteractor.SyncResult.State.Done(mergeRequests.count())
        )
        callback(mrsSyncState)
        return mergeRequestList
    }

    private fun prepareNotesForSync(syncMRList: List<MergeRequest>): MutableMap<Long, SyncInteractor.SyncResult.State> {
        val pendingMRsForSync: MutableMap<Long, SyncInteractor.SyncResult.State> =
            syncMRList.associate {
                it.id().toLong() to SyncInteractor.SyncResult.State.Pending
            }.toMutableMap()
        return pendingMRsForSync
    }
}