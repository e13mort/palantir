@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.sqldelight)
}
version = "unspecified"

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(libs.sqldelight.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":model-stubs"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlin.test.annotations.common)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.jvm)
                implementation(libs.org.slf4j.nop) // for sqldelight which depends on slf4j-api
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

sqldelight {
    databases {
        create("LocalModel") {
            packageName.set("com.e13mort.palantir.model.local")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}