# App Versioning

Part of `kmp-feature-scaffold`. Load this file when working on: app versioning.

---

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
> `kmp-xcframework-spm` for the full iOS release pipeline.

> **Library publishing note**: for KMP libraries (not apps), declare `version` in
> `gradle.properties` and read it with `version = project.property("VERSION_NAME")` in the
> module's `build.gradle.kts`. Do not use `BuildKonfig` in libraries — it is an app-only tool.

---

