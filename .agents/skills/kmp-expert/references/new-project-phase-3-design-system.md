# Phase 3 — Design system and previews (Steps 6-7)

Part of `kmp-expert` — a phase of the `/kmp-new-project` pipeline.
**[App] only** — a Library project skips straight from Phase 2 to Phase 4.

Load this file when the command reaches this phase; do not load all phases up front. The command itself holds the phase index and the gates between them.

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

