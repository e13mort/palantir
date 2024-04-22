/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.repository

import com.e13mort.palantir.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun projects(): Flow<Project>

    suspend fun findProject(id: Long): Project?

    suspend fun addProject(project: Project)

    suspend fun projectsCount(): Long

    suspend fun clear()

    suspend fun removeProjects(ids: Set<Long>)
}