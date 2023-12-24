package com.e13mort.palantir.repository

import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.User

interface MergeRequestRepository {
    suspend fun mergeRequest(id: Long): MergeRequest?

    suspend fun saveMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>)

    suspend fun deleteMergeRequest(id: Long)

    suspend fun deleteMergeRequestsForProject(projectId: Long)

    suspend fun assignees(id: Long): List<User>
}