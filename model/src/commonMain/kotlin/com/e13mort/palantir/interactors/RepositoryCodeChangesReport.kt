package com.e13mort.palantir.interactors

interface RepositoryCodeChangesReport {
    data class CodeChangesReportItem(val commitDiffs: Map<String, List<DiffWithRanges>>)

    data class DiffWithRanges(
        val range: Range,
        val diffs: List<CommitDiff>
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