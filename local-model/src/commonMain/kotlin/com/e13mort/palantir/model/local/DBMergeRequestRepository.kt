package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.User
import com.e13mort.palantir.repository.MergeRequestRepository

class DBMergeRequestRepository(
    private val localModel: LocalModel
) : MergeRequestRepository {
    override suspend fun mergeRequest(id: Long): MergeRequest? {
        val storedMR = localModel.mergeRequestsQueries.selectById(id).executeAsOneOrNull()
        return storedMR?.let { DBMergeRequest(it, localModel.mr_assigneesQueries, localModel.notesQueries) }
    }

    override suspend fun assignees(id: Long): List<User> {
        return localModel.mr_assigneesQueries
            .assignees(id)
            .executeAsList()
            .map {
                DBUser(it)
            }
    }

    override suspend fun deleteMergeRequest(id: Long) {
        localModel.mergeRequestsQueries.removeById(id)
    }

    override suspend fun saveMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>) {
        val mergeRequestsQueries = localModel.mergeRequestsQueries
        mergeRequestsQueries.transaction {
            mergeRequests.forEach { request ->
                mergeRequestsQueries.insert(
                    project_id = projectId,
                    id = request.id().toLong(),
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