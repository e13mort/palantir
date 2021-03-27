package com.e13mort.palantir.cli.render

interface DateStringConverter {
    fun convertDateToString(date: Long): String
}