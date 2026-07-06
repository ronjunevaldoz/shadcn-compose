---
name: kotlin-multiplatform-adaptive-layout
description: >-
  Adaptive UI for Kotlin Multiplatform — WindowSizeClass-driven layouts that
  respond correctly to Compact (phone), Medium (tablet), and Expanded (desktop)
  breakpoints. Covers list-detail splits, adaptive navigation (bottom bar → rail →
  drawer), single-source WindowSizeClass propagation, and Roborazzi tests for each
  breakpoint. Enforces cross-session pattern consistency so every screen in the project
  uses the same adaptive strategy.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-26'
  keywords:
    - adaptive layout
    - WindowSizeClass
    - WindowWidthSizeClass
    - responsive UI
    - Compact Medium Expanded
    - ListDetailPane
    - adaptive navigation
    - navigation rail
    - navigation drawer
    - tablet layout
    - desktop layout
    - split screen
    - list detail
    - side panel
    - calculateWindowSizeClass
---

## When to Use This Skill

Use when:
- Implementing any screen that needs a different layout on phone vs tablet vs desktop
- Adding list-detail (master-detail) split navigation
- Adapting navigation chrome (bottom bar → rail → drawer)
- A new session is started after adaptive layout was already established — check the
  existing pattern before writing new screens
- The reviewer flags `[ADAPTIVE] inconsistency` — one screen uses WindowSizeClass, a
  newly added screen does not

**Trigger keywords:** adaptive layout, responsive layout, WindowSizeClass, ListDetailPane,
tablet layout, desktop layout, mobile layout, phone layout, split screen, detail split,
list detail, side panel, navigation rail, navigation drawer, window size,
Compact Medium Expanded, adaptive navigation, calculateWindowSizeClass,
different layout phone tablet, different layout phone desktop, responsive composable,
multi-pane layout, master detail KMP, pane layout, screen size breakpoint,
layout per screen size, detect all screen layouts, all screen sizes,
page layout, layout consistency, consistent layout, screen consistency,
redesign page, layout redesign, page consistency, uniform layout, layout patterns.

**Freshness rule:** `material3-adaptive` is still evolving — recheck the API when upgrading
`androidx.compose.material3.adaptive`. `calculateWindowSizeClass()` moved packages between
CMP releases; verify the import against the current version in `libs.versions.toml`.

## Compose Layout Fundamentals

- https://developer.android.com/develop/ui/compose/layouts — official Compose layout overview; use this before picking a container or custom pattern
- https://developer.android.com/develop/ui/compose/layouts/adaptive/get-started-with-adaptive-apps — official adaptive-app entry point for phones, tablets, foldables, and other form factors
- https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts — canonical adaptive layout patterns for list-detail, supporting pane, and related responsive structures
- https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation — adaptive navigation patterns for bottom bar, rail, drawer, and multi-pane navigation shells
- https://developer.android.com/develop/ui/compose/layouts/custom — official custom-layout guide; use when built-in containers are not enough
- `Layout fundamentals` in the Compose docs covers the base mental model for `Box`, `Row`, `Column`, `Spacer`, modifiers, and constraints
- `Layout containers` and `Advanced and custom layouts` in the same doc tree cover lazy lists/grids, flow layouts, custom layouts, alignment lines, and intrinsic measurement

---

## Recommendation First

Calculate `WindowSizeClass` **once** at the app root and pass it down as a parameter.
Never call `calculateWindowSizeClass()` inside a leaf composable — it re-reads window
metrics on every recomposition and makes components impossible to preview or test in
isolation.

**Why:**
- A single read at the root means every screen snapshot test can supply a fake
  `WindowSizeClass` — no device, no emulator
- Passing as a parameter makes the layout decision visible in the call site — you can
  read a composable signature and immediately know it is adaptive
- Centralising the calculation prevents two screens from disagreeing on the breakpoint
  because they each read at a different recomposition moment

---

## Cross-session pattern consistency

Before implementing a new screen, run:

```bash
grep -r "WindowSizeClass\|calculateWindowSizeClass\|WindowWidthSizeClass" \
  <project_root>/*/src --include="*.kt" -l
```

If any file matches, the adaptive pattern is **already established**. Read one of those
files and replicate the exact same:
- `WindowSizeClass` parameter name and position in the screen signature
- Breakpoint switch structure (`when (windowSizeClass.widthSizeClass)`)
- Helper layout composable naming (`FooContentCompact`, `FooContentMedium`,
  `FooContentExpanded`)

Never introduce a second pattern in the same project.

If nothing matches, establish the pattern using the template below and update
`.claude/pipeline-context.json`:

```json
"adaptive_layout_established": true,
"adaptive_layout_root_file": "<path to the first screen that established the pattern>"
```

### Retrofitting an existing project

If the project already has many screens **without** `WindowSizeClass`, retrofitting all
of them in one session is impractical. Use migration mode to avoid being blocked:

1. Set `adaptive_layout_migration_mode: true` in `.claude/pipeline-context.json` and commit it
2. The reviewer will warn (not block) on pre-existing screens and only enforce the full
   rule on screens created or modified in the current session
3. Track the remaining screens as a follow-up ticket
4. Once all screens are migrated, set `adaptive_layout_migration_mode: false` and commit

---

## Core pattern — screen structure

```kotlin
// FooScreen.kt  (in :ui)
@Composable
fun FooScreen(
    windowSizeClass: WindowSizeClass,          // passed from nav host — never calculated here
    viewModel: FooViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FooContent(
        state = state,
        windowSizeClass = windowSizeClass,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun FooContent(
    state: FooContract.State,
    windowSizeClass: WindowSizeClass,
    onIntent: (FooContract.Intent) -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact  -> FooContentCompact(state, onIntent)
        WindowWidthSizeClass.Medium   -> FooContentMedium(state, onIntent)
        WindowWidthSizeClass.Expanded -> FooContentExpanded(state, onIntent)
        else                          -> FooContentCompact(state, onIntent)
    }
}
```

---

## List-detail split (Expanded)

```kotlin
@Composable
fun FooContentExpanded(
    state: FooContract.State,
    onIntent: (FooContract.Intent) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        FooList(
            items = state.items,
            selectedId = state.selectedId,
            onSelect = { onIntent(FooContract.Intent.SelectItem(it)) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        VerticalDivider()
        FooDetail(
            item = state.selectedItem,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun FooContentMedium(
    state: FooContract.State,
    onIntent: (FooContract.Intent) -> Unit
) {
    // Medium: show list; tapping an item opens detail as a pane or bottom sheet
    Row(modifier = Modifier.fillMaxSize()) {
        FooList(
            items = state.items,
            selectedId = state.selectedId,
            onSelect = { onIntent(FooContract.Intent.SelectItem(it)) },
            modifier = Modifier.weight(1f)
        )
        state.selectedItem?.let { item ->
            FooDetail(item = item, modifier = Modifier.weight(1f))
        }
    }
}
```

---

## Adaptive navigation

```kotlin
@Composable
fun AdaptiveNavScaffold(
    windowSizeClass: WindowSizeClass,
    destinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Scaffold(
            bottomBar = {
                AppBottomBar(destinations, currentDestination, onNavigate)
            },
            content = content
        )
        WindowWidthSizeClass.Medium -> Row(Modifier.fillMaxSize()) {
            AppNavigationRail(destinations, currentDestination, onNavigate)
            Scaffold(content = content)
        }
        WindowWidthSizeClass.Expanded -> Row(Modifier.fillMaxSize()) {
            AppPermanentDrawer(destinations, currentDestination, onNavigate)
            Scaffold(content = content)
        }
        else -> Scaffold(
            bottomBar = { AppBottomBar(destinations, currentDestination, onNavigate) },
            content = content
        )
    }
}
```

---

## Passing WindowSizeClass from the NavHost

```kotlin
// App.kt — the single calculation point
@Composable
fun App() {
    val windowSizeClass = calculateWindowSizeClass()
    AppTheme(darkTheme = isSystemInDarkTheme()) {
        AdaptiveNavScaffold(windowSizeClass = windowSizeClass, ...) {
            NavHost(...) {
                composable<HomeRoute> {
                    HomeScreen(windowSizeClass = windowSizeClass)
                }
                composable<ProfileRoute> {
                    ProfileScreen(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
```

---

## Roborazzi tests — required for every adaptive screen

Every screen with adaptive layout **must** have Roborazzi tests for all three breakpoints
and both themes. Minimum required captures per screen:

```kotlin
@OptIn(ExperimentalTestApi::class)
class FooContentScreenshotTest {

    private val compactWindowSize = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))
    private val mediumWindowSize  = WindowSizeClass.calculateFromSize(DpSize(700.dp, 800.dp))
    private val expandedWindowSize = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 900.dp))
    private val defaultState = FooContract.State(/* default */)

    @Test fun foo_compact_light() {
        captureRoboImage("foo_compact_light.png") {
            AppTheme(darkTheme = false) {
                FooContent(defaultState, compactWindowSize, {})
            }
        }
    }

    @Test fun foo_compact_dark() {
        captureRoboImage("foo_compact_dark.png") {
            AppTheme(darkTheme = true) {
                FooContent(defaultState, compactWindowSize, {})
            }
        }
    }

    @Test fun foo_expanded_light() {
        captureRoboImage("foo_expanded_light.png") {
            AppTheme(darkTheme = false) {
                FooContent(defaultState, expandedWindowSize, {})
            }
        }
    }

    @Test fun foo_expanded_dark() {
        captureRoboImage("foo_expanded_dark.png") {
            AppTheme(darkTheme = true) {
                FooContent(defaultState, expandedWindowSize, {})
            }
        }
    }
}
```

Medium is optional if it renders identically to Compact or Expanded. Compact + Expanded
is the required minimum.

---

## Common Anti-Patterns

- **`calculateWindowSizeClass()` inside a leaf composable** — re-reads window metrics on
  every recomposition and cannot be overridden in tests; always call once at the app root
- **Hardcoded dp breakpoints** — `if (screenWidth > 600.dp)` instead of
  `WindowWidthSizeClass.Medium`; magic numbers diverge from the system breakpoints and
  break on unusual screen sizes
- **`LocalConfiguration.current.screenWidthDp` for breakpoints** — not multiplatform;
  unavailable on Desktop and iOS; use `WindowSizeClass` throughout
- **Only testing Compact** — medium and expanded layouts diverge significantly; a
  Roborazzi test for Expanded catches list-detail regressions that phone-only tests miss
- **No dark mode variant for adaptive screenshots** — a muted text color readable in
  light mode on a white list-detail pane becomes invisible in dark mode; capture both
- **Inconsistent pattern across screens** — one screen uses `WindowSizeClass`, another
  uses hardcoded dp checks; always grep for existing pattern before implementing a new screen

---

## Related Skills

- `kotlin-multiplatform-design-system` — `AppTheme` drives the dark/light mode toggle
  that adaptive screenshots depend on; tokens must have both light and dark variants
- `kotlin-multiplatform-navigation` — the nav host is where `WindowSizeClass` is
  distributed to each screen route
- `kotlin-multiplatform-roborazzi` — required for capturing adaptive layout goldens;
  `WindowSizeClass.calculateFromSize(DpSize(...))` is the test-time substitute for a real device
- `kotlin-multiplatform-mvi` — `FooContent` is a pure MVI content composable; the
  adaptive switch lives inside it, not in the ViewModel

---

## Output Style

When implementing adaptive layout, respond in this order:
1. **Grep result** — confirm whether adaptive pattern already exists in the project
2. **Breakpoint strategy** — which layout differences exist at each breakpoint
3. **Screen structure** — `FooScreen` + `FooContent` + three layout composables
4. **Navigation** — whether `AdaptiveNavScaffold` needs updating for the new screen
5. **Roborazzi tests** — compact+dark, compact+light, expanded+dark, expanded+light
6. **Pipeline-context update** — set `adaptive_layout_established: true` if this is the first

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
| 2026-06-26 | Added official Compose layouts overview to the skill’s reference layer and called out the layout fundamentals / layout containers / custom layouts doc family for base container and constraint guidance. |
| 2026-06-26 | Added the official adaptive apps getting-started page and the Compose custom layouts guide as direct reference links for adaptive and custom layout work. |
| 2026-06-26 | Added canonical adaptive layouts and adaptive navigation reference pages so the skill now points to the full official adaptive layout path. |
