// Replace GROUP_ID and FEATURE_NAME with actual values.
// Namespace format: GROUP_ID.feature.FEATURE_NAME.api
// Plugin ID = GROUP_ID.feature.api (e.g. com.example.myapp.feature.api)
plugins {
    id("GROUP_ID.feature.api")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.api"
    }
}
