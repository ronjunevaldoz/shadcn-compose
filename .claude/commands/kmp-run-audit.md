# /kmp-run-audit $ARGUMENTS

**KMP Agent Skills** — run the architecture audit on a KMP project and get
per-finding remediation steps from the matching skill.

Target project: `$ARGUMENTS` (defaults to `.` if empty)

---

## Step 1 — Run the script

```bash
python3 skills/kmp-audit/scripts/audit_project.py "${ARGUMENTS:-.}"
```

Findings carry verifiable evidence — each shows `file:line` plus the matched source line
so you can confirm a finding before committing to a refactor:

```
god composable [HIGH]: composeApp/.../HomeScreen.kt:24 — 11 LaunchedEffect blocks, 7 effect.collect calls; …
    24 | LaunchedEffect(ttiVm) { ttiVm.effect.collect { … } }
```

The script detects architectural and design smells:

| Pattern | What it catches |
|---|---|
| `state copy race` | `_state.value = _state.value.copy(...)` — race condition in ViewModel |
| `sharedflow replay effect` | `MutableSharedFlow(replay=1)` used for one-shot UI effects |
| `network result in ui` | `NetworkResult<T>` leaking into `:ui` or `:presentation` layer |
| `data import in ui` | `*.data.*` imported from `:ui` — layer boundary violation |
| `manual screen capture` | `playwright`, `adb screencap`, `xcrun simctl io` — replace with Roborazzi |
| `magic color literal` | `Color(0xFF…)` written directly in a composable — dark-mode blind hex color |
| `named color in ui` | `Color.Black`, `Color.White`, `Color.Gray`, `Color.LightGray` etc. in UI files — static Material colors that don't adapt to dark mode; common in borders and dividers |
| `hardcoded divider color` | `HorizontalDivider(color = Color.X)` — divider color bypasses the token system |
| `system dark theme scatter` | `isSystemInDarkTheme()` called inside a composable instead of the theme entry point — dark/light logic scattered |
| `hardcoded spacing` | `padding(16.dp)` or `padding(horizontal = 8.dp)` in a UI file instead of `AppTheme.spacing.X` — layout inconsistency |
| `redundant screen title` | `AppTopAppBar` in topBar slot AND heading-style `AppText` in content — title shown twice |
| `adaptive coverage` | Screen composable missing `windowSizeClass` param while project uses adaptive layout |
| `hardcoded android versioncode` | `versionCode = <literal int>` in an Android app module — Play Console rejects an upload whose versionCode isn't strictly higher than the last accepted one; derive it from the semver source instead |
| `style default with body` | `Style { ... }` set as a parameter default instead of an empty `Style` — official Don't #2 |
| `style state wrong enabled property` | `.enabled = ` on a StyleState — the real property is `isEnabled` |
| `style param on screen composable` | A `style: Style` param on a `*Screen`/`*Content`/`*Page` composable — Styles are for components, not screens |
| `stale compositionlocal in style function` | A `@Composable fun ...Style(): Style` reading `MaterialTheme.*`/`Local*.current` before returning — captured once, goes stale |
| `missing indication null with style state` | A `pressed{}`/`hovered{}` Style block alongside a `clickable(...)` with no `indication = null` anywhere in the file — doubled ripple + Style effect |
| `toggle icon swap instead of rotation` | Both icons of a known chevron pair (`KeyboardArrowDown`/`Up`, `ChevronDown`/`Up`, `ExpandMore`/`Less`, `ArrowDropDown`/`Up`) in one file with no `graphicsLayer { rotationZ }`/`Modifier.rotate()` — swapping icon composables on toggle can shift the trigger's own layout bounds if their intrinsic sizes differ |
| `bare conditional collapse` | Collapsible content shown/hidden with a raw `if (isExpanded) { ... }` and no `AnimatedVisibility`/`.animateContentSize()` anywhere in the file — the instant layout snap reads as the trigger button moving |
| `focused state animates border width` | A `focused {}`/`selected {}` Style block changes `borderWidth`/`borderBottomWidth` instead of only `borderColor` — re-measures the component on focus/selection; reserve the final width at rest and animate color only |
| `combined lesson file` | A `docs/lessons/*.md` file contains more than one lesson (`## What we followed` appears more than once) — the harvester reads one Lesson per file; this breaks grouping and review |
| `combined layout screen file` | A `docs/layout-system/*.md` file (other than `_components.md`) has more than one top-level heading — more than one screen was written into one file |
| `combined sqldelight table file` | A `.sq` file defines more than one `CREATE TABLE` — keep `.sq` files focused, one file per table |
| `raw http bypasses established ktor client` | A raw platform HTTP API (`HttpURLConnection`, `NSURLSession`, etc.) is used somewhere in a project that already has an established Ktor client (`NetworkResult<T>`/`safeRequest` found elsewhere) — detected by content, not a fixed module name |
| `what-comment in control flow` | A `//` comment narrates WHAT a loop/conditional does (starts with an action verb like Loop/Check/Calculate, no WHY-marker present) instead of explaining WHY — heuristic, LOW severity, flags for human review or `/clean-comments` |
| `extensible abstract class in commonMain` | A public `abstract class` in `commonMain` with only abstract members, forcing every consumer to subclass it — replace with an interface consumers implement and inject |
| `module layer-order violation` | A module's `build.gradle.kts` declares a wrong-direction `projects.*` dependency (e.g. `:ui` directly on `:data`, skipping `:presenter`) — declared at the Gradle level, can exist before any file imports the forbidden package |
| `cross-feature module dependency` | A feature module's `build.gradle.kts` depends directly on another feature's module instead of going through a `:core:api` contract |
| `design system prefix mismatch` | An `App*`-named declaration under `core/designsystem` while `docs/design-system.md` records a different resolved `COMPONENT_PREFIX` — the resolved prefix wasn't actually used when generating |
| `empty platform source set` | An `androidMain`/`iosMain`/`jvmMain`/... source directory with no `.kt` files, or files containing only package/import/comments — dead scaffolding; Gradle compiles fine without it |
| `unauthorized app submodule` | A `build.gradle.kts` under `app/<name>/` where `<name>` isn't one of kmp-wizard's own four entry points (`androidApp`/`desktopApp`/`webApp`/`shared`) — new feature logic belongs in `:feature:*`, new cross-feature infrastructure in `:core:*`, never a new `:app:<name>` module |
| `leftover wizard demo code` | kmp-wizard's default `class Greeting`/`compose_multiplatform` logo demo still present — delete it before real feature work starts; `:app:shared` should only ever hold composition-root wiring |
| `lowercase unit composable` | A `@Composable` function returning `Unit` (no explicit return type) named camelCase like a verb (`appButton`) instead of PascalCase like a noun (`AppButton`) — per the Android Kotlin style guide's naming rules |
| `partial param documentation` | A KDoc block documents some of a function's 2+ parameters (via `@param` or inline `[name]`) but not others — reads as complete and isn't; coverage must be all-or-nothing |
| `kotlin-reflect in commonMain` | A `commonMain` file uses full reflection (`memberProperties`, `primaryConstructor`, etc.) — `kotlin-reflect` is JVM-primary, limited/absent on Native and JS/Wasm |
| `god utils file` | A `*Utils.kt`/`*Helpers.kt` file with 10+ top-level functions spanning 3+ distinct receiver types — a grab-bag with no single responsibility |
| `inline unnamed regex` | A `Regex(...)`/`.toRegex()` constructed inline inside an expression instead of bound to a named `val` — unreadable at the call site, recompiled on every call |
| `hardcoded ui string` | A literal string in a `Text`/`AppText`/`ShadcnText` call or `contentDescription` — not localizable; route through `stringResource(Res.string.x)` |
| `object creation in loop` | A known-expensive constructor (`SimpleDateFormat`, `HttpClient`, `MessageDigest`, ...) built inside a `for`/`while` body with no apparent dependency on the loop variable — hoist it before the loop |
| `public mutable collection exposure` | A public `val`/`fun` exposes `MutableList`/`MutableMap`/`MutableSet` directly — a caller can mutate internal state through the reference; expose the read-only type instead |
| `context leak in singleton` | A `companion object`/singleton `object` caches a `Context`/`Activity`/`FragmentActivity`/`AppCompatActivity`/`ComponentActivity` reference — outlives the Activity, prevents garbage collection; `applicationContext`/`Application` is the safe exception |

The script also prints a separate, non-blocking `HINTS` section:

| Hint | What it catches |
|---|---|
| `name-behavior drift` | A `*ViewModel` whose name shares no word with any of its own Intent variants (e.g. `AuthViewModel` handling only `RefreshTapped`/`LogoutClicked`) — a nudge to verify the name still describes the screen, never a blocker |
| `vague class name suffix` | A `class`/`interface`/`object` named with a filler suffix (`Manager`/`Processor`/`Helper`/`Info`/`Data`) that says what the class *is*, not what it *does* — per Clean Code's naming guidance; a well-scoped small class with this suffix can still be the right call, so this is a nudge, not a rule |
| `patch not root-cause fix` | An empty/log-only catch block, or an unjustified `@Suppress` with no comment explaining why — a nudge to verify it's a real fix, not a band-aid; a `TODO`/`FIXME` found nearby is noted as corroborating evidence |

---

## Step 1b — Structure diagram (optional, on request)

```bash
python3 skills/kmp-audit/scripts/generate_structure_diagram.py "${ARGUMENTS:-.}" --mermaid
```

Run when the user wants to visually verify the module layout, or after adding/removing/
renaming a module. Renders the actual App (`feature/*`'s 6-layer contract) or Library
(`library`/`library-testing`/`sample`) structure against the canonical shape, with a Mermaid
diagram. Purely informational — layer violations are already gated by Step 1's findings.

---

## Step 2 — Quality scan

```bash
python3 scripts/scan_skill_issues.py
```

Parse the JSON output (`total_issues`, `by_severity`, `issues[]`).

Print a brief summary before the architecture findings:

```
SKILLS QUALITY SCAN:
  Total issues:   <N>
  🔴 HIGH:        <N>   (testing gaps)
  🟡 MEDIUM:      <N>   (missing sections, stale skills)
  🔵 LOW:         <N>   (minor gaps)
```

If `total_issues > 0`, append:
```
  Run /summarize-issues for the full report with paste-ready fix prompts.
```

This step is non-blocking — continue to Step 4 regardless of the scan result.

---

## Step 4 — Explain each finding

For every finding, load the relevant skill and give a concrete fix:

| Finding | Skill | Fix |
|---|---|---|
| `state copy race` | `presenter-module`, `mvi` | `_state.update { it.copy(...) }` — atomic, race-free |
| `sharedflow replay effect` | `mvi` | `Channel<Effect>(Channel.BUFFERED).receiveAsFlow()` |
| `network result in ui` | `clean-architecture`, `network-layer` | Unwrap `NetworkResult` in `:presenter`; pass only `UiState` to `:ui` |
| `data import in ui` | `clean-architecture` | Move the shared type to `:model` or `:api`; import from there |
| `manual screen capture` | `roborazzi` | `captureRoboImage("name.png") { ... }` in `jvmTest` — no device needed |
| `magic color literal` | `design-system` | Replace `Color(0xFF…)` with `AppTheme.colors.X`; define the token in `AppColors.kt` |
| `system dark theme scatter` | `design-system` | Remove `isSystemInDarkTheme()` from the composable; use a semantic token (`AppTheme.colors.X`) instead |
| `hardcoded spacing` | `design-system`, `design-system-extended` | Replace `N.dp` with `AppTheme.spacing.X`; load the Screen Layout Contract from the design-system skill |
| `named color in ui` | `design-system` | Replace `Color.Gray` / `Color.Black` / `Color.White` etc. with `AppTheme.colors.outline` / `AppTheme.colors.onSurface` / `AppTheme.colors.surface` — named Material colors are static and break in dark mode |
| `hardcoded divider color` | `design-system` | Replace `HorizontalDivider(color = Color.X)` with `HorizontalDivider(color = AppTheme.colors.outline)` or use `AppDivider()` if your design system wraps it |
| `redundant screen title` | `design-system`, `adaptive-layout` | Remove the heading-style `AppText` from the content body — `AppTopAppBar` already provides the page title |
| `adaptive coverage` | `adaptive-layout` | Add `windowSizeClass: WindowSizeClass` param; branch layout at Compact / Medium / Expanded; run `/kmp-audit-adaptive` for full breakpoint report |
| `multi viewmodel screen` | `mvi`, `dependency-injection` | Screen instantiates 3+ `koinViewModel<>()` directly — move each into the child composable that owns it, or extract a coordinator ViewModel |
| `god composable` | `mvi` | Screen has 5+ `LaunchedEffect` blocks or 3+ `effect.collect` relays — extract a Coordinator ViewModel; move state assembly, effect collection, and persistence into `viewModelScope` (see MVI skill → Coordinator ViewModel) |
| `viewmodel in viewmodel` | `mvi`, `dependency-injection` | A ViewModel takes another ViewModel as a constructor param — breaks lifecycle/SavedStateHandle/DI; demote the sub-unit to a State Holder (plain class + injected `CoroutineScope`) or a use case |
| `viewmodel as composable param` | `mvi` | A `@Composable` takes a `*ViewModel` as a required param — use `vm: FooViewModel = koinViewModel()` (defaulted), or split into separate screens (see MVI skill decision order) |
| `string navigation` | `navigation` | String-based routes (`composable("…")`, `navigate("…")`, `startDestination = "…"`) — switch to `@Serializable` type-safe routes: `composable<Route>`, `navigate(Route)` |
| `dto leak to domain` | `repository-pattern`, `clean-architecture` | A `:domain` / `*UseCase` file imports `*.dto.*` or `*.entity.*` — map to domain models in `:data`; domain never sees DTOs/entities |
| `repository leaks data type` | `repository-pattern` | A `*Repository` **interface** references `*Dto` / `*Entity` — the interface must speak domain types only; return domain models and map DTOs/entities in `:data` |
| `raw component bypass` | `design-system`, `design-system-extended`, `shadcn-compose` | A screen uses raw Material components while the project has a design system wired — generated/owned (`Scaffold`→`AppScaffold`, `Button`→`AppButton`, …) or shadcn-compose (`Button`/`Card`/`TextField`/`AlertDialog`/`ModalBottomSheet`→`Shadcn*`; `Scaffold`/`TopAppBar` deliberately excluded for shadcn — no equivalent exists, keep them raw) |
| `fixed width overflow` | `adaptive-layout` | A fixed `.width(≥360.dp)` / `.size(≥360.dp)` or constraint-ignoring `.requiredWidth(…)` overflows a compact phone — use `fillMaxWidth()`, `weight()`, or `widthIn(max = …)`. For true "compact enough" verification, render at 360×800 via Roborazzi and run `/kmp-audit-screenshots` |
| `handwritten imagevector` | `imagevector-generator` | An `ImageVector.Builder` with 10+ hand-written path commands and no GENERATED header — hallucinated coordinates produce broken art; re-trace with `convert_image_to_imagevector.py` (`/kmp-vectorize`) |
| `raster asset in commonMain` | `imagevector-generator` | PNG/JPG in `commonMain` resources — icons/flat art should be compiled ImageVectors (`/kmp-vectorize`); photos are exempt under `assets/photos/` |
| `raw weight literal` | `layout-system`, `adaptive-layout` | `weight(0.37f)`-style arbitrary proportion — use simple fractions (`1f`, `1.5f`, `2f`, `3f`) from the slot-grid contract; regenerate the shell with `generate_slot_scaffold.py` |
| `breakpoint branch missing` | `adaptive-layout` | A `when(widthSizeClass)` that handles some but not all of Compact/Medium/Expanded with no `else` — the unhandled size falls through silently |
| `hardcoded android versioncode` | `release` | `versionCode = <literal>` in an Android app module — derive it from the semver source (`major*1_000_000 + minor*1_000 + patch`); a static value passes every check locally and only fails as a hard Play Console rejection on the second upload |
| `style default with body` | `design-system` | Use an empty `style: Style = Style` default; merge project defaults inside via `defaultStyle then style` in `Modifier.styleable(...)` |
| `style state wrong enabled property` | `design-system` | Replace `.enabled = ` with `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` |
| `style param on screen composable` | `design-system` | Remove the `style: Style` param from the screen; hoist the styling into a child component instead |
| `stale compositionlocal in style function` | `design-system` | Read the token via a `StyleScope` extension property inside the `Style { }` lambda, never outside it in a `@Composable`-returning-`Style` function |
| `missing indication null with style state` | `design-system` | Add `indication = null` to the `clickable(...)` call so the Style animation is the only visual feedback |
| `toggle icon swap instead of rotation` | `design-system-extended` | Use one icon rotated via `Modifier.graphicsLayer { rotationZ = ... }` (driven by `animateFloatAsState`) instead of swapping between two icon composables — see `AppAccordion` |
| `bare conditional collapse` | `compose-animation` | Wrap the conditional content in `AnimatedVisibility(expandVertically()/shrinkVertically())` or add `.animateContentSize()` to the containing layout |
| `focused state animates border width` | `design-system` | Reserve the final border width at rest (`borderColor(Color.Transparent)` if none at rest) and animate only `borderColor` on focus/selection |
| `combined lesson file` | `lessons` | Split into separate files via `create_lesson.py`, one invocation per finding |
| `combined layout screen file` | `layout-system` | Split into separate files via `create_wireframe.py`, one invocation per screen |
| `combined sqldelight table file` | `sqldelight-setup` | Split into one `.sq` file per table |
| `raw http bypasses established ktor client` | `network-layer` | Find the existing client by content (`grep -rl "HttpClient(\|safeRequest\|NetworkResult<"`), reuse it instead of the raw call — see Step 0 |
| `what-comment in control flow` | `code-quality` | Run `/clean-comments` on the flagged file — extracts a named function/variable so the code reads as its own explanation, or keeps the comment only if it's genuinely a WHY |
| `extensible abstract class in commonMain` | `clean-architecture` | Replace the abstract class with an interface, and wire the consumer's implementation through Koin (`dependency-injection`) instead of inheritance — see Composition Over Inheritance |
| `module layer-order violation` | `clean-architecture` | Remove the wrong-direction `implementation(projects.*)` line and route the dependency through the correct layer order instead |
| `cross-feature module dependency` | `clean-architecture` | Extract a `:core:api` contract the other feature implements, instead of depending on its module directly |
| `design system prefix mismatch` | `design-system` | Regenerate the flagged file(s) with the resolved `COMPONENT_PREFIX` directly — don't hand-rename `App*` symbols after the fact |
| `empty platform source set` | `feature-scaffold` | Delete the empty source directory, or implement the real `expect`/`actual` code if this module genuinely needs platform-specific logic — never scaffold the folder "just in case" |
| `unauthorized app submodule` | `feature-scaffold` | Move the module's content into `:feature:<name>:*` or `:core:*`, then delete the `app/<name>/` module and its `settings.gradle.kts` include |
| `leftover wizard demo code` | `feature-scaffold` | Delete `Greeting.kt`/the demo `App()` body; rewrite `:app:shared`'s `App()` as composition-root-only wiring (theme, Koin, NavHost) |
| `lowercase unit composable` | `code-quality` | Rename to PascalCase; update every call site and the Koin/nav references to it |
| `partial param documentation` | `code-quality` | Either document every listed parameter (mixing `@param` and inline `[name]` is fine) or strip back to a plain summary with no parameter-level detail at all |
| `kotlin-reflect in commonMain` | `code-quality` | Use `kotlinx.serialization` instead for cross-platform needs, or move the code to a JVM-only module |
| `god utils file` | `code-quality` | Split into files named for what each function is for (`StringExtensions.kt`, ...), or move each function into the module that owns its domain |
| `inline unnamed regex` | `code-quality` | Bind the pattern to a `private val <NAME>_RE = Regex(...)` constant and reference it by name at the call site |
| `hardcoded ui string` | `shared-resources` | Move the literal to `values/strings.xml`, wrap with `stringResource(Res.string.x)` |
| `object creation in loop` | `code-quality` | Move the constructor call to a `val` declared before the loop |
| `public mutable collection exposure` | `code-quality` | Change the declared type to `List`/`Map`/`Set`, back it with a private `Mutable*` |
| `context leak in singleton` | `code-quality` | Replace the cached `Context`/`Activity` with `applicationContext`, or stop caching the reference and pass it explicitly where needed |
| `name-behavior drift` (hint) | `mvi` | Read the ViewModel and its Contract — if the name genuinely no longer fits, rename it and update its Koin binding + composable references. If it's a false positive (generic name is fine), no action needed — this is a manual call, not a rule |
| `vague class name suffix` (hint) | `clean-architecture` | Rename to describe the actual responsibility (`SyncCoordinator`, `AuthService`, `TokenStore`, ...) — or leave it if the name genuinely fits a small, well-scoped concern; this is a manual call, not a rule |
| `patch not root-cause fix` (hint) | `code-quality` | Read the catch block/`@Suppress` and decide: fix the underlying issue, add a real recovery path, or leave a justification comment explaining why the current form is correct |

---

## Step 5 — Output

On findings:
```
PROJECT: <path>
RESULT:  <N> findings

FINDINGS:
  state copy race: feature/auth/presenter/AuthViewModel.kt
  → Fix: replace _state.value = _state.value.copy(...) with _state.update { it.copy(...) }
  → Full guidance: kmp-mvi / kmp-presenter-module

SUMMARY:
  Total:    <N>
  Blockers: <N>
```

On clean:
```
PROJECT: <path>
RESULT:  CLEAN — no architecture smells found
```

---

## Step 6 — Optional auto-fix

If the user says "fix it", load `agents/fixer.md`.

Apply only HIGH confidence fixes automatically. Present MEDIUM and LOW to the user
for a decision before touching anything.

---

## Step 7 — Skill gap reporting (automatic)

After presenting findings, check for findings in these categories that indicate a skill
gap rather than a consumer code problem:

- `agent-setup [HIGH]` — skill doesn't teach setup → reportable
- `design-system [MEDIUM]` — skill wiring guidance missing/wrong → reportable
- Any finding whose fix requires guidance not present in the relevant skill → reportable

For each reportable finding, prepare a draft issue:

```
─────────────────────────────────────────────
SKILL GAP DETECTED
Finding:  <finding text>
Skill:    <skill slug>
Evidence: <file:line>
─────────────────────────────────────────────
Would you like to file a GitHub issue for this?
The kmp-agent-skills team can improve the skill so future consumers don't hit this.

[y] Submit now   [n] Skip   [v] View draft first
─────────────────────────────────────────────
```

If the user says **y** or **v then y**, submit via:

```bash
python3 ~/.claude/skills/kmp-audit/scripts/draft_issue.py \
  --title "Skill gap: <finding summary>" \
  --evidence "<file:line — <finding text>>" \
  --recommendation "<fix guidance from Step 4>" \
  --skill "<skill slug>" \
  --submit \
  --repo ronjunevaldoz/kmp-agent-skills
```

If `gh` is not installed or not authenticated, print the draft body and tell the user
to open `https://github.com/ronjunevaldoz/kmp-agent-skills/issues/new` manually.

Only report findings that map to a skill gap — do not report consumer architecture
violations (state copy race, god viewmodel, etc.) as skill issues.
