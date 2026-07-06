import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for :feature:*:data modules.
 *
 * KMP with platform-specific implementations.
 * Provides: Ktor (networking), SQLDelight (persistence), Koin wiring.
 * Does NOT depend on :domain — siblings only. Depends on :api (declared per-module).
 * Targets: Android, iOS, Desktop (JVM), Web (JS + WasmJs).
 *
 * Each module must add:
 *   - namespace in androidLibrary {}
 *   - sqldelight {} block if persistence is needed
 *   - implementation(projects.feature.*.api) in commonMain
 *
 * Each module must add its own namespace:
 *   kotlin { androidLibrary { namespace = "com.example.feature.auth.data" } }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.koin")
    id("app.cash.sqldelight")
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
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.bundles.ktor.common)
            implementation(libs.bundles.sqldelight.common)
            // :feature:*:api + :core:* dependencies declared per-module
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        // Desktop: CIO engine (JVM-based, no extra setup required)
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        // Web: JS engine (works for both js and wasmJs targets)
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
