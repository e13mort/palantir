/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PrintProjectSummaryInteractor(
    private val projectRepository: ProjectRepository
) : Interactor<Long, ProjectSummary> {
    override fun run(arg: Long): Flow<ProjectSummary> {
        return flow {
            val project =
                projectRepository.findProject(arg) ?: throw Exception("Project $arg not found")
            val branchesCount = project.branches().count().toInt()
            val mergeRequestsCount = project.mergeRequests().count().toInt()
            object : ProjectSummary {

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

            }.apply { emit(this) }
        }
    }

}

interface ProjectSummary {
    fun projectName(): String

    fun projectId(): String

    fun branchCount(): Int

    fun mergeRequestCount(): Int
}


