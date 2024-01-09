package com.e13mort.palantir.model.local

import app.cash.sqldelight.db.SqlDriver

enum class DriverType {
    FILE, MEMORY
}

expect class DriverFactory {
    fun createDriver(type: DriverType = DriverType.FILE): SqlDriver
}