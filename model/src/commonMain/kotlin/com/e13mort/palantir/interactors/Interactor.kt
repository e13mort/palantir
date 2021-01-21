package com.e13mort.palantir.interactors

/*
    A use case implementation based on Command pattern.
 */
interface Interactor<T> {
    suspend fun run(): T
}