pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
