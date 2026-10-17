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
