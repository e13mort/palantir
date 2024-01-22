package com.e13mort.palantir.interactors

interface RepositoryCodeLinesCountReport {
    data class LinesCountReportItem(val range: Range, val lines: Map<String, LinesCountItem>)

    data class LinesCountItem(val codeLines: Int)

    val result: List<GroupedResults>

    data class GroupedResults(
        val groupName: String,
        val result: Map<String, List<LinesCountReportItem>>
    )
}

fun List<RepositoryCodeLinesCountReport.LinesCountReportItem>.allKeys(): MutableSet<String> {
    val allColumns = mutableSetOf<String>()
    this.forEach {
        allColumns += it.lines.keys
    }
    return allColumns
}