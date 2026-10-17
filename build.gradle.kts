/**
 * Keeps the Android and Compose compiler plugins available to the app module.
 *
 * The buildscript dependency sets the Kotlin version used by AGP's built-in Kotlin support. It matches
 * the Compose compiler plugin version from the catalog. `apply false` resolves the plugins here while
 * the app module chooses which ones to apply. The separate Kotlin Android plugin is not applied.
 */
buildscript {
    dependencies {
        // Built-in Kotlin and the Compose compiler must use the same Kotlin version.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}
