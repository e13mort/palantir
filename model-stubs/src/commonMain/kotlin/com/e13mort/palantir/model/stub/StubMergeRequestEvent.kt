/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User

data class StubMergeRequestEvent(
    val id: Long,
    var type: MergeRequestEvent.Type = MergeRequestEvent.Type.GENERAL_NOTE,
    var timeMillis: Long = 0L,
    var user: User = StubUser(1L, "Test user", "test.user"),
    var content: String = "Test content"
) : MergeRequestEvent {
    override fun id(): Long = id

    override fun type(): MergeRequestEvent.Type = type

    override fun timeMillis(): Long = timeMillis

    override fun user(): User = user

    override fun content(): String = content

}