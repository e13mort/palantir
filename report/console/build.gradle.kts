@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

version = "unspecified"

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":model"))
                implementation(project(":report:common"))
                implementation(libs.com.jakewharton.picnic)
                implementation(libs.org.jsoup)
            }
        }
    }
}
