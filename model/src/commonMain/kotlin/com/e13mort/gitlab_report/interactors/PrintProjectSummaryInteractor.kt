package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.Project
import com.e13mort.gitlab_report.model.ProjectRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

class PrintProjectSummaryInteractor(
    private val projectRepository: ProjectRepository,
    private val id: String
) : Interactor<ProjectSummary> {
    override suspend fun run(): ProjectSummary {
        val project = projectRepository.projects().filter {
            it.id() == id
        }.first()
        return object : ProjectSummary {
            override fun project(): Project {
                return project
            }

            override fun branchCount(): Int {
                // TODO: 27/11/2020 read from repository
                return -1
            }

            override fun mergeRequestCount(): Int {
                // TODO: 27/11/2020 read from repository
                return -1
            }

        }
    }
}

interface ProjectSummary {
    fun project(): Project

    fun branchCount(): Int

    fun mergeRequestCount(): Int
}


