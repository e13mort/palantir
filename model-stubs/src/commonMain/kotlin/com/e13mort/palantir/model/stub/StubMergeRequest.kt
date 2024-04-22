/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.Branch
import com.e13mort.palantir.model.MergeRequest
import com.e13mort.palantir.model.User

data class StubMergeRequest(
    val id: String,
    val localId: Long = id.toLong(),
    val state: MergeRequest.State = MergeRequest.State.OPEN,
    val sourceBranch: Branch = StubBranch("test"),
    val targetBranch: Branch = StubBranch("test"),
    val createdTime: Long = 0L,
    val closedTime: Long? = null,
    val assignees: List<User> = emptyList(),
) : MergeRequest {
    override fun id(): String = id

    override fun localId(): Long = localId

    override fun state(): MergeRequest.State = state

    override fun sourceBranch(): Branch = sourceBranch

    override fun targetBranch(): Branch = targetBranch

    override fun createdTime(): Long = createdTime

    override fun closedTime(): Long? = closedTime

    override fun assignees(): List<User> = assignees
}