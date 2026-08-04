---
name: kmp-library-publishing
description: >
  Publish a Kotlin Multiplatform library to Maven Central, GitHub Packages, or both.
  Covers: vanniktech maven-publish plugin setup, POM metadata, Sonatype OSSRH staging,
  multi-artifact BOM, kotlinx-binary-compatibility-validator API tracking, SNAPSHOT vs
  stable channels, and a release checklist. Pairs with kmp-xcframework-spm
  for iOS/SPM distribution.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
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
    - pre-1.0 api stability
    - NOTICE file
    - third-party license aggregation
    - CONTRIBUTING.md
    - open source contribution
    - dependency vulnerability scanning
    - publish to maven
    - open source library
    - license header
    - AbsentOrWrongFileLicense
    - file license
---

**Trigger keywords:** publish KMP library, Maven Central, publish library, maven-publish,
vanniktech, mavenPublishing, OSSRH, Sonatype, GitHub Packages library, BOM, bill of materials,
binary compatibility, apiCheck, apiDump, api dump, kotlinx-binary-compatibility-validator,
SNAPSHOT library, library release, distribute KMP, KMP library publishing, artifactId, groupId,
POM metadata, GPG signing library, library consumers, multiplatform library, open source KMP,
library versioning, staging repository, Central Portal, license header, file license header,
AbsentOrWrongFileLicense, per-file license.

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
- `kmp-xcframework-spm` — for iOS/SPM binary distribution alongside Maven
- `kmp-ci-github-actions` — automate publishing in CI
- `kmp-code-quality` — `detekt` and `ktlint` before publishing

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

**Clone the official JetBrains starting point first — never hand-write this from
scratch.** `Kotlin/multiplatform-library-template` is the real, actively-maintained
equivalent of `kmp-wizard` for a library (verified against the live repo, not assumed —
"official project" badge, same GitHub org):

```bash
git clone --depth 1 https://github.com/Kotlin/multiplatform-library-template <PROJECT_NAME>
cd <PROJECT_NAME> && rm -rf .git && git init
```

What it gives you out of the box: `vanniktech-mavenPublish`, the AGP 9
`com.android.kotlin.multiplatform.library` plugin, and `jvm()`/`androidLibrary()`/
`iosArm64()`/`iosSimulatorArm64()`/`linuxX64()` targets already wired in one `:library`
module. Add `js()`/`wasmJs()` yourself if `PLATFORMS` includes Web — the template doesn't
include them by default. The template's own README says explicitly what it deliberately
leaves out: binary-compatibility tracking, `explicitApi()`, licensing, and a contribution
guideline — that's exactly what Steps 2/5/12/13 below add on top, the same way this
collection's own 6-layer conventions layer on top of kmp-wizard for an app.

Resulting structure, once this collection's own additions (`library-testing`, `bom`,
`sample`) are layered on. **No `build-logic/`** — the template doesn't ship one, and for
a single `:library` module it adds nothing: there's only one `build.gradle.kts` to
configure, so there's no duplication for a convention plugin to remove. It only earns
its keep once Step 1a's multi-module split is in play — see that section for the real
wiring, not asserted here as a default:

```
my-library/
├── library/                      # Main library module (from the template)
│   └── build.gradle.kts
├── library-testing/              # Test helpers for consumers (optional, added by this skill)
├── bom/                          # Bill of Materials (optional, added by this skill)
├── sample/                       # Sample app that consumes the library (added by this skill)
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

For a library small enough to fit in one `:library` module, that's the whole structure.
Once `:library` itself grows past a handful of files covering genuinely separate
concerns, `kmp-clean-architecture`'s 6-layer contract applies to a
library's own internals the same way it does to an app's — `:model`/`:api` split,
`internal` visibility between layers — the difference is only that the *outermost*
public surface is what `explicitApi()`/`apiCheck` above govern, not an app's UI layer.

### Step 1a — Splitting into multiple published modules

Split when a sub-feature has a genuinely independent consumer surface — some consumers
shouldn't have to pull another facet's transitive deps. Not the default, and not just
because the code is "big" (that's what Step 1's internal 6-layer split is for, inside
one module).

Prefix every module and artifact with the library's own `PROJECT_NAME` — never the
literal word "library." This matches the `<PROJECT_NAME>-bom` convention already used
in Step 4 below, and real published multi-module libraries (Coil's `coil-core` /
`coil-compose` / `coil-network`, not `library-core`):

```
<PROJECT_NAME>/
├── <PROJECT_NAME>-core/       # io.github.you:<PROJECT_NAME>-core      — no Compose dep
│   └── build.gradle.kts
├── <PROJECT_NAME>-compose/    # io.github.you:<PROJECT_NAME>-compose   — depends on -core + Compose
│   └── build.gradle.kts
├── <PROJECT_NAME>-testing/    # io.github.you:<PROJECT_NAME>-testing   — fakes/test doubles, depends on -core only
│   └── build.gradle.kts
├── bom/                        # io.github.you:<PROJECT_NAME>-bom       — version-aligns all three
├── sample/
└── settings.gradle.kts
```

```kotlin
// settings.gradle.kts
includeBuild("build-logic")
include(":<PROJECT_NAME>-core")
include(":<PROJECT_NAME>-compose")
include(":<PROJECT_NAME>-testing")
include(":bom")
include(":sample:androidApp")
```

**This is where `build-logic/` actually earns its keep** — three-plus modules that all
need the same `explicitApi()`/AGP/`apiCheck` configuration is real duplication a
convention plugin removes. Single-module libraries (Step 1 above) skip this entirely.

```kotlin
// build-logic/build.gradle.kts
plugins { `kotlin-dsl` }
dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.vanniktech.mavenPublish.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
gradlePlugin {
    plugins {
        register("libraryModule") {
            id = "<PROJECT_NAME>.library-module"
            implementationClass = "LibraryModuleConventionPlugin"
        }
    }
}
```

```kotlin
// build-logic/src/main/kotlin/LibraryModuleConventionPlugin.kt — the shared config,
// written once, applied to <PROJECT_NAME>-core/-compose/-testing's own build.gradle.kts
class LibraryModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.vanniktech.maven.publish")
        extensions.configure<KotlinMultiplatformExtension> {
            explicitApi()
        }
    }
}
```

```kotlin
// <PROJECT_NAME>-core/build.gradle.kts — each module applies the convention plugin,
// then only its own module-specific bits (dependencies, its own coordinates())
plugins {
    id("<PROJECT_NAME>.library-module")
}
```

Dependency direction — one-way, never circular:

```
<PROJECT_NAME>-core  ←  <PROJECT_NAME>-compose
<PROJECT_NAME>-core  ←  <PROJECT_NAME>-testing
```

`-compose` and `-testing` may depend on `-core`; `-core` never depends on either.

Each module is its own `explicitApi()` surface with its own `.api` file — `apiCheck`
runs per module, not once for the whole repo:

```bash
./gradlew :<PROJECT_NAME>-core:apiCheck :<PROJECT_NAME>-compose:apiCheck :<PROJECT_NAME>-testing:apiCheck
```

Each gets its own `mavenPublishing { coordinates(...) }` block with its own artifactId
(`<PROJECT_NAME>-core`, `<PROJECT_NAME>-compose`, ...) — `bom/`'s `constraints` block
(Step 4 below) is what lets a consumer pin all of them to one version via a single BOM
import instead of separate version numbers per artifact.

**When to split vs keep one `:library`:** a genuinely separate consumer surface (core
logic vs a Compose UI layer vs test fakes) that some consumers want without the others'
dependencies. Splitting because it's "organized that way internally" isn't a reason —
that's Step 1's internal 6-layer split, inside one module, no extra published artifacts.

---

## Step 2 — Dependencies

`gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.4.0"
vanniktech-publish = "0.37.0"
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
    explicitApi()   // library-only — forces every public declaration to state its
                    // visibility and return type explicitly, see below

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
VERSION_NAME=0.1.0-SNAPSHOT
```

Start at `0.1.0`, not `1.0.0` — a fresh library has had zero real consumer usage yet, and
`1.0.0` is a stability promise this skill's own pre-1.0 policy (below) says to make
deliberately, not on the first commit. `Kotlin/multiplatform-library-template` (Step 1's
starting clone) hardcodes `version = "1.0.0"` in its own `build.gradle.kts` — change it
to `0.1.0` as part of the same configuration pass that sets `GROUP_ID`/coordinates, don't
leave the template's default in place.

### Per-file license headers (optional)

The POM `licenses { license { ... } }` block above is the legally required, project-level
license declaration for Maven Central — that's not optional. A per-file license header
comment repeated at the top of every `.kt` source file is a **separate, optional** choice,
worth it for a library specifically because each file may be vendored, copy-pasted, or
inspected independently of the repo it came from; a project-level `LICENSE` file alone
doesn't travel with an individual file once it's copied elsewhere. It's not needed for
app code (see `kmp-code-quality`'s Comment & KDoc Conventions).

Enforce it with Detekt's `AbsentOrWrongFileLicense` rule (off by default):

```yaml
# detekt.yml
comments:
  AbsentOrWrongFileLicense:
    active: true
    licenseTemplateFile: 'license.template.txt'
```

```
# license.template.txt
/*
 * Copyright 2026 Your Name or Org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
```

Keep the license identifier here consistent with the POM's `licenses { license { name = ... } }`
block — a per-file header claiming a different license than the POM declares is worse
than having no per-file header at all.

### `explicitApi()` — library-only, not for app code

Library code has a wider blast radius than app code: a `public` declaration nobody
intended to expose becomes part of the API surface the moment it ships, and removing it
later is a breaking change (exactly what `apiCheck`/binary-compatibility-validator in
Step 5 exists to catch after the fact). `explicitApi()` catches it *before* publishing
instead — it fails the build on any public declaration missing an explicit visibility
modifier or return type:

```kotlin
// ❌ fails to compile under explicitApi() — implicit public visibility, inferred return type
class UserRepository {
    fun getUser(id: String) = api.fetchUser(id)
}

// ✓ compiles — visibility and return type both explicit
public class UserRepository {
    public fun getUser(id: String): User = api.fetchUser(id)
}
```

**Don't add this to app code** — `kmp-clean-architecture`'s 6-layer
contract already controls what's exposed between modules via `internal`, and an app has
no external consumers to protect from an accidental public leak the way a published
library does. `explicitApi()` on app code is pure ceremony with no corresponding benefit.

Two modes: `explicitApi()` fails the build (`ExplicitApiMode.Strict`), `explicitApiWarning()`
only warns. Use the strict form for a library that's already past its first stable
release — a warning is easy to ignore and defeats the point of catching this before
publishing.

### No forced framework coupling in library internals

`kmp-dependency-injection` recommends Koin for **app code** — a
published library is a different situation. A consumer app might use Koin, Hilt, manual
DI, or nothing at all; a library that hard-imports `org.koin.*` inside its own public
classes forces that choice onto every consumer, or worse, silently requires Koin to be
on the consumer's classpath at all.

```kotlin
// ❌ forces Koin onto every consumer of this library
class UserRepository(scope: Scope) : KoinComponent {
    private val api: ApiClient by inject()
}

// ✓ plain constructor injection — the consumer wires it however they want
public class UserRepository(private val api: ApiClient) {
    // ...
}
```

If the library wants to *offer* Koin wiring as a convenience, ship it as a separate,
optional artifact (`my-library-koin`) with its own `module { }` — never bake the
dependency into the core artifact's own classes.

### KDoc coverage on the public API surface

`kmp-code-quality`'s Comment & KDoc Conventions section covers KDoc
*style* — this is about *coverage*. Once `explicitApi()` forces every public declaration
to be deliberate, an undocumented one is a real gap: a consumer sees the declaration in
autocomplete with no explanation of what it does or when to use it.

```kotlin
// ❌ compiles under explicitApi(), but a consumer has no idea what this does
public class RetryPolicy(public val maxAttempts: Int, public val backoffMs: Long)

// ✓ the public contract is documented, not just the visibility
/**
 * Controls retry behavior for transient network failures.
 * @property maxAttempts stop retrying after this many attempts, including the first
 * @property backoffMs delay between attempts, doubled after each failure
 */
public class RetryPolicy(public val maxAttempts: Int, public val backoffMs: Long)
```

`kmp-audit`'s `_detect_undocumented_public_api` flags a public
declaration with no preceding KDoc block, but only in a project that already uses
`explicitApi()` — without it, "public" isn't a deliberate signal worth checking.

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

### `apiCheck` catches *that* the API changed, not *whether the version bump matches*

`apiCheck` fails on any `.api` diff, forcing a deliberate `apiDump` — but it has no
concept of semver. It passes identically whether the diff is a source-compatible
addition (minor-worthy) or a signature change/removal that breaks every consumer
(major-worthy). Nothing currently blocks tagging a *breaking* diff as a minor release.

This isn't mechanically enforceable from the `.api` file alone — the file lists symbols,
not which specific lines changed *how* between two dumps, and "is this actually
source/binary breaking" needs a real diff, not just a checksum mismatch. Treat it as a
review-time discipline instead: before tagging, `git diff` the previous `library.api`
against the new one and classify every change —

| Change | Semver bump |
|---|---|
| New public class/function/property added | Minor |
| Existing public signature changed or removed | Major |
| Internal-only change, `.api` file untouched | Patch |

Get this wrong once (a breaking change shipped as a minor) and every consumer pinned to
`^x.y` silently breaks on their next `./gradlew build` — there's no compiler error on
their side, just a runtime `NoSuchMethodError` or a build failure with no obvious cause.

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

### Pre-1.0 API stability policy

State this explicitly in the README, not just implicitly through version numbers — a
consumer has no way to know your intent otherwise:

- **`0.x.y`**: any release, including a patch, may break the public API without a major
  bump. SemVer's own spec (2.4) says 0.x is for initial development and stability isn't
  promised yet. `apiCheck` still runs and still catches the diff — it just doesn't gate
  the version bump the way it does post-1.0.
- **`1.0.0` and later**: a breaking `apiCheck` diff requires a major bump, full stop —
  this is the point SemVer's stability promise actually starts.
- **Ship 1.0.0 deliberately**, not by drift. Cutting it means committing to the current
  public surface — do it after the API has had real consumer usage, not on the first
  release that happens to work.

---

## Step 9 — Release checklist

Before tagging a stable release:

```
[ ] apiCheck passes — no accidental public API changes
[ ] All targets build: ./gradlew build
[ ] Tests pass on all targets: ./gradlew allTests
[ ] VERSION_NAME in gradle.properties has no -SNAPSHOT suffix
[ ] CHANGELOG updated
[ ] NOTICE.md current — no newly-bundled dependency missing an entry (Step 12)
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

See `kmp-xcframework-spm` for the full iOS distribution flow.
The release CI should run both publish tasks in the same workflow run when a tag is pushed.

---

## Step 11 — Ongoing maintenance (post-1.0)

Everything above covers shipping. A published library also needs a maintenance
practice — real gaps found repeatedly in libraries that only had a publish checklist:

### Deprecation cycle, not silent removal

Never delete a public symbol a consumer might already depend on. Mark it first:

```kotlin
@Deprecated(
    message = "Use fetchUserV2() — handles pagination correctly",
    replaceWith = ReplaceWith("fetchUserV2(id)"),
    level = DeprecationLevel.WARNING,
)
fun fetchUser(id: String): User
```

Cycle, tied to SemVer:
1. **This minor version** — add `@Deprecated(level = WARNING)`. `apiCheck` still passes;
   this is not a binary-breaking change.
2. **Next minor version** — bump to `level = ERROR`. Consumers must migrate to keep
   compiling, but the symbol still exists (source-compatible migration window).
3. **Next major version** — remove the symbol entirely. `apiDump` records the removal;
   `apiCheck` correctly fails until the API dump is regenerated for the major bump.

### Communicating a breaking change

A binary-incompatible change (an `apiCheck` failure you're accepting on purpose, not a
mistake to fix) needs three things before it ships, not just a version bump:
- A `CHANGELOG.md` entry naming the exact symbol and the replacement, not just "breaking changes"
- A migration note if the fix isn't mechanical (find/replace) — show the before/after
- The major version bump itself, per SemVer — a breaking change is never a minor/patch release

### Dependency upgrade cadence

A library's own dependency versions become every consumer's transitive minimum. Pin
conservatively and review on a cadence, not reactively:
- Renovate or Dependabot on the repo, scoped to `gradle/libs.versions.toml`
- Treat a transitive major-version bump (Compose Multiplatform, Kotlin itself) as its own
  reviewed change, never bundled silently into an unrelated feature release
- Keep `sample/`'s own dependency versions pinned to the library's own — a stale sample
  masks a real compatibility break until a real consumer hits it first

### Dependency vulnerability scanning

Distinct from the version-cadence review above — a dependency can be current and still
carry a disclosed CVE. Enable GitHub's own **Dependabot security alerts** (Settings →
Security → Dependabot, or a `.github/dependabot.yml` scoped to `gradle`) on the repo — it
flags a known vulnerability in a dependency independent of whether a routine upgrade PR
would have touched it. Treat an alert on a library's own dependency as higher priority
than the same alert in an app: every consumer inherits it transitively, and a library
maintainer usually doesn't know how many downstream apps are affected.

### Keep `sample/` from drifting

The sample app is the only thing that actually compiles against the library's *public*
API the way a real consumer would — an internal test suite compiles against internals
too and can miss a public-surface break. Run the sample's build as its own CI job on
every PR, not just at release time:

```bash
./gradlew :sample:compileKotlinX  # X = every registered target
```

A sample that still compiles against a symbol scheduled for removal is a signal the
deprecation cycle above hasn't actually reached consumers yet — don't remove the symbol
from the library until the sample itself has migrated off it.

---

## Step 12 — Third-party license aggregation (NOTICE file)

Any dependency the library bundles or statically links (not a transitive Maven
dependency a consumer's own build resolves separately — the reused *art/code* inside
your own artifact) needs its license terms disclosed, not just satisfied silently. Real
precedent from this collection's own published libraries: `heroicons-compose` compiles
Tailwind Labs' MIT-licensed Heroicons into its own `ImageVector`s and ships a
`NOTICE.md` naming the origin, license, and copyright — required because the icon *art*
is redistributed inside the artifact, not merely referenced.

```markdown
<!-- NOTICE.md -->
This library includes compiled artwork from Heroicons (https://github.com/tailwindlabs/heroicons),
Copyright (c) Tailwind Labs, Inc., licensed under the MIT License. See LICENSES/heroicons-MIT.txt
for the full license text.
```

Rules:
- One entry per bundled/redistributed dependency, naming the project, copyright holder,
  license, and a pointer to the full license text (`LICENSES/<name>-<license>.txt`)
- A dependency a consumer resolves themselves via Maven (a normal `api`/`implementation`
  declaration) does **not** need a NOTICE entry — that consumer already sees the
  dependency's own license via their own build tool; NOTICE is for what's *inside* your
  artifact, not what's next to it on the classpath
- Regenerate the check as part of the release checklist (Step 9), not once at the start —
  a NOTICE file goes stale silently the moment a new bundled asset/dependency is added

---

## Step 13 — Open-source contribution scaffolding

Only add this once the library actually intends to take outside contributions — it's
overhead a solo-maintained library doesn't need yet. Once it does:

```
.github/
├── CONTRIBUTING.md          # build/test/PR steps — link back to this repo's own AGENTS.md
│                             #   if AI-assisted contributions are welcome
├── CODE_OF_CONDUCT.md        # Contributor Covenant is the common default
├── ISSUE_TEMPLATE/
│   ├── bug_report.md
│   └── feature_request.md
└── PULL_REQUEST_TEMPLATE.md  # checklist: apiCheck passes, CHANGELOG updated, tests added
```

`CONTRIBUTING.md` should point at this skill's own release checklist (Step 9) and
`kmp-code-quality`'s Ktlint/Detekt setup so a contributor's PR matches
CI expectations before review, not after a round-trip of comments. This is a library-
specific concern distinct from `kmp-project-docs-maintainer`'s
consumer-facing README/onboarding docs — CONTRIBUTING is for people *changing* the
library, not people *using* it.

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
| Per-file license header names a different license than the POM's `licenses { license { name = ... } }` | Keep both in sync — a mismatched per-file header is worse than no per-file header at all |
| No `explicitApi()` | A public declaration nobody intended to expose ships as part of the API surface; `apiCheck` only catches the *next* accidental change, not the first one |
| Library's public classes `import org.koin.*` directly | Forces the consumer's DI choice; use plain constructor injection, ship Koin wiring as a separate optional artifact if wanted |
| Public class/fun with no KDoc under `explicitApi()` | The declaration is deliberate but undocumented — a consumer sees it in autocomplete with no explanation |
| Shipping a breaking `.api` diff as a minor version | `apiCheck` only confirms the diff was deliberate, not that the semver bump matches its severity — classify every diff (addition = minor, signature change/removal = major) before tagging |
| Hand-writing `settings.gradle.kts`/root `build.gradle.kts` from scratch for a new library | Clone `Kotlin/multiplatform-library-template` first (Step 1) — the real official starting point, same discipline as `kmp-wizard` for an app |

---

## Related Skills

| Skill | When to use alongside this skill |
|---|---|
| `kmp-xcframework-spm` | Distributing to iOS consumers via SPM binary target (runs in parallel with Maven publishing) |
| `kmp-ci-github-actions` | Automating publish on tag push and SNAPSHOT on main merge |
| `kmp-code-quality` | Detekt + ktlint checks to run before publishing |
| `kmp-unit-testing` | All targets must pass tests before a stable release |
| `kmp-expect-actual` | Platform-specific implementations inside the library |
| `kmp-release` | App release pipeline (different from library publishing — covers Play Store / App Store) |
| `kmp-project-docs-maintainer` | `docs/libraries.md` catalogs every published coordinate/version — point the release checklist there |
| `kmp-docs-site` | Public GitHub Pages developer guide + Dokka HTML API reference; reuses this skill's Dokka setup for the separate HTML output, not the Javadoc jar |
| `kmp-dependency-injection` | That skill's Koin recommendation is scoped to app code — see "No forced framework coupling in library internals" above for why a library's own classes shouldn't hard-depend on it |
| `kmp-audit` | `_detect_undocumented_public_api` flags a public declaration with no KDoc, scoped to projects using `explicitApi()`; `_detect_library_missing_explicit_api`/`_detect_library_missing_binary_compat_validator`/`_detect_library_multimodule_missing_build_logic` check whether this skill's own Steps 1a/3/5 were actually followed, gated on vanniktech-mavenPublish being applied |
| `kmp-clean-architecture` | The 6-layer contract applies to a library's own `:library` internals too, once it outgrows a single module — see Step 1 |

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-01 | Fixed a self-contradiction found the same day: this skill's own pre-1.0 policy section says `1.0.0` is a deliberate stability promise cut after real usage, but its `gradle.properties` example (and the official `multiplatform-library-template` we clone in Step 1) both defaulted to `1.0.0` for a brand-new library. Changed the example to `0.1.0-SNAPSHOT` and added an explicit instruction to override the template's hardcoded `1.0.0`. Same fix applied to `kmp-release`'s version example and `/kmp-new-project`'s Library F-01. |
| 2026-07-31 | Fixed a second real gap found right after the correction below: `build-logic/` was listed as "optional but recommended" in every structure diagram but never actually wired anywhere — no `includeBuild`, no convention plugin content, and the real official template doesn't ship one at all. It adds nothing for a single `:library` module (nothing to de-duplicate), so removed it from Step 1's default diagram entirely. It does earn its keep once Step 1a's multi-module split is in play (3+ modules needing the same `explicitApi()`/AGP/`apiCheck` config) — added the real `includeBuild("build-logic")` wiring and a convention plugin example there instead of asserting it as a default. |
| 2026-07-31 | **Self-correction, verified via GitHub API + raw source, not assumed**: this skill and `/kmp-new-project` both stated "there is no equivalent to kmp-wizard for a library" — wrong. `Kotlin/multiplatform-library-template` is a real, official, actively-maintained JetBrains repo (same org as `kmp-wizard`, "official project" badge, 332 stars) that scaffolds exactly this: one `:library` module with `vanniktech-mavenPublish`, the AGP 9 `com.android.kotlin.multiplatform.library` plugin, and `jvm()`/`androidLibrary()`/`iosArm64()`/`iosSimulatorArm64()`/`linuxX64()` already wired — the template's own README explicitly says it omits binary-compat tracking, `explicitApi()`, licensing, and a contribution guideline, which is exactly what this skill's Steps 2/5/12/13 already add on top. Rewrote Step 1 to clone it as the mandatory starting point instead of hand-building the structure, mirroring `kmp-wizard`'s own discipline for apps. Added a matching anti-pattern. |
| 2026-07-31 | Added four more real maintenance gaps from a follow-up survey: a pre-1.0 API stability policy (0.x may break without a major bump per SemVer 2.4; 1.0+ is where the stability promise starts — state it in the README, don't leave it implicit), Step 12 (NOTICE file for bundled/redistributed third-party assets — distinct from a normal Maven dependency a consumer resolves themselves; `heroicons-compose`'s own `NOTICE.md` is the real precedent), Step 13 (OSS contribution scaffolding — CONTRIBUTING/CODE_OF_CONDUCT/issue+PR templates, only once a library actually takes outside contributions), and a dependency-vulnerability-scanning subsection under Step 11 (Dependabot security alerts, distinct from the routine upgrade-cadence review already there). |
| 2026-07-31 | Added Step 1a — splitting into multiple published modules: real gap where this skill only ever scaffolded one `:library` module, with a BOM step that aligns multiple artifacts' versions but no guidance on how/when to actually create them. Covers the split decision (genuinely independent consumer surface, not just "big code"), one-way dependency direction (`-core` never depends on `-compose`/`-testing`), per-module `apiCheck`, and the `<PROJECT_NAME>-*` naming convention (never the literal word "library" — matches real published multi-module libraries like Coil's `coil-core`/`coil-compose`). Wired into `/kmp-new-project`'s Library F-01 as a confirm-first branch. |
| 2026-07-31 | Added Step 11 — ongoing maintenance: real gap where this skill covered shipping (publish, apiCheck, signing) but nothing about maintaining a published library afterward. Covers the deprecation cycle (`@Deprecated(WARNING)` → `ERROR` → removal, tied to SemVer), breaking-change communication (CHANGELOG entry + migration note before a major bump, never bundled silently), dependency upgrade cadence (Renovate/Dependabot scoped to the version catalog, sample pinned to the library's own versions), and keeping `sample/` from drifting (its own CI compile job — the only thing that actually compiles against the real public surface the way a consumer would). |
| 2026-07-31 | Added "`apiCheck` catches that the API changed, not whether the version bump matches" — real gap: `apiCheck` has no concept of semver, so nothing blocks tagging a breaking `.api` diff as a minor release. Also cross-referenced `kmp-clean-architecture`'s 6-layer contract for a library's own internal structure once `:library` outgrows a single module. 1 new anti-pattern, 1 new Related Skills row. |
| 2026-07-20 | Added "No forced framework coupling in library internals" (a library's own classes shouldn't hard-import Koin — ship it as a separate optional artifact instead) and "KDoc coverage on the public API surface", the second wired to `kmp-audit`'s new `_detect_undocumented_public_api`. Real gaps from a library-vs-app rules discussion. 2 new anti-pattern rows, 2 new Related Skills. |
| 2026-07-20 | Added `explicitApi()` — real gap found in a library-vs-app rules survey: this skill covered binary compatibility, signing, and publishing channels but never the compiler mode that catches an accidental public API leak *before* it ships (as opposed to `apiCheck`, which only catches the *next* change to an already-public surface). Explicitly scoped to library code only — app code has no external consumers to protect and gains nothing from the ceremony. 1 new anti-pattern. |
| 2026-07-11 | Cross-referenced two new skills: `kmp-project-docs-maintainer`'s new `docs/libraries.md` catalog page (release checklist should point there instead of nowhere), and `kmp-docs-site` (public GitHub Pages developer guide, reuses this skill's Dokka setup for a separate HTML output, distinct from the Javadoc jar). |
| 2026-07-09 | Added a "Per-file license headers (optional)" section — Detekt's `AbsentOrWrongFileLicense` rule (off by default) with a license template, and why this is worth it for a library (files get vendored/copy-pasted independently of the repo) but not for app code. New anti-pattern: per-file header must stay consistent with the POM's declared license. |
| 2026-06-29 | Initial skill — vanniktech plugin, BOM, binary-compat-validator, SNAPSHOT/stable, GPG, GitHub Packages |
