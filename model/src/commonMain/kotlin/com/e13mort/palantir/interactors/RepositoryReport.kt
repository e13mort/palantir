package com.e13mort.palantir.interactors

interface RepositoryReport<T> {
    data class GroupedResults<T>(
        val groupName: String,
        val result: T
    )

    val result: List<GroupedResults<T>>

}