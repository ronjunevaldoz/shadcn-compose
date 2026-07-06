---
name: kotlin-multiplatform-feature-scaffold
description: >
  Scaffolds a production-ready Kotlin Multiplatform (KMP) multi-feature module
  architecture. Creates a full project by generating from the official Kotlin/kmp-wizard
  AGP 9 baseline, usually the `all-targets` branch for Android, iOS, Web, Desktop, and
  Server, or adds a new feature module group (:model/:api/:domain/:data/:presenter/:ui) to an existing
  KMP project. Uses AGP 9+, build-logic convention plugins, a TOML version catalog
  (`gradle/libs.versions.toml`), Compose Multiplatform, and Koin 4 (annotated or manual DI).
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-07-05'
  keywords:
    - Kotlin Multiplatform
    - KMP
    - KMM
    - multi-module
    - feature module
    - AGP 9
    - build-logic
    - convention plugins
    - Koin 4
    - Compose Multiplatform
    - CMP
    - version catalog
---

## When to Use This Skill

Use when you need to:
- Create a new Kotlin Multiplatform project from scratch, starting from Kotlin/kmp-wizard
  (usually the `all-targets` branch when you want Android + iOS + Web + Desktop + Server)
- Add a new feature module group (`:model/:api/:domain/:data/:presenter/:ui`) to an existing KMP project
- Set up AGP 9+ build-logic convention plugins and a version catalog
- Set up AGP 9+ build-logic convention plugins backed by `gradle/libs.versions.toml`
- Wire Koin 4 DI (annotated or manual) across KMP modules

**This is the foundational skill** — most other KMP skills (`network-layer`, `sqldelight-setup`,
`navigation`, `design-system`, etc.) require the project structure this skill creates.

**Trigger keywords:** create KMP project, scaffold feature module, new module, set up KMP,
add feature, multi-module, build-logic, convention plugin, AGP 9, Koin 4, KMP setup,
Kotlin/kmp-wizard, generate from template, baseline project,
add a screen, new screen, new feature, new feature module, add feature layer,
scaffold module, create module, add KMP screen, set up convention plugin.

**Branch recommendation:** default to the `all-targets` branch for full-stack KMP apps.
Use `all-frontends-shared` only when you want Android + iOS + Web + Desktop without a
server module.

**Build-logic rule:** always route module configuration through convention plugins in
`build-logic/` and keep versions in `gradle/libs.versions.toml`; do not scatter plugin
and dependency versions across module build files.

**Freshness rule:** AGP, Kotlin, CMP, and Koin version targets change quickly — recheck the
version table in `PLAN.md` and the kmp-wizard repo before scaffolding a new project.

---

## Recommendation First

Default to **kmp-wizard `all-targets` branch + build-logic convention plugins + `gradle/libs.versions.toml`**.

Why:
- `all-targets` gives Android + iOS + Web + Desktop + Server in one baseline — easier to trim
  than to add targets later
- convention plugins enforce consistent AGP/Kotlin configuration across every module
- a single version catalog eliminates version drift between modules

Use a narrower branch (`all-frontends-shared`) only when the product explicitly excludes server.
Never scaffold by hand — always start from kmp-wizard to avoid missing targets or misconfigured plugins.

---

## Overview

This skill produces a KMP multi-feature module architecture with the following decisions
baked in:

- **AGP 9 minimum** using the new `com.android.kotlin.multiplatform.library` plugin
  (replaces the old `kotlin("multiplatform")` + `com.android.library` pair for library modules)
- **build-logic** as a Gradle included build providing precompiled convention plugins
- **Version catalog** (`gradle/libs.versions.toml`) with proper group prefixes and bundles
- **Feature split**: thin (`:ui`), medium (`:presenter`+`:ui`), or full (all 6) — chosen in Step 0
- **Core modules**: `:core:common`, `:core:network`, `:core:database`, `:core:ui`
- **Compose Multiplatform (CMP)** as the default shared UI layer (CMP-first)
- **Koin 4** DI — annotated (default, via Koin Compiler Plugin) or manual

### Module dependency graph (per feature)

```
:feature:<name>:model      pure KMP — data classes, sealed types, enums (no deps)
        ↑
:feature:<name>:api        pure KMP — interfaces, nav contracts (depends on :model)
        ↑
:feature:<name>:domain     pure KMP — use cases, business logic (depends on :api)
        ↑
:feature:<name>:data       KMP + platform impls — Ktor, SQLDelight (depends on :api, NOT :domain)
:feature:<name>:presenter  pure KMP — ViewModels, MVI contracts (depends on :domain, NO Compose)
        ↑
:feature:<name>:ui         CMP — Compose screens + previews (depends on :presenter ONLY)
```

`:data` and `:presenter` are siblings — neither depends on the other.
`:presenter` has NO Compose dependency, so ViewModels are testable on plain JVM.

---

## Mode Detection

Before doing anything, inspect the working directory:

- **New Project mode**: no `settings.gradle.kts` or no `build-logic/` directory found.
  Scaffold the full project by copying the Kotlin/kmp-wizard AGP 9 `all-targets`
  baseline first, then layer the multi-feature module architecture on top.
- **Add Feature mode**: existing KMP project detected (has `settings.gradle.kts` and
  `build-logic/`). Only scaffold the new feature module group.

---

## Step 0: Decide Layer Depth Before Scaffolding

**Ask this before generating any modules.** The 6-layer structure is the maximum — not
the default. Scaffold only the layers the feature actually needs.

Ask the user (or infer from context):

| Question | Yes → add this layer |
|---|---|
| Does the feature load or write data from a server or database? | `:data` |
| Does it apply business rules that must be tested without a ViewModel? | `:domain` |
| Does it have user interactions and/or navigation effects? | `:presenter` (MVI) |
| Does it display a screen in Compose? | `:ui` |
| Does it define types shared across the above layers? | `:model` + `:api` |

**Three tiers:**

| Tier | Modules | When to use |
|---|---|---|
| **Thin** | `:ui` only | Static display screen, no async, no ViewModel needed |
| **Medium** | `:presenter` + `:ui` | Async load + navigation, no business logic to isolate |
| **Full** | `:model` + `:api` + `:domain` + `:data` + `:presenter` + `:ui` | CRUD, offline-first, business rules, or cross-feature shared types |

Default to **Medium** for most product features. Upgrade to Full when `:data` complexity
or cross-feature type sharing justifies it. Use Thin only for standalone utility screens.

Do not scaffold unused layers "in case they're needed later" — empty modules add Gradle
configuration overhead and signal to the team that something should be there.

---

## Step 1: Gather User Input

**Always ask before creating any files.** Collect these values from the user:

| Input | Description | Example |
|---|---|---|
| `PROJECT_NAME` | Root project name (PascalCase) | `MyAwesomeApp` |
| `GROUP_ID` | Base package / Maven group ID | `com.example.myapp` |
| `FEATURE_NAME` | First feature to scaffold (snake_case) | `auth` |
| `TIER` | `thin` / `medium` / `full` (from Step 0) | `full` |
| `DI_APPROACH` | `annotated` (default) or `manual` | `annotated` |

In **Add Feature mode**, only `GROUP_ID`, `FEATURE_NAME`, `TIER`, and `DI_APPROACH` are needed.

---

## Step 2: Version Reference

Use these exact versions. Do not substitute without explicit user confirmation.

```toml
agp                   = "9.2.0"
kotlin                = "2.4.0"
ksp                   = "2.4.0-2.0.0"
koin                  = "4.2.2"
koin-annotations      = "2.3.1"
ktor                  = "3.5.0"
sqldelight            = "2.3.2"
compose-multiplatform = "1.11.1"
buildkonfig           = "0.22.0"
android-compileSdk    = "36"
android-minSdk        = "24"
android-targetSdk     = "36"
androidx-lifecycle    = "2.11.0"
androidx-activity     = "1.13.0"
coroutines            = "1.11.0"
serialization         = "1.11.0"
datetime              = "0.8.0"
```

> **Note on Koin DI**: Koin 4.1+ ships a native Kotlin Compiler Plugin
> (`org.jetbrains.kotlin.plugin.koin`) that replaces the KSP-based annotation processor
> for KMP projects — no per-platform KSP configuration needed. Use this for `annotated`
> mode. For `manual` mode, skip the plugin entirely and write explicit `module {}` blocks.

> **Note on BuildKonfig**: `com.codingfeline.buildkonfig` is the KMP equivalent of
> Android's `BuildConfig`. It generates a `BuildKonfig` object accessible from
> `commonMain`, `androidMain`, and `iosMain`. Configure it in `:androidApp`'s
> `build.gradle.kts` using a `buildkonfig {}` block.

---

## App Versioning

**Three tools, one responsibility each:**

| Tool | Role |
|---|---|
| `gradle.properties` | Single source of truth — declare `VERSION_NAME` and `VERSION_CODE` here. CI bumps this one file. |
| `libs.versions.toml` | Dependency/plugin versions only — never put app version here. |
| `BuildKonfig` | Expose `APP_VERSION` to `commonMain` so shared code can read it (User-Agent, about screen, analytics). |

**`gradle.properties`** — add alongside the Gradle performance flags:
```properties
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
org.gradle.configuration-cache=true
org.gradle.parallel=true
kotlin.code.style=official

# App version — bump here; read everywhere else
VERSION_NAME=1.0.0
VERSION_CODE=1
```

**`androidApp/build.gradle.kts`** — read from properties:
```kotlin
android {
    defaultConfig {
        versionCode = (project.property("VERSION_CODE") as String).toInt()
        versionName = project.property("VERSION_NAME") as String
    }
}
```

**`buildkonfig {}` block** — expose version to `commonMain`:
```kotlin
buildkonfig {
    packageName = "GROUP_ID"

    defaultConfigs {
        buildConfigField(STRING, "APP_NAME", "PROJECT_NAME")
        buildConfigField(STRING, "APP_VERSION", project.property("VERSION_NAME") as String)
        buildConfigField(STRING, "BASE_URL", "https://api.example.com")
        buildConfigField(BOOLEAN, "DEBUG", "false")
    }

    targetConfigs {
        create("debug") {
            buildConfigField(BOOLEAN, "DEBUG", "true")
            buildConfigField(STRING, "BASE_URL", "https://api-staging.example.com")
        }
    }
}
```

**`AppConfig` in `commonMain`** — the public facade:
```kotlin
object AppConfig {
    val versionName: String  get() = BuildKonfig.APP_VERSION
    val baseUrl: String      get() = BuildKonfig.BASE_URL
    val isDebug: Boolean     get() = BuildKonfig.DEBUG
}
```

**CI version bump** (no Gradle plugin needed):
```bash
# In your release script or CI step:
sed -i "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" gradle.properties
sed -i "s/^VERSION_CODE=.*/VERSION_CODE=$NEW_CODE/" gradle.properties
git commit -am "chore: bump version to $NEW_VERSION"
```

> **iOS note**: `VERSION_NAME` and `VERSION_CODE` flow into `CFBundleShortVersionString` and
> `CFBundleVersion` via your Xcode project or a `xcconfig` file — see
> `kotlin-multiplatform-xcframework-spm` for the full iOS release pipeline.

> **Library publishing note**: for KMP libraries (not apps), declare `version` in
> `gradle.properties` and read it with `version = project.property("VERSION_NAME")` in the
> module's `build.gradle.kts`. Do not use `BuildKonfig` in libraries — it is an app-only tool.

---

## Step 3: New Project — Clone kmp-wizard (MANDATORY)

> **Never create build infrastructure by hand.** Always start from the official
> `Kotlin/kmp-wizard` repository. Hand-writing `build-logic`, convention plugins, or
> `settings.gradle.kts` from scratch leads to misconfigured Gradle included builds,
> broken precompiled script plugin accessor generation, and missing platform targets.
> The wizard gives you a known-good baseline; your job is to configure and extend it.

### 3a. Clone the baseline

```bash
# Default: all platforms (Android + iOS + Desktop + Web + Server)
git clone --depth 1 --branch all-targets \
  https://github.com/Kotlin/kmp-wizard <PROJECT_NAME>

# Frontend-only (no server module):
git clone --depth 1 --branch all-frontends-shared \
  https://github.com/Kotlin/kmp-wizard <PROJECT_NAME>

cd <PROJECT_NAME>
rm -rf .git          # detach from kmp-wizard history
git init             # start fresh project history
```

Choose `all-targets` by default. Use `all-frontends-shared` only when the project
explicitly excludes a server module.

### 3b. Configure the clone

After cloning, make these targeted edits — do not rewrite the files:

**`settings.gradle.kts`** — update the root project name:
```kotlin
rootProject.name = "PROJECT_NAME"
```

**`gradle/libs.versions.toml`** — update to the target versions from Step 2:
```toml
agp                   = "9.2.0"
kotlin                = "2.4.0"
compose-multiplatform = "1.11.1"
# … update all version entries to match Step 2 table
```

**`build-logic/convention/src/main/kotlin/`** — rename every convention plugin file
by substituting the wizard's placeholder group ID with `GROUP_ID`:
```bash
# Example: if kmp-wizard uses "org.example" as placeholder
for f in build-logic/convention/src/main/kotlin/*.kt; do
  mv "$f" "${f/org.example/GROUP_ID}"
done
# Then update the group ID string inside each file
find build-logic/convention/src/main/kotlin -name "*.kt" \
  -exec sed -i '' 's/org\.example/GROUP_ID/g' {} +
```

**`androidApp/build.gradle.kts`** and any `applicationId` occurrences — replace
the wizard placeholder with `GROUP_ID`.

### 3c. Verify the base builds

Run this before adding any modules:

```bash
./gradlew help
```

`BUILD SUCCESSFUL` means the base is sound. Fix any version resolution errors
before proceeding. Do not add feature modules to a broken base.

---

## Step 4: Extend build-logic with KMM Convention Plugins

kmp-wizard ships with its own convention plugins. You need to **add** the 6-layer
KMM-specific plugins on top — do not replace the wizard's existing plugins.

### 4a. Add plugin dependencies to `build-logic/convention/build.gradle.kts`

Add any missing plugin dependencies the wizard doesn't include (e.g. SQLDelight,
Roborazzi). Do not remove what the wizard already declares:

```kotlin
dependencies {
    // Keep whatever kmp-wizard already has, then add:
    compileOnly(libs.sqldelight.gradlePlugin)
    compileOnly("io.github.takahirom.roborazzi:io.github.takahirom.roborazzi.gradle.plugin:${libs.versions.roborazzi.get()}")
}
```

Add the new plugin registrations to the existing `gradlePlugin { plugins { … } }` block:

```kotlin
gradlePlugin {
    plugins {
        // Keep whatever kmp-wizard registers, then add:
        register("featureModel")    { id = "GROUP_ID.feature.model";    implementationClass = "FeatureModelConventionPlugin" }
        register("featureApi")      { id = "GROUP_ID.feature.api";      implementationClass = "FeatureApiConventionPlugin" }
        register("featureDomain")   { id = "GROUP_ID.feature.domain";   implementationClass = "FeatureDomainConventionPlugin" }
        register("featureData")     { id = "GROUP_ID.feature.data";     implementationClass = "FeatureDataConventionPlugin" }
        register("featurePresenter"){ id = "GROUP_ID.feature.presenter";implementationClass = "FeaturePresenterConventionPlugin" }
        register("featureUi")       { id = "GROUP_ID.feature.ui";       implementationClass = "FeatureUiConventionPlugin" }
        register("core")            { id = "GROUP_ID.core";             implementationClass = "CoreConventionPlugin" }
    }
}
```

> **Class-based plugins only.** Do NOT use precompiled `.gradle.kts` script plugins
> for convention plugins in included builds — Gradle 9's `generatePrecompiledScriptPluginAccessors`
> does not generate version catalog type-safe accessors for included builds, causing every
> `libs.*` reference to fail with "Unresolved reference". Always write convention plugins
> as classes implementing `Plugin<Project>` and access the catalog via
> `extensions.getByType<VersionCatalogsExtension>().named("libs")`.

### 4b. Add missing catalog entries to `gradle/libs.versions.toml`

Only add what the wizard doesn't already have (check before adding):

```toml
[versions]
sqldelight            = "2.3.2"
roborazzi             = "1.64.0"
turbine               = "1.2.1"
datetime              = "0.8.0"
koin                  = "4.2.2"

[libraries]
sqldelight-runtime         = { module = "app.cash.sqldelight:runtime",               version.ref = "sqldelight" }
sqldelight-coroutines      = { module = "app.cash.sqldelight:coroutines-extensions",  version.ref = "sqldelight" }
sqldelight-android-driver  = { module = "app.cash.sqldelight:android-driver",         version.ref = "sqldelight" }
sqldelight-sqlite-driver   = { module = "app.cash.sqldelight:sqlite-driver",          version.ref = "sqldelight" }
sqldelight-gradlePlugin    = { module = "app.cash.sqldelight:gradle-plugin",          version.ref = "sqldelight" }
roborazzi                  = { module = "io.github.takahirom.roborazzi:roborazzi",            version.ref = "roborazzi" }
roborazzi-compose          = { module = "io.github.takahirom.roborazzi:roborazzi-compose",    version.ref = "roborazzi" }
roborazzi-junit-rule       = { module = "io.github.takahirom.roborazzi:roborazzi-junit-rule", version.ref = "roborazzi" }
turbine                    = { module = "app.cash.turbine:turbine",                   version.ref = "turbine" }
kotlinx-datetime           = { module = "org.jetbrains.kotlinx:kotlinx-datetime",    version.ref = "datetime" }
koin-core                  = { module = "io.insert-koin:koin-core",                  version.ref = "koin" }
koin-core-viewmodel        = { module = "io.insert-koin:koin-core-viewmodel",        version.ref = "koin" }
koin-compose               = { module = "io.insert-koin:koin-compose",               version.ref = "koin" }
koin-compose-viewmodel     = { module = "io.insert-koin:koin-compose-viewmodel",     version.ref = "koin" }
koin-android               = { module = "io.insert-koin:koin-android",               version.ref = "koin" }
koin-androidx-compose      = { module = "io.insert-koin:koin-androidx-compose",      version.ref = "koin" }

[plugins]
kotlin-multiplatform       = { id = "org.jetbrains.kotlin.multiplatform",              version.ref = "kotlin" }
kotlin-android             = { id = "org.jetbrains.kotlin.android",                    version.ref = "kotlin" }
kotlin-compose             = { id = "org.jetbrains.kotlin.plugin.compose",             version.ref = "kotlin" }
kotlin-koin                = { id = "org.jetbrains.kotlin.plugin.koin",                version.ref = "kotlin" }
compose-multiplatform      = { id = "org.jetbrains.compose",                           version.ref = "compose-multiplatform" }
android-application        = { id = "com.android.application",                         version.ref = "agp" }
android-library-kmp        = { id = "com.android.kotlin.multiplatform.library",        version.ref = "agp" }
sqldelight                 = { id = "app.cash.sqldelight",                             version.ref = "sqldelight" }
```

---

## Step 5: Convention Plugin Templates

> **IMPORTANT — file naming**: With Gradle precompiled script plugins, the file name IS
> the plugin ID. When scaffolding, rename every template file by replacing `GROUP_ID` with
> your actual reversed-domain group ID (dots are valid in filenames here).
>
> Example for `GROUP_ID = com.example.myapp`:
> - `GROUP_ID.feature.api.gradle.kts` → `com.example.myapp.feature.api.gradle.kts`
> - `GROUP_ID.android.app.gradle.kts` → `com.example.myapp.android.app.gradle.kts`
> - etc.
>
> The template folder `templates/build-logic/convention/src/main/kotlin/` contains
> all eight files pre-named with the `GROUP_ID` placeholder for this purpose.

### `GROUP_ID.feature.model.gradle.kts`
Pure KMP — no framework deps. Data classes, sealed types, enums. Zero dependencies.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            // intentionally empty — :model has no external deps
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

### `GROUP_ID.feature.api.gradle.kts`
Pure KMP — no Compose, no Koin. Exposes interfaces, navigation contracts. Depends on `:model`.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

### `GROUP_ID.feature.domain.gradle.kts`
Pure KMP — use cases and business logic. No Compose, no data layer deps.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

### `GROUP_ID.feature.data.gradle.kts`
KMP + platform implementations — Ktor for networking, SQLDelight for persistence.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.sqldelight)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.bundles.ktor.common)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
```

### `GROUP_ID.feature.presenter.gradle.kts`
Pure KMP — ViewModels and MVI contracts. No Compose dependency. Testable on plain JVM.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)   // no Compose flavour
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

### `GROUP_ID.feature.ui.gradle.kts`
CMP — Compose Multiplatform screens only. Depends on `:presenter`, not on `:domain` or `:data`.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            implementation(compose.uiTooling)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

### `GROUP_ID.core.gradle.kts`
Base for all `:core:*` modules. Apply additional plugins per-module as needed.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

### `GROUP_ID.android.app.gradle.kts`
Android application entry point.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.koin)
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        versionCode = (project.property("VERSION_CODE") as String).toInt()
        versionName = project.property("VERSION_NAME") as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
}
```

---

## Step 6: Feature Module `build.gradle.kts` Templates

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

## Step 7: Koin DI Patterns

### Annotated mode (default — Koin Compiler Plugin)

Apply `id("org.jetbrains.kotlin.plugin.koin")` in the module's convention plugin or
directly in the build.gradle.kts. Then use annotations:

```kotlin
// In :feature:auth:domain
@Single
class GetUserUseCase(private val repo: UserRepository) {
    operator fun invoke(id: String): Flow<User> = repo.getUser(id)
}

// In :feature:auth:presenter (no Compose dep — testable on JVM)
@KoinViewModel
class AuthViewModel(private val getUser: GetUserUseCase) : ViewModel() { ... }
```

Auto-generated modules are collected in `AppModule`. Declare in `:androidApp`:

```kotlin
startKoin {
    androidContext(this@App)
    modules(AppModule.module)  // generated by Koin Compiler Plugin
}
```

### Manual mode

Write explicit `module {}` blocks per feature. Convention: one `<FeatureName>Module.kt`
in each `:domain` and `:data` module.

```kotlin
// :feature:auth:domain/src/commonMain/kotlin/.../AuthDomainModule.kt
val authDomainModule = module {
    factory { GetUserUseCase(get()) }
}

// :feature:auth:presenter/src/commonMain/kotlin/.../AuthPresenterModule.kt
val authPresenterModule = module {
    viewModel { AuthViewModel(get()) }
}
```

Declare all modules in `:androidApp`:

```kotlin
startKoin {
    androidContext(this@App)
    modules(authDomainModule, authPresenterModule, /* ... */)
}
```

---

## Step 8: Add Feature Mode

When adding a feature to an existing project:

1. Create the six module directories:
   ```
   feature/<FEATURE_NAME>/model/
   feature/<FEATURE_NAME>/api/
   feature/<FEATURE_NAME>/domain/
   feature/<FEATURE_NAME>/data/
   feature/<FEATURE_NAME>/presenter/
   feature/<FEATURE_NAME>/ui/
   ```
2. Write `build.gradle.kts` in each (see Step 6 templates above).
3. Add to `settings.gradle.kts`:
   ```kotlin
   include(":feature:FEATURE_NAME:model")
   include(":feature:FEATURE_NAME:api")
   include(":feature:FEATURE_NAME:domain")
   include(":feature:FEATURE_NAME:data")
   include(":feature:FEATURE_NAME:presenter")
   include(":feature:FEATURE_NAME:ui")
   ```
4. Wire into `:androidApp` dependencies:
   ```kotlin
   implementation(projects.feature.FEATURE_NAME.ui)
   ```
5. Add a preview stub beside each `*Content.kt` in `:feature:FEATURE_NAME:ui` so preview
   coverage is part of the scaffold, not an optional follow-up.

---

## Step 9: Source File Stubs

After creating build files, generate stub source files so each module compiles:

### `:feature:FEATURE_NAME:model`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/model/
    FEATURE_NAMEModel.kt             ← data class(es), sealed types, enums
```

### `:feature:FEATURE_NAME:api`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/api/
    FEATURE_NAMERepository.kt        ← interface (uses types from :model)
    FEATURE_NAMENavigation.kt        ← nav route objects/sealed class
```

### `:feature:FEATURE_NAME:domain`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/domain/
    Get<FEATURE_NAME>UseCase.kt
    di/FEATURE_NAME_DomainModule.kt  ← only in manual mode
```

### `:feature:FEATURE_NAME:data`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/data/
    FEATURE_NAMERepositoryImpl.kt
    remote/FEATURE_NAMERemoteDataSource.kt
    local/FEATURE_NAMELocalDataSource.kt
    di/FEATURE_NAME_DataModule.kt    ← only in manual mode
```

### `:feature:FEATURE_NAME:presenter`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/presenter/
    FEATURE_NAMEViewModel.kt         ← ViewModel, no Compose import
    FEATURE_NAMEUiState.kt           ← MVI state sealed class
    FEATURE_NAMEUiIntent.kt          ← MVI intent sealed class
    di/FEATURE_NAME_PresenterModule.kt  ← only in manual mode
```

### `:feature:FEATURE_NAME:ui`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/ui/
    FEATURE_NAMEScreen.kt            ← wires ViewModel from :presenter via koinViewModel()
    FEATURE_NAMEContent.kt           ← stateless @Composable, accepts state parameter
    previews/
        FEATURE_NAMEContentPreview.kt ← required preview stub for the Content composable
```

---

## Step 10: Test Infrastructure

### Convention plugin: `GROUP_ID.feature.test.gradle.kts`

A lightweight plugin that equips any module's test source sets with shared test tooling.
Apply it to modules that need Turbine, coroutines-test, or shared fakes.

```kotlin
// In any module's build.gradle.kts test configuration
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)  // shared fakes + builders
        }
    }
}
```

### `:core:testing` module

Add to `settings.gradle.kts`:
```kotlin
include(":core:testing")
```

The module exposes (via `api()`):
- `kotlin.test` — assertions
- `kotlinx.coroutines.test` — `runTest`, `TestCoroutineScheduler`
- `Turbine 1.2.1` — Flow testing

## Bundled Script

- `scripts/validate_module_graph.py` — checks a target project for the expected
  `:model/:api/:domain/:data/:presenter/:ui` feature module files, the `androidApp`
  feature UI link, and the required preview stub for each `*Content.kt` in `:feature:*:ui`.

### Turbine usage pattern

```kotlin
// commonTest — testing a ViewModel or use case that emits a Flow
@Test
fun `state emits Loading then Success`() = runTest {
    val viewModel = AuthViewModel(FakeGetUserUseCase())
    viewModel.uiState.test {
        assertEquals(AuthUiState.Loading, awaitItem())
        assertEquals(AuthUiState.Success(fakeUser), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Shared fakes pattern in `:core:testing`

```
src/commonMain/kotlin/GROUP_ID/core/testing/
    fakes/
        FakeTokenStorage.kt
        FakeNetworkClient.kt
    builders/
        UserBuilder.kt          ← test data builders with defaults
    rules/
        MainCoroutineRule.kt    ← TestCoroutineDispatcher setup
```

Example fake:
```kotlin
class FakeTokenStorage : TokenStorage {
    var accessToken: String? = "test-access-token"
    var refreshToken: String? = "test-refresh-token"
    override suspend fun getAccessToken() = accessToken
    override suspend fun getRefreshToken() = refreshToken
    override suspend fun saveTokens(access: String, refresh: String) {
        accessToken = access; refreshToken = refresh
    }
    override suspend fun clearTokens() { accessToken = null; refreshToken = null }
}
```

---

## Step 11: Verification

After scaffolding, verify in order:

1. `./gradlew help` — Gradle resolves the build without errors
2. `./gradlew :feature:FEATURE_NAME:api:compileKotlinMetadata` — KMP common compiles
3. `./gradlew :androidApp:assembleDebug --dry-run` — Android wiring is correct
4. Confirm all `include()` entries in `settings.gradle.kts` match actual directories
5. Confirm no module references another module that it should not (enforce the layer rules:
   `:ui` depends only on `:presenter`; `:presenter` has NO Compose dep; `:domain` must not depend on `:data`;
   `:data` must not depend on `:domain` or `:presenter`)
6. Confirm every `*Content.kt` in `:feature:FEATURE_NAME:ui` has a matching preview stub
   (`*ContentPreview.kt` or `previews/*ContentPreview.kt`)

---

## Guidelines

- Never create a `buildSrc/` directory — use `build-logic` instead
- Never use `id("kotlin-android")` — use `id("org.jetbrains.kotlin.android")` (AGP 9 requirement)
- Never add `android.builtInKotlin` or `android.newDsl` to `gradle.properties` — these are AGP 9 defaults
- Always use `androidLibrary {}` inside `kotlin {}` for library modules, not a standalone `android {}` block
- Always use TYPESAFE_PROJECT_ACCESSORS (`projects.feature.auth.api`) — never string-based `:feature:auth:api`
- Keep `:api` modules minimal — no DI framework dependencies, no platform deps
- Namespace format: `GROUP_ID.module.path` (e.g. `com.example.app.feature.auth.api`)

---

## Related Skills

- `docs/reference/compatibility-matrix.md` — compatibility table and conflict zones for all versions declared in this skill's version catalog
- `kotlin-multiplatform-dependency-injection` — wire Koin after the module structure is in place
- `kotlin-multiplatform-navigation` — add type-safe navigation after scaffold is complete
- `kotlin-multiplatform-mvi` — screen architecture layer built on top of this scaffold
- `kotlin-multiplatform-flavor-environment` — add dev/staging/prod environments after scaffolding
- `kotlin-multiplatform-ci-github-actions` — CI workflow consumes the module structure this skill creates

---

## Common Anti-Patterns

- scattering plugin versions across module `build.gradle.kts` files instead of `libs.versions.toml` — causes version drift
- skipping `build-logic` convention plugins for "simple" modules — they accumulate inconsistency over time
- adding `implementation` dependencies in `:api` modules — `:api` must stay dependency-free (only `:model`)
- adding Compose deps to `:presenter` — breaks JVM testability; Compose belongs only in `:ui`
- having `:ui` depend on `:domain` or `:data` directly — all state must flow through `:presenter`
- shipping a `:feature:*:ui` module with `*Content.kt` but no preview stub — preview coverage must be scaffolded, not added later
- putting domain types (data classes, sealed types) in `:api` instead of `:model` — `:api` should be interfaces only
- using string project references (`:feature:auth:api`) instead of typesafe accessors — breaks refactoring
- **scaffolding by hand instead of cloning kmp-wizard** — always use `git clone Kotlin/kmp-wizard` as the base; writing build-logic, convention plugins, or settings.gradle.kts from scratch causes broken Gradle included builds, missing platform targets, and cascading precompiled script plugin failures that are very hard to debug
- using precompiled `.gradle.kts` script plugins for convention plugins in included builds — Gradle 9 does not generate version catalog type-safe accessors for included builds; always use class-based `Plugin<Project>` instead
- pre-creating empty `src/androidMain/kotlin/`, `src/iosMain/kotlin/`, `src/jvmMain/kotlin/`, etc. directories "just in case" a module might need platform code later — Gradle compiles a target fine with zero files in its source set; an empty platform directory (or one containing only a package-declaration stub) is pure clutter and signals unclear architecture intent. Declare the compile targets in the convention plugin (`androidLibrary {}`, `iosArm64()`, ...) as usual — that's required for per-platform artifacts — but only create the physical source directory and write into it when there is real `expect`/`actual` code to place there

If a module is failing to compile on one target, check whether the convention plugin was applied and the source sets declared correctly.

---

## Output Style

When asked to scaffold a project or add a feature module, respond in this order:
1. clarify the target (new project vs new feature module in existing project)
2. version reference (confirm current AGP / Kotlin / CMP targets from PLAN.md)
3. directory structure
4. key file contents (build-logic convention plugin, module build file, settings)
5. wire-up step (Koin module registration, nav graph entry)

Ask for GROUP_ID and feature name before generating files. Map all paths to the actual values.

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-05 | Added anti-pattern against pre-creating empty platform source directories (`androidMain`, `iosMain`, `jvmMain`, ...) "just in case" — a real recurring smell reported from field experience. New audit detector `empty platform source set [LOW]` in `kotlin-multiplatform-audit` catches directories with zero `.kt` files or files containing only package/import/comments. Declaring the compile target is still required and correct; only the physical directory should be created on-demand, when there's real expect/actual code to write. |
| 2026-06-21 | **Improved** — App versioning pattern defined: `VERSION_NAME`/`VERSION_CODE` in `gradle.properties` as the single source of truth; `androidApp` convention plugin reads from properties; `BuildKonfig` exposes `APP_VERSION` to `commonMain`; CI bump pattern documented. |
| 2026-06-21 | **Breaking** — Step 3 rewritten: `git clone Kotlin/kmp-wizard` is now mandatory. Hand-scaffolding `build-logic`, convention plugins, or `settings.gradle.kts` from scratch is no longer supported. |
| 2026-06-21 | **Breaking** — Step 4 rewritten: convention plugins must be class-based `Plugin<Project>`. Precompiled `.gradle.kts` script plugins in included builds do not generate version catalog accessors in Gradle 9. |
| 2026-06-18 | 6-layer module structure enforced; `jvm()` target added to all convention plugin templates; Step 9 source stubs expanded. |
