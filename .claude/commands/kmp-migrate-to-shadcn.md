# /kmp-migrate-to-shadcn

Migrates an existing project from the owned `kmp-compose-design-system`
(`App*` components) to the published `shadcn-compose` library (`Shadcn*` components).
This is a full library swap, not an incremental fix — get explicit confirmation of the
experimental-API risk before touching any code, same as `/kmp-new-project`'s Step 6a
warning gate.

**What this does:** inventories every `App*` component call-site, drafts a `ShadcnTheme`
config, adds the dependency, migrates file-by-file with per-file confirmation, verifies
with Roborazzi, then removes the old generated design-system code once zero references
remain.

**What this never does:** guess a mapping for a component with no direct shadcn-compose
equivalent, or delete the old design-system module before confirming zero remaining
references.

---

## Step 0 — Confirm the risk

This is the same risk `/kmp-new-project` Step 6a warns about, but higher-stakes here —
you're replacing working, owned code with a real dependency, not choosing a starting
point:

```
Confirm: migrate this project from the owned design-system scaffold to shadcn-compose
as a real Gradle dependency, accepting that a future CMP release may break it with no
fix available except an upstream shadcn-compose release?
[yes / cancel]
```

Do not proceed past this point without an explicit "yes."

---

## Step 1 — Inventory current App* usage

```bash
grep -rn "\bApp\(Button\|Card\|Badge\|Chip\|TextField\|Text\|Icon\|IconButton\|Label\|Separator\|Avatar\|Spinner\|Progress\|CircularProgress\|Skeleton\|TopAppBar\|NavigationBar\|Tabs\|Checkbox\|RadioButton\|Switch\|Slider\|Select\|Alert\|ToastHost\|Scaffold\|Dialog\|AlertDialog\|Sheet\|Tooltip\|Popover\|Accordion\|ScrollArea\|ResizablePanelGroup\)\b" \
  --include="*.kt" "$PROJECT_ROOT" | grep -v "/build/\|/test/"
```

Group by component name and count occurrences. Print the inventory before starting:

```
App* component usage found (N call sites across M files):
  AppButton         12
  AppCard            8
  AppTextField       5
  AppTopAppBar       6   ⚠️  no direct shadcn-compose equivalent — see Step 4
  ...
```

If zero call sites are found, report `No App* components in use — nothing to migrate`
and stop.

---

## Step 2 — Draft the ShadcnTheme config

Same vibe-based inference as `/kmp-new-project` Step 6a-ii — infer the app type from the
project's existing README/description/domain, draft a preset/baseColor/accent
recommendation using the table in `kmp-shadcn-compose`'s "Picking a
preset by app vibe" section, and confirm before proceeding. Do not skip this and default
silently to `Vega`/`Neutral`/`Base` — the draft-and-confirm step is the point.

---

## Step 3 — Add the dependency and wire ShadcnTheme

Load `kmp-shadcn-compose` for the Gradle setup (Maven coordinate,
per-target artifacts, the `@OptIn(ExperimentalFoundationStyleApi::class)` requirement) and
wire `ShadcnTheme` at the app root using the confirmed Step 2 choices. Do not remove the
old `AppTheme`/`:core:designsystem` yet — both exist side by side until migration
completes (Step 6).

---

## Step 4 — Component mapping table

Verified against the real shadcn-compose component catalog
(`docs/components.md`) — not assumed 1:1 parity everywhere:

| `App*` component | `Shadcn*` equivalent | Notes |
|---|---|---|
| `AppButton` | `ShadcnButton` | Direct |
| `AppBadge` | `ShadcnBadge` | Direct |
| `AppCard` | `ShadcnCard` | Direct |
| `AppChip` | `ShadcnChip` | Direct |
| `AppTextField` | `ShadcnTextField` | Direct |
| `AppText` | `ShadcnText` | Direct |
| `AppLabel` | `ShadcnLabel` | Direct |
| `AppSeparator` | `ShadcnSeparator` | Direct |
| `AppAvatar` | `ShadcnAvatar` | Direct |
| `AppSpinner` | `ShadcnSpinner` | Direct |
| `AppProgress` | `ShadcnProgress` | Direct |
| `AppSkeleton` | `ShadcnSkeleton` | Direct |
| `AppTabs` | `ShadcnTabs` | Direct |
| `AppCheckbox` | `ShadcnCheckbox` | Direct |
| `AppRadioButton` | `ShadcnRadioGroup` | Name differs — group-based API |
| `AppSwitch` | `ShadcnSwitch` | Direct |
| `AppSlider` | `ShadcnSlider` | Direct |
| `AppSelect` | `ShadcnSelect` | Direct |
| `AppAlert` | `ShadcnAlert` | Direct |
| `AppToastHost` | `ShadcnToast` / `ShadcnToaster` | Name differs |
| `AppDialog` | `ShadcnDialog` | Direct |
| `AppAlertDialog` | `ShadcnAlertDialog` | Direct |
| `AppSheet` | `ShadcnSheet` | Direct |
| `AppTooltip` | `ShadcnTooltip` | Direct |
| `AppPopover` | `ShadcnPopover` | Direct |
| `AppAccordion` | `ShadcnAccordion` | Direct |
| `AppScrollArea` | `ShadcnScrollArea` | Direct |
| `AppResizablePanelGroup` | `ShadcnResizablePanelGroup` | Direct |
| `AppCircularProgress` | ⚠️ no explicit circular variant in the catalog | Verify against the catalog app before assuming `ShadcnProgress` covers it — ask the user rather than guess |
| `AppIcon` | ⚠️ no equivalent | shadcn-compose has no icon-library dependency — icons come from `ShadcnIconStyles` tokens, not a standalone composable. Keep the existing icon composable, or ask the user how they want icons handled |
| `AppIconButton` | ⚠️ no equivalent listed | Likely composed from `ShadcnButton` + an icon manually — verify against the catalog app, don't assume a 1:1 name |
| `AppTopAppBar` | ❌ no equivalent | shadcn/ui is web-first — no TopAppBar concept in its catalog. Keep Compose's own `Scaffold`/`TopAppBar` structure, placing `Shadcn*` components inside it |
| `AppNavigationBar` | ❌ no equivalent | Closest concept is `ShadcnSidebar` (side nav, not a mobile bottom bar) — ask the user before picking a replacement pattern |
| `AppScaffold` | ❌ no equivalent | `Scaffold` is a Compose layout concept, not part of shadcn/ui's web-derived catalog — keep Compose's own `Scaffold`, just place `Shadcn*` components inside its slots |

**For every ⚠️/❌ row actually present in the Step 1 inventory, stop and ask the user how
they want it handled before migrating anything** — these are the components where
guessing produces a worse result than asking.

---

## Step 5 — Migrate file by file

Same per-file confirmation pattern as `/kmp-fix-design`:

### 5a — Show the changes for one file

```
── feature/auth/ui/src/commonMain/kotlin/AuthContent.kt  (3 App* call sites) ──
  AppButton(onClick = ...) { AppText("Sign in") }
  AppTextField(value = ..., onValueChange = ...)
  AppCard { ... }
```

### 5b — Generate the diff using the Step 4 table

```diff
- AppButton(onClick = { onIntent(LoginClicked) }) { AppText("Sign in") }
+ ShadcnButton(onClick = { onIntent(LoginClicked) }) { ShadcnText("Sign in") }

- AppTextField(value = state.email, onValueChange = { ... })
+ ShadcnTextField(value = state.email, onValueChange = { ... })
```

Add `@file:OptIn(ExperimentalFoundationStyleApi::class)` to any file that now references
a component's `style` parameter.

### 5c — Confirm before writing

```
Apply these changes to AuthContent.kt? [yes / skip / show full file]
```

- **yes** → write the changes
- **skip** → move to next file (leave it on the old `App*` components for now)
- **show full file** → print the full proposed file, then ask again

Repeat for every file in the Step 1 inventory.

---

## Step 6 — Verify

```bash
./gradlew jvmTest -PrecordRoborazzi   # re-record goldens for every migrated screen
```

Run `/kmp-audit-screenshots` against the updated goldens to catch any visual regression
the diff-level migration missed (spacing/contrast/dark-mode drift between the old
generated components and their `Shadcn*` equivalents).

---

## Step 7 — Remove the old design system (only after verification passes)

Re-run Step 1's inventory scan. **Only proceed if it reports zero remaining `App*`
call-sites** — any file that was skipped in Step 5 keeps the old design-system module
needed:

```bash
grep -rln "\bApp\(Button\|Card\|Badge\|...\)\b" --include="*.kt" "$PROJECT_ROOT"
```

If zero: delete `:core:designsystem`'s generated `tokens/`, `theme/`, and `components/`
(everything `kmp-compose-design-system`/`-extended` generated), and remove the
module dependency from `settings.gradle.kts` and any `build.gradle.kts` that referenced
it.

If not zero: report which files still use `App*` components and stop — do not delete a
module that's still referenced.

---

## Rules

- **Never** delete the old design-system module before Step 7's inventory scan reports
  zero remaining `App*` usages.
- **Never** guess a mapping for a ⚠️/❌ component in Step 4 — ask the user how they want
  it handled.
- **Never** skip Step 0's explicit risk confirmation, even if the user asked for the
  migration directly — this command changes a real dependency, not a stylistic default.
- **Always** keep both design systems working side by side until Step 6's verification
  passes — a half-migrated project with both `App*` and `Shadcn*` components in
  different files is an expected, safe intermediate state, not a bug.
