# Step 1: Create :core:designsystem module

Part of `kmp-compose-design-system`. Load this file when working on: step 1: create :core:designsystem module.

---

Create `core/designsystem/build.gradle.kts`:

```kotlin
plugins {
    id("GROUP_ID.core")          // applies KMP + Compose targets
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            // No compose.material3 — fully custom
        }
    }
}
```

Register in `settings.gradle.kts`:

```kotlin
include(":core:designsystem")
```

---

