package com.e13mort.palantir.model.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DriverFactory(private val workDirectory: String) {
    companion object {
        const val DB_FILE_NAME = "data.sqlite"
    }

    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${workDirectory}/${DB_FILE_NAME}")
        LocalModel.Schema.create(driver)
        return driver
    }
}