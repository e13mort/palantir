package com.e13mort.gitlab_report.interactors

import com.e13mort.gitlab_report.model.ProjectRepository

class PrintProjectSummaryInteractor(
    private val projectRepository: ProjectRepository,
    private val id: Long
) : Interactor<ProjectSummary> {
    override suspend fun run(): ProjectSummary {
        val project = projectRepository.findProject(id) ?: throw Exception("Project $id not found")
        val branchesCount = project.branches().count().toInt()
        val mergeRequestsCount = project.mergeRequests().count().toInt()
        return object : ProjectSummary {

            override fun projectName(): String {
                return project.name()
            }

            override fun projectId(): String {
                return project.id()
            }

            override fun branchCount(): Int {
                return branchesCount
            }

            override fun mergeRequestCount(): Int {
                return mergeRequestsCount
            }

        }
    }
}

interface ProjectSummary {
    fun projectName(): String

    fun projectId(): String

    fun branchCount(): Int

    fun mergeRequestCount(): Int
}


