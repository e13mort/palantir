plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    application
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.0.1")
    implementation(kotlin("stdlib-common"))
    implementation(project(":model"))
    implementation(project(":local-model"))
    implementation(project(":remote-model"))
    implementation(project(":report:common"))
    implementation(project(":report:console"))
}

application {
    mainClass.set("com.e13mort.gitlab_report.cli.CliKt")
}
