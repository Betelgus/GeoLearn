pluginManagement {
    repositories {
        google {
            content {includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Corrected Kotlin DSL syntax below:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "geolearn"
include(":app")
