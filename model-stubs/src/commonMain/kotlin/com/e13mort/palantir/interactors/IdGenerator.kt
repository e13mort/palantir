package com.e13mort.palantir.interactors

class IdGenerator(
    private var currentId: Long = 1
) {

    fun nextId() = currentId.also {
        currentId += 1
    }
}