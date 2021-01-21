package com.e13mort.palantir.cli.render

import com.e13mort.palantir.interactors.PercentileInteractor
import com.e13mort.palantir.interactors.ReportRender
import com.jakewharton.picnic.table

class ASCIIPercentileReportRenderer : ReportRender<PercentileInteractor.PercentileReport, String> {
    override fun render(value: PercentileInteractor.PercentileReport): String {
        return table {
            cellStyle {
                border = true
            }
            header {
                row("Percentile", "Time")
            }
            value.iterate { percentile, l ->
                row(
                    percentile.name,
                    formatTimeDiff(l)
                )
            }
        }.toString()
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