package com.e13mort.gitlab_report.model

import kotlinx.coroutines.flow.Flow

interface Project {
    fun id(): String
    fun name(): String
}

interface ProjectRepository {
    suspend fun projects(): Flow<Project>

    suspend fun findProject(id: Long): Project?

    suspend fun addProject(project: Project)

    suspend fun clear()
}