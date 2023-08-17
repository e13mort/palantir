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
                api(project(":report:common")) //temporal for fast visualisation
                api(project(":report:console")) //temporal for fast visualisation
            }
        }
        val jvmMain by getting {
            dependencies {
                dependencies {
                    api(compose.desktop.common)
                }
            }
        }
    }
}
