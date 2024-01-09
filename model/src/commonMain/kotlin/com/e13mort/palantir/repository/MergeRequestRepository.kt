package com.e13mort.palantir.repository

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.User

interface MergeRequestRepository {
    suspend fun mergeRequest(id: Long): MergeRequest?

    suspend fun addMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>)

    suspend fun deleteMergeRequests(projectId: Long, ids: Set<Long>)

    suspend fun deleteMergeRequestsForProject(projectId: Long)

    suspend fun mergeRequestsForProject(projectId: Long): List<MergeRequest>

    suspend fun assignees(id: Long): List<User>
}