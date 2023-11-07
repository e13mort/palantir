package com.e13mort.palantir.model.stub

import com.e13mort.palantir.model.User

data class StubUser(
    val id: Long,
    val name: String,
    val userName: String
) : User {
    override fun id(): Long = id

    override fun name(): String = name

    override fun userName(): String = userName

}