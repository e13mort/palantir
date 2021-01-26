package com.e13mort.palantir.cli.properties

import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties as PlatformProperties

class FileBasedProperties(private val filePath: Path) : Properties {

    companion object {
        private const val PROPERTIES_FILENAME = "settings.properties"

        fun defaultInHomeDirectory(workDirectoryPath: Path): Properties {
            val propertiesFile = workDirectoryPath.resolve(PROPERTIES_FILENAME)
            if (!Files.exists(propertiesFile)) {
                createEmptyProperties(propertiesFile)
            }
            return FileBasedProperties(propertiesFile)
        }

        private fun createEmptyProperties(propertiesFile: Path) {
            Files.createFile(propertiesFile)
            val properties = PlatformProperties()
            for (value in Properties.StringProperty.values()) {
                properties[value.name] = ""
            }
            FileWriter(propertiesFile.toFile()).use {
                properties.store(it, null)
            }
        }
    }

    override fun stringProperty(property: Properties.StringProperty): String? {
        val propertyValue = FileReader(filePath.toFile()).use {
            PlatformProperties().apply {
                load(it)
            }[property.name]?.toString()?.trim()
        }
        if (propertyValue == null || propertyValue.isBlank()) return null
        return propertyValue
    }
}