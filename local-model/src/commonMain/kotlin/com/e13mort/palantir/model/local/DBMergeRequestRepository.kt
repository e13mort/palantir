/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.User
import com.e13mort.palantir.repository.MergeRequestRepository

class DBMergeRequestRepository(
    private val localModel: LocalModel
) : MergeRequestRepository {
    override suspend fun mergeRequest(id: Long): MergeRequest? {
        val storedMR = localModel.mergeRequestsQueries.selectById(id).executeAsOneOrNull()
        return storedMR?.let { DBMergeRequest(it, localModel.mr_assigneesQueries) }
    }

    override suspend fun assignees(id: Long): List<User> {
        return localModel.mr_assigneesQueries
            .assignees(id)
            .executeAsList()
            .map {
                DBUser(it)
            }
    }

    override suspend fun deleteMergeRequests(projectId: Long, ids: Set<Long>) {
        localModel.mergeRequestsQueries.transaction {
            ids.forEach { id ->
                localModel.mergeRequestsQueries.removeById(id, projectId)
            }
        }
    }

    override suspend fun deleteMergeRequestsForProject(projectId: Long) {
        localModel.mergeRequestsQueries.removeProjectsMergeRequests(projectId)
    }

    override suspend fun mergeRequestsForProject(projectId: Long): List<MergeRequest> {
        return localModel.mergeRequestsQueries.selectAll(projectId).executeAsList()
            .map { DBMergeRequest(it, localModel.mr_assigneesQueries) }
    }

    override suspend fun addMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>) {
        val mergeRequestsQueries = localModel.mergeRequestsQueries
        mergeRequestsQueries.transaction {
            mergeRequests.forEach { request ->
                mergeRequestsQueries.insert(
                    project_id = projectId,
                    id = request.id().toLong(),
                    local_id = request.localId(),
                    state = request.state().ordinal.toLong(),
                    source_branch_name = request.sourceBranch().name(),
                    target_branch_name = request.targetBranch().name(),
                    created_time = request.createdTime(),
                    closed_time = request.closedTime()
                )
                request.assignees().forEach { user ->
                    localModel.userQueries.put(user.id(), user.name(), user.userName())
                    localModel.mr_assigneesQueries.add(request.id().toLong(), user.id())
                }
            }
        }
    }
}