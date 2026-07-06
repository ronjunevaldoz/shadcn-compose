---
name: kotlin-multiplatform-xcframework-spm
description: >
  Builds an XCFramework from a Kotlin Multiplatform shared module and publishes
  it as a Swift Package Manager (SPM) binary target. Covers: Gradle XCFramework
  configuration, assembleReleaseXCFramework task, local SPM Package.swift for
  development, remote binary SPM target for distribution (GitHub Releases), and
  Xcode project integration. Assumes AGP 9+ and the project structure from
  kotlin-multiplatform-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-06'
  keywords:
    - XCFramework
    - Swift Package Manager
    - SPM
    - iOS
    - binary target
    - Kotlin Multiplatform
    - GitHub Releases
    - Xcode
---

## Overview

```
Gradle build
  → :shared:assembleReleaseXCFramework
  → shared/build/XCFrameworks/release/Shared.xcframework

Distribution options:
  A) Local SPM (development)   — Package.swift pointing to local path
  B) Remote SPM (distribution) — zip + GitHub Release + checksum in Package.swift
```

## When to Use This Skill

Use this skill when you need to:
- Export a KMP shared module as an XCFramework
- Package the framework for Swift Package Manager
- Set up local SPM for development or remote binary distribution
- Recheck Xcode and SPM integration before release

**Trigger keywords:** XCFramework, Swift Package Manager, SPM, binary target, Xcode,
shared framework, iOS distribution, Package.swift.

**Freshness rule:** recheck Xcode and Swift Package Manager docs before changing the
binary target layout or the release workflow.

---

## Recommendation First

Default to **`assembleXCFramework` Gradle task + SPM binary target published via GitHub Releases**.

Why:
- SPM binary targets consume a pre-built framework, so iOS team members don't need Kotlin or Gradle
- GitHub Releases + a checksum URL is the simplest distribution mechanism that Xcode resolves natively
- CI automates the release job on tag push — no manual XCFramework builds

Use CocoaPods only if the iOS project already uses CocoaPods and migrating is not feasible.
Avoid embedding the XCFramework manually — it breaks reproducible builds.

---

## Prerequisites

- KMP project with at least `iosArm64` and `iosSimulatorArm64` targets
- `:shared` module (or equivalent) that exports the iOS API surface
- CI skill (`kotlin-multiplatform-ci-github-actions`) for automated release

---

## Step 1: Configure XCFramework in `:shared/build.gradle.kts`

```kotlin
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("GROUP_ID.core")   // or whatever convention applies
}

kotlin {
    val xcf = XCFramework("Shared")

    iosArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }
    // Add iosX64 if you still need Intel simulator support:
    // iosX64 { binaries.framework { baseName = "Shared"; isStatic = true; xcf.add(this) } }
}
```

This generates:
- `assembleSharedDebugXCFramework`
- `assembleSharedReleaseXCFramework`

---

## Step 2: Build the XCFramework

```bash
# Debug (local development)
./gradlew :shared:assembleSharedDebugXCFramework

# Release (CI / distribution)
./gradlew :shared:assembleSharedReleaseXCFramework
```

Output: `shared/build/XCFrameworks/release/Shared.xcframework`

---

## Step 3A: Local SPM package (development)

Create `iosApp/Packages/Shared/Package.swift` pointing to the local XCFramework path:

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Shared",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            // Path relative to this Package.swift
            path: "../../../../shared/build/XCFrameworks/debug/Shared.xcframework"
        )
    ]
)
```

In Xcode: **File → Add Package Dependencies → Add Local…** → select the `Packages/Shared/` folder.

> Rebuild the XCFramework after any Kotlin changes (`./gradlew :shared:assembleSharedDebugXCFramework`),
> then clean build in Xcode (⇧⌘K) to pick up the new binary.

---

## Step 3B: Remote SPM binary target (distribution)

### 1. Zip the XCFramework

```bash
cd shared/build/XCFrameworks/release/
zip -r Shared.xcframework.zip Shared.xcframework
```

### 2. Compute the checksum

```bash
swift package compute-checksum Shared.xcframework.zip
# outputs a SHA-256 hash, e.g.:
# a1b2c3d4e5f6...
```

### 3. Upload to GitHub Releases

Upload `Shared.xcframework.zip` as a release asset. The URL will be:
```
https://github.com/ORG/REPO/releases/download/v1.0.0/Shared.xcframework.zip
```

### 4. Create `Package.swift` in a dedicated SPM repo (or root of KMP repo)

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Shared",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            url: "https://github.com/ORG/REPO/releases/download/v1.0.0/Shared.xcframework.zip",
            checksum: "a1b2c3d4e5f6..."  // from step 2
        )
    ]
)
```

Commit and tag `Package.swift` with the same version tag (`v1.0.0`).

---

## Step 4: Add to Xcode project

**Option A — In an existing Xcode project:**

1. File → Add Package Dependencies
2. Enter the GitHub URL: `https://github.com/ORG/REPO`
3. Select version rule (exact version, range, or branch)
4. Add `Shared` to your app target

**Option B — In `iosApp/project.pbxproj` via SPM:**

In the Xcode project, the package is resolved automatically when the URL and checksum match.

---

## Step 5: Automate in CI

In `.github/workflows/release.yml` (from `kotlin-multiplatform-ci-github-actions` skill):

```yaml
jobs:
  build-xcframework:
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
        run: ./gradlew :shared:assembleSharedReleaseXCFramework

      - name: Zip XCFramework
        run: |
          cd shared/build/XCFrameworks/release
          zip -r Shared.xcframework.zip Shared.xcframework

      - name: Compute checksum
        id: checksum
        run: |
          CHECKSUM=$(swift package compute-checksum shared/build/XCFrameworks/release/Shared.xcframework.zip)
          echo "checksum=$CHECKSUM" >> "$GITHUB_OUTPUT"

      - name: Update Package.swift checksum
        run: |
          sed -i '' "s/checksum: \".*\"/checksum: \"${{ steps.checksum.outputs.checksum }}\"/" Package.swift
          sed -i '' "s|/releases/download/.*Shared|/releases/download/${{ github.ref_name }}/Shared|" Package.swift

      - name: Commit updated Package.swift
        run: |
          git config user.email "ci@github.com"
          git config user.name "GitHub Actions"
          git add Package.swift
          git commit -m "chore: update Package.swift for ${{ github.ref_name }}"
          git push

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: shared/build/XCFrameworks/release/Shared.xcframework.zip
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Step 6: Kotlin/Swift interop tips

- **`@ObjCName`** — rename Kotlin declarations for Swift consumers:
  ```kotlin
  @ObjCName("UserRepository", swiftName = "UserRepository")
  interface UserRepository { ... }
  ```

- **`@HiddenFromObjC`** — hide internal Kotlin declarations from the Swift API:
  ```kotlin
  @HiddenFromObjC
  internal fun internalHelper() { ... }
  ```

- **`@ShouldRefineInSwift`** — mark a Kotlin function for Swift overlay replacement (advanced):
  ```kotlin
  @ShouldRefineInSwift
  fun getUser(id: String): UserResult
  ```

- Keep the public API surface minimal — only expose what iOS genuinely needs from `:shared`
- Use `isStatic = true` for the framework — dynamic frameworks require embedding steps in Xcode

---

## Guidelines

- Always use `isStatic = true` unless you have a specific reason for dynamic frameworks
- Commit `Package.swift` to the same repo as the KMP project — simpler than a separate SPM repo
- The checksum must match exactly — regenerate on every release
- Version the XCFramework by git tag — `v1.0.0`, `v1.1.0`, etc.
- Test local SPM integration before setting up the remote distribution flow
- `iosX64` (Intel simulator) is increasingly unnecessary — focus on `iosArm64` + `iosSimulatorArm64` (Apple Silicon)

---

## Verification

1. `./gradlew :shared:assembleSharedReleaseXCFramework` — XCFramework builds
2. `ls shared/build/XCFrameworks/release/Shared.xcframework/` — confirm `ios-arm64` and `ios-arm64-simulator` slices exist
3. Open local SPM package in Xcode — resolves without errors
4. Build iOS app target — links against Shared framework successfully
5. Call a Kotlin function from Swift — verify correct behavior

---

## Common Anti-Patterns

- embedding the XCFramework directly in the iOS repo — breaks reproducibility and bloats the repo
- publishing without a checksum in `Package.swift` — Xcode will reject the package or silently use a cached version
- using `embedAndSign` without a CI build that produces a consistent binary — signature mismatch on device
- forgetting to include the simulator slice (`iosSimulatorArm64`) — Apple Silicon Macs can't build the app
- committing `Package.swift` with a hardcoded local path instead of a release URL — breaks for other developers

If SPM resolution fails, verify the checksum in `Package.swift` matches the actual archive SHA256.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — the KMP project structure being exported as an XCFramework
- `kotlin-multiplatform-ci-github-actions` — CI job that assembles the XCFramework and publishes the binary target
- `kotlin-multiplatform-expect-actual` — platform-specific iOS implementations bundled inside the XCFramework

---

## Output Style

When asked about XCFramework or SPM distribution, respond in this order:
1. recommendation (assemble XCFramework, publish as SPM binary target)
2. project structure (Gradle task, Package.swift, CI release job)
3. code snippet (assembleXCFramework task config + Package.swift binaryTarget)
4. why binary target SPM is preferred over source SPM for KMP
5. main alternative (CocoaPods, manual xcframework embedding)

Keep the snippet to the Gradle task and one Package.swift block. Map to the user's actual framework name and repo URL when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-06 | Initial release. |
