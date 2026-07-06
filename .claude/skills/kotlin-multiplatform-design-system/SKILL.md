---
name: kotlin-multiplatform-design-system
description: >
  Scaffolds a custom Compose Multiplatform design system in :core:designsystem using
  the new Compose Styles API from Android docs (developer.android.com/develop/ui/compose/styles,
  @ExperimentalStylesApi). This is the styling layer, not the Slot API. Generates:
  color/typography/shape/spacing
  tokens, AppTheme with light/dark support, StyleScope extensions for token access,
  shadcn-inspired sealed variant systems (ButtonVariant, CardVariant, BadgeVariant,
  ChipVariant, TextFieldVariant), AppTextStyle enum (no Compose TextStyle collision),
  and 6 core components (AppButton, AppCard, AppTextField, AppChip, AppBadge, AppText)
  built on BasicXxx CMP primitives. "App" is a placeholder prefix derived from the
  project's actual name (scripts/derive_component_prefix.py), not a hardcoded literal.
  No Material dependency — fully custom, fully owned.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-07-05'
  keywords:
    - design system
    - Compose Styles API
    - AppTheme
    - custom theme
    - design tokens
    - shadcn
    - ButtonVariant
    - Kotlin Multiplatform
    - Compose Multiplatform
    - CMP
    - ExperimentalStylesApi
    - dark mode
    - token system
    - UI components
    - core:designsystem
    - rememberUpdatedStyleState
    - StyleStateKey
    - Style vs Modifier
    - component prefix
    - COMPONENT_PREFIX
    - rename App prefix
    - custom prefix design system
    - project-based prefix
---

## When to Use This Skill

Use this skill when the user asks to:
- Set up a custom design system, theme, or component library in a KMP project
- Avoid Material Design and build unstyled/custom components
- Use the new Compose Styles API (`@ExperimentalStylesApi`)
- Create reusable UI components with a variant system (like shadcn)
- Add dark mode support via custom tokens
- Wire AppTheme, tokens, or custom Composables into `:core:designsystem`

**Trigger keywords:** design system, custom theme, AppTheme, design tokens,
ButtonVariant, shadcn KMP, Compose Styles, ExperimentalStylesApi, custom components,
unstyled components, dark mode tokens, color scheme, no Material,
typography system, spacing tokens, custom button style, Material3 alternative,
app theme setup, brand colors, design token system, custom typography,
redesign, visual consistency, UI consistency, design consistency, page design,
screen design, UI look and feel, consistent styling, style guide, branding,
component library, theming, color palette, visual identity,
dark mode toggle, in-app theme override, user theme preference, theme settings,
LocalAppDarkTheme, isSystemInDarkTheme, system dark mode, follow system theme,
dynamic theme, runtime theme switch, light dark switch, theme preference setting,
component prefix, custom prefix instead of App, rename App to project name,
project-specific component names, COMPONENT_PREFIX, derive prefix from project name.

**Freshness rule:** `@ExperimentalStylesApi` is experimental (Android Jetpack Compose
`1.12.0-alpha03` at last check) and the Styles API changes between releases — Material
Design support for Styles is planned but not yet available. Recheck the official docs
before upgrading, and note these are Android Jetpack Compose docs; Compose Multiplatform
(JetBrains) support may lag behind:
- https://developer.android.com/develop/ui/compose/styles (overview)
- https://developer.android.com/develop/ui/compose/styles/fundamentals
- https://developer.android.com/develop/ui/compose/styles/state-animations
- https://developer.android.com/develop/ui/compose/styles/styles-vs-modifiers
- https://developer.android.com/develop/ui/compose/styles/theming
- https://developer.android.com/develop/ui/compose/styles/performance
- https://developer.android.com/develop/ui/compose/styles/dos-donts
- https://developer.android.com/develop/ui/compose/styles/examples
- https://developer.android.com/develop/ui/compose/styles/limitations

A full extracted summary of these pages (API surface, do's/don'ts, benchmarks,
limitations) lives in `references/compose-styles-api-reference.md` — use it to audit
generated code against the official guidance without re-fetching every page.

---

## Recommendation First

Default to **custom tokens + `AppTheme` + `@ExperimentalStylesApi` sealed variant systems —
no Material dependency**.

Why:
- full ownership of the token layer means no Material opinion leaking into spacing, shape, or color
- sealed variant classes (e.g., `ButtonVariant.Primary`) make component APIs explicit and auditable
- `@ExperimentalStylesApi` is the sanctioned path for custom styling in CMP; Material3 is an overlay
  on top of it, not a replacement

Use Material3 only when the product targets Material Design explicitly and design token ownership
is not a concern.

## Component API Placement

Use the smallest API that still preserves the product’s structure.

| Component type | Preferred pattern | Why |
|---|---|---|
| App shell / page chrome | Slot API | The caller owns the region content, but the shell stays fixed |
| Fixed visual region with a narrow contract | Restricted scope template | Keeps layout and ordering consistent while still allowing caller content |
| Small leaf control | Data + variant params | Simpler than slots when the content is not meaningfully custom |
| Deep theme / app-wide metadata | CompositionLocal | Shared context, not positional content |

Concrete KMM design-system mapping:
- `AppScaffold`, `AppTopAppBar`, `AppCard`, `AppDialog`, `AppBottomSheet` -> slot API
- `CardHeader`, `ToolbarRow`, `SectionHeader`, `ActionRow` -> restricted scope template when the region needs guardrails
- `AppButton`, `AppBadge`, `AppTextField`, `AppText`, `AppChip` -> data/variant APIs first; add slots only if callers truly need custom body content

---

## Screen Layout Contract

> **Requires extended skill:** `AppScaffold` and `AppTopAppBar` are defined in
> `kotlin-multiplatform-design-system-extended`. Apply that skill before using the
> screen layout contract below.

Every screen must follow this structure — no exceptions. Consistency across all pages
depends on every feature using the same scaffold shell.

```kotlin
@Composable
fun FooContent(
    state: FooContract.State,
    onIntent: (FooContract.Intent) -> Unit,
    // windowSizeClass: WindowSizeClass  // add if adaptive layout is in scope
) {
    AppScaffold(                                    // always AppScaffold, never raw Scaffold
        topBar = {
            AppTopAppBar(
                title = "Page Title",              // ← title lives HERE, nowhere else
                navigationIcon = {                 // back button lives HERE
                    AppIconButton(onClick = { onIntent(FooContract.Intent.NavigateBack) }) {
                        AppIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {                        // action buttons live HERE
                    AppIconButton(onClick = { onIntent(FooContract.Intent.OpenMenu) }) {
                        AppIcon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->                           // always consume PaddingValues
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)            // ← prevents clipping under TopAppBar
                .padding(horizontal = appTheme.spacing.lg)  // ← token, never 16.dp
        ) {
            // functional content only — no title text, no duplicate action buttons
        }
    }
}
```

### Rules

| What | Where it lives | Never |
|---|---|---|
| Screen title | `AppTopAppBar(title = "…")` | `Text("…")` in content body |
| Back / close | `AppTopAppBar(navigationIcon = { … })` | Custom button in content |
| Primary action (save, filter, search) | `AppTopAppBar(actions = { … })` | Floating button duplicating the TopAppBar action |
| Overflow menu | `AppTopAppBar(actions = { AppIconButton(MoreVert) { … } })` | Separate menu row inside content |
| Horizontal content padding | `appTheme.spacing.lg` (`16.dp` token) | Hardcoded `.dp` literals |

### Why redundant UI in content hurts

- A title in the content AND in the TopAppBar means the title scrolls away — the
  TopAppBar title remains anchored; use it
- Duplicate action buttons create two sources of truth for the same action; one will
  inevitably be wired differently or go stale
- Not consuming `PaddingValues` clips content under the TopAppBar on devices with
  status bars

### Content Layout Patterns

Choose **one pattern** for a feature and apply it consistently across **all screens in that feature**.
Mixing patterns inside the same flow is a `layout_inconsistency` violation caught by `scan_design_violations.py`.

| Pattern | When to use | What goes inside `AppScaffold { paddingValues -> … }` |
|---|---|---|
| **Flat** | Default. Lists, feeds, forms, step-by-step flows | `Column` or `LazyColumn` directly |
| **Card-sectioned** | Profile, settings, detail pages with distinct sections | `Column { AppCard { … }; AppCard { … } }` |
| **Tabbed** | Genuinely multi-categorical content (Active / Completed / Archived) | `Column { TabRow(…); HorizontalPager { … } }` |

Rules:
- Do not place `AppCard` as the first-level child in a flat-pattern screen — it creates an inconsistent elevation bump vs. sibling screens
- Tabbed screens define the chrome; **each tab page must use the same inner pattern** (all tabs flat, or all tabs card-sectioned — never mixed)
- If two screens genuinely need different patterns, they belong in different features or flows

`scan_design_violations.py --layout` flags any feature `ui/` directory where `*Content.kt` files use different patterns.

---

## Overview

```
Design system layers (top-down):

  Tokens (AppColors, AppTypography, AppShapes, AppSpacing)
      ↓ consumed via StyleScope extensions
  Styles (sealed variant objects with Style values)
      ↓ merged via `then`
  Components (AppButton, AppCard, AppTextField, AppChip, AppBadge, AppText)
      ↓ composed
  Screens (feature UIs consume AppTheme.provide { } at the top)
```

---

## Ownership Model

The design system follows the shadcn model — you own the generated code, not a dependency.
This gives full brand control without forking a library.

| Layer | Ownership | Update path |
|---|---|---|
| `tokens/` — `AppColors`, `AppTypography`, `AppShapes`, `AppSpacing` | **Project-owned** | Customize freely — never touched by `/update-design-system` |
| `theme/` — `AppTheme`, `StyleScopeExtensions` | **Project-owned** | Customize freely |
| `components/` — `App*.kt` | **Skill-owned** | Run `/update-design-system` to pull in bug fixes and new variants without touching tokens |

**Why not a published library?** The Compose Styles API (`@ExperimentalStylesApi`) changes
between CMP releases. A published library would break every downstream project on CMP upgrades.
The scaffold approach keeps each project on its own upgrade schedule.

Use `/update-design-system` to compare your project's components against the latest skill
version and selectively apply fixes. The comparison is powered by
`scripts/update_design_system.py`, which MD5-hashes each component block from this SKILL.md
against the project file and reports CURRENT / MODIFIED / MISSING status.

Use `/fix-design` to scan an existing project for violations (hardcoded colors, hardcoded
user-facing strings, dp literals, `MaterialTheme.*` usage, `TextStyle()` construction,
nested containers, component reimplementations, direct token imports) and fix them
file-by-file with per-file
confirmation. Primary scanner: `detekt-rules/` (PSI-based); fallback: `scripts/scan_design_violations.py`.
The fallback scanner also flags missing preview stubs, missing multi-device preview coverage,
and missing Roborazzi screenshot tests for feature `*Content.kt` files so preview drift gets
caught with the rest of the design cleanup.

Use `/record-design-baselines` after fixing to record new Roborazzi golden PNGs.
Use `/audit-design-visual` to run a vision pass over the goldens and catch spacing,
contrast, and cross-screen consistency issues that have no code-level signal.

### Project documentation template

Copy `references/design-system-template.md` to `docs/design-system.md` in your project
and fill it in. This living document records your token values, component inventory,
detekt rule overrides, multi-device preview coverage, and audit log.

The skill reads `docs/design-system.md` when it exists and uses it to:
- Read your confirmed component prefix (e.g. `Acme` instead of `App`) — highest precedence in Step 0
- Confirm your token names before generating code
- Detect deviations you've documented as intentional

```bash
cp skills/kotlin-multiplatform-design-system/references/design-system-template.md \
   your-project/docs/design-system.md
```

Then replace `PROJECT_NAME`, `GROUP_ID`, and `COMPONENT_PREFIX` globally and fill in
the token values for your brand.

---

## Style Rules

- Use the Compose Styles API for visual styling, state styling, and animated transitions.
- Do not confuse Styles with the Slot API: slots are for structure/content customization, not theming.
- Keep text, borders, surfaces, and disabled states neutral-first.
- Use palette colors for brand, emphasis, status, and primary actions only.
- If the user does not specify a palette, suggest 2-3 options based on the project domain.
- If typography is unspecified, suggest a font pair and type scale before generating components.
- Use Atlassian and shadcn as references for neutral-first palettes, crisp hierarchy, and restrained component shapes.

## Naming Rule

- `App` is a placeholder for the project's actual component prefix — see Step 0 for how
  to resolve it (docs/design-system.md → user-stated → derived from project name → `App`
  fallback). Never leave literal `App*` names in a real project without resolving this first.
- Keep the resolved prefix for shared design-system primitives only.
- Use plain names for feature-local or page-local components.
- Do not over-prefix layouts, canvases, or state models.
- Reserve the prefix for reusable primitives that live in `:core:designsystem`.

**Key API facts:**
- `Style { ... }` — lambda-based style descriptor; runs in Layout/Draw phase (not Composition), skipping recomposition entirely for property changes
- `style1 then style2` — merges styles; properties are **not additive** — last-write-wins per property, same as CSS cascade
- `Modifier.styleable(styleState, defaultStyle, overrideStyle)` — applies styles to a node; also works directly on layout composables (`Row`, `Column`, `Box`) that have no built-in `style` parameter
- `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` — the sanctioned way to create a `StyleState` that stays current across recomposition; **the property is `isEnabled`, not `enabled`**
- `MutableStyleState(interactionSource)` — lower-level constructor; prefer `rememberUpdatedStyleState` in components so `isEnabled`/custom state updates are never stale
- Built-in interaction states: `hovered {}`, `pressed {}`, `focused {}`, `selected {}`, `enabled`/`isEnabled` (query, not typically its own block), `toggled {}` — states can nest (e.g. `hovered { pressed { … } }` for hover+press combined)
- Custom states: define a `StyleStateKey(default)`, add a `var MutableStyleState.yourState` extension, and a `StyleScope.yourStateBlock {}` extension using `state(key, block, predicate)`
- `animate { ... }` inside a state block — animates the wrapped properties automatically; `animate(spring(...)) { ... }` or `animate(tween(...)) { ... }` for a custom `AnimationSpec`
- `StyleScope` extensions — the **only** correct way to read `CompositionLocal` values inside a Style (reading a `CompositionLocal` directly inside a `@Composable fun somethingStyle(): Style { ... }` captures a stale value — see Common Anti-Patterns)
- Style property inheritance priority (highest to lowest): **direct composable argument** (`AppText(color = ...)`) > **`style` parameter** > **`Modifier.styleable {}` chain** > **parent/inherited typography-color properties**
- All Styles API classes require `@OptIn(ExperimentalStylesApi::class)`
- Full official reference: `references/compose-styles-api-reference.md` in this skill

---

## Prerequisites

- Project scaffolded with `kotlin-multiplatform-feature-scaffold`
- CMP 1.11.1+ (`compose-multiplatform = "1.11.1"` in `libs.versions.toml`)
- Convention plugin `GROUP_ID.feature.ui` or `GROUP_ID.core` available

---

## Step 0: Determine the component prefix

> **Hard rule — never violated:** `App` (as in `AppButton`, `AppTheme`, `AppColors`) is a
> **template placeholder in this SKILL.md**, exactly like `GROUP_ID`. It must never be
> written to disk literally for a real project. Do not generate a file named
> `AppButton.kt` containing `class AppButton` for an actual project and leave a mental
> note to rename it later — resolve the real prefix FIRST (this step), then write every
> file directly with that name the first time. There is no "rename pass" step because
> there should be nothing left to rename.
>
> The only time literal `App*` is correct output is when the resolved prefix in the
> precedence below genuinely computes to `App` (rare — only for placeholder/example
> projects with no real name yet), or when working inside this skills repo itself,
> where `App` is the documented template convention on purpose.

**Precedence (highest to lowest):**

1. `COMPONENT_PREFIX` already recorded in `docs/design-system.md`, if that file exists — an explicit, previously-confirmed choice always wins
2. A prefix the user states directly in the request ("call it Acme", "use GB as the prefix")
3. Derived from the project name via the script below
4. `App` — only if nothing else yields a usable word (e.g. a genuine placeholder/example project)

**Run the derivation script** (steps 3–4 are deterministic, not a guess):

```bash
python3 ~/.claude/skills/kotlin-multiplatform-design-system/scripts/derive_component_prefix.py <project_root>
```

If running from inside kmm-agent-skills:
```bash
python3 skills/kotlin-multiplatform-design-system/scripts/derive_component_prefix.py <project_root>
```

The script reads, in order: `settings.gradle.kts` `rootProject.name` → the Gradle group
ID's last segment → the project directory name — strips generic noise words (`app`,
`android`, `ios`, `kmp`, `shared`, `compose`, `project`, `multiplatform`, `mobile`,
`client`, `core`, `main`), PascalCases what remains, and prints the result plus which
source it came from. Example: `rootProject.name = "GuildBase"` → prefix `GuildBase` →
`GuildBaseButton`, `GuildBaseCard`, `GuildBaseTextField`.

**Confirm before generating.** Show the derived prefix and its source, then ask the user
to confirm or override — a wrong prefix means regenerating every file, not a quick rename.
Once confirmed, record it in `docs/design-system.md` (`COMPONENT_PREFIX` field) so future
sessions read it from precedence step 1 instead of re-deriving.

**Then generate directly with the resolved name.** Every code block in Steps 1–9 below
shows `App` for template readability — when you actually write a file for a real project,
substitute the resolved prefix as you write it (`AppButton.kt` → `GuildBaseButton.kt`,
`class AppTheme` → `class GuildBaseTheme`), in the same pass, not as a follow-up edit.
The audit enforces this: `design system prefix mismatch` flags any `App*` class/fun/object
declaration under `core/designsystem` when `docs/design-system.md` records a different
`COMPONENT_PREFIX` — a real mismatch, not template text, since the audit only scans
actual `.kt` files in the target project.

---

## Step 1: Create `:core:designsystem` module

Create `core/designsystem/build.gradle.kts`:

```kotlin
plugins {
    id("GROUP_ID.core")          // applies KMP + Compose targets
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            // No compose.material3 — fully custom
        }
    }
}
```

Register in `settings.gradle.kts`:

```kotlin
include(":core:designsystem")
```

---

## Step 2: Design Tokens

> **Project-owned.** Customize `tokens/` and `theme/` freely — `/update-design-system`
> will never modify these files. This is your brand layer.

### Palette guidance

- Prefer neutral tokens for most text, surfaces, borders, and disabled UI.
- Reserve saturated palette colors for brand accents, primary actions, and semantic states.
- If the project brief does not name a palette, propose one that fits the product:
  - enterprise / admin: zinc, slate, neutral
  - modern consumer: blue, indigo, violet
  - creative / playful: violet, fuchsia, rose
  - trust / finance: blue, teal, emerald

### `tokens/AppColors.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    // Brand
    val primary: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val primaryDisabled: Color,
    val onPrimary: Color,

    // Secondary
    val secondary: Color,
    val secondaryHover: Color,
    val onSecondary: Color,

    // Destructive
    val destructive: Color,
    val destructiveHover: Color,
    val onDestructive: Color,

    // Surface
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,

    // Border
    val border: Color,
    val borderFocus: Color,

    // Ghost / muted
    val muted: Color,
    val onMuted: Color,

    // Status
    val success: Color,
    val warning: Color,
    val error: Color,
    val onStatus: Color,

    // State overlays
    val hoverOverlay: Color,
    val pressedOverlay: Color,

    val isLight: Boolean,
)

val LightColors = AppColors(
    primary          = Color(0xFF09090B),
    primaryHover     = Color(0xFF27272A),
    primaryPressed   = Color(0xFF3F3F46),
    primaryDisabled  = Color(0xFFD4D4D8),
    onPrimary        = Color(0xFFFAFAFA),

    secondary        = Color(0xFFF4F4F5),
    secondaryHover   = Color(0xFFE4E4E7),
    onSecondary      = Color(0xFF09090B),

    destructive      = Color(0xFFDC2626),
    destructiveHover = Color(0xFFB91C1C),
    onDestructive    = Color(0xFFFEF2F2),

    background       = Color(0xFFFFFFFF),
    surface          = Color(0xFFFFFFFF),
    surfaceVariant   = Color(0xFFF4F4F5),
    onSurface        = Color(0xFF09090B),
    onSurfaceVariant = Color(0xFF71717A),

    border           = Color(0xFFE4E4E7),
    borderFocus      = Color(0xFF09090B),

    muted            = Color(0xFFF4F4F5),
    onMuted          = Color(0xFF71717A),

    success          = Color(0xFF16A34A),
    warning          = Color(0xFFD97706),
    error            = Color(0xFFDC2626),
    onStatus         = Color(0xFFFFFFFF),

    hoverOverlay     = Color(0x0A000000),
    pressedOverlay   = Color(0x1A000000),

    isLight          = true,
)

val DarkColors = AppColors(
    primary          = Color(0xFFFAFAFA),
    primaryHover     = Color(0xFFE4E4E7),
    primaryPressed   = Color(0xFFD4D4D8),
    primaryDisabled  = Color(0xFF3F3F46),
    onPrimary        = Color(0xFF09090B),

    secondary        = Color(0xFF27272A),
    secondaryHover   = Color(0xFF3F3F46),
    onSecondary      = Color(0xFFFAFAFA),

    destructive      = Color(0xFF7F1D1D),
    destructiveHover = Color(0xFF991B1B),
    onDestructive    = Color(0xFFFEF2F2),

    background       = Color(0xFF09090B),
    surface          = Color(0xFF09090B),
    surfaceVariant   = Color(0xFF18181B),
    onSurface        = Color(0xFFFAFAFA),
    onSurfaceVariant = Color(0xFFA1A1AA),

    border           = Color(0xFF27272A),
    borderFocus      = Color(0xFFFAFAFA),

    muted            = Color(0xFF27272A),
    onMuted          = Color(0xFFA1A1AA),

    success          = Color(0xFF15803D),
    warning          = Color(0xFFB45309),
    error            = Color(0xFF7F1D1D),
    onStatus         = Color(0xFFFFFFFF),

    hoverOverlay     = Color(0x0AFFFFFF),
    pressedOverlay   = Color(0x1AFFFFFF),

    isLight          = false,
)
```

### `tokens/AppTypography.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class AppTypography(
    val displayLarge: TextStyle  = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold,   lineHeight = 44.sp, letterSpacing = (-0.5).sp),
    val displayMedium: TextStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold,   lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    val titleLarge: TextStyle    = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    val titleMedium: TextStyle   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    val titleSmall: TextStyle    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    val bodyLarge: TextStyle     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal,  lineHeight = 24.sp),
    val bodyMedium: TextStyle    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,  lineHeight = 20.sp),
    val bodySmall: TextStyle     = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,  lineHeight = 16.sp),
    val labelLarge: TextStyle    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium,  lineHeight = 20.sp, letterSpacing = 0.1.sp),
    val labelSmall: TextStyle    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium,  lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
```

### `tokens/AppShapes.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val none: Dp    = 0.dp,
    val xs: Dp      = 2.dp,
    val sm: Dp      = 4.dp,
    val md: Dp      = 6.dp,
    val lg: Dp      = 8.dp,
    val xl: Dp      = 12.dp,
    val xxl: Dp     = 16.dp,
    val full: Dp    = 9999.dp,
)
```

### `tokens/AppSpacing.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp  = 4.dp,
    val sm: Dp  = 8.dp,
    val md: Dp  = 12.dp,
    val lg: Dp  = 16.dp,
    val xl: Dp  = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
)
```

---

## Step 3: AppTheme + CompositionLocals

### `theme/AppTheme.kt`

```kotlin
package GROUP_ID.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import GROUP_ID.core.designsystem.tokens.AppColors
import GROUP_ID.core.designsystem.tokens.AppShapes
import GROUP_ID.core.designsystem.tokens.AppSpacing
import GROUP_ID.core.designsystem.tokens.AppTypography
import GROUP_ID.core.designsystem.tokens.DarkColors
import GROUP_ID.core.designsystem.tokens.LightColors

@Immutable
data class AppTheme(
    val colors: AppColors,
    val typography: AppTypography,
    val shapes: AppShapes,
    val spacing: AppSpacing,
) {
    companion object {
        val LocalAppTheme: ProvidableCompositionLocal<AppTheme> =
            staticCompositionLocalOf { AppTheme.light() }

        fun light(
            colors: AppColors         = LightColors,
            typography: AppTypography = AppTypography(),
            shapes: AppShapes         = AppShapes(),
            spacing: AppSpacing       = AppSpacing(),
        ) = AppTheme(colors, typography, shapes, spacing)

        fun dark(
            colors: AppColors         = DarkColors,
            typography: AppTypography = AppTypography(),
            shapes: AppShapes         = AppShapes(),
            spacing: AppSpacing       = AppSpacing(),
        ) = AppTheme(colors, typography, shapes, spacing)
    }
}

/**
 * Holds an in-app dark-mode override (true/false) set by a user settings toggle.
 * Null means "follow the system". Read via [LocalAppDarkTheme.current].
 *
 * Usage:
 * ```
 * // In your settings screen, persist and surface the override:
 * CompositionLocalProvider(LocalAppDarkTheme provides userPrefersDark) {
 *     AppTheme { ... }
 * }
 * ```
 */
val LocalAppDarkTheme = compositionLocalOf<Boolean?> { null }

/**
 * Root theme wrapper. Defaults to system dark-mode on all platforms.
 * An in-app override can be injected via [LocalAppDarkTheme].
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = LocalAppDarkTheme.current ?: isSystemInDarkTheme(),
    theme: AppTheme = if (darkTheme) AppTheme.dark() else AppTheme.light(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        AppTheme.LocalAppTheme provides theme,
        content = content,
    )
}

// Convenience accessor in Composable scope
val appTheme: AppTheme
    @Composable get() = AppTheme.LocalAppTheme.current
```

---

## Step 4: StyleScope Extensions

These are the **only** correct way to read `CompositionLocal` values inside a `Style`. Styles run outside Composition, so you cannot call `AppTheme.LocalAppTheme.current` directly.

### `theme/StyleScopeExtensions.kt`

```kotlin
package GROUP_ID.core.designsystem.theme

import androidx.compose.foundation.style.StyleScope
import androidx.compose.ui.ExperimentalComposeUiApi
import GROUP_ID.core.designsystem.tokens.AppColors
import GROUP_ID.core.designsystem.tokens.AppShapes
import GROUP_ID.core.designsystem.tokens.AppSpacing
import GROUP_ID.core.designsystem.tokens.AppTypography

// Note: @ExperimentalStylesApi — check actual annotation in your CMP version.
// In CMP 1.11.x this may be @OptIn(ExperimentalStylesApi::class)

val StyleScope.appTheme: AppTheme
    get() = AppTheme.LocalAppTheme.currentValue

val StyleScope.colors: AppColors
    get() = AppTheme.LocalAppTheme.currentValue.colors

val StyleScope.typography: AppTypography
    get() = AppTheme.LocalAppTheme.currentValue.typography

val StyleScope.shapes: AppShapes
    get() = AppTheme.LocalAppTheme.currentValue.shapes

val StyleScope.spacing: AppSpacing
    get() = AppTheme.LocalAppTheme.currentValue.spacing
```

> **Critical rule**: Never capture token values before the Style block:
> ```kotlin
> // ❌ WRONG — stale at creation time
> val color = AppTheme.LocalAppTheme.current.colors.primary
> val myStyle = Style { background(color) }
>
> // ✅ CORRECT — read at consume time via StyleScope extension
> val myStyle = Style { background(colors.primary) }
> ```

---

## Step 5: Variant Systems

> **Required in every style and component file:** add `@file:OptIn(ExperimentalStylesApi::class)`
> before the `package` line and `import androidx.compose.foundation.style.ExperimentalStylesApi`
> in the imports. The snippets below omit these for brevity — they are required for compilation.

### `styles/ButtonStyles.kt`

Mirrors shadcn Button: `default | outline | secondary | ghost | destructive | link`
Plus sizes: `xs | sm | md | lg | icon`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

// ── Interaction atoms (shared across variants) ────────────────────────────────

internal val buttonInteractionStyle = Style {
    hovered  { animate { alpha(0.90f) } }
    pressed  { animate { alpha(0.80f) } }
    disabled { animate { alpha(0.38f) } }
    focused  { animate { borderWidth(2.dp); borderColor(colors.borderFocus) } }
}

// ── Variant styles ─────────────────────────────────────────────────────────────

sealed interface ButtonVariant {
    val style: Style

    data object Default : ButtonVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.md))
        } then buttonInteractionStyle
    }

    data object Outline : ButtonVariant {
        override val style = Style {
            background(colors.background)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
            hovered { animate { background(colors.secondary) } }
            pressed { animate { background(colors.secondary) } }
        } then buttonInteractionStyle
    }

    data object Secondary : ButtonVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            shape(RoundedCornerShape(shapes.md))
            hovered { animate { background(colors.secondaryHover) } }
        } then buttonInteractionStyle
    }

    data object Ghost : ButtonVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.md))
            hovered { animate { background(colors.secondary) } }
            pressed { animate { background(colors.secondary) } }
        } then buttonInteractionStyle
    }

    data object Destructive : ButtonVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            shape(RoundedCornerShape(shapes.md))
            hovered { animate { background(colors.destructiveHover) } }
        } then buttonInteractionStyle
    }

    data object Link : ButtonVariant {
        override val style = Style {
            contentColor(colors.primary)
            hovered { animate { alpha(0.70f) } }
        }
    }
}

// ── Size styles ────────────────────────────────────────────────────────────────

sealed interface ButtonSize {
    val style: Style

    data object Xs : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.sm, vertical = spacing.xs)
            fontSize(12.sp)
            height(28.dp)
        }
    }

    data object Sm : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(14.sp)
            height(32.dp)
        }
    }

    data object Md : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.lg, vertical = spacing.sm)
            fontSize(14.sp)
            height(40.dp)
        }
    }

    data object Lg : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.xl, vertical = spacing.md)
            fontSize(16.sp)
            height(48.dp)
        }
    }

    data object Icon : ButtonSize {
        override val style = Style {
            padding(all = spacing.sm)
            width(40.dp)
            height(40.dp)
        }
    }
}
```

### `styles/BadgeStyles.kt`

Mirrors shadcn Badge: `default | secondary | destructive | outline | ghost`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface BadgeVariant {
    val style: Style

    data object Default : BadgeVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Secondary : BadgeVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Destructive : BadgeVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Outline : BadgeVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Ghost : BadgeVariant {
        override val style = Style {
            background(colors.muted)
            contentColor(colors.onMuted)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
        }
    }
}
```

### `styles/CardStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing
import GROUP_ID.core.designsystem.tokens.AppSpacing

sealed interface CardVariant {
    val style: Style

    data object Default : CardVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
        }
    }

    data object Elevated : CardVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
            // elevation via shadow — add Modifier.shadow in component
        }
    }

    data object Filled : CardVariant {
        override val style = Style {
            background(colors.surfaceVariant)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
        }
    }
}

sealed interface CardSize {
    val contentPadding: androidx.compose.ui.unit.Dp
    val headerSpacing: androidx.compose.ui.unit.Dp

    data object Default : CardSize {
        override val contentPadding = AppSpacing().xxl  // 24.dp
        override val headerSpacing  = AppSpacing().sm   // 8.dp
    }
    data object Sm : CardSize {
        override val contentPadding = AppSpacing().lg   // 16.dp
        override val headerSpacing  = AppSpacing().xs   // 4.dp
    }
}
```

### `styles/ChipStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface ChipVariant {
    val style: Style

    data object Default : ChipVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
            hovered { animate { background(colors.secondaryHover) } }
            pressed { animate { background(colors.secondaryHover) } }
        }
    }

    data object Selected : ChipVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
        }
    }

    data object Outline : ChipVariant {
        override val style = Style {
            borderWidth(1.dp)
            borderColor(colors.border)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
            hovered { animate { background(colors.secondary) } }
        }
    }
}
```

### `styles/TextFieldStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface TextFieldVariant {
    val style: Style

    data object Default : TextFieldVariant {
        override val style = Style {
            background(colors.background)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
            padding(horizontal = spacing.md, vertical = spacing.sm)
            fontSize(14.sp)
            focused { animate { borderWidth(2.dp); borderColor(colors.borderFocus) } }
            disabled { animate { alpha(0.38f) } }
        }
    }

    data object Filled : TextFieldVariant {
        override val style = Style {
            background(colors.surfaceVariant)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(topStart = shapes.md, topEnd = shapes.md, bottomStart = 0.dp, bottomEnd = 0.dp))
            padding(horizontal = spacing.md, vertical = spacing.sm)
            fontSize(14.sp)
            focused { animate { borderWidth(2.dp); borderColor(colors.borderFocus) } }
        }
    }

    data object Ghost : TextFieldVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            padding(horizontal = spacing.xs, vertical = spacing.xs)
            fontSize(14.sp)
            focused { animate { borderBottomWidth(1.dp); borderColor(colors.borderFocus) } }
        }
    }
}
```

---

## Step 6: Core Components

> **Skill-owned.** Components are updateable via `/update-design-system`. Avoid deep
> customisations here — put brand-specific variants in project-level composables that
> wrap these primitives instead.

| Component | Stability | Notes |
|---|---|---|
| `AppButton` | **Stable** | 6 variants, 5 sizes |
| `AppBadge` | **Stable** | 5 variants |
| `AppCard` | **Stable** | 3 variants, 2 sizes |
| `AppChip` | **Stable** | 3 variants, selected state |
| `AppTextField` | **Stable** | label, placeholder, leading/trailing icon, error state |
| `AppText` | **Stable** | `AppTextStyle` enum, muted mode |

### `components/AppButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ButtonSize
import GROUP_ID.core.designsystem.styles.ButtonVariant

/**
 * shadcn-inspired AppButton.
 *
 * Usage:
 * ```
 * AppButton(onClick = {}) { Text("Click me") }
 * AppButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Sm) { Text("Outline") }
 * AppButton(onClick = {}, variant = ButtonVariant.Destructive) { Text("Delete") }
 * // One-off style override:
 * AppButton(onClick = {}, style = Style { shape(CircleShape) }) { Text("Pill") }
 * ```
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Md,
    style: Style = Style,        // ← empty; DO NOT set a default Style here
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // rememberUpdatedStyleState keeps isEnabled current across recomposition without
    // recreating the StyleState — the sanctioned pattern from the official Styles API docs.
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,          // no ripple — use Style animations
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            // defaultStyle = variant.style then size.style; override via incoming `style`
            .styleable(styleState, variant.style then size.style, style),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
```

### `components/AppBadge.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.styles.BadgeVariant

/**
 * Label/tag component. Maps to shadcn Badge.
 *
 * Usage:
 * ```
 * AppBadge { Text("New") }
 * AppBadge(variant = BadgeVariant.Destructive) { Text("Error") }
 * AppBadge(variant = BadgeVariant.Outline) { Text("Draft") }
 * ```
 */
@Composable
fun AppBadge(
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    // Non-interactive — no interaction source needed, use a static StyleState
    val styleState = remember { MutableStyleState() }

    Box(
        modifier = modifier.styleable(styleState, variant.style, style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

### `components/AppCard.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.styles.CardSize
import GROUP_ID.core.designsystem.styles.CardVariant

/**
 * Maps to shadcn Card with slots: header, content, footer.
 *
 * Usage:
 * ```
 * AppCard(
 *     header = { CardHeader(title = "Title", description = "Subtitle") },
 *     footer = { AppButton(onClick = {}) { Text("Action") } }
 * ) {
 *     Text("Card body content")
 * }
 * ```
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    size: CardSize = CardSize.Default,
    style: Style = Style,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val styleState = remember { MutableStyleState() }

    Column(
        modifier = modifier.styleable(styleState, variant.style, style),
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(size.headerSpacing))
        }
        content()
        if (footer != null) {
            Spacer(Modifier.height(size.headerSpacing))
            footer()
        }
    }
}

@Composable
fun CardHeader(
    title: String,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            AppText(text = title, style = AppTextStyle.TitleSmall)
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                AppText(text = description, style = AppTextStyle.BodySmall, muted = true)
            }
        }
        if (action != null) {
            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)) {
                action()
            }
        }
    }
}
```

### `components/AppChip.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ChipVariant

/**
 * Selectable chip / filter tag.
 *
 * Usage:
 * ```
 * AppChip(label = "Kotlin", selected = true, onClick = { toggle() })
 * AppChip(label = "Swift", variant = ChipVariant.Outline, onClick = {})
 * ```
 */
@Composable
fun AppChip(
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    variant: ChipVariant = if (selected) ChipVariant.Selected else ChipVariant.Default,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    } else Modifier

    Row(
        modifier = modifier
            .then(clickableModifier)
            .styleable(styleState, variant.style, style),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
    ) {
        AppText(text = label)
    }
}
```

### `components/AppTextField.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import GROUP_ID.core.designsystem.styles.TextFieldVariant
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.colors

/**
 * Usage:
 * ```
 * AppTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "you@example.com")
 * AppTextField(value = pwd, onValueChange = { pwd = it }, label = "Password", visualTransformation = PasswordVisualTransformation())
 * AppTextField(value = q, onValueChange = { q = it }, variant = TextFieldVariant.Ghost, placeholder = "Search…")
 * AppTextField(value = bio, onValueChange = { bio = it }, singleLine = false, label = "Bio")
 * ```
 */
@OptIn(ExperimentalStylesApi::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    variant: TextFieldVariant = TextFieldVariant.Default,
    style: Style = Style,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    val errorStyle = if (isError) Style { borderColor(colors.error) } else Style

    Column(modifier = modifier) {
        if (label != null) {
            AppText(text = label, style = AppTextStyle.LabelLarge)
            Spacer(Modifier.height(appTheme.spacing.xxs))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .styleable(styleState, variant.style then errorStyle, style),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(Modifier.width(appTheme.spacing.xs))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            AppText(placeholder, style = AppTextStyle.BodyMedium, muted = true)
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(appTheme.spacing.xs))
                        trailingIcon()
                    }
                }
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(appTheme.spacing.xxs))
            AppText(
                text = supportingText,
                style = AppTextStyle.BodySmall,
                color = if (isError) appTheme.colors.error else appTheme.colors.onSurfaceVariant,
            )
        }
    }
}
```

---

### `components/AppText.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import GROUP_ID.core.designsystem.theme.appTheme

enum class AppTextStyle {
    DisplayLarge, DisplayMedium,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelSmall,
}

/**
 * Usage:
 * ```
 * AppText("Hello world")
 * AppText("Title", style = AppTextStyle.TitleLarge)
 * AppText("Subtitle", style = AppTextStyle.BodySmall, muted = true)
 * ```
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.BodyMedium,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified,
) {
    val theme = appTheme
    val resolvedStyle = when (style) {
        AppTextStyle.DisplayLarge  -> theme.typography.displayLarge
        AppTextStyle.DisplayMedium -> theme.typography.displayMedium
        AppTextStyle.TitleLarge    -> theme.typography.titleLarge
        AppTextStyle.TitleMedium   -> theme.typography.titleMedium
        AppTextStyle.TitleSmall    -> theme.typography.titleSmall
        AppTextStyle.BodyLarge     -> theme.typography.bodyLarge
        AppTextStyle.BodyMedium    -> theme.typography.bodyMedium
        AppTextStyle.BodySmall     -> theme.typography.bodySmall
        AppTextStyle.LabelLarge    -> theme.typography.labelLarge
        AppTextStyle.LabelSmall    -> theme.typography.labelSmall
    }

    val textColor = when {
        color != Color.Unspecified -> color
        muted                       -> theme.colors.onSurfaceVariant
        else                        -> theme.colors.onSurface
    }

    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle.copy(color = textColor),
        maxLines = maxLines,
        overflow = overflow,
    )
}
```

---

## Component Previews

Each design system component ships with a dedicated preview file under `previews/`.
These previews serve three purposes:

1. **IDE design review** — visible in the Desktop preview panel
   (`./gradlew :desktopApp:run` or Android Studio compose preview)
2. **Roborazzi per-component goldens** — captured by
   `./gradlew :core:designsystem:jvmTest`, producing one PNG per state
3. **`/fix-design` verification** — after a theme token change, run
   `:core:designsystem:jvmTest` to confirm all components still look correct
   before running full feature tests

Feature UI modules follow the same rule: every `*Content.kt` must have a preview stub and
matching Roborazzi screenshot coverage for phone, tablet, and desktop sizes.

> **Skill-owned.** Preview files follow the same ownership rule as components —
> updateable via `/update-design-system`. Never edit preview files to reflect
> project-specific states; create separate preview composables in the feature UI module.

---

### `previews/AppThemePreviewWrapper.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.theme.AppTheme

/**
 * Shared annotation for cross-device preview coverage.
 * Generates one screenshot per size class: phone, tablet, desktop.
 * Use on light/dark base variants. State variants (disabled, error) use plain @Preview.
 */
@Preview(name = "Phone",   widthDp = 360,  heightDp = 640)
@Preview(name = "Tablet",  widthDp = 673,  heightDp = 841)
@Preview(name = "Desktop", widthDp = 1280, heightDp = 800)
annotation class MultiDevicePreview

@Composable
fun AppThemePreviewWrapper(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
```

---

### `previews/AppButtonPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.ButtonVariant

@MultiDevicePreview
@Composable
fun AppButtonDefaultLightPreview() {
    AppThemePreviewWrapper(darkTheme = false) {
        AppButton(onClick = {}) { AppText("Continue") }
    }
}

@MultiDevicePreview
@Composable
fun AppButtonDefaultDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        AppButton(onClick = {}) { AppText("Continue") }
    }
}

@Preview
@Composable
fun AppButtonDisabledPreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, enabled = false) { AppText("Continue") }
    }
}

@Preview
@Composable
fun AppButtonOutlinePreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Outline) { AppText("Cancel") }
    }
}

@Preview
@Composable
fun AppButtonDestructivePreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Destructive) { AppText("Delete account") }
    }
}

@Preview
@Composable
fun AppButtonGhostPreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Ghost) { AppText("Skip") }
    }
}
```

---

### `previews/AppBadgePreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppBadge
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.BadgeVariant

@MultiDevicePreview
@Composable
fun AppBadgeAllVariantsPreview() {
    AppThemePreviewWrapper {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(variant = BadgeVariant.Default)    { AppText("New") }
            AppBadge(variant = BadgeVariant.Secondary)  { AppText("Beta") }
            AppBadge(variant = BadgeVariant.Destructive){ AppText("Error") }
            AppBadge(variant = BadgeVariant.Outline)    { AppText("Draft") }
            AppBadge(variant = BadgeVariant.Ghost)      { AppText("Info") }
        }
    }
}

@MultiDevicePreview
@Composable
fun AppBadgeDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(variant = BadgeVariant.Default)    { AppText("New") }
            AppBadge(variant = BadgeVariant.Destructive){ AppText("Error") }
        }
    }
}
```

---

### `previews/AppCardPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppCard
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.CardVariant

@MultiDevicePreview
@Composable
fun AppCardDefaultPreview() {
    AppThemePreviewWrapper {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppText("Card body content")
        }
    }
}

@Preview
@Composable
fun AppCardWithSlotsPreview() {
    AppThemePreviewWrapper {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            header = { AppText("Card Title") },
            footer = { AppButton(onClick = {}) { AppText("Action") } },
        ) {
            AppText("This is the card body. It can be multiple lines of description text.")
        }
    }
}

@Preview
@Composable
fun AppCardElevatedPreview() {
    AppThemePreviewWrapper {
        AppCard(modifier = Modifier.fillMaxWidth(), variant = CardVariant.Elevated) {
            AppText("Elevated card")
        }
    }
}

@MultiDevicePreview
@Composable
fun AppCardDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppText("Dark mode card")
        }
    }
}
```

---

### `previews/AppChipPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppChip
import GROUP_ID.core.designsystem.styles.ChipVariant

@MultiDevicePreview
@Composable
fun AppChipStatesPreview() {
    AppThemePreviewWrapper {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppChip(label = "Default",  onClick = {})
            AppChip(label = "Selected", onClick = {}, selected = true)
            AppChip(label = "Disabled", onClick = {}, enabled = false)
            AppChip(label = "Outline",  onClick = {}, variant = ChipVariant.Outline)
        }
    }
}

@MultiDevicePreview
@Composable
fun AppChipDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppChip(label = "Default",  onClick = {})
            AppChip(label = "Selected", onClick = {}, selected = true)
        }
    }
}
```

---

### `previews/AppTextFieldPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppTextField
import GROUP_ID.core.designsystem.styles.TextFieldVariant

@Preview
@Composable
fun AppTextFieldEmptyPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Enter email",
        )
    }
}

@MultiDevicePreview
@Composable
fun AppTextFieldWithLabelAndValuePreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "hello@example.com",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            placeholder = "you@example.com",
        )
    }
}

@Preview
@Composable
fun AppTextFieldErrorPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "bad-email",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            isError = true,
            supportingText = "Please enter a valid email address",
        )
    }
}

@Preview
@Composable
fun AppTextFieldDisabledPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "readonly@example.com",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            enabled = false,
        )
    }
}

@Preview
@Composable
fun AppTextFieldGhostPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Search…",
            variant = TextFieldVariant.Ghost,
        )
    }
}

@MultiDevicePreview
@Composable
fun AppTextFieldDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Email",
                placeholder = "you@example.com",
            )
            AppTextField(
                value = "bad",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Email",
                isError = true,
                supportingText = "Invalid email",
            )
        }
    }
}
```

---

### `previews/AppTextPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.components.AppTextStyle

@MultiDevicePreview
@Composable
fun AppTextTypescalePreview() {
    AppThemePreviewWrapper {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppText("DisplayLarge",  style = AppTextStyle.DisplayLarge)
            AppText("DisplayMedium", style = AppTextStyle.DisplayMedium)
            AppText("TitleLarge",    style = AppTextStyle.TitleLarge)
            AppText("TitleMedium",   style = AppTextStyle.TitleMedium)
            AppText("TitleSmall",    style = AppTextStyle.TitleSmall)
            AppText("BodyLarge",     style = AppTextStyle.BodyLarge)
            AppText("BodyMedium",    style = AppTextStyle.BodyMedium)
            AppText("BodySmall",     style = AppTextStyle.BodySmall)
            AppText("LabelLarge",    style = AppTextStyle.LabelLarge)
            AppText("LabelSmall",    style = AppTextStyle.LabelSmall)
        }
    }
}

@Preview
@Composable
fun AppTextMutedPreview() {
    AppThemePreviewWrapper {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("Normal text",  style = AppTextStyle.BodyMedium)
            AppText("Muted text",   style = AppTextStyle.BodyMedium, muted = true)
            AppText("Normal label", style = AppTextStyle.LabelSmall)
            AppText("Muted label",  style = AppTextStyle.LabelSmall,  muted = true)
        }
    }
}

@MultiDevicePreview
@Composable
fun AppTextDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("TitleLarge dark",  style = AppTextStyle.TitleLarge)
            AppText("BodyMedium dark",  style = AppTextStyle.BodyMedium)
            AppText("Muted dark",       style = AppTextStyle.BodySmall, muted = true)
        }
    }
}
```

---

## Step 7: Wire AppTheme in platform entry points

### Android — `androidApp/src/main/kotlin/.../MainActivity.kt`

```kotlin
setContent {
    AppTheme {          // isSystemInDarkTheme() is the default — no argument needed
        AppNavHost()
    }
}
```

### Desktop — `desktopApp/src/jvmMain/kotlin/main.kt`

```kotlin
application {
    Window(onCloseRequest = ::exitApplication, title = "App") {
        AppTheme {      // isSystemInDarkTheme() reads OS dark mode on JVM via AWT
            AppNavHost()
        }
    }
}
```

### iOS — `shared/src/iosMain/kotlin/AppView.kt`

```kotlin
@Composable
fun AppView() {
    AppTheme {          // isSystemInDarkTheme() reads UITraitCollection on iOS
        AppNavHost()
    }
}
```

### In-app theme toggle (settings screen)

To let users override the system theme, wrap `AppTheme` with `LocalAppDarkTheme`:

```kotlin
// Read the user's preference from DataStore / shared prefs:
val userDarkMode: Boolean? by viewModel.darkModePreference.collectAsStateWithLifecycle()

CompositionLocalProvider(LocalAppDarkTheme provides userDarkMode) {
    AppTheme {
        AppNavHost()
    }
}
```

`null` = follow system, `true` = always dark, `false` = always light.

---

## Step 8: Usage patterns

### Basic usage

```kotlin
// Default button
AppButton(onClick = { /* ... */ }) {
    AppText("Save")
}

// Variant + size
AppButton(
    onClick = { /* ... */ },
    variant = ButtonVariant.Outline,
    size = ButtonSize.Sm,
) {
    AppText("Cancel")
}

// Destructive with icon
AppButton(
    onClick = { deleteItem() },
    variant = ButtonVariant.Destructive,
) {
    Icon(Icons.Default.Delete, contentDescription = null)
    Spacer(Modifier.width(4.dp))
    AppText("Delete")
}
```

### One-off style override (escape hatch)

```kotlin
// Override just the corner radius on this specific instance
AppButton(
    onClick = {},
    style = Style { shape(CircleShape) },
) {
    AppText("Pill button")
}
```

### Style composition for custom variants

```kotlin
// Compose multiple styles — reuse without touching the design system
val accentButtonStyle = ButtonVariant.Default.style then Style {
    background(Color(0xFF7C3AED))   // brand purple
    contentColor(Color.White)
}

AppButton(onClick = {}, style = accentButtonStyle) {
    AppText("Accent")
}
```

### Card composition (shadcn-style slots)

```kotlin
AppCard(
    variant = CardVariant.Default,
    size = CardSize.Sm,
    header = {
        CardHeader(
            title = "Account",
            description = "Manage your account settings",
            action = { AppBadge(variant = BadgeVariant.Secondary) { AppText("Pro") } },
        )
    },
    footer = {
        Row(horizontalArrangement = Arrangement.End) {
            AppButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Sm) { AppText("Cancel") }
            Spacer(Modifier.width(8.dp))
            AppButton(onClick = {}) { AppText("Save") }
        }
    },
) {
    AppText("Card body content here.")
}
```

### Chips as filter group

```kotlin
val tags = listOf("Kotlin", "Swift", "Rust")
var selected by remember { mutableStateOf(setOf("Kotlin")) }

Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    tags.forEach { tag ->
        AppChip(
            label = tag,
            selected = tag in selected,
            onClick = { selected = if (tag in selected) selected - tag else selected + tag },
        )
    }
}
```

---

## Step 9: Add to `libs.versions.toml` (no extra deps needed)

The design system uses only:
- `compose.foundation` — `BasicText`, `BasicTextField`, Modifier APIs
- `compose.runtime` — CompositionLocal
- `compose.ui` — Modifier, Color, Dp, TextStyle

All of these are already in `compose-multiplatform`. No new catalog entries required.

---

## Guidelines

- **Never capture CompositionLocal in a Style lambda** — use `StyleScope` extensions (see Step 4)
- **Never set a default Style in a component parameter** — always pass `Style` (empty) and merge defaults inside `Modifier.styleable()`
- **You own this code** — the skill scaffolds a starting point; customize tokens and add variants freely
- **`@OptIn(ExperimentalStylesApi::class)`** required on every file using the Styles API; add to each component/style file
- **`indication = null`** on all clickable components — let Style `pressed {}` / `hovered {}` blocks handle visual feedback
- **Infinite animations** are not supported in Styles — use `rememberInfiniteTransition()` in the component body instead
- **Disabled state**: use `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` — not `MutableStyleState(interactionSource)` followed by manual mutation, and not `.enabled` (the property is `isEnabled`)
- **Dark mode**: swap `AppTheme.dark()` vs `AppTheme.light()` at the entry point; all Styles pick up correct tokens automatically via `StyleScope` extensions

---

## Verification

1. `./gradlew :core:designsystem:compileCommonMainKotlinMetadata` — tokens and styles compile in commonMain
2. `./gradlew :androidApp:assembleDevDebug` — AppTheme, AppButton, AppBadge, AppCard render
3. `./gradlew :desktopApp:run` — Desktop renders with same tokens
4. Toggle `darkTheme = true` in entry point — all component colors update correctly
5. Hover a button on Desktop — verify `hovered {}` style animation fires (JVM only)
6. Set `enabled = false` on `AppButton` — verify `disabled { alpha(0.38f) }` applies
7. Call `./gradlew :core:designsystem:jsTest` and `:wasmJsTest` — web targets compile clean

---

## Testing

```kotlin
// Design system testing is primarily visual — Roborazzi screenshot pairs (light + dark)
// for every token category and component, plus interaction tests for interactive tokens.

@Test fun `color_tokens_light screenshot`() {
    captureRoboImage("ds_color_tokens_light.png") {
        AppTheme(darkTheme = false) {
            val t = appTheme
            Column(modifier = Modifier.padding(t.spacing.lg)) {
                Box(Modifier.size(48.dp).background(t.colors.primary))
                Box(Modifier.size(48.dp).background(t.colors.secondary))
                Box(Modifier.size(48.dp).background(t.colors.surface))
                Box(Modifier.size(48.dp).background(t.colors.error))
            }
        }
    }
}

@Test fun `color_tokens_dark screenshot`() {
    captureRoboImage("ds_color_tokens_dark.png") {
        AppTheme(darkTheme = true) {
            val t = appTheme
            Column(modifier = Modifier.padding(t.spacing.lg)) {
                Box(Modifier.size(48.dp).background(t.colors.primary))
                Box(Modifier.size(48.dp).background(t.colors.secondary))
                Box(Modifier.size(48.dp).background(t.colors.surface))
                Box(Modifier.size(48.dp).background(t.colors.error))
            }
        }
    }
}

@Test fun `typography_scale screenshot`() {
    captureRoboImage("ds_typography_scale.png") {
        AppTheme {
            Column(modifier = Modifier.padding(appTheme.spacing.lg)) {
                AppText("Display Large",  style = AppTextStyle.DisplayLarge)
                AppText("Display Medium", style = AppTextStyle.DisplayMedium)
                AppText("Body Large",     style = AppTextStyle.BodyLarge)
                AppText("Label Small",    style = AppTextStyle.LabelSmall)
            }
        }
    }
}

@Test fun `spacing tokens match expected dp values`() {
    // Assert the compile-time constants — catches accidental token renames
    assertEquals(16.dp, AppSpacing().lg)
    assertEquals(8.dp,  AppSpacing().sm)
    assertEquals(4.dp,  AppSpacing().xs)
}
```

---

## Detekt Rules (PSI-based scanner)

The design system ships a custom detekt rule set that replaces regex-based violation
detection with full Kotlin PSI analysis. PSI traversal resolves variable aliases,
handles trailing-lambda syntax correctly, and enables two rules that regex cannot
express: component reimplementation detection and import boundary enforcement.

### Module location

Copy `detekt-rules/` from this skill into your project's `:core:designsystem` module:

```
core/designsystem/
├── detekt-rules/
│   ├── build.gradle.kts
│   ├── config/
│   │   └── detekt-design-system.yml
│   └── src/
│       ├── main/kotlin/GROUP_ID/designsystem/detekt/
│       │   ├── DesignSystemRuleSetProvider.kt
│       │   ├── HardcodedColorRule.kt
│       │   ├── HardcodedDpRule.kt
│       │   ├── MaterialThemeUsageRule.kt
│       │   ├── DirectTextStyleRule.kt
│       │   ├── NestedContainerRule.kt
│       │   ├── ComponentRegistryRule.kt
│       │   ├── ImportBoundaryRule.kt
│       │   ├── RedundantScreenTitleRule.kt
│       │   └── HardcodedGridColumnsRule.kt
│       └── test/kotlin/GROUP_ID/designsystem/detekt/
│           ├── HardcodedColorRuleTest.kt
│           ├── ComponentRegistryRuleTest.kt
│           ├── ImportBoundaryRuleTest.kt
│           ├── RedundantScreenTitleRuleTest.kt
│           └── HardcodedGridColumnsRuleTest.kt
```

Replace `GROUP_ID` with your actual group ID (e.g. `com.example.myapp`) — same as your convention plugin names in `build-logic/`.

### Wire into the Gradle build

In `core/designsystem/build.gradle.kts`:

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom("detekt-rules/config/detekt-design-system.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins(project(":core:designsystem:detekt-rules"))
}
```

Add to `settings.gradle.kts`:

```kotlin
include(":core:designsystem:detekt-rules")
```

### Run

```bash
# Check violations (CI mode)
./gradlew detekt

# Fix session (re-scan after each edit)
./gradlew detekt --rerun-tasks --continue
```

### Rules summary

| Rule ID | Severity | What it catches | What regex missed |
|---|---|---|---|
| `HardcodedColor` | Error | `Color(0xFF…)`, `Color(r,g,b)` | Variable aliases in local scope |
| `HardcodedDp` | Warning | `.dp` literals in layout modifiers | Modifier chains deeper than 1 level |
| `MaterialThemeUsage` | Error | `MaterialTheme.colors.*`, `MaterialTheme.colorScheme.*` | — |
| `DirectTextStyle` | Error | `TextStyle(…)` construction | — |
| `NestedContainer` | Warning | `Card { Card {` and `Surface { Surface {` | Trailing-lambda form `Card { }` |
| `ComponentRegistryViolation` | Warning | `@Composable fun MyButton` outside `core/designsystem/` | Entire class — regex can't see function definitions |
| `DesignTokenImportBoundary` | Error | `import …tokens.AppColors` in `feature/*/ui/` | Entire class — regex can't check import context |
| `RedundantScreenTitle` | Warning | `Text("…")` / `AppText("…")` with a string literal inside `*Content` / `*Screen` composables | Cannot infer that the composable is a screen or that a TopAppBar already shows the same string |
| `HardcodedGridColumns` | Warning | `GridCells.Fixed(N≥2)` — fixed column count ignores screen width | Cannot count GridCells arguments or distinguish `Fixed` from `Adaptive` |

### Configuration

Customize `config/detekt-design-system.yml`:

```yaml
design-system:
  ComponentRegistryRule:
    active: true
    # Must match the prefix resolved in Step 0 (Acme, GuildBase, ...) — not the literal
    # word "App" unless that's genuinely what Step 0 resolved to for this project.
    componentPrefix: 'Acme'
  HardcodedDp:
    active: true
    # To disable dp warnings while keeping color/MaterialTheme errors:
    # active: false
```

### Quick CLI fallback

When detekt is not yet wired into the project, use the Python scanner for a fast check:

```bash
python3 skills/kotlin-multiplatform-design-system/scripts/scan_design_violations.py \
  /path/to/project --json
```

The Python scanner covers rules 1–5 (`HardcodedColor` through `NestedContainer`) but
not `ComponentRegistryViolation` or `DesignTokenImportBoundary`.

---

## Common Anti-Patterns

- magic color literals in composables — `Color(0xFF6200EE)` written directly inside a `@Composable` instead of `appTheme.colors.primary`; the audit script flags `Color(0x…)` in any `/ui/` or `/presentation/` file that is not a token definition file
- hardcoded spacing in composables — `padding(16.dp)` or `padding(horizontal = 8.dp)` written directly instead of `padding(horizontal = appTheme.spacing.lg)`; the audit script flags `.dp` literals inside `padding(…)` calls in UI files
- accessing `AppTheme.colors`, `AppTheme.spacing`, or `AppTheme.typography` as static properties — these are instance properties; use the `appTheme` `@Composable` accessor or `AppTheme.LocalAppTheme.current` inside a composable
- title text in content body — a `Text("Screen Title")` composable inside the content column when it should be `AppTopAppBar(title = "Screen Title")`; makes the title scroll away and duplicates chrome
- action buttons outside the TopAppBar — a "Save" `AppButton` at the bottom of a form when it belongs in `AppTopAppBar(actions = { … })`; creates two interaction points for the same operation
- not consuming `PaddingValues` from `AppScaffold` — `AppScaffold { MyContent() }` without `Modifier.padding(paddingValues)` clips the content under the TopAppBar on status-bar devices
- using Material3 `MaterialTheme.colorScheme` alongside `AppTheme` — the two token systems conflict
- defining component variants as boolean parameters (`isOutlined`, `isDanger`) — use a sealed variant class
- putting design system tokens in `:feature:*` modules — tokens belong in `:core:designsystem` only
- skipping the `StyleScope` extension layer — leads to token access scattered across composables
- reading a `CompositionLocal` directly inside a `@Composable fun somethingStyle(): Style { val c = MaterialTheme.colorScheme.background; return Style { background(c) } }` — the value is captured at definition time, not consume time, and goes stale when the theme changes; always read the token via a `StyleScope` extension inside the `Style { }` body instead
- using `styleState.enabled = enabled` or a raw `MutableStyleState(interactionSource)` + manual mutation — the property is `isEnabled`, and `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` is the sanctioned pattern that stays current across recomposition
- providing a default with a body — `style: Style = Style { background(Color.Red) }` as a parameter default; always declare `style: Style = Style` (empty) and merge project defaults inside via `defaultStyle then style` in `Modifier.styleable(...)`
- using a Style to hold click handling, gesture detection, or other business logic — Styles are visual-only; behavior belongs on `Modifier.clickable`/gesture modifiers
- adding a `style: Style` parameter to a screen-level or raw layout composable (`FooContent`, `FooScreen`, a bare `Column`/`Row` used as page structure) — Styles are for components, not layouts; the official docs call this out explicitly as unclear to callers
- using `pressed {}` / `hovered {}` without `indication = null` on the same `clickable` modifier — both the Style animation and the default ripple render simultaneously, producing a visibly doubled effect
- animating an unbounded/looping effect (a pulsing loader, a spinner) inside a Style's `animate {}` block — Styles cannot express infinite animations; use `rememberInfiniteTransition()` in the component body instead
- defining a custom `Shape` inside a Style or animating a shape transition — custom shapes and shape animation are not yet supported by the Styles API (tracked as a future fix, not currently available)

If the design system feels inconsistent, check: (1) are all pages using `AppScaffold` + `AppTopAppBar`? (2) are spacing and colors coming from tokens or from hardcoded literals? (3) is there duplicated chrome (title, actions) in the content body?

---

## References

The `references/` directory contains project-facing documents the skill uses at generation time:

| File | Purpose | Usage |
|---|---|---|
| `references/design-system-template.md` | Living design system doc — tokens, component inventory, detekt overrides, audit log | Copy to `docs/design-system.md` in your project; fill in token values and prefix |
| `references/compose-styles-api-reference.md` | Extracted ground truth from the 9 official Compose Styles API doc pages (API surface, do's/don'ts, performance benchmarks, limitations) | Audit generated Style code against this before applying `/update-design-system` or reviewing a PR that touches `styles/` or `components/` |
| `scripts/derive_component_prefix.py` | Deterministically derives the component prefix (`App` placeholder replacement) from the project name | Run in Step 0 before generating any code; see precedence order there |

The skill reads `docs/design-system.md` when it exists in the target project to infer
the component prefix and token names before generating code. If the file is absent,
defaults (`App` prefix, token names as shown in the steps) are used.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — `:core:designsystem` follows the same convention plugin pattern
- `kotlin-multiplatform-design-system-extended` — additional components (`AppDialog`, `AppToast`, `AppTabs`, etc.) built on this foundation
- `kotlin-multiplatform-shared-resources` — fonts and icons loaded via `Res` accessors inside the design system
- `kotlin-multiplatform-preview-driven-development` — Desktop previews for each component variant using `PreviewParameterProvider`

---

## Output Style

When asked about design system setup or components, respond in this order:
1. recommendation (default token/component approach)
2. project structure (`:core:designsystem` layout)
3. code snippet (smallest useful token or component)
4. why that approach is preferred (no Material, full ownership)
5. main alternative (Material3 wrapper)

Keep snippets small. Use the user's package name and token names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-05 | Hardened Step 0 into a non-negotiable rule: `App` must never be written to disk literally for a real project — generate directly with the resolved prefix in the same pass, no "rename later" step. New audit detector `design system prefix mismatch [HIGH]` catches the case where `docs/design-system.md` records a resolved `COMPONENT_PREFIX` but `App*`-named declarations still exist under `core/designsystem` — verified against 5 scenarios (mismatch, consistent, genuinely-App, no-doc, unfilled-template). |
| 2026-07-05 | Auditing complete for Style API compliance: `audit_project.py` now has 5 dedicated detectors (`style default with body`, `style state wrong enabled property`, `style param on screen composable`, `stale compositionlocal in style function`, `missing indication null with style state`) enforcing the Do's/Don'ts/Limitations in `references/compose-styles-api-reference.md`. All verified with positive + negative test cases. `design-system-extended` audited component-by-component for actual Style API wiring — see its new "Style API coverage" table; `AppAvatar` fixed (had dead unused Style imports from an unfinished wiring attempt). |
| 2026-07-05 | Added new Step 0 — "App" is now formalized as a placeholder token (like `GROUP_ID`), resolved via precedence: `docs/design-system.md` COMPONENT_PREFIX -> user-stated -> derived from the project name -> `App` fallback. New `scripts/derive_component_prefix.py` deterministically derives a PascalCase prefix from `settings.gradle.kts` rootProject.name, the Gradle group ID, or the directory name, stripping generic noise words (app, android, ios, kmp, shared, compose, ...). Updated the Naming Rule, `docs/design-system.md` guidance, and the detekt `componentPrefix` example to stop treating `'App'` as a hardcoded literal. |
| 2026-07-05 | Audited against the 9 official Compose Styles API doc pages; added `references/compose-styles-api-reference.md` as ground truth for future audits. Fixed a real bug found across `AppButton`/`AppChip`/`AppTextField`/Guidelines: `styleState.enabled = enabled` used the wrong property name and a non-idiomatic construction pattern — corrected to `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` per the official examples. Added Style inheritance priority order, built-in/custom state facts, and 6 new anti-patterns backed by official Don'ts and Limitations (stale CompositionLocal capture in a `@Composable fun …Style()`, default-with-body style params, business logic in Styles, Style params on layout/screen composables, missing `indication = null` double-ripple, unsupported infinite/shape animation). Expanded Freshness rule with direct links to all 9 pages and the CMP-may-lag-Android caveat. |
| 2026-06-29 | `AppTheme` default changed from `darkTheme = false` to `isSystemInDarkTheme()` — all platforms now follow system dark mode automatically. Added `LocalAppDarkTheme` composition local (`Boolean?`) for in-app theme override. Removed hardcoded `darkTheme = false` from Desktop/iOS Step 7 wiring. |
| 2026-06-26 | Added component API placement guidance that maps shell components to slots, guarded regions to restricted scope templates, and leaf controls to data/variant APIs. |
| 2026-06-22 | Added `references/design-system-template.md` — project-facing living document covering tokens, component inventory, detekt overrides, multi-device preview coverage, and audit log. Wired copy instructions into Ownership Model section. |
| 2026-06-22 | Added `RedundantScreenTitleRule` (flags `Text`/`AppText` with string literals inside `*Content`/`*Screen` composables) and `HardcodedGridColumnsRule` (flags `GridCells.Fixed(N≥2)`). Added `@MultiDevicePreview` annotation (phone 360dp / tablet 673dp / desktop 1280dp) to `AppThemePreviewWrapper.kt`; applied to base light/dark variants of all 6 core component previews. Updated `/audit-design-visual` with duplicate title check and multi-device layout table. Updated `/record-design-baselines` with multi-device PNG naming. |
| 2026-06-22 | Added `detekt-rules/` PSI-based scanner module with 7 rules (HardcodedColor, HardcodedDp, MaterialThemeUsage, DirectTextStyle, NestedContainer, ComponentRegistryViolation, DesignTokenImportBoundary). Added `/record-design-baselines` and `/audit-design-visual` commands. Updated `/fix-design` to use detekt as primary scanner. |
| 2026-06-22 | Added `scripts/scan_design_violations.py` and `/fix-design` command: scans Compose files for hardcoded colors/dp/MaterialTheme/nested containers, fixes file-by-file, verifies with Roborazzi vision. |
| 2026-06-22 | Added ownership model section (project-owned tokens vs skill-owned components). Added stability tiers to component overview. Added `scripts/update_design_system.py` reference. |
| 2026-06-22 | Added `AppTextField` component (was missing from Step 6). Renamed `TextStyle` enum → `AppTextStyle` to avoid Compose collision. Fixed test code: `AppTheme.spacing.*` → `appTheme.*`, `Text()` → `AppText()`. Added `@OptIn` note to Steps 5–6. Fixed missing `sp`/`FontWeight` imports in Button/Badge/Chip/TextField style snippets. Fixed `CardSize` hardcoded dp → `AppSpacing()` tokens. Added cross-skill note for `AppScaffold`/`AppTopAppBar`. |
| 2026-06-06 | Initial release. |
