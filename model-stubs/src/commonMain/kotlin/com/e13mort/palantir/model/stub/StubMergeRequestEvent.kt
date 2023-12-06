package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User

data class StubMergeRequestEvent(
    val id: Long,
    val type: MergeRequestEvent.Type = MergeRequestEvent.Type.GENERAL_NOTE,
    val timeMillis: Long = 0L,
    val user: User = StubUser(1L, "Test user", "test.user"),
    val content: String = "Test content"
) : MergeRequestEvent {
    override fun id(): Long = id

    override fun type(): MergeRequestEvent.Type = type

    override fun timeMillis(): Long = timeMillis

    override fun user(): User = user

    override fun content(): String = content

}