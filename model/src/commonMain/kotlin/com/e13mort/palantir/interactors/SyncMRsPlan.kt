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
    private val remoteNotesRepository: NotesRepository,
    private val type: SyncType
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.MrsSyncState> {

    enum class SyncType { Forced, Incremental }

    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): SyncInteractor.SyncResult.MrsSyncState {
        val notesForSync = mergeRequests(callback)
        return SyncMRCommentsPlan(
            remoteNotesRepository,
            mergeRequestNotesRepository,
            notesForSync,
            projectId
        ).sync(callback)
    }

    private suspend fun mergeRequests(callback: suspend (SyncInteractor.SyncResult.MrsSyncState) -> Unit): MutableMap<Long, SyncInteractor.SyncResult.State> {
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
        val prepareNotesForSync = when (type) {
            SyncType.Forced -> performForceSync(mergeRequestList)
            SyncType.Incremental -> performIncrementalSync(mergeRequestList)
        }
        val mrsSyncState = SyncInteractor.SyncResult.MrsSyncState(
            state = SyncInteractor.SyncResult.State.Done(mergeRequests.count())
        )
        callback(mrsSyncState)
        return prepareNotesForSync
    }

    private suspend fun performForceSync(mergeRequestList: List<MergeRequest>): MutableMap<Long, SyncInteractor.SyncResult.State> {
        mergeRequestRepository.deleteMergeRequestsForProject(projectId)
        mergeRequestRepository.addMergeRequests(projectId, mergeRequestList)
        return prepareNotesForSync(mergeRequestList)
    }

    private suspend fun performIncrementalSync(updatedMRsList: List<MergeRequest>): MutableMap<Long, SyncInteractor.SyncResult.State> {
        val existingMRsForProject = mergeRequestRepository.mergeRequestsForProject(projectId)
        val existingMRStates: Map<Long, MergeRequest.State> = existingMRsForProject.associate {
            it.localId() to it.state()
        }

        val newMRStates: Map<Long, MergeRequest.State> = updatedMRsList.associate {
            it.localId() to it.state()
        }

        val commonMrsIds: Set<Long> = existingMRStates.keys.intersect(newMRStates.keys)

        val itemsToKeep: Set<Long> = commonMrsIds.filter { id ->
            val existingMrState = existingMRStates[id] ?: return@filter false
            val newMrState = newMRStates[id] ?: return@filter false
            return@filter isMRMightBeSkipped(existingMrState, newMrState)
        }.toSet()

        val obsoleteItemsToRemove: Set<Long> = existingMRStates.keys - newMRStates.keys

        val mrsToSave = updatedMRsList.toMutableList()
        mrsToSave.removeIf {
            itemsToKeep.contains(it.localId())
        }

        val itemsToRemove: Set<Long> = obsoleteItemsToRemove + existingMRStates.keys - itemsToKeep

        mergeRequestRepository.deleteMergeRequests(projectId, itemsToRemove)
        mergeRequestRepository.addMergeRequests(projectId, mrsToSave)

        val fullMRsIds = existingMRStates.keys + newMRStates.keys
        val pendingMRsForSync =
            fullMRsIds.toList().associate {
                val mrId = it
                val itemState =
                    if (itemsToKeep.contains(mrId)) SyncInteractor.SyncResult.State.Skipped
                    else if (obsoleteItemsToRemove.contains(mrId)) SyncInteractor.SyncResult.State.Removed
                    else SyncInteractor.SyncResult.State.Pending
                mrId to itemState
            }.toMutableMap()
        return pendingMRsForSync
    }

    private fun isMRMightBeSkipped(
        existingMrState: MergeRequest.State,
        newMrState: MergeRequest.State
    ): Boolean {
        return (existingMrState == newMrState) && (existingMrState == MergeRequest.State.MERGED || existingMrState == MergeRequest.State.CLOSED)
    }

    private fun prepareNotesForSync(syncMRList: List<MergeRequest>): MutableMap<Long, SyncInteractor.SyncResult.State> {
        val pendingMRsForSync: MutableMap<Long, SyncInteractor.SyncResult.State> =
            syncMRList.associate {
                it.localId() to SyncInteractor.SyncResult.State.Pending
            }.toMutableMap()
        return pendingMRsForSync
    }
}