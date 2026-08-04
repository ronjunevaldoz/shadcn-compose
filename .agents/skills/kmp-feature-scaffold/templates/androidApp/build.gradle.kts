// Replace GROUP_ID, PROJECT_NAME, and APPLICATION_ID with actual values.
// Plugin ID = GROUP_ID.android.app (e.g. com.example.myapp.android.app)
plugins {
    id("GROUP_ID.android.app")
}

android {
    namespace = "GROUP_ID"

    defaultConfig {
        applicationId = "APPLICATION_ID"
        versionCode = 1
        versionName = "1.0.0"
    }
}

// BuildKonfig: KMP equivalent of Android BuildConfig.
// Generates BuildKonfig object accessible from commonMain.
buildkonfig {
    packageName = "GROUP_ID"

    defaultConfigs {
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "APP_NAME", "PROJECT_NAME")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "BASE_URL", "https://api.example.com")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    }

    targetConfigs {
        create("debug") {
            buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN, "DEBUG", "true")
            buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "BASE_URL", "https://api-staging.example.com")
        }
    }
}

dependencies {
    // Wire all feature UI modules here
    implementation(projects.feature.FEATURE_NAME.ui)
    implementation(projects.core.ui)
    implementation(projects.core.common)
}
