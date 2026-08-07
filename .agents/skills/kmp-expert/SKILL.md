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

Full content: `references/required-vs-optional-skills.md`.

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

## The 69 Skills and What They Own

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
| `kmp-shadcn-compose-layouts` | Composes shadcn-compose components into full page layouts — login/auth forms, generic forms, data table screens, admin/dashboard shells — plus `scan_shadcn_layout_gaps.py` auditing for hand-rolled fields/tables/shells that should migrate to `ShadcnField`/`ShadcnTable`/`ShadcnSidebar` |
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

Full content: `references/dependency-graph.md`.

## Build Order for a New Project

Full content: `references/build-order.md`.

## Feature Slice Checklist

Full content: `references/feature-slice-checklist.md`.

## Decision Trees

Full content: `references/decision-trees.md`.

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
| "shadcn login form", "shadcn admin layout", "shadcn dashboard", "shadcn data table", "ShadcnField", "ShadcnFieldGroup", "ShadcnTable", "ShadcnSidebar", "shadcn compose form", "admin shell compose multiplatform" | `kmp-shadcn-compose-layouts` |
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

Full content: `references/quick-health-check.md`.

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

Full content: `references/project-specific-source-of-truth.md`.

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

## References

Full implementation content lives in `references/*.md`: `dependency-graph`,
`build-order`, `feature-slice-checklist`, `decision-trees`, `quick-health-check`,
`required-vs-optional-skills`, `project-specific-source-of-truth`, `changelog`. Load the
specific file named in the pointer under its matching heading above, not all of them.

Two reference groups back a *command*, not a heading here — orchestration is this skill's:

- `references/agents-md-templates.md` — the `.claude/AGENTS.md` and `CLAUDE.md` bodies
  `/kmp-setup-agents` writes. Here, not in the command, because a command reaches a
  consumer as a single bare `.md` while skills are always deployed.
- `references/new-project-phase-1..5-*.md` — the five phases of `/kmp-new-project`; the
  command is just the index and gates. **Load one phase at a time**, or the split is moot.
The 68 Skills table and Skill Invocation Map stay inline above — `validate_skill_map.py`
and `validate_keyword_routing.py` check this file's own text directly.

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

Full content: `references/changelog.md`.

