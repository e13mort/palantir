package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.SyncableProjectRepository
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.toList

class UpdateProjectsListSyncPlan(
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository
) : SyncInteractor.SyncPlan<SyncInteractor.SyncResult.State> {
    override suspend fun sync(callback: suspend (SyncInteractor.SyncResult.State) -> Unit): SyncInteractor.SyncResult.State {
        callback(SyncInteractor.SyncResult.State.Pending)
        callback(SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.LOADING))
        val remoteProjects = remoteRepository.projects().toList()
        callback(SyncInteractor.SyncResult.State.InProgress(SyncInteractor.SyncResult.State.ProgressState.SAVING))
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
        val done = SyncInteractor.SyncResult.State.Done(remoteProjectsForAddition.size.toLong())
        callback(done)
        return done.copy()
    }
}