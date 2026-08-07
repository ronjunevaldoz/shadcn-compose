# Phase 2 — Foundation and core infrastructure (Steps 4-5)

Part of `kmp-expert` — a phase of the `/kmp-new-project` pipeline.
Run after the plan is confirmed. Ends with a building skeleton.

Load this file when the command reaches this phase; do not load all phases up front. The command itself holds the phase index and the gates between them.

---

## Step 4 — Foundation (always first, always in this order)

**If `--dry-run` was set in Step 1:** stop here, before F-01. Using the confirmed
`PROJECT_TYPE`, `PLATFORMS`, and the feature list from Step 3's sprint plan, print the
module structure that would be created — no `git clone`, no file writes, nothing touches
disk. For `[App]`, use kmp-wizard's own real module map (documented in F-01 below) plus
one `:feature:<name>/{model,api,domain,data,presenter,ui}` block per planned feature and
the `:core:*` modules Step 5's skill-loading table would trigger. For `[Library]`, use
F-01's own `library`/`library-testing`/`sample` structure. End with:
```
Dry run complete — <N> features, <N> modules planned. Re-run without --dry-run to scaffold.
```
Do not proceed to F-01 itself in a dry run.

Run these before any feature work. They establish the module graph and layer contract
everything else depends on. `PROJECT_TYPE` branches which foundation gets built.

### [App] F-01: Project scaffold — clone kmp-wizard first

Load `kmp-feature-scaffold` and follow its `references/step3-clone-kmp-wizard.md`. That
file is the single owner of the clone command, the real `all-targets` module map, the two
things kmp-wizard leaves in a state this collection's own audit flags (the bare `:core`,
`:app:shared`'s demo content), and the `:app:*` boundary rule.

> **Do not restate that module map here.** This pipeline used to carry its own copy, and
> the two drifted twice: once on `:androidApp` vs `:app:androidApp` (fixed in both places
> rather than deduplicated, see `kmp-feature-scaffold`'s 2026-07-31 changelog entry), and
> again on when `:server` exists — this copy claimed "only if SERVER is in PLATFORMS"
> when the *branch* decides it (`all-targets` ships it, `all-frontends-shared` doesn't).
> One owner, no drift.

Then apply the Step 1 intake values to the fresh clone — this part is specific to this
pipeline and has no equivalent in the skill, which scaffolds without an intake:

- Rename project to `PROJECT_NAME`
- Set `group = GROUP_ID` in `gradle.properties`
- Set `android.minSdk = MIN_SDK` in `libs.versions.toml` or `build.gradle.kts`
- Set `iosDeploymentTarget = IOS_TARGET` in the iOS Gradle target block
- If `DISTRIBUTION` is `Play Store + App Store`: keep default signing placeholders; enable ProGuard in release build type
- If `DISTRIBUTION` is `Internal / enterprise`: configure release signing from env vars; disable store-upload CI step
- If `DISTRIBUTION` is `Open source / side project`: skip signing config; CI publishes only artifacts, no store upload

Then add the 6-layer convention plugins on top of what kmp-wizard already ships.
Run `./gradlew help` — must be `BUILD SUCCESSFUL` before any feature work begins.

Never write `build-logic/`, `settings.gradle.kts`, or `gradle.properties` from scratch —
kmp-wizard is the only valid starting point for a new project.

### [Library] F-01: Project scaffold — clone the official library template first

Load `kmp-library-publishing`. There **is** an equivalent to kmp-wizard
for a library — `Kotlin/multiplatform-library-template` (verified against the live repo,
official JetBrains org, same as kmp-wizard). Clone it as the mandatory starting point,
the same discipline as the App path's F-01:

```bash
git clone --depth 1 https://github.com/Kotlin/multiplatform-library-template <PROJECT_NAME>
cd <PROJECT_NAME> && rm -rf .git && git init
```

It ships one `:library` module with `vanniktech-mavenPublish`, the AGP 9
`com.android.kotlin.multiplatform.library` plugin, and `jvm()`/`androidLibrary()`/
`iosArm64()`/`iosSimulatorArm64()`/`linuxX64()` targets already wired. It does **not**
include `js()`/`wasmJs()` (add them if `PLATFORMS` has Web), binary-compat tracking,
`explicitApi()`, licensing, or a contribution guideline — that's what the rest of this
step and `library-publishing`'s Steps 2/5/12/13 add on top.

**Ask first (one `AskUserQuestion`):** does the intake description name more than one
independent consumer surface (e.g., "core logic + a Compose UI layer," "core + testing
fakes consumers need separately")? If yes, restructure the template's single `:library`
module into the multi-module split below; if no or unclear, keep the template's
single-module structure as-is — splitting speculatively is the wrong default.

**Single module (default — the template's own layout, extended).** No `build-logic/` —
the template doesn't ship one, and one `:library` module has nothing for a convention
plugin to de-duplicate:

```
<PROJECT_NAME>/
├── library/                      # Main library module (from the template)
│   └── build.gradle.kts
├── library-testing/              # Test helpers for consumers (optional — add if the
│                                  #   library exposes fakes/test doubles consumers need)
├── sample/                       # Sample app that consumes the library
│   └── build.gradle.kts          # com.android.application only here
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts              # Root: coordinates + publishing config
```

**Multi-module (only when confirmed above)** — per `library-publishing`'s Step 1a,
prefixed with `PROJECT_NAME`, never the literal word "library":

```
<PROJECT_NAME>/
├── build-logic/                  # Shared explicitApi()/AGP/apiCheck config — see
│                                  #   library-publishing's Step 1a for the real wiring
├── <PROJECT_NAME>-core/          # io.github.you:<PROJECT_NAME>-core
├── <PROJECT_NAME>-compose/       # io.github.you:<PROJECT_NAME>-compose — depends on -core
├── <PROJECT_NAME>-testing/       # io.github.you:<PROJECT_NAME>-testing — depends on -core only
├── bom/                          # io.github.you:<PROJECT_NAME>-bom
├── sample/
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts
```

Configure using the intake values:
- `rootProject.name = PROJECT_NAME` in `settings.gradle.kts`
- `GROUP_ID` as the Maven `groupId`
- **Override the template's `version = "1.0.0"` to `0.1.0`** — the clone hardcodes
  `1.0.0`, but a fresh library with zero consumer usage hasn't earned a `1.0.0`
  stability promise yet; per `library-publishing`'s pre-1.0 policy, that version is cut
  deliberately later, not left as the scaffold default
- `android.minSdk = MIN_SDK` if an Android target is included in `PLATFORMS`
- `iosDeploymentTarget = IOS_TARGET` if iOS is included
- Wire `explicitApi()`, the `vanniktech` publish plugin, and `binary-compatibility-validator`
  per that skill's Step 2/3/5 — do this now, not deferred to Step 8, since retrofitting
  `explicitApi()` after public declarations already exist means fixing every violation at
  once instead of writing them correctly from the first line
- Add `:bom` at scaffold time only when the multi-module split above was confirmed —
  single-module projects skip it; add it later via Step 4 if the need appears

Run `./gradlew help` — must be `BUILD SUCCESSFUL` before any API work begins. Skip F-02
and F-03 below entirely — jump to Step 5.

### [App] F-02: Clean architecture
Load `kmp-clean-architecture`. Generate the 6-layer module structure
(`:model`, `:api`, `:domain`, `:data`, `:presenter`, `:ui`) for each inferred feature.

After each foundation step: run `validate_module_graph.py` and confirm zero errors before proceeding.

### [App] F-03: Draft wireframes and architecture diagram (required, before design system or feature work)

Design must exist before code — draft both now, using the confirmed screen list from
Step 3, and confirm with the user before proceeding to Step 5. Do not defer this to
after design system or feature implementation.

*Architecture diagram* (`kmp-project-docs-maintainer`'s Architecture
Diagram Rule): a short diagram showing the project's major modules, layers, and
runtime flow — feature modules, shared core layers, entry points. Print it as a
text/ASCII block for confirmation now, then **write `docs/architecture.md` immediately**
once confirmed — do not wait for Step 10. A session that stops any time after this point
must not lose the architecture record; Step 10 later appends the `## Features` and
`## Stack` sections once the sprint plan and dependency versions are final — it does
not create this file from scratch.

```markdown
# Architecture — <PROJECT_NAME>

## Module structure

Each feature follows the 6-layer pattern:
  :feature:<name>:model      — data classes, sealed results (no deps)
  :feature:<name>:api        — repository interfaces (depends on :model)
  :feature:<name>:domain     — use cases (depends on :api)
  :feature:<name>:data       — repository implementations (depends on :api)
  :feature:<name>:presenter  — MVI ViewModel (depends on :domain)
  :feature:<name>:ui         — Compose screens (depends on :presenter)

## Rules

- Domain layer has zero Android/iOS imports
- ViewModel never imports a Composable
- No business logic in Composables — intents only
- Repository interface in :api, implementation in :data
- Koin bindings in *Module.kt files only
```

*Per-screen wireframes* (`kmp-layout-system`): for every screen in the
confirmed MVP + post-MVP feature list, generate a file in
`docs/layout-system/<feature>/<ScreenName>.md` containing:
- **Component table** — every visible element, its type, and the design-system component it maps to
- **ASCII wireframe** — structural layout showing slot positions, spacing zones, and scroll regions
- **State variants** — one wireframe per meaningful state (loading, empty, error, filled)

Example for a product list screen:
```
docs/layout-system/
  products/
    ProductListScreen.md   — list, loading, empty state variants
    ProductDetailScreen.md — hero image, details, CTA button
  auth/
    LoginScreen.md         — form fields, submit, forgot password link
  orders/
    OrderHistoryScreen.md  — grouped list, empty state
  _components.md           — shared component registry (AppButton, AppTextField, etc.)
```

**UX placement sanity check** — before presenting for confirmation, review each
wireframe's component *placement* against the common convention for that screen's
archetype, not just its structural validity (row widths, frontmatter, one file per
screen — `kmp-layout-system`'s own checks already cover those).
Placement is a judgment call the structural checks can't catch, and a wrong one
survives all the way to real code with nothing to flag it otherwise:

| Archetype | Common placement convention |
|---|---|
| Chat / composer | Attach/tool icons sit in a toolbar row directly above the input, not in the header |
| Form | Primary submit action bottom or bottom-right; destructive actions never adjacent to primary without a gap |
| List with create action | Add/create action in a consistent, single spot (top-right bar or FAB) — not per-item |
| Navigation | Active item visually distinct (already required by the wireframe's own `*` convention); the trigger for a collapsed nav stays reachable in every state |

If a wireframe's draft violates the convention for its archetype, fix it before
presenting — don't present a known-wrong layout and rely on the user to catch it.
This is a text-level sanity pass on the ASCII content itself, not a substitute for
`/audit-design-visual`'s later screenshot-based review (that runs on real rendered
output after implementation; this runs on the plan, before any code exists).

Present the architecture diagram and all wireframes together, then use
`AskUserQuestion` (same pattern as Step 3c) to confirm before continuing to Step 5.
Wireframes are a living spec, not a frozen constraint — they get updated as the design
evolves, but they must exist before design tokens or feature code are written, not
retrofitted after.

---

## Step 5 — Core infrastructure (if needed)

Use intake answers directly — do not re-infer. Run each in dependency order:

| Intake value | Skill | What it generates |
|---|---|---|
| `[App]` `PERSISTENCE = local` | `kmp-sqldelight-setup` | Schema, drivers, migrations, Flow queries |
| `[App]` `PERSISTENCE = settings` | `kmp-datastore` | Preferences DataStore, expect/actual factory |
| `[App]` `BACKEND = REST API` | `kmp-network-layer` | Ktor client, NetworkResult<T>, safeRequest |
| `[App]` `BACKEND = kRPC` | `kmp-kotlin-rpc` | Shared contract, Ktor auth integration |
| `[App]` `AUTH = yes` | `kmp-ktor-auth-service` | Bearer/JWT, login/refresh/logout |
| `[Library]` always | `kmp-library-publishing` | `explicitApi()`, `apiCheck`/`apiDump`, GPG signing, `PUBLISH_TARGET` wiring (already started in Step 4 — this is where BOM/multi-artifact gets added if the need appeared) |
| `[Library]` `PLATFORMS` includes iOS | `kmp-xcframework-spm` | XCFramework + SPM export alongside Maven |
| always | `kmp-dependency-injection` | Koin modules, scope rules |
| always | `kmp-logging` | Kermit setup, log levels, Koin wiring |
| `CI_CD = yes` | `kmp-ci-github-actions` | GitHub Actions matrix: build, test, detekt, ktlint |
| always | `kmp-code-quality` | Ktlint + Detekt config, baseline, CI gate |
| always | `kmp-unit-testing` | Test source sets, fakes/mocks conventions, coroutine test rules for every layer |
| always | `kmp-android-cli` | Wires the `android` CLI's stable command surface — emulator management, `android run` deploy, SDK installs — so the Android target is buildable/runnable from the terminal without opening Android Studio |
| always | `kmp-project-docs-maintainer` | README, onboarding, `docs/reference/` sync — kept current as each step below writes new project docs, not deferred to the end |

Code quality, unit testing, android-cli, docs maintenance, DI, and logging are always
included — every new project needs them from day one, regardless of what feature work
the intake describes. CI/CD is the one exception: it's optional (`CI_CD` intake field,
defaults to yes) since every check it runs (Detekt, Ktlint, tests, the audit script) also
runs identically from a local terminal — GitHub Actions automates the schedule, not the
substance. If `CI_CD = no`, skip this row and mention in the final report that
`/setup-hooks`'s Option A (git pre-commit) is worth wiring instead, so quality gates still
run automatically without a CI provider.

---

