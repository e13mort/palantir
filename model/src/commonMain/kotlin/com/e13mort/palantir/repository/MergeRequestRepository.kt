package com.e13mort.palantir.repository

import com.e13mort.palantir.model.MergeRequest

interface MergeRequestRepository {
    suspend fun mergeRequest(id: Long): MergeRequest?

    suspend fun saveMergeRequests(projectId: Long, mergeRequests: List<MergeRequest>)

    suspend fun deleteMergeRequest(id: Long)
}