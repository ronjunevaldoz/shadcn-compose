import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for :androidApp.
 *
 * Android application entry point for the KMP project.
 * Wires together all :feature:*:ui modules.
 *
 * The app's build.gradle.kts must declare:
 *   android { namespace = "com.example.app" defaultConfig { applicationId = "..." } }
 *   and add all :feature:*:ui dependencies.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.koin")
    id("com.codingfeline.buildkonfig")
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // applicationId, versionCode, versionName declared per-module
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.bundles.koin.app)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.core.ktx)
    // :feature:*:ui modules declared per-module
}
