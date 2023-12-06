package com.e13mort.palantir.repository

import com.e13mort.palantir.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun projects(): Flow<Project>

    suspend fun findProject(id: Long): Project?

    suspend fun addProject(project: Project)

    suspend fun projectsCount(): Long

    suspend fun clear()
}