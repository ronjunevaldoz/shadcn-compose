---
name: kotlin-multiplatform-compose-state-container
description: >
  Choosing the right state container in Compose Multiplatform: remember vs
  rememberSaveable vs ViewModel vs rememberCoroutineScope. Covers: what survives
  recomposition, config changes, and process death; when each container applies;
  rememberSaveable with custom Saver for complex types; ViewModel scoping to
  nav back-stack entries; and the most common wrong choices (ViewModel for dropdown
  state, remember for form data that must survive rotation). Zero new dependencies.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-06'
  keywords:
    - remember
    - rememberSaveable
    - ViewModel
    - state container
    - rememberCoroutineScope
    - custom Saver
    - state survival
    - config change
    - process death
    - back stack scoped ViewModel
    - nav-scoped ViewModel
    - Compose state
    - Kotlin Multiplatform
    - CMP
    - when to use ViewModel
    - ephemeral state
---

## When to Use This Skill

Use when you need to:
- Decide whether state belongs in `remember`, `rememberSaveable`, or a `ViewModel`
- Understand what "survives recomposition" vs "survives config change" actually means
- Implement a custom `Saver` for `rememberSaveable` with a non-bundleable type
- Scope a ViewModel to a nav back-stack entry vs the whole nav graph
- Diagnose state being lost on screen rotation or back navigation

**Trigger keywords:** remember vs ViewModel, rememberSaveable, state container, when to use
ViewModel, ephemeral state, state survival, config change state, process death state,
custom Saver, nav-scoped ViewModel, state lost on rotation.

**Freshness rule:** Compose state survival and nav-scoped ViewModel APIs change with lifecycle
and navigation releases — recheck the Jetpack and CMP docs before upgrading.

---

## Recommendation First

Default decision: **ephemeral UI state → `remember`; form/input state → `rememberSaveable`;
business data or screen state → `ViewModel`**.

Why:
- `remember` is enough for transient state (dropdown open, tooltip visible) — no persistence needed
- `rememberSaveable` survives config changes, preventing form input loss on rotation
- `ViewModel` is the correct boundary for state that outlives a single screen instance and must
  survive the back-stack navigation lifecycle

Do not put business logic or domain objects inside `remember` or `rememberSaveable` — that belongs
in the ViewModel, not the composition.

---

## What Each Container Survives

| Container | Recomposition | Config change (rotation) | Process death | Scope |
|---|---|---|---|---|
| `remember {}` | ✓ | ✗ | ✗ | Composition lifetime |
| `rememberSaveable {}` | ✓ | ✓ | ✓ (Bundle types only) | Composition + SavedState |
| `ViewModel` | ✓ | ✓ | ✗ (unless SavedStateHandle) | Nav back-stack entry or nav graph |
| `ViewModel` + `SavedStateHandle` | ✓ | ✓ | ✓ | Nav back-stack entry or nav graph |

**Recomposition** — Compose re-executes the composable body when state changes. `remember`
keeps the value alive across re-executions of the same composable instance.

**Config change** — Android destroys and recreates the Activity (screen rotation, locale
change, font size change). `remember` is lost; `rememberSaveable` and `ViewModel` survive.

**Process death** — Android kills the process entirely (low memory). Only `rememberSaveable`
(via `Bundle`/`SavedState`) and `ViewModel` with `SavedStateHandle` survive.

---

## The Decision Tree

```
Does this state involve async operations, IO, or repository calls?
├── YES → ViewModel
└── NO
    ├── Does this state need to survive screen rotation or config change?
    │   ├── YES
    │   │   ├── Is the type Bundle-safe (primitives, String, Parcelable, Serializable)?
    │   │   │   ├── YES → rememberSaveable {}
    │   │   │   └── NO  → rememberSaveable(stateSaver = customSaver) { }
    │   │   │              or ViewModel (if logic justifies it)
    │   │   └── Is this state shared with another screen/route?
    │   │       ├── YES → ViewModel (scoped to nav graph)
    │   │       └── NO  → rememberSaveable {}
    │   └── NO → remember {}
    └── Is this state shared between sibling composables?
        ├── YES → hoist to parent (see kotlin-multiplatform-compose-state-hoisting)
        └── NO  → remember {} in the composable that owns it
```

---

## `remember {}` — In-Composition Memory

`remember` keeps a value alive for the **lifetime of the composable** in the composition.
When the composable leaves the tree and re-enters (back navigation, conditional rendering),
`remember` starts fresh.

```kotlin
// ✓ Correct uses of remember
@Composable
fun DropdownMenu() {
    var expanded by remember { mutableStateOf(false) }   // ephemeral UI toggle
    // ...
}

@Composable
fun AnimatedComponent() {
    val animatable = remember { Animatable(0f) }         // animation object
    // ...
}

@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }         // local input before submit
    // ...
}
```

```kotlin
// ❌ Wrong — remember loses this on rotation; user's half-typed form is gone
@Composable
fun RegistrationForm() {
    var email by remember { mutableStateOf("") }         // lost on config change!
    var name by remember { mutableStateOf("") }          // lost on config change!
    // ...
}
```

### `remember` with keys

When a remembered value depends on inputs, use keys. The value is recomputed when any
key changes:

```kotlin
// Recomputed when userId changes — stale cache is discarded
val formatter = remember(userId) { UserFormatter(userId) }

// Recomputed when both locale and theme change
val painter = remember(locale, theme) { buildPainter(locale, theme) }
```

---

## `rememberSaveable {}` — Rotation-Proof Local State

`rememberSaveable` writes the value to a `Bundle` on config change and restores it.
Works automatically for Bundle-safe types: `Boolean`, `Int`, `Long`, `Float`, `Double`,
`String`, and anything `@Parcelize`/`Serializable`.

```kotlin
// ✓ Form that survives rotation
@Composable
fun SearchScreen() {
    var query by rememberSaveable { mutableStateOf("") }   // survives rotation

    Column {
        AppTextField(value = query, onValueChange = { query = it })
        AppButton(onClick = { performSearch(query) }) { AppText("Search") }
    }
}
```

### Custom Saver for non-Bundle types

When the type isn't Bundle-safe, write a `Saver`:

```kotlin
data class FilterState(
    val category: String?,
    val priceRange: IntRange,
    val sortOrder: SortOrder,
)

val FilterStateSaver = Saver<FilterState, Map<String, Any>>(
    save = { state ->
        mapOf(
            "category"   to (state.category ?: ""),
            "priceMin"   to state.priceRange.first,
            "priceMax"   to state.priceRange.last,
            "sortOrder"  to state.sortOrder.name,
        )
    },
    restore = { map ->
        FilterState(
            category   = (map["category"] as String).ifEmpty { null },
            priceRange = (map["priceMin"] as Int)..(map["priceMax"] as Int),
            sortOrder  = SortOrder.valueOf(map["sortOrder"] as String),
        )
    },
)

// Usage
var filterState by rememberSaveable(stateSaver = FilterStateSaver) {
    mutableStateOf(FilterState(category = null, priceRange = 0..1000, sortOrder = SortOrder.Newest))
}
```

**Limits of `rememberSaveable`:** Bundles have a size cap (~1 MB total). Don't store lists
of items, images, or large collections — use a ViewModel with `SavedStateHandle` for those
(store only the IDs, reload the data from repository).

---

## `ViewModel` — Config-Change-Proof Business State

A ViewModel survives configuration changes because Android holds it separately from the
Activity/Fragment. In KMP, `androidx.lifecycle.ViewModel` works across Android, Desktop,
and iOS (with lifecycle support from JetBrains).

```kotlin
// ✓ Correct uses of ViewModel
class ProductListViewModel(private val repo: ProductRepository) : ViewModel() {

    // Async data load — needs viewModelScope
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = repo.getProducts()
        }
    }
}

// ✓ Shared across screens (scoped to nav graph)
class CartViewModel : ViewModel() {
    val items = mutableStateListOf<CartItem>()
    fun addItem(item: CartItem) { items.add(item) }
}
```

```kotlin
// ❌ Wrong — ViewModel for pure ephemeral UI state
class SearchViewModel : ViewModel() {
    var isDropdownOpen by mutableStateOf(false)   // no business logic — belongs in remember
    var tooltipVisible by mutableStateOf(false)   // no business logic — belongs in remember
}
```

### ViewModel + SavedStateHandle (process-death survival)

```kotlin
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SearchRepository,
) : ViewModel() {

    // Automatically restored after process death
    var query by savedStateHandle.saveable { mutableStateOf("") }

    fun onQueryChanged(newQuery: String) {
        query = newQuery
        // launch search, etc.
    }
}
```

`savedStateHandle.saveable` is the ViewModel equivalent of `rememberSaveable`. Same size
limits apply — store IDs, not full objects.

---

## ViewModel Scoping in Navigation Compose

By default, `koinViewModel()` / `viewModel()` scopes the ViewModel to the **current
back-stack entry**. When you navigate back, the ViewModel is cleared.

```kotlin
// Scoped to current back-stack entry — default, correct for most screens
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel = koinViewModel(),  // cleared when you pop back
) { ... }
```

To share a ViewModel across multiple destinations in a nested nav graph:

```kotlin
// Scoped to the nav graph — survives navigation between screens within the graph
@Composable
fun CheckoutScreen(navController: NavController) {
    val backStackEntry = remember(navController) {
        navController.getBackStackEntry("checkout_graph")   // graph-level entry
    }
    val viewModel: CheckoutViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
    // ...
}
```

This is the correct pattern for multi-step flows (checkout, onboarding) where all steps
share state — the ViewModel lives as long as the user is anywhere in the graph.

---

## `rememberCoroutineScope` — Composable-Scoped Coroutines

When you need to launch a coroutine from a composable (not from a ViewModel), use
`rememberCoroutineScope()`. The scope is cancelled when the composable leaves the tree.

```kotlin
@Composable
fun SaveButton(onSave: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    AppButton(
        onClick = {
            scope.launch {
                isSaving = true
                onSave()
                isSaving = false
            }
        },
        enabled = !isSaving,
    ) {
        if (isSaving) AppSpinner() else AppText("Save")
    }
}
```

**Use `rememberCoroutineScope` when:**
- The coroutine must be tied to the composable lifecycle, not the ViewModel
- You need to launch a coroutine from a click handler (not from a `LaunchedEffect`)
- The coroutine triggers UI-only side effects (scroll, show keyboard, haptic)

**Do NOT use `rememberCoroutineScope` for:**
- API calls or repository access — those belong in `viewModelScope`
- State that needs to outlive the composable

---

## Quick Reference

```kotlin
// Ephemeral UI toggle — lost on rotation, that's fine
var isDropdownOpen by remember { mutableStateOf(false) }

// User input that must survive rotation — not worth a ViewModel
var searchQuery by rememberSaveable { mutableStateOf("") }

// Async data, business logic, or cross-screen state — ViewModel
val state by viewModel.state.collectAsStateWithLifecycle()

// Coroutine tied to composable lifecycle (scroll, animation, keyboard)
val scope = rememberCoroutineScope()

// Config-change AND process-death survival, with ID reference only
var selectedId by savedStateHandle.saveable { mutableStateOf<String?>(null) }
```

---

## `derivedStateOf` — Memoized Derived State

When a value is derived from other `State` but changes less frequently than its inputs,
wrap it in `derivedStateOf` so Compose only recomposes consumers when the derived value
actually changes.

```kotlin
// ❌ Recomputes canSubmit on every keystroke — recomposes Button even when result is stable
val canSubmit = email.isNotBlank() && password.length >= 8

// ✓ derivedStateOf — Button only recomposes when canSubmit flips true ↔ false
val canSubmit by remember(email, password) {
    derivedStateOf { email.isNotBlank() && password.length >= 8 }
}
```

Always wrap `derivedStateOf` in `remember` — without it, a new `DerivedState` is created
each recomposition and the memoization is lost.

See `kotlin-multiplatform-compose-state-hoisting` for full decision table and examples.

---

## `snapshotFlow` — Compose State as a Flow

`snapshotFlow` converts Compose `State` into a `Flow` so you can apply coroutine operators
(debounce, distinctUntilChanged, flatMapLatest) to Compose state changes.

```kotlin
// Debounce a search field — local Compose state → debounced coroutine → ViewModel intent
@Composable
fun SearchBar(onIntent: (SearchContract.Intent) -> Unit) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(300)
            .distinctUntilChanged()
            .collect { q -> onIntent(SearchContract.Intent.Search(q)) }
    }

    AppTextField(value = query, onValueChange = { query = it })
}
```

| Use `snapshotFlow` when | Avoid it when |
|---|---|
| You need to debounce or throttle Compose state changes | The value already comes from a `StateFlow` — collect it normally |
| You need `distinctUntilChanged` on a Compose state | The transformation is pure — `derivedStateOf` is lighter |
| Bridging Compose state to analytics, accessibility, or an external SDK | |

---

## `rememberUpdatedState` — Latest Value Without Restarting an Effect

When a `LaunchedEffect(Unit)` captures a value or lambda that might change between
recompositions, wrap the captured value with `rememberUpdatedState`. The effect keeps a
stable reference that always points to the latest value — without restarting the coroutine.

```kotlin
// ✓ rememberUpdatedState — timer reads the latest onTick without restarting every recompose
@Composable
fun Ticker(onTick: () -> Unit) {
    val currentOnTick by rememberUpdatedState(onTick)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            currentOnTick()   // always the latest lambda
        }
    }
}
```

```kotlin
// ❌ Stale capture — onTick at launch time, not the latest
LaunchedEffect(Unit) {
    while (true) { delay(1_000); onTick() }   // stale if parent recomposes with new lambda
}

// ❌ Lambda as key — effect restarts (and timer resets) on every parent recompose
LaunchedEffect(onTick) {
    while (true) { delay(1_000); onTick() }
}
```

**When to use:**
- `LaunchedEffect(Unit)` that reads a callback or config value that may change
- Any long-running effect that should NOT restart when a captured value changes

**When NOT needed:**
- Effect key already tracks the value (the effect will restart cleanly on key change)
- The captured value is `val` and provably never reassigned

---

## Common Mistakes

**1. `remember` for form data**
A registration form with `remember` loses all user input on screen rotation.
Use `rememberSaveable` for form fields, or a ViewModel if the form has validation logic.

**2. ViewModel for every bit of UI state**
`isDropdownOpen`, `isTooltipVisible`, `expandedCardIndex` — none of these belong in
a ViewModel. They're ephemeral, local, and have no business logic. `remember` is correct.

**3. `rememberSaveable` for large collections**
Bundle has a size limit. Saving a list of 500 products in `rememberSaveable` will crash
with a `TransactionTooLargeException` on Android. Save only IDs; reload data from cache/repo.

**4. Creating a new ViewModel scope per recomposition**
`viewModel()` / `koinViewModel()` must not be called inside a loop or conditional — it
creates a new ViewModel per call. Call it once at the screen level.

**5. Not understanding that Desktop/iOS don't have "config changes"**
`rememberSaveable` and `ViewModel` config-change behavior only applies on Android.
On Desktop and iOS, composable lifetime is tied to the window/view lifecycle. Plan your
state survival strategy accordingly if cross-platform survival matters.

---

## Verification

1. Rotate device (Android) — `rememberSaveable` values persist, `remember` values reset
2. Navigate away and back — back-stack-scoped ViewModel is cleared, new one created on return
3. Navigate between graph screens — graph-scoped ViewModel persists within the graph
4. Kill app from recents and relaunch — `savedStateHandle.saveable` values restored
5. `remember` state (dropdown open) resets when the composable re-enters the tree

---

## Testing

```kotlin
// Test ViewModel state via SavedStateHandle — verifies state survives process death
@Test fun `viewmodel restores state from savedStateHandle`() = runTest {
    val savedState = SavedStateHandle(mapOf("query" to "hello"))
    val vm = SearchViewModel(savedState)
    assertEquals("hello", vm.state.value.query)
}

// Test remember vs rememberSaveable semantics with ComposeTestRule
@get:Rule val composeRule = createComposeRule()

@Test fun `rememberSaveable counter survives recomposition`() {
    composeRule.setContent {
        var count by rememberSaveable { mutableStateOf(0) }
        Column {
            Button(
                onClick = { count++ },
                modifier = Modifier.testTag("increment"),
            ) { Text("+") }
            Text(count.toString(), modifier = Modifier.testTag("count"))
        }
    }
    composeRule.onNodeWithTag("increment").performClick()
    composeRule.onNodeWithTag("count").assertTextEquals("1")
}

@Test fun `remember resets when trigger changes`() {
    var key by mutableStateOf(0)
    composeRule.setContent {
        val value = remember(key) { key * 10 }
        Text(value.toString(), modifier = Modifier.testTag("value"))
    }
    composeRule.onNodeWithTag("value").assertTextEquals("0")
    key = 3
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("value").assertTextEquals("30")
}
```

---

## Common Anti-Patterns

- using `derivedStateOf` without `remember` — memoization is lost; wrap every `derivedStateOf` in `remember`
- reading a `StateFlow` inside `snapshotFlow {}` — collect it with `collectAsState()` first; `snapshotFlow` only tracks Compose `State` objects
- storing domain objects in `remember` — they don't survive config change and belong in a ViewModel
- using `rememberSaveable` for large or non-parcelable types without a custom `Saver` — crashes at runtime
- scoping a ViewModel to the whole NavHost when it belongs to a nested graph — leaks state across features
- hoisting a `ViewModel` to a parent composable that doesn't need it — breaks the boundary between features
- using `remember` state to track loading/error — those are screen-state concerns and belong in the ViewModel
- capturing a changing lambda in `LaunchedEffect(Unit)` without `rememberUpdatedState` — the effect calls a stale closure; use `val current by rememberUpdatedState(lambda)` and call `current()` inside the loop

If state is disappearing on rotation, audit whether `rememberSaveable` or ViewModel is the right container.

---

## Related Skills

- `kotlin-multiplatform-mvi` — ViewModel as the primary state container with `StateFlow` + `Channel<Effect>`
- `kotlin-multiplatform-compose-state-hoisting` — when and where to hoist state vs keeping it in a container
- `kotlin-multiplatform-presenter-module` — pure ViewModel pattern for containers with no Compose dependency
- `kotlin-multiplatform-unit-testing` — testing ViewModel state transitions with Turbine

---

## Output Style

When asked about state containers or state survival, respond in this order:
1. recommendation (which container fits the survival requirement)
2. survival matrix row for the case (remember / rememberSaveable / ViewModel)
3. code snippet (smallest useful example)
4. why that choice is preferred
5. main alternative

Keep the survival matrix reference tight. Map to actual state names when the user provides them.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Add rememberUpdatedState section: stable reference to latest value in LaunchedEffect(Unit), when to use vs not, one new anti-pattern. |
| 2026-06-28 | Add derivedStateOf (memoized derived state, remember rule) and snapshotFlow (Compose State → Flow, debounce bridge) sections. Two new anti-patterns. |
| 2026-06-06 | Initial release. |
