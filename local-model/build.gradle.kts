buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.4.3")
    }
}

plugins {
    id("com.squareup.sqldelight") version "1.4.3"
    kotlin("multiplatform") version "1.4.10"
}
version = "unspecified"

repositories {
    mavenCentral()
    jcenter()
}

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
                implementation("com.squareup.sqldelight:runtime:1.4.3")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.4.3")
//                implementation("org.jetbrains.exposed:exposed-core:0.24.1")
//                implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
//                implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")
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
                implementation("com.squareup.sqldelight:sqlite-driver:1.4.3")
            }
        }
    }
}

sqldelight {
    database("LocalModel") {
        packageName = "com.e13mort.gitlab_report.model.local"
    }
}