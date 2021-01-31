plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

version = "unspecified"

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
    mainClass.set("com.e13mort.palantir.cli.PlntrKt")
    applicationName = "plntr"
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
