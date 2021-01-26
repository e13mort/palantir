package com.e13mort.palantir.cli.properties

class EnvironmentProperties : Properties {

    override fun stringProperty(property: Properties.StringProperty): String? {
        return System.getenv("PALANTIR_${property.name.toUpperCase()}")
    }

}