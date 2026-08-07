# Step 1: Update :core:database/build.gradle.kts

Part of `kmp-sqldelight-setup`. Load this file when working on: step 1: update :core:database/build.gradle.kts.

---

The `GROUP_ID.core` plugin already adds all KMP targets. Add `sqldelight {}` and platform drivers:

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.GROUP_ID.core)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.core.database"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)   // Desktop
        }
        jsMain.dependencies {
            implementation(libs.sqldelight.web.worker.driver)
            implementation(npm("sql.js", "1.6.2"))
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
        // wasmJsMain: no SQLDelight driver available — skip or use alternative
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("GROUP_ID.core.database")
            // Enable schema versioning for migrations
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}
```

---

