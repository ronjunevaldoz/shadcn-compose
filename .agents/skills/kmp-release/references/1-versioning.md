# 1. Versioning

Part of `kmp-release`. Load this file when working on: 1. versioning.

---

### `gradle.properties` as the single source of truth

```properties
# gradle.properties
# Library version — edit this line to bump, then publish
VERSION=0.1.0
```

Start a fresh library at `0.1.0`, not `1.0.0` — see
`kmp-library-publishing`'s pre-1.0 policy: `1.0.0` is a deliberate
stability promise cut after real consumer usage, not a scaffold default. An app's first
release starting at `1.0.0` is fine — apps carry no public-API stability promise the way
a published library does.

Each publishable module reads it:

```kotlin
// build.gradle.kts
val libraryVersion = (project.findProperty("VERSION") as? String)
    ?: error("VERSION not set in gradle.properties")

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.yourhandle", "your-artifact", libraryVersion)
}
```

Use `error()` instead of a fallback string — a missing `VERSION` should fail loudly, not publish silently with a stale value.

### Version bump workflow

| Release type | Action |
|---|---|
| Patch (bug fix) | Edit `VERSION=x.y.Z+1` in `gradle.properties`, commit, tag, publish |
| Minor (new feature) | Edit `VERSION=x.Y+1.0`, commit, tag, publish |
| Major (breaking) | Edit `VERSION=X+1.0.0`, commit, tag, publish |

Keep version bumps as a standalone commit: `chore(release): bump version to 1.2.0`. This gives git-cliff a clean anchor.

### Development versions

Use suffixes in `gradle.properties` to signal pre-release intent. Never publish a suffixed version to Maven Central — use GitHub Packages or `mavenLocal` instead.

| Stage | Suffix convention | Example | Publish target |
|---|---|---|---|
| Local dev build | `-LOCAL` | `1.2.0-LOCAL` | `mavenLocal` only — never pushed |
| First alpha | `-alpha01` | `1.2.0-alpha01` | GitHub Packages |
| Subsequent alphas | `-alpha02`, `-alpha03` | `1.2.0-alpha02` | GitHub Packages |
| Beta | `-beta01` | `1.2.0-beta01` | GitHub Packages |
| Release candidate | `-rc01` | `1.2.0-rc01` | GitHub Packages |
| Stable | _(no suffix)_ | `1.2.0` | Maven Central |
| SNAPSHOT | `-SNAPSHOT` | `1.2.0-SNAPSHOT` | `mavenLocal` only — Central rejects it |

Follow AndroidX/JetBrains zero-padded suffix convention (`alpha01` not `alpha1`) — it sorts correctly as a string.

#### Local dev builds

```bash
# Publish to ~/.m2 for local consumer testing
./gradlew publishToMavenLocal
```

Consumer adds `mavenLocal()` first in their `repositories {}` block and uses `VERSION=1.2.0-LOCAL`. Remove `mavenLocal()` before shipping.

#### GitHub Packages for pre-release distribution

Add a second publish target alongside the Central configuration:

```kotlin
// build.gradle.kts
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)  // stable only
    signAllPublications()
    // GitHub Packages — used for alpha/beta/rc
    repositories.maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/yourhandle/your-repo")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

Publish pre-release to GitHub Packages:

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository --no-configuration-cache
```

#### Promotion path

```
gradle.properties: VERSION=1.2.0-alpha01
    ↓ feedback / fixes
VERSION=1.2.0-beta01   (no new features)
    ↓ stability testing
VERSION=1.2.0-rc01     (no changes unless critical bug)
    ↓ confirmed stable
VERSION=1.2.0          → publish to Maven Central
```

Each stage is its own commit + tag (`v1.2.0-alpha01`, `v1.2.0-beta01`, etc.) so git-cliff produces a pre-release CHANGELOG section automatically.

### App targets: deriving platform-native version fields from one semver source

Everything above covers **library publishing**, where the single source of truth
(`gradle.properties` `VERSION`, or a project's `libs.versions.toml` app entry) maps to a
single Maven coordinate version. An **app** with an Android and/or iOS target has a second
problem: each platform store has its own **platform-native version field**, separate from
semver, that must be derived — never hand-maintained — from that one source.

**Rule: one semver source of truth. N platform-native version fields, all derived from it,
none hardcoded, none edited independently.**

| Platform | Semver field | Platform-native field | Store requirement |
|---|---|---|---|
| Android | `versionName` (any string) | `versionCode` (strictly increasing `Int`) | Play Console rejects an upload whose `versionCode` isn't higher than the last **accepted** upload |
| iOS | `CFBundleShortVersionString` (semver-like) | `CFBundleVersion` (build number, must increase per build submitted for the same short version) | App Store Connect rejects a build with a `CFBundleVersion` ≤ an existing build for that version |
| Desktop (packaging) | app semver | package version (MSI/DMG/deb use their own version schemes, some reject non-numeric suffixes) | Varies by packager; MSI in particular requires a strict `major.minor.build.revision` |

**Android — derive `versionCode` from semver, never hardcode it:**

```kotlin
// androidApp/build.gradle.kts (or androidApp/build.gradle.kts convention plugin)
val appVersion = libs.versions.app.get()   // single source of truth, e.g. "1.19.1"
val (major, minor, patch) = appVersion.split(".").map { it.toIntOrNull() ?: 0 }
    .let { Triple(it.getOrElse(0) { 0 }, it.getOrElse(1) { 0 }, it.getOrElse(2) { 0 }) }

android {
    defaultConfig {
        versionName = appVersion
        versionCode = major * 1_000_000 + minor * 1_000 + patch   // strictly increasing, derived
    }
}
```

The `major * 1_000_000 + minor * 1_000 + patch` formula assumes minor/patch never exceed
999 — pick wider multipliers if your project bumps minor/patch past that. What matters is
that `versionCode` is **computed**, not a literal you remember to bump.

**iOS — same pattern for `CFBundleVersion`:**

```kotlin
// iosApp Info.plist generation, or via a Gradle/Fastlane step
val appVersion = libs.versions.app.get()          // "1.19.1" → CFBundleShortVersionString
val buildNumber = System.getenv("CI_BUILD_NUMBER") ?: appVersion.replace(".", "")
    // CFBundleVersion just needs to increase per submitted build — CI build number is a
    // safe monotonic source; deriving it from semver alone breaks if you re-submit a patch
```

Unlike Android's `versionCode`, `CFBundleVersion` only needs to be monotonic **per
`CFBundleShortVersionString`**, so a CI build number (which is already monotonic) is often
the more robust source than deriving it purely from semver.

**The silent-trap pattern to always check for:** a hardcoded `versionCode = 1` (or any
literal platform version field) compiles, runs, and passes local testing identically to a
correctly-derived one. The bug only surfaces as a hard store rejection on the **second**
release — by which point several versions may have shipped without anyone noticing the
field never moved. Grep for `versionCode\s*=\s*\d` with a literal integer as part of any
pre-release review.

---

