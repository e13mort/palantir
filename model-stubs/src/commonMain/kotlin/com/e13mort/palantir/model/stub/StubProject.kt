/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Branches
import com.e13mort.palantir.model.ClonePaths
import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.MergeRequests
import com.e13mort.palantir.model.Project

data class StubProject(
    val id: String,
    val name: String = "StubProject",
    val branches: Branches = StubBranches(),
    val mergeRequests: List<MergeRequest> = emptyList(),
    val clonePaths: ClonePaths = object : ClonePaths {
        override fun ssh(): String = "ssh://test"

        override fun http(): String = "http://test"

    }
) : Project {
    override fun id(): String = id

    override fun name(): String = name

    override fun branches(): Branches = branches

    override fun mergeRequests(): MergeRequests = StubMergeRequests(this, mergeRequests)

    override fun clonePaths(): ClonePaths = clonePaths
}