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
) : ReportRender<RepositoryReport<CodeChangesReportItem>, String, DataColumn> {
    override fun render(
        value: RepositoryReport<CodeChangesReportItem>,
        params: DataColumn
    ): String {
        return table {
            cellStyle {
                border = true
            }
            value.result.forEach { groupedResult ->
                val reportItem = groupedResult.result
                val rangesResults = reportItem.calculateUserChanges()
                val allAvailableRanges = rangesResults.values.map { it.ranges() }.flatten()
                val columnsCount = allAvailableRanges.size + 1
                row {
                    cell(groupedResult.groupName) {
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
                    row {
                        cell("author - ${params.name}")
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
                                    params.extract(it)
                                } ?: "-"
                                cell(content)
                            }
                        }
                    }
                }
            }
        }.toString()
    }
}
