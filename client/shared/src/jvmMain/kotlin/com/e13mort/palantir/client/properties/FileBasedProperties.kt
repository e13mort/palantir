/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.client.properties

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
                properties[value.name] = value.defaultValue
            }
            for (value in Properties.IntProperty.values()) {
                properties[value.name] = value.defaultValue.toString()
            }
            FileWriter(propertiesFile.toFile()).use {
                properties.store(it, null)
            }
        }
    }

    override fun stringProperty(property: Properties.StringProperty): String? {
        val propertyValue = rawProperty(property.name)
        if (propertyValue == null || propertyValue.isBlank()) return null
        return propertyValue
    }

    override fun intProperty(property: Properties.IntProperty): Int? {
        return (rawProperty(property.name) ?: return null).toIntOrNull()
    }

    private fun rawProperty(property: String): String? {
        return FileReader(filePath.toFile()).use {
            PlatformProperties().apply {
                load(it)
            }[property]?.toString()?.trim()
        }
    }
}