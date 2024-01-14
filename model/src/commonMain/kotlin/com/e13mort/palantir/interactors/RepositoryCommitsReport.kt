package com.e13mort.palantir.interactors

interface RepositoryCommitsReport {
    data class RangeReportItem(val range: Range, val commitsCount: Int, val uniqueAuthorsEmailsCount: Int)
    val result: Map<String, List<RangeReportItem>>
}