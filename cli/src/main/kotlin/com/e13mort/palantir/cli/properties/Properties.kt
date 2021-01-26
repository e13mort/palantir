package com.e13mort.palantir.cli.properties

interface Properties {
    enum class StringProperty {
        GITLAB_KEY,
        GITLAB_URL
    }

    enum class IntProperty(val defaultValue: Int) {
        SYNC_PERIOD_MONTHS(1)
    }

    fun stringProperty(property: StringProperty): String?

    fun intProperty(property: IntProperty): Int?
}

fun Properties.safeStringProperty(property: Properties.StringProperty) : String {
    return stringProperty(property) ?: throw IllegalStateException("Please provide property $property")
}

fun Properties.safeIntProperty(property: Properties.IntProperty) : Int {
    return intProperty(property) ?: property.defaultValue
}

operator fun Properties.plus(nextHandler: Properties) : Properties {
    return PropertyChainItem(this, nextHandler)
}