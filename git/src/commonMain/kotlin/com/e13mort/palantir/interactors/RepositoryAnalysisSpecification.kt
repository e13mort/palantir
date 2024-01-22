package com.e13mort.palantir.interactors

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RepositoryAnalysisSpecification(
    val projects: Map<String, List<ProjectSpecification>>
) {

    companion object {
        fun fromString(string: String): RepositoryAnalysisSpecification? {
            return try {
                Json.decodeFromString(string)
            } catch (e: Exception) {
                null
            }
        }
    }

    @Serializable
    data class ProjectSpecification(
        val localPath: String,
        val targetBranch: String? = null,
        val linesSpec: LinesSpec? = null
    )

    @Serializable
    data class LinesSpec(
        val languages: List<String> = emptyList(),
        val excludedPaths: List<String> = emptyList()
    )
}