package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.MergeRequestRepository
import com.e13mort.palantir.repository.NotesRepository
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val mergeRequestNotesRepository: NotesRepository,
    private val strategy: SyncStrategy = SyncStrategy.FullSyncForActiveProjects,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback = SyncableProjectRepository.SyncableProject.SyncCallback.Empty
) : Interactor<SyncInteractor.SyncResult> {

    sealed interface SyncStrategy {
        data object UpdateProjects : SyncStrategy
        data object FullSyncForActiveProjects : SyncStrategy
        data class FullSyncForProject(val projectId: Long) : SyncStrategy
    }

    data class SyncResult(val projectsUpdated: Long)

    override suspend fun run(): SyncResult {
        return when(strategy) {
            SyncStrategy.FullSyncForActiveProjects -> runFullSyncForSelectedProjects()
            is SyncStrategy.FullSyncForProject -> runFullSyncForProject(strategy.projectId)
            SyncStrategy.UpdateProjects -> updateLocalProjects()
        }
    }

    private suspend fun updateLocalProjects(): SyncResult {
        val remoteProjects = remoteRepository.projects().toList()
        val localProjects = projectRepository.projects().toList()

        val remoteProjectIds = remoteProjects.map { it.id().toLong() }.toSet()
        val localProjectIds = localProjects.map { it.id().toLong() }.toSet()

        val unmodifiedProjectsIds = localProjectIds.intersect(remoteProjectIds)
        val localProjectIdsForRemoval = localProjectIds - unmodifiedProjectsIds
        val remoteProjectsForAddition = remoteProjectIds - unmodifiedProjectsIds

        projectRepository.removeProjects(localProjectIdsForRemoval)

        remoteProjects.filter {
            remoteProjectsForAddition.contains(it.id().toLong())
        }.forEach { project ->
            projectRepository.addProject(project)
        }
        return SyncResult(remoteProjectsForAddition.size.toLong())
    }

    private suspend fun runFullSyncForSelectedProjects(): SyncResult {
        var syncedCounter = 0L
        projectRepository.syncedProjects().collect {
            remoteRepository.findProject(it.id().toLong())?.let { remoteProject ->
                syncProject(it, remoteProject)
                syncedCounter++
            }
        }
        return SyncResult(syncedCounter)
    }

    private suspend fun runFullSyncForProject(projectId: Long): SyncResult {
        val localProject = projectRepository.projects().filter { it.id().toLong() == projectId }.firstOrNull()
            ?: throw IllegalArgumentException("Local project with id $projectId doesn't exists")
        val remoteProject = remoteRepository.findProject(projectId) ?: throw IllegalArgumentException("Remote project with id $projectId doesn't exists")
        localProject.updateSynced(true)
        syncProject(localProject, remoteProject)
        return SyncResult(1)
    }

    private suspend fun syncProject(
        localProject: SyncableProjectRepository.SyncableProject,
        remoteProject: Project
    ) {
        localProject.updateBranches(remoteProject.branches(), syncCallback)
        syncMRs(remoteProject, syncCallback)
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