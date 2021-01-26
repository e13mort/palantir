package com.e13mort.palantir.cli.properties

interface Properties {
    enum class StringProperty {
        GITLAB_KEY,
        GITLAB_URL
    }

    fun stringProperty(property: StringProperty): String?
}

fun Properties.safeStringProperty(property: Properties.StringProperty) : String {
    return stringProperty(property) ?: throw IllegalStateException("Please provide property $property")
}

operator fun Properties.plus(nextHandler: Properties) : Properties {
    return PropertyChainItem(this, nextHandler)
}