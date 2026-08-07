---
name: kmp-compose-design-system-extended
description: >
  Extends :core:designsystem (from kmp-compose-design-system) with 28
  production-ready components using the Compose Styles API. Covers: Icon, IconButton,
  Label, Separator, Avatar, TopAppBar, NavigationBar, Tabs, Checkbox, RadioButton,
  Switch, Slider, Select/Dropdown, Progress (linear + circular), Skeleton, Spinner,
  Alert, Toast/Snackbar system (AppToastHostState + Scaffold slot), Dialog, AlertDialog,
  Sheet (BottomSheet), Tooltip, Popover, Accordion/Collapsible, ScrollArea (Desktop-only
  scrollbar via expect/actual), ResizablePanelGroup (draggable divider). All components
  built on CMP primitives (no Material3). "App" is a placeholder prefix — see the base
  skill's Step 0 for how it is resolved from the project name
  (scripts/derive_component_prefix.py). Requires kmp-compose-design-system skill.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-08'
  keywords:
    - design system extended
    - Dialog
    - BottomSheet
    - Toast
    - Snackbar
    - Tabs
    - TopAppBar
    - NavigationBar
    - Checkbox
    - Switch
    - Slider
    - Select
    - Dropdown
    - Progress
    - Skeleton
    - Spinner
    - Tooltip
    - Popover
    - Accordion
    - Collapsible
    - Avatar
    - Separator
    - Icon
    - Compose Styles API
    - CMP
    - no Material
    - ScrollArea
    - Scrollbar
    - VerticalScrollbar
    - ResizablePanelGroup
    - resizable panel
    - draggable divider
    - drag to resize
---

## When to Use This Skill

Use **after** `kmp-compose-design-system` has been applied. Use when the user asks to:
- Add Dialog, BottomSheet, Toast/Snackbar, Tabs, TopAppBar, or BottomNav
- Add form controls: Checkbox, RadioButton, Switch, Slider, Select/Dropdown
- Add loading states: Progress, Skeleton, Spinner
- Add Tooltip, Popover, Accordion/Collapsible, Avatar, Separator
- Complete the design system for production use

**Trigger keywords:** dialog, bottom sheet, toast, snackbar, tabs, top app bar,
bottom navigation, checkbox, radio, switch, slider, select, dropdown, progress bar,
loading, skeleton, spinner, tooltip, popover, accordion, collapsible, avatar,
divider, separator, icon button, form label, extended design system,
redesign, visual consistency, UI components, component library, page components,
add components, component set, UI kit, component design, redesign page,
button, component, use component, add button, create component,
show dialog, show toast, loading state, empty state, error state,
circular progress, progress ring, determinate progress, indeterminate progress.

**Freshness rule:** `@ExperimentalStylesApi` and CMP primitive APIs change between releases —
recheck the Compose docs and apply the same freshness check as `kmp-compose-design-system`.

---

## Recommendation First

Default to **using a pre-built extended component before building a custom one**.

Why:
- all 28 components are built on CMP primitives with no Material dependency — they are safe to
  use alongside the base design system
- they follow the same sealed variant pattern as the core components, so the token layer stays consistent
- building a custom component takes longer and may drift from the design system tokens

Only build a custom component when none of the 27 extended components fit the design requirement,
and apply the same `@ExperimentalStylesApi` token pattern as the core system.

---

## Prerequisites

- `kmp-compose-design-system` skill already applied (tokens, AppTheme, StyleScopeExtensions, 6 core components present)
- `:core:designsystem` module exists with `GROUP_ID.core.designsystem` package
- The project's component prefix already resolved via the base skill's **Step 0** —
  `App` below is the same placeholder token (`AppIconButton` → `GuildBaseIconButton`,
  `AppToastHost` → `GuildBaseToastHost`, etc.), never a hardcoded literal

---

## Ownership Model

> **Skill-owned.** All extended components are updateable via `/kmp-update-design-system`.
> Project-owned files (`tokens/`, `theme/`) are never touched.

## Component Overview

| Group | Components | Stability |
|---|---|---|
| Primitives | `AppIcon`, `AppIconButton`, `AppLabel`, `AppSeparator` | **Stable** |
| Display | `AppAvatar`, `AppSpinner`, `AppSkeleton`, `AppProgress`, `AppCircularProgress` | **Stable** |
| Navigation | `AppTopAppBar`, `AppNavigationBar`, `AppScaffold` | **Stable** |
| Tabs | `AppTabs` | **Stable** |
| Form Controls | `AppCheckbox`, `AppRadioButton`, `AppSwitch`, `AppSlider` | **Stable** |
| Form Controls | `AppSelect` | **Experimental** — API may change |
| Feedback | `AppAlert`, `AppToastHost` (with `AppToastHostState` + `AppScaffold`) | **Stable** |
| Overlays | `AppDialog`, `AppAlertDialog`, `AppSheet` | **Stable** |
| Overlays | `AppTooltip`, `AppPopover` | **Experimental** — positioning varies by platform |
| Expandable | `AppAccordion` | **Experimental** — animation API in flux |

**Stability tiers:**
- **Stable** — API locked; breaking changes come with a migration note in the Changelog.
- **Experimental** — API may change between skill versions; review diffs before accepting updates.

### Style API coverage

Not every component should expose a `style: Style` override — see the base skill's
Component API Placement table. This is the honest per-component status so the audit
doesn't flag correctly-exempt components as gaps:

| Component | Style API status | Why |
|---|---|---|
| `AppIconButton` | ✅ Wired | Interactive leaf control — `rememberUpdatedStyleState` + `styleable` |
| `AppAvatar` | ✅ Wired | Static leaf control — `style` escape hatch for one-off overrides (e.g. status ring) |
| `AppIcon`, `AppLabel`, `AppSeparator` | ⚠️ Not yet wired | Simple data+param leaf controls; a `style` escape hatch would still be valid — candidates for a future pass |
| `AppSpinner` | ✅ Correctly exempt | Infinite rotation animation — Styles API does not support infinite animations (see `references/compose-styles-api-reference.md` §10); uses `rememberInfiniteTransition` instead, as documented in its own docstring |
| `AppSkeleton`, `AppProgress` | ⚠️ Not yet wired | Same infinite-animation constraint may apply to the shimmer/indeterminate variants — verify per-variant before wiring |
| `AppCheckbox`, `AppRadioButton`, `AppSwitch` | ⚠️ Not yet wired | Custom Canvas-drawn glyphs (checkmark, dot, thumb) sit outside the Style property set (no arbitrary path-drawing property) — per Styles-vs-Modifiers guidance this is legitimately Modifier/Canvas territory; only the container chrome (background/border color per checked/enabled state) is a real Style candidate, not yet extracted |
| `AppSlider` | ✅ Correctly exempt | Continuous drag value, not a discrete interaction state — doesn't fit the StyleState model |
| `AppSelect` | ⚠️ Not yet wired | Leaf control candidate — dropdown chrome (background/border/shape) is stylable |
| `AppTopAppBar`, `AppNavigationBar`, `AppScaffold`, `AppTabs` | ✅ Correctly exempt | Slot API / app-shell chrome per the base skill's Component API Placement table — caller owns content, shell stays fixed |
| `AppAlert`, `AppToastHost` | ⚠️ Not yet wired | Variant-driven leaf controls (info/success/warning/error) — good Style candidates |
| `AppCircularProgress` | ⚠️ Not yet wired | Same status as its sibling `AppProgress` (linear) — track/fill colors are plain params, not yet Style-driven |
| `AppDialog`, `AppAlertDialog`, `AppSheet`, `AppTooltip`, `AppPopover` | ✅ Correctly exempt | Slot API — overlay chrome, not a themed variant leaf control |
| `AppAccordion` | ⚠️ Not yet wired | Animation API is still in flux (Experimental tier); revisit once stabilized |

**Reading this table:** ✅ means the current state is correct and should not be flagged.
⚠️ means a real gap — a future pass should extract the container chrome (background,
border, shape) into a `Style` value and expose a `style: Style = Style` parameter,
following the `AppAvatar` pattern above for static components or the base skill's
`AppButton` pattern (`rememberUpdatedStyleState` + custom state keys) for interactive ones.

---

## Implementation Steps

Each step's full component code lives in its own reference file — load only the
step(s) relevant to the current task, not all of them.

| Step | Reference file | Components |
|---|---|---|
| 1. Styles | `references/step1-styles.md` | `CheckboxStyles`, `SwitchStyles`, `TabStyles`, `AlertStyles` |
| 2. Primitives | `references/step2-primitives.md` | `AppIcon`, `AppIconButton`, `AppLabel`, `AppSeparator`, `AppAvatar` |
| 3. Loading states | `references/step3-loading-state.md` | `AppSpinner`, `AppProgress`, `AppCircularProgress`, `AppSkeleton` |
| 4. Navigation | `references/step4-navigation.md` | `AppTopAppBar`, `AppNavigationBar`, `AppTabs` |
| 5. Form controls | `references/step5-form-controls.md` | `AppCheckbox`, `AppRadioButton`, `AppSwitch` |
| 5b. Form controls (cont.) | `references/step5b-slider-select.md` | `AppSlider`, `AppSelect` |
| 6. Feedback | `references/step6-feedback.md` | `AppAlert`, `AppToast`, `AppScaffold` |
| 7. Overlays | `references/step7-overlays.md` | `AppDialog`, `AppSheet`, `AppTooltip`, `AppPopover` |
| 8. Expandable | `references/step8-expandable.md` | `AppAccordion`, `AppScrollArea`, `AppResizablePanelGroup` |

Implement steps in order — later steps' usage examples assume earlier
components exist (e.g. Step 6's `AppScaffold` composes `AppToastHost` from
the same step; Step 9 below wires it at the app entry point).

---

## Step 9: Wire AppScaffold at entry points

Replace existing entry-point `AppTheme` wrappers:

```kotlin
// androidApp/src/main/kotlin/.../MainActivity.kt
setContent {
    AppTheme(darkTheme = isSystemInDarkTheme()) {
        val toastState = remember { AppToastHostState() }
        AppScaffold(toastHostState = toastState) { _ ->
            AppNavHost()
        }
    }
}

// Anywhere in the app — show a toast:
val toastState = LocalAppToastHostState.current
LaunchedEffect(saveResult) {
    if (saveResult.isSuccess) {
        toastState.show("Saved successfully", variant = AppToastVariant.Success)
    }
}
```

---

## Step 10: Usage examples

### Form screen

```kotlin
@Composable
fun ProfileForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(true) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val toast = LocalAppToastHostState.current

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Name", required = true)
            AppTextField(value = name, onValueChange = { name = it }, placeholder = "Your name")
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Email")
            AppTextField(value = email, onValueChange = { email = it }, placeholder = "you@example.com")
        }
        AppSwitch(checked = notifications, onCheckedChange = { notifications = it }, label = "Email notifications")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Digest frequency")
            AppSelect(
                options = listOf("Daily", "Weekly", "Monthly"),
                selected = frequency,
                onSelect = { frequency = it },
                placeholder = "Select frequency",
            )
        }

        AppProgress(progress = 0.65f)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(onClick = { toast.show("Profile saved", variant = AppToastVariant.Success) }) {
                AppText("Save changes")
            }
            AppButton(onClick = { showDeleteDialog = true }, variant = ButtonVariant.Destructive) {
                AppText("Delete account")
            }
        }
    }

    if (showDeleteDialog) {
        AppAlertDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { deleteAccount() },
            title = "Delete account",
            description = "This action cannot be undone. All your data will be permanently removed.",
            confirmText = "Delete account",
        )
    }
}
```

### Settings page with Accordion

```kotlin
@Composable
fun SettingsPage() {
    AppAccordion(
        items = listOf(
            AccordionItem("Privacy") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppSwitch(checked = true, onCheckedChange = {}, label = "Show profile publicly")
                    AppSwitch(checked = false, onCheckedChange = {}, label = "Allow data analytics")
                }
            },
            AccordionItem("Notifications") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppCheckbox(checked = true, onCheckedChange = {}, label = "Push notifications")
                    AppCheckbox(checked = false, onCheckedChange = {}, label = "Email digest")
                }
            },
            AccordionItem("Appearance") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText("Theme", style = AppTextStyle.LabelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("System", "Light", "Dark").forEach { opt ->
                            AppChip(label = opt, selected = opt == "System", onClick = {})
                        }
                    }
                }
            },
        )
    )
}
```

---

## Guidelines

- **`AppScaffold` is required for Toast** — never place `AppToastHost` inside a `Box` without scaffold-level z-ordering; toasts will be clipped by parent composables
- **Dialog and Sheet use `androidx.compose.ui.window.Dialog`** — works across all CMP targets; `Popup`-based overlays have WasmJs viewport positioning issues
- **Tooltip is Desktop-first** — hover state is only available on Desktop/Web pointer devices; on touch targets the tooltip is simply never shown
- **Slider uses `pointerInput`** not `Modifier.draggable` — `draggable` only tracks one axis and lacks tap-to-seek behavior
- **Checkbox/RadioButton draw their own indicator via `Canvas`** — no dependency on Material icons or drawables
- **Accordion chevron uses `graphicsLayer { rotationZ }`** — runs on the draw layer, skips Composition on rotation
- **Switch thumb uses `animateDpAsState` with spring** — spring physics handles interruptions if toggled mid-animation
- **`AppSelect` uses `AnimatedVisibility` + z-index** — not a `Popup`, so it stacks in document order; use `zIndex` on the parent if other composables need to render on top

---

## Verification

1. `./gradlew :core:designsystem:compileCommonMainKotlinMetadata` — all 28 components compile
2. Show/dismiss `AppDialog` — appears centered, scrim dismisses on outside tap
3. Show `AppSheet` — slides in from bottom, drag handle visible, scrim dismisses
4. `toastState.show("Test")` — toast appears bottom-center, auto-dismisses after 3s
5. `AppTabs` — all 3 variants render, `AnimatedContent` transitions on tab switch
6. `AppCheckbox` + `AppSwitch` — animated state changes work
7. `AppAccordion(multiExpand = false)` — only one section open at a time
8. `AppSelect` — dropdown opens/closes, selected value updates, keyboard accessible
9. Desktop hover on `AppIconButton` inside `AppTooltip` — tooltip appears above, does not blink
10. `AppResizablePanelGroup` — dragging the divider resizes both panes smoothly, clamped to `minWeight`/`maxWeight`
11. `AppScrollArea` on Desktop — scrollbar sits on the trailing edge, thumb drags and tracks scroll.
    On Android/iOS there is no visible thumb. That is expected, not a bug.
12. `./gradlew :desktopApp:run` — all components render correctly on JVM target

---

## Testing

```kotlin
// Every component in the extended system needs: light + dark screenshots + 1 interaction test
@get:Rule val composeRule = createComposeRule()

// --- AppButton ---
@Test fun `app_button_primary_light screenshot`() {
    captureRoboImage("dsx_button_primary_light.png") {
        AppTheme(darkTheme = false) { AppButton(text = "Continue", onClick = {}) }
    }
}

@Test fun `app_button_primary_dark screenshot`() {
    captureRoboImage("dsx_button_primary_dark.png") {
        AppTheme(darkTheme = true) { AppButton(text = "Continue", onClick = {}) }
    }
}

@Test fun `app_button_fires_onclick`() {
    var clicked = false
    composeRule.setContent { AppTheme { AppButton(text = "Go", onClick = { clicked = true }) } }
    composeRule.onNodeWithText("Go").performClick()
    assertTrue(clicked)
}

// --- AppTextField ---
@Test fun `app_text_field_default screenshot`() {
    captureRoboImage("dsx_text_field_default.png") {
        AppTheme { var t by remember { mutableStateOf("") }; AppTextField(value = t, onValueChange = {}, label = "Email") }
    }
}

@Test fun `app_text_field_onValueChange fires on input`() {
    var value = ""
    composeRule.setContent {
        AppTheme { AppTextField(value = value, onValueChange = { value = it }, label = "Name") }
    }
    composeRule.onNodeWithText("Name").performTextInput("Alice")
    assertEquals("Alice", value)
}

// --- AppIcon / AppIconButton ---
@Test fun `app_icon_button_fires_onclick`() {
    var clicked = false
    composeRule.setContent {
        AppTheme {
            AppIconButton(onClick = { clicked = true }, contentDescription = "Close") {
                AppIcon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
    composeRule.onNodeWithContentDescription("Close").performClick()
    assertTrue(clicked)
}
```

---

## Common Anti-Patterns

- using an extended component before applying `kmp-compose-design-system` — tokens are missing
- overriding component internals via `Modifier` hacks instead of adding a variant — breaks the style contract
- building a custom sheet or dialog without checking `AppBottomSheet` / `AppDialog` first
- mixing Material3 components with extended design system components — creates token conflicts
- creating an `AppToastHostState` without wiring it into the `AppScaffold` slot — toasts silently do nothing
- swapping between two icon composables (`if (isExpanded) IconUp else IconDown`) for a collapsible chevron instead of rotating one icon via `Modifier.graphicsLayer { rotationZ = ... }` — if the two icons' intrinsic sizes differ even slightly, the trigger row remeasures and visibly shifts position on toggle; caught by the audit's `toggle icon swap instead of rotation [MEDIUM]`
- toggling collapsible content with a bare `if (isExpanded) { ... }` instead of `AnimatedVisibility`/`.animateContentSize()` — the instant layout snap reads as the trigger button itself moving, not just the content appearing; caught by `bare conditional collapse [MEDIUM]`
- stacking `.animateContentSize()` on the container **and** `AnimatedVisibility` on the same collapsible content — both animate the size change independently at slightly different rates, and the visible symptom is the collapsible content briefly overlapping the sibling below it during the transition. Pick one: `AnimatedVisibility` alone already reflows siblings correctly for expand/shrink; only add `.animateContentSize()` on a container whose size changes for a reason `AnimatedVisibility` doesn't cover (e.g. text reflow, not a mount/unmount)
- showing a `Popup`-based tooltip the instant `isHovered` flips true, with no delay — `popupContentSize` is `IntSize.Zero` on the Popup's first frame, so the position calculation can briefly land at/near the anchor's own bounds, un-hovering the anchor and hiding the tooltip it just showed — the classic tooltip blink. Debounce with `LaunchedEffect(isHovered) { delay(delayMillis); showTooltip = true }` and set `PopupProperties(focusable = false)` so the popup never steals hover/focus from the anchor — see `AppTooltip`
- animating `borderWidth`/`borderBottomWidth` in a `focused {}`/`selected {}` Style block instead of only `borderColor` — this is the Compose equivalent of reaching for a CSS `ring` when a plain rule solves it: reserve the final border width at rest (`borderColor(Color.Transparent)` if there's no border at rest) and animate color only, so focusing/selecting never re-measures the component; caught by the audit's `focused state animates border width [MEDIUM]` — see the base skill's Style Rules → "Ring vs border"
- placing `AppVerticalScrollbar` as a normal member of the same `Column`/`Row` as the scrollable content instead of overlaid in a `Box` with `Modifier.align(Alignment.CenterEnd).fillMaxHeight()` — it renders in the wrong place or pushes the content over instead of overlaying it
- assuming `VerticalScrollbar`/`rememberScrollbarAdapter` works on Android/iOS — it's Desktop/Web only in Compose Multiplatform's foundation library; wire it through an `expect`/`actual` and no-op on touch platforms rather than shipping a broken call on mobile
- using `Modifier.draggable` for `AppResizablePanelGroup`'s divider instead of `pointerInput` + `detectDragGestures` — same reasoning as `AppSlider`: `draggable` only tracks one axis and doesn't give you the delta needed to clamp the resulting weight to `minWeight`/`maxWeight`
- letting a resizable panel's weight drift outside a sane range (a pane collapsing to zero width or swallowing the whole row) — always `.coerceIn(minWeight, maxWeight)` the computed weight, never assign the raw drag delta directly

Check the component list in this skill before building a custom alternative.

---

## Related Skills

- `kmp-compose-design-system` — the token and component foundation this skill extends
- `kmp-compose-slot-api` — slot APIs used by `AppDialog`, `AppBottomSheet`, and `AppScaffold`
- `kmp-compose-preview-driven-development` — Desktop previews for each extended component variant
- `kmp-shared-resources` — icons and images loaded inside extended components via `Res`

---

## Output Style

When asked about extended design system components, respond in this order:
1. recommendation (which component to use and its variant)
2. code snippet (component with its required props)
3. why that component fits the use case
4. main alternative (build from scratch, use Material3)

Assume `kmp-compose-design-system` is already applied. Use the user's variant names and theme tokens when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split `references/step5-form-controls.md` (546 lines, `AppCheckbox`/`AppRadioButton`/`AppSwitch`/`AppSlider`/`AppSelect`) into two files — `step5-form-controls.md` keeps `AppCheckbox`/`AppRadioButton`/`AppSwitch`, new `step5b-slider-select.md` gets `AppSlider`/`AppSelect`. Both now under 500 lines. Triggered by a new `oversized_reference_md` check in `scan_skill_issues.py` — the 500-line agentskills.io guideline, applied one level down to individual `references/*.md` files, not just `SKILL.md` itself. |
| 2026-08-04 | Split Steps 1-8 (all 28 components' Kotlin code, 2676 lines) out of SKILL.md into 8 `references/step*.md` files, one per step, with a new "Implementation Steps" pointer table telling the agent which file covers which components — SKILL.md dropped from 3101 to 442 lines, clearing the agentskills.io 500-line recommendation and removing this skill's `oversized_skill_md` known-debt entry (KI-008). No content removed, only relocated. Also fixed `kmp-audit/scripts/audit_skills_repo.py`'s design-system content checks (`_check_design_system`), which only scanned `SKILL.md` text — they now also scan `references/*.md` so the static-`AppTheme`-access and hardcoded-`.dp` checks keep seeing the actual component code after the split. |
| 2026-07-08 | Added 2 new components (26 → 28), closing gaps found via real shadcn-compose bug reports: `AppScrollArea`/`AppVerticalScrollbar` (expect/actual — Desktop wires the real `VerticalScrollbar`/`rememberScrollbarAdapter`, Android/iOS intentionally no-op since Compose Multiplatform's foundation library has no scrollbar implementation for those targets; verified against a real-world CMP app's identical expect/actual shape) and `AppResizablePanelGroup` (draggable divider via `pointerInput`/`detectDragGestures`, weight clamped to `minWeight`/`maxWeight`). New drag-interaction and scrollbar-positioning anti-patterns; fixed 2 pre-existing stale "27 components" counts left over from an earlier 27→26 correction. |
| 2026-07-08 | Fixed a real hover-flicker bug in `AppTooltip`: the `Popup` was shown the instant `isHovered` flipped true, with no debounce — `popupContentSize` is `IntSize.Zero` on the Popup's first frame, so the position calculation could briefly land near the anchor's own bounds, un-hovering it and hiding the tooltip it just showed. Fixed with a `delayMillis`-debounced `LaunchedEffect` decoupling `showTooltip` from raw `isHovered`, and `PopupProperties(focusable = false)` so the popup never steals focus/hover from the anchor. Added 3 anti-patterns (double-animation collapsible overlap, tooltip blink, focused-state border width) from real shadcn-compose bug reports. |
| 2026-07-08 | Added 2 anti-patterns for collapsible/toggle layout stability: icon-swap chevrons (instead of `graphicsLayer { rotationZ }` rotation) and bare `if (isExpanded) { ... }` collapse (instead of `AnimatedVisibility`/`.animateContentSize()`) — both can visibly shift a trigger button's position on toggle. Backed by new `kmp-audit` detectors `toggle icon swap instead of rotation [MEDIUM]` and `bare conditional collapse [MEDIUM]`; `AppAccordion` already followed the correct pattern and is cited as the reference implementation. |
| 2026-07-05 | Completeness audit found 3 real gaps: (1) the Toast/Snackbar subsystem (`ToastHost`, `ToastHostState`, `ToastData`, `ToastVariant`, `LocalToastHostState`) never carried the `App` prefix at all — renamed to `AppToastHost`/`AppToastHostState`/`AppToastData`/`AppToastVariant`/`LocalAppToastHostState` and fixed a resulting double-prefix typo in Common Anti-Patterns; (2) the skill promised "Progress (linear + circular)" but only linear existed — added `AppCircularProgress` (determinate ring + indeterminate rotating arc, same infinite-animation constraint as `AppSpinner`) and fixed `AppProgress`'s docstring, which falsely claimed it delegated to `AppSpinner` for the indeterminate case; (3) description claimed "27 components" — corrected to the accurate count (26). Added the `App`-is-a-placeholder cross-reference to Prerequisites and the frontmatter description, matching the base skill's Step 0. |
| 2026-07-05 | Added "Style API coverage" table classifying all 24 components (wired / correctly slot-API-exempt / correctly exempt due to a real limitation / not-yet-wired) so the audit's Style-compliance detectors don't flag legitimate exemptions as gaps. Wired `AppAvatar` (was importing `Style`/`MutableStyleState`/`styleable` unused — dead code from an unfinished wiring attempt): added a `style: Style = Style` escape hatch and an `avatarDefaultStyle` for its background/shape. |
| 2026-07-05 | Fixed `AppIconButton`: `styleState.enabled = enabled` used the wrong property name and a non-idiomatic construction — corrected to `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` per the official Compose Styles API docs (see base skill's `references/compose-styles-api-reference.md`). |
| 2026-06-22 | Renamed all `TextStyle.` references → `AppTextStyle.` to align with base skill rename. |
| 2026-06-06 | Initial release. |
