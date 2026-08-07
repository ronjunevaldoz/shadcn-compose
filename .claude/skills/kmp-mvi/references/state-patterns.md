# State Patterns

Part of `kmp-mvi`. Load this file when working on: state patterns.

---

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
- **A coordinator is not exempt from the same size limits as any other ViewModel.** Choosing Option 2/3 to escape a god composable doesn't grant immunity from becoming a god ViewModel instead — `kmp-audit`'s `_detect_viewmodel_size` still applies to `DashboardCoordinatorViewModel` exactly like any other ViewModel. If a coordinator crosses that threshold even after delegating to State Holders/use cases and extracting a `StateAssembler`, that's a signal the sub-units were never simultaneous-and-interactive enough to justify Option 2/3 in the first place — split back out to Option 1 (separate screens + repository) rather than adding a second layer of coordinators.

---

