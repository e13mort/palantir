package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Branch
import com.e13mort.palantir.model.Branches
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

data class StubBranches(
    val branches: List<Branch>
) : Branches {
    override suspend fun count(): Long = branches.size.toLong()

    override suspend fun values(): Flow<Branch> = branches.asFlow()
}