# /kmp-new-project $ARGUMENTS

**KMP Agent Skills** — scaffold a complete KMP project from a natural language description.

`$ARGUMENTS` is optional:
- Omitted: the command asks what the app does before proceeding
- Plain description: `build a todo app in kmm`
- A path to a sample spec: `samples/todo-app.md`
- Append `--dry-run` anywhere in the description to run intake, inference, and planning
  through Step 4's F-03 architecture diagram, then print the resulting module structure
  and stop — no `git clone`, no file writes. Re-run without `--dry-run` to actually
  scaffold once the preview looks right. Strip `--dry-run` from the text before treating
  the rest as the app description.

This command drives the full pipeline end-to-end across 11 steps:
intake → infer → **plan (compact MVP + delivery slices, gated approval, persisted to `PLAN.md`)** → scaffold → infrastructure → design system → screen layouts + previews → features → verify → agent setup → summary.
Any assumptions made are printed before implementation begins.

For every gated decision below (plan confirmation, design token draft, component
library choice, sprint review, etc.), use the `AskUserQuestion` tool to present the
choice — not a printed block the user replies to in free text. Each such point below
still shows the *content* of the options; render them as `AskUserQuestion` options
rather than plain prose.

---

## Step 1 — Read the description and collect the intake

If `$ARGUMENTS` is empty, ask first:
```
What does your app do? (describe in one sentence or a few words)
```
Wait for the answer and use it as the description before continuing.

If `$ARGUMENTS` ends in `.md` and the file exists, read it as the full project spec.
Otherwise treat `$ARGUMENTS` as the raw description.

Print:
```
PROJECT: <description summary, one line>
TARGET:  <current working directory>
```

If the working directory is not empty (contains files other than `.git`, `.claude`, `README.md`),
print a warning:
```
WARNING: directory is not empty — scaffolding will add files alongside existing content.
         If you want a clean project, run this from an empty directory.
```
Do not stop — continue regardless.

Then collect the project intake. Ask for the minimum information needed to start a
consumer project cleanly:

The intake is intentionally short and modal-friendly. If a UI renders it as popup
questions, ask one field at a time in this order and keep the same defaults.

| Field | Ask | Default if omitted |
|---|---|---|
| `PROJECT_TYPE` | Is this an app, or a library other projects will depend on? | Inferred from the description (`library`/`SDK`/`package`/"other projects can use this" → Library; otherwise App) |
| `PROJECT_NAME` | What is the app/project name? | Derived from the description |
| `GROUP_ID` | What package/group ID should the project use? | `com.example.<project>` |
| `APP_TYPE` | What kind of app is this? (App only) | Derived from the description |
| `WHAT_IT_DOES` | What does the app/library do in one sentence? | Derived from the description |
| `PLATFORMS` | Which targets should we scaffold? | Android + iOS |
| `MIN_SDK` | Minimum Android SDK version? | 26 |
| `IOS_TARGET` | Minimum iOS deployment target? | 16.0 |
| `PERSISTENCE` | Does it need local storage, settings, or neither? (App only) | Inferred |
| `BACKEND` | Does it talk to an API, auth service, or server? (App only) | none |
| `AUTH` | Does it have login / sign-in / identity? (App only) | none |
| `DI_APPROACH` | Annotated or manual DI? | annotated |
| `DISTRIBUTION` | Where will the app be distributed? (App only) | Play Store + App Store |
| `PUBLISH_TARGET` | Maven Central, GitHub Packages, or both? (Library only) | Maven Central |
| `CI_CD` | Wire GitHub Actions now, or skip and rely on running scripts locally for now? | yes |

Distribution options: `Play Store + App Store` · `Internal / enterprise` · `Open source / side project`
This affects signing config, ProGuard aggressiveness, and whether the CI release lane includes store upload.

**`PROJECT_TYPE = Library` changes the rest of this pipeline significantly** — no UI,
no screens, no design system. Steps 3 (wireframes), 6 (design system), and 7 (design
previews) are skipped entirely; Step 4's foundation uses
`kmp-library-publishing`'s project structure instead of the kmp-wizard
app clone; Step 8's "features" become public API surfaces instead of screens. Each
step below is marked `[App]`, `[Library]`, or unmarked (applies to both) — resolve
`PROJECT_TYPE` before Step 2 and follow only the steps that apply.

`CI_CD = no` is a legitimate choice, not a shortcut being discouraged — `./gradlew detekt`/
`ktlintCheck`/`test` and `audit_project.py` all run identically from a local terminal;
GitHub Actions automates *when* they run, it doesn't change what they check. Skipping it
doesn't skip code quality or testing (`kmp-code-quality`/`unit-testing`
stay in the always-included list below) — it only skips the automation wrapper around
running them.

If the user omits a field, state the assumption before proceeding and keep moving.

---

## Step 2 — Infer feature set and load expert routing

Pass the description and intake answers to `kmp-expert` to identify which skills are needed.

From the description, extract:
- **App type** (todo, social feed, e-commerce, etc.)
- **Platforms** — default: Android + iOS if not stated
- **Features** — derive from the app type; list each as a ticket-sized unit
- **Data layer** — default: SQLDelight (offline-first) if persistence is implied; DataStore if settings/preferences only
- **Backend** — default: none (local-only) unless the description mentions API, server, sync, or auth
- **Auth** — default: none unless mentioned

⛔ **No code is written during Steps 1–3.** Planning and implementation are strictly
separated. The first line of code is written only after the user confirms the plan in Step 3c.

Print the raw feature list:

```
## Inferred features (planning only — no code yet)

Platforms: <platforms from intake>
Features (raw):
  F-01  Project scaffold
  F-02  Clean architecture
  F-03  <feature name>
  ...
Data:    SQLDelight (offline-first)
Backend: none (local-only)
Auth:    none

Proceeding to planning phase (Steps 3-6a). Implementation starts at Step 4 after approval.
```

---

## Step 3 — Plan: MVP, delivery slices, tasks

This is the gate before any code is written. Always produce one **compact drafted plan**
with recommendations pre-selected — never present a blank template or split the plan
into competing roadmap/task/sprint systems. The user should only need to say
"looks good" or tweak one item.

### 3a — Draft the MVP scope (always recommend first)

Apply these rules to auto-select the MVP cut, then label each choice as recommended:

- Every MVP needs: navigation shell + at least one data-bearing screen
- Auth is MVP only if the app cannot work anonymously (otherwise post-MVP)
- Nice-to-haves (settings, profile, onboarding, notifications) → post-MVP by default
- No analytics, crash reporting, or push notifications in MVP unless explicitly stated

Print a plain numbered list so items can be referenced by number:

```
## Draft: MVP scope

MVP (Sprint 1-N):
  1. <feature> — <why it's core> (recommended)
  2. <feature> — <why it's core> (recommended)
  3. <feature> — included because auth is required to function

Post-MVP (after first release):
  4. <feature> — nice-to-have, not blocking
  5. <feature> — can ship without it
```

### 3b — Draft the delivery slices (roadmap + tasks together)

Always generate concrete slice names, not placeholders. Use the app type to name them
meaningfully (e.g. "Alpha — browse products" not "Milestone 1").

```
## Draft: delivery plan

Slice 1 — Foundation (~1 week)
  Outcome: clean build, CI green, design system renders
  Tasks: F-01 F-02 F-03 F-04 F-05 F-06 F-07

Slice 2 — <First MVP feature> (~1 week)
  Outcome: <feature> works end-to-end
  Tasks: A-01 A-02 A-03 A-04 A-05 A-06 A-07 A-08 A-09 A-10

Slice 3 — <Second MVP feature> (~1 week)
  Outcome: <feature> works end-to-end
  Tasks: B-01 ... B-10

Slice N — Polish + QA (~1 week)
  Outcome: ready for internal alpha release
  Tasks: layout wireframes reviewed, accessibility pass, Roborazzi goldens, release build validation

Estimated MVP: <N> sprints (~<N> weeks)
```

### 3c — Confirm the plan

Print 3a and 3b's drafts together, then use `AskUserQuestion` (not a printed prompt
waiting for free text) to gate the decision. Offer these options:

- **Looks good** — accept the plan as drafted, proceed to persisting it (3d)
- **Move a task to a different slice** — ask which task and which slice
- **Add or remove a feature from MVP** — ask which one
- **Split a slice** — ask which slice and how

**Do not proceed to Step 3d until the user confirms.** After any change: re-print only
the affected section with the change highlighted, then ask again via `AskUserQuestion`.
Never re-print the entire plan after a minor edit — just the delta.

### 3d — Persist the plan to `PLAN.md`

Immediately after confirmation — before any code is written — write `PLAN.md` at the
project root. This is the actual, durable record of the plan; the printed draft in 3a/3b
is not sufficient on its own; a session that stops after this point must not lose the
plan. Mirror this format (checkbox per task, one section per slice):

```markdown
# <PROJECT_NAME> — Development Plan

<WHAT_IT_DOES>

## Status key

| Symbol | Meaning |
|---|---|
| [ ] | Not started |
| [x] | Done |

## MVP scope

- [ ] <feature 1> — <why it's core>
- [ ] <feature 2> — <why it's core>

## Post-MVP

- <feature> — nice-to-have, not blocking

## Delivery plan

### Slice 1 — Foundation (~1 week)
Outcome: clean build, CI green, design system renders
- [ ] F-01 <task>
- [ ] F-02 <task>

### Slice 2 — <First MVP feature> (~1 week)
Outcome: <feature> works end-to-end
- [ ] A-01 <task>
- [ ] A-02 <task>

### Slice N — Polish + QA (~1 week)
Outcome: ready for internal alpha release
- [ ] Layout wireframes reviewed
- [ ] Accessibility pass
- [ ] Roborazzi goldens
- [ ] Release build validation
```

Step 8c checks off each sprint's tasks in this file as they complete — `PLAN.md` is
the live source of truth for what's done, not the chat transcript.

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

Load `kmp-feature-scaffold`. The first action is always:

```bash
git clone --depth 1 --branch all-targets \
  https://github.com/Kotlin/kmp-wizard <PROJECT_NAME>
cd <PROJECT_NAME> && rm -rf .git && git init
```

Then configure the clone using the intake values:
- Rename project to `PROJECT_NAME`
- Set `group = GROUP_ID` in `gradle.properties`
- Set `android.minSdk = MIN_SDK` in `libs.versions.toml` or `build.gradle.kts`
- Set `iosDeploymentTarget = IOS_TARGET` in the iOS Gradle target block
- If `DISTRIBUTION` is `Play Store + App Store`: keep default signing placeholders; enable ProGuard in release build type
- If `DISTRIBUTION` is `Internal / enterprise`: configure release signing from env vars; disable store-upload CI step
- If `DISTRIBUTION` is `Open source / side project`: skip signing config; CI publishes only artifacts, no store upload

The real `all-targets` branch (verified against the live template, not assumed) ships
this module map — know it before touching anything:

```
:app:androidApp   — thin Android entry point (MainActivity only), depends on :app:shared
:app:desktopApp   — thin Desktop entry point (main() only), depends on :app:shared
:app:webApp       — thin JS/Wasm entry point, depends on :app:shared
app/iosApp/       — native Xcode project (NOT a Gradle module) consuming :app:shared's
                    built XCFramework — build/run it via Xcode, not ./gradlew
:app:shared       — the CMP composition root: depends on :core, pulls in Compose
                    runtime/foundation/material3, holds the App() entry composable
:core             — ships as ONE bare module by default — this is exactly the
                    "bare core module [HIGH]" anti-pattern kmp-audit's
                    _detect_bare_core_module flags; must not survive past this step
:server           — Ktor/kRPC backend module (present only if SERVER is in PLATFORMS)
```

Before adding the 6-layer convention plugins, fix the two things kmp-wizard leaves in a
state this collection's own audit would immediately flag:

1. **Split the bare `:core`.** Rename it to `:core:common` — update its
   `settings.gradle.kts` include line and `:app:shared/build.gradle.kts`'s
   `implementation(project(":core"))` reference to `project(":core:common")`. Do not add
   `:core:network`/`:core:database`/`:core:ui`/`:core:testing` yet — those get created
   on demand per Step 5's skill-loading table, not all at once at scaffold time.
2. **Gut `:app:shared`'s placeholder content.** kmp-wizard ships it with demo
   Greeting/counter code — delete it. Rewire `:app:shared` as a thin **composition
   root only**: the top-level `App()` composable, theme wrapper, Koin `startKoin {}`
   call, and a `NavHost` referencing `:feature:*:ui` screens. Never put business logic,
   data access, or feature-specific UI directly in `:app:shared` — that is what
   `:feature:*` and `:core:*` exist for.

**Rule — nothing new lives directly under `:app:*`.** The only modules ever nested
under `:app:` are the four kmp-wizard already created (`androidApp`, `desktopApp`,
`webApp`, `shared`). A new feature or a new piece of shared infrastructure never gets
its own `:app:<name>` module — it goes in `:feature:<name>:*` or `:core:*`. This is
enforced mechanically by `kmp-audit`'s `unauthorized app submodule`
check.

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

## Step 6 — Design system [App only — skip entirely for Library, go to Step 8]

### 6a — Draft design decisions (always pre-recommend, always confirm before generating)

Before generating a single token, draft a design recommendation based on the app type,
then confirm it via `AskUserQuestion` (see below).

**Color palette** — infer from app type:

| App type | Recommended palette |
|---|---|
| E-commerce / retail | Neutral base (white/gray) + strong accent (indigo or orange) |
| Finance / banking | Trust blues + conservative gray, minimal accent |
| Health / fitness | Energetic green or orange primary, white surface |
| Social / community | Vibrant primary (purple or teal), warm neutrals |
| Productivity / tools | Cool gray base, single accent (blue), minimal decoration |
| Food / restaurant | Warm base (cream/orange tint), rich accent (red or amber) |
| Education | Friendly blue or purple primary, soft surfaces |
| Travel | Sky blue or teal primary, warm secondary |

Draft three concrete color options, not just a category name, then use
`AskUserQuestion` in two batches (the tool supports up to 4 questions per call):

**Batch 1 — tokens** (4 questions, one call):
1. Color palette — the drafted `<Name>` options with hex values, one recommended for `<app type>`
2. Mode — Light + Dark (recommended, system default) / Light only / Dark first
3. Typography — Sans-serif system font (recommended) / Rounded sans / Slab serif
4. Corner radius — Medium 8dp/12dp (recommended) / Small 4dp / Large 16dp

**Batch 2 — component sourcing** (3 questions, one call):
1. Component library — **shadcn-compose** (recommended default: published library, 70+ components, real preset theming via `ShadcnTheme`, faster start — note inline: depends on the experimental `@ExperimentalFoundationStyleApi`; a future CMP release that changes it breaks the dependency with no fix except an upstream shadcn-compose release) vs. **Generated & owned** (`kmp-compose-design-system`, no external dependency, full control, safe across CMP upgrades — pick this to avoid the experimental-API risk entirely)
2. Icons — **heroicons-compose** (recommended default: published, Maven Central, faster start, Outline variant only today) vs. Generate on demand (`kmp-imagevector-generator`, no dependency, exact icons, deterministic)
3. Utility styling — Skip (recommended) vs. add tailwind-compose (stable-API utility modifiers, combines with either component library choice)

**Do not generate any design system code until both batches are confirmed.** The Batch 2
question's inline risk note is the confirmation for shadcn-compose — since it's now the
recommended default, do not add a second separate confirmation step on top of it; the
user already saw and answered the risk in the same question. If the owned scaffold was
picked instead, skip Step 6a-ii and go straight to Step 6b's owned-scaffold branch.

### 6a-ii — Draft a ShadcnTheme recommendation (only if shadcn-compose was confirmed)

shadcn-compose's `ShadcnTheme(preset, baseColor, accent, ...)` takes real, named enum
values, not raw hex — infer a recommendation from the same app type used for the color
palette above, using shadcn-compose's actual documented preset personalities (verified
against `ShadcnStylePreset.kt`'s own KDoc, not invented) and the same accent family
already named in the color palette table:

| App type | Preset (documented personality) | Base color | Accent |
|---|---|---|---|
| E-commerce / retail | `Vega` — "clean, neutral, and familiar" | `Neutral` | `Indigo` |
| Finance / banking | `Vega` — "clean, neutral, and familiar" | `Zinc` (cool gray) | `Blue` |
| Health / fitness | `Maia` — "rounded, generous spacing," fluid/bouncy | `Neutral` | `Green` |
| Social / community | `Maia` — "rounded, generous spacing," fluid/bouncy | `Neutral` | `Purple` |
| Productivity / tools | `Nova` — "reduced padding and margins," snappy | `Zinc` (cool gray) | `Blue` |
| Food / restaurant | `Luma` — "fluid, luminous, and soft" | `Stone` (warm gray) | `Orange` |
| Education | `Sera` — "editorial and typographic" | `Neutral` | `Blue` |
| Travel | `Luma` — "fluid, luminous, and soft" | `Neutral` | `Sky` |

Use `AskUserQuestion` (3 questions, one call — `AskUserQuestion` always offers a free-text
"Other" option too, so a preset/accent not in the shortlist is still reachable):

1. **Preset** — Vega "clean, neutral, and familiar" (recommended for `<app type>`) /
   Nova "reduced padding and margins, snappy" / Maia "rounded, generous spacing,
   fluid" / Luma "fluid, luminous, soft". (The full catalog also has Lyra, Mira, Sera,
   Rhea — reachable via "Other" if none of the four fit.)
2. **Base color** — Neutral, true gray (recommended default) / Stone, warm gray /
   Zinc, cool gray / a different palette from the catalog app (Mauve/Olive/Mist/Taupe)
3. **Accent** — the accent already implied by the color palette drafted in 6a
   (recommended) / a different named accent (Amber/Blue/Cyan/Emerald/Fuchsia/Green/
   Indigo/Lime/Orange/Pink/Purple/Red/Rose/Sky/Teal/Violet/Yellow, via "Other")

**Do not add the dependency or wire `ShadcnTheme` until this draft is confirmed** — same
rule as the token draft in 6a.

### 6b — Generate the design system using confirmed tokens

**If the owned scaffold was chosen:**

Load `kmp-compose-design-system`. Generate using the confirmed choices:
- `AppColors` — light and dark color schemes with the confirmed palette
- `AppTypography` — type scale using the confirmed font style
- `AppSpacing` — spacing scale (4dp base grid)
- `AppTheme` — wires colors + typography + shapes
- `AppScaffold` and `AppTopAppBar` base components
- `AppThemePreview` wrapper for Roborazzi

If the inferred plan has more than 3 screens, also load
`kmp-compose-design-system-extended` for Dialog, Sheet, Toast, Tabs.

**If shadcn-compose was chosen (recommended default):**

Load `kmp-shadcn-compose` instead of `kmp-compose-design-system`
— it covers the Maven dependency (`io.github.ronjunevaldoz:shadcn-compose` plus the
per-target artifact for each registered platform), the required
`@OptIn(ExperimentalFoundationStyleApi::class)`, and the `ShadcnTheme` wrapper. Do not also
load `kmp-compose-design-system` — the two are alternative component sources,
never combined in the same project.

Wire `ShadcnTheme` at the app root using the preset/baseColor/accent confirmed in 6a-ii:

```kotlin
ShadcnTheme(
    preset = ShadcnStylePreset.<confirmed preset>,
    baseColor = ShadcnBaseColor.<confirmed base color>,
    accent = ShadcnAccent.<confirmed accent>,
    isDark = isSystemInDarkTheme(),
) {
    // app content
}
```

**If heroicons-compose was chosen:**

Add the Maven dependency `io.github.ronjunevaldoz:heroicons-outline` instead of loading
`kmp-imagevector-generator`. Note the Outline-only limitation to the
user if the plan's screens reference icon styles beyond outline.

**If tailwind-compose was chosen:**

Add the Maven dependency `io.github.ronjunevaldoz:tailwind-compose` alongside whichever
component library choice was made — this is a utility-modifier layer, not a
component source, so it combines with either.

---

## Step 7 — Design previews [App only]

Wireframes were already drafted in Step 4's F-03, before design system or feature work
— this step turns those confirmed wireframes into compilable preview stubs, still
before real implementation.

Load `kmp-compose-preview-driven-development`. For each screen, generate stub
`Content` composables with `@Preview` annotations covering all state variants — before
the real implementation. This makes layout mistakes visible immediately on Desktop
without running a device or emulator.

```kotlin
// Generated stub — real logic added in Step 8
@Composable
fun ProductListContent(
    state: ProductListContract.State = ProductListContract.State(),
    onIntent: (ProductListContract.Intent) -> Unit = {},
) {
    // TODO: implement — layout spec in docs/layout-system/products/ProductListScreen.md
}

@Preview @Composable
private fun ProductListLoadingPreview() =
    AppThemePreview { ProductListContent(state = ProductListContract.State(isLoading = true)) }

@Preview @Composable
private fun ProductListEmptyPreview() =
    AppThemePreview { ProductListContent(state = ProductListContract.State(products = emptyList())) }

@Preview @Composable
private fun ProductListFilledPreview() =
    AppThemePreview { ProductListContent(state = ProductListContract.State(products = sampleProducts)) }
```

After generating stubs: run `./gradlew :app:desktopApp:run` (or open Android Studio
previews) and confirm the slot structure looks right before moving to Step 8.

---

## Step 8 — Features (sprint by sprint, gated)

Execute the approved sprint plan one sprint at a time. **Never start the next sprint
until the user reviews and confirms the current one.**

### For each sprint:

**8a — Announce the sprint**

Print before writing any code for that sprint:

```
## Sprint <N> — <Sprint name>

Tasks: X-01 X-02 ... X-N
Goal:  <sprint goal from approved plan>
```

Then use `AskUserQuestion` — "Starting Sprint <N>, this will generate code. Proceed?"
— options: proceed / adjust tasks first. Wait for confirmation before writing any code.

**8b — Implement the sprint tasks in order**

### [App] For each task in the sprint:

1. **Implement** — load the relevant skill(s), generate all 6 layers:
   - `:model` — data classes, sealed results
   - `:api` — repository interface
   - `:domain` — use cases
   - `:data` — repository impl, mappers, SQLDelight/network calls. If the real backend
     isn't ready yet for this task, generate an `InMemory<Feature>Repository` instead
     (see `kmp-repository-pattern`'s "In-memory repository (no backend
     yet)" section) — same interface, swapped in behind one Koin binding, so the app
     runs and demos end to end without blocking on the API. Never name it `Mock*`/`Fake*`
     — those names mean test-only-safe-to-delete, and this one runs the real app.
   - `:presenter` — MVI ViewModel, UiState, UiEffect, Channel
   - `:ui` — `Screen` (wired ViewModel) + `Content` (pure, previewable)
2. **Wire DI** — add bindings to the feature's Koin module.
3. **Add navigation** — add a type-safe route to NavHost. Load
   `kmp-navigation` on first screen, reuse pattern for subsequent ones.
4. **Write tests** — for every feature:
   - `:presenter` unit tests with `runTest` + Turbine
   - Roborazzi screenshot tests — all state variants, light + dark
5. **Validate** — after each task:
   ```bash
   python3 skills/kmp-audit/scripts/audit_project.py .
   ```
   Fix any findings before moving to the next task.

### [Library] For each task in the sprint:

A library's "sprint tasks" are public API surfaces, not screens — there is no
`:presenter`/`:ui`/MVI/navigation. Each task is a class, interface, or function set the
library exposes, plus its tests and docs:

1. **Implement the public API** — a `:library` module, or a sub-package if `:library` is
   still small enough to stay one module (see `kmp-clean-architecture`'s
   6-layer contract, which `library-publishing` cross-references, once it outgrows that):
   - Design the API surface first — public interface/function signatures, before the
     implementation — since `explicitApi()` (wired in Step 4) makes every visibility
     choice deliberate and `apiCheck` (Step 5) will flag any change to it later
   - Write the KDoc for every public declaration alongside the code, not after — an
     undocumented public API is exactly what `_detect_undocumented_public_api` flags
   - Keep the library's own classes free of Koin/other framework imports — see
     `library-publishing`'s "No forced framework coupling in library internals"
2. **Write tests** — `library/src/commonTest` unit tests for the public contract; add
   platform-specific tests under `androidTest`/`iosTest` only for platform-specific
   (`expect`/`actual`) behavior.
3. **Update the API dump** — after any public API change:
   ```bash
   ./gradlew apiDump   # regenerate library/api/library.api
   git add library/api/
   ```
4. **Validate** — after each task:
   ```bash
   ./gradlew apiCheck   # confirms the dump matches, and was regenerated deliberately
   python3 skills/kmp-audit/scripts/audit_project.py .
   ```
   Fix any findings before moving to the next task.

**8c — Sprint review gate**

After all tasks in the sprint are done: check off this sprint's tasks in `PLAN.md`
(`- [ ]` → `- [x]`) — this is what makes `PLAN.md` the live source of truth instead of
a snapshot from Step 3d that immediately goes stale. Then commit both the sprint work
and the updated `PLAN.md`:

```bash
git add -A
git commit -m "feat(<sprint-name>): complete Sprint <N> — <sprint goal>"
```

Then print a summary — the third field on the `Audit:` line is `Screenshots: <N>
recorded` for `[App]` or `apiCheck: PASS` for `[Library]`; print only the one matching
`PROJECT_TYPE`, resolved to plain text, never a raw conditional marker:

```
## Sprint <N> complete

Done:
  [x] X-01 <task name>
  [x] X-02 <task name>

Audit: PASS | Tests: <N> passed | Screenshots: <N> recorded

Next up - Sprint <N+1>: <sprint name>
  Tasks: Y-01 Y-02 ...
```

Then use `AskUserQuestion` — options: continue to Sprint `<N+1>` / redo a specific task
/ add a task to this sprint before moving on / stop here (resume later with
`/kmp-implement-feature`). **Do not start the next sprint until the user responds.**

**[App]** Skills to load per common feature type:

| Feature type | Skills |
|---|---|
| List + detail | `repository-pattern`, `mvi`, `paging` (if list is large) |
| Create / edit form | `mvi`, `form-validation` |
| Settings / preferences | `datastore`, `mvi` |
| Auth / login | `ktor-auth-service`, `mvi`, `form-validation`, `biometric-auth` (if mentioned) |
| Offline list | `sqldelight-setup`, `offline-first`, `mvi` |

**[Library]** Skills to load per common API surface type:

| API surface type | Skills |
|---|---|
| Any public class/function | `clean-architecture` (once `:library` outgrows one module), `unit-testing` |
| Platform-specific behavior | `expect-actual` — common-first rule, interface injection before `expect`/`actual` |
| Native/JNI bridge | `jni-pro` |
| Kotlin/Native cinterop | `expect-actual` |
| Multi-artifact split | `library-publishing`'s BOM step (Step 4) |

---

## Step 9 — Record goldens + run `/kmp-verify` [App]

After all sprints are complete, record Roborazzi golden images first — screenshot tests
always fail on a fresh project if goldens haven't been recorded yet:

```bash
./gradlew recordRoborazziJvm
git add src/**/roborazzi/**/*.png
git commit -m "test: record initial Roborazzi screenshot goldens"
```

Then run the full validation pipeline:

```bash
/kmp-verify .
```

This runs:
- Architecture audit
- ktlint + detekt
- jvmTest (unit tests + Roborazzi diffs against the just-recorded goldens)
- Visual design audit on screenshot goldens

Fix any blockers. Do not mark the project complete until `/kmp-verify` reports `RESULT: PASS`.

## Step 9 — API dump + local publish smoke test [Library]

After all sprints are complete, generate the initial API dump if not already committed
from Step 8's per-task `apiDump` calls, then confirm the library actually resolves as a
Maven artifact before calling it done — the release checklist's own
`./gradlew publishToMavenLocal` smoke test, run now instead of waiting for the first
real release:

```bash
./gradlew apiDump
git add library/api/
git commit -m "chore: initial API dump" --allow-empty

./gradlew build test apiCheck
./gradlew publishToMavenLocal
```

Then verify a throwaway consumer project can actually resolve it from `mavenLocal()`
before reporting success — an artifact that only builds in isolation but never resolves
as a real dependency isn't actually done. Fix any blockers. Do not mark the project
complete until `apiCheck`/`build`/`test` all pass and the local-publish resolve succeeds.

---

## Step 10 — Generate agent setup

After verify passes, generate both the project-owned Claude scaffold and the deployed
`.claude/` runtime so the team gets agent-driven workflows on day one without burying
all agent authoring inside runtime-only files.

**Write the project-owned source scaffold** first:

```
agents/README.md
rules/README.md
hooks/README.md
commands/README.md
skills/README.md
docs/reference/ai-collaboration.md
docs/reference/agent-catalog.md
CLAUDE.md
```

`docs/reference/ai-collaboration.md` should explain:
- project-specific artifacts live in `agents/`, `rules/`, `hooks/`, `commands/`, `skills/`
- `docs/reference/ai-collaboration.md` is the canonical explanation of that layout
- `rules/` is optional for assistant-specific overlays and must not duplicate the canonical policy doc
- `docs/*` owns stable project design; `skills/*` owns repo-local execution guidance
- `.claude/AGENTS.md` is the deployed routing/context copy for Claude
- `.claude/settings.json` owns runtime permissions and hook wiring
- any project-owned agent artifact must be re-deployed into `.claude/` after edits

`docs/reference/agent-catalog.md` should explain:
- provider-neutral model tiers such as `flagship-coding`, `balanced-coding`, `fast-utility`, `precision-review`
- provider-specific model mapping belongs in one canonical doc, not in every agent file
- thin entrypoints like `AGENTS.md`, `CLAUDE.md`, and `GEMINI.md` should point back to that catalog

`skills/README.md` should include a minimal `skills/<name>/SKILL.md` starter template
with YAML frontmatter (`name`, `description`) and a note that project-owned custom
skills are synced into `.claude/skills/<name>/` — and, per agentskills.io's own
cross-client convention (verified in `docs/reference/agentskills-io-standards.md`),
also into `.agents/skills/<name>/` so the skill is visible to any agentskills.io-
compliant client, not just Claude Code.

`CLAUDE.md` should stay thin and only bootstrap Claude into the generated runtime:

```markdown
### Claude Code Project Profile

### Load skills context on initialization
--system-prompt-file=".claude/AGENTS.md"

### Default flags
--compact
--verbose=false

### Canonical project-owned agent sources
- docs/reference/ai-collaboration.md
- docs/reference/agent-catalog.md
- agents/
- rules/     (optional overlays only)
- hooks/
- commands/
- skills/

### Ignore generated and vendor directories
--ignore="**/build/**"
--ignore="**/.gradle/**"
--ignore="**/vendor/**"
--ignore="**/third_party/**"
```

**[App] Write `.claude/AGENTS.md`** — tailored to this project's actual modules and stack:

```markdown
# AGENTS.md — <PROJECT_NAME>

This project uses [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills).
Skills are installed in `.claude/skills/`.

## Project persona

<1 short paragraph describing the app-specific agent identity>

Examples:
- Todo app: Task Steward — optimize for fast capture, clear prioritization, low-friction completion, and zero clutter.
- Habit app: Coach — keep streaks visible, reduce shame-heavy language, and make progress obvious.
- Finance app: Steward — prioritize clarity, trust, and careful review over flashy automation.

## Skill routing

| Topic | Skill |
|---|---|
| New feature end-to-end | `feature-scaffold` → `clean-architecture` → `mvi` |
| ViewModel / screen state | `mvi` |
| Navigation | `navigation` |
| Dependency injection | `dependency-injection` |
| Code quality / linting | `code-quality` |
| Unit tests | `unit-testing` |
| Android CLI / emulator / deploy | `android-cli` |
| Project docs / onboarding | `project-docs-maintainer` |
<only rows for skills actually scaffolded in this project:>
| Auth / login | `ktor-auth-service` |
| Local database | `sqldelight-setup` |
| REST API / network | `network-layer` |
| Key-value settings | `datastore` |
| Screenshot tests | `roborazzi` |
| ProGuard / release build | `proguard-r8` |
| Design system | `design-system` (or `shadcn-compose`/`tailwind-compose`/`heroicons-compose` if chosen in Step 6a) |
| Architecture audit | `audit` |

## Feature modules

| Feature | Layers |
|---|---|
<one row per :feature:<name> module group, e.g.:>
| auth | :domain :data :presenter :ui |
| <feature> | :domain :data :presenter :ui |

## Stack

| Library | Skill |
|---|---|
<one row per library detected in libs.versions.toml>

## Commands installed

See `.claude/commands/kmp-*.md` for available slash commands.
Key commands:
- `/kmp-implement-feature <name>` — plan → implement → validate → review a new feature
- `/kmp-run-audit` — run architecture audit with per-finding remediation
- `/kmp-verify` — full validation pipeline (tests, audit, design, screenshots)
- `/kmp-execute-ticket <id>` — implement a GitHub issue end-to-end
- `/kmp-fix-design` — scan and fix design system violations
- `/kmp-update-skills` — pull latest skills and re-deploy
```

**[Library] Write `.claude/AGENTS.md`** — same shape `/kmp-setup-agents` generates for
an existing library project (see that command's Step 4 "For LIBRARY projects" template
verbatim), so a library scaffolded here and one onboarded later via `/kmp-setup-agents`
end up with identical routing:

```markdown
# AGENTS.md — <PROJECT_NAME>

This project uses [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

<1–2 sentences: what the library does, target consumers>
Group ID: <GROUP_ID>   Artifact: <PROJECT_NAME>   Published to: <PUBLISH_TARGET>

## Skill routing

| Topic | Skill |
|---|---|
| Publishing to Maven Central | `kmp-library-publishing` |
| iOS / SPM distribution | `kmp-xcframework-spm` |
| API surface management | `kmp-library-publishing` (apiCheck / apiDump) |
| Platform-specific implementations | `kmp-expect-actual` |
| Unit / integration tests | `kmp-unit-testing` |
| Code quality (detekt, ktlint) | `kmp-code-quality` |
| CI automation | `kmp-ci-github-actions` |
| Android CLI / emulator / deploy | `kmp-android-cli` |
| Project docs / onboarding | `kmp-project-docs-maintainer` |
| Architecture audit | `kmp-audit` |
| Harvest consumer lessons | `kmp-audit` (`--harvest` mode via `/kmp-harvest-lessons`) |

## Published artifacts

| Artifact | Module |
|---|---|
| <GROUP_ID>:<PROJECT_NAME> | :library |
| <GROUP_ID>:<PROJECT_NAME>-testing | :library-testing (if present) |

## API surface rules

- Never remove or rename public symbols without a major version bump
- Run `./gradlew apiDump` after any public API change; commit the `.api` file
- `./gradlew apiCheck` runs in CI and blocks merge if API diff is uncommitted
- Mark internal symbols with `@InternalApi` to exclude from the dump

## Commands installed

See `.claude/commands/kmp-*.md` for available slash commands.
Key commands:
- `/kmp-run-audit` — architecture audit with per-finding remediation
- `/kmp-harvest-lessons` — collect patterns to upstream to skills
- `/kmp-verify` — full validation pipeline (build + test + apiCheck)
- `/kmp-check-updates` — check for skill updates
```

**[App] Write `README.md`** at the project root:

```markdown
# <PROJECT_NAME>

<WHAT_IT_DOES>

## Platforms

<list platforms from intake>

## Build

```bash
./gradlew :app:androidApp:assembleDebug              # Android APK
./gradlew :app:shared:assembleSharedReleaseXCFramework  # iOS XCFramework (if iOS target) — open app/iosApp/iosApp.xcodeproj in Xcode to build/run the app itself
./gradlew jvmTest                                     # All tests
```

## Architecture

6-layer clean architecture per feature: `:model` → `:api` → `:domain` → `:data` → `:presenter` → `:ui`

See `docs/architecture.md` for the full structure.

## Agent workflows

Install [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills), then:
- `/kmp-implement-feature <name>` — add a feature end-to-end
- `/kmp-run-audit` — check architecture health
- `/kmp-verify` — full validation pipeline
```

**[Library] Write `README.md`** at the project root:

```markdown
# <PROJECT_NAME>

<WHAT_IT_DOES>

## Install

```kotlin
// build.gradle.kts
dependencies {
    implementation("<GROUP_ID>:<PROJECT_NAME>:<VERSION>")
}
```

Published to <PUBLISH_TARGET>. See [releases](../../releases) for the latest version.

## Platforms

<list platforms from intake>

## Build

```bash
./gradlew build test           # build + unit tests, all targets
./gradlew apiCheck              # verify public API surface is unchanged
./gradlew publishToMavenLocal   # smoke-test a local consumer can resolve it
```

## API stability

This library uses `explicitApi()` and `binary-compatibility-validator` — every public
declaration is deliberate, and `library/api/library.api` tracks the full surface.
See `docs/reference/agentskills-io-standards.md`'s sibling doc (once written for this
project) or `kmp-library-publishing`'s semver classification table
before bumping versions.

## Agent workflows

Install [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills), then:
- `/kmp-run-audit` — check architecture health
- `/kmp-verify` — full validation pipeline (build + test + apiCheck)
```

**[App] Finalize `docs/`** — `docs/architecture.md` and `docs/layout-system/` already exist
from Step 4's F-03 (written immediately after confirmation, not deferred to here).
Append the `## Features` and `## Stack` sections now that implementation is complete —
the sprint plan and `libs.versions.toml` weren't final back at Step 4:

```
docs/
  architecture.md     — written in Step 4, finalized here with Features + Stack
  decisions/          — Architecture Decision Records (ADRs), written now
    001-mvi-pattern.md
    002-sqldelight-vs-room.md   (if SQLDelight was chosen)
    003-koin-di.md
  layout-system/      — already written by Step 4's F-03 (screen wireframes per feature)
```

Append to the existing `docs/architecture.md`:
```markdown
## Features

<list of features from the sprint plan>

## Stack

<key libraries and versions from libs.versions.toml>
```

**Copy the consumer command set** into `.claude/commands/`:

```
Commands to copy (these are safe for consumer projects):
  kmp-implement-feature.md
  kmp-run-audit.md
  kmp-verify.md
  kmp-execute-ticket.md
  kmp-fix-design.md
  kmp-audit-screenshots.md
  kmp-record-design-baselines.md
  kmp-audit-design-visual.md
  kmp-update-design-system.md
  kmp-update-skills.md
  kmp-report-skill-issue.md
  kmp-check-updates.md
```

Do NOT copy repo-internal commands (`kmp-new-skill.md`, `kmp-modify-skill.md`,
`kmp-maintain-docs.md`, `kmp-release-notes.md`, `kmp-setup-hooks.md`) —
those are for maintaining this skills repo, not consumer projects.

Use `AskUserQuestion` — "Also deploy this project's agents/commands to Codex CLI and/or
Gemini CLI?" — options: Codex only / Gemini only / both / skip. Never deploy silently.
Real, verified capability per provider (see `docs/reference/ai-collaboration.md`'s
Per-Provider Capability Matrix): Codex CLI has subagents only (`.codex/agents/*.toml`,
no custom-commands mechanism); Gemini CLI has commands only (`.gemini/commands/*.toml`,
no confirmed subagent mechanism). If chosen, translate `agents/*.md`/`commands/*.md`
(this project's own project-owned sources, written in Step 5's scaffold) into the
target TOML shape — see `/kmp-setup-agents`'s Step 6a for the exact field mapping. Tell
the user explicitly that translated content may reference Claude-specific tool names
that don't map cleanly — review before relying on it.

**Deploy skills into `.claude/skills/`** in two passes:
- first copy the shared `kmp-agent-skills/skills/` bundle
- then sync any project-owned custom skills from `skills/<name>/` into `.claude/skills/<name>/`

**Mirror the same copy into `.agents/skills/`** — the project-level half of
agentskills.io's cross-client convention (verified in
`docs/reference/agentskills-io-standards.md`), so any agentskills.io-compliant client
working in this new project sees the same skills, not just Claude Code.

**Write `.agents/pipeline-context.json`** — seed the project planner with initial context.
Deliberately not under `.claude/`: `agents/planner.md`'s body is copied verbatim into
`.codex/agents/planner.toml` if the user opts into Codex deployment (Step 6a below), and
a `.claude/`-prefixed path baked into that shared source text would be broken there —
`.agents/` is the cross-client-neutral location, same reasoning as `.agents/skills/`:

```json
{
  "project": "<PROJECT_NAME>",
  "group_id": "<GROUP_ID>",
  "platforms": ["<platform list>"],
  "skills_used": ["<skill list from this run>"],
  "recurring_issues": [],
  "proven_patterns": []
}
```

The `planner` agent reads this to avoid known pitfalls and reuse proven approaches.
Update `recurring_issues` and `proven_patterns` manually as the project evolves.

**Write `.claude/settings.json`** with allowlist for common read operations:

```json
{
  "permissions": {
    "allow": [
      "Bash(./gradlew *)",
      "Bash(git status)",
      "Bash(git diff*)",
      "Bash(git log*)",
      "Bash(python3 .claude/skills/kmp-audit/scripts/*)",
      "Bash(find . -name *.kt*)",
      "Bash(grep *)"
    ]
  }
}
```

---

## Step 11 — Summary

Print a summary of everything generated. The two templates below are **fully separate**
— pick the one matching `PROJECT_TYPE` and print it as-is with placeholders filled in.
**Never print an `<if>`/`</if>` tag itself** — those markers exist only in this
command's source to keep one file instead of two; the actual terminal output must be
plain text, fully resolved, with consistent column alignment (pad labels to the widest
one in each block).

**`[App]` template:**

```
## Project complete

App:       <name> — <one-line description>
Platforms: <platforms from intake>

Features: <N> implemented
  [x] F-01  Project scaffold
  [x] F-02  Clean architecture
  [x] F-03  <feature>

Generated:
  Modules:      <N> Gradle modules
  Source files: <N> .kt files
  Tests:        <N> unit tests, <N> Roborazzi screenshot tests
  Screenshots:  <N> PNG goldens (<N> light, <N> dark)

Docs:
  README.md              — project overview, build commands, architecture link
  PLAN.md                — MVP scope + delivery plan, checked off as sprints complete
  docs/architecture.md   — 6-layer rules, module map, stack
  docs/decisions/        — ADRs for key tech choices
  docs/layout-system/    — ASCII wireframes per screen

Agent setup:
  agents/ rules/ hooks/ commands/ skills/  — project-owned source scaffold
  docs/reference/ai-collaboration.md       — canonical cross-agent policy
  CLAUDE.md                                — thin bootstrap into .claude/AGENTS.md
  .claude/AGENTS.md                        — skill routing + feature module table
  .claude/commands/kmp-*.md                — <N> slash commands installed
  .claude/skills/ + .agents/skills/        — <N> skills deployed to both (cross-client)
  .agents/pipeline-context.json            — project context for the planner agent
  .claude/settings.json                    — Bash allowlist + hook wiring home
  (if deployed) .codex/agents/             — <N> subagents translated to TOML
  (if deployed) .gemini/commands/          — <N> commands translated to TOML

Verify:      PASS
Skills used: <list>

Not yet wired: git/CI architecture hooks (pre-commit audit, PostToolUse validation).
Run /kmp-setup-hooks now to add them — recommended for every team project.

Next steps:
  ./gradlew :app:androidApp:assembleDebug                 — build Android APK (if Android in platforms)
  ./gradlew :app:shared:assembleSharedReleaseXCFramework   — build iOS XCFramework, then open app/iosApp/iosApp.xcodeproj (if iOS in platforms)
  ./gradlew :app:desktopApp:run                            — run Desktop app (if Desktop in platforms)
  ./gradlew jvmTest                                        — run all tests
  /kmp-setup-hooks                                         — wire git/CI architecture hooks
  /kmp-implement-feature <name>                            — add your next feature
```

**`[Library]` template:**

```
## Project complete

Library:   <name> — <one-line description>
Platforms: <platforms from intake>

API surfaces: <N> implemented
  [x] F-01  Project scaffold (library-publishing structure)
  [x] F-02  <public API task>

Generated:
  Modules:      <N> Gradle modules
  Source files: <N> .kt files
  Tests:        <N> unit tests
  API dump:     library/api/library.api (<N> public declarations)

Docs:
  README.md   — install instructions, API stability notes
  PLAN.md     — MVP scope + delivery plan, checked off as sprints complete

Agent setup:
  agents/ rules/ hooks/ commands/ skills/  — project-owned source scaffold
  docs/reference/ai-collaboration.md       — canonical cross-agent policy
  CLAUDE.md                                — thin bootstrap into .claude/AGENTS.md
  .claude/AGENTS.md                        — skill routing + published artifacts + API surface rules
  .claude/commands/kmp-*.md                — <N> slash commands installed
  .claude/skills/ + .agents/skills/        — <N> skills deployed to both (cross-client)
  .agents/pipeline-context.json            — project context for the planner agent
  .claude/settings.json                    — Bash allowlist + hook wiring home
  (if deployed) .codex/agents/             — <N> subagents translated to TOML
  (if deployed) .gemini/commands/          — <N> commands translated to TOML

Verify:      PASS
Skills used: <list>

Not yet wired: git/CI architecture hooks (pre-commit audit, PostToolUse validation).
Run /kmp-setup-hooks now to add them — recommended for every team project.

Next steps:
  ./gradlew apiCheck              — verify public API surface
  ./gradlew publishToMavenLocal   — smoke-test local resolution
  /kmp-setup-hooks                — wire git/CI architecture hooks
  /kmp-implement-feature <name>   — add your next API surface
```

---

## Notes

- **[App]** Always generate `Content` composables (pure state, no ViewModel) — they are
  what Roborazzi tests inject. Never screenshot a `Screen` directly.
- **[App]** Every screen needs `AppScaffold` + `AppTopAppBar`. The visual audit will
  catch missing chrome.
- **[App]** Roborazzi golden images must be recorded and committed:
  `./gradlew recordRoborazziJvm` — run this before Step 9.
- **[Library]** Design the public API signature before the implementation — `explicitApi()`
  (wired in Step 4) makes every visibility choice deliberate from the first line instead
  of a retrofit; changing a signature after `apiDump` has run means a real `apiCheck`
  failure, not just a style nit.
- This command is the consumer-facing entry point. For E2E testing the skills themselves,
  use a spec from `samples/` as the `$ARGUMENTS` input in a clean sandbox directory.
