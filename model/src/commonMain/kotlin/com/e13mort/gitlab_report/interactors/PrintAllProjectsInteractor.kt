package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.SyncableProjectRepository
import kotlinx.coroutines.flow.toList

class PrintAllProjectsInteractor(
    private val projectRepository: SyncableProjectRepository
) : Interactor<AllProjectsReport>{
    override suspend fun run() : AllProjectsReport {
        val result = projectRepository.projects().toList()

        return object : AllProjectsReport {
            override fun walk(visitor: (Project, Boolean) -> Unit) {
                result.forEach {
                    visitor(it, it.synced())
                }
            }
        }
    }
}

interface AllProjectsReport {
    fun walk(visitor: (Project, Boolean) -> Unit)
}