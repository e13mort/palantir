package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

data class StubMergeRequests(
    val project: Project,
    val requests: List<MergeRequest>
) : MergeRequests {
    override suspend fun project(): Project = project

    override suspend fun count(): Long = requests.size.toLong()

    override suspend fun values(): Flow<MergeRequest> = requests.asFlow()
}

