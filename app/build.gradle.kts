/**
 * Builds both login renderers into the same APK so switching UI does not change the app or ViewModel.
 *
 * The Android block keeps the namespace, SDK levels, bytecode target, and release settings together.
 * View Binding generates the XML binding classes; Compose enables compiler support for the Composable
 * screen. The Kotlin bytecode target matches Java 17. Language and API versions are pinned to 2.4,
 * where explicit backing fields are stable, so the example needs no experimental compiler flag.
 *
 * Dependencies use the version catalog. The Compose BOM supplies compatible UI library versions for
 * both app and instrumentation tests. Coroutine tests exercise the ViewModel on the JVM; Espresso and
 * Compose tests exercise the activity with either renderer. Release minification is disabled, so the
 * configured ProGuard rules take effect only if it is enabled later.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.stateflow"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.stateflow"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
