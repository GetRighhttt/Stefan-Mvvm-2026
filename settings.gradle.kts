/**
 * Defines the repositories and the single app module before project build scripts are evaluated.
 *
 * pluginManagement resolves Gradle plugins. dependencyResolutionManagement resolves app and test
 * libraries and rejects module-specific repository declarations. Keeping those repositories here gives
 * both UI examples the same dependency sources. The root project is named StateFlow and includes :app.
 */
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

rootProject.name = "StateFlow"
include(":app")
