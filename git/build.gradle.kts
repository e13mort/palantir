/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

version = "unspecified"

kotlin {
    jvm()

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(project(":model-stubs"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlin.test.annotations.common)
                implementation(libs.io.kotest.assertions)
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
                implementation(project(":model"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.org.eclipse.jgit)
            }
        }
    }
}