import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for :feature:*:ui modules.
 *
 * Compose Multiplatform (CMP) screens and ViewModels.
 * Depends on :feature:*:api and :feature:*:domain (declared per-module).
 * Targets: Android, iOS, Desktop (JVM), Web (JS + WasmJs).
 *
 * Each module must add its own namespace:
 *   kotlin { androidLibrary { namespace = "com.example.feature.auth.ui" } }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.koin")
}

kotlin {
    // iOS
    iosArm64()
    iosSimulatorArm64()

    // Desktop
    jvm()

    // Web
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            // :feature:*:api, :feature:*:domain, :core:ui declared per-module
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        // Desktop: Swing dispatcher required for coroutines on JVM UI thread
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Make uiTooling available for Compose Preview in debug builds
dependencies {
    androidRuntimeClasspath(compose.uiTooling)
}
