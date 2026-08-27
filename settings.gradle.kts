pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "tamanho-certo"

include(":app")
include(":core:model")
include(":core:ui")
include(":core:files")
include(":core:ads")
include(":imaging")
include(":pdf")
include(":engine")
include(":feature:tools")
