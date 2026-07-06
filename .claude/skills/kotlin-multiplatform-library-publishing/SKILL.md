---
name: kotlin-multiplatform-library-publishing
description: >
  Publish a Kotlin Multiplatform library to Maven Central, GitHub Packages, or both.
  Covers: vanniktech maven-publish plugin setup, POM metadata, Sonatype OSSRH staging,
  multi-artifact BOM, kotlinx-binary-compatibility-validator API tracking, SNAPSHOT vs
  stable channels, and a release checklist. Pairs with kotlin-multiplatform-xcframework-spm
  for iOS/SPM distribution.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
  keywords:
    - maven central
    - maven publish
    - library publishing
    - KMP library
    - vanniktech
    - mavenPublishing
    - OSSRH
    - Sonatype
    - GitHub Packages
    - BOM
    - bill of materials
    - binary compatibility
    - apiCheck
    - api dump
    - kotlinx-binary-compatibility-validator
    - SNAPSHOT
    - artifactId
    - groupId
    - POM metadata
    - publish artifact
    - distribute KMP
    - library versioning
    - staging repository
    - release candidate
    - library consumers
    - multiplatform library
    - publish to maven
    - open source library
---

**Trigger keywords:** publish KMP library, Maven Central, publish library, maven-publish,
vanniktech, mavenPublishing, OSSRH, Sonatype, GitHub Packages library, BOM, bill of materials,
binary compatibility, apiCheck, apiDump, api dump, kotlinx-binary-compatibility-validator,
SNAPSHOT library, library release, distribute KMP, KMP library publishing, artifactId, groupId,
POM metadata, GPG signing library, library consumers, multiplatform library, open source KMP,
library versioning, staging repository, Central Portal

**Freshness rule:** vanniktech plugin releases frequently; check
[github.com/vanniktech/gradle-maven-publish-plugin/releases](https://github.com/vanniktech/gradle-maven-publish-plugin/releases)
and `SonatypeHost.CENTRAL_PORTAL` vs `SonatypeHost.S01` before wiring.
`binary-compatibility-validator` and `dokka` also track Kotlin releases closely —
verify versions in `libs.versions.toml` against the latest Kotlin version in the project.

---

## When to Use This Skill

Use when:
- You are building a KMP library for other developers to consume (not an end-user app)
- You need to publish to Maven Central or GitHub Packages
- You need to manage API surface across versions (`apiCheck`, binary dumps)
- You want a BOM so consumers can align versions across multiple artifacts
- You need SNAPSHOT builds for pre-release testing

**Pairs with:**
- `kotlin-multiplatform-xcframework-spm` — for iOS/SPM binary distribution alongside Maven
- `kotlin-multiplatform-ci-github-actions` — automate publishing in CI
- `kotlin-multiplatform-code-quality` — `detekt` and `ktlint` before publishing

---

## Recommendation First

Use **`com.vanniktech.maven.publish`** (vanniktech plugin). It is the de-facto standard for
KMP → Maven Central. It handles:
- Sonatype OSSRH staging (legacy + Central Portal)
- Javadoc/Dokka jar generation
- Sources jar
- POM generation from DSL
- Signing via GPG

Never wire `maven-publish` manually for Maven Central — POM requirements are strict and
the vanniktech plugin handles all the boilerplate correctly.

---

## Step 1 — Library project structure

A KMP library has **no** application plugin. The root module exposes multiplatform targets.

```
my-library/
├── build-logic/                  # Convention plugins (optional but recommended)
├── library/                      # Main library module
│   └── build.gradle.kts
├── library-testing/              # Test helpers for consumers (optional)
├── bom/                          # Bill of Materials (optional, for multi-artifact)
├── sample/                       # Sample app that consumes the library
│   └── build.gradle.kts          # Has com.android.application — only here
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts              # Root: coordinates + publishing config
```

`settings.gradle.kts` for a library:

```kotlin
rootProject.name = "my-library"

include(":library")
include(":library-testing")    // optional
include(":bom")                // optional
include(":sample:androidApp")  // sample only — no maven-publish applied here
```

---

## Step 2 — Dependencies

`gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.1.21"
vanniktech-publish = "0.30.0"
binary-compat = "0.17.0"
dokka = "2.0.0"

[plugins]
vanniktech-publish = { id = "com.vanniktech.maven.publish", version.ref = "vanniktech-publish" }
binary-compat      = { id = "org.jetbrains.kotlinx.binary-compatibility-validator", version.ref = "binary-compat" }
dokka              = { id = "org.jetbrains.dokka", version.ref = "dokka" }
```

Root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.binary.compat)
    alias(libs.plugins.dokka) apply false
}

// Binary compatibility: track all public APIs
apiValidation {
    ignoredProjects += setOf("sample", "sample-androidApp", "bom")
    nonPublicMarkers += listOf("io.mylib.InternalApi")
}
```

---

## Step 3 — Library module `build.gradle.kts`

```kotlin
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)       // only if targeting Android
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    js(IR) { browser(); nodejs() }
    wasmJs { browser() }
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            // shared dependencies
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)  // use OSSRH for legacy accounts

    signAllPublications()   // requires GPG key in env (see Step 6)

    coordinates(
        groupId    = "io.github.yourhandle",
        artifactId = "my-library",
        version    = version.toString(),   // read from gradle.properties
    )

    pom {
        name = "My Library"
        description = "A concise description of what the library does."
        url = "https://github.com/yourhandle/my-library"
        inceptionYear = "2024"

        licenses {
            license {
                name = "Apache-2.0"
                url  = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id   = "yourhandle"
                name = "Your Name"
                url  = "https://github.com/yourhandle"
            }
        }

        scm {
            url                 = "https://github.com/yourhandle/my-library"
            connection          = "scm:git:git://github.com/yourhandle/my-library.git"
            developerConnection = "scm:git:ssh://git@github.com/yourhandle/my-library.git"
        }
    }
}
```

`gradle.properties` (version managed here, not in build script):

```properties
GROUP=io.github.yourhandle
POM_ARTIFACT_ID=my-library
VERSION_NAME=1.0.0-SNAPSHOT
```

---

## Step 4 — BOM (Bill of Materials) for multi-artifact libraries

Use a BOM when the library ships multiple artifacts that consumers should always
align (`my-library-core`, `my-library-testing`, `my-library-compose`).

`bom/build.gradle.kts`:

```kotlin
plugins {
    `java-platform`
    alias(libs.plugins.vanniktech.publish)
}

javaPlatform { allowDependencies() }

dependencies {
    constraints {
        api(project(":library"))
        api(project(":library-compose"))
        api(project(":library-testing"))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.yourhandle", "my-library-bom", version.toString())
    // … same pom block
}
```

Consumer then uses:

```kotlin
// Consumer build.gradle.kts
dependencies {
    implementation(platform("io.github.yourhandle:my-library-bom:1.0.0"))
    implementation("io.github.yourhandle:my-library")           // no version needed
    testImplementation("io.github.yourhandle:my-library-testing") // no version needed
}
```

---

## Step 5 — Binary compatibility validator

The `binary-compatibility-validator` plugin generates `.api` dump files that track
every public symbol. A CI check (`apiCheck`) fails if a release PR accidentally removes
or changes a public API.

**One-time setup (after configuring the plugin in root build):**

```bash
./gradlew apiDump   # generates library/api/library.api
git add library/api/
git commit -m "chore: initial API dump"
```

**On every PR:**

```bash
./gradlew apiCheck  # fails if public API changed without a matching apiDump
```

**When intentionally changing the API:**

```bash
./gradlew apiDump   # regenerate the dump
git add library/api/
git commit -m "feat!: add Foo.bar() to public API"
```

**Marking internal APIs** (excluded from dump):

```kotlin
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class InternalApi
```

Add `InternalApi` to `nonPublicMarkers` in `apiValidation { }` (Step 2).

---

## Step 6 — GPG signing and secrets

Maven Central requires every artifact to be signed with a GPG key.

**Generate a key (one-time):**

```bash
gpg --gen-key
gpg --list-secret-keys --keyid-format LONG   # note the KEY_ID
gpg --armor --export-secret-keys KEY_ID > signing.gpg
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

**GitHub Actions secrets** (Settings → Secrets):

| Secret | Value |
|---|---|
| `SIGNING_KEY_ID` | Last 8 chars of KEY_ID |
| `SIGNING_KEY` | Contents of `signing.gpg` (base64: `cat signing.gpg \| base64`) |
| `SIGNING_PASSWORD` | Your GPG passphrase |
| `OSSRH_USERNAME` | Sonatype / Central Portal username |
| `OSSRH_PASSWORD` | Sonatype / Central Portal token |

**`gradle.properties`** (never commit secrets here — only for local development):

```properties
signing.keyId=ABCDEF12
signing.password=your-passphrase
signing.secretKeyRingFile=/Users/you/.gnupg/secring.gpg
```

---

## Step 7 — GitHub Packages (simpler alternative / supplement)

GitHub Packages requires no Sonatype account and works with existing GitHub tokens.
Good for: internal libraries, pre-release testing, organisations on GitHub.

`library/build.gradle.kts` (add alongside or instead of Central):

```kotlin
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url  = uri("https://maven.pkg.github.com/yourhandle/my-library")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Consumers add the repository:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/yourhandle/my-library")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull  ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

---

## Step 8 — SNAPSHOT vs stable release channels

| Channel | `VERSION_NAME` | Publishes to | When |
|---|---|---|---|
| SNAPSHOT | `1.1.0-SNAPSHOT` | OSSRH snapshots / GitHub Packages | Every merge to `main` |
| RC | `1.1.0-rc.1` | Maven Central staging | Pre-release testing |
| Stable | `1.1.0` | Maven Central (released) | Tagged releases |

**SNAPSHOT publishing** in CI (`publish.yml`):

```yaml
- name: Publish snapshot
  if: github.ref == 'refs/heads/main'
  run: ./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache
  env:
    ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.OSSRH_USERNAME }}
    ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.OSSRH_PASSWORD }}
    ORG_GRADLE_PROJECT_signingInMemoryKeyId:       ${{ secrets.SIGNING_KEY_ID }}
    ORG_GRADLE_PROJECT_signingInMemoryKey:         ${{ secrets.SIGNING_KEY }}
    ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.SIGNING_PASSWORD }}
```

**Stable publishing** (triggered by version tag `v*`):

```yaml
- name: Publish release
  if: startsWith(github.ref, 'refs/tags/v')
  run: ./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache
  env:
    # same secrets as above
```

---

## Step 9 — Release checklist

Before tagging a stable release:

```
[ ] apiCheck passes — no accidental public API changes
[ ] All targets build: ./gradlew build
[ ] Tests pass on all targets: ./gradlew allTests
[ ] VERSION_NAME in gradle.properties has no -SNAPSHOT suffix
[ ] CHANGELOG updated
[ ] POM metadata complete (description, license, SCM, developers)
[ ] GPG key not expired: gpg --list-keys
[ ] ./gradlew publishToMavenLocal  → smoke-test consumer can resolve from mavenLocal()
[ ] Dry run: ./gradlew publishAllPublicationsToMavenCentralRepository --dry-run
[ ] Tag: git tag v1.1.0 && git push origin v1.1.0
[ ] Verify on search.maven.org (may take 15–30 min to appear)
```

---

## Step 10 — iOS distribution alongside Maven

KMP libraries targeting iOS consumers need two distribution channels in parallel:

| Consumer type | Distribution |
|---|---|
| Android / JVM / JS / Wasm | Maven Central (`implementation("io.github.you:lib:1.0.0")`) |
| iOS (Swift / Xcode) | XCFramework binary target in SPM Package.swift |

See `kotlin-multiplatform-xcframework-spm` for the full iOS distribution flow.
The release CI should run both publish tasks in the same workflow run when a tag is pushed.

---

## Output Style

When generating publishing configuration or release steps, output:
1. The complete `build.gradle.kts` block for the affected module (not diffs — consumers need the full context)
2. The `gradle.properties` fields to add/change
3. A copy-ready CI workflow snippet for the relevant trigger (push to main / tag push)
4. A numbered release checklist the developer can tick off before tagging

Never output partial Gradle snippets without the surrounding `mavenPublishing { }` block —
missing fields cause Maven Central validation failures that are hard to debug.

---

## Common Anti-Patterns

| Mistake | Fix |
|---|---|
| `VERSION_NAME` still has `-SNAPSHOT` on release | Remove the suffix in `gradle.properties` before tagging |
| Missing Javadoc jar | Dokka plugin must be applied; vanniktech plugin auto-configures it |
| `apiCheck` fails in CI but not locally | Run `./gradlew apiDump` locally and commit the `.api` file |
| GPG key expired | `gpg --edit-key KEY_ID` → `expire` → set new expiry → re-upload to keyserver |
| Consumer can't resolve SNAPSHOT | Must add OSSRH snapshot repo: `maven("https://s01.oss.sonatype.org/content/repositories/snapshots")` |
| `signAllPublications()` fails locally | Set `signing.*` properties in `~/.gradle/gradle.properties`, not in the project |
| Missing `scm` block in POM | Maven Central validation rejects POMs without SCM — always include it |

---

## Related Skills

| Skill | When to use alongside this skill |
|---|---|
| `kotlin-multiplatform-xcframework-spm` | Distributing to iOS consumers via SPM binary target (runs in parallel with Maven publishing) |
| `kotlin-multiplatform-ci-github-actions` | Automating publish on tag push and SNAPSHOT on main merge |
| `kotlin-multiplatform-code-quality` | Detekt + ktlint checks to run before publishing |
| `kotlin-multiplatform-unit-testing` | All targets must pass tests before a stable release |
| `kotlin-multiplatform-expect-actual` | Platform-specific implementations inside the library |
| `kotlin-multiplatform-release` | App release pipeline (different from library publishing — covers Play Store / App Store) |

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Initial skill — vanniktech plugin, BOM, binary-compat-validator, SNAPSHOT/stable, GPG, GitHub Packages |
