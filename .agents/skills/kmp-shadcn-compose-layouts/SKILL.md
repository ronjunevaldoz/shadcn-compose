---
name: kmp-shadcn-compose-layouts
description: >
  Composes shadcn-compose's individual Shadcn* components into full page layouts —
  login/auth forms, generic data-entry forms, data table screens, and admin/dashboard
  shells — using recipes verified directly against the library's own real source, not
  assumed by analogy to real shadcn/ui's React blocks. Also audits an existing screen for
  underutilized shadcn-compose primitives (hand-rolled form fields instead of ShadcnField,
  hand-rolled tables instead of ShadcnTable, admin shells missing ShadcnSidebar) and
  suggests the specific migration. Companion to kmp-shadcn-compose, which stops at
  individual-component verification and does not cover page-level composition.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-08-04'
  keywords:
    - shadcn login form
    - shadcn admin layout
    - shadcn dashboard
    - shadcn data table
    - ShadcnField
    - ShadcnFieldGroup
    - ShadcnTable
    - ShadcnSidebar
    - shadcn compose form
    - admin shell compose multiplatform
    - shadcn compose dashboard
    - shadcn compose layout
---

## When to Use This Skill

Use when:
- Building a login/auth form, a generic data-entry form, a data table screen, or an
  admin/dashboard shell in a project that already uses shadcn-compose (see
  `kmp-shadcn-compose` for adding the dependency and `ShadcnTheme` first — this skill
  assumes that's already done).
- Auditing an existing screen for under-utilizing shadcn-compose — hand-rolled form
  fields where `ShadcnField`/`ShadcnFieldGroup` would fit, a hand-rolled repeated-`Row`
  table where `ShadcnTable` would fit, or an admin shell with no `ShadcnSidebar` usage
  anywhere.

**Never** for:
- Individual component signature questions ("what params does `ShadcnSelect` take?") —
  `kmp-shadcn-compose`'s Component Keyword Matrix and
  `fetch_component_signature.py` cover that; this skill only composes components already
  verified there.
- Translating an HTML/React wireframe into Compose generically —
  `kmp-layout-system` covers that for any component library, not
  shadcn-compose specifically.
- Adding shadcn-compose to a project that doesn't have it yet — that's
  `kmp-shadcn-compose` Step 1/2 (or `/kmp-migrate-to-shadcn`), including
  the experimental-API risk confirmation. Never skip straight to this skill's recipes
  without that gate having been cleared first.

**Trigger keywords:** shadcn login form, shadcn admin layout, shadcn dashboard, shadcn
data table, ShadcnField, ShadcnFieldGroup, ShadcnTable, ShadcnSidebar, shadcn compose
form, admin shell compose multiplatform, shadcn compose dashboard.

**Freshness rule:** every signature below was verified directly against
`shadcn/core/src/commonMain/.../components/{ShadcnField,ShadcnTable,ShadcnSidebar}.kt`
in the `ronjunevaldoz/shadcn-compose` repo on 2026-08-04 (library version `0.2.6`), not
copied from this library's own KDoc examples — `kmp-shadcn-compose`'s Step 4 already
found a real case of the library's own KDoc naming composables
(`ShadcnItemMedia`/`ShadcnItemContent`/`ShadcnItemActions`) that don't exist anywhere in
the repo. Recheck with `fetch_component_signature.py` before trusting a recipe here
against a newer library version.

---

## Recommendation First

Default to the recipes below over hand-rolling a `Row`/`Column` layout from scratch once
a project has committed to shadcn-compose (see `kmp-shadcn-compose`'s own
Recommendation First for that prerequisite decision — this skill doesn't re-litigate it).

Why: real shadcn/ui ships these same four archetypes as its own documented, widely-copied
patterns — a `Card`-wrapped auth form, a `Field`/`FieldGroup`-composed settings form, a
`Table`-based data grid, and a `Sidebar`-based admin shell (shadcn.io/examples's `Form`,
`Data Table`, `Card`, and shadcn/ui's own dashboard blocks all demonstrate the same
shapes). shadcn-compose's `ShadcnField`/`ShadcnTable`/`ShadcnSidebar` families exist
specifically to reproduce these exact archetypes, verified 1:1 against real shadcn's own
`field.tsx`/`table.tsx`/`sidebar.tsx` structure per `docs/shadcn-parity.md` — they are not
generic primitives meant to be reassembled ad hoc every time a new screen needs one.

---

## Overview

Produces:
- Four verified page-composition recipes: login/auth form, generic data-entry form, data
  table screen, admin/dashboard shell — each built only from real signatures (no
  hallucinated parameters, matching `kmp-shadcn-compose`'s own hard rule).
- `scripts/scan_shadcn_layout_gaps.py` — audits an existing project for the *structural*
  underutilization patterns a 1:1 component-name scan can't catch (that 1:1 scan already
  exists — `kmp-audit`'s `_detect_raw_component_bypass` — this
  script is deliberately scoped to layout-shape gaps instead, not duplicating it).

---

## Step 1: Login / auth form

Real shadcn's `login-01` block shape: a centered, width-capped `Card` containing a
`FieldGroup`. Every symbol below verified against real source:

```kotlin
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun LoginForm(onSubmit: (email: String, password: String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ShadcnCard(
            modifier = Modifier.widthIn(max = 380.dp),
            header = {
                ShadcnCardHeader(
                    title = "Login to your account",
                    description = "Enter your email below to login to your account",
                )
            },
        ) {
            ShadcnFieldGroup {
                ShadcnField {
                    ShadcnFieldLabel("Email", required = true)
                    ShadcnTextField(value = email, onValueChange = { email = it }, placeholder = "m@example.com")
                }
                ShadcnField {
                    ShadcnFieldLabel("Password", required = true)
                    ShadcnTextField(value = password, onValueChange = { password = it })
                }
                ShadcnButton(
                    onClick = { onSubmit(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                ) { ShadcnText("Login") }
            }
        }
    }
}
```

Notes on why it's shaped this way:
- `ShadcnFieldGroup`'s own content lambda is `ColumnScope` with `spacing.xxl` between
  children — `ShadcnField`s and the submit `ShadcnButton` all drop straight in as direct
  children, no extra `Column` wrapper needed.
- `ShadcnField`'s default `orientation` is `Vertical` (label stacked above control) — pass
  `orientation = ShadcnFieldOrientation.Horizontal` only for a label-beside-control row
  (e.g. a settings toggle), not a text-entry field.
- `ShadcnFieldLabel` is a thin alias over `ShadcnLabel` (`text`, `required`, `disabled`) —
  it exists so field composition reads consistently, not because it's a different
  component underneath.
- `ShadcnTextField` has **no `singleLine`/`isPassword` parameter** (verified —
  `kmp-shadcn-compose` already documents the `singleLine` trap for the general case).
  There is no dedicated password-masking parameter on it as of `0.2.6` — if the project
  needs masked input, that's a real gap to flag to the user rather than guess a
  `visualTransformation` value works the same as real HTML `type="password"`; verify
  against `fetch_component_signature.py ShadcnTextField` before assuming.

---

## Step 2: Generic data-entry form

Same `ShadcnFieldGroup`/`ShadcnField` building blocks, extended with a section separator
and mixed control types — real shadcn's `field.tsx` demo covers exactly this shape:

```kotlin
ShadcnFieldGroup {
    ShadcnField {
        ShadcnFieldLabel("Project name", required = true)
        ShadcnTextField(value = name, onValueChange = { name = it })
        ShadcnFieldDescription("Shown on the dashboard and in notifications.")
    }
    ShadcnField {
        ShadcnFieldLabel("Visibility")
        ShadcnSelect(value = visibility, options = listOf("Private", "Team", "Public"), onValueChange = { visibility = it })
    }
    ShadcnField(orientation = ShadcnFieldOrientation.Horizontal) {
        ShadcnCheckbox(checked = notifyOnChange, onCheckedChange = { notifyOnChange = it })
        ShadcnFieldLabel("Notify me on changes")
    }
    ShadcnFieldSeparator(label = "Advanced")
    ShadcnField {
        ShadcnFieldLabel("Description")
        ShadcnTextarea(value = description, onValueChange = { description = it })
        if (descriptionError != null) ShadcnFieldError(descriptionError!!)
    }
    ShadcnButton(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) { ShadcnText("Save") }
}
```

- `ShadcnFieldSeparator(label = ...)` is the real component for a labeled section divider
  *between* fields in the same group — do not reach for a bare `ShadcnSeparator()` here,
  it has no label slot.
- `ShadcnFieldError` renders in `colors.error` — pair it with the field it belongs to,
  conditionally, the same way real shadcn's `FieldError`/`FormMessage` only renders when
  a validation message exists. This skill does not cover *how* to run that validation —
  see `kmp-form-validation` for the validation-logic layer; this
  skill only covers where the resulting message renders.

---

## Step 3: Data table screen

Real shadcn's `table.tsx` structure — header row with a bottom border, body rows
separated by hairlines, left-aligned cells with `Modifier.weight` for column sizing:

```kotlin
ShadcnCard(
    header = {
        ShadcnCardHeader(
            title = "Invoices",
            description = "${invoices.size} total",
            action = { ShadcnButton(onClick = onCreate) { ShadcnText("New invoice") } },
        )
    },
) {
    ShadcnTable {
        ShadcnTableHeaderRow {
            ShadcnTableHeadCell("Invoice", Modifier.weight(1f))
            ShadcnTableHeadCell("Status", Modifier.weight(1f))
            ShadcnTableHeadCell("Amount", Modifier.weight(1f))
        }
        invoices.forEachIndexed { index, invoice ->
            ShadcnTableRow(isLast = index == invoices.lastIndex) {
                ShadcnTableCell(invoice.number, Modifier.weight(1f))
                ShadcnBadge(modifier = Modifier.weight(1f)) { ShadcnText(invoice.status) }
                ShadcnTableCell(invoice.amount, Modifier.weight(1f), muted = true)
            }
        }
    }
}
ShadcnPagination(page = page, pageCount = pageCount, onPageChange = { page = it })
```

**Real, disclosed gap — not a guess to paper over**: `ShadcnTable` is a presentational
shell only (matching real shadcn's own bare `table.tsx`) — it has **no built-in
sorting, filtering, row selection, or virtualization**. Real shadcn/ui's shadcn.io
`Data Table` category is explicitly "TanStack-Table grids with sorting, filtering,
pagination, and row selection" — that's a *separate* pairing (`table.tsx` + TanStack
Table state), not something `table.tsx` (or `ShadcnTable`) provides alone. If the screen
needs sort/filter/selection, that state must be built by the caller (plain
`mutableStateOf` + `sortedBy`/`filter` on the list before rendering rows) — do not assume
`ShadcnTable` has a `sortable`/`onSort` parameter without verifying first; as of `0.2.6`
it does not.

`ShadcnTableHeaderRow`/`ShadcnTableRow`/`ShadcnTableCaption` are plain top-level
composables (not `ColumnScope` extensions, fixed in the library specifically so they also
work inside a `LazyGridItemScope` item) — safe to call inside a `LazyColumn` item scope
directly if the table needs to be one row of a larger scrollable screen, not only inside
`ShadcnTable`'s own `Column`.

---

## Step 4: Admin / dashboard shell

Two valid, real approaches — both verified against real usage of this exact library, not
just its own component signatures:

### 4a. `ShadcnSidebar` family (the library's own dedicated components)

```kotlin
var sidebarExpanded by remember { mutableStateOf(true) }
ShadcnSidebarProvider(expanded = sidebarExpanded, onExpandedChange = { sidebarExpanded = it }) {
    ShadcnSidebar {
        ShadcnSidebarHeader { ShadcnText("Acme Inc", style = ShadcnTextStyle.TitleMedium) }
        ShadcnSidebarGroup(label = "Platform") {
            ShadcnSidebarMenu(
                items = listOf(
                    ShadcnSidebarMenuItem(id = "overview", label = "Overview"),
                    ShadcnSidebarMenuItem(id = "invoices", label = "Invoices", badge = "3"),
                ),
                activeId = activeId,
                onItemClick = { activeId = it },
            )
        }
        ShadcnSidebarFooter { ShadcnText("v1.0.0", muted = true) }
    }
    ShadcnSidebarInset {
        Row(Modifier.fillMaxWidth().padding(shadcnTheme.spacing.sm)) {
            ShadcnSidebarTrigger()
        }
        // page content — dashboard stat cards, a data table (Step 3), etc.
    }
}
```

`ShadcnSidebar`'s default `width` is `240.dp` and it only *expands/collapses* (animates
`width` between `0.dp` and the given `width`) — it has **no responsive breakpoint
behavior of its own** (no automatic swap to a `ShadcnSheet`/drawer below some screen
width). If the shell needs that, wire it manually: watch the container width (e.g. via
`BoxWithConstraints`) and drive `expanded`/swap to a `ShadcnSheet` yourself below the
chosen breakpoint — `ShadcnSidebar` doesn't do this for you.

### 4b. Hand-rolled `Row { fixed-width Column ; content Column }`

What shadcn-compose's **own** catalog app actually does for its admin-style shell
(`app/shared/.../navigation/CatalogNavHost.kt` +
`app/shared/.../catalog/CatalogSidebar.kt`, in the `ronjunevaldoz/shadcn-compose`
repo itself) — a `Column { CatalogTopBar; Row { sidebar; NavHost content } }`, with a
`COMPACT_BREAKPOINT = 720.dp` collapsing the sidebar to a drawer below that width. This
is the pattern to reach for specifically *because* it needs that breakpoint-driven
drawer swap — not a case of the library's own reference app failing to use its own
component; `ShadcnSidebar` (4a) genuinely doesn't cover this responsive behavior, per the
gap disclosed above.

**Pick 4a for a fixed-width, desktop-only admin shell** (the common case — most internal
tools and dashboards never run at phone width). **Pick 4b only when the shell also needs
to collapse into a drawer below a real breakpoint** — and note inline, when suggesting
it, that it's the hand-rolled path specifically for that reason, not a default preferred
over 4a.

### Dashboard stat-card row

```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
    stats.forEach { stat ->
        ShadcnCard(
            modifier = Modifier.weight(1f),
            header = { ShadcnCardHeader(title = stat.label, description = stat.value) },
        ) {}
    }
}
```

`ShadcnCard`'s `content` lambda is required (not nullable) even with nothing to put in
it — an empty trailing lambda `{}` is the correct, real way to render a header-only card,
not a workaround.

---

## Testing

Same as `kmp-shadcn-compose`'s own Testing section —
`kmp-roborazzi`'s screenshot-testing pattern, nothing shadcn-compose-specific
changes it. Additionally, run the audit script before and after a layout migration to
confirm findings actually cleared:

```bash
python3 skills/kmp-shadcn-compose-layouts/scripts/scan_shadcn_layout_gaps.py <project_root>
python3 skills/kmp-shadcn-compose-layouts/scripts/scan_shadcn_layout_gaps.py <project_root> --json
python3 skills/kmp-shadcn-compose-layouts/scripts/scan_shadcn_layout_gaps.py <project_root> --file path/to/Screen.kt
```

Exit codes: `0` clean, `1` findings present, `2` project root does not exist. All four
detectors are heuristic (regex/line-window based, not a real Kotlin parser) — a finding
is a strong prompt to look, not an automatic truth; false positives are possible on
unusual layouts, same caveat `kmp-compose-design-system`'s own
`scan_design_violations.py` documents for its analogous checks.

---

## Common Anti-Patterns

- hand-rolling 2+ label+input pairs in a bare `Column` instead of `ShadcnField`/`ShadcnFieldGroup` — loses the group's consistent `spacing.xxl` rhythm and the `FieldDescription`/`FieldError` slots
- hand-rolling a repeated `Row`-of-`ShadcnText` list as a table instead of `ShadcnTable` — loses the real header-border/row-hairline styling for free
- building an admin shell with a raw fixed-width `Column` and zero `ShadcnSidebar`-family usage, without the explicit responsive-drawer reason Step 4b requires — reach for 4a by default
- assuming `ShadcnTable` ships sorting/filtering/row-selection — it's a presentational shell only as of `0.2.6`; that state is the caller's responsibility, verify before assuming otherwise
- copying a real shadcn/ui block's JSX/TSX 1:1 into these recipes — same discipline as `kmp-shadcn-compose` Step 4: every symbol here was independently verified against real Kotlin source, not transliterated from React
- using this skill's recipes as a reason to skip `kmp-shadcn-compose`'s `fetch_component_signature.py` verification for any component *not* shown here — these four recipes cover the archetypes in this skill's own scope, not the whole library
- reaching for `ShadcnCard`'s `content` lambda as nullable/optional — it's required; use an empty `{}` for a header-only card, not a conditional wrapper

---

## Output Style

When asked to build or audit a layout, respond in this order:
1. Confirm shadcn-compose is set up — look for `ShadcnTheme` in real source.
   If it is missing, point to `kmp-shadcn-compose` first.
2. Identify the archetype: login/auth form, generic form, data table, or admin shell.
   Ask when the request is genuinely ambiguous. Do not guess.
3. Give the recipe, using only verified signatures.
   Flag any parameter not shown here — verify it with `fetch_component_signature.py`.
4. Name any real, disclosed gap that affects the request.
   Examples: no password masking, no table sort/filter. Never paper over one silently.
5. For an audit request, run `scan_shadcn_layout_gaps.py`.
   Explain each finding's migration in the same terms as this skill's own recipes.

---

## Related Skills

- `kmp-shadcn-compose` — prerequisite: adding the dependency, `ShadcnTheme` setup, and individual-component signature verification (this skill only composes components already verified there)
- `kmp-form-validation` — pairs with Step 1/2's forms for the actual validation logic; this skill only covers where `ShadcnFieldError`'s message renders, not how it's computed
- `kmp-layout-system` — HTML/React wireframe translation generically, for shapes outside this skill's four archetypes
- `kmp-roborazzi` — screenshot-test any screen built from these recipes
- `kmp-audit` — `_detect_raw_component_bypass` already covers 1:1 component-name swaps (e.g. raw `Button` → `ShadcnButton`); this skill's own script is scoped to structural/layout-shape gaps instead, deliberately not duplicating that check

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Initial release — four page-composition recipes (login/auth form, generic data-entry form, data table screen, admin/dashboard shell) verified directly against shadcn-compose `0.2.6`'s real source (`ShadcnField`/`ShadcnFieldGroup` family, `ShadcnTable` family, `ShadcnSidebar` family). Disclosed two real gaps rather than guessing past them: `ShadcnTextField` has no password-masking parameter, and `ShadcnTable` has no built-in sort/filter/selection (real shadcn/ui pairs `table.tsx` with a separate TanStack Table integration for that). Documented both a dedicated-component (4a) and a hand-rolled (4b) admin-shell approach, using shadcn-compose's own catalog app (`CatalogNavHost.kt`/`CatalogSidebar.kt`) as the verified real-world reference for 4b's responsive-breakpoint reason. Added `scripts/scan_shadcn_layout_gaps.py` — audits for hand-rolled form fields, hand-rolled tables, admin shells missing `ShadcnSidebar`, and login-style screens missing a `ShadcnCard` wrapper; deliberately scoped to structural gaps `kmp-audit`'s existing 1:1 component-bypass check doesn't cover. |
