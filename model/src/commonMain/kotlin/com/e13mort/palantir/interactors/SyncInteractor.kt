package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ProjectRepository
import com.e13mort.palantir.model.SyncableProjectRepository
import kotlinx.coroutines.flow.collect

class SyncInteractor(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback
) : Interactor<SyncInteractor.SyncResult> {

    data class SyncResult(val projectsUpdated: Long)

    override suspend fun run(): SyncResult {
        var syncedCounter = 0L
        projectRepository.syncedProjects().collect {
            remoteRepository.findProject(it.id().toLong())?.let { remoteProject ->
                it.updateBranches(remoteProject.branches(), syncCallback)
                it.updateMergeRequests(remoteProject.mergeRequests(), syncCallback)
                syncedCounter++
            }
        }
        return SyncResult(syncedCounter)
    }
}