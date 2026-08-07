---
name: kmp-repository-pattern
description: >
  Repository pattern in Kotlin Multiplatform — the data layer between domain logic
  and storage/network. Covers: repository interface in :feature:x:api, implementation
  in :feature:x:data, single source of truth via SQLDelight Flow, the three fetch
  strategies (network-first, cache-first, offline-first), NetworkResult-to-domain
  mapping at the repository boundary, optimistic updates, and the common mistakes
  of leaking network types into domain and skipping local cache. Requires
  kmp-network-layer and kmp-sqldelight-setup.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-13'
  keywords:
    - repository pattern
    - data layer
    - single source of truth
    - SQLDelight Flow
    - network-first
    - cache-first
    - offline-first
    - optimistic update
    - domain mapping
    - NetworkResult
    - Kotlin Multiplatform
    - KMP
    - clean architecture
    - data source
    - local cache
    - in-memory repository
    - no backend yet
    - backend not ready
---

## When to Use This Skill

Use when you need to:
- Connect the network layer (Ktor) and local database (SQLDelight) into a unified data source
- Implement offline-capable features
- Ensure the UI always reflects local truth, not raw network responses
- Map network/database types to domain models at the correct boundary
- Implement optimistic updates (update UI immediately, sync in background)

**Requires:** `kmp-network-layer`, `kmp-sqldelight-setup`.

**Trigger keywords:** repository pattern, data layer, offline-first, cache-first, network-first,
single source of truth, local cache, domain mapping, repository implementation, data source,
optimistic update, sync strategy, data redundancy, content management, data handling,
cache strategy, data consistency, data sync, fetch strategy, content duplication,
in-memory repository, no backend yet, backend not ready, mock repository.

**Freshness rule:** Ktor and SQLDelight APIs change — recheck both before using this skill
with a version upgrade, and verify the fetch-strategy examples against the current driver APIs.

---

## Recommendation First

Default to **cache-first with SQLDelight as the single source of truth**.

Why:
- the UI observes a SQLDelight `Flow` — it updates automatically when the DB changes
- network failures don't affect the user if cached data is available
- the repository owns the mapping boundary: DTOs and entities never leak into domain code

Use network-first only when staleness is unacceptable (e.g., payment status, live inventory).
Use network-only for write operations or when there is no local persistence.

---

## Where the Repository Lives

```
:feature:auth:api        ← AuthRepository interface lives here
:feature:auth:domain     ← use cases call AuthRepository (via :api)
:feature:auth:data       ← AuthRepositoryImpl lives here
  ├── remote/AuthRemoteDataSource.kt   (Ktor calls)
  └── local/AuthLocalDataSource.kt     (SQLDelight queries)
```

The interface is in `:api` so both `:domain` and `:ui` can depend on it without depending
on `:data`. The implementation in `:data` depends on `:api`, `:core:network`, and
`:core:database`. Neither `:domain` nor `:ui` ever know `:data` exists.

---

## The Repository Interface

The interface belongs in `:feature:x:api` and speaks **domain types only** — no DTOs,
no database entities, no `NetworkResult` wrappers:

```kotlin
// :feature:auth:api/src/commonMain/kotlin/GROUP_ID/feature/auth/api/AuthRepository.kt
package GROUP_ID.feature.auth.api

import GROUP_ID.feature.auth.api.model.User
import GROUP_ID.feature.auth.api.model.AuthError
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Emits the current user from local cache, then updates when remote data changes.
     * Emits null if not authenticated.
     */
    fun observeCurrentUser(): Flow<User?>

    /**
     * Authenticates and persists the session locally.
     * Returns Result — callers decide how to handle failure.
     */
    suspend fun login(email: String, password: String): Result<User>

    suspend fun logout()

    suspend fun refreshSession(): Result<User>
}
```

**Rules for the interface:**
- Return types are domain models (`User`) or `Result<DomainType>` — never DTOs or DB entities
- Reactive reads return `Flow<T>` — callers observe, not poll
- Write operations are `suspend fun` returning `Result<T>`
- No network or database types leak through the interface boundary

---

## Domain Models

Domain models in `:feature:x:api` are clean data classes — no serialization annotations,
no SQLDelight-generated types, no platform dependencies:

```kotlin
// :feature:auth:api/src/commonMain/kotlin/GROUP_ID/feature/auth/api/model/User.kt
package GROUP_ID.feature.auth.api.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
)

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object NetworkUnavailable : AuthError
    data class Unknown(val message: String) : AuthError
}
```

---

## The Repository Implementation

Full content: `references/repository-implementation.md`.

## Type Mapping: The Mapper Pattern

Keep mapping functions in a `mapper/` package in `:data`. Never let the DTO or entity
escape the `:data` module boundary.

```kotlin
// :feature:auth:data/src/commonMain/kotlin/GROUP_ID/feature/auth/data/mapper/UserMapper.kt
package GROUP_ID.feature.auth.data.mapper

import GROUP_ID.core.database.UserEntity          // SQLDelight generated
import GROUP_ID.feature.auth.api.model.User
import GROUP_ID.feature.auth.data.remote.dto.UserDto   // Ktor DTO

// Network DTO → domain
fun UserDto.toDomain() = User(
    id          = id,
    email       = email,
    displayName = displayName ?: email.substringBefore("@"),
    avatarUrl   = avatarUrl,
)

// Domain → DB entity
fun User.toEntity() = UserEntity(
    id           = id,
    email        = email,
    display_name = displayName,
    avatar_url   = avatarUrl,
)

// DB entity → domain
fun UserEntity.toDomain() = User(
    id          = id,
    email       = email,
    displayName = display_name,
    avatarUrl   = avatar_url,
)
```

**Mapping direction:** `NetworkDto → Domain ← DbEntity`. The domain model is the center;
both data sources map into it. The domain model never knows about either source.

---

## The Three Fetch Strategies

Full content: `references/three-fetch-strategies.md`.

## Data Sources

Split remote and local into separate classes to keep the repository readable:

```kotlin
// Remote — only Ktor calls, returns NetworkResult<Dto>
class AuthRemoteDataSource(private val client: HttpClient) {
    suspend fun login(email: String, password: String): NetworkResult<UserDto> =
        client.safeRequest { post("/auth/login") { setBody(LoginRequest(email, password)) } }

    suspend fun logout(): NetworkResult<Unit> =
        client.safeRequest { post("/auth/logout") }
}

// Local — only SQLDelight queries, returns entities or Flow<Entity>
class AuthLocalDataSource(private val db: AppDatabase) {
    fun observeUser(): Flow<UserEntity?> =
        db.userQueries.selectCurrent().asFlow().mapToOneOrNull()

    suspend fun saveUser(entity: UserEntity) =
        db.userQueries.upsert(entity)

    suspend fun clearUser() =
        db.userQueries.deleteAll()
}
```

---

## Optimistic Updates

Update local state immediately; roll back on network failure:

```kotlin
override suspend fun toggleFavorite(productId: String): Result<Unit> {
    // 1. Read current state
    val current = local.getProduct(productId) ?: return Result.failure(Exception("Not found"))

    // 2. Flip locally — UI sees the change instantly
    local.updateProduct(current.copy(isFavorite = !current.isFavorite))

    // 3. Sync to network
    return when (val result = remote.setFavorite(productId, !current.isFavorite)) {
        is NetworkResult.Success -> Result.success(Unit)
        else -> {
            // 4. Roll back on failure
            local.updateProduct(current)
            Result.failure(Exception("Sync failed"))
        }
    }
}
```

---

## Koin Wiring

```kotlin
// :feature:auth:data/src/commonMain/kotlin/.../di/AuthDataModule.kt
val authDataModule = module {
    single { AuthRemoteDataSource(get()) }        // get() resolves HttpClient from NetworkModule
    single { AuthLocalDataSource(get()) }         // get() resolves AppDatabase from DatabaseModule
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
```

Module loading order: `DatabaseModule → NetworkModule → AuthDataModule → AuthDomainModule`

---

## Common Mistakes

**1. Leaking NetworkResult or DTOs through the repository interface**
The ViewModel should never see a `UserDto` or `NetworkResult`. The repository boundary
is where these types are translated into domain models and `Result<T>`.

**2. No local cache — direct pass-through repository**
```kotlin
// ❌ No value added — this is just a renamed Ktor call
override suspend fun getUser(id: String): User = remote.getUser(id).toDomain()
```
A repository without local storage provides no resilience to network failures, no offline
support, and forces the ViewModel to handle network errors directly.

**3. Calling `refreshProducts()` inside `observeProducts()`**
The Flow-based cache-first pattern works because `observeProducts()` is purely reactive —
it never triggers a network call. Callers trigger `refreshProducts()` separately.
Mixing them causes the refresh to fire on every new subscriber.

**4. Mutable domain models with database-internal IDs**
Domain models should not expose auto-increment SQLite IDs. Use UUIDs as primary keys
so the domain layer is decoupled from the database schema.

---

## Verification

1. `observeProducts()` emits cached data before any network call completes
2. `refreshProducts()` failure leaves existing cached data intact
3. SQLDelight Flow emits automatically after `replaceProducts()` — no manual invalidation
4. No `UserDto`, `UserEntity`, or `NetworkResult` type appears in `:feature:auth:api` or `:domain`
5. `AuthRepositoryImpl` can be replaced with a fake implementing `AuthRepository` in tests
6. Unit test: fake remote returns error → cache-first Flow still emits cached data

---

## Testing

```kotlin
class FakeUserRepository : UserRepository {
    val stored = mutableListOf<User>()
    var getResult: NetworkResult<User> = NetworkResult.Success(User.default())
    private val _flow = MutableStateFlow(stored.toList())

    override suspend fun getById(id: UserId): NetworkResult<User> = getResult
    override suspend fun save(user: User) { stored += user; _flow.value = stored.toList() }
    override fun observeAll(): Flow<List<User>> = _flow.asStateFlow()
}

@Test fun `getById returns success result`() = runTest {
    val repo = FakeUserRepository()
    val result = repo.getById(UserId("1"))
    assertTrue(result is NetworkResult.Success)
}

@Test fun `getById propagates error`() = runTest {
    val repo = FakeUserRepository().apply {
        getResult = NetworkResult.Error(404, "not found")
    }
    val result = repo.getById(UserId("x"))
    assertTrue(result is NetworkResult.Error)
    assertEquals(404, (result as NetworkResult.Error).code)
}

@Test fun `save emits updated list via flow`() = runTest {
    val repo = FakeUserRepository()
    repo.observeAll().test {
        assertEquals(emptyList(), awaitItem())
        repo.save(User(id = UserId("1"), name = "Alice"))
        assertEquals(1, awaitItem().size)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Common Anti-Patterns

- calling the repository from a composable directly instead of through a ViewModel — bypasses the MVI state machine. Mechanically caught by `kmp-audit`'s `_detect_repository_in_composable` when the repository is injected *and* its Flow collected in the same composable (forwarding it to a real ViewModel/state-holder constructor is fine — only the direct Flow-read is flagged)
- leaking `NetworkResult`, `UserDto`, or `UserEntity` types into `:domain` or `:ui` — they belong in `:data` only
- implementing fetch logic in a ViewModel instead of a repository — ViewModels coordinate, repositories fetch
- returning `null` from a repository method when a typed error sealed class is clearer
- skipping the mapper layer and mapping inside route handlers or composables
- naming a bring-up-phase in-memory repository `Mock*`/`Fake*` — those names signal "test-only, safe to delete," but an in-memory repository runs the real app; use `InMemory*` instead
- leaving an in-memory repository wired past the point the real backend exists — nothing persists or syncs, a silent data-loss bug once real users hit it

If domain or UI code is importing `:data` types directly, the mapper boundary is missing.

---

## References

Full implementation content lives in `references/*.md`: `repository-implementation`,
`three-fetch-strategies`. Load the specific file named in the pointer under its matching
heading above, not all of them.

---

## Related Skills

- `kmp-network-layer` — provides `HttpClient` and `safeRequest` that repository data sources call
- `kmp-sqldelight-setup` — the offline cache layer in cache-first and offline-first strategies
- `kmp-dependency-injection` — Koin binding for repository interfaces to their implementations
- `kmp-unit-testing` — fake repository pattern for testing ViewModels without real data sources

---

## Output Style

When asked about the repository pattern or data layer, respond in this order:
1. recommendation (fetch strategy: cache-first, network-first, or network-only)
2. project structure (`:data` layer with interface in `:api`)
3. code snippet (one repository method showing the chosen strategy)
4. why that strategy fits the use case
5. main alternative (different fetch strategy, or direct data source)

Keep the snippet to one repository method. Map to the user's actual entity and domain names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split "The Repository Implementation" and "The Three Fetch Strategies" out of SKILL.md into `references/*.md`, leaving pointer stubs plus a new References section. SKILL.md drops from 633 to 402 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as the other 12 skills fixed alongside it (KI-008). |
| 2026-08-03 | A consumer-project report confirmed a real gap: this skill's own long-standing "call the repository from a composable directly" anti-pattern had no mechanical check — an audit false positive on an unrelated finding (`viewmodel in viewmodel`, since fixed) surfaced that the *actual* smell in the reported code (repository injected and Flow-collected directly inside a `@Composable`) went uncaught. Added `kmp-audit`'s `_detect_repository_in_composable` (MEDIUM) and cross-referenced it here. |
| 2026-07-13 | Real gap closed: no rule existed for starting a feature/project before the real backend exists — only a generic test/preview "mock DI wiring" example, and `/kmp-new-project`'s data-layer step assumed a real backend from day one. Added "In-memory repository (no backend yet)" with a full `InMemoryAuthRepository` example, a swap-in DI pattern, and a naming rule (`InMemory*`, never `Mock*`/`Fake*` — those names mean test-only). Wired into `/kmp-new-project`'s sprint implementation step and `kmp-migration`'s Tier 3 row for existing projects mid-migration. 2 new anti-patterns (wrong naming, leaving it wired past backend-ready). |
| 2026-06-06 | Initial release. |
