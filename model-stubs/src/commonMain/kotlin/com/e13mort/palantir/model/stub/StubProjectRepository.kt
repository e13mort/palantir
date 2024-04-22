/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Project
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class StubProjectRepository(
    private val projects: MutableList<Project>
) : ProjectRepository {

    override suspend fun projects(): Flow<Project> {
        return projects.asFlow()
    }

    override suspend fun removeProjects(ids: Set<Long>) {
        projects.removeIf { ids.contains(it.id().toLong()) }
    }

    override suspend fun findProject(id: Long): Project? {
        return projects.firstOrNull {
            it.id().toLong() == id
        }
    }

    override suspend fun addProject(project: Project) {
        projects += project
    }

    override suspend fun projectsCount(): Long {
        return projects.size.toLong()
    }

    override suspend fun clear() {
        projects.clear()
    }
}