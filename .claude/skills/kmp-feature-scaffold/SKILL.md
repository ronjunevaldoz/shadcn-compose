---
name: kmp-feature-scaffold
description: >
  Scaffolds a production-ready Kotlin Multiplatform (KMP) multi-feature module
  architecture. Creates a full project by generating from the official Kotlin/kmp-wizard
  AGP 9 baseline, usually the `all-targets` branch for Android, iOS, Web, Desktop, and
  Server, or adds a new feature module group (:model/:api/:domain/:data/:presenter/:ui) to an existing
  KMP project. Uses AGP 9+, build-logic convention plugins, a TOML version catalog
  (`gradle/libs.versions.toml`), Compose Multiplatform, and Koin 4 (annotated or manual DI).
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - Kotlin Multiplatform
    - KMP
    - KMP
    - multi-module
    - feature module
    - AGP 9
    - build-logic
    - convention plugins
    - Koin 4
    - Compose Multiplatform
    - CMP
    - version catalog
---

## When to Use This Skill

Use when you need to:
- Create a new Kotlin Multiplatform project from scratch, starting from Kotlin/kmp-wizard
  (usually the `all-targets` branch when you want Android + iOS + Web + Desktop + Server)
- Add a new feature module group (`:model/:api/:domain/:data/:presenter/:ui`) to an existing KMP project
- Set up AGP 9+ build-logic convention plugins and a version catalog
- Set up AGP 9+ build-logic convention plugins backed by `gradle/libs.versions.toml`
- Wire Koin 4 DI (annotated or manual) across KMP modules

**This is the foundational skill** — most other KMP skills (`network-layer`, `sqldelight-setup`,
`navigation`, `design-system`, etc.) require the project structure this skill creates.

**Trigger keywords:** create KMP project, scaffold feature module, new module, set up KMP,
add feature, multi-module, build-logic, convention plugin, AGP 9, Koin 4, KMP setup,
Kotlin/kmp-wizard, generate from template, baseline project,
add a screen, new screen, new feature, new feature module, add feature layer,
scaffold module, create module, add KMP screen, set up convention plugin.

**Branch recommendation:** default to the `all-targets` branch for full-stack KMP apps.
Use `all-frontends-shared` only when you want Android + iOS + Web + Desktop without a
server module.

**Build-logic rule:** always route module configuration through convention plugins in
`build-logic/` and keep versions in `gradle/libs.versions.toml`; do not scatter plugin
and dependency versions across module build files.

**Freshness rule:** AGP, Kotlin, CMP, and Koin version targets change quickly — recheck the
version table in `PLAN.md` and the kmp-wizard repo before scaffolding a new project.

---

## Recommendation First

Default to **kmp-wizard `all-targets` branch + build-logic convention plugins + `gradle/libs.versions.toml`**.

Why:
- `all-targets` gives Android + iOS + Web + Desktop + Server in one baseline — easier to trim
  than to add targets later
- convention plugins enforce consistent AGP/Kotlin configuration across every module
- a single version catalog eliminates version drift between modules

Use a narrower branch (`all-frontends-shared`) only when the product explicitly excludes server.
Never scaffold by hand — always start from kmp-wizard to avoid missing targets or misconfigured plugins.

---

## Overview

This skill produces a KMP multi-feature module architecture with the following decisions
baked in:

- **AGP 9 minimum** using the new `com.android.kotlin.multiplatform.library` plugin
  (replaces the old `kotlin("multiplatform")` + `com.android.library` pair for library modules)
- **build-logic** as a Gradle included build providing precompiled convention plugins
- **Version catalog** (`gradle/libs.versions.toml`) with proper group prefixes and bundles
- **Feature split**: thin (`:ui`), medium (`:presenter`+`:ui`), or full (all 6) — chosen in Step 0
- **Core modules**: `:core:common`, `:core:network`, `:core:database`, `:core:ui`
- **Compose Multiplatform (CMP)** as the default shared UI layer (CMP-first)
- **Koin 4** DI — annotated (default, via Koin Compiler Plugin) or manual

### Module dependency graph (per feature)

```
:feature:<name>:model      pure KMP — data classes, sealed types, enums (no deps)
        ↑
:feature:<name>:api        pure KMP — interfaces, nav contracts (depends on :model)
        ↑
:feature:<name>:domain     pure KMP — use cases, business logic (depends on :api)
        ↑
:feature:<name>:data       KMP + platform impls — Ktor, SQLDelight (depends on :api, NOT :domain)
:feature:<name>:presenter  pure KMP — ViewModels, MVI contracts (depends on :domain, NO Compose)
        ↑
:feature:<name>:ui         CMP — Compose screens + previews (depends on :presenter ONLY)
```

`:data` and `:presenter` are siblings — neither depends on the other.
`:presenter` has NO Compose dependency, so ViewModels are testable on plain JVM.

---

## Mode Detection

Before doing anything, inspect the working directory:

- **New Project mode**: no `settings.gradle.kts` or no `build-logic/` directory found.
  Scaffold the full project by copying the Kotlin/kmp-wizard AGP 9 `all-targets`
  baseline first, then layer the multi-feature module architecture on top.
- **Add Feature mode**: existing KMP project detected (has `settings.gradle.kts` and
  `build-logic/`). Only scaffold the new feature module group.

---

## Step 0: Decide Layer Depth Before Scaffolding

**Ask this before generating any modules.** The 6-layer structure is the maximum — not
the default. Scaffold only the layers the feature actually needs.

Ask the user (or infer from context):

| Question | Yes → add this layer |
|---|---|
| Does the feature load or write data from a server or database? | `:data` |
| Does it apply business rules that must be tested without a ViewModel? | `:domain` |
| Does it have user interactions and/or navigation effects? | `:presenter` (MVI) |
| Does it display a screen in Compose? | `:ui` |
| Does it define types shared across the above layers? | `:model` + `:api` |

**Three tiers:**

| Tier | Modules | When to use |
|---|---|---|
| **Thin** | `:ui` only | Static display screen, no async, no ViewModel needed |
| **Medium** | `:presenter` + `:ui` | Async load + navigation, no business logic to isolate |
| **Full** | `:model` + `:api` + `:domain` + `:data` + `:presenter` + `:ui` | CRUD, offline-first, business rules, or cross-feature shared types |

Default to **Medium** for most product features. Upgrade to Full when `:data` complexity
or cross-feature type sharing justifies it. Use Thin only for standalone utility screens.

Do not scaffold unused layers "in case they're needed later" — empty modules add Gradle
configuration overhead and signal to the team that something should be there.

---

## Step 1: Gather User Input

**Always ask before creating any files.** Collect these values from the user:

| Input | Description | Example |
|---|---|---|
| `PROJECT_NAME` | Root project name (PascalCase) | `MyAwesomeApp` |
| `GROUP_ID` | Base package / Maven group ID | `com.example.myapp` |
| `FEATURE_NAME` | First feature to scaffold (snake_case) | `auth` |
| `TIER` | `thin` / `medium` / `full` (from Step 0) | `full` |
| `DI_APPROACH` | `annotated` (default) or `manual` | `annotated` |

In **Add Feature mode**, only `GROUP_ID`, `FEATURE_NAME`, `TIER`, and `DI_APPROACH` are needed.

---

## Step 2: Version Reference

Use these exact versions. Do not substitute without explicit user confirmation.

```toml
agp                   = "9.2.0"
kotlin                = "2.4.0"
ksp                   = "2.4.0-2.0.0"
koin                  = "4.2.2"
koin-annotations      = "2.3.1"
ktor                  = "3.5.0"
sqldelight            = "2.3.2"
compose-multiplatform = "1.11.1"
buildkonfig           = "0.22.0"
android-compileSdk    = "36"
android-minSdk        = "24"
android-targetSdk     = "36"
androidx-lifecycle    = "2.11.0"
androidx-activity     = "1.13.0"
coroutines            = "1.11.0"
serialization         = "1.11.0"
datetime              = "0.8.0"
```

> **Note on Koin DI**: Koin 4.1+ ships a native Kotlin Compiler Plugin
> (`org.jetbrains.kotlin.plugin.koin`) that replaces the KSP-based annotation processor
> for KMP projects — no per-platform KSP configuration needed. Use this for `annotated`
> mode. For `manual` mode, skip the plugin entirely and write explicit `module {}` blocks.

> **Note on BuildKonfig**: `com.codingfeline.buildkonfig` is the KMP equivalent of
> Android's `BuildConfig`. It generates a `BuildKonfig` object accessible from
> `commonMain`, `androidMain`, and `iosMain`. Configure it in `:app:androidApp`'s
> `build.gradle.kts` using a `buildkonfig {}` block.

---

## App Versioning

Full content: `references/app-versioning.md`.

## Step 3: New Project — Clone kmp-wizard (MANDATORY)

Full content: `references/step3-clone-kmp-wizard.md`.

## The `:app:*` Module Boundary

Nothing new is ever nested directly under `:app:*`. The only modules there are the four
kmp-wizard already created — `androidApp`, `desktopApp`, `webApp`, `shared` — plus the
native (non-Gradle) `app/iosApp/` Xcode project. A new feature, or a new piece of shared
infrastructure, never gets its own `:app:<name>` module: it goes in `:feature:<name>:*`
(business logic) or `:core:*` (cross-feature infrastructure). `:app:shared` itself stays
a thin composition root — if it starts accumulating anything beyond theme/DI/nav wiring,
that's a signal the content belongs in `:feature:*` or `:core:*` instead.

`kmp-audit`'s `unauthorized app submodule` check enforces this
mechanically — a `build.gradle.kts` under `app/<name>/` where `<name>` isn't one of the
four known entry points is a finding, not a judgment call.

---

## Step 4: Extend build-logic with KMP Convention Plugins

Full content: `references/step4-build-logic.md`.

## Step 5: Convention Plugin Templates

Full content: `references/step5-convention-plugin-templates.md`.

## Step 6: Feature Module `build.gradle.kts` Templates

Full content: `references/step6-feature-module-templates.md`.

## Step 7: Koin DI Patterns

Full content: `references/step7-koin-di-patterns.md`.

## Step 8: Add Feature Mode

When adding a feature to an existing project:

1. Create the six module directories:
   ```
   feature/<FEATURE_NAME>/model/
   feature/<FEATURE_NAME>/api/
   feature/<FEATURE_NAME>/domain/
   feature/<FEATURE_NAME>/data/
   feature/<FEATURE_NAME>/presenter/
   feature/<FEATURE_NAME>/ui/
   ```
2. Write `build.gradle.kts` in each (see Step 6 templates above).
3. Add to `settings.gradle.kts`:
   ```kotlin
   include(":feature:FEATURE_NAME:model")
   include(":feature:FEATURE_NAME:api")
   include(":feature:FEATURE_NAME:domain")
   include(":feature:FEATURE_NAME:data")
   include(":feature:FEATURE_NAME:presenter")
   include(":feature:FEATURE_NAME:ui")
   ```
4. Wire into `:app:androidApp` dependencies:
   ```kotlin
   implementation(projects.feature.FEATURE_NAME.ui)
   ```
5. Add a preview stub beside each `*Content.kt` in `:feature:FEATURE_NAME:ui` so preview
   coverage is part of the scaffold, not an optional follow-up.

---

## Step 9: Source File Stubs

Full content: `references/step9-source-file-stubs.md`.

## Step 10: Test Infrastructure

### Convention plugin: `GROUP_ID.feature.test.gradle.kts`

A lightweight plugin that equips any module's test source sets with shared test tooling.
Apply it to modules that need Turbine, coroutines-test, or shared fakes.

```kotlin
// In any module's build.gradle.kts test configuration
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)  // shared fakes + builders
        }
    }
}
```

### `:core:testing` module

Add to `settings.gradle.kts`:
```kotlin
include(":core:testing")
```

The module exposes (via `api()`):
- `kotlin.test` — assertions
- `kotlinx.coroutines.test` — `runTest`, `TestCoroutineScheduler`
- `Turbine 1.2.1` — Flow testing

## Bundled Script

Full content: `references/bundled-script.md`.

## Step 11: Verification

After scaffolding, verify in order:

1. `./gradlew help` — Gradle resolves the build without errors
2. `./gradlew :feature:FEATURE_NAME:api:compileKotlinMetadata` — KMP common compiles
3. `./gradlew :app:androidApp:assembleDebug --dry-run` — Android wiring is correct
4. Confirm all `include()` entries in `settings.gradle.kts` match actual directories
5. Confirm no module references another module that it should not (enforce the layer rules:
   `:ui` depends only on `:presenter`; `:presenter` has NO Compose dep; `:domain` must not depend on `:data`;
   `:data` must not depend on `:domain` or `:presenter`)
6. Confirm every `*Content.kt` in `:feature:FEATURE_NAME:ui` has a matching preview stub
   (`*ContentPreview.kt` or `previews/*ContentPreview.kt`)

---

## Guidelines

- Never create a `buildSrc/` directory — use `build-logic` instead
- Never use `id("kotlin-android")` — use `id("org.jetbrains.kotlin.android")` (AGP 9 requirement)
- Never add `android.builtInKotlin` or `android.newDsl` to `gradle.properties` — these are AGP 9 defaults
- Always use `androidLibrary {}` inside `kotlin {}` for library modules, not a standalone `android {}` block
- Always use TYPESAFE_PROJECT_ACCESSORS (`projects.feature.auth.api`) — never string-based `:feature:auth:api`
- Keep `:api` modules minimal — no DI framework dependencies, no platform deps
- Namespace format: `GROUP_ID.module.path` (e.g. `com.example.app.feature.auth.api`)

## Bootstrap / CLI Refactor Guardrails

Treat project bootstrap and command entrypoints as orchestration only.

- `main()` / `App.kt` / CLI runner files should parse input, set up the pipeline, and dispatch
  to helpers; they should not accumulate mode-specific business logic
- when a bootstrap file starts growing multiple workflow branches, split each mode into its
  own helper module or file instead of adding more inline branching
- keep helper names file-local unless the helper is genuinely shared across commands or bootstraps
- move long operational notes, design rationale, and refactor history into repo docs or skill docs,
  not into bootstrap code comments
- during structural refactors, compile after each extraction so the next move starts from a working tree

**Anti-patterns:**
- turning the bootstrap into a dispatcher-plus-helper-monolith
- sharing one-letter helper names across multiple modes in the same package
- deferring compilation until the end of a large structural extraction
- leaving bootstrap comments as the only source of architectural intent

---

## Related Skills

- `docs/reference/compatibility-matrix.md` — compatibility table and conflict zones for all versions declared in this skill's version catalog
- `kmp-dependency-injection` — wire Koin after the module structure is in place
- `kmp-navigation` — add type-safe navigation after scaffold is complete
- `kmp-mvi` — screen architecture layer built on top of this scaffold
- `kmp-flavor-environment` — add dev/staging/prod environments after scaffolding
- `kmp-ci-github-actions` — CI workflow consumes the module structure this skill creates
- `kmp-android-cli` — build/deploy/emulator management for this scaffold's Android target from the terminal, once the module structure exists
- `kmp-code-quality` — file/type/function/constant naming conventions for everything scaffolded here

---

## Common Anti-Patterns

- scattering plugin versions across module `build.gradle.kts` files instead of `libs.versions.toml` — causes version drift
- skipping `build-logic` convention plugins for "simple" modules — they accumulate inconsistency over time
- adding `implementation` dependencies in `:api` modules — `:api` must stay dependency-free (only `:model`)
- adding Compose deps to `:presenter` — breaks JVM testability; Compose belongs only in `:ui`
- having `:ui` depend on `:domain` or `:data` directly — all state must flow through `:presenter`
- shipping a `:feature:*:ui` module with `*Content.kt` but no preview stub — preview coverage must be scaffolded, not added later
- putting domain types (data classes, sealed types) in `:api` instead of `:model` — `:api` should be interfaces only
- using string project references (`:feature:auth:api`) instead of typesafe accessors — breaks refactoring
- **scaffolding by hand instead of cloning kmp-wizard** — always use `git clone Kotlin/kmp-wizard` as the base; writing build-logic, convention plugins, or settings.gradle.kts from scratch causes broken Gradle included builds, missing platform targets, and cascading precompiled script plugin failures that are very hard to debug
- using precompiled `.gradle.kts` script plugins for convention plugins in included builds — Gradle 9 does not generate version catalog type-safe accessors for included builds; always use class-based `Plugin<Project>` instead
- pre-creating empty `src/androidMain/kotlin/`, `src/iosMain/kotlin/`, `src/jvmMain/kotlin/`, etc. directories "just in case" a module might need platform code later — Gradle compiles a target fine with zero files in its source set; an empty platform directory (or one containing only a package-declaration stub) is pure clutter and signals unclear architecture intent. Declare the compile targets in the convention plugin (`androidLibrary {}`, `iosArm64()`, ...) as usual — that's required for per-platform artifacts — but only create the physical source directory and write into it when there is real `expect`/`actual` code to place there
- letting `main()` / bootstrap files accumulate mode-specific logic instead of dispatching to helpers — keep entrypoints thin and orchestration-only, and extract a new helper file when a branch becomes a workflow
- waiting until the end of a refactor to compile — compile after each structural extraction so failures point to the move you just made

If a module is failing to compile on one target, check whether the convention plugin was applied and the source sets declared correctly.

---

## Output Style

When asked to scaffold a project or add a feature module, respond in this order:
1. clarify the target (new project vs new feature module in existing project)
2. version reference (confirm current AGP / Kotlin / CMP targets from PLAN.md)
3. directory structure
4. key file contents (build-logic convention plugin, module build file, settings)
5. wire-up step (Koin module registration, nav graph entry)

Ask for GROUP_ID and feature name before generating files. Map all paths to the actual values.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split SKILL.md (1266 lines) into 8 `references/*.md` files (App Versioning, Steps 3/4/5/6/7/9, Bundled Script), leaving pointer stubs under each heading. SKILL.md drops to 420 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Fixed `scripts/check_compat_matrix.py`, which only scanned `SKILL.md` text — the `roborazzi` version pin moved into `references/step4-build-logic.md` and the drift check went blind to it; it now concatenates `references/*.md` too, same fix already applied to `kmp-audit`'s design-system checks. Part of the same backlog cleanup as `kmp-compose-design-system`/`-extended`/`kmp-mvi` (KI-008). |
| 2026-08-04 | Added `kmp-code-quality` to Related Skills — naming conventions (file/type/function/constant casing) existed but only `kmp-mvi` cross-referenced them, so an agent scaffolding new modules here had no route to them. |
| 2026-07-31 | Fixed real drift: this skill (and `/kmp-new-project`) referenced a bare `:androidApp` module and, in one place, a nonexistent `:composeApp` — verified against the live `Kotlin/kmp-wizard` `all-targets` template and found the real paths are `:app:androidApp`/`:app:desktopApp`/`:app:webApp`/`:app:shared`, plus a native (non-Gradle) `app/iosApp/` Xcode project. Fixed all 5 occurrences. Added Step 3d: kmp-wizard's default `:core` ships as one bare module (the exact `bare core module [HIGH]` anti-pattern this repo's own audit flags) and `:app:shared` ships with unused demo content — both must be fixed before Step 4, not scaffolded on top of. Added "The `:app:*` Module Boundary" section and a new `_detect_unauthorized_app_submodule` audit check enforcing that only kmp-wizard's own four entry points ever live under `app/`. 3 new regression tests. |
| 2026-07-19 | Cross-referenced `kmp-android-cli` in Related Skills — build/deploy/emulator management for this scaffold's Android target, surfaced whenever project-foundation work already triggers this skill instead of requiring the literal "android cli" phrase. |
| 2026-07-05 | Added anti-pattern against pre-creating empty platform source directories (`androidMain`, `iosMain`, `jvmMain`, ...) "just in case" — a real recurring smell reported from field experience. New audit detector `empty platform source set [LOW]` in `kmp-audit` catches directories with zero `.kt` files or files containing only package/import/comments. Declaring the compile target is still required and correct; only the physical directory should be created on-demand, when there's real expect/actual code to write. |
| 2026-07-09 | Added bootstrap / CLI refactor guardrails: keep entrypoints orchestration-only, split workflow modes into dedicated helpers, prefer file-local helper names, move long notes out of bootstrap code, and compile after each extraction. |
| 2026-06-21 | **Improved** — App versioning pattern defined: `VERSION_NAME`/`VERSION_CODE` in `gradle.properties` as the single source of truth; `androidApp` convention plugin reads from properties; `BuildKonfig` exposes `APP_VERSION` to `commonMain`; CI bump pattern documented. |
| 2026-06-21 | **Breaking** — Step 3 rewritten: `git clone Kotlin/kmp-wizard` is now mandatory. Hand-scaffolding `build-logic`, convention plugins, or `settings.gradle.kts` from scratch is no longer supported. |
| 2026-06-21 | **Breaking** — Step 4 rewritten: convention plugins must be class-based `Plugin<Project>`. Precompiled `.gradle.kts` script plugins in included builds do not generate version catalog accessors in Gradle 9. |
| 2026-06-18 | 6-layer module structure enforced; `jvm()` target added to all convention plugin templates; Step 9 source stubs expanded. |
