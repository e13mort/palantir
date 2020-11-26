package com.e13mort.gitlab_report.model.local

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:local-db.sqlite")
        LocalModel.Schema.create(driver)
        return driver
    }
}