package com.e13mort.gitlab_report.utils

interface Console {
    enum class WriteStyle {
        ADD, REPLACE_LAST
    }

    fun write(message: String, writeStyle: WriteStyle = WriteStyle.ADD)
}

fun String.writeTo(console: Console, writeStyle: Console.WriteStyle = Console.WriteStyle.ADD) = console.write(this, writeStyle)