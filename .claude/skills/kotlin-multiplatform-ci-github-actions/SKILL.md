---
name: kotlin-multiplatform-ci-github-actions
description: >
  Sets up GitHub Actions CI for a Kotlin Multiplatform (KMP) project.
  Produces two workflow files: ci.yml (lint, Android tests, iOS tests, Desktop JVM tests,
  Web JS + WasmJs tests, Gradle cache) and release.yml (XCFramework build + upload artifact).
  All target platforms are covered. Assumes AGP 9+ and the project structure from
  kotlin-multiplatform-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-06'
  keywords:
    - GitHub Actions
    - CI/CD
    - Kotlin Multiplatform
    - KMP
    - Android
    - iOS
    - Desktop
    - JVM
    - Web
    - JS
    - WasmJs
    - XCFramework
    - Gradle cache
---

## When to Use This Skill

Use when you need to:
- Set up GitHub Actions CI for a new or existing KMP project
- Add automated Android, iOS, Desktop, or Web test jobs
- Configure a release workflow that builds and publishes an XCFramework
- Wire Gradle caching into CI for faster builds

**Requires:** `kotlin-multiplatform-feature-scaffold` project structure (or equivalent AGP 9+ KMP layout).

**Trigger keywords:** set up CI, GitHub Actions, CI pipeline, automated tests, build workflow,
release workflow, KMP CI, XCFramework release, Gradle cache CI, PR checks,
continuous integration, continuous delivery, CD pipeline, GitHub workflow YAML,
automate build, merge checks, branch protection, automated release, deploy workflow.

**Freshness rule:** GitHub Actions runner images and `actions/setup-java` / `gradle/actions` versions
change frequently — recheck pinned versions and `runs-on` labels before using this skill in a new project.

---

## Recommendation First

Default to **two workflow files: `ci.yml` (PR matrix) + `release.yml` (tag-triggered XCFramework)**.

Why:
- per-target jobs (Android, iOS, Desktop, Web) run in parallel and give clear failure attribution
- a separate release workflow keeps tag-triggered publishing decoupled from PR validation
- Gradle cache restore/save steps are critical — without them, KMP CI is prohibitively slow

Set up Gradle caching and the `actions/setup-java` step before anything else.
Skip platform jobs only when the product explicitly excludes that target.

---

## Overview

Two workflow files:

| File | Trigger | Jobs |
|---|---|---|
| `.github/workflows/ci.yml` | push to `main`, all PRs | `lint`, `test-android`, `test-ios`, `test-desktop`, `test-web` |
| `.github/workflows/release.yml` | push tag `v*` | `build-xcframework` |

---

## Step 1: Create `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:

  # ─── Lint ───────────────────────────────────────────────────────────────────
  lint:
    name: Lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Run lint
        run: ./gradlew lint --continue

      - name: Upload lint reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-reports
          path: '**/build/reports/lint-results*.html'

  # ─── Android Tests ──────────────────────────────────────────────────────────
  test-android:
    name: Android Tests
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Run Android unit tests
        run: ./gradlew testDebugUnitTest --continue

      - name: Run KMP common tests (JVM)
        run: ./gradlew allTests --continue

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: android-test-results
          path: '**/build/reports/tests/'

  # ─── iOS Tests ──────────────────────────────────────────────────────────────
  test-ios:
    name: iOS Tests
    runs-on: macos-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Run iOS simulator tests (iosSimulatorArm64)
        run: ./gradlew iosSimulatorArm64Test --continue

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ios-test-results
          path: '**/build/reports/tests/'

  # ─── Desktop Tests ──────────────────────────────────────────────────────────
  test-desktop:
    name: Desktop (JVM) Tests
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Run Desktop (JVM) tests
        run: ./gradlew jvmTest --continue

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: desktop-test-results
          path: '**/build/reports/tests/'

  # ─── Web Tests ──────────────────────────────────────────────────────────────
  test-web:
    name: Web (JS + WasmJs) Tests
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Run JS tests
        run: ./gradlew jsTest --continue

      - name: Run WasmJs tests
        run: ./gradlew wasmJsTest --continue

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: web-test-results
          path: '**/build/reports/tests/'
```

---

## Step 2: Create `.github/workflows/release.yml`

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:

  # ─── Build XCFramework ──────────────────────────────────────────────────────
  build-xcframework:
    name: Build XCFramework
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

      - name: Build XCFramework
        run: ./gradlew :shared:assembleReleaseXCFramework

      - name: Upload XCFramework artifact
        uses: actions/upload-artifact@v4
        with:
          name: XCFramework-${{ github.ref_name }}
          path: shared/build/XCFrameworks/release/
          retention-days: 30

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: shared/build/XCFrameworks/release/**
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Step 3: Gradle configuration for CI

Add to `gradle.properties`:

```properties
# CI performance
org.gradle.configuration-cache=true
org.gradle.parallel=true
org.gradle.caching=true

# Kotlin daemon — reduce memory on CI
kotlin.daemon.jvm.options=-Xmx2g
```

Add to root `build.gradle.kts` — ensures all modules report test results in a CI-friendly format:

```kotlin
subprojects {
    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
        }
    }
}
```

---

## Step 4: Required GitHub secrets

Add these in **Settings → Secrets and variables → Actions**:

| Secret | Purpose |
|---|---|
| `GRADLE_ENCRYPTION_KEY` | Encrypts the Gradle build cache. Generate with: `openssl rand -base64 16` |

No other secrets are required for a public repo. For private repos, `GITHUB_TOKEN` is auto-provided.

---

## Step 5: Enable Gradle build cache on CI

The `gradle/actions/setup-gradle@v4` action automatically:
- Caches `~/.gradle/caches` between runs keyed on dependency hash
- Uploads/restores the Gradle configuration cache
- Reports cache hit/miss in the Actions summary

No extra `actions/cache` step is needed.

---

## Step 6: XCFramework Gradle config (shared module)

Ensure `:shared` (or your main shared KMP module) configures the XCFramework in its `build.gradle.kts`:

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
```

The `assembleReleaseXCFramework` task is auto-generated by the KMP plugin.

---

## Guidelines

- Always use `concurrency` with `cancel-in-progress: true` on CI to cancel stale PR runs
- Run `lint` as a gate before tests — fail fast on obvious issues
- Use `macos-latest` only for iOS jobs (billable minutes ~10× more than Ubuntu)
- Android, Desktop, and Web tests all run on `ubuntu-latest` — fast and cheap
- Use `gradle/actions/setup-gradle@v4` — it supersedes the older `gradle/gradle-build-action`
- Never store secrets in `gradle.properties` — use GitHub secrets and inject via `env:`
- Set `retention-days` on artifacts to avoid storage accumulation

## Verification

1. Open a draft PR — confirm `lint`, `test-android`, `test-ios`, `test-desktop`, `test-web` all trigger
2. Push a `v0.0.1` tag — confirm `build-xcframework` triggers and artifact appears in release
3. Check Actions summary for Gradle cache hit rates after the second run

---

## Common Anti-Patterns

- running all targets in one job — a single iOS failure blocks Android feedback; use per-target jobs
- skipping Gradle cache setup — KMP builds take 10–20 min cold; caching brings it under 5 min
- storing secrets in `gradle.properties` — use GitHub Secrets and inject via `env:` in the workflow
- using `actions/upload-artifact` without `retention-days` — storage accumulates quickly
- triggering the release job on every push instead of on version tags — publishes pre-release builds

If CI is slow, check the Gradle cache hit rate in the Actions summary before making other changes.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — the project structure CI builds and tests
- `kotlin-multiplatform-code-quality` — Ktlint and Detekt checks run as CI gates
- `kotlin-multiplatform-xcframework-spm` — XCFramework release job assembled and published from CI
- `kotlin-multiplatform-roborazzi` — screenshot diff CI job that runs on pull requests

---

## Output Style

When asked about CI setup or GitHub Actions for KMP, respond in this order:
1. recommendation (matrix workflow: Android/iOS/Desktop/Web + XCFramework release job)
2. workflow structure (jobs and trigger events)
3. YAML snippet (one job block)
4. why that CI shape fits KMP targets
5. main alternative (single job, different CI provider)

Keep the YAML snippet to one job. Map to the user's actual module names and signing secrets when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-06 | Initial release. |
