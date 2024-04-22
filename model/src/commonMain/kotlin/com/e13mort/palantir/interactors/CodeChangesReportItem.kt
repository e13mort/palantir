/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile

data class CodeChangesReportItem(
    val commitDiffs: Map<String, List<DiffWithRanges>>,
    val summary: Summary
) {
    data class DiffWithRanges(
        val range: Range,
        val diffs: List<CommitDiff>,
        val statisticsData: StatisticsData
    ) {
        fun totalAdded() = diffs.sumOf {
            it.linesAdded
        }

        fun totalRemoved() = diffs.sumOf {
            it.linesRemoved
        }

        fun totalChanged() = diffs.sumOf {
            it.totalChanges()
        }

        fun codeIncrement() = diffs.sumOf {
            it.codeIncrement()
        }

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

fun List<CodeChangesReportItem.DiffWithRanges>.firstItemPercentile(): Percentile? {
    return firstOrNull()?.statisticsData?.percentile
}