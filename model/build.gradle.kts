/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

version = "unspecified"

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":model-stubs"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlin.test.annotations.common)
                implementation(libs.io.kotest.assertions)
            }
        }
    }
}