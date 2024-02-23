@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

version = "unspecified"

kotlin {
    jvm()

    sourceSets {
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