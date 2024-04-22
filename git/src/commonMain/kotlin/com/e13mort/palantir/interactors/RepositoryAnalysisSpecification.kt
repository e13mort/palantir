/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile
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
        val linesSpec: LinesSpec? = null,
        val percentile: Percentile = Percentile.P100
    )

    @Serializable
    data class LinesSpec(
        val languages: List<String> = emptyList(),
        val excludedPaths: List<String> = emptyList()
    )
}