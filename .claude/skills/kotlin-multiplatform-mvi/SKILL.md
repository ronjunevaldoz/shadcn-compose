---
name: kotlin-multiplatform-mvi
description: >
  MVI (Model-View-Intent) architecture pattern for Kotlin Multiplatform + Compose
  Multiplatform. Covers: the Contract pattern (State/Intent/Effect per screen),
  MviViewModel base class with StateFlow for state and Channel for one-shot effects,
  atomic state updates, Compose screen/content split, testing ViewModels with Turbine,
  and the most common MVI pitfalls in KMP. Zero new dependencies — builds on
  androidx.lifecycle.ViewModel and kotlinx.coroutines already present in feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
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
---

## When to Use This Skill

Use when you need to:
- Implement a screen with observable UI state in a Kotlin Multiplatform + CMP project
- Handle one-shot side effects (navigation, toasts, dialogs) safely without replay bugs
- Structure a ViewModel that's testable without a Compose/Android dependency
- Explain or implement MVI, UDF (unidirectional data flow), or a screen state machine

**Requires:** `kotlin-multiplatform-feature-scaffold` project structure.
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
typed error state, UiError sealed, shared ViewModel, wizard ViewModel, multi-step flow.

**Freshness rule:** `lifecycle-viewmodel-compose` and CMP lifecycle integration change between
releases — recheck the AndroidX lifecycle and JetBrains CMP docs before upgrading.

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

Place this in `:core:common` (or `:core:ui`) so all feature ViewModels can extend it.

```kotlin
// :core:common/src/commonMain/kotlin/GROUP_ID/core/mvi/MviViewModel.kt
package GROUP_ID.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern.
 *
 * - [State]  — immutable data class representing everything the screen needs to render
 * - [Intent] — sealed interface of user actions / events
 * - [Effect] — sealed interface of one-shot side effects (navigation, toasts, dialogs)
 *
 * Usage:
 * ```
 * class AuthViewModel(private val repo: AuthRepository) :
 *     MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
 *         initialState = AuthContract.State()
 *     ) {
 *
 *     override fun handleIntent(intent: AuthContract.Intent) {
 *         when (intent) {
 *             is AuthContract.Intent.LoginClicked -> login()
 *             ...
 *         }
 *     }
 * }
 * ```
 */
abstract class MviViewModel<State : Any, Intent : Any, Effect : Any>(
    initialState: State,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    // Catches uncaught exceptions from handleIntent coroutines; subclasses may override
    // to update error state instead of crashing silently on KMP targets.
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throw throwable  // rethrow so crash reporters and tests can see it
    }

    /**
     * Called by the UI layer. Routes the intent to [handleIntent] on [viewModelScope].
     */
    fun onIntent(intent: Intent) {
        viewModelScope.launch(exceptionHandler) { handleIntent(intent) }
    }

    /**
     * Implement per-ViewModel intent handling. Runs on [viewModelScope].
     * Can be a suspend function — safe to call suspend APIs directly.
     */
    protected abstract suspend fun handleIntent(intent: Intent)

    /**
     * Atomically update state. Uses compare-and-swap — safe under concurrent intent handling.
     */
    protected fun updateState(block: State.() -> State) {
        _state.update(block)
    }

    /**
     * Send a one-shot effect. Buffered — delivered when a collector is active.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
```

---

## Implementing a ViewModel

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthViewModel.kt
package GROUP_ID.feature.auth.ui

import GROUP_ID.core.mvi.MviViewModel
import GROUP_ID.feature.auth.domain.AuthRepository
import GROUP_ID.feature.auth.domain.LoginResult

class AuthViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
    initialState = AuthContract.State(),
) {

    override suspend fun handleIntent(intent: AuthContract.Intent) {
        when (intent) {
            is AuthContract.Intent.EmailChanged ->
                updateState { copy(email = intent.value, error = null) }

            is AuthContract.Intent.PasswordChanged ->
                updateState { copy(password = intent.value, error = null) }

            is AuthContract.Intent.LoginClicked -> login()

            is AuthContract.Intent.ForgotPasswordClicked ->
                sendEffect(AuthContract.Effect.NavigateToForgotPassword)
        }
    }

    private suspend fun login() {
        val current = state.value
        if (current.isLoading) return   // guard — debounce rapid taps

        updateState { copy(isLoading = true, error = null) }

        when (val result = authRepository.login(current.email, current.password)) {
            is LoginResult.Success -> {
                updateState { copy(isLoading = false) }
                sendEffect(AuthContract.Effect.NavigateToHome)
            }
            is LoginResult.Error -> {
                // ✓ Always reset isLoading on error — forgetting this is a common bug
                updateState { copy(isLoading = false, error = result.message) }
                sendEffect(AuthContract.Effect.ShowError(result.message))
            }
        }
    }
}
```

---

## Compose Integration: Screen / Content Split

Split every screen into two composables:

- **`AuthScreen`** — wired to ViewModel, handles navigation, collects effects.
  No preview annotation.
- **`AuthContent`** — pure composable, receives `state` + `onIntent` lambda.
  Fully previewable and testable without a ViewModel.

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthScreen.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import GROUP_ID.core.designsystem.components.LocalToastHostState
import GROUP_ID.core.designsystem.components.ToastVariant
import org.koin.compose.viewmodel.koinViewModel

/**
 * Wired screen — owns navigation and side-effect handling.
 * Never use this in Compose @Preview.
 */
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toast = LocalToastHostState.current

    // Collect effects exactly once, scoped to this composable's lifecycle
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthContract.Effect.NavigateToHome ->
                    onNavigateToHome()

                is AuthContract.Effect.NavigateToForgotPassword ->
                    onNavigateToForgotPassword()

                is AuthContract.Effect.ShowError ->
                    toast.show(effect.message, variant = ToastVariant.Destructive)
            }
        }
    }

    AuthContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
```

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthContent.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.components.AppTextField
import GROUP_ID.core.designsystem.components.AppSpinner
import GROUP_ID.core.designsystem.styles.ButtonVariant
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pure composable — no ViewModel dependency, fully previewable.
 */
@Composable
fun AuthContent(
    state: AuthContract.State,
    onIntent: (AuthContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        AppTextField(
            value = state.email,
            onValueChange = { onIntent(AuthContract.Intent.EmailChanged(it)) },
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        AppTextField(
            value = state.password,
            onValueChange = { onIntent(AuthContract.Intent.PasswordChanged(it)) },
            placeholder = "Password",
            isPassword = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            AppText(text = state.error, style = TextStyle.BodySmall, color = colors.destructive)
        }

        Spacer(Modifier.height(24.dp))

        AppButton(
            onClick = { onIntent(AuthContract.Intent.LoginClicked) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) AppSpinner(color = colors.onPrimary)
            else AppText("Sign in")
        }

        Spacer(Modifier.height(8.dp))

        AppButton(
            onClick = { onIntent(AuthContract.Intent.ForgotPasswordClicked) },
            variant = ButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppText("Forgot password?")
        }
    }
}

@Preview
@Composable
private fun AuthContentPreview() {
    AuthContent(state = AuthContract.State(), onIntent = {})
}

@Preview
@Composable
private fun AuthContentLoadingPreview() {
    AuthContent(state = AuthContract.State(isLoading = true), onIntent = {})
}

@Preview
@Composable
private fun AuthContentErrorPreview() {
    AuthContent(
        state = AuthContract.State(error = "Invalid credentials"),
        onIntent = {},
    )
}
```

### `collectAsStateWithLifecycle` vs `collectAsState`

Always use `collectAsStateWithLifecycle()` in production screens. It pauses collection
when the composable's lifecycle drops below `STARTED` (screen goes to background) —
saving battery and stopping unnecessary work.

```kotlin
// ❌ collectAsState — keeps collecting even when the screen is in the background
val state by viewModel.state.collectAsState()

// ✓ collectAsStateWithLifecycle — pauses when lifecycle < STARTED
val state by viewModel.state.collectAsStateWithLifecycle()
```

| | `collectAsState` | `collectAsStateWithLifecycle` |
|---|---|---|
| Lifecycle-aware | No — always active | Yes — pauses below `STARTED` |
| Battery / CPU | Wastes work in background | Efficient |
| Use in | `@Preview`, tests | Production screens |
| Import | `androidx.compose.runtime` | `androidx.lifecycle.compose` |

Exception: inside `@Preview` composables there is no lifecycle, so `collectAsState` is
required. Never use `collectAsStateWithLifecycle` in a preview — it throws at preview time.

---

### `LaunchedEffect` vs `DisposableEffect` vs `SideEffect`

| Effect API | When it runs | Has cleanup? | Use for |
|---|---|---|---|
| `LaunchedEffect(key)` | On entry + when key changes; cancels on exit or key change | No (cancel is implicit) | Collecting flows, one-shot coroutines, side-effect on key change |
| `DisposableEffect(key)` | Synchronously on entry + when key changes; `onDispose` on exit | Yes — `onDispose {}` | Add/remove listeners, set/clear a holder, subscribe/unsubscribe resources |
| `SideEffect` | After **every** successful recomposition; no key | No | Sync Compose state to non-Compose code (analytics screen name, system UI flags) |

```kotlin
// LaunchedEffect — collect effects from ViewModel (coroutine, cancels when composable exits)
LaunchedEffect(viewModel) {
    viewModel.effect.collect { effect -> handleEffect(effect) }
}

// DisposableEffect — set/clear NavControllerHolder (synchronous, cleanup guaranteed)
DisposableEffect(navController) {
    holder.current = navController
    onDispose { holder.current = null }
}

// SideEffect — push current screen name to analytics after every recomposition
SideEffect {
    analytics.setCurrentScreen(screenName)
}
```

**Choosing between them:**
1. If you need a coroutine → `LaunchedEffect`
2. If you need guaranteed cleanup (listener, holder, resource) → `DisposableEffect`
3. If you need to push state out to non-Compose code on every frame → `SideEffect`

---

### `rememberUpdatedState` — latest lambda without restarting the effect

When a `LaunchedEffect` captures a lambda (callback from a parent) that might change
between recompositions, the effect has two bad options:
- use the lambda as the key → effect restarts on every callback change (defeats the point)
- ignore the change → effect calls a stale lambda

`rememberUpdatedState` solves both: it gives the effect a **stable reference** that always
delegates to the latest value, without restarting the coroutine.

```kotlin
@Composable
fun AutoSavingTimer(onAutoSave: () -> Unit) {
    // ✓ Always reads the latest onAutoSave without restarting the LaunchedEffect
    val currentOnAutoSave by rememberUpdatedState(onAutoSave)

    LaunchedEffect(Unit) {   // key = Unit — this coroutine never restarts
        while (true) {
            delay(30_000)
            currentOnAutoSave()   // delegates to the latest lambda
        }
    }
}
```

```kotlin
// ❌ Without rememberUpdatedState — lambda from parent may be stale after recomposition
LaunchedEffect(Unit) {
    while (true) {
        delay(30_000)
        onAutoSave()   // captured at launch time, not the latest value
    }
}

// ❌ Using the lambda as the key — effect restarts on every parent recomposition
LaunchedEffect(onAutoSave) {
    while (true) {
        delay(30_000)
        onAutoSave()   // correct value, but the timer resets on every parent recompose
    }
}
```

**When to reach for `rememberUpdatedState`:**

| Situation | Use |
|---|---|
| `LaunchedEffect(Unit)` captures a lambda that may change | `rememberUpdatedState(lambda)` |
| `LaunchedEffect(Unit)` captures a value that may change but shouldn't restart the effect | `rememberUpdatedState(value)` |
| Effect key already tracks the source of truth (e.g., `LaunchedEffect(viewModel)`) | Not needed — the effect restarts cleanly on key change |
| The lambda is stable (never reassigned after initial composition) | Not needed |

---

### Why `LaunchedEffect(viewModel)` not `LaunchedEffect(Unit)`?

`LaunchedEffect(Unit)` is started once per composition entry and cancelled when the
composable leaves the tree. `LaunchedEffect(viewModel)` ties the lifecycle to the ViewModel
instance — if the screen is re-entered with the same ViewModel (e.g., bottom nav tab
switch), the same coroutine resumes rather than starting a new one. Either works for most
cases, but `viewModel` is more correct when the ViewModel outlives a single composition.

---

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
the AndroidX `CreationExtras` bag. See `kotlin-multiplatform-dependency-injection` for
the full SavedStateHandle + Koin reference.

With **Koin annotated mode** (Koin Compiler Plugin):
```kotlin
@KoinViewModel
class AuthViewModel(private val authRepository: AuthRepository) : MviViewModel<...>(...) { ... }
```

---

## Nav Args as Initial State

Route arguments (e.g. `userId`, `orderId`) must reach the ViewModel as constructor
parameters — not as `Intent`. They are identity, not user input.

```kotlin
// commonMain nav route
@Serializable
data class UserProfileRoute(val userId: String)

// ViewModel receives the arg directly
class UserProfileViewModel(
    private val userId: String,           // from NavBackStackEntry via Koin
    private val repo: UserProfileRepository,
) : MviViewModel<UserProfileContract.State, ...>(UserProfileContract.State.Loading) {

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { UserProfileContract.State.Loading }
            repo.getProfile(userId).fold(
                onSuccess = { updateState { UserProfileContract.State.Success(it) } },
                onFailure = { updateState { UserProfileContract.State.Error(it.message.orEmpty()) } },
            )
        }
    }
}
```

Wire the arg through Koin using `getNavArguments()` or a `SavedStateHandle`:

```kotlin
// :feature:profile:ui/di
val profileUiModule = module {
    viewModel { params ->
        UserProfileViewModel(userId = params.get(), repo = get())
    }
}

// Screen — passes arg at call site
@Composable
fun UserProfileScreen(
    route: UserProfileRoute,
    onBack: () -> Unit,
    viewModel: UserProfileViewModel = koinViewModel(parameters = { parametersOf(route.userId) }),
) { ... }
```

**Rule:** never pass identity args as `Intent.Load(id)` — the ViewModel would need to
guard against double-loads and the arg would not survive process death.

---

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

### Test state transitions

Use Turbine to test `StateFlow` emissions as a sequence:

```kotlin
// :feature:auth:ui/src/commonTest/kotlin/.../AuthViewModelTest.kt
class AuthViewModelTest {

    @Test
    fun `login success transitions Loading then clears state and sends NavigateToHome effect`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.state.test {
            // Initial state
            assertEquals(AuthContract.State(), awaitItem())

            viewModel.onIntent(AuthContract.Intent.LoginClicked)

            // Loading
            assertEquals(AuthContract.State(isLoading = true), awaitItem())

            // Cleared
            assertEquals(AuthContract.State(isLoading = false), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure resets isLoading and sends ShowError effect`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(failsWith = "Invalid credentials"))

        // Collect effects alongside state
        val effects = mutableListOf<AuthContract.Effect>()
        val effectJob = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.state.test {
            awaitItem()  // initial
            viewModel.onIntent(AuthContract.Intent.LoginClicked)
            awaitItem()  // loading = true
            val errorState = awaitItem()  // loading = false, error set
            assertFalse(errorState.isLoading)
            assertEquals("Invalid credentials", errorState.error)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(
            listOf(AuthContract.Effect.ShowError("Invalid credentials")),
            effects,
        )
        effectJob.cancel()
    }

    @Test
    fun `email change updates state and clears error`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.state.test {
            awaitItem()  // initial

            viewModel.onIntent(AuthContract.Intent.EmailChanged("new@example.com"))

            val updated = awaitItem()
            assertEquals("new@example.com", updated.email)
            assertNull(updated.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Test content composable independently

```kotlin
class AuthContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `login button disabled when loading`() {
        composeTestRule.setContent {
            AuthContent(
                state = AuthContract.State(isLoading = true),
                onIntent = {},
            )
        }
        composeTestRule.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    @Test
    fun `error message shown when error in state`() {
        composeTestRule.setContent {
            AuthContent(
                state = AuthContract.State(error = "Invalid credentials"),
                onIntent = {},
            )
        }
        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
}
```

### Fake repository pattern

```kotlin
// :core:testing/src/commonMain/kotlin/GROUP_ID/core/testing/fakes/FakeAuthRepository.kt
class FakeAuthRepository(
    private val failsWith: String? = null,
) : AuthRepository {

    val loginCalls = mutableListOf<Pair<String, String>>()

    override suspend fun login(email: String, password: String): LoginResult {
        loginCalls.add(email to password)
        return if (failsWith != null) LoginResult.Error(failsWith)
        else LoginResult.Success(FakeUser)
    }
}
```

---

## State Patterns

### Loading / Success / Error (LSE) state machine

For screens that load async data, model the full lifecycle explicitly:

```kotlin
object UserProfileContract {

    sealed interface State {
        data object Loading : State
        data class Success(val user: UserProfile) : State
        data class Error(val message: String, val retryable: Boolean = true) : State
    }

    sealed interface Intent {
        data object Retry : Intent
        data class UpdateBio(val bio: String) : Intent
    }

    sealed interface Effect {
        data object ShowSaveSuccess : Effect
    }
}
```

Then in the ViewModel:

```kotlin
class UserProfileViewModel(
    private val repo: UserProfileRepository,
    private val userId: String,
) : MviViewModel<UserProfileContract.State, UserProfileContract.Intent, UserProfileContract.Effect>(
    initialState = UserProfileContract.State.Loading,
) {

    init {
        loadProfile()
    }

    override suspend fun handleIntent(intent: UserProfileContract.Intent) {
        when (intent) {
            is UserProfileContract.Intent.Retry -> {
                updateState { UserProfileContract.State.Loading }
                loadProfile()
            }
            is UserProfileContract.Intent.UpdateBio -> saveBio(intent.bio)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = repo.getProfile(userId)) {
                is Result.Success -> updateState { UserProfileContract.State.Success(result.data) }
                is Result.Error   -> updateState { UserProfileContract.State.Error(result.message) }
            }
        }
    }

    private suspend fun saveBio(bio: String) {
        val current = state.value as? UserProfileContract.State.Success ?: return
        repo.updateBio(bio)
        updateState { UserProfileContract.State.Success(current.user.copy(bio = bio)) }
        sendEffect(UserProfileContract.Effect.ShowSaveSuccess)
    }
}
```

### Inline loading flags vs sealed state

| Pattern | Use when |
|---|---|
| `data class State(isLoading: Boolean, ...)` | Screen shows content AND a loading overlay simultaneously (e.g., saving while form is visible) |
| `sealed interface State { Loading; Success; Error }` | Screen shows fundamentally different UI in each phase (skeleton vs content vs error page) |

### Typed errors in State

Prefer a `sealed class UiError` over raw `String` when the screen needs to distinguish
error categories (network vs auth vs validation) to show different UI or recovery actions.

```kotlin
// :feature:auth:ui
object AuthContract {

    sealed class UiError {
        data object NetworkUnavailable : UiError()
        data object InvalidCredentials : UiError()
        data class Unknown(val message: String) : UiError()
    }

    data class State(
        val isLoading: Boolean = false,
        val error: UiError? = null,
    )
}

// ViewModel maps domain error → UiError at the boundary
private suspend fun login() {
    updateState { copy(isLoading = true, error = null) }
    when (val result = repo.login(email, password)) {
        is LoginResult.Success -> {
            updateState { copy(isLoading = false) }
            sendEffect(AuthContract.Effect.NavigateToHome)
        }
        is LoginResult.Error.Network ->
            updateState { copy(isLoading = false, error = AuthContract.UiError.NetworkUnavailable) }
        is LoginResult.Error.Unauthorized ->
            updateState { copy(isLoading = false, error = AuthContract.UiError.InvalidCredentials) }
        is LoginResult.Error.Unknown ->
            updateState { copy(isLoading = false, error = AuthContract.UiError.Unknown(result.message)) }
    }
}
```

The content composable switches on `UiError` type to show the right copy and recovery action
(retry button for network errors, inline message for auth errors).

Use raw `String` only when there is one error category and the message is always safe to
display directly (e.g., form validation messages from the server).

### Shared ViewModel (multi-step flow / wizard)

When multiple screens form a linear flow (onboarding, checkout, multi-step form), scope a
single ViewModel to the parent `NavBackStackEntry` so all steps share state without
passing data through route arguments.

```kotlin
// The shared ViewModel — lives in :feature:onboarding:ui
class OnboardingViewModel : MviViewModel<OnboardingContract.State, ...>(OnboardingContract.State()) {
    override suspend fun handleIntent(intent: OnboardingContract.Intent) { ... }
}

// Parent destination in NavHost (the flow entry point)
composable<OnboardingRoute> { parentEntry ->
    val viewModel: OnboardingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
    OnboardingFlowHost(viewModel = viewModel)
}

// Step screen inside the flow — retrieves the same ViewModel instance
@Composable
fun OnboardingStep1Screen(navController: NavController) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry<OnboardingRoute>()
    }
    val viewModel: OnboardingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
    ...
}
```

**Rules:**
- The shared ViewModel is owned by the parent entry — it is cleared when the user exits the flow
- Each step screen must retrieve it via `getBackStackEntry<ParentRoute>()`, never via `koinViewModel()` alone (that would create a separate instance per step)
- Only use this pattern when steps genuinely share mutable state; if steps are independent, give each its own ViewModel

---

### Orchestrating multiple features — decision order

When a screen seems to need several feature units — assembling their states, relaying
their effects, persisting the result — **do not orchestrate this in the composable.**
A composable that holds 3+ `koinViewModel()` calls, 5+ `LaunchedEffect` blocks, or
relays effects between ViewModels is a *god composable*: untestable, recomposition-bound,
impossible to preview. But the answer is almost never a bigger ViewModel either.

Work through these in order. **Stop at the first one that fits — do not skip to a
coordinator because it feels powerful.**

#### Two hard rules (never violated)

> **Rule 1 — A ViewModel must NEVER take another ViewModel as a constructor parameter.**
> ViewModels are created by `ViewModelProvider`/factory with their own `viewModelScope`,
> `SavedStateHandle`, and `CreationExtras` — they are not regular DI graph objects.
> Nesting them causes lifecycle conflicts (the child's scope isn't owned by the parent),
> breaks `SavedStateHandle` propagation, and leaks the child past its intended scope.
> `class FooViewModel(val barVm: BarViewModel)` is always wrong.

> **Rule 2 — Features share data through a repository, never through each other.**
> If feature A needs feature B's output, both talk to a shared repository that is the
> single source of truth. A ViewModel never reads or writes another ViewModel's state.

---

#### Option 1 (DEFAULT) — Separate screens + NavHost

**If each feature can be its own screen, make it one.** This is the cleanest decomposition
and the correct default for hub-style apps (a dashboard launching feature screens, a settings
hub, a set of independent tools). There is no coordinator, no combined state, no relays.

```kotlin
// :app navigation — each feature is a route; the host owns nothing
NavHost(navController, startDestination = DashboardRoute) {
    composable<DashboardRoute> { DashboardScreen(onOpen = { navController.navigate(it) }) }
    composable<EditorRoute>    { EditorScreen() }   // owns its own ViewModel
    composable<ImporterRoute>  { ImporterScreen() } // owns its own ViewModel
    // ...
}
```

```kotlin
// Each feature screen owns exactly ONE ViewModel. No feature imports another's VM.
@Composable
fun EditorScreen(vm: EditorViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(vm) { vm.effect.collect { /* nav + toast only */ } }
    EditorContent(state = state, onIntent = vm::onIntent)
}
```

**Shared data flows through a repository — the source of truth that decouples features:**

```kotlin
// :data — every feature observes and writes this; none know about each other
interface ItemRepository {
    val items: Flow<List<Item>>
    suspend fun save(item: Item)
}

class EditorViewModel(private val repo: ItemRepository) : MviViewModel<...> {
    // writes results via repo.save(...) — never touches the dashboard
}

class DashboardViewModel(private val repo: ItemRepository) : MviViewModel<...> {
    // sees every feature's output by observing the repo, not their ViewModels
    val state = repo.items.map { DashboardContract.State(items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardContract.State())
}
```

This is what replaces a `LaunchedEffect { dashboardVm.onIntent(UpdateItems(merged)) }` relay:
the relay is a symptom of a missing repository. With the repo, the dashboard shows output from
every feature without knowing any feature exists.

**Use this when:** features are conceptually separate destinations, even if some share data.
Sharing data is *not* a reason to merge screens — that is what the repository is for.

---

#### Option 2 — One screen, sub-units demoted to State Holders

Only when the product genuinely requires **one screen showing several feature state
machines at once** (a true split-pane editor, not a tab switcher). Each sub-unit becomes a
*State Holder* — a plain class, **not** a `ViewModel` — that receives a `CoroutineScope`.

```kotlin
// :feature:dashboard:presenter — plain class, NOT a ViewModel
class EditorStateHolder(
    private val scope: CoroutineScope,             // injected — never its own viewModelScope
    private val saveItem: SaveItemUseCase,
) {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()
    fun onIntent(intent: EditorIntent) { scope.launch { /* update _state */ } }
}

// The coordinator depends on USE CASES (normal DI), never on ViewModels
class DashboardCoordinatorViewModel(
    private val saveItem: SaveItemUseCase,
    private val assembler: DashboardStateAssembler,
) : MviViewModel<DashboardContract.State, DashboardContract.Intent, DashboardContract.Effect>(
    initialState = DashboardContract.State(),
) {
    // Coordinator owns the holders, created with ITS scope — single lifecycle owner
    private val editor = EditorStateHolder(viewModelScope, saveItem)
    // ...

    val state: StateFlow<DashboardContract.State> =
        combine(editor.state, /* ... */) { editor, /* ... */ -> assembler.combine(editor, /* ... */) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardContract.State())

    init { restorePersistedState(); persistOnChanges() }

    override suspend fun handleIntent(intent: DashboardContract.Intent) { /* delegates to holders */ }
}
```

**Use this when:** all sub-features must be visible and interactive simultaneously AND each
has a real 5+-field state machine. If they don't share a screen → Option 1. If they're
mostly operations → Option 3.

---

#### Option 3 — One screen, sub-units are use cases

When sub-units mostly *do work* rather than hold long-lived state, they are use cases. Fold
their per-type state into the coordinator's single `State` as fields. No holders needed.

**Use this when:** one screen, but the "sub-features" are stateless operations (validate,
transform, submit) rather than independent state machines.

---

### Hardened rules (enforced by the audit)

- **Default to Option 1.** Separate screens + NavHost is the answer unless the product
  *requires* features on one screen at the same time. "They share data" is not such a reason.
- A ViewModel **never** receives another ViewModel — `viewmodel in viewmodel` is a HIGH finding.
- Features share state **only** through a repository — never `vmA` reading `vmB.state`.
- A screen **never** holds 3+ `koinViewModel()` calls — `multi viewmodel screen` finding.
- A screen **never** has 5+ `LaunchedEffect` blocks or 3+ `effect.collect` relays — `god composable` finding.
- State Holders are plain classes taking `scope: CoroutineScope`; the coordinator passes its `viewModelScope`. They are never `ViewModel` subclasses and never call `koinViewModel()`.
- A coordinator depends on **use cases** (regular Koin DI), wired with `viewModelOf(::DashboardCoordinatorViewModel)`.
- State assembly uses `combine(...).stateIn(...)` — never `derivedStateOf` in the composable.
- Effect collection lives in the ViewModel (`init {}` via `viewModelScope.launch`), never in the screen — the screen keeps exactly one `LaunchedEffect(vm)` for its own nav/toast effects.
- Extract state-combination into a pure `StateAssembler` object so precedence rules are unit-tested independently of the ViewModel.

---

## Multi-Source State and Flow Operators

### `combine` — merge two or more flows into one State

When a screen's `State` depends on more than one data source, use `combine` to merge
the flows. The ViewModel then exposes a single `StateFlow<State>` — no separate
`collect` calls, no manual synchronization.

```kotlin
class HomeViewModel(
    private val userRepo: UserRepository,
    private val feedRepo: FeedRepository,
) : MviViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>(
    initialState = HomeContract.State(),
) {
    // Derive state from two independent flows
    val derivedState: StateFlow<HomeContract.State> =
        combine(userRepo.observeUser(), feedRepo.observeFeed()) { user, feed ->
            HomeContract.State(
                userName = user.name,
                feedItems = feed,
                isEmpty = feed.isEmpty(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeContract.State())
}
```

### `SharingStarted.WhileSubscribed(5_000)` — why 5 000 ms?

| Value | Upstream stops when… | Problem |
|---|---|---|
| `Eagerly` | Never | Keeps running even with no collector — wastes battery |
| `Lazily` | Never (after first subscriber) | Same post-login leak |
| `WhileSubscribed(5_000)` | 5 s after last collector leaves | Survives rotation (< 1 s); stops after genuine navigation away |

Always use `WhileSubscribed(5_000)` for `stateIn` in ViewModels exposed to the UI.

### `flatMapLatest` — dependent flows (inner depends on outer)

Use `flatMapLatest` when the inner flow must restart every time the outer emits.
It cancels the running inner coroutine before starting the new one.

```kotlin
class TeamViewModel(
    private val memberRepo: MemberRepository,
    private val selectedTeamId: StateFlow<String>,
) : ViewModel() {

    // Restarts member observation whenever selected team changes
    val members: StateFlow<List<Member>> = selectedTeamId
        .flatMapLatest { teamId -> memberRepo.observeMembers(teamId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
```

### `snapshotFlow` — convert Compose State to a Flow

`snapshotFlow` reads Compose `State` inside a coroutine and emits whenever the value
changes. Use it to bridge Compose state into a coroutine for debouncing, analytics,
or triggering side-effects without polluting the ViewModel with Compose imports.

```kotlin
// Debounce a search text field — field is Compose State, search is a coroutine
@Composable
fun SearchBar(
    query: String,
    onIntent: (SearchContract.Intent) -> Unit,
) {
    var localQuery by remember { mutableStateOf(query) }

    LaunchedEffect(Unit) {
        snapshotFlow { localQuery }
            .debounce(300)
            .distinctUntilChanged()
            .collect { debouncedQuery ->
                onIntent(SearchContract.Intent.Search(debouncedQuery))
            }
    }

    AppTextField(value = localQuery, onValueChange = { localQuery = it })
}
```

**Rules:**
- `snapshotFlow` only reads `State` declared with `mutableStateOf` / `mutableStateListOf`
- Do not read a `StateFlow` inside `snapshotFlow` — use `collectAsState()` first, then read the `State`
- `snapshotFlow` runs in the composition snapshot; avoid side-effects inside the lambda

---

## ViewModel Size and Decomposition

A ViewModel that grows beyond ~150 lines is a smell. Beyond 300 lines it is a violation —
the ViewModel has taken on responsibilities that belong elsewhere.

### God ViewModel symptoms

Stop and decompose when the ViewModel:
- calls more than two unrelated repositories directly
- has multiple `private suspend fun` blocks that each contain their own business logic branches
- mixes data fetching, validation, formatting, and navigation logic in `handleIntent`
- has a `State` data class with more than ~8 fields
- contains `if/else` or `when` chains that span more than 10–15 lines per branch

### Decision table: what to extract

| Symptom | Extract to |
|---|---|
| `handleIntent` branch calls multiple repos in sequence | Use case (`operator fun invoke`) in `:domain` |
| Business rule lives inline (validate, calculate, transform) | Use case or domain function |
| Two unrelated screen sections share one ViewModel | Split into parent + child (shared ViewModel or separate) |
| State has a sub-group of fields that only change together | Nested data class in `State`, or separate ViewModel |
| The same logic appears in two different ViewModels | Use case extracted to `:domain`, shared via injection |

### Extracting a use case

Move logic out of the ViewModel when a `handleIntent` branch:
- calls two or more repositories
- contains branching business rules (eligibility, validation, rollback)
- is long enough to need its own test suite independent of UI state

```kotlin
// ❌ Before — business logic inline in ViewModel (god ViewModel symptom)
private suspend fun placeOrder() {
    val cart = cartRepo.getCart()
    if (cart.items.isEmpty()) {
        updateState { copy(error = "Cart is empty") }
        return
    }
    val inventory = inventoryRepo.check(cart.items)
    if (!inventory.allAvailable) {
        updateState { copy(error = "Some items out of stock") }
        return
    }
    val order = orderRepo.place(cart)
    updateState { copy(isLoading = false) }
    sendEffect(Effect.NavigateToConfirmation(order.id))
}

// ✓ After — use case owns the orchestration
class PlaceOrderUseCase(
    private val cartRepo: CartRepository,
    private val inventoryRepo: InventoryRepository,
    private val orderRepo: OrderRepository,
) {
    suspend operator fun invoke(): PlaceOrderResult {
        val cart = cartRepo.getCart()
        if (cart.items.isEmpty()) return PlaceOrderResult.EmptyCart
        val inventory = inventoryRepo.check(cart.items)
        if (!inventory.allAvailable) return PlaceOrderResult.OutOfStock(inventory.unavailable)
        val order = orderRepo.place(cart)
        return PlaceOrderResult.Success(order.id)
    }
}

// ViewModel is now thin — one call, one when
private suspend fun placeOrder() {
    updateState { copy(isLoading = true) }
    when (val result = placeOrderUseCase()) {
        is PlaceOrderResult.Success      -> sendEffect(Effect.NavigateToConfirmation(result.orderId))
        PlaceOrderResult.EmptyCart       -> updateState { copy(isLoading = false, error = "Cart is empty") }
        is PlaceOrderResult.OutOfStock   -> updateState { copy(isLoading = false, error = "Some items out of stock") }
    }
    updateState { copy(isLoading = false) }
}
```

### Splitting a ViewModel

Split into two ViewModels when the screen has two genuinely independent sections —
different data sources, different lifecycles, no shared state between them.

```kotlin
// ✓ Profile screen with independent "user info" and "activity feed" sections
// Each has its own loading state, error state, and data source

class ProfileInfoViewModel(private val userRepo: UserRepository) :
    MviViewModel<ProfileInfoContract.State, ...>(...) { ... }

class ActivityFeedViewModel(private val feedRepo: FeedRepository) :
    MviViewModel<ActivityFeedContract.State, ...>(...) { ... }

@Composable
fun ProfileScreen(...) {
    val infoVm: ProfileInfoViewModel = koinViewModel()
    val feedVm: ActivityFeedViewModel = koinViewModel()
    // each section gets its own ViewModel, no shared ViewModel needed
}
```

Split into a **shared (parent) ViewModel** only when sections share mutable state and
must stay synchronized — see the Shared ViewModel section above for the pattern.

---

## Common Anti-Patterns

- using `SharedFlow` for effects — events replay on new collectors and break "fire once" guarantees
- emitting `Effect` from `init {}` — fires on every ViewModel recreation, not just on user action
- putting navigation logic inside `State` — navigation is an effect, not persisted state
- using `copy {}` with a stale `state` reference instead of `update {}` — causes lost updates under concurrency
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

## Related Skills

- `kotlin-multiplatform-presenter-module` — simpler ViewModel pattern without `Effect`; use for screens with no one-shot events
- `kotlin-multiplatform-navigation` — separate-screens-first decomposition (Option 1); route each feature instead of coordinating
- `kotlin-multiplatform-repository-pattern` — repository as single source of truth; how features share data without referencing each other's ViewModels
- `kotlin-multiplatform-unit-testing` — `runTest` + Turbine for testing `StateFlow` transitions and `Channel` effects
- `kotlin-multiplatform-compose-state-container` — when to use `remember` vs ViewModel as the state container
- `kotlin-multiplatform-preview-driven-development` — `FooContent` stateless composables are the fast-preview target

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

| Date | Change |
|---|---|
| 2026-06-28 | Add @Stable/@Immutable rule for State types; CoroutineExceptionHandler in MviViewModel base class; rememberUpdatedState section with decision table. Three new anti-patterns.
| 2026-06-28 | Add multi-source state: combine(), WhileSubscribed(5_000) table, flatMapLatest, snapshotFlow with debounce example. Four new anti-patterns.
| 2026-06-28 | Add collectAsStateWithLifecycle vs collectAsState rule; LaunchedEffect vs DisposableEffect vs SideEffect decision table; SavedStateHandle + viewModelOf Koin wiring; four new anti-patterns.
| 2026-06-28 | Add auth gate and back-stack anti-patterns. Two new anti-patterns: storing auth state in MVI State for nav, and Effect.NavigateBack without popUpTo contract. |
| 2026-06-28 | Add ViewModel size rule, god ViewModel symptoms, use case extraction guide, and ViewModel split patterns. Two new anti-patterns for monolithic ViewModels. |
| 2026-06-29 | Reworked feature-orchestration guidance into a decision order led by Option 1 (separate screens + NavHost + repository as source of truth) before any coordinator. Two hard rules (no VM-in-VM, share via repository only). Hardened rules section mapped to audit findings. |
| 2026-06-29 | Coordinator ViewModel section rewritten to State Holder pattern — a ViewModel must never take another ViewModel as a constructor param; demote sub-units to State Holders (plain class + injected scope) or use cases. New anti-pattern + audit detector for VM-in-VM constructor. |
| 2026-06-29 | Added Coordinator ViewModel section — fixes god composables that orchestrate multiple sub-ViewModels in the UI layer (state assembly, effect relays, persistence in LaunchedEffect). New "god composable" anti-pattern. Detected by audit_project.py. |
| 2026-06-28 | Added "When NOT to Use MviViewModel" with thin patterns (no-ViewModel, no-Contract). Updated Recommendation First to lead with start-thin principle. Added Nav Args as Initial State, In-flight Cancellation, Typed Errors in State, Shared ViewModel. |
| 2026-06-06 | Initial release. |
