---
name: kotlin-multiplatform-flavor-environment
description: >
  Sets up multi-environment configuration (dev/staging/prod) in a Kotlin
  Multiplatform project using BuildKonfig. Covers: environment-specific
  BuildKonfig values, Android product flavors wired to BuildKonfig, a shared
  AppConfig object in commonMain, secret management, and switching environments
  at build time. Assumes the project was scaffolded with
  kotlin-multiplatform-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - BuildKonfig
    - multi-environment
    - product flavors
    - dev staging prod
    - KMP config
    - Kotlin Multiplatform
    - secrets
    - AppConfig
---

## When to Use This Skill

Use when you need to:
- Add dev / staging / prod environment configs to a KMP project
- Wire Android product flavors to BuildKonfig values (`BASE_URL`, `API_KEY`, `DEBUG`)
- Inject secrets at build time from `local.properties` or CI env vars
- Access environment config from `commonMain` without platform-specific code

**Requires:** `kotlin-multiplatform-feature-scaffold` project structure.

**Trigger keywords:** dev/staging/prod, environment config, BuildKonfig, product flavors,
API key secrets, build variants, AppConfig, env switching, multi-environment,
staging URL, API endpoint config, debug config, production config, environment variable,
build type config, switch API URL, hide API keys, config per environment.

**Freshness rule:** BuildKonfig plugin versions and AGP product flavor APIs change — recheck
the BuildKonfig repo and version catalog before upgrading.

---

## Recommendation First

Default to **BuildKonfig + `AppConfig` sealed class + `local.properties` for secrets**.

Why:
- BuildKonfig generates a `BuildKonfig` object in `commonMain` — accessible from shared code
  without expect/actual boilerplate
- wrapping it in a typed `AppConfig` sealed class keeps route handlers and services decoupled
  from raw string constants
- secrets stay in `local.properties` (gitignored) and CI injects them via environment variables

Use expect/actual environment injection only when BuildKonfig does not support a specific target.

---

## Overview

BuildKonfig generates a `BuildKonfig` object in `commonMain` — the KMP equivalent
of Android `BuildConfig`. This skill layers environments on top:

```
:androidApp
  Build variants:
    devDebug, devRelease
    stagingDebug, stagingRelease
    prodDebug, prodRelease
```

All variants produce a `BuildKonfig` object in commonMain with constants like
`BASE_URL`, `DEBUG`, `ENVIRONMENT`, which all KMP modules can consume.

---

## Prerequisites

- Project scaffolded with `kotlin-multiplatform-feature-scaffold`
- `:androidApp` applies `GROUP_ID.android.app` convention plugin (already includes BuildKonfig)
- `buildkonfig = "0.22.0"` in `libs.versions.toml`

---

## Step 1: Update `:androidApp/build.gradle.kts`

```kotlin
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    id("GROUP_ID.android.app")
}

android {
    // Define flavors
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
        create("prod") {
            dimension = "environment"
        }
    }
}

buildkonfig {
    packageName = "GROUP_ID"

    // Default config (required — used as base, overridden by flavor)
    // APP_VERSION reads from gradle.properties — the single source of truth for the version
    defaultConfigs {
        buildConfigField(STRING,  "APP_VERSION",  project.property("VERSION_NAME") as String)
        buildConfigField(STRING,  "BASE_URL",     "https://api.example.com")
        buildConfigField(STRING,  "ENVIRONMENT",  "prod")
        buildConfigField(BOOLEAN, "DEBUG",         "false")
        buildConfigField(BOOLEAN, "ENABLE_LOGGING","false")
    }

    // Dev flavor overrides
    defaultConfigs("dev") {
        buildConfigField(STRING,  "BASE_URL",      "https://dev-api.example.com")
        buildConfigField(STRING,  "ENVIRONMENT",   "dev")
        buildConfigField(BOOLEAN, "DEBUG",          "true")
        buildConfigField(BOOLEAN, "ENABLE_LOGGING", "true")
    }

    // Staging flavor overrides
    defaultConfigs("staging") {
        buildConfigField(STRING,  "BASE_URL",      "https://staging-api.example.com")
        buildConfigField(STRING,  "ENVIRONMENT",   "staging")
        buildConfigField(BOOLEAN, "DEBUG",          "false")
        buildConfigField(BOOLEAN, "ENABLE_LOGGING", "true")
    }

    // prod: uses defaultConfigs (no override needed)

    // Per-target overrides (optional — for platform-specific values)
    targetConfigs {
        // android flavor-specific values can also be set here
    }
}
```

---

## Step 2: Using BuildKonfig in commonMain

BuildKonfig generates:

```kotlin
// auto-generated in commonMain by the BuildKonfig plugin:
package GROUP_ID

internal object BuildKonfig {
    val APP_VERSION: String = /* value from gradle.properties VERSION_NAME */
    val BASE_URL: String = /* value from active flavor/build type */
    val ENVIRONMENT: String = /* "dev" | "staging" | "prod" */
    val DEBUG: Boolean = /* true | false */
    val ENABLE_LOGGING: Boolean = /* true | false */
}
```

Create a public wrapper `src/commonMain/kotlin/GROUP_ID/core/config/AppConfig.kt`:

```kotlin
package GROUP_ID.core.config

/**
 * Public facade over [BuildKonfig].
 *
 * Feature modules reference AppConfig, not BuildKonfig directly,
 * so the generated class stays internal.
 */
object AppConfig {
    val versionName: String    get() = BuildKonfig.APP_VERSION
    val baseUrl: String        get() = BuildKonfig.BASE_URL
    val environment: String    get() = BuildKonfig.ENVIRONMENT
    val isDebug: Boolean       get() = BuildKonfig.DEBUG
    val enableLogging: Boolean get() = BuildKonfig.ENABLE_LOGGING

    val isDev: Boolean     get() = environment == "dev"
    val isStaging: Boolean get() = environment == "staging"
    val isProd: Boolean    get() = environment == "prod"
}
```

Usage in any commonMain module:

```kotlin
// In network layer
createHttpClient(
    baseUrl = AppConfig.baseUrl,
    enableLogging = AppConfig.enableLogging,
)

// In a ViewModel
if (AppConfig.isDev) {
    showDeveloperMenu()
}
```

---

## Step 3: Secrets management

**Never commit secrets to source control.** Inject at build time via `local.properties` or CI environment variables.

Add to `local.properties` (git-ignored):

```properties
dev.api_key=abc123
staging.api_key=def456
prod.api_key=ghi789
```

Read in `build.gradle.kts`:

```kotlin
import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

buildkonfig {
    packageName = "GROUP_ID"

    defaultConfigs {
        buildConfigField(STRING, "BASE_URL", "https://api.example.com")
        buildConfigField(STRING, "API_KEY",  "")  // empty default
    }

    defaultConfigs("dev") {
        buildConfigField(STRING, "BASE_URL", "https://dev-api.example.com")
        buildConfigField(STRING, "API_KEY",
            localProps.getProperty("dev.api_key", "")
        )
    }

    defaultConfigs("staging") {
        buildConfigField(STRING, "API_KEY",
            localProps.getProperty("staging.api_key",
                System.getenv("STAGING_API_KEY") ?: "")
        )
    }

    defaultConfigs("prod") {
        buildConfigField(STRING, "API_KEY",
            localProps.getProperty("prod.api_key",
                System.getenv("PROD_API_KEY") ?: "")
        )
    }
}
```

In CI (GitHub Actions), inject via environment variables:

```yaml
- name: Build release
  run: ./gradlew assembleProdRelease
  env:
    PROD_API_KEY: ${{ secrets.PROD_API_KEY }}
```

---

## Step 4: Desktop and Web environments

Desktop and Web don't have Android flavors. Use Gradle project properties or environment variables instead:

In the Desktop or Web app's `build.gradle.kts`:

```kotlin
val env = project.findProperty("env")?.toString() ?: "dev"

buildkonfig {
    packageName = "GROUP_ID"

    defaultConfigs {
        buildConfigField(STRING,  "BASE_URL",    "https://api.example.com")
        buildConfigField(STRING,  "ENVIRONMENT", "prod")
        buildConfigField(BOOLEAN, "DEBUG",        "false")
    }

    defaultConfigs("dev") {
        buildConfigField(STRING,  "BASE_URL",    "https://dev-api.example.com")
        buildConfigField(STRING,  "ENVIRONMENT", "dev")
        buildConfigField(BOOLEAN, "DEBUG",        "true")
    }
}
```

Run with: `./gradlew :desktopApp:run -Penv=dev`

---

## Step 5: iOS environments

iOS doesn't use Gradle flavors. Use a pre-build script in Xcode or a build phase to select a Gradle property:

In Xcode Build Phases → Run Script:

```bash
cd "$SRCROOT/.."
ENVIRONMENT="${BUILD_CONFIGURATION}"  # Debug/Release/Staging

if [ "$ENVIRONMENT" = "Staging" ]; then
    ./gradlew :shared:compileKotlinIosArm64 -Penv=staging
elif [ "$ENVIRONMENT" = "Debug" ]; then
    ./gradlew :shared:compileKotlinIosArm64 -Penv=dev
else
    ./gradlew :shared:compileKotlinIosArm64 -Penv=prod
fi
```

---

## Step 6: Build variant quick reference

| Variant | Command | BASE_URL | DEBUG | LOGGING |
|---|---|---|---|---|
| Dev Debug | `./gradlew assembleDevDebug` | dev API | true | true |
| Dev Release | `./gradlew assembleDevRelease` | dev API | false | true |
| Staging Debug | `./gradlew assembleStagingDebug` | staging API | false | true |
| Staging Release | `./gradlew assembleStagingRelease` | staging API | false | true |
| Prod Release | `./gradlew assembleProdRelease` | prod API | false | false |

---

## Guidelines

- Keep `BuildKonfig` internal — expose only via `AppConfig` (public wrapper)
- Never hardcode `BASE_URL` or API keys as string literals in source
- Always provide a `defaultConfigs` block — BuildKonfig requires it even when all values are overridden
- Add `local.properties` to `.gitignore` — it should never be committed
- For CI, prefer environment variables over Gradle properties for secrets (env vars are masked in logs)
- Use `AppConfig.isDebug` guards instead of `BuildConfig.DEBUG` — stays portable across platforms

---

## Verification

1. `./gradlew assembleDevDebug` — dev variant builds, `BuildKonfig.BASE_URL` = dev URL
2. `./gradlew assembleProdRelease` — prod variant builds, `BuildKonfig.DEBUG` = false
3. `./gradlew :androidApp:generateDevDebugBuildKonfig` — generated file contains expected values
4. Check `build/generated/buildkonfig/commonMain/` for the generated `BuildKonfig.kt`

---

## Testing

```kotlin
// BuildKonfig generates AppConfig at compile time — wrap it behind an interface so tests
// can inject a fake without depending on a specific build variant
interface AppEnvironment {
    val baseUrl: String
    val isDebug: Boolean
    val environment: String
}

class FakeAppEnvironment(
    override val baseUrl: String = "https://dev.example.com",
    override val isDebug: Boolean = true,
    override val environment: String = "dev",
) : AppEnvironment

@Test fun `dev url is used when environment is dev`() = runTest {
    val env = FakeAppEnvironment(baseUrl = "https://dev.example.com", environment = "dev")
    val repo = UserRepositoryImpl(buildClient(env.baseUrl), FakeUserDao())
    assertTrue(env.baseUrl.contains("dev"))
}

@Test fun `debug flag is false in prod environment`() {
    val env = FakeAppEnvironment(baseUrl = "https://api.example.com", isDebug = false, environment = "prod")
    assertFalse(env.isDebug)
    assertEquals("prod", env.environment)
}

@Test fun `staging and prod base urls are different`() {
    val staging = FakeAppEnvironment(baseUrl = "https://staging.example.com", environment = "staging")
    val prod    = FakeAppEnvironment(baseUrl = "https://api.example.com", environment = "prod")
    assertNotEquals(staging.baseUrl, prod.baseUrl)
}
```

---

## Common Anti-Patterns

- committing secrets to `gradle.properties` or `build.gradle.kts` — use `local.properties` and CI secrets
- accessing `BuildKonfig` constants directly in feature code — wrap in `AppConfig` sealed class first
- defining a fourth "test" flavor — use a build type override or test-specific `local.properties` instead
- using BuildKonfig for feature flags — it is for environment config, not dynamic features
- forgetting to add `local.properties` to `.gitignore` — leaks API keys if pushed

If CI builds fail on missing secrets, verify the GitHub Actions `env:` block injects all required keys.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — `build-logic` convention plugins where `BuildKonfig` is applied
- `kotlin-multiplatform-ci-github-actions` — CI secret injection via `env:` that feeds BuildKonfig values
- `kotlin-multiplatform-network-layer` — consumes `BuildKonfig.BASE_URL` and `BuildKonfig.DEBUG`
- `kotlin-multiplatform-logging` — consumes `BuildKonfig.DEBUG` to enable verbose log writers

---

## Output Style

When asked about environment config or BuildKonfig, respond in this order:
1. recommendation (BuildKonfig default, with local.properties for secrets)
2. code snippet (the `build.gradle.kts` block and the AppConfig wrapper)
3. why that approach is preferred
4. main alternative (manual expect/actual, environment-injected constants)

Keep the snippet focused on one flavor. Map to the user's actual base URL and environment names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | **Improved** — `APP_VERSION` added to `defaultConfigs` block (reads from `gradle.properties VERSION_NAME`); `AppConfig.versionName` exposed in the public facade. |
| 2026-06-06 | Initial release. |
