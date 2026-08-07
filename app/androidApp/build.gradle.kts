import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Catalog app's own version, derived from the library's VERSION_NAME (gradle.properties)
// so a real semver bump always produces a strictly higher versionCode -- a hardcoded
// literal passes every local check and only fails as a hard Play Console rejection on
// the second upload.
val libVersionName = providers.gradleProperty("VERSION_NAME").get()
val (libMajor, libMinor, libPatch) = libVersionName.split(".").map { it.toInt() }
val catalogVersionCode = libMajor * 1_000_000 + libMinor * 1_000 + libPatch

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "io.github.ronjunevaldoz.shadcncompose.catalog"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.ronjunevaldoz.shadcncompose"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = catalogVersionCode
        versionName = libVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
