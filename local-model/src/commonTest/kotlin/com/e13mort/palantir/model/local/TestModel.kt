package com.e13mort.palantir.model.local

import com.e13mort.gitlabreport.model.local.DBProject

internal const val TEST_PROJECT_ID = 1L

class TestModel {
    internal val model = inMemoryModel()

    internal fun prepareTestProject() {
        model.projectQueries.insert(
            DBProject(
                TEST_PROJECT_ID,
                "test project",
                "ssh://test",
                "https://test"
            )
        )
    }
}

fun inMemoryModel(): LocalModel {
    val driver = DriverFactory("").createDriver(type = DriverType.MEMORY)
    return LocalModel(driver)
}