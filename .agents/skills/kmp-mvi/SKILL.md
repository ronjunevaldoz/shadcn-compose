---
name: kmp-mvi
description: >
  MVI (Model-View-Intent) architecture pattern for Kotlin Multiplatform + Compose
  Multiplatform. Covers: the Contract pattern (State/Intent/Effect per screen),
  MviViewModel base class with StateFlow for state and Channel for one-shot effects,
  atomic state updates, Compose screen/content split, testing ViewModels with Turbine,
  and the most common MVI pitfalls in KMP. Zero new dependencies — builds on
  androidx.lifecycle.ViewModel and kotlinx.coroutines already present in feature-scaffold.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-26'
  keywords:
    - MVI
    - Model-View-Intent
    - ViewModel
    - StateFlow
    - Channel
    - Effect
    - Intent
    - UiState
    - unidirectional data flow
    - UDF
    - Kotlin Multiplatform
    - Compose Multiplatform
    - CMP
    - Koin ViewModel
    - Turbine
    - architecture pattern
    - screen state
    - side effect
    - one-shot event
    - Divergent Change
    - God State
---

## When to Use This Skill

Use when you need to:
- Implement a screen with observable UI state in a Kotlin Multiplatform + CMP project
- Handle one-shot side effects (navigation, toasts, dialogs) safely without replay bugs
- Structure a ViewModel that's testable without a Compose/Android dependency
- Explain or implement MVI, UDF (unidirectional data flow), or a screen state machine

**Requires:** `kmp-feature-scaffold` project structure.
**Zero new dependencies** — `androidx.lifecycle.ViewModel`, `kotlinx.coroutines`, Koin, and
Turbine are already present.

**Trigger keywords:** MVI, Model-View-Intent, screen state, UiState, UiIntent, UiEffect,
unidirectional data flow, ViewModel state, one-shot effects, side effects, screen architecture,
StateFlow screen, channel effect, Contract pattern,
navigation effect, one-shot event, single event, show toast from ViewModel,
trigger navigation, event driven UI, MVVM vs MVI, unidirectional event,
screen, implement screen, add screen, new screen, screen logic, UI logic,
screen behavior, screen interaction, handle user input, form state, form handling,
user interaction, screen state management, UI state, state management,
nav args ViewModel, route arguments ViewModel, pass id to ViewModel,
search debounce ViewModel, cancel job intent, in-flight cancellation,
typed error state, UiError sealed, shared ViewModel, wizard ViewModel, multi-step flow,
Divergent Change, God State, unrelated concerns in one ViewModel, state cohesion.

**Freshness rule:** `lifecycle-viewmodel-compose` and CMP lifecycle integration change between
releases — recheck the AndroidX lifecycle and JetBrains CMP docs before upgrading.

**Koin compatibility note:** when MVI screens use Koin-backed ViewModels, keep the
`koin-compose-viewmodel` and `androidx.lifecycle.viewmodelCompose` versions aligned with the
dependency-injection skill. Mismatches can be silent on JVM/Android/iOS and only show up on Wasm
as `IrLinkageError`; include Wasm in verification whenever either side changes.

---

## Recommendation First

**Start thin. Add MviViewModel + Contract only when the screen has async state, user intents, and one-shot effects — all three.**

Decision in order:
1. No async, no persistent state → plain `@Composable`, no ViewModel
2. Async load only (no user actions) → thin `ViewModel` + `StateFlow`, no Contract
3. Async + user actions + navigation → full `MviViewModel` + `Contract`

When you do reach step 3, default to the **Contract pattern + MviViewModel + `Channel<Effect>`**:
- sealed `State`, `Intent`, and `Effect` make the full screen contract visible in one place
- `Channel<Effect>` prevents one-shot effects from replaying on recomposition
- `MutableStateFlow.update {}` is atomic under concurrent intent handling

---

## When NOT to Use MviViewModel

Start with the thinnest option that works. Add layers only when they carry weight.

| Screen type | Pattern | Why |
|---|---|---|
| Static display (help, legal, empty state) | `@Composable` with no ViewModel | No state to manage — props come from the caller |
| Simple local toggle / counter | `remember` / `rememberSaveable` | State doesn't survive process death anyway; no business logic |
| Parent-owned form field | Stateless composable + lambda | Parent screen owns the state; child just renders |
| Async load, no user actions | Thin `ViewModel` + `StateFlow` (no Contract) | Lifecycle awareness needed, but no intents or effects |
| Async load + user actions + navigation | Full `MviViewModel` + Contract | All three concerns present — Contract pays for itself |
| Multi-step flow | One shared `MviViewModel` + thin step screens | Steps share state; per-step ViewModels add no value |

### Thin pattern 1 — no ViewModel at all

```kotlin
// Static screen — no ViewModel, no Contract
@Composable
fun TermsScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Column {
        TermsContent()
        AppButton(onClick = onAccept) { AppText("Accept") }
        AppButton(onClick = onDecline, variant = ButtonVariant.Ghost) { AppText("Decline") }
    }
}
```

### Thin pattern 2 — ViewModel with no Contract

For screens that load data and display it with no user-driven state transitions:

```kotlin
// No Contract object, no sealed Intent, no Channel<Effect>
class UserProfileViewModel(
    private val userId: String,
    private val repo: UserProfileRepository,
) : ViewModel() {

    val state: StateFlow<ProfileState> = flow { emit(repo.getProfile(userId)) }
        .map<User, ProfileState> { ProfileState.Success(it) }
        .catch { emit(ProfileState.Error(it.message.orEmpty())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState.Loading)
}

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
}
```

The `ProfileState` sealed interface lives in the same file as the ViewModel — no
`Contract` object wrapper needed until there are Intents and Effects to group with it.

### When the full Contract pattern earns its place

Add a `Contract` object when a screen has **at least two** of:
- Observable state with multiple fields that change independently
- User intents that trigger async operations
- One-shot effects (navigation, toasts, dialogs)

If only one is present, the thin pattern handles it with less indirection.

---

## Core Concepts

### Why MVI?

MVI enforces **one direction of data flow**:

```
UI → Intent → ViewModel → State update → UI re-render
                        ↘ Effect → UI side effect (navigate, toast, dialog)
```

- **State** (`StateFlow`) — what the screen renders. Always up-to-date, never missed.
- **Intent** — what the user did. A sealed interface of user-triggered events.
- **Effect** — one-shot side effects that should NOT be replayed on recomposition
  (navigation, showing a snackbar, triggering a dialog).

### Why `Channel<Effect>` and not `SharedFlow<Effect>`?

This is the most common MVI mistake in KMP/Android.

`SharedFlow(replay = 0)` **drops effects** if no collector is active (e.g., during
process restart, screen rotation, or Compose lifecycle pause). `SharedFlow(replay = 1)`
**replays the last effect on re-subscription**, causing double-navigation.

`Channel` delivers each effect **exactly once** to exactly one collector. If no collector
is active the effect is buffered (up to `Channel.BUFFERED` capacity) and delivered when
one subscribes. This matches what "one-shot event" actually means.

```kotlin
// ❌ Wrong — replays navigation event on recomposition
private val _effect = MutableSharedFlow<Effect>(replay = 1)

// ✓ Correct — exactly-once delivery, buffered until collector is ready
private val _effect = Channel<Effect>(Channel.BUFFERED)
val effect: Flow<Effect> = _effect.receiveAsFlow()
```

### Why `MutableStateFlow.update {}` and not direct assignment?

`StateFlow.update {}` is **atomic** — it uses compare-and-swap under the hood. Direct
assignment is not:

```kotlin
// ❌ Race condition — reads value, updates, writes back; concurrent coroutines can stomp each other
_state.value = _state.value.copy(isLoading = true)

// ✓ Atomic — compare-and-swap, safe under concurrent intent handling
_state.update { it.copy(isLoading = true) }
```

---

## The Contract Pattern

Group `State`, `Intent`, and `Effect` together in a single `Contract` object per screen.
This makes the full interface of a screen visible in one place.

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthContract.kt
package GROUP_ID.feature.auth.ui

object AuthContract {

    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class EmailChanged(val value: String) : Intent
        data class PasswordChanged(val value: String) : Intent
        data object LoginClicked : Intent
        data object ForgotPasswordClicked : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
        data object NavigateToForgotPassword : Effect
        data class ShowError(val message: String) : Effect
    }
}
```

**Rules for State:**
- Always a `data class` — enables `copy()` and structural equality
- All fields have defaults — the initial state needs no arguments
- No business objects (domain models) directly in state — map to UI-specific types
- Annotate `State` with `@Stable` (or `@Immutable` when all fields are truly immutable) so
  the Compose compiler can skip recomposition of consumers when the reference hasn't changed

```kotlin
import androidx.compose.runtime.Immutable

@Immutable
data class State(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

**`@Stable` vs `@Immutable`:**

| Annotation | Contract | Use when |
|---|---|---|
| `@Immutable` | All public fields are deeply immutable (only `val` of immutable types) | `data class` whose fields are primitives, `String`, or other `@Immutable` types |
| `@Stable` | Reads are stable (same inputs → same outputs) and Compose is notified of changes via snapshot state | Fields include mutable collections or types Compose can't infer stability for |

Without either annotation, the Compose compiler conservatively marks the type as **unstable**
and recomposes every consumer on every parent recomposition — even when `State` hasn't changed.

**Rules for Intent:**
- `sealed interface`, not `sealed class` — Kotlin 1.9+ `data object` for no-arg intents
- Names are past-tense user actions, not commands: `LoginClicked` not `DoLogin`
- No callbacks or lambdas — intents are data, not behavior

**Rules for Effect:**
- One-shot only — navigation, toasts, dialogs, haptic feedback
- State changes are NOT effects — if the screen needs to show a success banner persistently,
  put it in `State`, not `Effect`

## Screen / Content Split

Split every screen into two composables:

- `FooScreen(viewModel = ...)` owns DI, state collection, and effect collection.
- `FooContent(state, onIntent)` is pure, previewable, and testable.
- Navigation callbacks stay as lambdas (`onBack`, `onNavigateToX`) instead of being
  pushed into `Intent` unless they are true in-screen actions.
- If a screen has multiple nav callbacks, group them into a `FooNavActions` data class.

```kotlin
@Composable
fun FooScreen(
    onBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: FooViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FooContract.Effect.Close -> onBack()
                is FooContract.Effect.OpenDetails -> onNavigateToDetails(effect.id)
            }
        }
    }
    FooContent(state = state, onIntent = viewModel::onIntent)
}
```

---

## MviViewModel Base Class

Full content: `references/mvi-viewmodel-base-class.md`.

## Implementing a ViewModel

Full content: `references/implementing-a-viewmodel.md`.

## Compose Integration: Screen / Content Split

Full content: `references/compose-integration.md`.

## Koin Wiring

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/di/AuthUiModule.kt
package GROUP_ID.feature.auth.ui.di

import GROUP_ID.feature.auth.ui.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authUiModule = module {
    viewModelOf(::AuthViewModel)   // preferred — Koin 4 zero-boilerplate form
}
```

Use `viewModel { AuthViewModel(get()) }` only when you need custom qualifiers or
conditional wiring. For everything else, `viewModelOf` is less code and identical behavior.

**ViewModels that need `SavedStateHandle`** (nav args, back-stack results):

```kotlin
class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repo: CheckoutRepository,
) : MviViewModel<...>(...) { ... }

val checkoutModule = module {
    viewModelOf(::CheckoutViewModel)   // SavedStateHandle injected automatically via CreationExtras
}
```

Never construct `SavedStateHandle()` yourself — Koin's ViewModelFactory provides it from
the AndroidX `CreationExtras` bag. See `kmp-dependency-injection` for
the full SavedStateHandle + Koin reference.

If you update either `koin-compose-viewmodel` or `androidx.lifecycle.viewmodelCompose`,
run the Wasm target as part of the verification pass — that is where version drift is most likely
to surface first.

With **Koin annotated mode** (Koin Compiler Plugin):
```kotlin
@KoinViewModel
class AuthViewModel(private val authRepository: AuthRepository) : MviViewModel<...>(...) { ... }
```

---

## Nav Args as Initial State

Full content: `references/nav-args-initial-state.md`.

## In-flight Cancellation

When an intent triggers a job that should supersede any prior job of the same type
(search, filter, reload), cancel the previous job before launching the new one.

```kotlin
private var searchJob: Job? = null

private fun search(query: String) {
    searchJob?.cancel()
    if (query.isBlank()) {
        updateState { copy(results = emptyList(), isSearching = false) }
        return
    }
    searchJob = viewModelScope.launch {
        updateState { copy(isSearching = true) }
        delay(300)                    // debounce — skip if cancelled during delay
        val results = repo.search(query)
        updateState { copy(results = results, isSearching = false) }
    }
}
```

The `delay(300)` acts as a debounce: if a new `SearchQueryChanged` intent arrives within
300 ms the coroutine is cancelled before the network call fires.

**When NOT to cancel:** submit, save, and delete actions should not be cancellable by
re-typing — guard those with an `isLoading` flag instead (see `login()` example above).

---

## Testing

Full content: `references/testing.md`.

## State Patterns

Full content: `references/state-patterns.md`.

## Multi-Source State and Flow Operators

Full content: `references/multi-source-state.md`.

## ViewModel Size and Decomposition

Full content: `references/viewmodel-size-decomposition.md`.

## Common Anti-Patterns

- a ViewModel's constructor injecting a `*Repository` directly instead of a use case — the bright-line rule has no trivial-pass-through exception, and `_detect_module_layer_violation` can't catch it since `presenter -> api` is an allowed module-level dependency for other reasons
- a ViewModel's `Intent` sealed type growing past ~15 variants — a god-ViewModel signal that line count alone can miss, since terse `when` branches keep the file short while the ViewModel still does one screen too many jobs
- exposing `state1`/`state2`/`state3` as separate public `StateFlow` properties instead of `combine()`-ing them into one `State` — breaks the Contract pattern's "one State per screen" rule without tripping a size threshold
- Divergent Change in a `State` type — fields for unrelated concerns (chat + project + session) that each change for their own reason, wearing one `State`; see "Field count alone isn't the test — Divergent Change is" above
- using `SharedFlow` for effects — events replay on new collectors and break "fire once" guarantees
- emitting `Effect` from `init {}` — fires on every ViewModel recreation, not just on user action
- putting navigation logic inside `State` — navigation is an effect, not persisted state
- using `copy {}` with a stale `state` reference instead of `update {}` — causes lost updates under concurrency
- treating a Coordinator ViewModel as exempt from `_detect_viewmodel_size`'s god-ViewModel threshold — escaping a god composable by centralizing into a coordinator just relocates the same size problem unless it actually stays small; a coordinator that keeps growing after delegating to State Holders/use cases needed Option 1 (separate screens), not a bigger coordinator
- exposing mutable `StateFlow` from the ViewModel — UI should never mutate state directly
- missing `isLoading` guard on submit actions — lets rapid taps fire multiple network calls
- forgetting to reset `isLoading` on error — every branch that sets it `true` must reset it in success, error, and cancellation
- navigating by observing a `navigateTo: Route?` field in `State` — fires on every recomposition; use `Effect` instead
- holding domain objects (DTOs, entities) directly in `State` — map to UI-specific types at the ViewModel boundary
- using `GlobalScope` or bare `CoroutineScope()` in a ViewModel — always use `viewModelScope`
- calling `onIntent` from inside the ViewModel — `onIntent` is a UI-layer API; call private suspend functions directly
- using `LaunchedEffect(state.someField)` for effect collection — restarts on every state change; use `LaunchedEffect(viewModel)` instead
- nesting `collect` inside `collect` for multi-source state — use `combine()` to merge flows into one `StateFlow`
- using `SharingStarted.Eagerly` or `Lazily` in `stateIn` — upstream never stops after navigation; always use `WhileSubscribed(5_000)`
- using `flatMap` instead of `flatMapLatest` for dependent flows — the previous inner coroutine keeps running in parallel with the new one
- reading a `StateFlow` directly inside `snapshotFlow {}` — collect it with `collectAsState()` first, then read the resulting `State` inside the lambda
- using `collectAsState()` instead of `collectAsStateWithLifecycle()` in production screens — keeps collecting in the background; wastes battery; use `collectAsState()` only in `@Preview`
- using `LaunchedEffect` when cleanup is needed — if you add a listener or set a holder, use `DisposableEffect` so `onDispose` can remove it
- using `SideEffect` for coroutines — `SideEffect` is synchronous and has no cancel; use `LaunchedEffect` for any suspend work
- constructing `SavedStateHandle()` manually — always let Koin/AndroidX provide it via `viewModelOf(::ViewModel)` or `viewModel { ViewModel(get(), get()) }`
- god ViewModel (400–900+ lines) — all screen logic in one place instead of delegating business operations to use cases; extract any `handleIntent` branch that touches two or more repos into a use case
- god composable — a screen holding 3+ `koinViewModel()` calls, 5+ `LaunchedEffect` blocks, or relaying effects between ViewModels (`subVm.effect.collect { parentVm.onIntent(...) }`); extract a Coordinator ViewModel and move state assembly, effect collection, and persistence into `viewModelScope`
- ViewModel taking another ViewModel as a constructor parameter (`class FooViewModel(val barVm: BarViewModel)`) — breaks lifecycle, `SavedStateHandle`, and DI; demote the sub-unit to a State Holder (plain class taking `scope: CoroutineScope`) or a use case
- direct repository calls in ViewModel for complex orchestration — if the ViewModel `when` branch needs multiple repos or has business rules, it belongs in a use case, not the ViewModel
- storing auth status as `isAuthenticated: Boolean` in `State` and navigating on state change — use `SessionViewModel` + a `LaunchedEffect` in `AppNavHost` to guard the entire nav graph; MVI screens should not own auth gate logic
- using `Effect.NavigateBack` without a clear back-stack contract — always pair it with the correct NavHost `popUpTo` rule; bare `popBackStack()` can leave the user on an authenticated screen after logout
- not annotating `State` data class with `@Immutable` or `@Stable` — Compose conservatively marks it unstable and recomposes all consumers on every parent recomposition, even when state hasn't changed
- bare `viewModelScope.launch {}` with no `CoroutineExceptionHandler` — uncaught exceptions from `handleIntent` coroutines are swallowed silently on some KMP targets; override `exceptionHandler` in the ViewModel to surface them as error state
- reading a changing lambda inside `LaunchedEffect(Unit)` without `rememberUpdatedState` — the effect captures a stale closure and calls the wrong version of the callback; wrap with `val current by rememberUpdatedState(lambda)` and call `current()` inside the loop

If effects are replaying or the state machine is hard to test, audit the above list first.
If the ViewModel is growing beyond 150–200 lines, apply the decomposition decision table above.

---

## References

Full implementation content lives in `references/*.md` — one file per heading above with
a pointer (`mvi-viewmodel-base-class`, `implementing-a-viewmodel`, `compose-integration`,
`nav-args-initial-state`, `testing`, `state-patterns`, `multi-source-state`,
`viewmodel-size-decomposition`, `changelog`). Load the specific file named in the pointer
under the matching heading, not all of them.

---

## Related Skills

- `kmp-presenter-module` — simpler ViewModel pattern without `Effect`; use for screens with no one-shot events
- `kmp-navigation` — separate-screens-first decomposition (Option 1); route each feature instead of coordinating
- `kmp-repository-pattern` — repository as single source of truth; how features share data without referencing each other's ViewModels
- `kmp-unit-testing` — `runTest` + Turbine for testing `StateFlow` transitions and `Channel` effects
- `kmp-compose-state-container` — when to use `remember` vs ViewModel as the state container
- `kmp-compose-preview-driven-development` — `FooContent` stateless composables are the fast-preview target
- `kmp-audit` — `_detect_viewmodel_too_many_intents` (15+ Intent variants — a god-ViewModel signal line count alone can miss), `_detect_viewmodel_multiple_stateflows` (2+ exposed StateFlow properties beyond `state` — the Contract pattern's "one State per screen" broken a different way), and `_detect_viewmodel_injects_repository` (a `*Repository` constructor param — the ViewModel-depends-only-on-`:domain` rule, made mechanically enforced instead of just documented)

---

## Output Style

When asked about MVI or screen architecture, respond in this order:
1. recommendation (Contract pattern + MviViewModel)
2. Contract snippet (State, Intent, Effect sealed types)
3. ViewModel snippet (processIntent + emit pattern)
4. Screen / Content split
5. why Channel over SharedFlow for effects

Keep each snippet to one block. Use the user's actual screen name and state fields when provided.

---

## Changelog

Full content: `references/changelog.md`.

