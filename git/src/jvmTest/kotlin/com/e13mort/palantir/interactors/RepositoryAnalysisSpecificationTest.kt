/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.interactors.RepositoryAnalysisSpecification.MailMapType
import com.e13mort.palantir.model.Percentile
import com.e13mort.palantir.readTestFile
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.collections.shouldMatchInOrder
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldMatchAll
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class RepositoryAnalysisSpecificationTest {

    private val spec1Content: String
        get() = readTestFile("spec1.json")

    @Test
    fun `spec1 should not be null`() {
        RepositoryAnalysisSpecification.fromString(spec1Content) shouldNotBe null
    }

    @Test
    fun `wrong content should be null`() {
        RepositoryAnalysisSpecification.fromString("not-a-json") shouldBe null
    }

    @Test
    fun `spec1 should be correct`() {
        val specification = RepositoryAnalysisSpecification.fromString(spec1Content)
        specification!! should { spec ->
            spec.projects shouldContainKey "group1"
            spec.projects["group1"]!! should { projects ->
                projects shouldMatchEach listOf(
                    {
                        it.localPath shouldBe "/local/path1"
                        it.targetBranch shouldBe null
                        it.linesSpec shouldBe null
                        it.mailMap shouldBe MailMapType.Auto
                        it.excludeRevisions shouldBe emptyList()
                    },
                    {
                        it.localPath shouldBe "/local/path2"
                        it.targetBranch shouldBe "master"
                        it.linesSpec shouldBe null
                        it.mailMap shouldBe MailMapType.Disabled
                        it.excludeRevisions shouldMatchEach listOf(
                            {
                                it shouldBe "hash1"
                            },
                            {
                                it shouldBe "hash2"
                            },
                        )
                    }
                )
            }
            spec.authorGroups shouldMatchAll mapOf(
                "user1" to {
                    it shouldMatchEach listOf({
                        it shouldBe "group1"
                    }, {
                        it shouldBe "group2"
                    })
                },
                "user2" to {
                    it shouldBe emptyList()
                }
            )
        }
    }

    @Test
    fun `spec2 should be correct`() {
        val specification = RepositoryAnalysisSpecification.fromString(spec1Content)
        specification!! should { spec ->
            spec.projects shouldContainKey "group2"
            spec.projects["group2"]!! should { projects ->
                projects shouldMatchEach listOf {
                    it.localPath shouldBe "/local/path1"
                    it.targetBranch shouldBe "master"
                    it.mailMap shouldBe MailMapType.Auto
                    it.linesSpec!! should {
                        it.languages shouldMatchInOrder listOf(
                            { it shouldBe "java" },
                            { it shouldBe "kotlin" }
                        )
                        it.excludedPaths shouldMatchInOrder listOf(
                            { it shouldBe "/build" },
                            { it shouldBe "/module/build" }
                        )
                    }
                    it.percentile shouldBe Percentile.P95
                }
            }
        }
    }
}
