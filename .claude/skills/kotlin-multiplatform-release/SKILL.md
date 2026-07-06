---
name: kotlin-multiplatform-release
description: >
  End-to-end KMP library and app release pipeline. Covers versioning strategy
  (gradle.properties as source of truth), Maven Central publishing via the
  vanniktech plugin, GPG signing, Sonatype Central Portal, changelog generation
  with git-cliff and Conventional Commits, GitHub Release creation, secrets
  management patterns, and — for app targets — deriving platform-native version
  fields (Android versionCode, iOS CFBundleVersion) from a single semver source.
  Distinct from the CI skill (which owns workflow YAML) and the xcframework-spm
  skill (which owns SPM binary distribution).
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-07-04'
  keywords:
    - Maven Central
    - publish
    - release
    - versioning
    - semantic versioning
    - gradle.properties
    - vanniktech
    - Sonatype
    - GPG signing
    - git-cliff
    - changelog
    - Conventional Commits
    - GitHub Release
    - Doppler
    - secrets
    - version bump
    - versionCode
    - versionName
    - CFBundleVersion
    - CFBundleShortVersionString
    - Play Store release
    - Play Console upload rejected
    - App Store build number
---

## When to Use This Skill

Use when you need to:
- Publish a KMP library to Maven Central for the first time
- Set up a repeatable versioning + changelog + release workflow
- Wire Maven credentials into CI without exposing them as plain text
- Generate a structured CHANGELOG.md from git history
- Create a GitHub Release with auto-generated release notes
- Decide between secrets management approaches (GitHub Secrets / Doppler / env vars)
- Cut a project release end to end, including version bump, tag, changelog, and GitHub Release

**Trigger keywords:** publish to Maven Central, Maven publish, release library, release project,
cut release, ship version, versioning, semantic versioning, bump version, gradle.properties
version, vanniktech, Sonatype, Central Portal, GPG signing, git-cliff, changelog,
conventional commits, GitHub Release, release pipeline, publish KMP library, release workflow,
secrets management publish, alpha release, beta release, release candidate, pre-release,
snapshot, development version, GitHub Packages, promote to stable, version suffix.

**Freshness rule:** Sonatype Central Portal API and the vanniktech plugin change frequently —
recheck the [vanniktech plugin releases](https://github.com/vanniktech/gradle-maven-publish-plugin/releases)
and [Central Portal docs](https://central.sonatype.com/publishing) before scaffolding a new publish setup.

**Does not own:**
- GitHub Actions workflow YAML → `kotlin-multiplatform-ci-github-actions`
- XCFramework build + SPM binary target → `kotlin-multiplatform-xcframework-spm`
- App Store / Play Store submission → out of scope for this skill

---

## Recommendation First

Default to: **`gradle.properties` VERSION → vanniktech plugin → `ORG_GRADLE_PROJECT_*` env vars → git-cliff changelog → `gh release create`**.

Why:
- `gradle.properties` keeps version as a single editable line — no Gradle task, no plugin needed to bump it
- `ORG_GRADLE_PROJECT_*` env vars are mapped automatically by Gradle — no `-P` flags in scripts, no credential leaks
- vanniktech's `com.vanniktech.maven.publish` is the de-facto standard for KMP Central publishing
- git-cliff + Conventional Commits turns commit history into a structured CHANGELOG automatically
- `gh release create` wires the GitHub Release to the git tag in one command

Use a secrets manager (Doppler, 1Password, AWS Secrets Manager) only if your team already has one. GitHub Secrets is sufficient for most projects.

---

## 1. Versioning

### `gradle.properties` as the single source of truth

```properties
# gradle.properties
# Library version — edit this line to bump, then publish
VERSION=1.0.0
```

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

## 2. Maven Central Publishing

### Plugin setup

```toml
# gradle/libs.versions.toml
[versions]
vanniktech-publish = "0.37.0"

[libraries]
vanniktech-publish-gradlePlugin = { module = "com.vanniktech:gradle-maven-publish-plugin", version.ref = "vanniktech-publish" }

[plugins]
vanniktech-publish = { id = "com.vanniktech.maven.publish", version.ref = "vanniktech-publish" }
# Convention plugin — id matches the precompiled script filename in build-logic
GROUP_ID-library-publish = { id = "GROUP_ID.library.publish", version = "unspecified" }
```

```kotlin
// build-logic/convention/build.gradle.kts
compileOnly(libs.vanniktech.publish.gradlePlugin)
```

```kotlin
// build-logic/convention/src/main/kotlin/GROUP_ID.library.publish.gradle.kts
// Centralized publish convention — apply this in every publishable module.
// Shared POM metadata, signing, and Central Portal target live here once.
// Each module overrides only its own artifactId via mavenPublishing { coordinates() }.
plugins {
    alias(libs.plugins.vanniktech.publish)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set(project.name)
        description.set(project.description ?: project.name)
        url.set("https://github.com/yourhandle/your-repo")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("yourhandle")
                name.set("Your Name")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/yourhandle/your-repo.git")
            developerConnection.set("scm:git:ssh://github.com/yourhandle/your-repo.git")
            url.set("https://github.com/yourhandle/your-repo")
        }
    }
}
```

Each publishable module applies the convention plugin and sets its own coordinates:

```kotlin
// feature/core/build.gradle.kts
plugins {
    alias(libs.plugins.GROUP_ID.library.publish)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.yourhandle",
        artifactId = "your-library-core",
        version = providers.gradleProperty("VERSION").getOrElse(error("VERSION not set")),
    )
}
```

### Credentials via `ORG_GRADLE_PROJECT_*` env vars

Gradle automatically maps `ORG_GRADLE_PROJECT_X` → project property `X`. No `-P` flags needed.

| Env var | Purpose |
|---|---|
| `ORG_GRADLE_PROJECT_mavenCentralUsername` | Sonatype Central Portal username |
| `ORG_GRADLE_PROJECT_mavenCentralPassword` | Sonatype Central Portal password (user token) |
| `ORG_GRADLE_PROJECT_signingInMemoryKey` | ASCII-armored GPG private key |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` | GPG key passphrase |

Store these in your secrets manager of choice and inject them at publish time. Never commit them.

### Publish command

```bash
./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache
```

`--no-configuration-cache` is required — the vanniktech plugin is not configuration-cache compatible as of v0.37.

### Secrets management options

| Approach | When to use |
|---|---|
| **GitHub Secrets** (default) | Simple projects; secrets injected automatically in Actions workflows |
| **Doppler** | Teams already using Doppler; local publish scripts need the same secrets as CI |
| **1Password / AWS Secrets Manager** | Enterprise setups with existing secrets infrastructure |

For GitHub Secrets, add `ORG_GRADLE_PROJECT_*` variables directly in Settings → Secrets → Actions. They are available as env vars in the workflow with no extra configuration.

---

## 3. Changelog with git-cliff

### `cliff.toml` (project root)

```toml
[changelog]
header = "# Changelog\n"
body = """
{% if version %}\
## [{{ version | trim_start_matches(pat="v") }}] — {{ timestamp | date(format="%Y-%m-%d") }}
{% else %}\
## [Unreleased]
{% endif %}\
{% for group, commits in commits | group_by(attribute="group") %}
### {{ group }}
{% for commit in commits %}
- {% if commit.scope %}**{{ commit.scope }}:** {% endif %}{{ commit.message | upper_first }}\
  {% if commit.breaking %} ⚠ BREAKING{% endif %}
{%- endfor %}
{% endfor %}\n
"""
trim = true

[git]
conventional_commits = true
filter_unconventional = true
commit_parsers = [
  { message = "^feat",     group = "Features" },
  { message = "^fix",      group = "Bug Fixes" },
  { message = "^refactor", group = "Refactoring" },
  { message = "^perf",     group = "Performance" },
  { message = "^docs",     group = "Documentation" },
  { message = "^build",    group = "Build" },
  { message = "^ci",       group = "CI" },
  { message = "^chore",    skip = true },
  { message = "^test",     skip = true },
]
filter_commits = true
tag_pattern = "v[0-9].*"
sort_commits = "oldest"
```

### Commands

```bash
# Update CHANGELOG.md for a new tag
git-cliff --tag v1.2.0 --output CHANGELOG.md

# Generate release notes only (for GitHub Release body)
git-cliff --tag v1.2.0 --unreleased --strip all

# Full history regeneration
git-cliff --output CHANGELOG.md
```

Install: `brew install git-cliff`

Requires Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.) — enforce with the `code-quality` skill's commit-lint setup.

---

## 4. GitHub Release

```bash
# Generate release notes from git-cliff
NOTES=$(git-cliff --tag v1.2.0 --unreleased --strip all)

# Create the GitHub Release
gh release create v1.2.0 \
  --title "v1.2.0" \
  --notes "$NOTES" \
  --verify-tag
```

`--verify-tag` ensures the tag exists before creating the release. Push the tag before running this:

```bash
git tag v1.2.0
git push origin v1.2.0
gh release create v1.2.0 ...
```

For libraries that also ship an XCFramework: attach the `.zip` artifact to the release and update `Package.swift`. See `kotlin-multiplatform-xcframework-spm` for the full SPM binary target workflow.

---

## 5. Local publish script

A reproducible local publish script eliminates "works on my machine" publish failures.

```bash
#!/usr/bin/env bash
# scripts/publish.sh — publish to Maven Central
# Usage: ./scripts/publish.sh [patch|minor|major]
set -euo pipefail

BUMP="${1:-patch}"
PROPS="gradle.properties"

# Read current version
CURRENT=$(grep '^VERSION=' "$PROPS" | cut -d= -f2)
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"

case "$BUMP" in
  patch) NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))" ;;
  minor) NEW_VERSION="$MAJOR.$((MINOR + 1)).0" ;;
  major) NEW_VERSION="$((MAJOR + 1)).0.0" ;;
  *) echo "Usage: $0 [patch|minor|major]"; exit 1 ;;
esac

echo "Bumping $CURRENT → $NEW_VERSION"

# Bump version
# cross-platform: BSD (macOS) and GNU (Linux) sed differ on -i syntax
if sed --version 2>/dev/null | grep -q GNU; then
    sed -i "s/^VERSION=.*/VERSION=$NEW_VERSION/" "$PROPS"
else
    sed -i '' "s/^VERSION=.*/VERSION=$NEW_VERSION/" "$PROPS"
fi

# Run publish (credentials injected via env or secrets manager)
./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache

# Commit, tag, push
git add "$PROPS"
git commit -m "chore(release): bump version to $NEW_VERSION"
git tag "v$NEW_VERSION"
git push origin main "v$NEW_VERSION"

# GitHub Release
NOTES=$(git-cliff --tag "v$NEW_VERSION" --unreleased --strip all)
gh release create "v$NEW_VERSION" --title "v$NEW_VERSION" --notes "$NOTES" --verify-tag

echo "Released v$NEW_VERSION"
```

---

## 6. Release checklist

Before tagging:
- [ ] All tests pass locally (`./gradlew check`)
- [ ] `CHANGELOG.md` is up to date (`git-cliff --tag vX.Y.Z --output CHANGELOG.md`)
- [ ] `VERSION` in `gradle.properties` matches the intended tag
- [ ] No `-SNAPSHOT` suffix in `VERSION`
- [ ] Maven credentials are available in the environment
- [ ] GPG key is loaded (`gpg --list-secret-keys`)

After tagging:
- [ ] GitHub Release created with release notes
- [ ] Artifact visible on [central.sonatype.com](https://central.sonatype.com) (allow ~10 min propagation)
- [ ] XCFramework zip attached to the release and `Package.swift` checksum updated (if shipping SPM)

**App target with an Android/iOS/store target** (in addition to the checks above):
- [ ] `versionCode` (Android) is derived from the semver source, not a literal integer — grep for `versionCode\s*=\s*\d`
- [ ] `versionCode` for this build is strictly higher than the last **accepted** Play Console upload
- [ ] `CFBundleVersion` (iOS) is monotonic for this `CFBundleShortVersionString` — not reused from a prior submission

---

## Common Anti-Patterns

| Anti-pattern | Rule |
|---|---|
| Fallback version string in `build.gradle.kts` (`?: "0.1.0"`) | Use `error()` — a missing VERSION should fail loudly |
| Publishing `-SNAPSHOT` or any pre-release suffix to Maven Central | Central rejects snapshots and pre-release suffixes; use GitHub Packages for alpha/beta/rc distribution |
| Credentials as `-P` flags (`-PmavenCentralUsername=...`) | Use `ORG_GRADLE_PROJECT_*` env vars — Gradle maps them automatically, no flags needed |
| Version bump in CI | Version is a publisher decision; bump in `gradle.properties` before the CI publish job |
| Committing credentials to `gradle.properties` or `.env` | Always gitignore `.env`; store credentials in GitHub Secrets or a secrets manager |
| Skipping `--no-configuration-cache` | vanniktech plugin is incompatible with configuration cache; omitting this flag causes silent failures |
| Hardcoding Android `versionCode` (or any platform-native version field) as a static literal | Derive it from the single semver source (`major * 1_000_000 + minor * 1_000 + patch`) — a static value builds and runs fine locally but causes a hard Play Console rejection on the *second* upload |
| Deriving iOS `CFBundleVersion` purely from semver | Use a monotonic CI build number instead — `CFBundleVersion` must increase per build submitted for the same `CFBundleShortVersionString`, which pure semver derivation breaks on a re-submitted patch |

---

## Testing the Release Pipeline

The release pipeline itself has no unit-testable Kotlin code — it is a build script and CI workflow. Test it by validating the inputs and outputs at each stage:

| What to verify | How |
|---|---|
| `VERSION` is read correctly | `./gradlew printVersion` — add a task: `tasks.register("printVersion") { doLast { println(project.findProperty("VERSION")) } }` |
| Publish runs without credentials | `./gradlew publishAllPublicationsToMavenCentralRepository --dry-run` — confirms task graph without uploading |
| git-cliff produces valid output | `git-cliff --tag vX.Y.Z --unreleased --strip all` — inspect the output before creating a release |
| `ORG_GRADLE_PROJECT_*` mapping works | Set vars locally and run `./gradlew publishToMavenLocal` — verifies credential injection without hitting Central |
| Changelog is up to date | `git-cliff --output CHANGELOG.md && git diff CHANGELOG.md` — should show only the new section |

**Freshness rule:** run `./gradlew publishToMavenLocal` before every real publish to catch configuration drift early.

---

## Related Skills

- `kotlin-multiplatform-ci-github-actions` — GitHub Actions workflow YAML that triggers the publish job on tag push
- `kotlin-multiplatform-xcframework-spm` — XCFramework build + SPM binary target; attach the zip to the GitHub Release created here
- `kotlin-multiplatform-code-quality` — commit-lint enforcement for Conventional Commits (required for git-cliff)
- `kotlin-multiplatform-flavor-environment` — dev/staging/prod config; keep release credentials separate from app config

---

## Output Style

When setting up a release pipeline:

1. **Versioning** — confirm `gradle.properties` VERSION is the source of truth
2. **Plugin** — add vanniktech to version catalog + convention plugin
3. **Credentials** — identify the secrets approach (GitHub Secrets / Doppler / other) and show the `ORG_GRADLE_PROJECT_*` mapping
4. **Changelog** — scaffold `cliff.toml`; confirm Conventional Commits are in use
5. **Release script** — generate `scripts/publish.sh` for local use
6. **CI integration** — point to `kotlin-multiplatform-ci-github-actions` for the workflow YAML
7. **Checklist** — present the pre/post-release checklist

Never generate credentials or keys. If GPG setup is needed, give the commands the user runs themselves.

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-04 | Added "App targets: deriving platform-native version fields from one semver source" — Android versionCode, iOS CFBundleVersion, desktop package versions, all derived (never hardcoded) from the single semver source. New checklist items and anti-patterns for the hardcoded-versionCode silent trap (filed as GitHub issue #2). |
| 2026-06-27 | Replaced id("...") with alias(libs.plugins.*) in both the convention plugin and consuming modules. Added convention plugin alias to libs.versions.toml. |
| 2026-06-26 | Bumped vanniktech maven-publish plugin base version to 0.37.0. |
| 2026-06-24 | Added explicit `release project` / `cut release` / `ship version` trigger keywords so project release requests route here instead of the consumer changelog agent. |
| 2026-06-23 | Initial release — versioning, Maven Central, git-cliff, GitHub Release, local publish script, anti-patterns. |
