/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.interactors.CodeChangesReportItem.CommitDiff
import com.e13mort.palantir.model.Percentile

data class CodeChangesReportItem(
    val commitDiffs: Map<String, List<DiffWithRanges>>,
    val summary: Summary
) {

    fun calculateUserChanges(): Map<String, DiffWithRanges.UserChanges> {
        return commitDiffs.mapValues { it.value.calculateByRange() }
    }

    private fun List<DiffWithRanges>.calculateByRange(): DiffWithRanges.UserChanges {
        val data: MutableMap<Range, MutableMap<String, MutableList<CommitDiff>>> = mutableMapOf()
        forEach { diffWithRanges ->
            val accumulatorForRange = data.getOrPut(diffWithRanges.range) {
                mutableMapOf()
            }
            diffWithRanges.diffs.forEach { diff: CommitDiff ->
                val user = diff.authorEmailAddress
                val userCommits = accumulatorForRange.getOrPut(user) {
                    mutableListOf()
                }
                userCommits += diff
            }
        }
        val resultData = data.mapValues {
            it.value.mapValues { commitsList ->
                DiffWithRanges.UserCommits(commitsList.value)
            }
        }
        return DiffWithRanges.UserChanges(resultData)
    }

    data class DiffWithRanges(
        val range: Range,
        val diffs: List<CommitDiff>,
        val statisticsData: StatisticsData
    ) {
        fun totalAdded() = diffs.totalLinesAdded()

        fun totalRemoved() = diffs.totalLinesRemoved()

        fun totalChanged() = diffs.totalChanged()

        fun codeIncrement() = diffs.totalIncrement()

        fun uniqueAuthors() = diffs.map {
            it.authorEmailAddress
        }.toSet()

        data class StatisticsData(
            val percentile: Percentile,
            val linesAdded: Int,
            val totalChanged: Int,
            val codeIncrement: Int,
            val addedAvg: Int
        )

        class UserChanges(val data: Map<Range, Map<String, UserCommits>>) {
            fun userData(email: String, range: Range): UserCommits? {
                return data[range]?.get(email)
            }

            fun ranges() = data.keys

            fun activeUsers() = data.values.flatMap { it.keys }.toSet()
        }

        class UserCommits(val data: List<CommitDiff>) {
            val totalLinesAdded by lazy {
                data.totalLinesAdded()
            }

            val totalLinesRemoved by lazy {
                data.totalLinesRemoved()
            }

            val totalLinesChanged by lazy {
                data.totalChanged()
            }

            val totalIncrement by lazy {
                data.totalIncrement()
            }
        }
    }

    data class CommitDiff(
        val baseCommitSHA1: String,
        val targetCommitSHA1: String,
        val linesAdded: Int,
        val linesRemoved: Int,
        val ignoredLinesAdd: Int,
        val ignoredLinesRemove: Int,
        val authorEmailAddress: String
    ) {
        fun totalChanges() = linesAdded + linesRemoved

        fun codeIncrement() = linesAdded - linesRemoved
    }

    data class Summary(
        val rangedData: Map<Range, DiffWithRanges>,
        val total: DiffWithRanges
    )
}

fun List<CommitDiff>.totalLinesAdded() = sumOf { it.linesAdded }

fun List<CommitDiff>.totalLinesRemoved() = sumOf { it.linesRemoved }

fun List<CommitDiff>.totalChanged() = sumOf { it.totalChanges() }

fun List<CommitDiff>.totalIncrement() = sumOf { it.codeIncrement() }

fun List<CodeChangesReportItem.DiffWithRanges>.firstItemPercentile(): Percentile? {
    return firstOrNull()?.statisticsData?.percentile
}