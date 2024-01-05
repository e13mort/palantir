import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("com.jakewharton.mosaic:mosaic-gradle-plugin:0.10.0")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

apply(plugin = "com.jakewharton.mosaic")

version = "unspecified"

dependencies {
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
