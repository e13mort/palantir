plugins {
    kotlin("multiplatform") version "1.4.10"
}

version = "unspecified"

repositories {
    mavenCentral()
}

repositories {
    jcenter()
}

kotlin {
    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":model"))
                implementation(project(":report:common"))
                implementation("com.jakewharton.picnic:picnic:0.5.0")
            }
        }
    }
}
