package com.e13mort.palantir.interactors

interface RepositoryCommitsReport {
    data class RangeReportItem(val range: Range, val commitsCount: Int, val uniqueAuthorsEmails: Set<String>)

    val result: List<GroupedResults>
    data class GroupedResults(
        val groupName: String,
        val result: Map<String, List<RangeReportItem>>
    ) {
        fun totalCommitsCount() = result.values.sumOf { rangeReportItems ->
            rangeReportItems.totalCommitsCount()
        }

        fun totalAuthors(): Set<String> {
            val allAuthors = mutableSetOf<String>()
            result.values.forEach { rangeReportItems ->
                allAuthors += rangeReportItems.uniqueAuthors()
            }
            return allAuthors
        }
    }
}

fun List<RepositoryCommitsReport.RangeReportItem>.totalCommitsCount(): Int {
    return sumOf { reportItem ->
        reportItem.commitsCount
    }
}

fun List<RepositoryCommitsReport.RangeReportItem>.uniqueAuthors(): Set<String> {
    val set = mutableSetOf<String>()
    forEach { reportItem ->
        set += reportItem.uniqueAuthorsEmails
    }
    return set
}