package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.SyncableProjectRepository

class ScanProjectInteractor(
    private val projectId: Long,
    private val projectRepository: SyncableProjectRepository
) : Interactor<ScanProjectInteractor.ScanProjectResult> {
    override suspend fun run(): ScanProjectResult {
        val localProject = projectRepository.findProject(projectId) ?: throw Exception("Project $projectId isn't found")
        localProject.updateSynced(true)
        return object : ScanProjectResult {
            override fun projectName(): String {
                return localProject.name()
            }

        }
    }

    interface ScanProjectResult {
        fun projectName(): String
    }
}