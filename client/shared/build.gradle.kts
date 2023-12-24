@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.desktop)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.components.resources)
                api(libs.premo.main)
                api(libs.premo.navigation)
                api(project(":model"))
                api(project(":local-model"))
                api(project(":remote-model"))
                api(libs.io.github.chozzle.composemacostheme)
            }
        }
        val jvmMain by getting {
            dependencies {
                dependencies {
                    api(compose.desktop.common)
                }
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
