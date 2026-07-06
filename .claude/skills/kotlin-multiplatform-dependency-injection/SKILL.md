---
name: kotlin-multiplatform-dependency-injection
description: >
  KMP dependency injection with Koin — recommend manual modules first, then annotated
  mode when less wiring is preferred. Covers app/feature scope boundaries, constructor
  injection, module organization, platform startup, test overrides, and the anti-patterns
  that hide architecture problems behind DI. Use this when deciding how to wire KMP
  dependencies instead of repeating Koin setup across other skills.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-13'
  keywords:
    - dependency injection
    - DI
    - Koin
    - manual modules
    - annotated Koin
    - constructor injection
    - module scope
    - ViewModel injection
    - test override
    - KMP DI
    - Kotlin Multiplatform
    - dependency graph
    - startKoin
---

## When to Use This Skill

Use this skill when you need to:
- Decide how to wire dependencies in a KMP app or feature
- Choose between manual Koin modules and annotated Koin mode
- Organize app, core, feature, and platform DI modules
- Override bindings in tests or previews
- Review whether DI is hiding a boundary problem

**Recommended default:** manual Koin modules with constructor injection.
Use annotated mode when you want less wiring and the project is comfortable with the
compiler-plugin workflow.

**Trigger keywords:** dependency injection, DI, Koin, manual modules, annotated mode,
constructor injection, startKoin, module scope, ViewModel injection, test override,
single binding, factory binding, qualifier,
inject dependency, wire dependencies, Koin module, provide dependency,
Koin setup, IoC, inversion of control, Hilt alternative, service locator.

**Freshness rule:** Koin 4 annotation processing and compiler-plugin conventions change —
recheck the Koin docs and changelog when upgrading past a minor version.

---

## Version Catalog Entries

Add to `gradle/libs.versions.toml` before wiring any Koin module:

```toml
[versions]
koin = "4.2.2"

[libraries]
koin-core              = { module = "io.insert-koin:koin-core",              version.ref = "koin" }
koin-core-viewmodel    = { module = "io.insert-koin:koin-core-viewmodel",    version.ref = "koin" }
koin-compose           = { module = "io.insert-koin:koin-compose",           version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-android           = { module = "io.insert-koin:koin-android",           version.ref = "koin" }
koin-androidx-compose  = { module = "io.insert-koin:koin-androidx-compose",  version.ref = "koin" }

[plugins]
kotlin-koin            = { id = "org.jetbrains.kotlin.plugin.koin",          version.ref = "kotlin" }
```

> If `feature-scaffold` was applied first, these entries are already present — do not duplicate them.

---

## Recommendation First

Default to **manual modules + constructor injection**.

Why:
- the dependency graph stays visible in code
- tests are easier to override
- feature boundaries are easier to audit
- the project does not rely on generated wiring to understand startup

Use annotated mode when:
- the app wants less module boilerplate
- the team is comfortable with Koin compiler-plugin conventions
- the bindings are straightforward and not heavily qualified

---

## Project Structure

Show DI in the same places the architecture already uses it:

```text
androidApp/
  src/main/kotlin/.../App.kt
core/
  common/
    di/CommonModule.kt
  network/
    di/NetworkModule.kt
  database/
    di/DatabaseModule.kt
  ui/
    di/UiModule.kt
feature/
  auth/
    domain/
      di/AuthDomainModule.kt
    data/
      di/AuthDataModule.kt
    ui/
      di/AuthUiModule.kt
```

Rules:
- `:app` or platform bootstrap owns `startKoin`
- `:core:*` owns shared platform-independent bindings
- `:feature:*:domain` owns use-case bindings
- `:feature:*:data` owns repository / data-source bindings
- `:feature:*:ui` owns ViewModel bindings
- constructors do the real work; Koin only assembles objects

---

## Manual Mode

Manual mode is the recommended baseline.

```kotlin
// feature/auth/data/src/commonMain/kotlin/.../di/AuthDataModule.kt
val authDataModule = module {
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
```

```kotlin
// feature/auth/domain/src/commonMain/kotlin/.../di/AuthDomainModule.kt
val authDomainModule = module {
    factory { LoginUseCase(get()) }
    factory { ObserveCurrentUserUseCase(get()) }
}
```

```kotlin
// feature/auth/ui/src/commonMain/kotlin/.../di/AuthUiModule.kt
val authUiModule = module {
    viewModel { AuthViewModel(get(), get()) }
}
```

### ViewModels with `SavedStateHandle`

Koin's AndroidX ViewModel integration provides `SavedStateHandle` automatically via
`CreationExtras` — you do not need to resolve it manually. Declare it as a constructor
parameter and use `viewModelOf` (Koin 4+ DSL) or the `get()` shorthand:

```kotlin
// ViewModel — SavedStateHandle is just another constructor parameter
class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repo: CheckoutRepository,
) : MviViewModel<CheckoutContract.State, CheckoutContract.Intent, CheckoutContract.Effect>(
    initialState = CheckoutContract.State(),
) {
    // Read nav result written by a child screen
    init {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("selected_city", null)
                .filterNotNull()
                .collect { city ->
                    updateState { copy(city = city) }
                    savedStateHandle["selected_city"] = null  // consume once
                }
        }
    }
}

// Koin module — option 1: viewModelOf (Koin 4, zero boilerplate)
val checkoutUiModule = module {
    viewModelOf(::CheckoutViewModel)   // resolves SavedStateHandle + CheckoutRepository automatically
}

// Koin module — option 2: explicit viewModel {} if you need custom qualifiers
val checkoutUiModule = module {
    viewModel { CheckoutViewModel(get(), get()) }
    // get() for SavedStateHandle is resolved by Koin's ViewModelFactory from CreationExtras
}
```

**Rules:**
- `viewModelOf(::ClassName)` is the preferred form — less code, same behavior
- Never construct `SavedStateHandle()` yourself — always let Koin/AndroidX provide it
- `savedStateHandle.getStateFlow<T?>(key, null)` is the idiomatic way to receive back-stack results
- Access nav-args set by Navigation Compose via the same `savedStateHandle`: the navigation library writes route arguments there automatically

```kotlin
// androidApp/src/main/kotlin/.../App.kt
startKoin {
    androidContext(this@App)
    modules(
        commonModule,
        networkModule,
        databaseModule,
        authDataModule,
        authDomainModule,
        authUiModule,
    )
}
```

> **Existing project:** if `startKoin` is already called somewhere in the app, do **not** add
> a second call — that throws `KoinApplicationAlreadyStartedException`. Instead, add new
> modules to the existing `modules(...)` list, or call `loadKoinModules(newModule)` at any
> point after startup.

Use manual mode when you want:
- explicit dependencies
- custom qualifiers
- easy test overrides
- fewer moving parts during audits

---

## Annotated Mode

Use annotated mode only when the project wants less wiring.

```kotlin
@Single
class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
) : AuthRepository
```

```kotlin
@KoinViewModel
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
) : MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
    initialState = AuthContract.State(),
)
```

Use annotated mode when:
- the modules are mostly straightforward single bindings
- you want less explicit module wiring
- the project already uses the Koin compiler plugin consistently

Do not mix styles randomly. Pick one per project or enforce a clear rule:
- manual for infrastructure and feature wiring
- annotated only for simple class graphs

---

## Scope Rules

### App scope
- app-wide config
- network client
- database driver
- shared dispatchers
- logging and analytics

### Feature scope
- repository implementation
- use cases
- ViewModels
- feature-specific helpers

### Screen scope
- ephemeral UI state stays in Compose state, not Koin
- use Koin to create the ViewModel, not to store screen flags

### Platform scope
- platform-specific SDK wrappers belong in platform modules
- inject them through platform-specific modules or `expect/actual` when needed

### Session scope — objects that exist only while the user is logged in

Some objects (authenticated API client, user preferences, per-user cache) must be created
on login and destroyed on logout. Koin named scopes handle this without polluting the
app-wide graph with nullable holders.

```kotlin
// :app — scope definition
val SESSION_SCOPE_ID = named("user_session")

val sessionModule = module {
    // Objects that only exist while authenticated
    scope(SESSION_SCOPE_ID) {
        scoped { AuthenticatedApiClient(get(), get<UserSession>()) }
        scoped { UserPreferencesRepository(get<UserSession>().userId, get()) }
    }
}

// On login — create the scope with a unique ID
fun onLoginSuccess(userId: String) {
    getKoin().createScope("session_$userId", SESSION_SCOPE_ID)
}

// On logout — close the scope; all scoped objects are destroyed
fun onLogout(userId: String) {
    getKoin().getScopeOrNull("session_$userId")?.close()
}

// Inject from the session scope in a ViewModel
class ProfileViewModel(
    private val scope: Scope,   // the session scope
) : ViewModel() {
    private val prefs: UserPreferencesRepository by lazy { scope.get() }
}
```

**Rules:**
- Name the scope with a user ID so multiple concurrent sessions (test, multi-account) don't collide
- Close the scope in `SessionViewModel.onCleared()` or a dedicated logout use case — not in a composable
- Never use `single {}` for objects that have a logged-in/logged-out lifecycle — they will hold stale state after logout

---

## Testing Overrides

Tests should replace bindings, not production code.

```kotlin
val testAuthModule = module {
    single<AuthRepository> { FakeAuthRepository() }
    single<Clock> { FakeClock() }
}
```

```kotlin
startKoin {
    modules(testAuthModule)
}
```

Prefer replacing:
- repositories
- remote data sources
- clocks
- dispatchers
- platform wrappers

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — module structure this skill populates with DI modules
- `kotlin-multiplatform-mvi` — ViewModels are wired via Koin using `viewModel {}` bindings
- `kotlin-multiplatform-repository-pattern` — repository and data source bindings live in feature DI modules
- `kotlin-multiplatform-network-layer` — `HttpClient` and `NetworkDataSource` are app-scope singletons in Koin
- `kotlin-multiplatform-sqldelight-setup` — database driver and DAO bindings live in `:core:database` DI module

---

## Common Anti-Patterns

- constructing `SavedStateHandle()` manually — always use `viewModelOf` or `viewModel { ViewModel(get()) }`; Koin provides it from CreationExtras
- using `viewModel { ViewModel(get(), get()) }` when `viewModelOf(::ViewModel)` would do — adds boilerplate for no gain; only use the explicit form for custom qualifiers
- injecting business rules into Koin modules
- resolving dependencies inside composables when screen-boundary injection is enough
- making everything a singleton by habit
- mixing manual and annotated bindings without a project rule
- hiding bad boundaries behind DI
- putting ephemeral screen state in Koin

If the DI graph feels too complicated, audit the architecture first.

---

## Output Style

When asked about DI, respond in this order:
1. recommendation
2. project structure
3. code snippet
4. why that choice is preferred
5. main alternative

Keep the snippet small and direct. If the user wants a project-specific answer, map the
bindings to the actual module names in the repo.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Add session scope pattern: named Koin scope created on login, closed on logout; rules for auth-gated objects. One new anti-pattern.
| 2026-06-28 | Add SavedStateHandle + Koin wiring section: viewModelOf preferred form, automatic CreationExtras injection, getStateFlow for back-stack results. Two new anti-patterns. |
| 2026-06-13 | Initial release. |
