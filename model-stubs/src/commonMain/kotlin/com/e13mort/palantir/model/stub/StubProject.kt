package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.model.ClonePaths
import com.e13mort.palantir.model.MergeRequests
import com.e13mort.palantir.model.Project

data class StubProject(
    val id: String,
    val name: String,
    val branches: Branches,
    val mergeRequests: MergeRequests,
    val clonePaths: ClonePaths
) : Project {
    override fun id(): String = id

    override fun name(): String = name

    override fun branches(): Branches = branches

    override fun mergeRequests(): MergeRequests = mergeRequests

    override fun clonePaths(): ClonePaths = clonePaths
}