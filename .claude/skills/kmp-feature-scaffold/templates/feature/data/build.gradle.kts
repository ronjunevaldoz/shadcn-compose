// Replace GROUP_ID and FEATURE_NAME with actual values.
// Plugin ID = GROUP_ID.feature.data (e.g. com.example.myapp.feature.data)
plugins {
    id("GROUP_ID.feature.data")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.api)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.common)
        }
    }
}

// Uncomment and configure if this feature needs its own SQLDelight database schema.
// sqldelight {
//     databases {
//         create("FEATURE_NAMEDatabase") {
//             packageName.set("GROUP_ID.feature.FEATURE_NAME.data.db")
//         }
//     }
// }
