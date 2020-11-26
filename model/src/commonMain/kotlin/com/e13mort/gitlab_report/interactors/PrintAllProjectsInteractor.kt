package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.ProjectRepository
import kotlinx.coroutines.flow.toList

class PrintAllProjectsInteractor(
    private val projectRepository: ProjectRepository
) : Interactor<AllProjectsReport>{
    override suspend fun run() : AllProjectsReport {
        val result = projectRepository.projects().toList()

        return object : AllProjectsReport {
            override fun walk(visitor: (Project) -> Unit) {
                result.forEach(visitor)
            }
        }
    }
}

interface AllProjectsReport {
    fun walk(visitor: (Project) -> Unit)
}