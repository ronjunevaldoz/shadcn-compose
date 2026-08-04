// :core:testing — shared test utilities, fakes, and builders.
// This module is a TEST dependency only — never include it in production code.
//
// Replace GROUP_ID with your actual group ID.
plugins {
    id("GROUP_ID.core")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.core.testing"
    }

    sourceSets {
        commonMain.dependencies {
            // Expose test utilities as api() so consumers get them transitively
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
            implementation(projects.core.common)
        }
    }
}
