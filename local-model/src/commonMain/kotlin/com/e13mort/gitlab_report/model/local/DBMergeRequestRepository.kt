package com.e13mort.gitlab_report.model.local

import com.e13mort.gitlab_report.model.MergeRequest
import com.e13mort.gitlab_report.model.MergeRequestRepository

class DBMergeRequestRepository(
    private val localModel: LocalModel
) : MergeRequestRepository {
    override suspend fun mergeRequest(id: Long): MergeRequest? {
        val storedMR = localModel.mergeRequestsQueries.selectById(id).executeAsOneOrNull()
        return storedMR?.let { DBMergeRequest(it) }
    }
}