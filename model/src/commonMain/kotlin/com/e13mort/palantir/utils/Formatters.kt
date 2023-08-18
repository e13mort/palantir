package com.e13mort.palantir.utils

import com.e13mort.palantir.interactors.PercentileReport

fun Long.secondsToFormattedTimeDiff(): String {
    return StringBuilder().apply {
        (this@secondsToFormattedTimeDiff / (24 * 60 * 60)).let {
            if (it > 0) append("${it}d ")
        }
        ((this@secondsToFormattedTimeDiff / (60 * 60) % 24)).let {
            if (it > 0) append("${it}h ")
        }
        ((this@secondsToFormattedTimeDiff / 60) % 60).let {
            if (it > 0) append("${it}m ")
        }
        if (isEmpty()) append("Invalid")
    }.toString()
}

fun PercentileReport.period(
    i: Int,
    dateStringConverter: DateStringConverter
): String {
    return period(i).let {
        "${it.start.toDateString(dateStringConverter)} - ${it.end.toDateString(dateStringConverter)}"
    }
}

fun Long.toDateString(dateStringConverter: DateStringConverter): String {
    return dateStringConverter.convertDateToString(this)
}

fun interface DateStringConverter {
    fun convertDateToString(date: Long): String
}