@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

version = "unspecified"

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.test.annotations.common)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(libs.org.gitlab4j.gitlab4j.api)
            }
        }
    }
}