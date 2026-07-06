---
name: kotlin-multiplatform-presenter-module
description: >
  Sets up the :presenter module in a KMP feature — pure Kotlin ViewModels,
  MVI UiState/UiIntent contracts, Koin wiring, and convention plugin. No Compose
  dependency so ViewModels are testable on plain JVM without an Android device or emulator.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-18'
  keywords:
    - presenter
    - ViewModel
    - MVI
    - KMP
    - Kotlin Multiplatform
    - pure KVM
    - JVM testable
    - Koin
    - UiState
    - UiIntent
    - StateFlow
---

## When to Use This Skill

Use when you need to:
- Add a `:presenter` module to a KMP feature
- Write a ViewModel that is testable on plain JVM (no Android/Compose dep)
- Define MVI contracts (`UiState`, `UiIntent`) isolated from Compose
- Wire the ViewModel into Koin so the `:ui` layer can consume it
- Test ViewModel logic with `runTest` + Turbine, no emulator

**Trigger keywords:** presenter module, ViewModel testable, MVI ViewModel, KMP ViewModel,
pure Kotlin ViewModel, JVM ViewModel test, UiState UiIntent, StateFlow ViewModel,
presenter layer, no Compose ViewModel, screen logic, UI state, state management,
screen state, ViewModel setup, test ViewModel, screen behavior, handle user input,
form state, form handling, screen interaction.

**Freshness rule:** `androidx.lifecycle.viewmodel` KMP artifact and its `commonMain` API
change between lifecycle versions — recheck `libs.versions.toml` before wiring.

---

## Recommendation First

Default to **a single `:presenter` module per feature, with `UiState` + `UiIntent` sealed classes
and a `ViewModel` that exposes `StateFlow<UiState>`**.

Why:
- no Compose dependency in `:presenter` = ViewModels compile and test on pure JVM
- `StateFlow` is the simplest state contract — no shared flow races, no replay hacks
- `UiIntent` sealed class makes every user action explicit and testable
- Koin `@KoinViewModel` annotation generates the module — no manual boilerplate

The Screen/Content split in `:ui` is the counterpart: `Screen` holds the ViewModel,
`Content` is a stateless `@Composable` that accepts `UiState` — no ViewModel reference in tests.

---

## Module Structure

```
feature/<name>/presenter/
├── build.gradle.kts
└── src/
    └── commonMain/kotlin/GROUP_ID/feature/<name>/presenter/
        ├── <Name>ViewModel.kt
        ├── <Name>UiState.kt
        ├── <Name>UiIntent.kt
        └── di/
            └── <Name>PresenterModule.kt    ← manual mode only
```

---

## Convention Plugin: `GROUP_ID.feature.presenter.gradle.kts`

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)   // KMP, no Compose flavour
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

Key points:
- `jvm()` target is declared explicitly so `jvmTest` runs on CI without a device
- `androidx.lifecycle.viewmodel` — use the KMP artifact, NOT `lifecycle-viewmodel-compose`
- NO `org.jetbrains.compose` or `org.jetbrains.kotlin.plugin.compose` plugins here

---

## Module `build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.presenter")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.presenter"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.FEATURE_NAME.domain)
        }
    }
}
```

---

## MVI Contracts

### `<Name>UiState.kt`

```kotlin
sealed interface AuthUiState {
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
```

### `<Name>UiIntent.kt`

```kotlin
sealed interface AuthUiIntent {
    data class LoadUser(val userId: String) : AuthUiIntent
    data object Retry : AuthUiIntent
    data object SignOut : AuthUiIntent
}
```

### `<Name>ViewModel.kt`

```kotlin
class AuthViewModel(
    private val getUser: GetUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AuthUiIntent) {
        when (intent) {
            is AuthUiIntent.LoadUser -> loadUser(intent.userId)
            AuthUiIntent.Retry -> retry()
            AuthUiIntent.SignOut -> signOut()
        }
    }

    private fun loadUser(userId: String) {
        viewModelScope.launch {
            getUser(userId)
                .catch { _uiState.value = AuthUiState.Error(it.message ?: "Unknown error") }
                .collect { _uiState.value = AuthUiState.Success(it) }
        }
    }

    private fun retry() { /* re-trigger last load */ }
    private fun signOut() { /* clear state */ }
}
```

No Compose import anywhere in this file.

---

## Multi-Source State with `combine`

When a screen needs data from two or more independent flows, merge them with `combine`
into a single `StateFlow`. Never call `collect` inside a `collect` — that nests coroutines
and causes stale data when either source changes.

```kotlin
class DashboardViewModel(
    private val userRepo: UserRepository,
    private val notificationsRepo: NotificationsRepository,
    private val connectivityRepo: ConnectivityRepository,
) : ViewModel() {

    // ✓ Three independent flows merged into one StateFlow
    val uiState: StateFlow<DashboardUiState> =
        combine(
            userRepo.observeUser(),
            notificationsRepo.observeUnread(),
            connectivityRepo.observeConnected(),
        ) { user, unreadCount, isConnected ->
            DashboardUiState.Success(
                user = user,
                unreadCount = unreadCount,
                isOffline = !isConnected,
            )
        }
        .catch { emit(DashboardUiState.Error(it.message ?: "Unknown")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading,
        )
}
```

### Why `SharingStarted.WhileSubscribed(5_000)`?

| `SharingStarted` value | Upstream behaviour | Problem |
|---|---|---|
| `Eagerly` | Starts immediately, never stops | Upstream runs even when no screen is showing — wastes resources |
| `Lazily` | Starts on first subscriber, never stops | Same leak after screen leaves |
| `WhileSubscribed(5_000)` | Starts on first subscriber, stops 5 s after last subscriber leaves | Survives a config change (rotation takes < 1 s); stops when screen is gone for good |

The 5 000 ms window is long enough to survive an Android config change (rotation, dark-mode
toggle) without restarting the upstream, and short enough to stop it after genuine navigation.

### Dependent flows with `flatMapLatest`

When the second flow depends on a value emitted by the first, use `flatMapLatest`.
It cancels the inner flow and restarts it whenever the outer emits a new value.

```kotlin
class TeamDetailViewModel(
    private val teamRepo: TeamRepository,
    private val memberRepo: MemberRepository,
    private val selectedTeamId: StateFlow<String>,  // from a shared parent ViewModel
) : ViewModel() {

    // ✓ Members reload whenever selectedTeamId changes; previous load is cancelled
    val members: StateFlow<List<Member>> = selectedTeamId
        .flatMapLatest { teamId -> memberRepo.observeMembers(teamId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
```

```kotlin
// ❌ Never nest collect — the inner coroutine outlives the outer emission
viewModelScope.launch {
    selectedTeamId.collect { teamId ->
        memberRepo.observeMembers(teamId).collect { members ->  // leaks!
            _state.update { it.copy(members = members) }
        }
    }
}
```

---

## Koin Wiring

### Annotated mode (default)

```kotlin
// :feature:auth:presenter
@KoinViewModel
class AuthViewModel(private val getUser: GetUserUseCase) : ViewModel() { ... }
```

Koin Compiler Plugin generates the binding. In `:androidApp`:

```kotlin
startKoin {
    androidContext(this@App)
    modules(AppModule.module)
}
```

### Manual mode

```kotlin
// :feature:auth:presenter/di/AuthPresenterModule.kt
val authPresenterModule = module {
    viewModel { AuthViewModel(get()) }
}
```

Declare in `:androidApp`:
```kotlin
modules(authDomainModule, authPresenterModule)
```

---

## Screen/Content Split in `:ui`

The `:ui` module consumes the ViewModel via `koinViewModel()` in the Screen composable.
The Content composable is stateless — it accepts `UiState` directly:

```kotlin
// :feature:auth:ui — AuthScreen.kt
@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuthContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

// :feature:auth:ui — AuthContent.kt
@Composable
fun AuthContent(
    state: AuthUiState,
    onIntent: (AuthUiIntent) -> Unit,
) {
    when (state) {
        AuthUiState.Loading -> CircularProgressIndicator()
        is AuthUiState.Success -> UserProfile(state.user)
        is AuthUiState.Error -> ErrorView(state.message, onRetry = { onIntent(AuthUiIntent.Retry) })
    }
}

@Preview
@Composable
private fun AuthContentLoadingPreview() {
    AuthContent(state = AuthUiState.Loading, onIntent = {})
}

@Preview
@Composable
private fun AuthContentSuccessPreview() {
    AuthContent(state = AuthUiState.Success(PreviewData.user), onIntent = {})
}
```

`AuthContent` is a pure render function — ideal for Desktop previews and Roborazzi screenshot tests.

---

## Related Skills

- `kotlin-multiplatform-clean-architecture` — the layer contract that justifies this split
- `kotlin-multiplatform-feature-scaffold` — creates the module structure; presenter is one of six layers
- `kotlin-multiplatform-unit-testing` — how to test `AuthViewModel` with `runTest` + Turbine
- `kotlin-multiplatform-preview-driven-development` — `AuthContent` previews on Desktop JVM
- `kotlin-multiplatform-roborazzi` — screenshot tests run from `AuthContent` previews

---

## Common Anti-Patterns

- nesting `collect` inside `collect` for multi-source state — use `combine()` instead; nested collectors leak coroutines and miss updates
- using `SharingStarted.Eagerly` or `SharingStarted.Lazily` in `stateIn` — upstream never stops after navigation; use `WhileSubscribed(5_000)` so it stops 5 s after the screen leaves
- using `flatMap` instead of `flatMapLatest` for dependent flows — previous inner coroutine keeps running alongside the new one
- importing `androidx.compose.*` or `org.jetbrains.compose.*` in `:presenter` — kills JVM testability
- putting `UiState` / `UiIntent` in `:ui` — they must live in `:presenter` so tests can use them without Compose
- calling use cases directly from `:ui` screens — all business invocations must go through the ViewModel
- using `MutableSharedFlow(replay = 1)` as a state holder — use `MutableStateFlow` instead; SharedFlow is for one-shot events
- calling `_state.value = _state.value.copy(...)` without a mutex — race condition under concurrent updates; use `update {}` instead

If `viewModelScope` is not available, ensure the KMP lifecycle artifact is on the `commonMain` classpath.

---

## Output Style

When asked to set up a presenter module or ViewModel, respond in this order:
1. MVI contracts (`UiState`, `UiIntent` sealed classes)
2. `ViewModel` implementation
3. convention plugin or `build.gradle.kts` snippet
4. Koin wiring (annotated or manual)
5. Screen/Content split in `:ui` that consumes the ViewModel

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Add multi-source state patterns: combine() for merging flows, SharingStarted.WhileSubscribed(5_000) explanation, flatMapLatest for dependent flows. Three new anti-patterns. |
| 2026-06-18 | Initial release. |
