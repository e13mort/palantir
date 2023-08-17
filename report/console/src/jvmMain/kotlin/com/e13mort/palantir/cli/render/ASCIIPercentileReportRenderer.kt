package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PercentileReport
import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.model.ReportsRepository
import com.jakewharton.picnic.table

class ASCIIPercentileReportRenderer(
    private val dateToStringConverter: DateStringConverter,
    private val requestedPercentiles: List<ReportsRepository.Percentile>,
    private val showBorders: Boolean = true
) : ReportRender<PercentileReport, String> {
    override fun render(value: PercentileReport): String {
        return table {
            cellStyle {
                border = showBorders
            }
            header {
                row {
                    cell("Period")
                    cell("MR count")
                    requestedPercentiles.forEach {
                        cell(it.name)
                    }
                }
            }
            for (i in 0 until value.periodsCount()) {
                row {
                    cell(period(value, i))
                    cell(value.totalMRCount(i))
                    requestedPercentiles.forEach {
                        val content = StringBuilder(formatTimeDiff(value.periodValue(i, it)))
                        if (i != 0) {
                            content.append("(${formatDiff(value.compareTwoPeriods(i, i - 1, it))}%)")
                        }
                        cell(content)
                    }
                }
            }
        }.toString()
    }

    private fun formatDiff(
        diff: Float
    ): String {
        val diffPercent = (diff * 100 - 100).toInt()
        return diffPercent.let {
            if (it > 0) "+${it}" else it.toString()
        }
    }

    private fun period(
        value: PercentileReport,
        i: Int
    ): String {
        return value.period(i).let {
            "${dateToStringConverter.convertDateToString(it.start)} - ${dateToStringConverter.convertDateToString(it.end)}"
        }
    }

    private fun formatTimeDiff(l: Long): String {
        return StringBuilder().apply {
            (l / (24 * 60 * 60)).let {
                if (it > 0) append("${it}d ")
            }
            ((l / (60 * 60) % 24)).let {
                if (it > 0) append("${it}h ")
            }
            ((l / 60) % 60).let {
                if (it > 0) append("${it}m ")
            }
            if (isEmpty()) append("Invalid")
        }.toString()
    }


}