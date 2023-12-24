package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.ProjectRepository

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback = SyncableProjectRepository.SyncableProject.SyncCallback.Empty
) : Interactor<SyncInteractor.SyncResult> {

    data class SyncResult(val projectsUpdated: Long)

    override suspend fun run(): SyncResult {
        var syncedCounter = 0L
        projectRepository.syncedProjects().collect {
            remoteRepository.findProject(it.id().toLong())?.let { remoteProject ->
                it.updateBranches(remoteProject.branches(), syncCallback)
                it.updateMergeRequests(
                    remoteProject.id(),
                    remoteProject.mergeRequests(),
                    syncCallback
                )
                syncedCounter++
            }
        }
        return SyncResult(syncedCounter)
    }
}