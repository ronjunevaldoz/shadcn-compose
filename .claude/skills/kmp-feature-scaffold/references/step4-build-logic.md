# Step 4: Extend build-logic with KMP Convention Plugins

Part of `kmp-feature-scaffold`. Load this file when working on: step 4: extend build-logic with kmp convention plugins.

---

kmp-wizard ships with its own convention plugins. You need to **add** the 6-layer
KMP-specific plugins on top — do not replace the wizard's existing plugins.

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

