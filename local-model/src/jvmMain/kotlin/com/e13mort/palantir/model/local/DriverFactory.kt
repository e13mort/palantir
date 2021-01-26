package com.e13mort.palantir.model.local

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

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