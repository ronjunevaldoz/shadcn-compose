# AGENTS.md — shadcn-compose

This project uses [kmm-agent-skills](https://github.com/ronjunevaldoz/kmm-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

A shadcn/ui-inspired Compose Multiplatform component library built on the Compose
Styles API (`@ExperimentalFoundationStyleApi`) -- token-based theming, sealed variant
systems, zero Material dependency. Targets Android, iOS (arm64 + simulator), Desktop
(JVM), and Web (JS + WasmJS).

Group ID: `io.github.ronjunevaldoz`   Artifact: `shadcn-compose`   Published to: Maven Central (dry-run wired, not yet released)

## Dependencies

- **tailwind-compose** (sibling project at `/Users/ronvaldoz/StudioProjects/tailwind-compose`,
  group `io.github.ronjunevaldoz`) — a real Gradle `implementation` dependency in
  `:library`'s `commonMain`, resolved from **Maven Central** (verified directly against
  `repo1.maven.org`'s `maven-metadata.xml`, not the search index, which lags actual
  publishes by a while) at version `0.1.0`, matching `gradle/libs.versions.toml`.
  `settings.gradle.kts` no longer needs a `mavenLocal()` fallback for it. It's a
  utility-class/atomic styling layer (Tailwind-style `Modifier`/`TextStyle` extensions
  for spacing, color, shadow, filters, etc.) sitting *underneath*/*alongside*
  shadcn-compose's component-level Style API variants — not a replacement for the sealed
  variant system. Same relationship as Tailwind CSS + shadcn/ui on the web: utilities
  handle one-off layout/spacing, the component library handles semantic variants
  (`ButtonVariant`, `CardVariant`, etc.).
  - `implementation`, not `api` — no `Shadcn*` function signature should ever require a
    caller to import `io.github.ronjunevaldoz.tailwind.*`.
  - Bumped to `0.1.1` — also live on Maven Central, verified the same way. `Oklch(l, c,
    h).toColor()` in its `tailwind-core` module is the preferred way to add any *new*
    color token going forward (traces 1:1 to shadcn's real `oklch(...)` CSS source,
    no separate hand-verified hex step) — see `ShadcnColors.kt`'s `card`/`popover`/
    `sidebar` fields for the reference example.
- **tailwind-icons-outline** (same sibling project, added at `0.1.1`) — a full
  Heroicons-Outline set compiled to Compose `ImageVector`. **`:app:shared`-only,
  never `:library`** — the catalog app's examples (e.g. Date Picker's trigger icon)
  may use it, but every `:library` component still uses plain text glyphs for icons
  (`"☰"`, `"✕"`, `"↓"`, ...), matching this library's zero-icon-set-dependency stance.
  Render an `ImageVector` with `androidx.compose.foundation.Image(imageVector = ...,
  colorFilter = ColorFilter.tint(...))` — not `androidx.compose.material.Icon`, which
  would pull in a Material dependency this project deliberately avoids.
  - Only reach for a `tailwind-compose` utility where shadcn-compose's own `Style`/
    `StyleScope` DSL has **no equivalent property at all** — don't use it to duplicate
    something the Style block already handles (e.g. its `twCard()` combinator does its
    own `.shadow().clip().background()`, which would double-paint on top of a
    `Style{}` block that already sets `background()`/`shape()`). Confirmed example:
    `StyleScope` has no shadow/elevation property, so `ShadcnCard`'s `CardVariant.Elevated`
    applies `Modifier.shadowMd(shape)` on the outer modifier chain, *before*
    `.styleable(...)` (shadow draws behind the Style block's own background/border, not
    instead of them) — see `ShadcnCard.kt`'s comment for the full reasoning, including why
    the same `Shape` must be passed to both to keep the shadow and the card's rounded
    corners concentric.

## Skill routing

| Topic | Skill |
|---|---|
| New design system component | `kotlin-multiplatform-design-system` / `kotlin-multiplatform-design-system-extended` |
| Publishing to Maven Central | `kotlin-multiplatform-library-publishing` |
| iOS / SPM distribution | `kotlin-multiplatform-xcframework-spm` |
| API surface management | `kotlin-multiplatform-library-publishing` (apiCheck / apiDump — not yet wired, see Notes) |
| Platform-specific implementations | `kotlin-multiplatform-expect-actual` |
| Catalog app navigation | `kotlin-multiplatform-navigation` |
| Unit / integration tests | `kotlin-multiplatform-unit-testing` (not yet wired, see Notes) |
| Code quality (detekt, ktlint) | `kotlin-multiplatform-code-quality` |
| CI automation | `kotlin-multiplatform-ci-github-actions` |
| Architecture audit | `kotlin-multiplatform-audit` |
| Harvest consumer lessons | `kotlin-multiplatform-audit` (`--harvest` mode via `/kmm-harvest-lessons`) |

## Module graph

| Module | Purpose |
|---|---|
| `:library` | Published artifact (`io.github.ronjunevaldoz:shadcn-compose`) -- tokens, `ShadcnTheme`, components, styles |
| `:core` | Small shared utility module (currently minimal, stock demo code) |
| `:app:shared` | Catalog/docs app shared code -- navigation, sidebar, per-component doc pages (not published) |
| `:app:androidApp` / `:app:desktopApp` / `:app:webApp` | Catalog app platform entry points (not published) |
| `:app:iosApp` | Xcode project for the catalog app on iOS (not published) |

## Published artifacts

| Artifact | Module |
|---|---|
| `io.github.ronjunevaldoz:shadcn-compose` | `:library` |

## API surface rules

- No `binary-compatibility-validator` wired yet -- consider adding it via
  `kotlin-multiplatform-library-publishing` before the first real Maven Central release,
  so accidental public API breaks are caught in CI.
- Until then, treat any signature change to a public `Shadcn*` component or `styles/*Variant`
  sealed interface as a breaking change requiring a version bump.

## Component styling rules

1. **Variants are flat, stateless sealed interfaces** (`data object` per variant, e.g.
   `ButtonVariant`, `ChipVariant`) -- no hardcoded theme values baked into the object.
2. **Variant -> `Style` mapping is a `@Composable fun <Variant>.rememberStyle(): Style`**
   (see `styles/ButtonStyles.kt`, `styles/ChipStyles.kt`), not a plain non-composable
   `val style: Style get() = ...` property. Read the theme via `ShadcnTheme.current`,
   then `remember(this, <every theme sub-object the block reads>) { Style { ... } }`.
   **The remember keys must list every theme field the block actually reads** (`colors`,
   `shapes`, `spacing`, ...) -- switching a `ShadcnStylePreset` changes `shapes`/`spacing`
   without necessarily changing `colors`, so an incomplete key list leaves a stale cached
   `Style` until an unrelated key happens to change too. **Known bug:** `ChipStyles.kt`'s
   `remember(this, colors)` is missing `shapes`/`spacing` despite reading `shapes.full`
   and `spacing.md`/`spacing.xs` -- exactly the caching failure this rule exists to
   prevent. Fix by keying on all three like every other `*Styles.kt` file already does.
3. Inside a `Style { }` block passed to `Modifier.styleable`, use the
   `StyleScope.colors/shapes/spacing/typography` extensions
   (`theme/StyleScopeExtensions.kt`, backed by `StyleScope.currentValue`), not the
   `@Composable shadcnTheme` getter -- `Style` blocks can resolve outside normal
   composition, and `currentValue` is the only read guaranteed fresh there.
4. **`contentColor()` set inside a `Style` block is not reliable for text painted by a
   *nested* `ShadcnText`/`BasicText`/`BasicTextField`** on a live dark-mode toggle, for
   variants with no explicit `background()` (confirmed live: `ChipVariant.Outline`).
   Fix: read the resolved color via a plain (non-memoized) `@Composable get()` and pass
   it explicitly into `ShadcnText(color = ...)` / `BasicTextField(textStyle = ...,
   cursorBrush = ...)` -- never rely on ambient Style-color inheritance for text.
   **Known regression:** `ShadcnChip.kt` dropped its `color = variant.contentColor`
   pass-through when `ChipVariant.rememberStyle()` was introduced -- needs re-adding.
5. Context-aware modifiers may default *derivable, non-per-instance* values (color,
   shape, corner radius) from `CompositionLocal` when the caller omits them -- e.g.
   `shadcnFocusRing(isFocused, shape = null, color = null)` resolving `shape`/`color`
   internally. **Per-instance interaction state (`isFocused`, `checked`, `pressed`) must
   stay an explicit required parameter** -- there is no single global "the currently
   focused node," so it can never come from a `CompositionLocal`. "Parameter-free call
   sites" applies only to the derivable subset, not to per-instance state.
6. `shadcnFocusRing`'s ring **must be drawn before `drawContent()`**, not after -- see the
   doc comment on `styles/FocusRing.kt` explaining why (a centered `Stroke` at `offset =
   0` relies on the component's own opaque background painting over the stroke's inward
   half; drawing the ring after content leaves that half visible as a translucent
   overlay, roughly doubling the ring's apparent thickness). Fixed -- draw order is now
   ring-then-`drawContent()`, matching the doc comment.
   Also fixed in the same pass: `shadcnFocusRing`'s corner-radius growth math
   unconditionally added the ring's outward `growth` to *every* corner, including
   corners that are legitimately square (`px == 0`, e.g. the flush inner edge between
   two `ShadcnButtonGroup` items via `LocalGroupCorners`) -- rounding a corner that
   should stay perfectly square. Now `corner(px) = if (px <= 0f) 0f else px + growth`.
   `ShadcnChip` was also the one of 7 `shadcnFocusRing` call sites missing an explicit
   `shape` -- it fell back to the default `shapes.lg` instead of its own pill shape
   (`shapes.full`), visibly mismatching the ring to the pill underneath. `ShadcnButton`/
   `ShadcnToggle` are correctly exempt from needing an explicit shape: they rely on the
   null-shape default specifically to auto-inherit `LocalGroupCorners` inside a
   `ButtonGroup`/`ToggleGroup`, so forcing a shape there would break grouping instead.
7. Model compound-component spacing/density (icon+label pairs, group item corners,
   leading/trailing addons) as **explicit per-position parameters passed down the
   composition** (see `ButtonGroupCorners` / `ToggleCorners`), not a `CompositionLocal`.
   Real shadcn's `data-slot="..."` is inert HTML markup that exists only for CSS
   attribute selectors (`[data-slot=button-group]:gap-2`) -- it carries no spacing logic
   of its own, and Compose has no CSS-selector equivalent to translate it into. A
   `LocalShadcnDataSlots`-style CompositionLocal is a plausible-sounding but non-
   equivalent reimplementation; only introduce one once a *second* real component needs
   the exact same slot values -- don't add it speculatively.
8. `ShadcnStylePreset` bundling `shapes`/`spacing`/`typography`/`ring`/`animations`/
   `icons` per entry is correct. **`ShadcnBaseColor` is a separate axis from style
   presets**, matching real shadcn where `tailwind.baseColor` and the style/registry
   choice are independent `components.json` fields. Real shadcn's `tailwind.baseColor`
   only accepts **`neutral`, `gray`, `zinc`, `stone`, `slate`** -- five values, nothing
   else. `Mauve`/`Olive`/`Mist`/`Taupe` (already in `ShadcnBaseColor.kt`) are **not**
   part of the official spec -- they're this library's own deliberate extension beyond
   it; document them that way rather than as "official," and note `Gray`/`Slate` (both
   real, currently missing here) would be needed for actual spec parity.
9. Color conversion already has one documented, verified methodology
   (`docs/shadcn-parity.md` §5): oklch -> linear sRGB -> gamma-encoded hex, **no
   perceptual gamut mapping applied**. Don't introduce a second "wide-gamut
   approximation" path for the same tokens -- one checked-against-real-source conversion
   method per color, not two competing ones.
   **`tailwind-compose`'s `tailwind-core` module has a real, tested `Oklch(lightness,
   chroma, hue).toColor()` utility** (`io.github.ronjunevaldoz.tailwind.core.Oklch`,
   exported transitively through the `tailwind-compose` facade this project already
   depends on) implementing this exact same conversion -- prefer it over a fresh
   hand-verified hex literal for any *new* token going forward: it traces 1:1 to
   shadcn's real `oklch(L C H)` CSS source values with no separate "trust this hex"
   step. `ShadcnColors.kt`'s `card`/`onCard`/`popover`/`onPopover`/`sidebar`/`onSidebar`
   fields use it already (verified pixel-identical to the prior hand-computed hex via
   the existing screenshot baselines, no new goldens needed). **Not yet migrated:**
   the rest of `ShadcnColors.kt`'s default palette and all 7 `ShadcnBaseColor` families
   still use hex literals -- a deliberate, explicitly scoped decision (converting ~150
   existing values needs fresh real oklch lookups per base-color family, not a
   mechanical swap) rather than an oversight; don't assume it's "the next obvious step"
   without asking first.
10. **`ShadcnColors` has per-container-role tokens** (`card`/`onCard`, `popover`/
    `onPopover`, `sidebar`/`onSidebar`, `sidebarPrimary`/`onSidebarPrimary`,
    `sidebarAccent`/`onSidebarAccent`, `sidebarBorder`, `sidebarRing`), matching real
    shadcn's `--card`/`--popover`/`--sidebar*` CSS variable families (verified against
    `ui.shadcn.com/r/colors/zinc.json`) -- these are a **separate namespace** from the
    generic `surface`/`background`/`muted` tokens, not aliases of them, even though the
    default light palette's hex values happen to coincide (real shadcn's own light theme
    does too; only dark mode raises card/popover/sidebar to a distinct panel tone).
    **Use `card`/`popover` correctly, don't default back to `surface`:** `ShadcnCard` →
    `card`/`onCard`; floating anchored panels (`ShadcnPopover`, `ShadcnDropdownMenu`,
    `ShadcnContextMenu`, `ShadcnCommand`, `ShadcnCombobox`, `ShadcnHoverCard`,
    `ShadcnNavigationMenu`'s panel) → `popover`/`onPopover`; `ShadcnSidebar` → `sidebar`/
    `onSidebar` for the rail, `sidebarAccent`/`onSidebarAccent` for the active menu item,
    `sidebarBorder` for its edge divider. **`ShadcnDialog`/`ShadcnSheet`/`ShadcnDrawer`
    stay on plain `background`** -- confirmed against real dialog.tsx/sheet.tsx source,
    they use `bg-background`, not `bg-popover`, unlike every anchored-popup component.
    `ShadcnTooltip` stays on `onSurface`/`background` (inverted) -- confirmed against
    real tooltip.tsx's `bg-foreground text-background`, a genuinely different pattern
    from the popover family, not a card/popover role at all.
    For `ShadcnBaseColor`'s 7 base families: light-mode card/popover/sidebar are set
    `== background` (matches real shadcn universally); dark-mode values are derived via
    `blend()` (a 50/50 RGB midpoint of `background`/`secondary`) rather than
    hand-verified per-family oklch, since that reproduces Zinc's real verified value
    (`#18181B`) to within one rounding unit -- see `blend()`'s doc comment in
    `ShadcnBaseColor.kt`. `sidebarPrimary`/`onSidebarPrimary` are aliased to
    `primary`/`onPrimary` rather than importing real shadcn's own dark-mode zinc.json
    anomaly (`sidebar-primary` is a hardcoded blue in dark mode only, unrelated to this
    project's `ShadcnStylePreset` accent system, and an artifact of their theme
    generator, not a deliberate two-tier design -- their own light palette keeps
    `sidebar-primary == primary`).

## Component creation checklist

Every new component's doc comment and screenshot-test coverage must account for these
before considering the component done -- most of the bugs found in review passes so far
(ring corner-radius mismatch, Collapsible fade-through overlap, ScrollArea thumb
mispositioning, Tooltip/HoverCard focus-steal flicker, missing Carousel width) were
caused by one of these being silently skipped, not by a typo:

1. **List every visual state** the component can be in (default, hover, focused,
   pressed, selected, disabled, error, loading, empty) and confirm each one actually has
   a screenshot test -- not just the ones that happen to be easy to trigger without a
   gesture. A state with no golden is a state nobody is actually checking.
2. **List every animation/transition** (expand/collapse, fade, slide) and check it at a
   **mid-transition frame**, not just the two settled endpoints. `mainClock.autoAdvance =
   false` + `mainClock.advanceTimeBy(...)` is the safe, gesture-free way to do this (see
   `ShadcnCollapsible`'s doc comment) -- freezing the animation clock is not a gesture
   simulation and has never caused the hangs `performMouseInput`/`performTouchInput` drag
   simulation has (see point 4). The Collapsible overlap bug only showed up mid-fade;
   both settled states looked completely correct.
3. **Positive scenario:** does the component render/behave correctly with realistic
   content at its intended size? **Negative scenario:** what happens with zero items, an
   empty string, content wider than the container, or a size the caller forgot to
   constrain (see `ShadcnCarousel`'s doc comment -- an unconstrained `HorizontalPager`
   silently expands to fill the ambient width instead of failing loudly). Prefer a
   component that documents a hard requirement (`modifier` must set a main-axis size) or
   handles the empty case explicitly over one that silently does something ambient/
   unpredictable.
4. **Any pointer-driven interaction** (drag-to-resize, drag-to-scroll, hover-to-open)
   needs its *math* extracted into a plain, non-Composable function and unit tested
   directly (see `resizablePanelFraction` for `ShadcnResizablePanelGroup`,
   `scrollDragDeltaToContentDelta` for `ShadcnScrollArea`) -- never write the positioning
   math inline inside a `rememberDraggableState { }` lambda where it can only be
   exercised by actually dragging. Simulating a real drag/hover gesture via
   `performMouseInput`/`performTouchInput` has hung this project's JVM test worker
   outright before; the *rendering* half (does the resulting layout look right at a given
   state) is instead covered by screenshot-testing fixed values (a few different
   `initialFraction`s, a few different scroll positions) rather than by reaching those
   values through a live gesture.
5. **Any `Popup`-backed hover-triggered overlay** (`Tooltip`, `HoverCard`) must pass
   `focusable = false` to `ShadcnAnchoredPopup` -- a focusable popup competes with the
   trigger's own `hoverable()` for focus the instant it opens, which flickers the popup
   open/closed in a loop on desktop. Click-triggered overlays (`Popover`, `DropdownMenu`,
   `Dialog`) keep the `focusable = true` default; their content genuinely needs keyboard
   focus and Escape-to-dismiss.
6. **A component's own doc page must not duplicate itself.** `ComponentDetailScreen`
   shows `examples.first()` once under "Preview & Code"; the "Examples" section must
   only render `examples.drop(1)` (and only render the whole section when that's
   non-empty) -- otherwise every single-example component shows the identical
   preview+code twice on its own page.

## Registry parity status

Every real shadcn/ui component (`shadcn-ui/ui`, `apps/v4/registry/new-york-v4/ui`,
fetched fresh, not from memory) is implemented **except one, by deliberate scope
decision**:

- **`native-select`** -- skipped. Real shadcn's version renders an actual HTML
  `<select>`, so on the web it triggers the *browser's own OS-level* native dropdown --
  not custom-drawn JS. Every component in this library, `ShadcnSelect` included, is
  100% pure Compose drawing with zero platform-widget interop. Matching `native-select`'s
  real behavior would mean embedding actual native pickers per platform (`UIPickerView`
  on iOS, Android `Spinner`, no equivalent on Desktop, and Compose-for-Web doesn't render
  to real DOM at all) -- a fundamentally different, much larger scope than every other
  component here. A Compose-drawn "alias" with the same name wouldn't actually be native,
  so it wasn't built as a consolation. User-confirmed scope decision, not an oversight.
- **`direction`** -- not a gap, not implemented, and shouldn't be: Radix's
  `DirectionProvider`/`useDirection` exists because the web has no built-in RTL/LTR
  concept. Compose already has one natively (`LocalLayoutDirection`) -- there's nothing
  to port.
- **`form`** -- consolidated into `ShadcnField`. Real shadcn's `form.tsx` is itself a thin
  wrapper of the same `Field` primitives around `react-hook-form`; this library has no
  form-state-management dependency (every component here is already caller-hoisted
  state), so there's no separate `ShadcnForm`.
- **`input`**/**`sonner`** -- implemented as `ShadcnTextField`/`ShadcnToast` (naming
  choices, not gaps).
- **`data-table`** -- N/A, not a gap: real shadcn's own docs say *"instead of a
  data-table component ... a guide on how to build your own"* on top of `Table` +
  TanStack Table. No Compose Multiplatform equivalent of TanStack Table exists, and
  building one is a much larger scope than this component library's remit.
- **`toast`** (as distinct from `sonner`) -- N/A: real shadcn's own `toast` docs page
  says it's *"deprecated. Use sonner instead"* -- already covered by the `sonner` ->
  `ShadcnToast` mapping above, not a second component to build.
- **`date-picker`** -- N/A as a standalone component in real shadcn either (their docs:
  *"built using a composition of the `<Popover />` and the `<Calendar />`
  components"*) -- added as a catalog doc example (`DatePickerDoc.kt`) composing this
  library's own `ShadcnPopover` + `ShadcnCalendar`, not a new `:library` component.
- **`select`** -- **was a real gap**, fixed. `ShadcnSelect` previously existed only as
  an internal utility (this catalog app's own theme/base-color/accent pickers in
  `CatalogTopBar`), never exposed as a documented public component, and its trigger
  used `ButtonVariant.Outline` (a filled-button look) rather than real shadcn's actual
  `SelectTrigger` style (`border-input bg-transparent`, a bordered field). Redesigned
  the trigger to match, widened `value` from `T` to `T?` so it can show placeholder
  text (backward-compatible -- `CatalogTopBar`'s existing non-null call sites still
  compile unchanged), fixed its popup panel to use the `popover`/`onPopover` container-
  role tokens instead of generic `surface`/`onSurface` (same class of fix as the
  Popover/DropdownMenu/ContextMenu/Command/Combobox/HoverCard/NavigationMenu pass
  earlier), and gave it a proper `SelectDoc.kt` catalog page.
- **`dropdown-menu`/`command`/`menubar`/`context-menu`** -- **was a real gap**, fixed.
  All four previously took a flat `items: List<ShadcnDropdownMenuItem>` (or
  `ShadcnCommandItem`), with no way to reproduce real shadcn's default demos --
  a "My Account" label, a grouped set of items, a separator, then a destructive item.
  `ShadcnDropdownMenu`/`ShadcnContextMenu`/`ShadcnMenubarMenu` were redesigned from a
  data-list API to a slot-based `content: @Composable ShadcnDropdownMenuScope.() -> Unit`
  (`ShadcnDropdownMenuItem`/`ShadcnDropdownMenuLabel` are now composables in that scope,
  freely mixed with the existing `ShadcnDropdownMenuSeparator`; `ShadcnContextMenu` and
  `ShadcnMenubarMenu` reuse the same scope since they already shared the row rendering).
  `ShadcnCommand` kept a data model instead (`groups: List<ShadcnCommandGroup>`, each
  with an optional `heading`) rather than going full slot-based, because its live
  search-filtering needs to inspect every item's label up front -- a freely-composed
  slot API would need extra machinery to hide non-matching children mid-composition.
  This was a deliberate, user-approved API redesign (not additive/backward-compatible --
  nothing is published to Maven Central yet, so no external consumers exist).

**shadcn's "AI Elements" family is implemented**, not out of scope: `ShadcnMarker`,
`ShadcnMessage`/`ShadcnMessageGroup`, `ShadcnBubble`/`ShadcnBubbleGroup`,
`ShadcnAttachment`/`ShadcnAttachmentGroup`, `ShadcnMessageScroller` (`attachment.tsx`,
`bubble.tsx`, `marker.tsx`, `message.tsx`, `message-scroller.tsx` in the real registry).
An earlier pass in this project excluded them by guessing their purpose from the
filename alone, without fetching real source -- fetching and reading the actual `.tsx`
confirmed they're general chat/AI-assistant UI primitives, not narrowly AI-chat-specific
in a way that would exclude them from a general-purpose component library, and the user
explicitly asked for them to be added. **Lesson generalized**: never classify a shadcn
component as out-of-scope from its filename -- always fetch
`raw.githubusercontent.com/shadcn-ui/ui/main/apps/v4/registry/new-york-v4/ui/<name>.tsx`
and read it first.

If a new component lands in the real registry, re-check this list before assuming it's
missing.

## Notes for future sessions

- **The Compose Styles API does not match what the `kotlin-multiplatform-design-system`
  skill assumes.** The real annotation in Compose Multiplatform 1.11.1 is
  `androidx.compose.foundation.style.ExperimentalFoundationStyleApi`, not
  `ExperimentalStylesApi`. `padding()` does not exist on `StyleScope` -- use
  `contentPadding(...)`. Verify any new Style API usage against the real jar
  (`~/.gradle/caches/modules-2/files-2.1/org.jetbrains.compose.foundation/`) with a
  compile spike before writing component code, the same way `library/build.gradle.kts`'s
  history did -- don't trust the skill's code samples verbatim for this API surface.
- **Focus rings are drawn directly with `Stroke`, not `dropShadow`.** shadcn's real focus
  indicator (`focus-visible:ring-[3px] ring-ring/50`) is a crisp, hard-edged ring; the
  Style API's `dropShadow` always rasterizes through an offscreen bitmap and visibly
  blurs a ring this thin. Every interactive component composes the shared
  `styles/FocusRing.kt` (`Modifier.shadcnFocusRing`) for this -- do not reintroduce
  `dropShadow` or a border-width-reservation hack to fix focus-related resizing bugs.
- **Colors and hover semantics are checked against real shadcn/ui source**, not memory --
  see `ui.shadcn.com/docs/theming` for token values and
  `github.com/shadcn-ui/ui/blob/main/apps/v4/registry/new-york-v4/ui/*.tsx` for exact
  per-variant Tailwind classes (hover treatment, border presence, disabled opacity).
  When adding a new component, fetch its real source first rather than approximating.
  The same rule applies to `components.json` fields (`tailwind.baseColor`, `style`) --
  see item 8 above for a case where this was skipped and a fabricated-sounding but
  actually-real set of custom base colors got documented as "official" when it isn't.
- Roborazzi screenshot tests are wired (`library/src/jvmTest/`, Robolectric-less
  JVM/Desktop capture, see `docs/visual-testing.md`) and compile/pass cleanly (verified
  2026-07-08 via `./gradlew :library:verifyRoborazziJvm`). Plain `:library:jvmTest` does
  *not* pixel-compare against the committed goldens (`captureRoboImage` is a no-op writer
  without the record/verify system properties the wrapper tasks set) -- after any
  component visual change, run `./gradlew :library:recordRoborazziJvm` to update the
  affected goldens, then `./gradlew :library:verifyRoborazziJvm` to confirm a clean diff
  before committing.

## Commands installed

See `.claude/commands/kmm-*.md` for available slash commands.
Key commands:
- `/kmm-run-audit` — architecture audit with per-finding remediation
- `/kmm-harvest-lessons` — collect patterns to upstream to skills
- `/kmm-verify` — full validation pipeline (build + test + apiCheck)
- `/kmm-check-updates` — check for skill updates
