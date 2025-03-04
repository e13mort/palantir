/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.jvm")
    application
}

version = "unspecified"

dependencies {
    implementation(compose.runtime)
    implementation(libs.mosaic)
    implementation(libs.com.github.ajalt.clikt)
    implementation(project(":model"))
    implementation(project(":local-model"))
    implementation(project(":remote-model"))
    implementation(project(":report:common"))
    implementation(project(":report:console"))
    implementation(project(":client:shared"))
}

application {
    mainClass.set("com.e13mort.palantir.cli.PlntrKt")
    applicationName = "plntr"
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}
