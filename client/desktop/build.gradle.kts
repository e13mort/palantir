import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.desktop)
}

kotlin {
    jvm("desktop") {
        withJava()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":client:shared"))
                implementation(project(":model"))
                implementation(project(":local-model"))
                implementation(project(":remote-model"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.e13mort.palantir.client.desktop.PlntrDesktopKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Palantir"
            packageVersion = "1.0.0"

            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "f8bb99df-6518-4bde-8977-42bf6282e6f7"
            }

            macOS {
                // Use -Pcompose.desktop.mac.sign=true to sign and notarize.
                bundleID = "com.github.e13mort.plntr"
            }
        }
    }
}