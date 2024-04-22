/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.model.local

import app.cash.sqldelight.db.SqlDriver

enum class DriverType {
    FILE, MEMORY
}

expect class DriverFactory {
    fun createDriver(type: DriverType = DriverType.FILE): SqlDriver
}