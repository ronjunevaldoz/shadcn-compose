---
name: kmp-shadcn-compose
description: >
  Consumes the published shadcn-compose library (io.github.ronjunevaldoz:shadcn-compose) —
  a shadcn/ui-inspired Compose Multiplatform component library with 70+ components. Covers
  Maven Central setup, the required @OptIn(ExperimentalFoundationStyleApi::class), the
  ShadcnTheme wrapper and its preset/baseColor/accent/isDark/baseRadius/ring parameters, and
  real component usage (ShadcnButton, ShadcnCard, etc.) verified against the library's own
  source. Alternative to kmp-compose-design-system's generated/owned approach — not
  both in the same project. Carries a real risk this skill exists specifically to disclose:
  a hard dependency on the experimental Compose Foundation Styles API that can break on any
  CMP upgrade, with no fix available except an upstream shadcn-compose release.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - shadcn-compose
    - ShadcnButton
    - ShadcnTheme
    - ShadcnCard
    - shadcn ui kotlin
    - ExperimentalFoundationStyleApi
    - shadcn kmp
    - shadcn compose multiplatform
    - ShadcnAccordion
    - ShadcnAlert
    - ShadcnAlertDialog
    - ShadcnAspectRatio
    - ShadcnAttachment
    - ShadcnAvatar
    - ShadcnBadge
    - ShadcnBreadcrumb
    - ShadcnBubble
    - ShadcnButtonGroup
    - ShadcnCalendar
    - ShadcnCarousel
    - ShadcnChart
    - ShadcnCheckbox
    - ShadcnChip
    - ShadcnCollapsible
    - ShadcnCombobox
    - ShadcnCommand
    - ShadcnContextMenu
    - ShadcnDialog
    - ShadcnDrawer
    - ShadcnDropdownMenu
    - ShadcnEmojiText
    - ShadcnEmpty
    - ShadcnField
    - ShadcnGroupCorners
    - ShadcnHoverCard
    - ShadcnIcon
    - ShadcnInputGroup
    - ShadcnInputOTP
    - ShadcnItem
    - ShadcnKbd
    - ShadcnLabel
    - ShadcnMarker
    - ShadcnMenubar
    - ShadcnMessage
    - ShadcnNavigationMenu
    - ShadcnPagination
    - ShadcnPopover
    - ShadcnProgress
    - ShadcnRadioButton
    - ShadcnRadioGroup
    - ShadcnResizable
    - ShadcnResizablePanelGroup
    - ShadcnScrollArea
    - ShadcnSelect
    - ShadcnSeparator
    - ShadcnSheet
    - ShadcnSidebar
    - ShadcnSkeleton
    - ShadcnSlider
    - ShadcnSpinner
    - ShadcnStepper
    - ShadcnSwitch
    - ShadcnTable
    - ShadcnTabs
    - ShadcnTabsList
    - ShadcnText
    - ShadcnTextField
    - ShadcnTextarea
    - ShadcnToast
    - ShadcnToggle
    - ShadcnToggleGroup
    - ShadcnTooltip
---

## When to Use This Skill

Use only when:
- The project's `/kmp-new-project` Step 6a design draft chose the shadcn-compose component
  library option (not the default owned scaffold), or
- The user explicitly asks to add shadcn-compose to an existing project, having accepted
  the experimental-API risk

**Never combine with `kmp-compose-design-system`.** They are alternative component
sources for the same layer — pick one.

**If a project already calls `ShadcnTheme(...)` in real source, treat shadcn-compose as
that project's established system — stop suggesting `App*`/`AppTheme` design-system
patterns for it, even in passing.** Check before recommending: a real `ShadcnTheme(` (or
trailing-lambda `ShadcnTheme {`) call site anywhere in the project's source means the
choice was already made; re-litigating it per-suggestion just reintroduces the mixing
this section already forbids. `kmp-audit`'s
`_detect_mixed_design_system_usage` makes this mechanically checkable — it flags a
project that calls both `ShadcnTheme(...)` and `AppTheme(...)`, which means either a
migration was left half-finished or a suggestion slipped through this rule.

**Actually adding this dependency requires explicit user choice** — via `/kmp-new-project`
Step 6a, `/kmp-migrate-to-shadcn`, or an equivalent direct confirmation. Never add it
silently.

**Mentioning a specific `Shadcn*` component as an option is fine, even in a project that
doesn't use this library yet** — for example, a layout-quality finding (mixed flat/card/
tabbed patterns across screens, per `scan_design_violations.py`'s `layout_inconsistency`
check) may suggest the matching component (`ShadcnTabsList`, `ShadcnItem`/`ShadcnItemGroup`,
etc.) as one option alongside consolidating to the project's existing pattern manually.
**Every such suggestion must state the experimental-API risk inline, in the same
message** — never a bare "use ShadcnTabsList" with the risk left for the user to discover
later. `kmp-compose-design-system`'s Ownership Model exists specifically to avoid
this risk (a hard dependency on `@OptIn(ExperimentalFoundationStyleApi::class)`, an actual
Jetpack Compose Foundation experimental annotation the Compose team can change or remove in
any release) — a suggestion that omits it isn't a complete recommendation.

**Trigger keywords:** shadcn-compose, ShadcnButton, ShadcnTheme, ShadcnCard, shadcn ui
kotlin, shadcn compose multiplatform, ExperimentalFoundationStyleApi, shadcn kmp. Plus
every individual `Shadcn*` component name — see the Component Keyword Matrix below for
the full list (64 real components as of shadcn-compose 0.2.3, verified against the library's own repo).

**Freshness rule:** this library is young and moves fast — four releases (`0.2.0` →
`0.2.1` → `0.2.2` → `0.2.3`) shipped between 2026-07-10 and this skill's latest recheck,
adding 2 new components (`ShadcnIcon`, `ShadcnStepper`). Recheck
[the README](https://github.com/ronjunevaldoz/shadcn-compose#readme) and
[Maven Central's actual repository](https://repo1.maven.org/maven2/io/github/ronjunevaldoz/shadcn-compose/)
directly — not `search.maven.org`, which lagged the real publish by over a day when
verified — before pinning a version.

## Component Keyword Matrix

Every real component in the library (64 files as of 0.2.3, verified live against the repo via
`scripts/fetch_component_signature.py`'s `_list_component_files()` — not guessed),
grouped by category, so a prompt naming any one of them routes here instead of only
the handful this skill used to list as trigger keywords (`ShadcnButton`/`ShadcnCard`
only, previously — a real routing gap this table exists to close).

| Category | Components |
|---|---|
| Form inputs | `ShadcnTextField`, `ShadcnTextarea`, `ShadcnCheckbox`, `ShadcnRadioButton`, `ShadcnRadioGroup` (in `ShadcnRadioButton.kt`), `ShadcnSwitch`, `ShadcnSlider`, `ShadcnSelect`, `ShadcnCombobox`, `ShadcnInputOTP`, `ShadcnInputGroup`, `ShadcnField`, `ShadcnToggle`, `ShadcnToggleGroup`, `ShadcnCalendar` |
| Buttons & actions | `ShadcnButton`, `ShadcnButtonGroup`, `ShadcnGroupCorners` |
| Overlays | `ShadcnDialog`, `ShadcnAlertDialog`, `ShadcnSheet`, `ShadcnDrawer`, `ShadcnPopover`, `ShadcnHoverCard`, `ShadcnTooltip`, `ShadcnDropdownMenu`, `ShadcnContextMenu`, `ShadcnCommand` |
| Feedback & status | `ShadcnAlert`, `ShadcnToast`, `ShadcnProgress`, `ShadcnSkeleton`, `ShadcnSpinner`, `ShadcnEmpty`, `ShadcnBadge`, `ShadcnMarker`, `ShadcnChip` |
| Navigation | `ShadcnTabs`, `ShadcnTabsList` (in `ShadcnTabs.kt`), `ShadcnBreadcrumb`, `ShadcnPagination`, `ShadcnNavigationMenu`, `ShadcnMenubar`, `ShadcnSidebar`, `ShadcnStepper`/`ShadcnStepperStep` (indicator only — Back/Next and step content are the caller's job) |
| Data display | `ShadcnCard`, `ShadcnCardHeader` (in `ShadcnCard.kt`), `ShadcnTable`, `ShadcnAvatar`, `ShadcnAvatarBadge`/`ShadcnAvatarFallback`/`ShadcnAvatarGroup` (in `ShadcnAvatar.kt`), `ShadcnChart`, `ShadcnKbd`, `ShadcnAspectRatio` |
| Layout & structure | `ShadcnItem`, `ShadcnItemGroup`/`ShadcnItemDescription`/`ShadcnItemTitle`/`ShadcnItemSeparator` (in `ShadcnItem.kt`), `ShadcnSeparator`, `ShadcnResizable`, `ShadcnResizablePanelGroup`, `ShadcnScrollArea`, `ShadcnCollapsible`, `ShadcnAccordion`, `ShadcnCarousel` |
| Text & content | `ShadcnText`, `ShadcnLabel`, `ShadcnEmojiText`, `ShadcnIcon` |
| Chat / messaging | `ShadcnMessage`, `ShadcnMessageScroller`, `ShadcnBubble`, `ShadcnAttachment` |
| Theming (not widgets) | `ShadcnTheme`, `ShadcnStylePreset`, `ShadcnBaseColor`, `ShadcnAccent` (all under `tokens/`, not `components/`) |

Component name and real file path are not always the same — several names above are
composables nested inside a differently-named file (`ShadcnTabsList` in `ShadcnTabs.kt`,
`ShadcnRadioGroup` in `ShadcnRadioButton.kt`, etc — each noted above). Always confirm the
real signature with `scripts/fetch_component_signature.py <ComponentName>` before using
one — this table is for routing/discovery, not a substitute for the verify-before-use
rule in Step 3.

---

## Recommendation First

Default to `kmp-compose-design-system` unless the user has explicitly chosen this
library and confirmed they accept the experimental-API risk (see
`kmp-compose-design-system`'s Ownership Model note, and the warning-gated Step 6a
flow in `/kmp-new-project`).

Why:
- `ExperimentalFoundationStyleApi` (`androidx.compose.foundation.style`) is a real Jetpack
  Compose Foundation experimental annotation — not something this library controls. A
  future Compose release can change or remove it with no migration path except waiting for
  shadcn-compose itself to catch up.
- This is a real dependency, not generated code — a CMP upgrade that breaks the
  experimental API breaks your build until shadcn-compose ships a compatible release,
  whereas the owned scaffold in `kmp-compose-design-system` stays on your own
  upgrade schedule.
- Once genuinely chosen (faster start, 70+ components, real published maintenance), use it
  as documented below — this skill isn't gatekeeping the choice, just making sure it's made
  with the risk visible.

---

## Prerequisites

- Project scaffolded with `kmp-feature-scaffold`
- The `/kmp-new-project` Step 6a component-library choice was shadcn-compose, with the
  second confirmation given — or the user has otherwise explicitly accepted the
  experimental-API risk
- Compose Multiplatform 1.11.1+ / Kotlin 2.4.0+ (matches this library's own CI pin —
  recheck the README before assuming an older toolchain works)

---

## Step 1: Gradle setup

```toml
# gradle/libs.versions.toml
[versions]
shadcn-compose = "0.2.3"

[libraries]
shadcn-compose = { module = "io.github.ronjunevaldoz:shadcn-compose", version.ref = "shadcn-compose" }
```

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.shadcn.compose)
        }
    }
}
```

Every file that references a component's `style` parameter needs the opt-in:

```kotlin
@file:OptIn(ExperimentalFoundationStyleApi::class)
```

**Do not also load `kmp-compose-design-system` in the same project** — the two are
alternative component sources for the same layer.

---

## Step 2: Theme setup

Wrap the app root in `ShadcnTheme` — verified against the library's real source
(`shadcn/core/.../theme/ShadcnTheme.kt`), not assumed:

```kotlin
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun App() {
    ShadcnTheme(
        preset = ShadcnStylePreset.Vega,   // default — real shadcn/ui also ships other presets
        baseColor = ShadcnBaseColor.Neutral,
        accent = ShadcnAccent.Base,
        isDark = isSystemInDarkTheme(),
    ) {
        // app content
    }
}
```

`ShadcnTheme` also accepts `baseRadius` (default `6.dp`) and `ring` (defaults to the
preset's own ring) if the project needs a different corner radius or focus-ring style than
the chosen preset ships.

### Picking a preset by app vibe

`ShadcnStylePreset` isn't a cosmetic label — each value carries a real, documented
personality (shape, spacing, animation timing, icon weight), verified against
`ShadcnStylePreset.kt`'s own KDoc:

| Preset | Documented personality | Fits |
|---|---|---|
| `Vega` | "Clean, neutral, and familiar" — balanced default | General-purpose, e-commerce, finance |
| `Nova` | "Reduced padding and margins," snappy, ultra-tight | Productivity tools, dense utility apps |
| `Maia` | "Rounded, with generous spacing," fluid and bouncy | Social, community, consumer/playful apps |
| `Lyra` | "Boxy and sharp. For mono fonts," blueprint aesthetic | Developer tools, technical/admin apps |
| `Mira` | "Made for compact interfaces," tightest timings | Dense dashboards, data-heavy screens |
| `Luma` | "Fluid, luminous, and soft," slow elegant fades | Wellness, travel, lifestyle, premium feel |
| `Sera` | "Editorial and typographic" | Content/reading apps, education |
| `Rhea` | Luma's softness, Nova's compactness | Soft aesthetic that still needs density |

`ShadcnBaseColor` (`Neutral`, `Stone`, `Zinc`, `Mauve`, `Olive`, `Mist`, `Taupe`) is the
neutral gray family for background/foreground/border — `Neutral` is a safe default;
`Stone` (warm) and `Zinc` (cool) are the two with an unambiguous undertone, useful when
the app's existing palette leans warm or cool. `ShadcnAccent` has 18 real named colors
(`Amber`, `Blue`, `Cyan`, `Emerald`, `Fuchsia`, `Green`, `Indigo`, `Lime`, `Orange`,
`Pink`, `Purple`, `Red`, `Rose`, `Sky`, `Teal`, `Violet`, `Yellow`, plus `Base` for no
override) — pick whichever matches the project's already-chosen brand accent by name.

`/kmp-new-project` Step 6a-ii runs this exact inference automatically from the project's
app type, using the same app-type category as the color-palette draft, and confirms the
choice with the user before generating — don't skip that confirmation when adding this
library outside the new-project flow either; always present the inferred preset/base
color/accent as a recommendation, not a silent default.

### Suggesting a component for a layout-quality problem

When an audit finds a genuine layout smell, the matching component below is worth
mentioning as one option — regardless of whether the project uses shadcn-compose yet —
**as long as the experimental-API risk is stated in the same message**:

| Layout smell (existing detector) | Suggested component |
|---|---|
| Mixed flat/card/tabbed patterns across screens (`scan_design_violations.py`'s `layout_inconsistency`, majority `tabbed`) | `ShadcnTabsList` |
| Same, majority `card` | `ShadcnCard` (consistent header/content/footer slots) |
| Same, majority `flat` | `ShadcnItem`/`ShadcnItemGroup` |
| Ad-hoc empty states with no consistent pattern | `ShadcnEmpty` |
| Ad-hoc multi-pane/split layouts | `ShadcnResizablePanelGroup` |
| Ad-hoc data grids | `ShadcnTable` |

This is a suggestion, not an instruction — the fix that doesn't add a new dependency
(consolidating to the project's own existing pattern) is still valid and often the
right call for a project not otherwise considering shadcn-compose. Present both options
when a layout-quality finding fires; don't default to only the shadcn-compose one.

---

## Step 3: Using components

### The one rule that matters more than any example below

**Never call a `Shadcn*` component with a parameter you haven't verified exists on its
real signature.** Do not assume a parameter exists by analogy to Jetpack Compose's own
`TextField`/Material components, to HTML/CSS attributes, or to another `Shadcn*`
component's shape — every component here has its own specific, independently-designed
API. Two confirmed, real examples of what guessing produces, found by fetching the
actual source rather than trusting a table like the one below:

- `ShadcnTextField` has **no `singleLine` parameter** — a real project's implementation
  used `singleLine = false` (a real Compose `TextField`/`BasicTextField` parameter,
  assumed to carry over) and it would not compile. The real multi-line component is the
  separate `ShadcnTextarea` (see below) — not a parameter toggle on `ShadcnTextField`.
- The component commonly assumed to be `ShadcnTabs` is actually named **`ShadcnTabsList`**
  — this skill's own component table said `ShadcnTabs` until this was checked against
  the real source.

Before writing a call to any component **not** shown with a verified signature below,
fetch the real one first — one command, no need to remember the file path or grep
pattern by hand:
```bash
python3 skills/kmp-shadcn-compose/scripts/fetch_component_signature.py <ComponentName>
```
It handles the two cases that break a naive lookup: a component living in a
differently-named file (checks the obvious filename first, then searches every
component file), and nested parens in a default value (uses a balanced-paren scan, not
a single-level regex, so the signature isn't truncated early). Or, if the project
already resolves the dependency, read it directly from the Gradle cache / IDE-decompiled
sources. Never skip this to save a lookup — a wrong guess costs more time than the
lookup would have.

### Verified signatures (checked against real source, 2026-07-12)

```kotlin
ShadcnButton(onClick = {}) { ShadcnText("Click me") }
ShadcnButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Sm) { ShadcnText("Outline") }
ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Delete") }
// ButtonVariant: Default | Outline | Secondary | Ghost | Destructive | Link — 6 variants, 5 sizes

ShadcnTextField(value = text, onValueChange = { text = it }, placeholder = "Email")
// value, onValueChange, modifier, enabled, label, placeholder, leadingIcon, trailingIcon,
// isError, supportingText, variant, style, keyboardOptions, keyboardActions, visualTransformation
// NO singleLine parameter — this is a single-line-only field by design.

ShadcnTextarea(value = prompt, onValueChange = { prompt = it }, placeholder = "Describe the scene")
// value, onValueChange, modifier, enabled, label, placeholder, isError, supportingText,
// variant, style, keyboardOptions, keyboardActions — the multi-line equivalent of
// ShadcnTextField above; wraps it internally. Use this for an HTML wireframe's <textarea>,
// never ShadcnTextField with a guessed multi-line parameter.

ShadcnSelect(value = selected, options = listOf("A", "B"), onValueChange = { selected = it }, label = { it })
// fun <T> ShadcnSelect(value: T?, options: List<T>, onValueChange: (T) -> Unit, modifier,
// label: (T) -> String = { it.toString() }, placeholder: String, variant, style, icon)

ShadcnCard(header = { ShadcnCardHeader(title = "Title") }) { ShadcnText("Body content") }
// fun ShadcnCard(modifier, variant, size, style, header: (@Composable () -> Unit)?,
// footer: (@Composable () -> Unit)?, content: @Composable ColumnScope.() -> Unit)
// Slot-based — header/footer are optional composable slots, not string parameters.
// ShadcnCardHeader(title, description, action, modifier) is a separate helper composable.

ShadcnCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
// checked, onCheckedChange: ((Boolean) -> Unit)?, modifier, indeterminate, enabled, style

ShadcnSwitch(checked = isOn, onCheckedChange = { isOn = it })
// checked, onCheckedChange: ((Boolean) -> Unit)?, modifier, enabled, style

ShadcnAvatar { ShadcnAvatarFallback("JD") }
// fun ShadcnAvatar(modifier, size: ShadcnAvatarSize, content: @Composable BoxScope.() -> Unit)
// Slot-based, with separate companion composables: ShadcnAvatarFallback(text, modifier),
// ShadcnAvatarBadge(modifier), ShadcnAvatarGroup(modifier) { content }.

ShadcnTabsList(items = tabItems, selected = selectedId, onSelectedChange = { selectedId = it })
// NOT "ShadcnTabs" — items: List<ShadcnTabItem>, selected: String, onSelectedChange: (String) -> Unit, modifier
```

See the
[component catalog](https://github.com/ronjunevaldoz/shadcn-compose/blob/main/docs/components.md)
for the full 70+ component list; each entry links to a live usage page in the library's own
catalog app (`app/shared/.../catalog/docs/*Doc.kt`) — treat that catalog app as the
authoritative usage reference for anything not verified above, not a guess from the name
alone.

No icon-library dependency is bundled *into* shadcn-compose — every component draws from
this library's own tokens for color/shape, not icon art. An icon needed in a screen built
with these components comes from a separate dependency:
[`heroicons-compose`](https://github.com/ronjunevaldoz/heroicons-compose)
(`io.github.ronjunevaldoz:heroicons-outline:<version>`, Maven Central, Heroicons compiled
to CMP `ImageVector` — Outline variant only today; Solid/Mini/Micro not yet built), or
`kmp-imagevector-generator` for anything Heroicons doesn't cover. Do not
assume any icon set ships automatically with the `shadcn-compose` dependency itself.

### Density/sizing requests — reach for the library's own parameters first

This isn't only about the literal word "compact." Any request that's really about
density or sizing — "make this compact," "tighter," "denser," "smaller," "more
breathing room," "roomier" — has two real levers already built into this library.
Check both before writing a single custom `Style { }` override:

1. **Whole-app/whole-screen density → `ShadcnTheme`'s `preset` parameter.** The preset
   table earlier in this skill is not cosmetic — `Mira` ("made for compact interfaces,"
   tightest timings) and `Nova` ("reduced padding and margins," snappy) both compress
   spacing/timing across every component at once. If the request is about the app's
   overall feel, changing `preset` is very likely the actual fix — not a per-component
   override, and not introducing a custom modifier at all.
2. **One component's size → its own preset `Size` enum.** Every sized `Shadcn*`
   component ships one. Verified against real source (2026-07-31):

```kotlin
sealed interface ButtonSize {
    data object Xs : ButtonSize    // 28.dp height — the compact preset
    data object Sm : ButtonSize    // 32.dp height
    data object Md : ButtonSize    // 36.dp height — default
    data object Lg : ButtonSize    // 40.dp height
    data object Icon : ButtonSize  // 36×36.dp square, icon-only
}
```

`ShadcnCard` and `ShadcnAvatar` (`ShadcnAvatarSize`) also take a `size` parameter — verify
each component's actual `Size` enum with `fetch_component_signature.py` before assuming it
does or doesn't have one; do not assume a component lacks a preset just because one isn't
shown above. Only reach for a custom `Style { }` override when the request needs a value
neither lever covers (an exact one-off `height`, not a general density change) — and say
so explicitly when doing it, since it's the exception, not the norm.

---

## Step 4: Composing a real screen from multiple components

Knowing one component's signature isn't the same as knowing how several fit together into
a good screen. Worked example — a settings-style list of rows inside a card, every symbol
below individually verified against real source (`fetch_component_signature.py`), not
copied wholesale from the library's own KDoc usage examples:

```kotlin
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun AccountSettingsCard(members: List<Member>, onView: (Member) -> Unit) {
    ShadcnCard(
        header = { ShadcnCardHeader(title = "Team members", description = "${members.size} people") },
    ) {
        ShadcnItemGroup {
            members.forEach { member ->
                ShadcnItem(variant = ShadcnItemVariant.Outline) {
                    ShadcnAvatar { ShadcnAvatarFallback(member.initials) }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        ShadcnItemTitle(member.name)
                        ShadcnItemDescription(member.email)
                    }
                    ShadcnButton(onClick = { onView(member) }) { ShadcnText("View") }
                }
            }
        }
    }
}
```

What's real here and why it's shaped this way:
- `ShadcnCard`'s `content` lambda is `ColumnScope` — `ShadcnItemGroup` drops straight in,
  no wrapper needed.
- `ShadcnItemGroup` **already paints a hairline separator between each `ShadcnItem`** —
  confirmed in its own KDoc ("Vertically stacks a list of ShadcnItems with a hairline
  separator between each"). Adding a manual `ShadcnSeparator()` between items double-draws
  the divider. `ShadcnSeparator` is for dividing unrelated sections, not rows inside a group
  — that's already handled.
- `ShadcnItem`'s `content` lambda is `RowScope`, not a set of named slots — a `Row`/`Column`
  and plain `Modifier.weight()` inside it is the normal way to lay out avatar/text/action,
  the same as composing any other `RowScope` content.

**A real trap this example exists to name**: `ShadcnItem`'s own KDoc usage example shows
`ShadcnItemMedia { }`, `ShadcnItemContent { }`, and `ShadcnItemActions { }` as if they were
real slot composables. Checked against the actual file
(`ShadcnItem.kt`) — **none of the three exist anywhere in the repo.** Only
`ShadcnItem`, `ShadcnItemGroup`, `ShadcnItemTitle`, `ShadcnItemDescription`, and
`ShadcnItemSeparator` are real functions. Even the library's own official KDoc example is
not a substitute for `fetch_component_signature.py` — this is the concrete case proving why.

For a split view (list + detail), the same discipline applies: wrap the two panes in
`ShadcnResizablePanelGroup` (verified in the Component Keyword Matrix's Layout & structure
row) rather than a bare `Row` with manual weights — it gets a draggable divider and clamped
min/max weights for free, matching what `kmp-layout-system`'s Pattern A
wireframe expects for a nav+side+main layout.

### Layout-pattern lookup when nothing here covers the shape needed

[Shadcn Studio](https://shadcnstudio.com/) — **not the official shadcn/ui project, and not
this library.** Verified directly (2026-07-13): a third-party, independently-run paid
catalog ($99–$849 one-time) of 800+ UI blocks and 20+ page templates built on real
shadcn/ui, explicitly "not affiliated with shadcn/ui." Useful here for exactly one thing —
seeing how a layout *shape* (a dashboard, a pricing table, a settings page) is typically
structured — never as a source to copy code from.

**Why code can't be copied from it directly**: its output is React/JSX + Tailwind, not
Kotlin/Compose. Treat any block from it exactly like an HTML/CSS wireframe — run it through
`kmp-layout-system`'s "Translating an External HTML/CSS Wireframe" mapping
table, then verify every resulting `Shadcn*` component name with
`fetch_component_signature.py` before using it, the same discipline Step 3/Step 4 already
require for this library's own components. A block behind the paid tier is not accessible
without payment — don't assume free access to a specific block by name.

---

## Testing

No tests to write for the library itself — it's an external dependency, and its own CI
already covers it. For screens built with these components, use
`kmp-roborazzi`'s screenshot-testing pattern the same as any other Compose
UI; nothing shadcn-compose-specific changes that workflow.

---

## Common Anti-Patterns

- combining this skill with `kmp-compose-design-system` in the same project — pick one component source, never both
- adding this dependency without the user having seen the experimental-API warning — route through `/kmp-new-project` Step 6a or get explicit confirmation first
- forgetting `@OptIn(ExperimentalFoundationStyleApi::class)` on a file that references a component's `style` parameter — a compile error, not a runtime issue, but confusing without knowing the cause
- pinning a version from `search.maven.org` — it lagged the real Maven Central publish by over a day when verified; check `repo1.maven.org` or the README directly
- assuming an icon set ships bundled with the `shadcn-compose` dependency itself — it doesn't; `heroicons-compose` is a real, separate, published dependency (Maven Central, same author), not a guess or an unmet gap
- reaching for a custom `Style { }` padding/height override on any density/sizing request (compact, tighter, denser, smaller, roomier, ...) before checking `ShadcnTheme`'s own `preset` (e.g. `Mira`/`Nova` for whole-app density) and the component's own `Size` enum (`ButtonSize.Xs`, etc.) — one of the two almost always covers it; a custom override should be the fallback, not the first move
- treating this as a stable, slow-moving dependency — 3 releases shipped in 3 days during this skill's own research; recheck before every use, not just once
- suggesting a `Shadcn*` component for a layout-quality finding without stating the experimental-API risk in the same message — a suggestion that omits it isn't complete, even if it's "just an option"
- copying a component's own official KDoc usage example verbatim — `ShadcnItem`'s KDoc shows `ShadcnItemMedia`/`ShadcnItemContent`/`ShadcnItemActions` as if real; none exist anywhere in the repo (confirmed by searching the actual file). Verify every individual symbol with `fetch_component_signature.py`, even ones shown in the library's own documentation
- manually adding `ShadcnSeparator()` between `ShadcnItem`s inside a `ShadcnItemGroup` — the group already paints a hairline separator between each item; a manual one double-draws it
- suggesting a `Shadcn*` component as the *only* fix for a layout-quality finding — the no-new-dependency fix (consolidate to the project's existing pattern) is still valid and should be presented alongside it, not replaced by it
- assuming a component's parameter exists by analogy to Jetpack Compose's own Material components (e.g. `singleLine` on a text field) — every `Shadcn*` component has its own independently-designed API; a real project's implementation used a hallucinated `singleLine` parameter on `ShadcnTextField` that doesn't exist
- guessing a component's top-level name from a pattern (e.g. assuming "ShadcnTabs" because `ShadcnButton`/`ShadcnCard` follow that shape) instead of checking the real source — the real name is `ShadcnTabsList`, found only by verifying, not by pattern-matching against other components in the same family

---

## Output Style

When asked to add or use shadcn-compose, respond in this order:
1. Confirm the choice was made deliberately (via `/kmp-new-project` Step 6a or explicit
   request) and the experimental-API risk was seen — ask if unclear, don't add it silently
2. Gradle setup (version catalog entry + dependency)
3. `ShadcnTheme` wrapper at the app root
4. The specific component(s) requested, with the exact variant/size parameters needed
5. Note the `@OptIn(ExperimentalFoundationStyleApi::class)` requirement on any new file

---

## Related Skills

- `kmp-compose-design-system` — the default, owned-scaffold alternative this skill exists to be compared against; see its Ownership Model note for the full risk tradeoff
- `kmp-feature-scaffold` — project must be scaffolded first
- `kmp-roborazzi` — screenshot-test screens built with these components the same as any other Compose UI
- `/kmp-migrate-to-shadcn` — the file-by-file migration path from an existing `kmp-compose-design-system` project to this library, with the full `App*`→`Shadcn*` mapping table
- `kmp-layout-system` — required translation step for any layout pulled from Shadcn Studio or any other HTML/React source; never copy code from an external site directly

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-31 | Fixed a real drift: this skill's own icon-dependency note read as if no real icon library was available at all, but `heroicons-compose` (`io.github.ronjunevaldoz:heroicons-outline`, Maven Central) shipped since — verified real and live. Corrected the note to name it directly instead of only pointing at the icon-generator skill. Added a "Density/sizing requests" section after a report that these were producing hand-rolled `Style{}` overrides instead of using the library's own two real levers: `ShadcnTheme`'s `preset` param (`Mira`/`Nova` compress spacing/timing app-wide) for whole-app/whole-screen density, and each component's own `Size` enum (documented the real, verified `ButtonSize`: `Xs`/`Sm`/`Md`/`Lg`/`Icon`) for one component. Deliberately not scoped to the literal word "compact" — covers any density/sizing phrasing. Added a matching anti-pattern. Also extended `kmp-audit`'s `_detect_raw_component_bypass` to cover shadcn-compose projects (see that skill's own changelog). |
| 2026-07-17 | Added explicit "if `ShadcnTheme` is already in use, stop suggesting `App*`/`AppTheme`" guidance, mirrored in `kmp-compose-design-system`'s own doc. The "never combine" rule existed but was never mechanically checked — added `kmp-audit`'s `_detect_mixed_design_system_usage`, scoped to both theme wrappers coexisting (not individual `App*` component names, which risked a false positive on unrelated real identifiers like `AppConfig(...)`). Caught and fixed a real bug in my own first draft: the regex only matched `ShadcnTheme(`/`AppTheme(` with parens, missing the common parenthesis-free trailing-lambda call shape (`AppTheme { ... }`) both functions support since every other param is defaulted. 4 new regression tests. |
| 2026-07-13 | Added a layout-pattern lookup reference (Shadcn Studio, shadcnstudio.com) for when no wireframe template or component here covers the needed shape — verified directly it's a third-party paid catalog, explicitly not affiliated with shadcn/ui or this library. Labeled clearly as a shape reference only: its output is React/JSX, not Kotlin/Compose, so any block from it must go through `kmp-layout-system`'s HTML-translation table plus `fetch_component_signature.py` verification, never copied directly. |
| 2026-07-13 | Added Step 4: a worked multi-component composition example (settings-list-in-a-card, using `ShadcnCard`/`ShadcnItemGroup`/`ShadcnItem`/`ShadcnAvatar`/`ShadcnButton` together) — closes a real gap where the skill only taught single-component verification and per-element HTML mapping, never how to assemble components into a good screen. Found and documented a real trap in the process: `ShadcnItem`'s own official KDoc usage example references `ShadcnItemMedia`/`ShadcnItemContent`/`ShadcnItemActions` as if they were real slot composables — none exist anywhere in the repo (confirmed by searching the actual source, not just the doc comment). Also documented that `ShadcnItemGroup` auto-separates its items, so a manual `ShadcnSeparator` between them double-draws. 2 new anti-patterns. |
| 2026-07-13 | Rechecked the real README and Maven Central directly (not `search.maven.org`): latest published version is `0.2.3`, not `0.2.1` — updated the Gradle version pin. Component count grew 62 → 64: found 2 new real components via a live file-list diff (`ShadcnIcon` — tinted icon renderer resolving `LocalShadcnContentColor`; `ShadcnStepper`/`ShadcnStepperStep` — multi-step progress indicator, presentational only, same pattern as `ShadcnTabs`/`ShadcnAccordion`). Added both to the Component Keyword Matrix and frontmatter keywords. |
| 2026-07-13 | Fixed a real keyword-routing gap: trigger keywords only named 2 of the library's 62 real components (`ShadcnButton`, `ShadcnCard`) — a prompt naming any other real component (e.g. "how do I use ShadcnDialog") wouldn't route here. Added a full Component Keyword Matrix, grouped by category (form inputs, overlays, feedback, navigation, data display, layout, text, chat, theming), built from the live component file list (`scripts/fetch_component_signature.py`'s `_list_component_files()`, not guessed) plus a follow-up check for composables nested inside a differently-named file (`ShadcnRadioGroup` in `ShadcnRadioButton.kt`, `ShadcnAvatarBadge`/`Fallback`/`Group` in `ShadcnAvatar.kt`, `ShadcnItemGroup`/`Description`/`Title`/`Separator` in `ShadcnItem.kt`). All 62 top-level component names added to frontmatter keywords. |
| 2026-07-12 | Fixed two real bugs found in a consumer project's implementation, both from this skill's own incomplete verification: `ShadcnTextField` was called with a hallucinated `singleLine` parameter (doesn't exist — real multi-line component is the separate `ShadcnTextarea`), and this skill's own component table said `ShadcnTabs` when the real name is `ShadcnTabsList` (also wrong in `scan_design_violations.py`'s layout-quality suggestion, now fixed with a regression test). Rewrote Step 3 with 9 signatures verified directly against real source (Button, TextField, Textarea, Select, Card+CardHeader, Checkbox, Switch, Avatar+companions, TabsList) and a mandatory rule: never call a component with a parameter not verified against its real signature, with the fetch command to do that verification. Added `scripts/fetch_component_signature.py` — turns that verification from a manual GitHub lookup into one command; handles a component living in a differently-named file (checks the obvious filename first, then searches every component file) and nested parens in a default value (balanced-paren scan, not a single-level regex). Verified 6 more signatures with it (Checkbox, RadioGroup/RadioButton, Slider, Table, Dialog) to expand `kmp-layout-system`'s HTML mapping table. 2 new anti-patterns, 3 new script regression tests. |
| 2026-07-11 | Initial release — Maven Central setup, `ShadcnTheme` wrapper (verified against real source), component usage (verified against real KDoc examples), and the experimental-API risk this skill exists specifically to disclose rather than hide. Gated to explicit user choice via `/kmp-new-project` Step 6a, never suggested unprompted. Added "Picking a preset by app vibe" — full `ShadcnStylePreset`/`ShadcnBaseColor`/`ShadcnAccent` reference (verified against their own KDoc/source), and wired `/kmp-new-project` Step 6a-ii to auto-infer a preset/base color/accent recommendation from the same app-type category as the color-palette draft, always confirmed before generating, never a silent default. Added `/kmp-migrate-to-shadcn` — a full `App*`→`Shadcn*` migration command for existing design-system projects, with an honest mapping table (verified against the real component catalog, not assumed 1:1 parity) flagging the components with no direct equivalent (`AppScaffold`, `AppTopAppBar`, `AppNavigationBar`, `AppIcon`, `AppIconButton`) for explicit user decision rather than a guessed replacement. Wired `scan_design_violations.py`'s `layout_inconsistency` finding to suggest a matching `Shadcn*` component (`ShadcnTabs`/`ShadcnCard`/`ShadcnItem`) as an option regardless of whether the project uses shadcn-compose yet — every such suggestion states the experimental-API risk inline, and is presented alongside (never instead of) the no-new-dependency fix. |
