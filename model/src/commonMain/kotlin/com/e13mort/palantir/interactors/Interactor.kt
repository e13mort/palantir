package com.e13mort.palantir.interactors

import kotlinx.coroutines.flow.Flow

/*
    A use case implementation based on Command pattern.
 */
interface Interactor<I, T> {
    suspend fun run(arg: I): Flow<T>
}