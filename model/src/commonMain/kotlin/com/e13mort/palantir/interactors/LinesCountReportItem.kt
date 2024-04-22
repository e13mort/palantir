/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

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
