package com.e13mort.palantir.model.local

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequestRepository

class DBMergeRequestRepository(
    private val localModel: LocalModel
) : MergeRequestRepository {
    override suspend fun mergeRequest(id: Long): MergeRequest? {
        val storedMR = localModel.mergeRequestsQueries.selectById(id).executeAsOneOrNull()
        return storedMR?.let { DBMergeRequest(it, localModel.mr_assigneesQueries, localModel.notesQueries) }
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
            }
        }
    }
}