// Replace GROUP_ID and FEATURE_NAME with actual values.
// Plugin ID = GROUP_ID.feature.domain (e.g. com.example.myapp.feature.domain)
plugins {
    id("GROUP_ID.feature.domain")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.api)
        }
    }
}
