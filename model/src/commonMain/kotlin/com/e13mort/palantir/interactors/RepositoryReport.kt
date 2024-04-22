package com.e13mort.palantir.interactors

data class RepositoryReport<T>(
    val result: List<GroupedResults<T>>
) {
    data class GroupedResults<T>(
        val groupName: String,
        val result: T
    )
}