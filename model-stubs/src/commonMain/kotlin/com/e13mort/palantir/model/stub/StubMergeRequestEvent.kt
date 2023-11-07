package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.MergeRequestEvent
import com.e13mort.palantir.model.User

data class StubMergeRequestEvent(
    val id: Long,
    val type: MergeRequestEvent.Type,
    val timeMillis: Long,
    val user: User,
    val content: String
) : MergeRequestEvent {
    override fun id(): Long = id

    override fun type(): MergeRequestEvent.Type = type

    override fun timeMillis(): Long = timeMillis

    override fun user(): User = user

    override fun content(): String = content

}