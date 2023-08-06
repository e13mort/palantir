dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven( "https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "gitlab-report"
include("model")
include(":client:shared")
include(":client:cli")
include(":client:desktop")
include("local-model")
include("remote-model")
include("report:common")
findProject(":report:common")
include(":report:console")
