package com.e13mort.gitlab_report.interactors

/*
    A use case implementation based on Command pattern.
 */
interface Interactor<T> {
    suspend fun run(): T
}