# Screen Layout Contract

Part of `kmp-compose-design-system`. Load when scaffolding or reviewing a screen's top-level layout.

---

> **Requires extended skill:** `AppScaffold` and `AppTopAppBar` are defined in
> `kmp-compose-design-system-extended`. Apply that skill before using the
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

