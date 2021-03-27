package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.ReportRender
import com.e13mort.palantir.model.ReportsRepository
import com.jakewharton.picnic.table

class ASCIIPercentileReportRenderer(private val dateToStringConverter: DateStringConverter) : ReportRender<PercentileInteractor.PercentileReport, String> {
    override fun render(value: PercentileInteractor.PercentileReport): String {
        return table {
            cellStyle {
                border = true
            }
            val percentiles = ReportsRepository.Percentile.values().toList()
            header {
                row {
                    cell("Period")
                    percentiles.forEach {
                        cell(it.name)
                    }
                }
            }
            for (i in 0 until value.periodsCount()) {
                row {
                    cell(period(value, i))
                    percentiles.forEach {
                        cell(formatTimeDiff(value.periodValue(i, it)))
                    }
                }
            }
        }.toString()
    }

    private fun period(
        value: PercentileInteractor.PercentileReport,
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
            (l % 60).let {
                if (it > 0) append("${it}s ")
            }
            if (isEmpty()) append("Invalid")
        }.toString()
    }


}