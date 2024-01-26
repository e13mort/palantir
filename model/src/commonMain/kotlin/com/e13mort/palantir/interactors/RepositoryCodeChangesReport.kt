package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Percentile

interface RepositoryCodeChangesReport {
    data class CodeChangesReportItem(val commitDiffs: Map<String, List<DiffWithRanges>>)

    data class DiffWithRanges(
        val range: Range,
        val diffs: List<CommitDiff>,
        val percentileData: PercentileData
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

        data class PercentileData(
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


    val result: List<GroupedResults>

    data class GroupedResults(
        val groupName: String,
        val result: CodeChangesReportItem
    )
}

fun List<RepositoryCodeChangesReport.DiffWithRanges>.firstItemPercentile(): Percentile? {
    return firstOrNull()?.percentileData?.percentile
}