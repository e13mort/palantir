package com.e13mort.palantir.utils

interface Console {

    companion object Empty : Console {
        override fun write(message: String, writeStyle: WriteStyle) = Unit
    }

    enum class WriteStyle {
        ADD, REPLACE_LAST
    }

    fun write(message: String, writeStyle: WriteStyle = WriteStyle.ADD)
}

fun String.writeTo(console: Console, writeStyle: Console.WriteStyle = Console.WriteStyle.ADD) =
    console.write(this, writeStyle)