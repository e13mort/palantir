/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

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