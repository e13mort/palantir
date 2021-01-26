package com.e13mort.palantir.cli.properties

import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties as PlatformProperties

class FileBasedProperties(private val filePath: Path) : Properties {

    companion object {
        private const val WORK_DIRECTORY_NAME = ".plntr"
        private const val PROPERTIES_FILENAME = "settings.properties"
        fun defaultInHomeDirectory(): Properties {
            val homeDirectoryStr =
                System.getProperty("user.home") ?: throw IllegalStateException("Can't find home directory user.home")
            val homeDirectoryPath = Paths.get(homeDirectoryStr)
            if (!Files.exists(homeDirectoryPath)) throw IllegalStateException("Home directory doesn't exists")
            val propertiesDirectory = homeDirectoryPath.resolve(WORK_DIRECTORY_NAME)
            if (!Files.exists(propertiesDirectory)) {
                Files.createDirectory(propertiesDirectory)
            }
            val propertiesFile = propertiesDirectory.resolve(PROPERTIES_FILENAME)
            if (!Files.exists(propertiesFile)) {
                Files.createFile(propertiesFile)
                val properties = PlatformProperties()
                for (value in Properties.StringProperty.values()) {
                    properties[value.name] = ""
                }
                FileWriter(propertiesFile.toFile()).use {
                    properties.store(it, null)
                }
            }
            return FileBasedProperties(propertiesFile)
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