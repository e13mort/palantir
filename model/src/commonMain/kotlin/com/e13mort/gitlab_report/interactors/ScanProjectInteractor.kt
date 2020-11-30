package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.ProjectRepository
import com.e13mort.gitlab_report.model.SyncableProjectRepository

class ScanProjectInteractor(
    private val projectId: Long,
    private val projectRepository: SyncableProjectRepository,
    private val remoteRepository: ProjectRepository
) : Interactor<ScanProjectInteractor.ScanProjectResult> {
    override suspend fun run(): ScanProjectResult {
        val remoteProject = remoteRepository.findProject(projectId) ?: throw Exception("Remote project $projectId isn't found")
        val localProject = projectRepository.findProject(projectId) ?: throw Exception("Project $projectId isn't found")
        localProject.updateSynced(true)
        localProject.updateBranches(remoteProject.branches())
        val syncedBranches = localProject.branches().count()
//        val syncedBranches = 0L
        return object : ScanProjectResult {
            override fun projectName(): String {
                return localProject.name()
            }

            override fun syncedBranchesCount(): Long {
                return syncedBranches
            }

        }
    }

    interface ScanProjectResult {
        fun projectName(): String

        fun syncedBranchesCount(): Long
    }
}