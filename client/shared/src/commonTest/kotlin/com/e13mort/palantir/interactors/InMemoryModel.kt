package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.local.DriverFactory
import com.e13mort.palantir.model.local.DriverType
import com.e13mort.palantir.model.local.LocalModel

internal fun inMemoryModel(): LocalModel {
    val driver = DriverFactory("").createDriver(type = DriverType.MEMORY)
    return LocalModel(driver)
}