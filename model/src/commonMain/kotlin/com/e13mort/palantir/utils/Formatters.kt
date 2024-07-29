/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.utils

import com.e13mort.palantir.interactors.Range

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

fun Range.asString(
    dateStringConverter: DateStringConverter
): String {
    return let {
        "${it.start.toDateString(dateStringConverter)} - ${it.end.toDateString(dateStringConverter)}"
    }
}

fun Long.toDateString(dateStringConverter: DateStringConverter): String {
    return dateStringConverter.convertDateToString(this)
}

fun interface DateStringConverter {
    fun convertDateToString(date: Long): String
}

fun interface StringDateConverter {
    fun convertStringToDate(string: String): Long
}

fun Range.asInputString(dateStringConverter: DateStringConverter): String {
    return "${start.toDateString(dateStringConverter)}:${end.toDateString(dateStringConverter)}"
}

