package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Branch

data class StubBranch(
    val name: String
) : Branch {
    override fun name(): String = name
}