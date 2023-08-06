import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

version = "unspecified"

dependencies {
    implementation(libs.com.github.ajalt.clikt)
    implementation(project(":model"))
    implementation(project(":local-model"))
    implementation(project(":remote-model"))
    implementation(project(":report:common"))
    implementation(project(":report:console"))
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
