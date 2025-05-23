/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.CodeChangesReportItem
import com.e13mort.palantir.interactors.RepositoryReport
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString

class CSVUserImpactReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryReport<CodeChangesReportItem>, String, UserImpactRenderOptions> {
    override fun render(
        value: RepositoryReport<CodeChangesReportItem>,
        params: UserImpactRenderOptions
    ): String {
        return StringBuilder().apply {
            value.result.forEach { groupedResult ->
                val reportItem = groupedResult.result
                val rangesResults = reportItem.calculateUserChanges()
                val allAvailableRanges = rangesResults.values.map { it.ranges() }.flatten().toSet()
                appendGroupHeader(groupedResult.groupName)
                rangesResults.forEach { item ->
                    val projectName = item.key
                    val rows = item.value.activeUsers()
                    appendGroupHeader(projectName)
                    if (!params.showOnlySummary) {
                        append("author - ${params.dataColumn.name}")
                        append(",")
                        append("groups")
                        append(",")
                        allAvailableRanges.forEachIndexed { index, it ->
                            if (index != 0)
                                append(",")
                            append(it.asString(formatter))
                        }
                        append("\n")
                        rows.forEach { row ->
                            append(row)
                            append(",")
                            append(reportItem.authorGroups[row]?.joinToString(":") ?: "")
                            append(",")
                            allAvailableRanges.forEachIndexed { columnIndex, column ->
                                val userData = item.value.userData(row, column)
                                val content = userData?.let {
                                    params.dataColumn.extract(it)
                                } ?: 0
                                if (columnIndex != 0)
                                    append(",")
                                append(content)
                            }
                            append("\n")
                        }
                    }
                }
                append("Summary")
                append("\n")
                val groupChanges = groupedResult.result.calculateUserChanges()
                val activeGroupUsers: Set<String> = groupChanges.values.map { it.activeUsers() }
                    .reduce { acc, strings -> acc + strings }
                append("author - ${params.dataColumn.name}")
                append(",")
                append("groups")
                append(",")
                append("\n")
                activeGroupUsers.forEach { row ->
                    append(row)
                    append(",")
                    append(reportItem.authorGroups[row]?.joinToString(":") ?: "")
                    append(",")
                    allAvailableRanges.forEachIndexed { index, column ->
                        val userData: Int = groupChanges.map {
                            it.value.userData(row, column)?.let {
                                params.dataColumn.extract(it)
                            } ?: 0
                        }.reduce { acc, i -> acc + i }
                        if (index != 0)
                            append(",")
                        append(userData)
                    }
                    append("\n")
                }
            }
        }.toString()
    }

    private fun StringBuilder.appendGroupHeader(
        title: String
    ) {
        append(title)
        append("\n")
    }

}
