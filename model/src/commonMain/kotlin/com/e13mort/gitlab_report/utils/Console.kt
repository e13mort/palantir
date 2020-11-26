package com.e13mort.gitlab_report.utils

fun interface Console {
    fun write(message: String)
}

fun String.writeTo(console: Console) = console.write(this)