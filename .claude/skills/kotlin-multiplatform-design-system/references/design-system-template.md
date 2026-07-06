# PROJECT_NAME Design System

> **Usage:** Copy this file to `docs/design-system.md` in your project and fill in every
> `_fill in_` section. The `kotlin-multiplatform-design-system` skill reads
> `docs/design-system.md` if it exists, so it can tailor generated code to your actual
> token names and component prefix.
>
> Placeholders to replace globally before saving:
> - `PROJECT_NAME` → your project name (e.g. `GuildBase`)
> - `GROUP_ID` → your Maven group ID (e.g. `com.example.myapp`)
> - `COMPONENT_PREFIX` → your design system prefix (e.g. `App`, `Acme`, `My`). Don't
>   guess this — run `python3 scripts/derive_component_prefix.py <project_root>` to
>   derive it deterministically from `rootProject.name` / the group ID / the directory
>   name (see the base skill's Step 0), then record the confirmed value here.

---

## Overview

| Field | Value |
|---|---|
| Project | PROJECT_NAME |
| Group ID | GROUP_ID |
| Component prefix | COMPONENT_PREFIX |
| CMP version | _fill in_ (e.g. `1.11.1`) |
| Last updated | _fill in_ |
| Owner | _fill in_ (team or person) |

### Module location

```
:core:designsystem
  ├── tokens/        ← project-owned, customize freely
  ├── theme/         ← project-owned, customize freely
  ├── components/    ← skill-owned, update via /kmm-update-design-system
  ├── previews/      ← one file per component
  └── detekt-rules/  ← PSI-based violation scanner
```

---

## Design Tokens

> **These are project-owned.** The `/kmm-update-design-system` command will never touch
> `tokens/` or `theme/`. Change these freely to match your brand.

### Color palette

Describe the palette intent in one sentence: _fill in (e.g. "Neutral-first with indigo as the primary brand accent")_

#### Light mode

| Token | Value | Usage |
|---|---|---|
| `primary` | _fill in_ (e.g. `#4F46E5`) | Primary actions, active state |
| `primaryHover` | _fill in_ | Hover state on primary |
| `primaryPressed` | _fill in_ | Pressed state on primary |
| `primaryDisabled` | _fill in_ | Disabled primary |
| `onPrimary` | _fill in_ | Text/icon on primary surface |
| `secondary` | _fill in_ | Secondary actions |
| `secondaryHover` | _fill in_ | Hover on secondary |
| `onSecondary` | _fill in_ | Text/icon on secondary surface |
| `destructive` | _fill in_ | Errors, delete actions |
| `destructiveHover` | _fill in_ | |
| `onDestructive` | _fill in_ | |
| `background` | _fill in_ | App background |
| `surface` | _fill in_ | Cards, sheets |
| `surfaceVariant` | _fill in_ | Alternate surface (e.g. input background) |
| `onSurface` | _fill in_ | Primary text |
| `onSurfaceVariant` | _fill in_ | Secondary/placeholder text |
| `outline` | _fill in_ | Borders, dividers |
| `outlineVariant` | _fill in_ | Subtle dividers |
| `success` | _fill in_ | Positive status |
| `warning` | _fill in_ | Caution status |
| `info` | _fill in_ | Informational status |

#### Dark mode

| Token | Value |
|---|---|
| `primary` | _fill in_ |
| `primaryHover` | _fill in_ |
| `background` | _fill in_ |
| `surface` | _fill in_ |
| `onSurface` | _fill in_ |
| _(continue for all tokens)_ | |

---

### Typography

Font family: _fill in_ (e.g. `Inter`, `Roboto`, system default)

| Token | Weight | Size | Line height | Usage |
|---|---|---|---|---|
| `displayLarge` | _fill in_ | _fill in_ sp | _fill in_ sp | Hero headers |
| `displayMedium` | _fill in_ | _fill in_ sp | _fill in_ sp | |
| `headlineLarge` | _fill in_ | _fill in_ sp | _fill in_ sp | Section titles |
| `headlineMedium` | _fill in_ | _fill in_ sp | _fill in_ sp | |
| `titleLarge` | _fill in_ | _fill in_ sp | _fill in_ sp | Card titles |
| `titleMedium` | _fill in_ | _fill in_ sp | _fill in_ sp | |
| `bodyLarge` | _fill in_ | _fill in_ sp | _fill in_ sp | Body copy |
| `bodyMedium` | _fill in_ | _fill in_ sp | _fill in_ sp | |
| `bodySmall` | _fill in_ | _fill in_ sp | _fill in_ sp | Captions |
| `labelLarge` | _fill in_ | _fill in_ sp | _fill in_ sp | Button labels |
| `labelMedium` | _fill in_ | _fill in_ sp | _fill in_ sp | Chips, badges |
| `labelSmall` | _fill in_ | _fill in_ sp | _fill in_ sp | Tags |

---

### Spacing scale

> All spacing tokens map to a `dp` value. Use them via `appTheme.spacing.*` — never hardcode `.dp`.

| Token | Value | Usage |
|---|---|---|
| `xs` | _fill in_ dp | Tight inner padding (e.g. badge, chip) |
| `sm` | _fill in_ dp | Default inner padding (e.g. list item) |
| `md` | _fill in_ dp | Section gaps |
| `lg` | _fill in_ dp | Content horizontal padding (standard: 16dp) |
| `xl` | _fill in_ dp | Section headers, large gaps |
| `xxl` | _fill in_ dp | Hero spacing |

---

### Shapes

| Token | Corner radius | Usage |
|---|---|---|
| `small` | _fill in_ dp | Chips, badges, input fields |
| `medium` | _fill in_ dp | Cards, buttons |
| `large` | _fill in_ dp | Sheets, dialogs |
| `extraLarge` | _fill in_ dp | Full-rounded pills |

---

## Component Inventory

> `COMPONENT_PREFIX` replaces `App` if your project uses a different prefix.
> Update the component names below to match.

| Component | File | Variants | Status | Notes |
|---|---|---|---|---|
| `COMPONENT_PREFIXButton` | `components/AppButton.kt` | `Default`, `Outline`, `Ghost`, `Destructive` | _Stable / In progress / Planned_ | _fill in_ |
| `COMPONENT_PREFIXCard` | `components/AppCard.kt` | `Default`, `Elevated`, `Outlined` | _Stable / In progress / Planned_ | _fill in_ |
| `COMPONENT_PREFIXTextField` | `components/AppTextField.kt` | `Default`, `WithLabel`, `Error` | _Stable / In progress / Planned_ | _fill in_ |
| `COMPONENT_PREFIXChip` | `components/AppChip.kt` | `Filter`, `Input`, `Suggestion` | _Stable / In progress / Planned_ | _fill in_ |
| `COMPONENT_PREFIXBadge` | `components/AppBadge.kt` | `Default`, `Success`, `Warning`, `Error` | _Stable / In progress / Planned_ | _fill in_ |
| `COMPONENT_PREFIXText` | `components/AppText.kt` | `(type scale)` | _Stable / In progress / Planned_ | _fill in_ |

### Extended components (from `kotlin-multiplatform-design-system-extended`)

Add rows as each extended component is scaffolded:

| Component | File | Variants | Status |
|---|---|---|---|
| `COMPONENT_PREFIXTopAppBar` | | | _Planned_ |
| `COMPONENT_PREFIXDialog` | | | _Planned_ |
| `COMPONENT_PREFIXBottomSheet` | | | _Planned_ |
| `COMPONENT_PREFIXToast` | | | _Planned_ |
| _(add more)_ | | | |

---

## Ownership Model

| Layer | Files | Owner | Update path |
|---|---|---|---|
| Tokens | `tokens/AppColors.kt`, `AppTypography.kt`, `AppShapes.kt`, `AppSpacing.kt` | **Project** | Edit freely — never touched by skill updates |
| Theme | `theme/AppTheme.kt`, `StyleScopeExtensions.kt` | **Project** | Edit freely |
| Components | `components/COMPONENT_PREFIX*.kt` | **Skill** | Run `/kmm-update-design-system` to pull bug fixes; review diff before applying |
| Previews | `previews/COMPONENT_PREFIX*Preview.kt` | **Skill** | Regenerated when component updates |
| Detekt rules | `detekt-rules/` | **Skill** | Pulled via `/kmm-update-design-system`; config in `detekt-design-system.yml` is project-owned |

---

## Detekt Rules Configuration

Active rules and any project-specific overrides. Full config lives in
`detekt-rules/config/detekt-design-system.yml`.

| Rule | Severity | Active | Notes |
|---|---|---|---|
| `HardcodedColor` | Error | ✅ | Flags `Color(0xFF…)` and `Color(r,g,b)` outside token files |
| `HardcodedDp` | Warning | ✅ | Flags `.dp` literals in layout modifiers; 0.dp / 1.dp exempt |
| `MaterialThemeUsage` | Error | ✅ | Flags `MaterialTheme.colors.*`, `MaterialTheme.colorScheme.*` |
| `DirectTextStyle` | Error | ✅ | Flags `TextStyle(…)` construction outside token files |
| `NestedContainer` | Warning | ✅ | Flags `Card { Card {` and `Surface { Surface {` |
| `ComponentRegistryViolation` | Warning | ✅ | Flags `@Composable fun COMPONENT_PREFIXFoo` outside `:core:designsystem` |
| `DesignTokenImportBoundary` | Error | ✅ | Flags `import …tokens.AppColors` in `feature/*/ui/` |
| `RedundantScreenTitle` | Warning | ✅ | Flags `Text("…")` inside `*Content`/`*Screen` — title belongs in `AppTopAppBar` |
| `HardcodedGridColumns` | Warning | ✅ | Flags `GridCells.Fixed(N≥2)` — use `GridCells.Adaptive` instead |

Component prefix override (if not `App`):
```yaml
design-system:
  ComponentRegistryRule:
    componentPrefix: 'COMPONENT_PREFIX'
```

---

## Multi-Device Preview Coverage

Base light/dark variants use `@MultiDevicePreview` (phone 360dp / tablet 673dp / desktop 1280dp).
State variants (Disabled, Error, Ghost, Outline) use plain `@Preview`.

| Component | Phone | Tablet | Desktop | Dark |
|---|---|---|---|---|
| `COMPONENT_PREFIXButton` | ✅ | ✅ | ✅ | ✅ |
| `COMPONENT_PREFIXCard` | ✅ | ✅ | ✅ | ✅ |
| `COMPONENT_PREFIXTextField` | ✅ | ✅ | ✅ | ✅ |
| `COMPONENT_PREFIXChip` | ✅ | ✅ | ✅ | ✅ |
| `COMPONENT_PREFIXBadge` | ✅ | ✅ | ✅ | ✅ |
| `COMPONENT_PREFIXText` | ✅ | ✅ | ✅ | ✅ |

---

## Known Deviations

Document intentional divergences from the skill defaults here so future contributors
understand why the code looks different from the skill template:

| Area | Deviation | Reason |
|---|---|---|
| _(fill in)_ | | |

---

## Design Audit Log

Record the last time `/kmm-fix-design` and `/kmm-audit-design-visual` were run:

| Date | Command | Findings | Action taken |
|---|---|---|---|
| _fill in_ | `/kmm-fix-design` | _N violations_ | _fixed / deferred_ |
| _fill in_ | `/kmm-audit-design-visual` | _N screens reviewed_ | _fixed / deferred_ |
| _fill in_ | `/kmm-record-design-baselines` | _N goldens updated_ | |
