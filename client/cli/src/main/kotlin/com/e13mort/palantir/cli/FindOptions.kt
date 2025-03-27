/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli

import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.OptionWithValues

inline fun <reified T> List<Option>.findOptionSafe(name: String): T? {
    val dataColumns = findOptions<T>(name)
    return dataColumns.firstOrNull()
}

fun <T>List<Option>.asSingleFlagSet(name: String, value: T): Set<T> {
    return if(findOptionSafe<Boolean>(name) == true) setOf(value) else emptySet()
}

fun List<Option>.hasFlag(vararg names: String): Boolean {
    names.forEach {
        if(findOptionSafe<Boolean>(it) == true) return true
    }
    return false
}

inline fun <reified T> List<Option>.findOption(name: String): T {
    val dataColumns = findOptions<T>(name)
    return dataColumns.first()
}

inline fun <reified T> List<Option>.findOptions(
    name: String
): List<T & Any> {
    val dataColumns = mapNotNull { option ->
        if (option.names.contains(name) && option is OptionWithValues<*, *, *>) {
            if (option.value is T) {
                return@mapNotNull option.value as T
            }
        }
        null
    }
    return dataColumns
}