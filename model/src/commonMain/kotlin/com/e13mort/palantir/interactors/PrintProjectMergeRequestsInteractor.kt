/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.MergeRequests
import com.e13mort.palantir.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class PrintProjectMergeRequestsInteractor(
    private val localRepository: ProjectRepository
) : Interactor<Long, PrintProjectMergeRequestsInteractor.MergeRequestsReport> {

    class MergeRequestsReport(private val mrs: MergeRequests) {

        suspend fun walk(callBack: (id: String, sourceBranch: String, targetBranch: String, created: Long, closed: Long?, state: String) -> Unit) {
            val toList = mrs.values().toList()
            toList.forEach {
                callBack(
                    it.localId().toString(),
                    it.sourceBranch().name(),
                    it.targetBranch().name(),
                    it.createdTime(),
                    it.closedTime(),
                    it.state().toString()
                )
            }
        }
    }

    override fun run(arg: Long): Flow<MergeRequestsReport> {
        return flow {
            val project = localRepository.findProject(arg)
                ?: throw Exception("Project with id $arg not found")
            emit(MergeRequestsReport(project.mergeRequests()))
        }
    }
}