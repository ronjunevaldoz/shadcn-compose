// Replace GROUP_ID and FEATURE_NAME with actual values.
// Plugin ID = GROUP_ID.feature.ui (e.g. com.example.myapp.feature.ui)
plugins {
    id("GROUP_ID.feature.ui")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.api)
            implementation(projects.feature.FEATURE_NAME.domain)
            implementation(projects.core.ui)
            implementation(projects.core.common)
        }
    }
}
