import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin for shared test utilities across feature modules.
 *
 * Provides: kotlin.test, Turbine (Flow testing), coroutines-test, and
 * a dependency on :core:testing for shared fakes and builders.
 * Targets: Android, iOS, Desktop (JVM), Web (JS + WasmJs).
 *
 * Apply this plugin in any module's TEST source sets that need shared test infra.
 * It is NOT applied automatically — add it per-module where needed.
 *
 * Each module must add its own namespace:
 *   kotlin { androidLibrary { namespace = "com.example.feature.auth.test" } }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
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
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
