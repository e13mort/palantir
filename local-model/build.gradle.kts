buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.4.4")
    }
}

plugins {
    id("com.squareup.sqldelight") version "1.4.4"
    kotlin("multiplatform")
}
version = "unspecified"

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":model"))
                implementation("com.squareup.sqldelight:runtime:1.4.4")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.4.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
                implementation("org.xerial:sqlite-jdbc:3.34.0")
            }
        }
    }
}

sqldelight {
    database("LocalModel") {
        packageName = "com.e13mort.gitlab_report.model.local"
    }
}