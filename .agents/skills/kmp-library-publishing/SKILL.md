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

Full content: `references/step1-library-project-structure.md`.

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

Full content: `references/step3-library-module-build-gradle.md`.

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

Full content: `references/step5-binary-compat-validator.md`.

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

Full content: `references/step11-ongoing-maintenance.md`.

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

## References

Full implementation content lives in `references/*.md`: `step1-library-project-structure`,
`step3-library-module-build-gradle`, `step5-binary-compat-validator`,
`step11-ongoing-maintenance`. Load the specific file named in the pointer under its
matching heading above, not all of them.

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
| 2026-08-04 | Added a "`core`/`helper`/`sugar` — higher stakes here than in an app" section to Step 11 (Ongoing maintenance) — cross-references `kmp-code-quality`'s new core/helper/sugar/sample-local/deprecated categorization and maps it to this skill's own mechanisms: `core`/`sugar` are both binary-compat surface tracked by `apiCheck`/`apiDump`, `helper` is compiler-enforced via `explicitApi()`, `sample-local` is the existing `sample/` module guidance, `deprecated` is the existing cycle below it. |
| 2026-08-04 | Split SKILL.md (972 lines) into 4 `references/*.md` files (Step 1 Library project structure, Step 3 build.gradle.kts, Step 5 Binary compat validator, Step 11 Ongoing maintenance), leaving pointer stubs plus a new References section. SKILL.md drops to 482 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as `kmp-compose-design-system`/`-extended`/`kmp-mvi`/`kmp-feature-scaffold`/`kmp-code-quality` (KI-008). |
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
