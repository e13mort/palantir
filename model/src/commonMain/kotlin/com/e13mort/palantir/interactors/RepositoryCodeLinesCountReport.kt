package com.e13mort.palantir.interactors

typealias CodeLinesResult = Map<String, List<RepositoryCodeLinesCountReport.LinesCountReportItem>>

interface RepositoryCodeLinesCountReport : RepositoryReport<CodeLinesResult> {
    data class LinesCountReportItem(val range: Range, val lines: Map<String, LinesCountItem>)

    data class LinesCountItem(val codeLines: Int)
}

fun List<RepositoryCodeLinesCountReport.LinesCountReportItem>.allKeys(): MutableSet<String> {
    val allColumns = mutableSetOf<String>()
    this.forEach {
        allColumns += it.lines.keys
    }
    return allColumns
}