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
}