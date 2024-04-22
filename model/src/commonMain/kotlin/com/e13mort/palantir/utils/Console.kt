/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

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