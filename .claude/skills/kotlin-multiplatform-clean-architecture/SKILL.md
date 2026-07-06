---
name: kotlin-multiplatform-clean-architecture
description: >
  Defines the 6-layer clean architecture contract for KMP feature modules:
  :model / :api / :domain / :data / :presenter / :ui. Covers layer dependency
  rules, :model vs :api split, internal visibility enforcement, and Detekt
  architecture fitness functions that make violations fail the build.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-28'
  keywords:
    - clean architecture
    - Kotlin Multiplatform
    - KMP
    - multi-module
    - layer dependency
    - internal visibility
    - Detekt
    - architecture rules
    - model module
    - presenter module
---

## When to Use This Skill

Use when you need to:
- Understand or enforce the 6-layer dependency contract across feature modules
- Decide what belongs in `:model` vs `:api` vs `:domain`
- Enforce `internal` visibility so module internals do not leak across layer boundaries
- Write Detekt architecture rules that fail the build on layer violations
- Review a pull request for architecture compliance

**Trigger keywords:** clean architecture, layer contract, dependency rule, model vs api,
internal visibility, architecture violation, Detekt architecture, layer rule, feature layers,
module boundaries, 6-layer architecture, domain isolation, which layer, domain model,
api contract, dependency inversion, layer ownership, where does this code go,
architecture design, content design, code organization, module design, project structure,
layer design, data architecture, content strategy, code structure,
core module, feature module, core vs feature, shared module, use case pattern,
mapper pattern, DTO mapper, domain error, typed error, sealed error, DomainError,
cross-feature navigation, navigate to another feature, AppNavigator, feature dependency.

**Freshness rule:** Detekt rule set API changes between minor versions — recheck the
`ArchitectureRule` DSL when upgrading Detekt.

---

## Recommendation First

**Start thin. Add layers only when they carry weight.** The 6-layer structure is the maximum for a complex feature — not a template to fill in by default.

When you do need the full structure, enforce **strict unidirectional dependency flow:
`:model` → `:api` → `:domain` → `:presenter` → `:ui`** with `:data` as a sibling of
`:presenter` (both depend on `:api`, neither depends on the other).

Why the contract matters when you reach this point:
- `:presenter` with no Compose dep = ViewModels testable on plain JVM
- `:model` as the root = types shared across all layers with no circular risk
- `:ui` depending only on `:presenter` = Compose screens are pure render functions
- `internal` at module boundaries = no accidental cross-layer coupling

Enforce with Gradle dependency declarations first (makes violations uncompilable),
Detekt rules second (catches import-level violations within a valid dep graph).

---

## Layer Contract

```
:model      pure KMP — data classes, sealed types, enums
              ↑ (no deps)
:api        pure KMP — repository interfaces, nav contracts
              ↑ (depends on :model only)
:domain     pure KMP — use cases, business logic
              ↑ (depends on :api)
:data       KMP + platform — Ktor/SQLDelight repository impls
              (depends on :api, NOT :domain or :presenter)
:presenter  pure KMP — ViewModels, MVI state/intent types
              (depends on :domain, NO Compose)
              ↑
:ui         CMP — Compose screens, previews
              (depends on :presenter ONLY)
```

### What goes where

| Layer | Contains | Does NOT contain |
|---|---|---|
| `:model` | `data class`, `sealed class`, `enum class`, `typealias` | Interfaces, business logic, framework deps |
| `:api` | Repository interfaces, nav route contracts | Implementations, data classes |
| `:domain` | Use cases (`operator fun invoke`), pure business rules | Framework deps, DI annotations |
| `:data` | `RepositoryImpl`, DTOs, mappers, data sources | UI state, ViewModels |
| `:presenter` | `ViewModel`, MVI `UiState`, `UiIntent` sealed classes | Compose imports, UI framework |
| `:ui` | `@Composable` screens, `@Preview` functions | Business logic, direct repo/use-case calls |

---

## Layer Weight — Add Only When It Carries Weight

The 6-layer structure is the **maximum** for a complex feature. Start thin and add
layers only when they justify the indirection.

### ViewModel — when to add

| Screen type | ViewModel? | Why |
|---|---|---|
| Static display (help, about, legal) | No | No state to manage |
| Simple local toggle / counter | No | `remember` handles it |
| Async load, display only | Yes — thin | Lifecycle awareness needed |
| Async + user actions + navigation | Yes — full MVI | All three concerns present |

A ViewModel with a single `val state = flow { ... }.stateIn(...)` and no intent handling
is valid — do not wrap it in `MviViewModel` just to follow the pattern.

### Use case — when to add

| Scenario | Use case? | Why |
|---|---|---|
| `return repository.getUser(id)` | No | Pure passthrough — no value added |
| Calls two repositories and combines results | Yes | Orchestration logic belongs in `:domain` |
| Applies a business rule before saving | Yes | Rule must be testable without a ViewModel |
| Same logic needed in two different ViewModels | Yes | Reuse justifies the layer |

If the use case would be one line, call the repository from the ViewModel directly.

### `:data` module — when to add

| Scenario | Separate `:data`? | Why |
|---|---|---|
| Single local data source (DataStore) | Can inline in `:domain` | No DTO mapping or multiple sources |
| Remote + local with caching | Yes | Sync logic and mapping belong in `:data` |
| Multiple data sources with conflict resolution | Yes | Complexity justifies isolation |

### The thin feature — all layers optional

A screen that loads a list and navigates on tap can be as thin as:

```
:feature:notifications
└── ui/            ← Screen composable + NotificationsViewModel (StateFlow only)
    └── SKILL.md   ← no :model, :api, :domain, :data modules needed
```

Only add `:domain` when there is a use case that earns its place. Only add `:data` when
there is a repository implementation worth isolating. The 6-layer structure exists for
features that need it — not as a template to fill in by default.

---

## `:core` vs `:feature` Split

`:core` modules are **shared infrastructure** — code that multiple features depend on
but that has no feature-specific logic. `:feature` modules are **vertical slices** — one
module group per product feature.

| Module | Lives in | What it contains |
|---|---|---|
| `:core:model` | `:core` | Shared domain types (e.g. `User`, `Money`, `AppError`) used across features |
| `:core:api` | `:core` | Shared repository interfaces (e.g. `SessionRepository`, `ConfigRepository`) |
| `:core:domain` | `:core` | Cross-feature use cases (e.g. `GetCurrentUserUseCase`) |
| `:core:data` | `:core` | Shared data-source implementations, network client, DB driver setup |
| `:core:testing` | `:core` | Fake implementations, test fixtures, `FakeSessionRepository` etc. |
| `:core:ui` | `:core` | Design system, `AppTheme`, reusable components, `MviViewModel` base class |
| `:feature:auth` | `:feature` | Auth flow — `auth:model`, `auth:api`, `auth:domain`, `auth:data`, `auth:presenter`, `auth:ui` |
| `:feature:profile` | `:feature` | Profile flow — same 6-layer structure |

**Rules:**
- A `:feature` module must never depend on another `:feature` module directly.
  Cross-feature navigation goes through `:core:api` nav contracts (see Cross-Feature Navigation).
- `:core:ui` is the only place with Compose outside `:feature:*:ui` modules.
- `:core:testing` is a `testImplementation` / `commonTest` dependency only — never ship it in production.

```
app/
├── core/
│   ├── model/
│   ├── api/
│   ├── domain/
│   ├── data/
│   ├── testing/
│   └── ui/
└── feature/
    ├── auth/
    │   ├── model/
    │   ├── api/
    │   ├── domain/
    │   ├── data/
    │   ├── presenter/
    │   └── ui/
    └── profile/
        └── ...
```

---

## Use Case Pattern

Use cases live in `:domain` (feature or core). Each use case is a single class with
`operator fun invoke(...)` — one responsibility, directly invokable.

```kotlin
// :feature:auth:domain
class LoginUseCase(
    private val authRepository: AuthRepository,   // from :feature:auth:api
    private val sessionRepository: SessionRepository, // from :core:api
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val user = authRepository.login(email, password).getOrElse { return Result.failure(it) }
        sessionRepository.saveSession(user.token)
        return Result.success(user)
    }
}
```

**Rules:**
- One class, one public function (`invoke`). No utility use cases with multiple methods.
- Use cases depend on **interfaces** from `:api`, never on `:data` implementations.
- Use cases may call other use cases from `:core:domain` — never from sibling `:feature` domains.
- DI annotation (`@Single`, etc.) goes on the `:domain` module's Koin module, not on the use case class.

---

## Mapper Pattern

DTOs (data transfer objects from Ktor/SQLDelight) must not leak into `:domain` or `:presenter`.
Mappers live in `:data` and convert at the repository boundary.

```kotlin
// :feature:auth:data — DTO (internal to :data)
internal data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
)

// :feature:auth:data — mapper (internal)
internal fun UserDto.toDomain(): User = User(
    id = UserId(id),
    email = Email(email),
    displayName = displayName,
    avatarUrl = avatarUrl,
)

// :feature:auth:data — repository impl calls mapper at the boundary
internal class AuthRepositoryImpl(
    private val api: AuthApiService,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        runCatching { api.login(email, password).toDomain() }
}
```

**Rules:**
- Mappers are `internal` extension functions in `:data` — never public, never in `:domain`.
- Map **away from** the DTO before returning from any `Repository` function.
- SQLDelight-generated types (e.g. `SelectAllUsers`) are also DTOs — map them at the
  `DataSource` or `RepositoryImpl` boundary, not at the use-case level.

---

## Typed Domain Errors

Typed errors let callers distinguish and handle failure cases without parsing strings.
They live in `:model` (if shared) or `:feature:*:model` (if feature-specific).

```kotlin
// :core:model or :feature:auth:model
sealed class AuthError {
    data object InvalidCredentials : AuthError()
    data object AccountLocked : AuthError()
    data class NetworkError(val cause: Throwable) : AuthError()
    data class Unknown(val cause: Throwable) : AuthError()
}
```

Repository interface in `:api` returns `Result<T>` wrapping the typed error:

```kotlin
// :feature:auth:api
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    // throws AuthError subtypes captured in Result.failure(...)
}
```

The `:data` impl maps HTTP/network errors to the sealed type:

```kotlin
override suspend fun login(email: String, password: String): Result<User> = runCatching {
    api.login(email, password).toDomain()
}.mapFailure { cause ->
    when {
        cause is HttpException && cause.code == 401 -> AuthError.InvalidCredentials
        cause is HttpException && cause.code == 423 -> AuthError.AccountLocked
        cause is IOException -> AuthError.NetworkError(cause)
        else -> AuthError.Unknown(cause)
    }
}
```

The `:presenter` maps `AuthError` to a `UiError` for display — domain errors never flow
to the UI layer as-is (see `kotlin-multiplatform-mvi` for the `UiError` sealed type).

---

## Cross-Feature Navigation

`:feature` modules must not depend on each other. When feature A needs to navigate to
feature B, the nav contract is declared in `:core:api` (or the target feature's `:api`)
and both features depend only on that.

```kotlin
// :core:api — navigation contracts visible to all features
interface AppNavigator {
    fun navigateToProfile(userId: String)
    fun navigateToCheckout(cartId: String)
    fun navigateToHome()
}
```

The `:app` module provides the `AppNavigator` implementation. `NavController` is only
available after `rememberNavController()` inside a composable, so `AppNavigatorImpl` cannot
be constructed at Koin startup. The solution is a `NavControllerHolder` singleton that
`AppNavHost` populates at composition time:

```kotlin
// :app — holder bridges Koin DI time and Compose time
class NavControllerHolder {
    var current: NavController? = null
}

class AppNavigatorImpl(private val holder: NavControllerHolder) : AppNavigator {
    override fun navigateToProfile(userId: String) =
        holder.current?.navigate(ProfileRoute(userId))
    override fun navigateToCheckout(cartId: String) =
        holder.current?.navigate(CheckoutRoute(cartId))
    override fun navigateToHome() =
        holder.current?.navigate(HomeRoute) { popUpTo<HomeRoute> { inclusive = true } }
}

// :app — Koin DI module (constructs at startup, holder is empty until AppNavHost runs)
val appModule = module {
    single { NavControllerHolder() }
    single<AppNavigator> { AppNavigatorImpl(get()) }
}

// :app — AppNavHost sets the holder as soon as navController is ready
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val holder: NavControllerHolder = koinInject()

    DisposableEffect(navController) {
        holder.current = navController
        onDispose { holder.current = null }   // clear on teardown — prevents leaks
    }

    NavHost(navController = navController, startDestination = HomeRoute) {
        homeGraph()
        cartGraph()
        profileGraph()
    }
}
```

The feature `:presenter` injects `AppNavigator` and calls it directly from `handleIntent`:

```kotlin
// :feature:cart:presenter
class CartViewModel(
    private val navigator: AppNavigator,
    private val repo: CartRepository,
) : MviViewModel<CartContract.State, CartContract.Intent, CartContract.Effect>(...) {

    override suspend fun handleIntent(intent: CartContract.Intent) {
        when (intent) {
            CartContract.Intent.CheckoutClicked -> {
                val cartId = state.value.cartId
                navigator.navigateToCheckout(cartId)
            }
        }
    }
}
```

**Rules:**
- `AppNavigator` is the single cross-feature navigation surface — one interface, one impl, in `:app`.
- `AppNavigatorImpl` must be created inside `AppNavHost` after `rememberNavController()` — never as a Koin `single {}`.
- Within a feature graph, use navigation lambdas passed from NavHost — not `AppNavigator`.
- Never pass a `NavController` into a `:presenter` ViewModel — that creates a Compose dependency.
- Feature `:ui` modules expose `NavGraphBuilder` extensions that accept only lambdas or `AppNavigator`, never `NavController`.

---

## Internal Visibility Rules

Every declaration that is not part of the module's public surface should be `internal`.
The public surface of each layer:

| Layer | Public API |
|---|---|
| `:model` | All types — they are shared across every layer |
| `:api` | Repository interfaces, nav contracts |
| `:domain` | Use case classes (consumed by `:presenter`) |
| `:data` | Only the DI module (e.g., `val authDataModule`) — impl classes are `internal` |
| `:presenter` | `ViewModel` class, `UiState`, `UiIntent` sealed types |
| `:ui` | Top-level `@Composable` screen entry point only |

```kotlin
// :feature:auth:data — implementation is internal
internal class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
) : AuthRepository { ... }

// :feature:auth:data — only the module is public
val authDataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
```

The Gradle dependency graph enforces layer isolation. `internal` enforces encapsulation
within a layer's public API surface.

---

## Detekt Architecture Rules

Add to `detekt.yml` to fail the build when import-level violations are detected:

```yaml
libraries:
  rules:
    - name: 'NoPresentationInDomain'
      active: true
      includes: ['**/domain/**']
      excludes: []
      forbidden:
        - 'androidx.lifecycle.*'
        - 'androidx.compose.*'
        - '*.presenter.*'
        - '*.ui.*'

    - name: 'NoDataInUi'
      active: true
      includes: ['**/ui/**']
      excludes: []
      forbidden:
        - '*.data.*'
        - '*.domain.*'
        - 'io.ktor.*'
        - 'app.cash.sqldelight.*'

    - name: 'NoComposeInPresenter'
      active: true
      includes: ['**/presenter/**']
      excludes: []
      forbidden:
        - 'androidx.compose.*'
        - 'org.jetbrains.compose.*'
```

These rules complement the Gradle dependency graph — they catch cases where a developer
adds a compile dep and imports it directly rather than through a proper module boundary.

---

## Fitness Functions

Run these checks in CI to detect architecture drift:

```bash
# 1. Verify :presenter has no Compose dep in any feature module
grep -r "compose" feature/*/presenter/build.gradle.kts && echo "VIOLATION" || echo "OK"

# 2. Verify :ui does not depend on :data or :domain
grep -r "projects\.feature\.\w*\.\(data\|domain\)" feature/*/ui/build.gradle.kts && echo "VIOLATION" || echo "OK"

# 3. Detekt with architecture rules
./gradlew detekt
```

Wire these as CI gates via `kotlin-multiplatform-ci-github-actions`.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — creates the 6-layer module structure this skill governs
- `kotlin-multiplatform-presenter-module` — `:presenter` layer in depth: MVI contracts, ViewModel, Koin wiring
- `kotlin-multiplatform-unit-testing` — JVM-based ViewModel tests enabled by the `:presenter`/`:ui` split
- `kotlin-multiplatform-code-quality` — Ktlint + Detekt setup; Detekt is the enforcement mechanism here

---

## Common Anti-Patterns

- putting data classes in `:api` — they belong in `:model`; `:api` should be interfaces only
- adding Compose to `:presenter` — kills JVM testability; Compose belongs only in `:ui`
- `:ui` importing from `:data` directly — all state must route through `:presenter`
- `:domain` depending on `:data` — use cases should depend on repository *interfaces* from `:api`, not implementations
- skipping `internal` on `RepositoryImpl` — leaks the implementation type across modules
- one `:feature` depending on another `:feature` — cross-feature calls go through `:core:api` contracts
- leaking DTOs into `:domain` — map to domain types at the `:data` repository boundary
- using raw `String` for domain errors in multi-category failure scenarios — use a `sealed class` in `:model`
- passing `NavController` into a `:presenter` ViewModel — use `AppNavigator` from `:core:api` instead
- putting cross-feature shared types in a feature `:model` — shared types belong in `:core:model`
- binding `AppNavigatorImpl` as a Koin `single {}` — it holds a `NavController` which is only available after `rememberNavController()` inside the `AppNavHost` composable; create it with `remember(navController)` there instead
- passing `NavController` through a `NavGraphBuilder` extension — extensions receive lambdas or `AppNavigator`, never `NavController` directly

If a layer violation is hard to fix, it usually means a type belongs one layer lower (closer to `:model`).

---

## Output Style

When asked about architecture layers or module boundaries, respond in this order:
1. which layer the concept belongs to and why
2. the dependency rule it must satisfy
3. concrete file/class placement
4. how to enforce it (Gradle dep or Detekt rule)
5. the anti-pattern it avoids

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Fixed AppNavigator Koin binding: use NavControllerHolder singleton pattern so AppNavigatorImpl can be a Koin single{} while NavController is set by AppNavHost via DisposableEffect. |
| 2026-06-28 | Added "Layer Weight" section with ViewModel/use-case/data decision tables and thin feature pattern. Updated Recommendation First to lead with start-thin principle. Added: core vs feature split, use case pattern, mapper pattern, typed domain errors, cross-feature navigation. |
| 2026-06-18 | Initial release. |
