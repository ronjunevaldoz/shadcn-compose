---
name: kmp-compose-design-system
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
  author: kmp-agent-skills
  last-updated: '2026-07-26'
  keywords:
    - design system
    - Compose Styles API
    - AppTheme
    - custom theme
    - design tokens
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
    - rememberStyle
    - StyleVariant
    - Modifier.composed
    - stateless variant
    - context-aware modifier
    - memoized style
---

## When to Use This Skill

Use this skill when the user asks to:
- Set up a custom design system, theme, or component library in a KMP project
- Avoid Material Design and build unstyled/custom components
- Use the new Compose Styles API (`@ExperimentalStylesApi`)
- Create reusable UI components with a variant system (like shadcn)
- Add dark mode support via custom tokens
- Wire AppTheme, tokens, or custom Composables into `:core:designsystem`

**If a project already calls `ShadcnTheme(...)` in real source, it's already on
`kmp-shadcn-compose` — don't suggest `App*`/`AppTheme` for it.** Same
rule as that skill's own "never combine" note, stated here too since either skill can be
the one consulted first. `kmp-audit`'s
`_detect_mixed_design_system_usage` flags a project calling both theme wrappers.

**Trigger keywords:** design system, custom theme, AppTheme, design tokens,
ButtonVariant, Compose Styles, ExperimentalStylesApi, custom components,
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

Concrete KMP design-system mapping:
- `AppScaffold`, `AppTopAppBar`, `AppCard`, `AppDialog`, `AppBottomSheet` -> slot API
- `CardHeader`, `ToolbarRow`, `SectionHeader`, `ActionRow` -> restricted scope template when the region needs guardrails
- `AppButton`, `AppBadge`, `AppTextField`, `AppText`, `AppChip` -> data/variant APIs first; add slots only if callers truly need custom body content

---

## Screen Layout Contract

Full content (the hard rule + template): `references/screen-layout-contract.md`.

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

**Utility-class layer:** [`tailwind-compose`](https://github.com/ronjunevaldoz/tailwind-compose)
is a stable-API-only (no `@ExperimentalStylesApi`), Maven Central-published library providing
Tailwind-style utility modifiers (spacing, layout, color, typography) for Compose Multiplatform.
Because it depends on nothing experimental, it's safe to add as a real dependency alongside the
generated `tokens/`/`components/` layers above — use it for one-off utility styling in screen
code where writing a full token+style pair would be overkill, not as a replacement for
`components/`. Component libraries that depend on the experimental Styles API (e.g.
[`shadcn-compose`](https://github.com/ronjunevaldoz/shadcn-compose), published to Maven
Central as of `0.2.1`) are still not recommended here — publication doesn't remove the
risk this skill is scaffold-based specifically to avoid: a real dependency on
`@ExperimentalFoundationStyleApi` still breaks on the next CMP release that changes that
API. See the Changelog entry below.

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
missing Roborazzi screenshot tests, and missing commonTest UI interaction tests (`runComposeUiTest`)
for feature `*Content.kt` files — all four required, no exceptions — so coverage gaps get
caught with the rest of the design cleanup. `scripts/scaffold_preview_coverage.py` generates
all four missing files at once.

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
cp skills/kmp-compose-design-system/references/design-system-template.md \
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

### Ring vs border — never animate `borderWidth`

Don't add a separate "ring" primitive to imitate CSS `box-shadow`-based focus rings — the
actual problem a `ring` solves in CSS is that `box-shadow` never participates in the box
model, so it can't jitter content. The Compose fix is a rule, not a new component:

**Never animate `borderWidth` (or `borderBottomWidth`, etc.) in a state block.** Reserve
the final width at rest — `borderColor(Color.Transparent)` if the variant has no border
at rest — and animate only `borderColor` on `focused {}`/`selected {}`. The border's
footprint never changes size, so focusing/selecting a component never re-measures or
shifts a sibling's position — the same bug class as an unrotated icon swap shifting an
accordion trigger (see `kmp-roborazzi`'s "Layout stability regression
test").

```kotlin
// ❌ WRONG — width animates from 0 to 2.dp, content shifts under focus
focused { animate { borderWidth(2.dp); borderColor(colors.borderFocus) } }

// ✅ CORRECT — width reserved (transparent) at rest, only color animates
data object Default : ButtonVariant {
    override val style = Style {
        background(colors.primary)
        borderWidth(2.dp)
        borderColor(Color.Transparent)
    } then buttonInteractionStyle   // buttonInteractionStyle's focused{} only touches borderColor
}
```

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

- Project scaffolded with `kmp-feature-scaffold`
- CMP 1.11.1+ (`compose-multiplatform = "1.11.1"` in `libs.versions.toml`)
- Convention plugin `GROUP_ID.feature.ui` or `GROUP_ID.core` available

---

## Step 0: Determine the component prefix

Full content: `references/step0-component-prefix.md`.

## Step 1: Create `:core:designsystem` module

Full content: `references/step1-module-setup.md`.

## Step 2: Design Tokens

Full content: `references/step2-design-tokens.md`.

## Step 3: AppTheme + CompositionLocals

Full content: `references/step3-apptheme.md`.

## Step 4: StyleScope Extensions

Full content: `references/step4-stylescope-extensions.md`.

## Step 5: Variant Systems

Full content: `references/step5-variant-systems.md`.

## Step 6: Core Components

Full content: `references/step6-core-components.md`.

## Component Previews

Full content: `references/component-previews.md`.

## Step 7: Wire AppTheme in platform entry points

Full content: `references/step7-wire-apptheme.md`.

## Step 8: Usage patterns

Full content: `references/step8-usage-patterns.md`.

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

Full content: `references/testing.md`.

## Detekt Rules (PSI-based scanner)

Full content: `references/detekt-rules.md`.

## Common Anti-Patterns

- magic color literals in composables — `Color(0xFF6200EE)` written directly inside a `@Composable` instead of `appTheme.colors.primary`; the audit script flags `Color(0x…)` in any `/ui/` or `/presentation/` file that is not a token definition file
- inlining `variant.style then size.style` directly in a modifier chain instead of `rememberStyle(variant, size)` — rebuilds the merged descriptor on every recomposition instead of once per variant/size change
- bundling multiple components into one file under `core/designsystem/components/` — every generated template in this skill and `kmp-compose-design-system-extended` puts one component per file; `kmp-audit`'s `_detect_combined_component_file` flags 3+ components in one file
- bundling multiple `*Variant` sealed types into one `styles/` file — `styles/ButtonStyles.kt` holds exactly `ButtonVariant`, matched 1:1 with `components/AppButton.kt`; `kmp-audit`'s `_detect_combined_style_file` flags 2+ variant types in one file
- a `Modifier` extension taking a theme value as a required parameter with a hardcoded literal default (`fun Modifier.appDivider(color: Color = Color(0xFFE4E4E7))`) — resolve it internally via `Modifier.composed { AppTheme.LocalAppTheme.current... }` so call sites stay parameter-free
- a sealed variant `data object` holding a pre-resolved `Color`/`Dp` value instead of a `Style` descriptor built from `StyleScope` extensions (`colors.primary`, not a captured `Color` literal) — breaks theme switching and light/dark parity
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
| `references/step0-component-prefix.md`, `step1-module-setup.md`, `step2-design-tokens.md`, `step3-apptheme.md`, `step4-stylescope-extensions.md`, `step5-variant-systems.md`, `step6-core-components.md`, `component-previews.md`, `step8-usage-patterns.md`, `testing.md`, `detekt-rules.md`, `screen-layout-contract.md`, `changelog.md` | This skill's own implementation content, split out of `SKILL.md` for progressive disclosure | Load the specific file named in the pointer left under the matching heading in `SKILL.md` |
| `scripts/derive_component_prefix.py` | Deterministically derives the component prefix (`App` placeholder replacement) from the project name | Run in Step 0 before generating any code; see precedence order there |

The skill reads `docs/design-system.md` when it exists in the target project to infer
the component prefix and token names before generating code. If the file is absent,
defaults (`App` prefix, token names as shown in the steps) are used.

---

## Related Skills

- `kmp-feature-scaffold` — `:core:designsystem` follows the same convention plugin pattern
- `kmp-compose-design-system-extended` — additional components (`AppDialog`, `AppToast`, `AppTabs`, etc.) built on this foundation
- `kmp-audit` — `_detect_combined_component_file`/`_detect_combined_style_file` mechanically check the one-component-per-file and one-component's-variants-per-file conventions above
- `kmp-shared-resources` — fonts and icons loaded via `Res` accessors inside the design system
- `kmp-compose-preview-driven-development` — Desktop previews for each component variant using `PreviewParameterProvider`
- `kmp-shadcn-compose` — the published-library alternative to this skill's owned-scaffold approach; see its own skill for the experimental-API risk tradeoff in full
- `/kmp-migrate-to-shadcn` — the file-by-file migration path if a project decides to switch fully from this skill's generated components to shadcn-compose
- `kmp-code-quality` — the `@Composable`-returning-`Unit`-must-be-PascalCase rule and other naming conventions this skill's components already follow by convention

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

Full content: `references/changelog.md`.

