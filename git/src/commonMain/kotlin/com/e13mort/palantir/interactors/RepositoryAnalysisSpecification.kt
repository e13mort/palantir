/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RepositoryAnalysisSpecification(
    val projects: Map<String, List<ProjectSpecification>>,
    val authorGroups: Map<String, List<String>> = emptyMap()
) {

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            decodeEnumsCaseInsensitive = true
        }

        fun fromString(string: String): RepositoryAnalysisSpecification? {
            return try {
                json.decodeFromString(string)
            } catch (e: Exception) {
                null
            }
        }
    }

    enum class MailMapType {
        Auto, Disabled
    }

    @Serializable
    data class ProjectSpecification(
        val localPath: String,
        val targetBranch: String? = null,
        val linesSpec: LinesSpec? = null,
        val percentile: Percentile = Percentile.P100,
        val mailMap: MailMapType = MailMapType.Auto,
        val excludeRevisions: List<String> = emptyList()
    )

    @Serializable
    data class LinesSpec(
        val languages: List<String> = emptyList(),
        val excludedPaths: List<String> = emptyList()
    )
}