package com.e13mort.palantir.model.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties

actual class DriverFactory(private val workDirectory: String) {
    companion object {
        const val DB_FILE_NAME = "data.sqlite"
    }

    actual fun createDriver(type: DriverType): SqlDriver {
        val url = when (type) {
            DriverType.FILE -> "jdbc:sqlite:${workDirectory}/${DB_FILE_NAME}"
            DriverType.MEMORY -> JdbcSqliteDriver.IN_MEMORY
        }

        return JdbcSqliteDriver(
            url = url,
            properties = Properties().apply { put("foreign_keys", "true") },
            schema = LocalModel.Schema
        )
    }
}