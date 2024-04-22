/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.properties

internal class PropertyChainItem(
    private val activeHandler: Properties,
    private val nextHandler: Properties
) : Properties {
    override fun stringProperty(property: Properties.StringProperty): String? {
        return activeHandler.stringProperty(property) ?: nextHandler.stringProperty(property)
    }

    override fun intProperty(property: Properties.IntProperty): Int? {
        return activeHandler.intProperty(property) ?: nextHandler.intProperty(property)
    }

}