package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.ProjectRepository
import com.e13mort.palantir.model.SyncableProjectRepository

class ScanProjectInteractor(
    private val projectId: Long,
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository,
    private val syncCallback: SyncableProjectRepository.SyncableProject.SyncCallback
) : Interactor<ScanProjectInteractor.ScanProjectResult> {
    override suspend fun run(): ScanProjectResult {
        val remoteProject = remoteRepository.findProject(projectId) ?: throw Exception("Remote project $projectId isn't found")
        val localProject = projectRepository.findProject(projectId) ?: throw Exception("Project $projectId isn't found")
        localProject.updateSynced(true)
        localProject.updateBranches(remoteProject.branches(), syncCallback)
        localProject.updateMergeRequests(remoteProject.mergeRequests(), syncCallback)
        val syncedBranches = localProject.branches().count()
        val syncedMergeRequests = localProject.mergeRequests().count()
        return ScanProjectResult(syncedBranches, syncedMergeRequests, localProject)
    }

    class ScanProjectResult(
        private val syncedBranches: Long,
        private val syncedMergeRequests: Long,
        private val localProject: SyncableProjectRepository.SyncableProject
    ) {
        fun projectName(): String {
            return localProject.name()
        }

        fun syncedBranchesCount(): Long {
            return syncedBranches
        }

        fun syncedMergeRequests(): Long {
            return syncedMergeRequests
        }
    }
}