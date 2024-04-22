/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.properties

class EnvironmentProperties : Properties {

    companion object {
        const val PROPERTY_PREFIX = "PALANTIR"
    }

    override fun stringProperty(property: Properties.StringProperty): String? {
        return readRawProperty(property.name)
    }

    override fun intProperty(property: Properties.IntProperty): Int? {
        return (readRawProperty(property.name) ?: return null).toIntOrNull()
    }

    private fun readRawProperty(property: String) =
        System.getenv("${PROPERTY_PREFIX}_${property}")

}