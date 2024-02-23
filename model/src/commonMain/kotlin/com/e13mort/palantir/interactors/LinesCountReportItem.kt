package com.e13mort.palantir.interactors


typealias CodeLinesResult = Map<String, List<LinesCountReportItem>>

data class LinesCountReportItem(val range: Range, val lines: Map<String, LinesCountItem>) {
    data class LinesCountItem(val codeLines: Int)
}

fun List<LinesCountReportItem>.allKeys(): MutableSet<String> {
    val allColumns = mutableSetOf<String>()
    this.forEach {
        allColumns += it.lines.keys
    }
    return allColumns
}
