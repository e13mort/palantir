package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.repository.NotesRepository
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.toList

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val mergeRequestNotesRepository: NotesRepository,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback = SyncableProjectRepository.SyncableProject.SyncCallback.Empty
) : Interactor<SyncInteractor.SyncResult> {

    data class SyncResult(val projectsUpdated: Long)

    override suspend fun run(): SyncResult {
        var syncedCounter = 0L
        projectRepository.syncedProjects().collect {
            remoteRepository.findProject(it.id().toLong())?.let { remoteProject ->
                it.updateBranches(remoteProject.branches(), syncCallback)
                syncMRs(remoteProject, syncCallback)
                syncedCounter++
            }
        }
        return SyncResult(syncedCounter)
    }

    private suspend fun syncMRs(
        remoteProject: Project,
        syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback
    ) {
        val projectId = remoteProject.id().toLong()
        val mergeRequests = remoteProject.mergeRequests()

        mergeRequestRepository.deleteMergeRequestsForProject(projectId)
        syncCallback.onMREvent(SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.RemoteLoadingStarted)

        val mergeRequestList = mergeRequests.values().toList()
        mergeRequestRepository.saveMergeRequests(projectId, mergeRequestList)

        mergeRequestList.forEachIndexed {index, mergeRequest ->
            notifyMRProcessing(mergeRequest, syncCallback, index, mergeRequestList.size)
            mergeRequestNotesRepository.saveMergeRequestEvents(mergeRequest.id().toLong(), mergeRequest.events())
        }
    }

    private fun notifyMRProcessing(
        mergeRequest: MergeRequest,
        callback: SyncableProjectRepository.SyncableProject.UpdateMRCallback,
        index: Int,
        totalSize: Int
    ) {
        callback.onMREvent(
            SyncableProjectRepository.SyncableProject.UpdateMRCallback.MREvent.LoadMR(
                mergeRequest.id(),
                index,
                totalSize
            )
        )
    }

}