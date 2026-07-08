# shadcn-compose

[![CI](https://github.com/ronjunevaldoz/shadcn-compose/actions/workflows/ci.yml/badge.svg)](https://github.com/ronjunevaldoz/shadcn-compose/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
![Platforms](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-blue.svg)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A [shadcn/ui](https://ui.shadcn.com)-inspired component library for **Kotlin Multiplatform / Compose
Multiplatform** — Android, iOS, Desktop (JVM), and Web (JS + WasmJS) from one `commonMain` source set.
Token-based theming and sealed variant systems (`ButtonVariant`, `CardVariant`, ...) built on the
experimental **Compose Styles API**. No Material dependency — every component is drawn from this
library's own design tokens, the same way real shadcn/ui owns its Tailwind-driven CSS instead of
wrapping a design system it doesn't control.

**No icon-library dependency either.** Every icon inside `:library` is a plain text/Unicode glyph
(`↓`, `✕`, `⌄`, `☰`, ...) rendered via `ShadcnText` — not a swappable prop in most components, since
it's inline in the component's own source, except where a component already exposes an icon slot
(`ShadcnAlert(icon = {...})`, `ShadcnTextField(leadingIcon/trailingIcon = {...})`, etc.), which
accepts any `@Composable` content. The catalog app (`/app/shared` only, never `/library`) additionally
depends on [`heroicons-outline`](https://github.com/ronjunevaldoz/heroicons-compose) (full
Heroicons-Outline set as Compose `ImageVector`s) for real icons in some doc examples, purely to show
what a consumer app could layer on top — that dependency is never pulled into the published library
artifact.

If you're an AI agent working in this repo: the [component catalog](#component-catalog) below is
written as a keyword index — search it for the UI pattern you need ("modal", "chat bubble", "date
picker", "toast notification", ...) before assuming a component doesn't exist or reaching for a
raw `Popup`/`Box` from scratch. Full usage examples for every entry live in the catalog app
(`/app/shared/.../catalog/docs/*Doc.kt`) and render live at `installation`/`introduction` plus one
page per component.

## Installation

Not yet published to Maven Central (`VERSION_NAME` is still a `-SNAPSHOT`). Once released:

```toml
# gradle/libs.versions.toml
[versions]
shadcn-compose = "0.1.0"

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

Every file that references a component's `style` parameter needs an opt-in for the experimental
Compose Styles API:

```kotlin
@file:OptIn(ExperimentalFoundationStyleApi::class)
```

## Component catalog

Every entry maps to a real shadcn/ui `base/*` component (or is a deliberate, documented deviation —
see [Registry parity](#registry-parity) below). **Keywords** are search terms an agent or developer
might actually type — match on any of them, not just the component name.

### Core primitives

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnButton` | Trigger an action; 6 variants (default/outline/secondary/ghost/destructive/link), 5 sizes | button, CTA, submit, click handler, icon button |
| `ShadcnCard` | Bordered content container with optional header/footer slots | card, panel, box, container, login form, settings section |
| `ShadcnBadge` | Small status/count label, usually inline with text | badge, tag, status pill, count indicator, label chip |
| `ShadcnChip` | Compact, often removable/selectable token (filter chips, multi-select tags) | chip, filter tag, removable tag, token, pill button |
| `ShadcnTextField` | Single-line text input (real shadcn's `input.tsx`) | text field, input box, text input, form field |
| `ShadcnText` | Themed text with the design system's typography scale | text, label, typography, heading, body copy |

### Forms & inputs

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnLabel` | Accessible field label, pairs with any input | label, form label, field title |
| `ShadcnCheckbox` | Boolean toggle, supports indeterminate | checkbox, tick box, boolean input, agree to terms |
| `ShadcnRadioGroup` | Pick exactly one of N mutually-exclusive options | radio button, radio group, single choice, option list |
| `ShadcnSwitch` | On/off toggle (real shadcn's `switch.tsx`, iOS-style pill) | switch, toggle switch, on/off, settings toggle |
| `ShadcnToggle` | Pressable two-state button (bold/italic-style toolbar buttons) | toggle button, pressed state, formatting toolbar button |
| `ShadcnSlider` | Drag a thumb to pick a value/range on a track | slider, range input, volume slider, value picker |
| `ShadcnToggleGroup` | A row of `Toggle`s where selection is mutually exclusive (or multi-select) | toggle group, segmented control, button group toggle |
| `ShadcnInputGroup` | Text field with leading/trailing addons (icon, button, unit label) | input group, input with icon, prefixed input, input addon |
| `ShadcnButtonGroup` | Visually joined row of buttons with shared/flush corners | button group, split button, joined buttons, toolbar |
| `ShadcnTextarea` | Multi-line text input | textarea, multiline input, comment box, message box |
| `ShadcnField` / `ShadcnFieldGroup` | Label + control + description/error layout for building forms (consolidates real shadcn's `form.tsx`) | form field, field group, form builder, validation error text |
| `ShadcnInputOTP` | Boxed one-time-passcode input, fills left-to-right | OTP input, verification code, 2FA code, PIN input |

### Data display

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnAvatar` | Circular user/entity image with fallback initials | avatar, profile picture, user icon, initials circle |
| `ShadcnAspectRatio` | Constrain a child to a fixed width:height ratio | aspect ratio, 16:9 box, responsive image container |
| `ShadcnSeparator` | Thin dividing line, horizontal or vertical | separator, divider, hairline, horizontal rule |
| `ShadcnKbd` | Styled keyboard-key label | kbd, keyboard shortcut, hotkey badge |
| `ShadcnItem` / `ShadcnItemGroup` | List row with media/content/actions slots (order rows, settings rows) | list item, row, list tile, settings row |
| `ShadcnEmpty` | Empty-state placeholder (icon/title/description/action) | empty state, no results, zero state, placeholder screen |

### Feedback

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnAlert` | Inline banner for a message that needs attention | alert, banner, warning box, info box, callout |
| `ShadcnProgress` | Determinate progress bar | progress bar, loading bar, upload progress |
| `ShadcnSkeleton` | Pulsing placeholder shape while content loads | skeleton, loading placeholder, shimmer, content loader |
| `ShadcnSpinner` | Indeterminate rotating loading indicator | spinner, loading spinner, activity indicator |
| `ShadcnToast` / `ShadcnToaster` | Transient stacked notification (real shadcn's `sonner.tsx`) | toast, snackbar, notification popup, sonner |

### Disclosure & navigation

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnCollapsible` | Single show/hide section behind a trigger | collapsible, expandable section, show more/less |
| `ShadcnAccordion` | Multiple collapsible sections, FAQ-style | accordion, FAQ, expandable list, collapsible group |
| `ShadcnTabs` | Switch between panels via a tab strip | tabs, tab bar, tabbed interface, segmented view |
| `ShadcnBreadcrumb` | "Home › Section › Page" navigation trail | breadcrumb, nav trail, path navigation |

### Overlays & navigation

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnTooltip` | Text-only hint on hover/focus (inverted colors, matches real shadcn) | tooltip, hover hint, help text popup |
| `ShadcnPopover` | Click-triggered anchored panel for arbitrary rich content | popover, floating panel, dropdown panel |
| `ShadcnHoverCard` | Hover-triggered rich preview panel (unlike Tooltip, not text-only) | hover card, preview card, hover preview |
| `ShadcnDropdownMenu` | Anchored list of actions | dropdown menu, action menu, kebab menu, overflow menu |
| `ShadcnContextMenu` | Right-click-triggered menu at the cursor | context menu, right-click menu |
| `ShadcnDialog` | Centered modal for focused tasks | dialog, modal, popup window |
| `ShadcnAlertDialog` | Modal that blocks until the user confirms/cancels a destructive action | confirm dialog, delete confirmation, alert dialog |
| `ShadcnSheet` | Modal panel sliding in from a screen edge | sheet, side panel, slide-in panel |
| `ShadcnDrawer` | Sliding panel with real drag-to-dismiss (distinct from Sheet) | drawer, bottom sheet, swipe to dismiss, mobile drawer |
| `ShadcnCombobox` | Searchable/filterable single-select dropdown | combobox, searchable dropdown, autocomplete, filterable select |
| `ShadcnSelect` | Plain (non-searchable) dropdown select | select, dropdown, picker, plain select list |
| Date Picker (recipe: `ShadcnPopover` + `ShadcnCalendar`) | Pick a date via a calendar popup | date picker, calendar picker, choose a date |
| `ShadcnCommand` | Searchable/filterable action list (⌘K palette building block) | command palette, cmd+k, quick actions, fuzzy search menu |
| `ShadcnMenubar` | Desktop-app-style "File Edit View" horizontal menu bar | menubar, app menu bar, desktop menu |
| `ShadcnNavigationMenu` | Top-level site nav with optional flyout panels | navigation menu, nav bar, mega menu |

### Data & layout

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnTable` | Bordered data table with header/rows | table, data table, grid, rows and columns |
| `ShadcnPagination` | Page-number navigation control | pagination, page numbers, next/prev pages |
| `ShadcnScrollArea` | Custom-styled scrollable viewport with a draggable thumb | scroll area, custom scrollbar, scrollable container |
| `ShadcnChart` / `ShadcnBarChart` | Config-driven bar/line chart (Canvas-drawn, no Material) | chart, bar chart, graph, data visualization |
| `ShadcnCalendar` | Month grid date picker | calendar, date grid, month view |
| `ShadcnCarousel` | Swipeable/paged content slider | carousel, slider, image slider, paged content |
| `ShadcnResizablePanelGroup` | Two panes divided by a draggable handle | resizable panels, split pane, drag to resize |
| `ShadcnSidebar` | Collapsible side navigation rail with grouped menu sections | sidebar, side nav, app shell nav, collapsible rail |

### AI Elements

Chat/AI-assistant UI primitives (mirrors real shadcn's "AI Elements" family).

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnMessage` / `ShadcnMessageGroup` | One chat row: avatar + content, mirrored for the sender's own messages | chat message, message row, avatar + bubble layout |
| `ShadcnBubble` / `ShadcnBubbleContent` / `ShadcnBubbleReactions` | Colored chat bubble with variants, self-aligned, optional emoji-reaction pill | chat bubble, speech bubble, message bubble, emoji reactions |
| `ShadcnAttachment` / `ShadcnAttachmentGroup` | File-attachment chip for a chat composer's upload tray (upload/processing/error states) | file attachment, upload chip, attached file, upload progress |
| `ShadcnMarker` | Labeled divider in a chat transcript (date separator, pinned-messages banner) | date separator, chat divider, section marker |
| `ShadcnMessageScroller` | Auto-follows new messages to the bottom, releases on manual scroll, floating jump-to-bottom button | chat scroll view, auto-scroll chat, AI streaming scroll, jump to bottom, sticky scroll |

### Utils

Modifiers, not standalone components (mirrors real shadcn's `docs/utils/*` pages).

| Utility | Use case | Keywords |
|---|---|---|
| `Modifier.shadcnShimmer()` | Sweeping highlight over text for "generating response"/"thinking" states | shimmer, loading text animation, generating response, thinking indicator |
| `Modifier.shadcnScrollFade()` | Fades a scrollable container's edges based on scroll position, hinting overflow | scroll fade, edge fade, overflow hint, fade mask, scroll shadow |

## Registry parity

Every component in real shadcn/ui's `base/*` registry is implemented here, with a small set of
deliberate, documented exceptions — `native-select` (needs real native OS widgets per platform,
out of this library's pure-Compose scope), `direction` (Compose already has
`LocalLayoutDirection`), `data-table` (a guide in real shadcn too, not a component — needs
TanStack Table, no Compose equivalent), and `toast` (deprecated by shadcn itself in favor of
`sonner`, which this library already implements as `ShadcnToast`). Full status and the exact
reasoning for each is tracked in [`.claude/AGENTS.md`](.claude/AGENTS.md#registry-parity-status).

## Project structure

- [`/library`](./library/src) — the published component library itself (`commonMain` has every
  `Shadcn*` component; this is the artifact consumers actually depend on).
- [`/core`](./core/src) — shared utilities used across the catalog app's targets.
- [`/app/shared`](./app/shared/src) — the catalog/documentation app's shared UI (one page per
  component, live previews + copyable code).
- [`/app/androidApp`](./app/androidApp), [`/app/desktopApp`](./app/desktopApp),
  [`/app/webApp`](./app/webApp) — per-platform entry points for the catalog app.
- [`/app/iosApp`](./app/iosApp/iosApp) — the iOS entry point; open this in Xcode to run/debug iOS.

### Running the catalog app

- Android: `./gradlew :app:androidApp:assembleDebug`
- Desktop:
  - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
  - Standard run: `./gradlew :app:desktopApp:run`
- Web:
  - Wasm target (faster, modern browsers): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
  - JS target (slower, supports older browsers): `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- iOS: open [`/app/iosApp`](./app/iosApp) in Xcode and run it from there.

### Running tests

- Library unit + Roborazzi screenshot tests (JVM): `./gradlew :library:jvmTest`
- Library, all platforms: `./gradlew :library:allTests`
- Catalog app (Desktop/JVM): `./gradlew :app:shared:jvmTest`
- Catalog app (Web): `./gradlew :app:shared:wasmJsTest` / `./gradlew :app:shared:jsTest`
- Catalog app (iOS simulator): `./gradlew :app:shared:iosSimulatorArm64Test`
- Lint: `./gradlew ktlintCheck`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
and the source of design truth, [shadcn/ui](https://ui.shadcn.com).
