package com.e13mort.palantir.cli.properties

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