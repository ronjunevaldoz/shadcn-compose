// Replace PROJECT_NAME with the actual project name (PascalCase)
rootProject.name = "PROJECT_NAME"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Platform apps
include(":androidApp")
// iosApp is an Xcode project — not a Gradle module

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:ui")

// Feature modules — add new features here following the pattern:
// include(":feature:FEATURE_NAME:api")
// include(":feature:FEATURE_NAME:domain")
// include(":feature:FEATURE_NAME:data")
// include(":feature:FEATURE_NAME:ui")
include(":feature:FEATURE_NAME:api")
include(":feature:FEATURE_NAME:domain")
include(":feature:FEATURE_NAME:data")
include(":feature:FEATURE_NAME:ui")
