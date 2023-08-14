package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Project
import com.e13mort.palantir.model.SyncableProjectRepository
import kotlinx.coroutines.flow.toList

class PrintAllProjectsInteractor(
    private val projectRepository: SyncableProjectRepository
) : Interactor<AllProjectsResult>{
    override suspend fun run() : AllProjectsResult {
        val result = projectRepository.projects().toList()

        return object : AllProjectsResult {

            override fun projects(synced: Boolean): List<Project> {
                return result.filter {
                    synced == it.synced()
                }
            }
        }
    }
}