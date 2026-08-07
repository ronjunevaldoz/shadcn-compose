# Step 6: Feature Module build.gradle.kts Templates

Part of `kmp-feature-scaffold`. Load this file when working on: step 6: feature module build.gradle.kts templates.

---

For each new feature `FEATURE_NAME` with group `GROUP_ID`, create these six files.
Replace `FEATURE_NAME` and `GROUP_ID` with actual values.

### `:feature:FEATURE_NAME:model/build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.model")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.model"
    }
}
```

### `:feature:FEATURE_NAME:api/build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.api")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.feature.FEATURE_NAME.model)
        }
    }
}
```

### `:feature:FEATURE_NAME:domain/build.gradle.kts`

```kotlin
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
```

### `:feature:FEATURE_NAME:data/build.gradle.kts`

```kotlin
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
        }
    }
}
```

### `:feature:FEATURE_NAME:presenter/build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.presenter")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.presenter"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.domain)
        }
    }
}
```

### `:feature:FEATURE_NAME:ui/build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.ui")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.presenter)
            implementation(projects.core.ui)
        }
    }
}
```

---

