/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.properties

interface Properties {
    enum class StringProperty(val defaultValue: String = "") {
        GITLAB_KEY,
        GITLAB_URL,
        PERIOD_DATE_FORMAT("dd-MM-yyyy"),
        PERCENTILES_IN_REPORTS
    }

    enum class IntProperty(val defaultValue: Int) {
        SYNC_PERIOD_MONTHS(1)
    }

    fun stringProperty(property: StringProperty): String?

    fun intProperty(property: IntProperty): Int?
}

fun Properties.safeStringProperty(property: Properties.StringProperty): String {
    return stringProperty(property)
        ?: throw IllegalStateException("Please provide property $property")
}

fun Properties.safeIntProperty(property: Properties.IntProperty): Int {
    return intProperty(property) ?: property.defaultValue
}

operator fun Properties.plus(nextHandler: Properties): Properties {
    return PropertyChainItem(this, nextHandler)
}