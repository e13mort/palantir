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
import com.jakewharton.picnic.table

class ASCIIUserImpactReportRender(
    private val formatter: DateStringConverter
) : ReportRender<RepositoryReport<CodeChangesReportItem>, String, UserImpactRenderOptions> {
    override fun render(
        value: RepositoryReport<CodeChangesReportItem>,
        params: UserImpactRenderOptions
    ): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                val reportItem: CodeChangesReportItem = groupedResult.result
                val groupName = groupedResult.groupName
                val rangesResults = reportItem.calculateUserChanges()
                val allAvailableRanges = rangesResults.values.map { it.ranges() }.flatten().toSet()
                val columnsCount = allAvailableRanges.size + 1
                row {
                    cell(groupName) {
                        columnSpan = columnsCount
                    }
                }
                rangesResults.forEach { item ->
                    val projectName = item.key
                    val rows = item.value.activeUsers()
                    row {
                        cell(projectName) {
                            columnSpan = columnsCount
                        }
                    }
                    if (!params.showOnlySummary) {
                        row {
                            cell("author - ${params.dataColumn.name}")
                            allAvailableRanges.forEach {
                                cell(it.asString(formatter))
                            }
                        }
                        rows.forEach { row ->
                            row {
                                cell(row)
                                allAvailableRanges.forEach { column ->
                                    val userData = item.value.userData(row, column)
                                    val content = userData?.let {
                                        params.dataColumn.extract(it)
                                    } ?: "-"
                                    cell(content)
                                }
                            }
                        }
                    }
                }
                row {
                    cell("Summary: ${groupedResult.groupName}") {
                        columnSpan = columnsCount
                    }
                }
                val groupChanges = groupedResult.result.calculateUserChanges()
                val activeGroupUsers: Set<String> = groupChanges.values.map { it.activeUsers() }
                    .reduce { acc, strings ->
                        acc + strings
                    }
                row {
                    cell("author - ${params.dataColumn.name}")
                    allAvailableRanges.forEach {
                        cell(it.asString(formatter))
                    }
                }
                activeGroupUsers.forEach { row ->
                    row {
                        cell(row)
                        allAvailableRanges.forEach { column ->
                            val userData: Int = groupChanges.map {
                                it.value.userData(row, column)?.let {
                                    params.dataColumn.extract(it)
                                } ?: 0
                            }.reduce { acc, i -> acc + i }
                            cell(userData)
                        }
                    }
                }
            }
        }.toString()
    }
}
