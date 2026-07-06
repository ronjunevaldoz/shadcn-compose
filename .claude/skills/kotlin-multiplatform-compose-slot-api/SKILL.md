---
name: kotlin-multiplatform-compose-slot-api
description: >
  The Slot API pattern in Compose Multiplatform — designing components with
  composable lambda parameters (slots) instead of data parameters. Covers: single
  and named slots, scoped slots with receiver types (RowScope, ColumnScope),
  trailing lambda convention, slot-based component library design, CompositionLocal
  as a deep-slot alternative, performance characteristics, and when NOT to use
  slots. Zero new dependencies.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-26'
  keywords:
    - Slot API
    - composable lambda
    - content slot
    - named slots
    - scoped slots
    - RowScope
    - ColumnScope
    - trailing lambda
    - CompositionLocal
    - component design
    - Compose Multiplatform
    - CMP
    - flexible components
    - inversion of control
---

## When to Use This Skill

Use when you need to:
- Design a reusable component that should render caller-provided content in a specific area
- Understand why `AppButton { content }` is better than `AppButton(text = "...")`
- Implement `Scaffold`-style components with multiple named slots
- Decide between slots and `CompositionLocal` for deep content injection
- Avoid the "prop drilling" problem in Compose

**Trigger keywords:** slot API, content lambda, composable slot, named slots, scoped content,
trailing lambda, flexible component, scaffold pattern, component design, RowScope slot,
inversion of control Compose, redesign component, component redesign, flexible layout,
reusable component, component architecture, component pattern, composable design.

**Freshness rule:** Compose Multiplatform slot conventions track CMP releases — recheck the
JetBrains CMP docs before using or copying snippets into a new project.

---

## Recommendation First

Default to **trailing lambda for a single slot, named `@Composable () -> Unit` parameters for
multiple slots**.

Why:
- trailing lambdas are idiomatic Kotlin and require no extra parameter names for the common case
- named slot parameters make multi-area components (header/body/footer) explicit and readable
- scoped slots (`RowScope`, `ColumnScope`) should be used only when the caller needs layout scope

Use `CompositionLocal` only when the slot content is needed at arbitrary depth and passing it
down the tree would require drilling through many intermediate composables.

---

## What Is a Slot?

A **slot** is a `@Composable () -> Unit` parameter — it lets the caller decide what goes
inside a component instead of the component deciding for itself.

```kotlin
// ❌ Data-driven — the component owns what it renders
@Composable
fun AppButton(text: String, onClick: () -> Unit) {
    Box(Modifier.clickable(onClick = onClick)) {
        Text(text)               // hardcoded: can only show text
    }
}

// ✓ Slot-driven — the caller owns what goes inside
@Composable
fun AppButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,   // slot
) {
    Box(Modifier.clickable(onClick = onClick)) {
        content()                // caller decides: text, icon, spinner, anything
    }
}

// Caller usage — trailing lambda convention
AppButton(onClick = { submit() }) {
    AppText("Submit")
}

// Loading state — same component, different content
AppButton(onClick = {}, enabled = !isLoading) {
    if (isLoading) AppSpinner() else AppText("Submit")
}

// Icon + text — no new parameters needed
AppButton(onClick = { save() }) {
    Row {
        AppIcon(Icons.Default.Save)
        Spacer(Modifier.width(8.dp))
        AppText("Save")
    }
}
```

The data-driven version would need `AppButton(text, icon, isLoading, iconPosition, ...)` — a
combinatorial explosion. The slot version handles all of these with zero new parameters.

---

## Single Slot: The Trailing Lambda Convention

When a composable has exactly one slot parameter and it's the last parameter, Kotlin's
trailing lambda syntax removes the need for explicit `content = { ... }` labels:

```kotlin
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,   // last parameter → trailing lambda
) { ... }

// With trailing lambda — preferred
AppCard {
    AppText("Hello")
}

// Without — also valid but more verbose
AppCard(content = { AppText("Hello") })

// With other args — trailing lambda still works
AppCard(modifier = Modifier.fillMaxWidth()) {
    AppText("Hello")
}
```

**Convention:** always put the primary content slot last. If a composable has only one
parameter besides `modifier` and it's a content slot, name it `content`.

---

## Named Slots: Multiple Content Areas

Named slots let callers populate distinct regions of a component independently:

```kotlin
@Composable
fun AppTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,   // optional slot
    actions: (@Composable () -> Unit)? = null,           // optional slot
) {
    Row(modifier = modifier.fillMaxWidth()) {
        if (navigationIcon != null) navigationIcon()
        Box(modifier = Modifier.weight(1f)) { title() }
        if (actions != null) actions()
    }
}

// Usage
AppTopAppBar(
    title = { AppText("Settings") },
    navigationIcon = {
        AppIconButton(onClick = { navBack() }) {
            AppIcon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
    },
    actions = {
        AppIconButton(onClick = { openMenu() }) {
            AppIcon(Icons.Default.MoreVert, contentDescription = "More")
        }
    },
)
```

**Nullable slots** (`(@Composable () -> Unit)?`) are cleaner than boolean flags:

```kotlin
// ❌ Boolean flag — component decides what "no icon" renders
AppTopAppBar(title = "Settings", showBackButton = true, backButtonVisible = false)

// ✓ Nullable slot — caller provides nothing = nothing renders
AppTopAppBar(title = { AppText("Settings") })           // no back button
AppTopAppBar(
    title = { AppText("Settings") },
    navigationIcon = { /* back button here */ },         // with back button
)
```

---

## Scoped Slots: Constraining Slot Content

Scoped slots give the caller access to the parent's layout scope, enabling correct
alignment and measurement of slotted content:

```kotlin
@Composable
fun AppRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,   // scoped slot — caller gets RowScope
) {
    Row(modifier = modifier) {
        content()   // content() runs inside RowScope, so Modifier.weight() is available
    }
}

// Caller can use Modifier.weight() because they're in RowScope
AppRow {
    AppText("Left", modifier = Modifier.weight(1f))
    AppText("Right")
}
```

```kotlin
// Real design system pattern from AppButton
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Default,
    content: @Composable RowScope.() -> Unit,    // RowScope — caller has horizontal layout context
) {
    Row(
        modifier = modifier.clickable(...).styleable(...),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()   // caller can use Modifier.weight(), Arrangement, etc.
    }
}

// Caller can use RowScope modifiers
AppButton(onClick = {}) {
    AppIcon(Icons.Default.Save)
    Spacer(Modifier.width(8.dp))
    AppText("Save", modifier = Modifier.weight(1f))   // weight() works because of RowScope
}
```

---

## Restricted Scope Template Pattern

A **restricted scope template** is a slot with guardrails: the caller still provides
content, but only inside a narrow, purpose-built contract. Use it when the region should
stay visually and behaviorally consistent across the product, and plain `@Composable () -> Unit`
would make the API too open-ended.

Typical forms:
- a scoped slot like `@Composable RowScope.() -> Unit` or `@Composable ColumnScope.() -> Unit`
- a custom receiver such as `CardHeaderScope` or `ToolbarScope`
- a template component that exposes only a few child-building functions instead of raw layout
  primitives

Use this pattern when:
- the region has a fixed purpose and ordering, like a header row, title bar, or card footer
- callers need a little layout freedom, but not full compositional freedom
- you want consistent spacing, alignment, and typography across all call sites

Avoid this pattern when:
- the content region is genuinely arbitrary and should accept any composable
- the component is a simple leaf and a plain slot would stay clearer
- the only real requirement is choosing between a few variants; use data/variant params instead

| Pattern | Best for | Example |
|---|---|---|
| Plain slot | Fully custom caller-owned content | `content: @Composable () -> Unit` |
| Restricted scope template | Caller content inside a fixed region with guardrails | `content: @Composable RowScope.() -> Unit` |
| Data/variant API | A small set of predefined looks or behaviors | `variant: ButtonVariant` |

---

## Slot Composition: Building Complex Components From Slots

Slots compose — a component with slots can itself be used as a slot:

```kotlin
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        topBar?.invoke()
        Box(modifier = Modifier.weight(1f)) {
            content(PaddingValues())
        }
        bottomBar?.invoke()
    }
}

// AppTopAppBar is used as the topBar slot
AppScaffold(
    topBar = {
        AppTopAppBar(
            title = { AppText("Home") },
            navigationIcon = { /* back button */ },
        )
    },
    bottomBar = {
        AppNavigationBar(items = navItems, selectedIndex = selectedTab, onItemSelected = { selectedTab = it })
    },
) { padding ->
    HomeContent(modifier = Modifier.padding(padding))
}
```

---

## CompositionLocal: The Alternative for Deep Slots

When you need the same value available many levels deep without threading it through
every intermediate composable, use `CompositionLocal` instead of slot parameters or
Koin injection.

```kotlin
// Problem: need theme colors 5 levels deep — slot threading becomes prop drilling
@Composable
fun AppTheme(
    theme: AppThemeData,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppTheme provides theme) {
        content()
    }
}

val LocalAppTheme = compositionLocalOf { lightTheme() }

// Any descendant can access without parameter threading
@Composable
fun SomeDeepComponent() {
    val colors = LocalAppTheme.current.colors    // no parameter needed
    Box(modifier = Modifier.background(colors.primary)) { ... }
}
```

**Rule: slots vs CompositionLocal**

| Use slots when | Use CompositionLocal when |
|---|---|
| Content is positional (goes in a specific layout region) | Value needs to be available to an arbitrary subtree |
| Caller customizes for a specific instance of a component | All descendants share the same value (theme, locale, toast host) |
| The component has 1–4 distinct content regions | Threading through 3+ layers becomes unreadable |

CompositionLocal is not a global variable — it's scoped to the subtree under the
`CompositionLocalProvider`. Providers nest cleanly:

```kotlin
AppTheme(theme = darkTheme) {         // dark theme for everything inside
    AppScaffold(...) {
        AppTheme(theme = lightTheme) { // light theme override for this subtree only
            SpecialDialog()
        }
    }
}
```

### `compositionLocalOf` vs `staticCompositionLocalOf`

This choice controls how Compose handles value changes:

| | `compositionLocalOf` | `staticCompositionLocalOf` |
|---|---|---|
| Value can change at runtime | Yes — only consumers recompose | No — **entire subtree recomposes** |
| Use for | Theme, locale, toast host, user preferences | Values set once at startup and never changed (e.g., platform type, screen density) |
| Default factory required | Yes — called when no Provider is found | Yes — should `error("no provider")` if the value is always provided |

```kotlin
// ✓ Changes at runtime (dark/light switch) → compositionLocalOf
val LocalAppTheme = compositionLocalOf<AppThemeData> { lightTheme() }

// ✓ Never changes after app start → staticCompositionLocalOf
val LocalPlatform = staticCompositionLocalOf<Platform> {
    error("LocalPlatform must be provided at the root")
}
```

### Cross-cutting concerns via CompositionLocal

Use CompositionLocal for composition-scoped services that many unrelated composables need
without a shared parent (toast hosts, snackbar state, analytics trackers, in-app review):

```kotlin
// Toast host — provided at the Scaffold level, consumed anywhere below it
val LocalToastHostState = compositionLocalOf<ToastHostState> {
    error("LocalToastHostState must be provided")
}

@Composable
fun AppScaffold(content: @Composable (PaddingValues) -> Unit) {
    val toastHostState = remember { ToastHostState() }
    CompositionLocalProvider(LocalToastHostState provides toastHostState) {
        Scaffold(
            snackbarHost = { ToastHost(toastHostState) },
            content = content,
        )
    }
}

// Any composable inside AppScaffold — no parameter threading needed
@Composable
fun SomeScreen() {
    val toast = LocalToastHostState.current
    Button(onClick = { toast.show("Saved!") }) { ... }
}
```

### When NOT to use CompositionLocal

| Do not use CompositionLocal for | Why | Use instead |
|---|---|---|
| Business logic services (repositories, use cases) | Composition-scoped DI bypasses testability; ViewModel lifecycle is separate from Compose | Koin `koinInject()` or ViewModel |
| Values only needed 1–2 levels deep | Parameter threading is clearer at this depth | Explicit function parameters |
| Navigation (passing NavController down) | NavController is not a composition-scoped value — it lives in `AppNavHost` | Navigation lambdas or `AppNavigator` |
| ViewModel instances | ViewModels have their own lifecycle; use `koinViewModel()` | `koinViewModel()` |

### Testing with CompositionLocal

Override any `CompositionLocal` in your test's `setContent`:

```kotlin
@Test
fun `toast shows on save success`() {
    val fakeToast = FakeToastHostState()
    composeTestRule.setContent {
        CompositionLocalProvider(LocalToastHostState provides fakeToast) {
            SomeScreen()
        }
    }
    composeTestRule.onNodeWithText("Save").performClick()
    assertEquals("Saved!", fakeToast.lastMessage)
}
```

This is the main testability advantage of `CompositionLocal` over threading a parameter
through every composable — you override at the root of the test, not in every caller.

---

## Slot Performance

Slots are composable lambdas captured at the call site. Two performance characteristics to know:

**1. Slots recompose independently when their inputs change**

```kotlin
@Composable
fun Parent() {
    var count by remember { mutableStateOf(0) }

    AppCard {
        // This slot lambda captures `count` — recomposes when count changes
        AppText("Count: $count")
    }

    AppButton(onClick = { count++ }) { AppText("Increment") }
}
```

The `AppCard` itself does NOT recompose when `count` changes — only the slot lambda
and its descendants do. This is correct behavior and a performance win.

**2. Unstable lambdas can break skippability**

If the slot lambda captures a non-stable value (a regular class, not a data class or
primitive), Compose cannot determine if the lambda changed between recompositions.
Use `remember` to stabilize:

```kotlin
// ❌ Non-stable: a new lambda is created every recomposition
AppButton(onClick = { viewModel.onIntent(SomeIntent) }) { AppText("Click") }

// ✓ Stable: lambda is stable because viewModel::onIntent is a stable function reference
AppButton(onClick = viewModel::onIntent.let { { it(SomeIntent) } }) { AppText("Click") }

// ✓ Or: use a remembered lambda
val onClickStable = remember(viewModel) { { viewModel.onIntent(SomeIntent) } }
AppButton(onClick = onClickStable) { AppText("Click") }
```

---

## When NOT to Use Slots

**1. When a simple value parameter is sufficient**

```kotlin
// ❌ Over-engineered for simple text
@Composable
fun AppLabel(text: @Composable () -> Unit) { ... }

// ✓ Text is text — no need for a slot
@Composable
fun AppLabel(text: String) { Text(text) }
```

**2. When slot content needs to be measured by the parent**

`SubcomposeLayout` lets parents measure slot content before placing it. If your component
needs to know the size of slot content before layout (e.g., to size the container to fit),
you need `SubcomposeLayout`, not a plain slot. This is rare and adds complexity.

**3. When you have more than ~4 named slots**

Beyond 4 slots, the call site becomes noisy. Consider splitting into smaller components
or using a DSL builder pattern.

---

## Design System Slot Pattern Summary

Every component in a well-designed Compose library should follow this signature shape:

```kotlin
@Composable
fun AppSomething(
    // 1. Required behavior parameters first (onClick, onValueChange, etc.)
    onClick: () -> Unit,

    // 2. Modifier — always second if there's no onClick, or third if there is
    modifier: Modifier = Modifier,

    // 3. Optional style/variant parameters
    enabled: Boolean = true,
    variant: SomeVariant = SomeVariant.Default,

    // 4. Optional named slots (nullable for optional regions)
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,

    // 5. Primary content slot — always last (trailing lambda)
    content: @Composable RowScope.() -> Unit,
)
```

---

## Verification

1. Component compiles with trailing lambda syntax at the call site
2. Nullable slots render nothing when null — no empty space, no crash
3. Scoped slot caller can use `Modifier.weight()` (confirms `RowScope`/`ColumnScope`)
4. Slot content change triggers recomposition only of the slot subtree, not the parent
5. Swapping slot content at runtime (e.g., text vs spinner) works without new component parameters

---

## Related Skills

- `kotlin-multiplatform-design-system` — slot API is how design system components accept custom content
- `kotlin-multiplatform-design-system-extended` — extended components use slots for dialog/sheet content areas
- `kotlin-multiplatform-compose-state-hoisting` — pair with hoisting when the slot content owns state
- `kotlin-multiplatform-mvi` — screen/content split uses slots to inject preview-friendly content composables

---

## Testing

```kotlin
@get:Rule val composeRule = createComposeRule()

@Test fun `header slot renders caller-provided content`() {
    composeRule.setContent {
        AppCard(
            header = { Text("Card Title", modifier = Modifier.testTag("slot_header")) },
            content = { Text("Body") },
        )
    }
    composeRule.onNodeWithTag("slot_header").assertExists()
    composeRule.onNodeWithTag("slot_header").assertTextEquals("Card Title")
}

@Test fun `empty slot lambda does not crash`() {
    composeRule.setContent {
        AppCard(header = {}, content = {})
    }
    // Composable must accept empty slots gracefully — no exception thrown
}

@Test fun `action slot fires callback on click`() {
    var clicked = false
    composeRule.setContent {
        AppCard(
            header = { Text("Title") },
            content = { Text("Body") },
            action = { AppButton(text = "OK", onClick = { clicked = true }) },
        )
    }
    composeRule.onNodeWithText("OK").performClick()
    assertTrue(clicked)
}

// Roborazzi screenshot for visual contract
@Test fun `app_card_default_light screenshot`() {
    captureRoboImage("slot_card_default_light.png") {
        AppTheme(darkTheme = false) {
            AppCard(
                header = { Text("Card Header") },
                content = { Text("Card body text.") },
            )
        }
    }
}
```

---

## Common Anti-Patterns

- passing data into a slot that the slot itself could compute — slots should compose, not receive data
- using `CompositionLocal` for content that is only one level deep — a slot parameter is clearer
- defining more than 3–4 named slots on one component — signals the component needs to be split
- using scoped slots (`RowScope`) when the caller does not need layout scope — adds noise without benefit
- calling composable slot parameters from non-composable lambdas — always annotate slot parameters with `@Composable`

If a component's API is hard to read or slot content is getting complex, split the component first.

---

## Output Style

When asked about slots or component design, respond in this order:
1. recommendation (lead with the default slot pattern)
2. code snippet (smallest useful example)
3. why that pattern is preferred
4. main alternative (CompositionLocal, or props)

Keep snippets small. If the user provides a component name, use it in the example.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Expanded CompositionLocal section: compositionLocalOf vs staticCompositionLocalOf decision table, cross-cutting concerns pattern (toast host, analytics), when NOT to use CompositionLocal (DI, nav, ViewModel), and test override example. |
| 2026-06-26 | Added restricted scope template guidance plus a pattern comparison table to distinguish plain slots, guarded scopes, and data/variant APIs. |
| 2026-06-06 | Initial release. |
