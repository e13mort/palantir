/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.model.Percentile
import com.e13mort.palantir.render.ReportRender
import com.e13mort.palantir.utils.DateStringConverter
import com.e13mort.palantir.utils.asString
import com.e13mort.palantir.utils.secondsToFormattedTimeDiff
import com.jakewharton.picnic.table

class ASCIIPercentileReportRenderer(
    private val dateToStringConverter: DateStringConverter
) : ReportRender<PercentileReport, String, List<Percentile>> {
    override fun render(
        value: PercentileReport,
        params: List<Percentile>
    ): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row {
                    cell("Period")
                    cell("MR count")
                    params.forEach {
                        cell(it.name)
                    }
                }
            }
            for (i in 0 until value.periodsCount()) {
                row {
                    cell(value.period(i).asString(dateToStringConverter))
                    cell(value.totalMRCount(i))
                    params.forEach {
                        cell(value.formatPercentileString(i, it))
                    }
                }
            }
        }.toString()
    }

}


fun PercentileReport.formatPercentileString(
    index: Int,
    percentile: Percentile
): String {
    val content = StringBuilder(periodValue(index, percentile).secondsToFormattedTimeDiff())
    if (index != 0) {
        content.append("(${formatDiff(compareTwoPeriods(index, index - 1, percentile))}%)")
    }
    return content.toString()
}

private fun formatDiff(
    diff: Float
): String {
    val diffPercent = (diff * 100 - 100).toInt()
    return diffPercent.let {
        if (it > 0) "+${it}" else it.toString()
    }
}