---
name: kmp-expert
description: >
  KMP Expert Orchestrator — maps all skills in this collection, their dependency
  order, and how to sequence them for any Kotlin Multiplatform project. Use this skill
  first to decide which other skill to invoke, in what order, for a given task. Covers:
  skill dependency graph, layer-by-layer build order, feature-slice assembly sequence,
  decision trees for the most common "what do I use here?" questions, and when to hand
  off to the project audit skill. This is a meta-skill; it delegates to domain skills
  for implementation and review, and it can turn confirmed audit findings into issue
  drafts or question drafts when the repo needs tracking. The long-term goal is to keep
  this skills collection aligned with the cleanest KMP architecture patterns possible.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - KMP expert
    - orchestrator
    - skill sequencing
    - dependency graph
    - project setup order
    - KMP architecture
    - Kotlin Multiplatform expert
    - what skill should I use
    - skill map
    - meta-skill
    - feature assembly
    - KMP decision tree
    - audit
    - project review
    - architecture review
    - issue draft
    - question draft
    - kmp-agent-skills
    - kmp-skills
    - KMP agent skills
    - skill collection
    - skills index
---

## When to Use This Skill

Use this skill when you need to:
- Start a new KMP project and don't know which skills to invoke or in what order
- Start a new KMP project from the Kotlin/kmp-wizard baseline and don't know which
  skills to invoke or in what order
- Add a new full feature to an existing KMP project (network + DB + UI + navigation)
- Decide which skill answers a specific question ("where do I put this?", "which pattern fits?")
- Route an existing KMP project into the audit skill before making changes
- Convert confirmed audit findings into GitHub issue drafts or question drafts before
  fixing if the user wants repo tracking
- Get a high-level roadmap before diving into implementation

**Do NOT use this skill, or route to any skill in this collection, when the request
names a different stack** (React, Swift/SwiftUI native-only, Flutter, plain Android
Views, a backend framework unrelated to Ktor/kRPC, etc.) or no stack at all with no
Kotlin/KMP signal anywhere in the request. This collection is Kotlin Multiplatform +
Compose Multiplatform + Koin + Ktor specific — nothing here applies to "build a React
tic-tac-toe app" or similarly unrelated requests, even if the task shape sounds generic
(a UI component, a state machine, a build pipeline). If the stack is genuinely
ambiguous, ask which stack before assuming KMP.

**Branch recommendation:** use `Kotlin/kmp-wizard` `all-targets` by default for new
full-stack KMP projects. Use `all-frontends-shared` only if you want to omit the server.

**Build-logic rule:** route plugin and dependency versions through `build-logic/`
convention plugins and `gradle/libs.versions.toml`; do not scatter version strings
across module build files when creating or updating KMP projects.

**Trigger keywords:** where do I start KMP, full KMP setup, new KMP feature, which skill,
skill order, KMP architecture decision, KMP expert, KMP project plan, which pattern KMP,
KMP checklist, review my KMP project, custom agent for project, custom command for project,
project-specific agent, project-specific command, project-specific skill.

**Freshness rule:** recheck the Skill Invocation Map and dependency graph entries whenever
a new skill is added or removed — the routing table and skill count must stay in sync with
the actual `skills/` directories. Run `python3 skills/kmp-expert/scripts/validate_skill_map.py --repo-root .`
after any skill addition.

---

## Recommendation First

Default to **reading the current skill list and dependency graph before recommending anything**.

Why:
- the skill collection grows; a recommendation based on a stale skill list misroutes work
- the dependency graph in this skill defines the correct build order — skipping foundation skills
  causes downstream failures
- routing to the wrong skill wastes a context window on the wrong patterns

Use this skill as an entry point for open-ended KMP questions. Then hand off to the specific skill.
Do not implement — route and explain.

## Priority Ladder

When more than one skill could apply, rank them instead of enabling everything.

1. Contract and scaffold skills first: `clean-architecture`, `feature-scaffold`, `presenter-module`
2. Foundation and project plumbing next: `dependency-injection`, `flavor-environment`, `ci-github-actions`, `logging`
3. Core infrastructure after the foundation is clear: `network-layer`, `sqldelight-setup`, `datastore`, `logging`, `kotlin-rpc`, `ktor-auth-service`, `mongodb-database`, `xcframework-spm`
4. Feature building blocks after the data and platform shape is known: `navigation`, `mvi`, `repository-pattern`, `shared-resources`, `paging`, `analytics`, `form-validation`, `image-loading`, `permissions`, `deep-linking`, `biometric-auth`, `push-notifications`, `workmanager`, `feature-flags`, `crash-reporting`
5. UI, testing, quality, docs, and release last: `design-system`, `design-system-extended`, `adaptive-layout`, `compose-*`, `preview-driven-development`, `unit-testing`, `roborazzi`, `code-quality`, `accessibility`, `project-docs-maintainer`, `audit`, `release`

Load the earliest tier that answers the request, then add lower tiers only when the task genuinely needs them.

## Model Routing

Route subagents by work type, not by habit.

Use the strongest available reasoning model for:
- ambiguous planning
- complex architecture decisions
- performance investigations
- benchmark-topping work
- root-cause analysis on hard failures
- final review of claims, numbers, and tradeoffs

Use a cheaper or faster model for:
- mechanical implementation after the plan is clear
- repetitive file generation
- straightforward wiring
- bulk edits with no design decision

Use a precision-focused strong model for:
- validation
- review
- anything where an incorrect conclusion would be expensive to unwind

If a task is both complex and high-impact, escalate the planning and review stages first;
keep the implementation stage on the smallest model that can still follow the plan cleanly.

---

## Required vs Optional Skills

Classify every skill into one of five bands before recommending. Always cover the **Required core** first; pull in lower bands only when the task or the app's capabilities demand them.

### Required core (every KMP feature)
These implement the architecture contract — no proper feature ships without them.

| Skill | Why required |
|---|---|
| `clean-architecture` | The 6-layer contract — the rules everything else obeys |
| `feature-scaffold` | Module structure, build-logic, version catalog |
| `presenter-module` | Every feature has a no-Compose, JVM-testable ViewModel |
| `mvi` | The Screen/Content state pattern for every screen |
| `dependency-injection` | Koin wiring spans every layer |

### Conditionally required (depends on app capability)
Required **if** the app has that capability — most production apps do.

| Skill | Required when… |
|---|---|
| `network-layer` | App calls any backend/API |
| `sqldelight-setup` **or** `datastore` | App persists data (DB vs key-value) |
| `repository-pattern` | App has both network and local storage |
| `navigation` | App has more than one screen |
| `design-system` | App renders any custom UI |
| `shared-resources` | App needs localization / strings / assets |
| `expect-actual` | App needs platform-specific code |
| `xcframework-spm` | Shipping a shared framework to an iOS team |

### Strongly recommended (project health)
Optional in theory; skipping them costs quality and velocity.

`flavor-environment`, `ci-github-actions`, `code-quality`, `logging`, `unit-testing`, `preview-driven-development`

### Optional (feature-specific)
Pull in only when a feature explicitly needs it.

`design-system-extended`, `adaptive-layout`, `compose-slot-api`, `compose-state-hoisting`, `compose-state-container`, `compose-animation`, `graphics-modifiers`, `roborazzi`, `accessibility`, `paging`, `analytics`, `form-validation`, `image-loading`, `permissions`, `deep-linking`, `biometric-auth`, `push-notifications`, `workmanager`, `feature-flags`, `crash-reporting`, `ktor-auth-service`, `mongodb-database`, `kotlin-rpc`, `legal-docs`, `release`

### Opt-in (never auto-select — must be named explicitly)
- `offline-first` — only when the user names "offline-first", "background sync", or "conflict resolution". For plain caching or a local source of truth, use `repository-pattern` + `sqldelight-setup` instead. Offline-first layers `SyncManager` + `WorkManager`/`BGTaskScheduler` on top, which is overkill unless explicitly wanted.

### Meta (tooling, not app code)
`expert` (routing), `audit` (review), `kmp-jni-pro` (native bridge), `docs-maintainer`, `changelog`, `benchmark` (invoked on-demand for a specific performance claim — never scaffolded speculatively), `docs-site` (public developer guide — library-only, gated on real surface area, never scaffolded for an app or a trivial library)

---

## Routing Precedence

When a request could fit more than one surface, use this order:

1. Repo README, `docs/`, agent docs, command docs, or routing text -> `docs-maintainer`
2. Downstream project README, onboarding docs, or reference docs -> `project-docs-maintainer`
3. Wireframes, screen flows, layout specs, design handoff, or component API direction -> `designer`
4. Consumer release notes, `CHANGELOG.md`, or per-skill changelog tables -> `changelog`
5. Project release, versioning, or publishing flow -> `release`
6. Navigation structure -> `navigation`; external URL handling -> `deep-linking`

If the request still spans two surfaces after that, route the earlier-layer owner first and name the follow-up skill explicitly.

### Docs Scope Guard

Before routing docs work, classify the target:
- repo-internal docs, agents, commands, or routing text -> `docs-maintainer`
- downstream consumer docs -> `project-docs-maintainer`

If the user has not said which one they mean, resolve the scope before editing.

---

## Freshness Rule

At the start of every session, treat the repo files in front of you as the source of
truth. Re-read the current `README.md` and the relevant `skills/*/SKILL.md` files before
recommending an approach. Do not rely on a previous session's skill list or remembered
versions when the local repo can be checked directly.

---

## The 68 Skills and What They Own

### Layer 0 — Architecture Contract
| Skill | Owns |
|---|---|
| `kmp-clean-architecture` | 6-layer dependency contract, `:model` vs `:api` split, `internal` visibility rules, Detekt architecture enforcement |
| `kmp-feature-scaffold` | Project structure, 6-layer module graph, AGP 9, build-logic, version catalog, Koin 4 |
| `kmp-presenter-module` | Pure-Kotlin ViewModel, MVI `UiState`/`UiIntent` contracts, no Compose dep, Koin wiring, Screen/Content split |

### Layer 1 — Project Foundation
| Skill | Owns |
|---|---|
| `kmp-dependency-injection` | Koin module organization, manual vs annotated wiring, app/feature/ViewModel scopes, test overrides |
| `kmp-flavor-environment` | Dev/staging/prod config, BuildKonfig, secrets, `AppConfig` facade |
| `kmp-ci-github-actions` | GitHub Actions, test matrix, XCFramework release workflow YAML |
| `kmp-android-cli` | Google's `android` CLI — agent-first project scaffolding, emulator/device management, build + deploy, SDK installs; `android init`/`android skills add` agent bootstrap |
| `kmp-release` | Versioning (`gradle.properties`), Maven Central (vanniktech), GPG signing, git-cliff changelog, GitHub Release, secrets management, local publish script |
| `kmp-audit` | Existing project health checks, boundary review, architecture drift, readiness gaps; `--roadmap` for adoption plan |
| `kmp-migration` | Incremental adoption guide: assess current state, prioritized skill adoption order, MVVM→MVI, monolith→multi-module, Hilt→Koin migration paths |
| `kmp-refactor` | Rename/move/copy/delete: textual sweep (docs/skills/commands) vs IDE-native refactor (Kotlin symbols), module-move checklist against the 6-layer contract, safe-delete dangling-reference check |
| `kmp-project-docs-maintainer` | Consumer-facing README, onboarding, and docs/reference sync for downstream KMP projects |
| `kmp-layout-system` | ASCII wireframe docs for screens — draft and document app layout before or after implementation; lives in `docs/layout-system/` |
| `kmp-lessons` | Structured lesson files capturing pattern mismatches and fixes; feeds the skill-harvester |
| `kmp-skill-harvester` | Reads accumulated lesson files and proposes amendments to source skills; produces a harvest report |
| `kmp-legal-docs` | Privacy Policy, Terms & Conditions, Google Play Data Safety, App Store privacy labels, GDPR/CCPA, in-app `LegalDocsScreen`, consent gate |
| `kmp-proguard-r8` | R8 minification for KMP Android release builds: keep rules per library (Koin, Ktor, SQLDelight, serialization), release crash diagnosis, mapping.txt management |
| `kmp-in-app-purchases` | IAP and subscriptions: shared `PurchaseState` domain model, Play Billing (Android) and StoreKit 2 (iOS) implementations, MVI ViewModel integration, server-side validation |
| `kmp-desktop-app` | Desktop-specific: window management, system tray, file picker, native menu bar, keyboard shortcuts, drag-and-drop, JPackage packaging (dmg/msi/deb) |

### Layer 2 — Core Infrastructure
| Skill | Owns |
|---|---|
| `kmp-ktor-auth-service` | Ktor auth service, bearer/JWT, sessions, Ktor RPC, login/refresh/logout flows, protected routes |
| `kmp-mongodb-database` | MongoDB coroutine driver, repository boundary, document mapping, reactive reads with Flow, change streams |
| `kmp-kotlin-rpc` | Kotlin RPC boundaries, shared service contracts, client/server layout, Ktor auth integration |
| `kmp-network-layer` | Ktor 3 client, `NetworkResult<T>`, `safeRequest {}`, token refresh interceptor |
| `kmp-sqldelight-setup` | SQLDelight 2, platform drivers, schema files, migrations, Flow queries |
| `kmp-datastore` | Preferences DataStore + Proto DataStore, expect/actual factory, Koin wiring, SharedPreferences migration |
| `kmp-xcframework-spm` | XCFramework build, SPM binary target, Xcode integration |
| `kmp-library-publishing` | Maven Central publishing (vanniktech plugin), GitHub Packages, BOM, binary-compatibility-validator, SNAPSHOT vs stable channels, GPG signing, release checklist |
| `kmp-docs-site` | GitHub Pages developer guide for a published library — MkDocs Material, Dokka HTML API reference, compiler-verified snippet extraction, release-tag-triggered CI deploy |
| `kmp-api-mimicry` | Mimicking a reference API's shape (Modifier-style chains, slot lambdas, DSL markers) for a from-scratch library on a non-standard runtime (custom native renderer, custom transport) — plain-function DSL vs. real-compiler-plugin decision, mirror-map documentation |
| `kmp-logging` | logger wrapper, kotlin-logging or Kermit, log levels, logger factory, crash breadcrumb bridge, Koin wiring |

### Layer 3 — Platform Patterns
| Skill | Owns |
|---|---|
| `kmp-expect-actual` | `expect/actual` mechanism, interface-injection alternative, `@ObjCName`, Kotlin/Native memory |
| `kmp-repository-pattern` | Data layer, single source of truth, fetch strategies, domain mapping, optimistic updates |
| `kmp-jni-pro` | JVM↔C++ JNI bridges (`JNIEnv`, `Java_*`, `GetStringUTFChars`, `*-jni.cpp`/`*-wrapper.cpp`), memory safety across the JVM boundary, 3rd-party C++ as read-only black box + C-shim wrapping, symbol-conflict isolation. **NOT** Kotlin/Native cinterop (`CPointer`/`.def`) |
| `kmp-native-authoring` | Authoring brand-new, first-party C/C++ source for a KMP library's native core (directory layout, CMake, public C-ABI header, native ctest) — always followed by `jni-pro` for the actual bridge. **NOT** bridging to code that already exists |

### Layer 4 — Feature Building Blocks
| Skill | Owns |
|---|---|
| `kmp-navigation` | Type-safe routes, nested graphs, bottom nav, deep links |
| `kmp-shared-resources` | Strings, images, fonts, plurals, localization |
| `kmp-mvi` | MVI architecture, Contract pattern, `MviViewModel`, State/Intent/Effect, one-shot effects |
| `kmp-paging` | Paging 3 — `PagingSource`, `Pager`, `PagingData`, cursor vs offset, `RemoteMediator`, load-state handling |
| `kmp-analytics` | Sealed `AnalyticsEvent`, `Analytics` interface, Firebase/Amplitude impls, screen tracking, `FakeAnalytics` |
| `kmp-form-validation` | `ValidationResult`, `FieldState`, synchronous + async validators, submit gating, `ValidatedTextField` |
| `kmp-image-loading` | Coil 3 — `AsyncImage`, `AvatarImage`, `HeroImage`, single `ImageLoader`, memory/disk cache |
| `kmp-permissions` | `PermissionState` sealed type, `expect/actual PermissionController`, Android launcher, iOS Info.plist |
| `kmp-deep-linking` | App Links + Universal Links, `DeepLinkParser`, NavHost `navDeepLink`, intent handling, AASA |
| `kmp-biometric-auth` | `BiometricResult`, `expect/actual BiometricAuthenticator`, `BiometricPrompt`, `LAContext` |
| `kmp-push-notifications` | FCM + APNs, `PushToken`, `FirebaseMessagingService`, `NotificationHandler` expect/actual, deep-link routing |
| `kmp-workmanager` | `CoroutineWorker`, `BGTaskScheduler`, `expect/actual BackgroundScheduler`, one-time + periodic, retry |
| `kmp-feature-flags` | `FeatureFlag` enum, `FeatureFlagProvider`, Firebase Remote Config, A/B variants, kill switch, fake provider |
| `kmp-offline-first` | `SyncState` sealed class, `SyncManager` interface, optimistic updates with rollback, conflict resolution, local-first read pattern |
| `kmp-crash-reporting` | `CrashReporter` interface, Firebase Crashlytics + Sentry actuals, breadcrumb logger bridge, dSYM symbolication |

### Layer 5 — UI System
| Skill | Owns |
|---|---|
| `kmp-compose-design-system` | Tokens (colors, typography, shapes, spacing), dark mode, 6 core components, no Material dependency |
| `kmp-compose-design-system-extended` | 27 additional components: Dialog, Sheet, Toast, Tabs, TopAppBar, Checkbox, etc. |
| `kmp-shadcn-compose` | Published-library alternative to `design-system` — Maven Central setup, `ShadcnTheme`, 70+ components. Gated to explicit user choice (`/kmp-new-project` Step 6a); never suggested unprompted — carries a real experimental-API dependency risk |
| `kmp-compose-adaptive-layout` | WindowSizeClass, Compact/Medium/Expanded breakpoints, list-detail split, adaptive navigation, cross-session pattern consistency |
| `kmp-compose-slot-api` | `@Composable () -> Unit` slots, scoped slots, CompositionLocal, component API shape |
| `kmp-compose-state-hoisting` | Hoist-until-shared rule, controlled components, stateless vs stateful composables |
| `kmp-compose-state-container` | `remember` vs `rememberSaveable` vs `ViewModel` survival matrix, custom Saver |
| `kmp-compose-graphics-modifiers` | `graphicsLayer`, Canvas, drawBehind, drawWithCache, workflow node shells, custom drawing performance |
| `kmp-compose-preview-driven-development` | Desktop-first `@Preview` workflow, `@PreviewParameterProvider`, PDD cycle, `./gradlew :desktopApp:run` |
| `kmp-imagevector-generator` | Raster/SVG → compiled ImageVector toolchain (quantize/trace/normalize/codegen), semantic vs literal tinting, node budget, no hand-written path data |

### Layer 6 — Testing & Quality
| Skill | Owns |
|---|---|
| `kmp-unit-testing` | `runTest`, Turbine, fake-over-mock, `:core:testing` fixtures module, JVM ViewModel tests |
| `kmp-roborazzi` | Screenshot tests from `@Preview` on JVM/Desktop, golden images, CI diff job |
| `kmp-code-quality` | Ktlint (formatting) + Detekt (architecture rules), CI gates |
| `kmp-compose-accessibility` | Semantic roles, `contentDescription`, `mergeDescendants`, touch targets, traversal order, Roborazzi a11y snapshots |
| `kmp-compose-animation` | `AnimatedVisibility`, `animateContentSize`, `Crossfade`, `AnimatedContent`, `animateXAsState`, shared elements, reduced motion |
| `kmp-benchmark` | `kotlinx-benchmark` setup, `@State`/`@Benchmark` conventions, per-target registration, `docs/reference/benchmark-matrix.md` result placement |
| `kmp-compose-web-performance` | Live browser profiling for the Web/Wasm target via the official `chrome-devtools-mcp` — performance traces, Lighthouse audits, network waterfall, Wasm bundle-size awareness |

---

## Dependency Graph

```
kmp-clean-architecture     ← read first (defines the rules)
kmp-feature-scaffold       ← scaffold second (implements the rules)
├── kmp-presenter-module   (depends on: scaffold, clean-architecture)
├── kmp-flavor-environment (no deps)
├── kmp-ci-github-actions  (no deps)
├── kmp-android-cli        (no deps)
├── kmp-release            (depends on: ci-github-actions, xcframework-spm)
├── kmp-dependency-injection (no deps)
├── kmp-audit              (no deps for review work)
├── kmp-migration          (depends on: audit, clean-architecture, mvi)
├── kmp-refactor           (depends on: clean-architecture, code-quality)
├── kmp-project-docs-maintainer (depends on: audit)
├── kmp-layout-system      (no deps)
├── kmp-lessons            (no deps)
├── kmp-skill-harvester    (depends on: lessons)
├── kmp-logging            (depends on: scaffold)
├── kmp-ktor-auth-service  (no deps)
├── kmp-mongodb-database   (no deps)
├── kmp-kotlin-rpc         (no deps)
├── kmp-network-layer      (depends on: scaffold)
├── kmp-sqldelight-setup   (depends on: scaffold)
├── kmp-xcframework-spm    (depends on: scaffold, ci)
├── kmp-api-mimicry        (depends on: library-publishing)
├── kmp-native-authoring   (no deps; always followed by jni-pro)
├── kmp-expect-actual      (depends on: scaffold)
├── kmp-repository-pattern (depends on: network-layer, sqldelight-setup)
├── kmp-navigation         (depends on: scaffold)
├── kmp-shared-resources   (depends on: scaffold)
├── kmp-mvi                (depends on: scaffold, navigation)
├── kmp-compose-design-system      (depends on: scaffold, shared-resources)
├── kmp-compose-design-system-extended (depends on: design-system)
├── kmp-compose-slot-api   (depends on: design-system)
├── kmp-compose-state-hoisting (depends on: mvi)
├── kmp-compose-state-container (depends on: mvi, navigation)
├── kmp-compose-graphics-modifiers (depends on: design-system, compose-state-container)
├── kmp-compose-preview-driven-development (depends on: presenter-module, design-system)
├── kmp-unit-testing       (depends on: presenter-module)
├── kmp-roborazzi          (depends on: preview-driven-development)
├── kmp-code-quality       (depends on: scaffold, clean-architecture)
├── kmp-paging             (depends on: mvi, network-layer, repository-pattern)
├── kmp-analytics          (depends on: mvi, dependency-injection)
├── kmp-form-validation    (depends on: mvi, design-system)
├── kmp-image-loading      (depends on: design-system, network-layer)
├── kmp-permissions        (depends on: mvi, dependency-injection)
├── kmp-deep-linking       (depends on: navigation)
├── kmp-biometric-auth     (depends on: mvi, dependency-injection)
├── kmp-push-notifications (depends on: permissions, deep-linking, workmanager)
├── kmp-workmanager        (depends on: dependency-injection)
├── kmp-feature-flags      (depends on: dependency-injection, analytics)
├── kmp-compose-accessibility      (depends on: design-system, roborazzi, compose-animation)
├── kmp-compose-animation  (depends on: design-system)
├── kmp-offline-first      (depends on: repository-pattern, sqldelight-setup, workmanager)
└── kmp-crash-reporting    (depends on: logging, dependency-injection)
```

---

## Build Order for a New Project

### Phase 1: Foundation (do once per project)
1. **`clean-architecture`** — read the layer contract before writing any code
2. **`feature-scaffold`** — create the project from Kotlin/kmp-wizard, 6-layer module structure
3. **`flavor-environment`** — set up dev/staging/prod before writing any API code
4. **`network-layer`** — Ktor client, `NetworkResult`, auth interceptor
5. **`sqldelight-setup`** — local database, platform drivers, Koin wiring
6. **`logging`** — structured logging wrapper setup before any feature adds log calls
7. **`ci-github-actions`** — CI before any feature merges
8. **`code-quality`** — Ktlint + Detekt as CI gates from day one

### Phase 2: iOS/Desktop Readiness (if shipping to those platforms)
9. **`xcframework-spm`** — SPM binary target for iOS team
10. **`expect-actual`** — platform-specific code (UUID, SecureStorage, dispatchers)

### Phase 3: First Feature (repeat for each feature)
11. **`design-system`** — tokens and core components (once per project, before first feature)
12. **`navigation`** — add the feature's routes to the nav graph
13. **`shared-resources`** — add strings/assets the feature needs
14. **`repository-pattern`** — wire `RemoteDataSource` + `LocalDataSource` → `FooRepository`
15. **`presenter-module`** — `FooViewModel` (no Compose dep) + `FooUiState`/`FooUiIntent`
16. **`mvi`** — `FooScreen`/`FooContent` split consuming the presenter
17. **`preview-driven-development`** — Desktop `@Preview` for all states before wiring logic
18. **`unit-testing`** — `runTest` + Turbine tests for the ViewModel before shipping

### Phase 4: Richer UI & Quality (as needed)
19. **`design-system-extended`** — pull in Dialog, Sheet, Toast etc. when the feature needs them
20. **`compose-slot-api`** — when designing reusable components for the design system
21. **`compose-state-hoisting`** — when a component hierarchy gets complex
22. **`compose-state-container`** — when debugging state survival across rotation/back-nav
23. **`roborazzi`** — screenshot golden tests once the UI is stable

---

## Feature Slice Checklist

For every new feature module group (`:feature:x:model/:api/:domain/:data/:presenter/:ui`), verify:

**`:feature:x:model` (pure types)**
- [ ] Only `data class`, `sealed class`, `enum class` — no interfaces, no framework imports
- [ ] No dependency on any other module

**`:feature:x:api` (interfaces)**
- [ ] `FooRepository` interface returns domain types and `Flow<T>` / `Result<T>` only
- [ ] `sealed interface FooError` defined for typed error cases
- [ ] Depends only on `:model` — no logic, no framework deps

**`:feature:x:data` (implementation)**
- [ ] `FooRemoteDataSource` returns `NetworkResult<FooDto>`
- [ ] `FooLocalDataSource` returns `FooEntity` / `Flow<FooEntity?>`
- [ ] `FooRepositoryImpl` maps all types — no DTO or entity escapes to `:api`
- [ ] `FooDataModule` (Koin) wires both data sources and `FooRepository`

**`:feature:x:domain` (use cases, if complexity warrants)**
- [ ] Use cases have a single `invoke` operator
- [ ] Use cases depend only on `:api` — no `:data` imports

**`:feature:x:presenter` (ViewModel — no Compose)**
- [ ] `FooViewModel` has zero Compose imports
- [ ] `FooUiState` and `FooUiIntent` sealed classes defined here
- [ ] Exposes `StateFlow<FooUiState>` — no `SharedFlow` as state holder
- [ ] `_state.update { it.copy(...) }` — never `_state.value = _state.value.copy(...)`

**`:feature:x:ui` (Compose screens)**
- [ ] `FooScreen` wires ViewModel via `koinViewModel()` only
- [ ] `FooContent` is a stateless `@Composable` — accepts `FooUiState` as parameter
- [ ] `@Preview` functions cover Loading / Error / Empty / Success states
- [ ] No direct `:domain` or `:data` imports

---

## Decision Trees

### "Where does this code go?"

```
Is it platform-specific behavior?
├── YES: Does it wrap a platform SDK or require a platform type?
│   ├── YES → expect/actual (kmp-expect-actual)
│   └── NO  → interface + Koin injection in platform sourcesets
└── NO:
    ├── Is it a domain type (data class, sealed, enum)?  → :feature:x:model
    ├── Is it a repository interface or nav contract?    → :feature:x:api
    ├── Is it network communication?     → :core:network + network-layer skill
    ├── Is it local persistence?         → :core:database + sqldelight-setup skill
    ├── Is it domain logic?              → :feature:x:domain use cases
    ├── Is it data fetching + mapping?   → :feature:x:data repository-pattern skill
    ├── Is it ViewModel / UiState?       → :feature:x:presenter (presenter-module skill)
    ├── Is it a Compose screen?          → :feature:x:ui (mvi skill, Content composable)
    ├── Is it a reusable UI component?   → :core:designsystem slot-api + state-hoisting skills
    └── Is it app-wide config?           → :core:common or flavor-environment skill
```

### "Which state container?"

```
Does the state involve async, IO, or repository calls?
├── YES → ViewModel (mvi skill)
└── NO:
    ├── Must survive rotation? YES
    │   ├── Bundle-safe type? → rememberSaveable {}
    │   └── Complex type?     → rememberSaveable(stateSaver = customSaver)
    └── Must survive rotation? NO → remember {}
    └── Shared with another screen? → ViewModel (graph-scoped)
```

Full survival matrix: see `kmp-compose-state-container`.

### "Which transport for a backend call?"

Before following the tree below, check by **content**, not by module name, whether a
Ktor client already exists anywhere in the project — a new server module or feature
with a different name is still the same transport concern. Real bug this fixed: an
agent found no module literally named `:core:network` and defaulted to a raw HTTP call
instead of the project's actual (differently-named) client:

```bash
grep -rl "HttpClient(\|safeRequest\|NetworkResult<" */src --include="*.kt"
```

If that finds matches, reuse whatever module they're in — never scaffold a second client
or write a raw platform HTTP call because the path didn't match an assumed name. See
`kmp-network-layer`'s Step 0 for the full detection procedure.

```
grep -r "RemoteService\|@Rpc\|withRpc\|KtorRPCClient\|rpcClient\|\.rpc(" */src --include="*.kt" -l

Results found?
├── YES (kRPC is in the project):
│   ├── Does an existing RPC service interface expose this operation?
│   │   ├── YES → call through the RPC client; do NOT add safeRequest
│   │   └── NO  → extend the service interface with a new method; do NOT add a parallel HTTP call
│   └── Is the call to a DIFFERENT backend (external REST API, third-party service)?
│       └── YES → safeRequest is correct; this is a separate network boundary
└── NO (kRPC not present):
    ├── Is the backend a Kotlin-first Ktor server you control?
    │   ├── YES → consider kRPC (kmp-kotlin-rpc skill) before adding HTTP
    │   └── NO  → use safeRequest (kmp-network-layer skill)
    └── Is the backend a third-party REST API?
        └── YES → safeRequest is correct
```

### "expect/actual or interface?"

```
Is it a pure behavior difference (same API, different platform behavior)?
→ Interface + Koin injection

Does it require a platform-specific constructor argument (Context, UIViewController)?
→ expect class / typealias actual

Does it wrap a platform SDK with no clean interface abstraction?
→ expect class (Category 3 in expect-actual skill)

Is it a stateless primitive with no constructor (UUID, currentTimeMillis)?
→ expect fun (Category 4 in expect-actual skill)
```

Full guide: see `kmp-expect-actual`.

### "What layer does this DTO/entity/model belong to?"

```
NetworkDto (from Ktor JSON)      → stays inside :feature:x:data/remote/dto/
DatabaseEntity (from SQLDelight) → stays inside :feature:x:data/local/
DomainModel (data class)         → lives in :feature:x:model/
RepositoryInterface              → lives in :feature:x:api/
UiState / UiIntent               → lives in :feature:x:presenter/
Composable screen                → lives in :feature:x:ui/
```

The rule: data flows **inward** through mappers. DTOs and entities never cross the `:data`
boundary. Domain types (in `:model`) are the lingua franca across `:api`, `:domain`, and `:presenter`.

### "Improve the performance of X" — where do I even look?

There is no single performance skill — routing depends entirely on what X names.
Never guess at a target; if X is unnamed or app-wide ("the app feels slow"), ask the
user to narrow it to one of the branches below before picking a skill.

```
What is X?
├── A specific composable re-rendering too often / UI feels janky?
│   → kmp-compose-state-container (wrong container, e.g. ViewModel
│     for ephemeral state) or kmp-compose-state-hoisting (state
│     buried too deep, forcing a wide recomposition scope)
├── Custom drawing (Canvas, graphicsLayer, drawBehind) is slow?
│   → kmp-compose-graphics-modifiers
├── A JNI/native bridge call?
│   → kmp-jni-pro (minimize boundary crossings, batch marshalling,
│     GPU sync tips already in the skill)
├── Database queries?
│   → kmp-sqldelight-setup (indices, Flow query batching)
├── Network calls / sync?
│   → kmp-network-layer or kmp-offline-first
│     (cache-first, avoid redundant refresh)
├── App startup time or binary/APK size?
│   → kmp-proguard-r8
├── Web/Wasm target: slow page load, dropped frames in the browser, or bundle size?
│   → kmp-compose-web-performance (live browser profiling via chrome-devtools-mcp —
│     distinct from kmp-benchmark, which measures a Kotlin function in isolation,
│     not the running browser's own load/render cost)
├── A specific function/class flagged as complex (long, many params, deep nesting)?
│   → kmp-code-quality (Detekt `complexity:` rules — LongMethod,
│     CyclomaticComplexMethod, LongParameterList)
├── Need a real number, not a guess (comparing two implementations, confirming a fix)?
│   → kmp-benchmark
└── Unnamed / whole-app / "it feels slow"?
    → STOP — do not pick a skill on a guess. Ask which of the above the user means,
      or profile first (Android Studio Profiler / Instruments, or
      kmp-benchmark for a specific function/class) to get a concrete
      target, then re-route through this tree.
```

### "How do I handle audit findings?"

```
Finding confirmed?
├── NO → keep it as a question and ask the user for clarification
└── YES:
    ├── Needs tracking in the repo? → draft a GitHub issue
    └── Needs design/product input?  → draft a GitHub question
```

Include the skill name in every draft so attribution stays visible.

---

## Common Anti-Patterns

Review each of these before shipping a feature:

- [ ] **DTO leaking to ViewModel**: `state.userDto.name` in a Screen composable
- [ ] **NetworkResult in MVI State**: `State(result: NetworkResult<User>)` — map to domain first
- [ ] **Direct DB query in ViewModel**: `db.userQueries.select()` in `handleIntent()` — use Repository
- [ ] **`GlobalScope` coroutine**: anywhere in the codebase — use `viewModelScope` or `CoroutineScope(SupervisorJob())`
- [ ] **Mutable `LaunchedEffect` key**: `LaunchedEffect(state.someFlag)` — restarts the effect on every change; use `Channel<Effect>` instead
- [ ] **`isLoading = true` without reset on error**: every `updateState { copy(isLoading = true) }` must have a matching `false` in the error branch
- [ ] **State in `remember` that must survive rotation**: registration form, search query, scroll offset with meaning
- [ ] **ViewModel state for dropdown/tooltip open state**: pure ephemeral UI → `remember`
- [ ] **`@Preview` impossible because state is buried**: composable has internal `remember` that can't be injected — hoist it
- [ ] **`actual everywhere` for pure Kotlin logic**: identical actuals on all platforms → move to `commonMain`
- [ ] **No local cache — pass-through repository**: `override suspend fun getUser() = remote.getUser().toDomain()` — no resilience, no offline support
- [ ] **`observeProducts()` triggers a network call**: the Flow should be reactive (SQLDelight); refresh is a separate `suspend fun`

---

## Skill Invocation Map

When the user asks about one of these topics, invoke the corresponding skill:

| User asks about | Invoke skill |
|---|---|
| "layer contract", "clean architecture", "which layer", ":model vs :api", "internal visibility" | `kmp-clean-architecture` |
| "composition over inheritance", "abstract class in commonMain", "extensible base class", "agent over-abstracting", "requires consumer to extend", "AbstractClassCanBeInterface" | `kmp-clean-architecture` |
| "set up a new KMP project", "create feature module", "6-layer scaffold" | `kmp-feature-scaffold` |
| "presenter module", "ViewModel no Compose", "MVI ViewModel", "UiState UiIntent" | `kmp-presenter-module` |
| "Koin", "dependency injection", "manual modules", "annotated mode" | `kmp-dependency-injection` |
| "review my KMP project", "audit this repo", "what's wrong with this architecture" | `kmp-audit` |
| "project docs", "consumer docs", "project README", "getting started", "project docs reference", "onboarding docs", "architecture diagram", "library docs", "app docs" | `kmp-project-docs-maintainer` |
| "layout system", "screen wireframe", "ASCII wireframe", "draft screen", "document screen layout", "layout doc", "screen layout", "layout-system" | `kmp-layout-system` |
| "write a lesson", "capture lesson", "document a finding", "pattern mismatch", "lesson file" | `kmp-lessons` |
| "harvest lessons", "propose skill amendments", "skill harvester", "harvest findings", "update skills from lessons" | `kmp-skill-harvester` |
| "migrate existing project", "adopt MVI", "LiveData to StateFlow", "migrate to clean architecture", "incremental adoption", "where to start", "brownfield", "refactor architecture", "migration path", "legacy project" | `kmp-migration` |
| "rename symbol", "rename class", "move file", "move class", "move package", "move module", "copy class", "safe delete", "rename skill", "rename command", "package rename", "IntelliJ refactor", "Android Studio refactor", "extract module", "dangling reference" | `kmp-refactor` |
| "repo README", "repo docs", "agent docs", "command docs", "routing text", "skills repo docs" | `docs-maintainer` |
| "wireframes", "screen flows", "layout specs", "design handoff", "component API", "visual direction" | `designer` |
| "release notes", "consumer release notes", "per-skill changelog", "CHANGELOG.md" | `changelog` |
| "logging", "logger wrapper", "logger facade", "kotlin-logging", "KotlinLogging", "Kermit", "log level", "crash reporting", "Crashlytics logging" | `kmp-logging` |
| "token saver", "token-saver", "token saving", "token reduction", "prompt compression", "context compression", "context headroom", "verbose output", "too much output", "caveman", "ponytail", "headroom", "rtk" | `kmp-token-saver` |
| "string.format", "decimalformat", "simpledateformat", "locale formatting", "number formatting", "date formatting", "shared formatter", "kmp formatter" | `kmp-expect-actual` |
| "auth", "authentication", "authorization", "JWT", "sessions", "Ktor RPC" | `kmp-ktor-auth-service` |
| "MongoDB", "database", "collection", "Flow", "change stream", "server-side Kotlin" | `kmp-mongodb-database` |
| "kotlin rpc", "kRPC", "kotlinx rpc", "RPC service", "shared RPC models" | `kmp-kotlin-rpc` |
| "add Ktor", "network layer", "API calls", "token refresh" | `kmp-network-layer` |
| "local database", "SQLite", "SQLDelight", "offline storage" | `kmp-sqldelight-setup` |
| "CI", "GitHub Actions", "run KMP tests" | `kmp-ci-github-actions` |
| "android cli", "android-cli", "android init", "android skills add", "create AVD from terminal", "android run apk", "agent-first android", "android studio quail", "render compose preview cli", "build and run android app", "deploy to emulator", "run KMP android target" | `kmp-android-cli` |
| "publish to Maven Central", "Maven publish", "release library", "release project", "cut release", "ship version", "versioning", "semantic versioning", "bump version", "vanniktech", "Sonatype", "git-cliff", "changelog", "GitHub Release", "release pipeline", "GPG signing" | `kmp-release` |
| "dev/staging/prod", "BuildKonfig", "environment config" | `kmp-flavor-environment` |
| "XCFramework", "Swift Package Manager", "SPM", "iOS binary" | `kmp-xcframework-spm` |
| "ImageVector", "vector icon", "vectorize", "SVG to Compose", "PNG to vector", "trace image", "icon from image", "logo vector", "raster to vector", "vtracer", "potrace", "convert image to icon", "compile icon", "app icon vector", "no PNG icons", "icon pipeline", "extract logo", "extract icon" | `kmp-imagevector-generator` |
| "publish KMP library", "Maven Central library", "KMP library publishing", "vanniktech maven publish", "mavenPublishing", "OSSRH", "Sonatype staging", "GitHub Packages library", "binary compatibility", "apiCheck", "apiDump", "api dump", "BOM library", "bill of materials", "distribute KMP library", "library consumers", "artifactId", "groupId", "POM metadata", "GPG signing library", "SNAPSHOT library", "library release checklist" | `kmp-library-publishing` |
| "GitHub Pages", "developer guide", "docs site", "MkDocs", "MkDocs Material", "Dokka HTML", "API reference site", "documentation website", "gh-deploy", "publish developer docs", "library documentation site" | `kmp-docs-site` |
| "expect actual", "platform-specific", "@ObjCName", "iOS interop" | `kmp-expect-actual` |
| "repository", "data layer", "offline-first", "cache", "single source of truth" | `kmp-repository-pattern` |
| "navigation", "screen routing", "NavHost", "deep links", "web routing", "browser fragment", "hash navigation" | `kmp-navigation` |
| "paging", "Paging 3", "PagingSource", "infinite scroll", "load more", "next page", "cursor pagination", "offset pagination", "LazyPagingItems", "paginate" | `kmp-paging` |
| "shared strings", "strings.xml", "stringresource", "hardcoded strings", "localization", "image assets", "fonts" | `kmp-shared-resources` |
| "MVI", "ViewModel state", "one-shot effects", "Screen/Content split" | `kmp-mvi` |
| "design system", "AppTheme", "design tokens", "dark mode", "spacing tokens", "layout consistency", "AppScaffold", "AppTopAppBar", "page title", "top bar", "action button placement" | `kmp-compose-design-system` |
| "update design system", "sync design system", "update components", "sync components", "update AppButton", "design system out of date", "new version of design system", "design system changed", "refresh design system" | `/kmp-update-design-system` |
| "fix design", "fix colors", "fix spacing", "fix typography", "hardcoded color", "hardcoded dp", "design inconsistencies", "wrong colors", "MaterialTheme instead of AppTheme", "nested cards", "redundant surface", "design violations", "design audit project", "fix design system usage", "detekt design rules", "component reimplementation", "token import boundary" | `/kmp-fix-design` |
| "record baselines", "record golden screenshots", "update golden images", "Roborazzi baseline", "screenshot baseline", "update screenshots", "record design screenshots" | `/kmp-record-design-baselines` |
| "visual audit", "audit screenshots", "check visual consistency", "design visual check", "cross-screen consistency", "spacing rhythm", "color contrast audit", "vision audit design" | `/kmp-audit-design-visual` |
| "adaptive layout", "WindowSizeClass", "tablet layout", "desktop layout", "mobile layout", "phone layout", "list detail", "detail split", "split screen", "navigation rail", "Compact Medium Expanded", "responsive UI", "master detail", "multi-pane", "different layout phone tablet", "different layout phone desktop", "screen size breakpoint", "pane layout", "layout per screen size", "layout phone desktop" | `kmp-compose-adaptive-layout` |
| "dialog", "bottom sheet", "toast", "tabs", "TopAppBar", "Checkbox" | `kmp-compose-design-system-extended` |
| "shadcn-compose", "ShadcnButton", "ShadcnTheme", "ShadcnCard", "shadcn ui kotlin", "shadcn compose multiplatform", "ExperimentalFoundationStyleApi", "shadcn kmp" | `kmp-shadcn-compose` |
| "mimic api", "api mimicry", "clone api shape", "inspired by jetpack compose", "custom dsl engine", "from-scratch renderer", "vulkan ui", "metal ui", "port api ergonomics", "reimplement compose-like dsl", "non-compose renderer", "engine-agnostic dsl", "own compiler-free dsl", "api shape porting" | `kmp-api-mimicry` |
| "slot API", "content lambda", "composable parameter", "scoped slot" | `kmp-compose-slot-api` |
| "state hoisting", "hoist state", "controlled component", "where does state go" | `kmp-compose-state-hoisting` |
| "remember vs ViewModel", "rememberSaveable", "state survival", "config change" | `kmp-compose-state-container` |
| "graphicsLayer", "Canvas", "drawWithCache", "workflow node", "custom drawing" | `kmp-compose-graphics-modifiers` |
| "@Preview", "desktop preview", "PDD", "fast UI iteration", "PreviewParameterProvider" | `kmp-compose-preview-driven-development` |
| "unit test", "runTest", "Turbine", "Flow test", "fake repository", ":core:testing" | `kmp-unit-testing` |
| "screenshot test", "Roborazzi", "golden image", "visual regression", "CI diff" | `kmp-roborazzi` |
| "test canvas layout", "canvas screenshot", "layout regression test", "visual accuracy", "pixel-perfect test", "arrangement test", "test node placement", "UI layout verification", "100% accuracy test" | `kmp-roborazzi` |
| "Ktlint", "Detekt", "code quality", "formatting", "architecture rules", "CI gate" | `kmp-code-quality` |
| "benchmark", "microbenchmark", "kotlinx-benchmark", "performance number", "measure performance", "profile this", "@Benchmark", "JMH", "is this faster", "compare performance", "performance regression" | `kmp-benchmark` |
| "web performance", "chrome devtools", "lighthouse", "performance trace", "wasm bundle size", "core web vitals", "network waterfall", "skiko performance", "compose web performance", "first paint", "wasmJs performance" | `kmp-compose-web-performance` |
| "analytics", "event tracking", "track event", "Firebase Analytics", "screen tracking", "AnalyticsTracker", "event schema", "amplitude KMP", "mixpanel KMP" | `kmp-analytics` |
| "form validation", "field validation", "required field", "email validation", "inline error", "submit disabled", "async validation", "FieldState", "ValidationResult" | `kmp-form-validation` |
| "image loading", "Coil", "Coil 3", "AsyncImage", "network image", "image placeholder", "circular image", "avatar image", "image cache", "disk cache" | `kmp-image-loading` |
| "permissions", "runtime permission", "camera permission", "location permission", "permission denied", "PermissionState", "permission rationale", "iOS permission" | `kmp-permissions` |
| "deep linking", "App Links", "Universal Links", "deep link", "AASA", "Digital Asset Links", "intent filter", "route parsing", "notification deep link" | `kmp-deep-linking` |
| "biometric", "fingerprint", "Face ID", "Touch ID", "BiometricPrompt", "LocalAuthentication", "biometric result", "device credential" | `kmp-biometric-auth` |
| "push notifications", "FCM", "APNs", "Firebase Messaging", "push token", "FirebaseMessagingService", "remote notification", "notification tap" | `kmp-push-notifications` |
| "WorkManager", "background work", "background task", "BGTaskScheduler", "BGProcessingTask", "one-time work", "periodic work", "CoroutineWorker", "background sync" | `kmp-workmanager` |
| "feature flags", "feature toggle", "remote config", "Firebase Remote Config", "A/B test", "experiment", "kill switch", "flag evaluation", "FeatureFlagProvider" | `kmp-feature-flags` |
| "accessibility", "a11y", "TalkBack", "VoiceOver", "contentDescription", "semantic role", "screen reader", "touch target", "WCAG", "traversal order", "mergeDescendants" | `kmp-compose-accessibility` |
| "animation", "AnimatedVisibility", "animateContentSize", "Crossfade", "AnimatedContent", "animateFloatAsState", "shared element", "enter transition", "exit transition", "reduced motion", "spring animation" | `kmp-compose-animation` |
| "offline first", "offline-first", "local first", "conflict resolution", "conflict handling", "background sync", "SyncManager", "SyncState" (opt-in — do NOT match on bare "sync", "cache", or "single source of truth"; those route to `repository-pattern`/`sqldelight-setup`) | `kmp-offline-first` |
| "crash reporting", "crashlytics", "firebase crashes", "sentry", "non-fatal", "symbolication", "dSYM", "breadcrumb bridge", "crash handler", "breadcrumb crash" | `kmp-crash-reporting` |
| "DataStore", "Preferences DataStore", "Proto DataStore", "save settings", "persist user prefs", "SharedPreferences migration", "createDataStore", "local key-value store" | `kmp-datastore` |
| "JNI", "JNI bridge", "native bridge", "JNIEnv", "Java_*", "GetStringUTFChars", "jbyteArray", "wrapper.cpp", "vendor C++", "3rd-party C++", "CMake JNI", "NDK", "call C++ from Kotlin/JVM", "native memory leak", "symbol conflict", "C-shim", "header compatibility" | `kmp-jni-pro` |
| "native core", "first-party native code", "author C++ library", "write native code from scratch", "native library scaffold", "public C-ABI header", "native renderer", "custom engine", "native ctest" | `kmp-native-authoring` |
| Disambiguation — "platform-specific code", "iOS implementation", "CPointer", "cinterop", ".def file", "Kotlin/Native" → `kmp-expect-actual` (NOT `kmp-jni-pro`; JNI is JVM-only) | — |
| "privacy policy", "terms and conditions", "terms of service", "GDPR", "CCPA", "data safety", "App Store privacy", "legal docs", "user data disclosure", "consent screen", "privacy screen", "play store legal", "app store compliance" | `kmp-legal-docs` |
| "ProGuard", "R8", "obfuscation", "minification", "keep rules", "proguard-rules.pro", "release build crash", "ClassNotFoundException release", "NoSuchMethodException release", "APK size", "minifyEnabled", "shrinkResources", "Koin keep", "Ktor keep", "SQLDelight keep", "kotlinx.serialization keep" | `kmp-proguard-r8` |
| "in-app purchases", "IAP", "subscriptions", "Play Billing", "StoreKit", "StoreKit 2", "paywall", "premium feature", "purchase flow", "restore purchases", "entitlement", "billing", "unlock premium", "one-time purchase", "auto-renewing subscription" | `kmp-in-app-purchases` |
| "Desktop target", "Compose Desktop", "CMP Desktop", "window management", "system tray", "file picker", "native menu bar", "keyboard shortcut Desktop", "drag and drop Desktop", "packaging Desktop", "distributable", "macOS app", "Windows app", "Linux app", "rememberWindowState", "jpackage", "dmg", "msi" | `kmp-desktop-app` |

---

## Quick Health Check for Existing Projects

Run through these 6 questions for any KMP project audit:

1. **Dependency direction**: do `:ui` or `:domain` modules ever import from `:data`?
   If yes → architectural violation; data layer details are leaking.

2. **Presenter boundary**: does `:presenter` import `androidx.compose.*` or `org.jetbrains.compose.*`?
   If yes → ViewModels cannot be tested on JVM; move Compose to `:ui` only.

3. **Network/DB types at the boundary**: does any `UiState` contain a `Dto`, `Entity`,
   or `NetworkResult`? If yes → mapping is missing at the repository boundary.

4. **Effect delivery**: are effects `SharedFlow` or `StateFlow`? They should be `Channel<Effect>`.
   `SharedFlow` can replay effects (double navigation, double toast).

5. **State atomicity**: are there any `_state.value = _state.value.copy(...)` calls?
   They should be `_state.update { it.copy(...) }` to be thread-safe under concurrent intents.

6. **Expect/actual ratio**: what fraction of platform files have identical implementations?
   High ratio → probable over-use of expect/actual; move shared logic to `commonMain`.

## Docs-First Rule

Before coding a feature, check the official docs and the project docs:

- verify official Android / Compose guidance first
- prefer standard APIs over custom wrappers unless the docs force a custom path
- record the decision in the project docs before implementation

Use this when the user asks to audit or extend an existing project:

1. Read the project architecture docs
2. Confirm the module boundary
3. Check whether the feature belongs in an existing pattern skill
4. Only then write code or a new skill

**Skill naming rule:** if step 3 reveals no existing skill covers the domain, propose the new
skill by its full name: `kmp-<topic>`. Never suggest a bare topic name
without the `kmp-` prefix. Then route to `/kmp-new-skill kmp-<topic>`.

## Project-Specific Commands/Agents/Skills — Source of Truth

When a user asks for a custom command, agent, skill, or hook for **their own project**
(not one of this collection's own), or an agent decides one is needed — author it at a
project-owned source location first, then deploy a copy into `.claude/` for Claude Code
to actually discover it. Never author directly into `.claude/agents/*.md`,
`.claude/commands/*.md`, or `.claude/skills/*/` as the only copy.

**The model to mirror is this very repo**: `kmp-agent-skills` itself keeps project-owned
agent assets at the repo root, with runtime copies generated separately. A consumer
project should do the same for its *own* custom artifacts so the source stays versioned
next to the app code, reviewable in a normal PR diff, and portable if the project ever
needs to regenerate or move its `.claude/` setup.

Layout — flat, `<name>` is the artifact's own name, never the app/project name:
```
<project root>/
├── agents/<agent-name>.md               ← source
├── rules/<rule-name>.md                 ← source
├── commands/<command-name>.md           ← source
├── skills/<skill-name>/SKILL.md         ← source — project-owned CUSTOM skills only,
│                                           never bundled kmp-agent-skills content
├── hooks/<hook-name>.sh                 ← source
├── docs/reference/ai-collaboration.md   ← canonical cross-agent policy
├── docs/reference/agent-catalog.md      ← canonical model-tier mapping
├── AGENTS.md                            ← optional thin bootstrap
├── CLAUDE.md                            ← optional thin bootstrap
├── GEMINI.md                            ← optional thin bootstrap
├── .agents/
│   └── skills/<skill-name>/             ← DEPLOYED — bundled kmp-agent-skills + mirrored
│                                           custom skills; the cross-client target, read
│                                           by any agentskills.io-compliant client
└── .claude/
    ├── AGENTS.md                        ← deployed routing/context
    ├── commands/<command-name>.md       ← deployed copy
    ├── skills/<skill-name>/             ← deployed copy, mirrors .agents/skills/
    └── settings.json                    ← permissions + hook wiring
```

Thin entrypoints (`AGENTS.md`, `CLAUDE.md`, `GEMINI.md`) should point to the canonical
docs, keep only startup-critical guardrails, and avoid becoming the only copy of
project policy. `docs/reference/agent-catalog.md` owns provider-neutral model tiers and
provider-specific mappings. Do not hardcode stale provider model names across every
agent file when one canonical catalog can carry that mapping.

`rules/` exists for optional project-specific rule snippets or assistant overlays that
should stay project-owned even if only one assistant consumes them today. Do **not**
copy the same policy text from `docs/reference/ai-collaboration.md` into `rules/`.
Keep the explanation canonical in `docs/reference/ai-collaboration.md`; use `rules/`
only when the project genuinely needs short assistant-facing overlays in addition to
that canonical doc.

Use this split consistently:

- `docs/*` answers "how is this project designed?"
- `skills/*` answers "how should an agent work in this repo?"

If a repo-local skill starts retelling architecture docs, stop and move the stable
design guidance back into `docs/*`.

If a project has no custom artifacts yet, still scaffold these folders with placeholder
README files. Empty-but-present source locations make future additions land in the
right place instead of drifting straight into `.claude/`.

**Never nest a project artifact under an app/project-name folder** (e.g.
`skills/<app-name>/<skill-name>/`). Verified against the real, official skill
anatomy (`anthropic-skills:skill-creator`'s own documented convention): a skill's
folder is named after what the skill *does*, flat under `skills/` — this is also how
`.claude/skills/` is actually scanned. If a project-owned skill's name might collide
with one of this collection's 64, resolve it by giving the project-owned skill a more
specific name (e.g. `awaken-ecs-conventions`, not `ecs`) — not by nesting it under an
app-name folder, which isn't a real convention Claude Code (or this collection)
recognizes.

Deploy the copy after every edit to the source — a stale `.claude/` copy that's drifted
from its project-owned source is worse than no source at all, since it looks authoritative
but silently isn't. Simple `cp`/`rsync` is enough; no need for a dedicated script unless
the project has many artifacts to keep in sync. If the project uses
`update-consumer-skills.sh`, that sync path should copy project-owned custom skills from
`skills/<name>/` into `.claude/skills/<name>/` as part of the normal refresh.

**Real gap this closes**: a review of a real KMP game-engine project found two custom
agent definitions (`ecs-dev`, `game-framework-dev`) authored directly into
`.claude/agents/` with no project-owned source anywhere — meaning the only copy of that
authoring work lived in a directory this rule now treats as deploy-only.

**Audited automatically**: `kmp-audit`'s `_detect_project_skill_standards`
checks every `skills/<name>/` folder it finds against the real skill anatomy — SKILL.md
present, opening YAML frontmatter with `name`/`description`, body under ~500 lines unless
a `references/` subdirectory exists. It also checks that the deployed `.claude/skills/`
copy exists and is not stale. Run it any time a project skill is added or edited, not
just once at creation.

## Recommendation Format

When recommending an approach, always present it in this order:

1. Recommend the default first.
2. Show the relevant project structure.
3. Show a small code snippet.
4. Explain why that path is preferred.
5. Mention the main alternative only after the default is clear.

Use this format when the user asks what to build next, which pattern to use, or how a
skill should be applied. Keep the snippet small and directly tied to the structure.

## Naming Rule

Use neutral names by default. Prefix only when the prefix adds clarity at the boundary.

- Shared design-system primitives may use an `App` prefix: `AppButton`, `AppCard`,
  `AppText`, `AppIcon`.
- Feature-local UI should usually stay plain: `UsersScreen`, `UsersList`, `GraphSurface`.
- Layout and state types should be descriptive, not branded: `ViewportState`,
  `LayoutMode`, `Breakpoint`, `SelectionState`.
- Avoid repeating the layer in the name: prefer `Toolbar` over `GraphUiToolbar`,
  `Canvas` over `GraphUiCanvas`, unless a collision actually exists.

If a name feels noisy, remove the prefix first. Add a prefix only when the codebase
already has multiple same-named concepts or the component is part of a shared library.

## Bundled Script

- `scripts/validate_skill_map.py` — checks that the README and expert map still list
  the current skill folders and that the declared skill count matches the repo.

---

## Related Skills

- `kmp-audit` — run this after every feature to verify no architecture smells were introduced
- `kmp-project-docs-maintainer` — use this when a downstream project's README, onboarding, or reference docs drift from the actual code
- `kmp-clean-architecture` — the 6-layer contract that all skill routing assumes
- `kmp-feature-scaffold` — establishes the module structure before any feature skills are loaded
- `kmp-dependency-injection` — every feature plan must include Koin wiring; load this if the plan references bindings

---

## Output Style

When asked for a KMP recommendation, routing decision, or anti-pattern check, respond in this order:
1. recommendation (name the skill and the default choice)
2. the decision rule or dependency graph node that applies
3. why that skill or pattern fits
4. skills to use next (if the task spans multiple domains)

Keep the response concise — this skill routes to other skills, not implements. Name the exact skill to invoke for follow-up work.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-01 | Added an explicit non-KMP scope guard to "When to Use This Skill" — verified no literal trigger-keyword collision exists with unrelated stacks (React, Flutter, etc.), but nothing stated the boundary explicitly for a request naming a different stack outright or no stack at all. Now states plainly: don't route to this collection unless there's real Kotlin/KMP signal, even when the task shape sounds generic (a UI component, a state machine). |
| 2026-07-31 | Fixed the "Project-Specific Commands/Agents/Skills" canonical layout diagram — it never mentioned `.agents/skills/` at all, only `.claude/`, despite `.agents/skills/` being the actual cross-client deploy target this collection has used since an earlier fix. Added it to the diagram, and clarified project-root `skills/` is for custom skills only. Matches the same fix applied to `docs/reference/ai-collaboration.md`. |
| 2026-07-31 | Added `kmp-native-authoring` (66th skill) — real gap: `kmp-jni-pro` explicitly assumes the native C/C++ code already exists (its whole framing is "3rd-party files are read-only," library-first discovery) and never covered authoring brand-new first-party native source. Scaffolds directory layout, CMake, public C-ABI header design, and native-side testing; always hands off to `jni-pro` for the actual bridge. Added to the Meta list, Skill Invocation Map, and dependency graph. |
| 2026-07-31 | Added `kmp-api-mimicry` (65th skill) — a real gap: nothing covered mimicking a reference API's *shape* (Modifier-style chains, slot lambdas, DSL markers) when building a KMP library on a non-standard runtime (custom native renderer, custom transport) that isn't real Compose Multiplatform underneath. Distinct from `design-system`, which builds atop the real Compose runtime. Added to the Meta list, Skill Invocation Map, and dependency graph. |
| 2026-07-15 | Expanded the project-owned scaffold contract for Claude consumers: `rules/` and `docs/reference/ai-collaboration.md` are now part of the canonical source layout, and `CLAUDE.md` is explicitly treated as a thin bootstrap rather than the only copy of project policy. This keeps project-specific agent guidance at the repo root while `.claude/` remains the deployed runtime layer. |
| 2026-07-14 | Added "Project-Specific Commands/Agents/Skills — Source of Truth": a real gap found while reviewing a consumer project (a KMP game engine) whose two custom agent definitions were authored directly into `.claude/agents/` with no project-owned source anywhere. Documents mirroring this repo's own layout (`agents/`, `commands/`, `skills/`, `hooks/` at the project root as canonical source, `.claude/` as the deployed copy) for any project-specific artifact that isn't from `kmp-agent-skills` itself. Cross-referenced from `/kmp-setup-agents`, which only deploys this collection's own skills/commands, not project-owned ones. Corrected same-day: the layout initially nested a skill under an app-name folder (`skills/<app-name>/<skill-name>/`) — verified against `anthropic-skills:skill-creator`'s real, official skill anatomy that this isn't a recognized convention; skills are flat, named after what they do. Fixed to `skills/<skill-name>/`, with name-collision guidance (rename the skill, don't nest it) instead. |
| 2026-07-11 | Added an invocation-map row routing "composition over inheritance"/"abstract class in commonMain"/"agent over-abstracting" to `kmp-clean-architecture`'s new Composition Over Inheritance section — a real, recurring anti-pattern where an agent creates a public abstract class in commonMain requiring consumer inheritance. |
| 2026-07-11 | Added `kmp-docs-site` (62nd skill) — public GitHub Pages developer guide for a published library (MkDocs Material + Dokka HTML + compiler-verified snippet extraction), explicitly gated to library projects with real surface area, never apps or trivial libraries. Added to the Meta list and Skill Invocation Map. |
| 2026-07-10 | Two real gaps closed: (1) added a "Improve the performance of X" decision tree — there was no routing path for performance requests at all (only a model-routing hint, not a skill-routing rule); routes by naming what X is and explicitly stops rather than guessing when X is unnamed or whole-app; added `kmp-benchmark` (61st skill) as its "get a real number" branch. (2) Broadened "Which transport for a backend call?" to check for an existing Ktor client by content (`HttpClient(`/`safeRequest`/`NetworkResult<`) before the kRPC-specific grep — the prior version only checked kRPC symbols, so a project with a plain (differently-named) Ktor client and no kRPC could still fall through to a raw HTTP call; cross-referenced to `kmp-network-layer`'s new Step 0. |
| 2026-06-24 | Refined routing precedence for repo docs, downstream docs, changelogs, and navigation/deep-link collisions. |
| 2026-06-24 | Added architecture-diagram / library-docs / app-docs routing keywords for `kmp-project-docs-maintainer`. |
| 2026-06-24 | Added explicit release routing keywords (`release project`, `cut release`, `ship version`) so project release requests route to `kmp-release`. |
| 2026-06-24 | Added web routing / browser fragment / hash navigation keywords for `kmp-navigation`. |
| 2026-06-24 | Added direct designer routing for wireframes, screen flows, layout specs, and design handoff requests. |
| 2026-06-22 | Fixed kmp-jni-pro routing: was tagged with Kotlin/Native cinterop vocabulary (`CPointer`, `kotlin native interop`) but the skill is strictly JVM JNI. Corrected description + trigger map to JNI terms (`JNIEnv`, `Java_*`, `wrapper.cpp`, `vendor C++`, `C-shim`); added expect-actual disambiguation row for cinterop. |
| 2026-06-21 | Removed private project reference from docs-first rule; rule is now generic. |
| 2026-06-18 | Initial release — 30-skill routing map, dependency graph, build order, decision trees, anti-pattern checklist. |
