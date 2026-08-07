---
name: kmp-compose-state-hoisting
description: >
  State hoisting in Compose Multiplatform — the pattern of moving state up to the
  lowest common ancestor that needs it. Covers: stateful vs stateless composables,
  the controlled component pattern (value + onValueChange), the hoist-until-shared
  rule, UI state vs business state distinction, when to stop hoisting, and the
  common mistakes of over-hoisting (everything in ViewModel) and under-hoisting
  (buried state that can't be tested). Zero new dependencies.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-20'
  keywords:
    - state hoisting
    - stateful composable
    - stateless composable
    - controlled component
    - value onValueChange
    - hoist state
    - unidirectional data flow
    - UDF
    - Compose state
    - state lifting
    - single source of truth
    - Compose Multiplatform
    - CMP
    - UI state
    - business state
---

## When to Use This Skill

Use when you need to:
- Decide where state should live in a Compose component tree
- Make a composable testable and previewable by removing internal state
- Share state between sibling composables
- Understand why a component is hard to test (state is buried)
- Explain or implement "controlled" vs "uncontrolled" composable patterns

**Trigger keywords:** state hoisting, lift state, hoist state, stateful composable,
stateless composable, controlled input, value onValueChange, where does state go,
single source of truth, state sharing, Compose state management.

**Freshness rule:** Compose state management guidance tracks CMP releases — recheck the
JetBrains CMP docs before upgrading or copying patterns into a new project.

---

## Recommendation First

Default to **hoisting state to the lowest common ancestor of all consumers**.

Why:
- hoisted state makes a composable stateless and previewable with fixed input
- a stateless composable is trivially unit-testable — no ViewModel or Compose rule needed
- shared state belongs at the level where siblings can both read and write it, not duplicated

Keep state internal (unhoisted) only when it is truly ephemeral and no other composable
in the tree will ever need it (e.g., a tooltip open flag on a local button).

---

## The Core Rule

> **Hoist state to the lowest ancestor that all consumers share.**

That's it. Everything else is application of this rule.

If only one composable reads and writes a value, state stays there. If two siblings need it,
it moves to their parent. If the whole screen needs it, it belongs in a ViewModel.

---

## Stateful vs Stateless Composables

**Stateful** — owns its own state internally:

```kotlin
// Stateful — state lives here, caller cannot observe or control it
@Composable
fun CounterButton() {
    var count by remember { mutableStateOf(0) }
    AppButton(onClick = { count++ }) {
        AppText("Clicked $count times")
    }
}
```

**Stateless** — caller provides state and a callback to change it:

```kotlin
// Stateless — caller owns the state, composable is a pure render function
@Composable
fun CounterButton(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppButton(onClick = onIncrement, modifier = modifier) {
        AppText("Clicked $count times")
    }
}
```

The stateless version is:
- **Testable** — pass any count value, verify the display
- **Previewable** — `CounterButton(count = 42, onIncrement = {})`
- **Shareable** — the count can be read by sibling composables
- **Controllable** — a parent can reset, cap, or react to the count

The stateful version is convenient for truly self-contained UI (a toggle with no external
observers), but is a dead end for sharing or testing.

---

## The Controlled Component Pattern

Kotlin/Compose's equivalent of React's controlled input: the component receives the
current value and a callback to request a change, but never owns the state.

```kotlin
// ✓ Controlled — standard pattern for form inputs
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        ...
    )
}
```

The parent owns the value:

```kotlin
@Composable
fun LoginForm() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        AppTextField(value = email, onValueChange = { email = it })
        AppTextField(value = password, onValueChange = { password = it })
        AppButton(onClick = { submit(email, password) }) { AppText("Login") }
    }
}
```

**Why this matters:** with the controlled pattern, the parent can:
- Validate: `onValueChange = { if (it.length <= 50) email = it }`
- Transform: `onValueChange = { email = it.lowercase() }`
- React: disable the submit button while email is invalid
- Reset: clear both fields after a failed login attempt

---

## The Hoist-Until-Shared Rule in Practice

Full content: `references/hoist-until-shared-rule.md`.

## `derivedStateOf` — Memoize Derived Compose State

Use `derivedStateOf` when a computation reads one or more `State` values and you only want
it to re-run (and trigger recomposition) when the **result** changes, not every time any
input state ticks.

```kotlin
// ❌ Recomputes and recomposes on every keystroke, even if canSubmit doesn't change
@Composable
fun LoginForm(email: String, password: String, ...) {
    val canSubmit = email.isNotBlank() && password.length >= 8   // recomputes every recomposition
    AppButton(enabled = canSubmit, ...) { ... }
}

// ✓ derivedStateOf — only recomposes the Button when canSubmit actually flips
@Composable
fun LoginForm(email: String, password: String, ...) {
    val canSubmit by remember(email, password) {
        derivedStateOf { email.isNotBlank() && password.length >= 8 }
    }
    AppButton(enabled = canSubmit, ...) { ... }
}
```

**When to use `derivedStateOf`:**

| Situation | Use |
|---|---|
| Derived value changes less often than inputs (e.g., `isValid` from a text field) | `derivedStateOf` |
| Derived value changes at the same rate as inputs | Plain expression — `derivedStateOf` adds overhead for no gain |
| Multiple unrelated states feed one derived value | `derivedStateOf` — avoids redundant recompositions |
| The expression is expensive (sort, filter a list) | `derivedStateOf` — caches until inputs change |

```kotlin
// ✓ Filtering a list — only recompose when filteredItems actually changes
@Composable
fun ProductList(query: String, products: List<Product>) {
    val filteredItems by remember(query, products) {
        derivedStateOf {
            if (query.isBlank()) products
            else products.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    LazyColumn { items(filteredItems) { ProductItem(it) } }
}
```

**Rule:** wrap `derivedStateOf` in `remember` — otherwise a new `DerivedState` object is
created on every recomposition and the memoization is lost.

---

## `@Stable` and `@Immutable` — Compose Stability Annotations

The Compose compiler infers whether a type is **stable** (reads are deterministic and
Compose is notified of any change) or **unstable** (Compose can't prove this). Composables
whose parameters are all stable can be **skipped** when the parent recomposes and none of
the parameters changed.

`data class` parameters that contain only primitives, `String`, and other stable types
are inferred as stable automatically. Classes with mutable fields, interfaces, or
non-Compose-aware collections are inferred as **unstable** — which disables skipping for
every composable that receives them.

Add annotations to opt in explicitly:

```kotlin
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

// ✓ @Immutable — all fields are deeply immutable (val + immutable types only)
@Immutable
data class ProductCardState(
    val name: String,
    val priceFormatted: String,
    val isFavorite: Boolean,
)

// ✓ @Stable — fields may include types Compose can't automatically verify
@Stable
data class FilterState(
    val tags: List<String>,     // List<> inferred as unstable without @Stable
    val sortOrder: SortOrder,
)
```

| Annotation | Contract | Use when |
|---|---|---|
| `@Immutable` | All public fields are deeply immutable | `data class` with only `val` of primitive / `String` / `@Immutable` types |
| `@Stable` | Reads are stable; Compose notified of any change | Fields include `List`, `Map`, or other types Compose can't prove are stable |
| Neither | Compose conservatively marks unstable | When you haven't audited stability yet — add annotations once you know the type is safe |

**Where to apply:**
- MVI `State` data classes (see `kmp-mvi`) — the most impactful place
- Design system types passed down through deep composable trees
- ViewModel-owned UI model classes that appear as composable parameters

**Do NOT annotate types that genuinely mutate** without notifying Compose — the annotation is
a contract, and breaking it produces hard-to-debug phantom recompositions.

---

## When to Stop Hoisting

Not everything belongs in a ViewModel. Over-hoisting creates bloated ViewModels full of
UI-only state that has nothing to do with business logic.

**Keep in local `remember`:**
- Dropdown open/closed
- Tooltip visible
- Focus state
- Animation target values
- Temporary input before validation
- Scroll position (use `rememberScrollState()`)

**Move to ViewModel:**
- Any async data load result
- Form values that persist across navigation
- State shared with another screen
- State derived from repository data
- State that affects what API calls are made

```kotlin
// ❌ Hoisted too far — ViewModel shouldn't own dropdown open state
class SearchViewModel : ViewModel() {
    var isDropdownOpen by mutableStateOf(false)   // pure UI state, no business logic
}

// ✓ Correct — dropdown state is ephemeral UI, lives in the composable
@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    // ...
}
```

---

## Providing a Stateful Convenience Wrapper

For components used in both controlled (hoisted) and standalone contexts, provide both:

```kotlin
// Stateless — the "real" component, fully controllable
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) { ... }

// Stateful convenience wrapper — for when the caller doesn't need to observe the value
@Composable
fun AppCheckbox(
    initialChecked: Boolean = false,
    modifier: Modifier = Modifier,
    label: String? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    var checked by remember { mutableStateOf(initialChecked) }
    AppCheckbox(
        checked = checked,
        onCheckedChange = { checked = it; onCheckedChange?.invoke(it) },
        modifier = modifier,
        label = label,
    )
}
```

Prefer the stateless version in design systems — leave the choice to the caller.

---

## State Hoisting and the Screen/Content Split

Combining state hoisting with the Screen/Content split (from `kmp-mvi`)
gives you fully testable leaf composables:

```kotlin
// Screen — wires ViewModel to Content; not previewable, not unit-testable
@Composable
fun SearchScreen(viewModel: SearchViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(
        query = state.query,
        results = state.results,
        isLoading = state.isLoading,
        onQueryChanged = { viewModel.onIntent(SearchIntent.QueryChanged(it)) },
        onResultClicked = { viewModel.onIntent(SearchIntent.ResultSelected(it)) },
    )
}

// Content — all state hoisted out; fully previewable and testable
@Composable
fun SearchContent(
    query: String,
    results: List<SearchResult>,
    isLoading: Boolean,
    onQueryChanged: (String) -> Unit,
    onResultClicked: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
) { ... }
```

This is the end result of applying both state hoisting and the MVI pattern together:
the `Screen` composable holds nothing, the `ViewModel` holds business state, and the
`Content` composable is a pure function of state.

---

## Common Mistakes

Full content: `references/common-mistakes.md`.

## Verification

1. Stateless composable previews render correctly with any state value
2. Sibling composables sharing hoisted state stay in sync when either changes
3. ViewModel state change triggers recomposition of `Content` composable
4. Inline `remember` state (dropdown, tooltip) does NOT persist to ViewModel
5. Unit test: instantiate `Content` with fixed state, assert rendered output without a ViewModel

---

## Testing

Full content: `references/testing.md`.

## Common Anti-Patterns

- using `derivedStateOf` without `remember` — a new `DerivedState` is created every recomposition and the memoization is lost
- not using `derivedStateOf` for expensive derived values (filter, sort) — computation runs on every recomposition even when output is unchanged
- passing a `data class` with `List` or interface fields to a composable without `@Stable` — Compose marks it unstable and recomposes the consumer on every parent recomposition even when the data hasn't changed
- using `@Immutable` on a class that has mutable fields — breaks the contract; the compiler trusts the annotation and skips recomposition when it should update
- keeping state internal to avoid "extra parameters" — hides testability problems behind convenience
- hoisting state higher than the lowest common ancestor — forces unrelated composables to carry state they don't use
- duplicating state in multiple composables instead of hoisting to a shared ancestor
- using `MutableState` as a parameter type — callers should receive `value` + `onValueChange`, not the holder
- lifting state all the way to the ViewModel when it is purely ephemeral UI state (tooltip, dropdown)

If a composable is hard to preview or test, check whether the state is in the right place.

---

## References

Full implementation content lives in `references/*.md`: `hoist-until-shared-rule`,
`common-mistakes`, `testing`. Load the specific file named in the pointer under its
matching heading above, not all of them.

---

## Related Skills

- `kmp-compose-state-container` — when hoisting reaches the ViewModel boundary
- `kmp-mvi` — MVI Contract as the top of the hoisting hierarchy for screen state
- `kmp-compose-preview-driven-development` — stateless composables from hoisting enable fast Desktop previews
- `kmp-compose-slot-api` — slot APIs pair naturally with hoisted state callbacks
- `kmp-audit` — `_detect_compose_unstable_collection_param` flags a raw `List`/`Map`/`Set` composable parameter (Compose treats these as unstable, forcing recomposition even when contents are unchanged — use `kotlinx.collections.immutable`'s `ImmutableList`/`ImmutableMap`/`ImmutableSet` instead; the library is still **Alpha** — verified against its own repo, "API is subject to change" — pin its version deliberately and don't expose it across a library's own public API surface)

---

## Output Style

When asked about state hoisting or composable testability, respond in this order:
1. recommendation (hoist to the appropriate level)
2. before/after code showing state lifted out of the component
3. why hoisting makes the component testable
4. main alternative (keep state internal, CompositionLocal)

Keep snippets small. Use the user's actual composable names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split "The Hoist-Until-Shared Rule in Practice", "Common Mistakes", and "Testing" out of SKILL.md into `references/*.md`, leaving pointer stubs plus a new References section. SKILL.md drops from 618 to 449 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as the other 14 skills fixed alongside it (KI-008). |
| 2026-07-20 | Cross-referenced `kmp-audit`'s new `_detect_compose_unstable_collection_param` — mechanically flags the exact raw `List`/`Map`/`Set` parameter case the existing @Stable/@Immutable section already warns about, instead of relying on an agent to remember it unprompted. |
| 2026-06-28 | Add @Stable/@Immutable stability annotation section: decision table, where to apply, contract rules. Two new anti-patterns. |
| 2026-06-28 | Add derivedStateOf section: memoized derived Compose state, decision table (when to use vs plain expression), list-filter example, remember wrapping rule. Two new anti-patterns. |
| 2026-06-06 | Initial release. |
