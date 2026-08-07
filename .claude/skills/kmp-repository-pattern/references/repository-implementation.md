# The Repository Implementation

Part of `kmp-repository-pattern`. Load this file when working on: the repository implementation.

---

The implementation in `:feature:x:data` wires together remote and local data sources,
maps between types, and owns the fetch strategy:

```kotlin
// :feature:auth:data/src/commonMain/kotlin/GROUP_ID/feature/auth/data/AuthRepositoryImpl.kt
package GROUP_ID.feature.auth.data

import GROUP_ID.core.network.NetworkResult
import GROUP_ID.feature.auth.api.AuthRepository
import GROUP_ID.feature.auth.api.model.User
import GROUP_ID.feature.auth.data.local.AuthLocalDataSource
import GROUP_ID.feature.auth.data.remote.AuthRemoteDataSource
import GROUP_ID.feature.auth.data.mapper.toDomain
import GROUP_ID.feature.auth.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
) : AuthRepository {

    // Single source of truth — UI observes the local DB, not the network
    override fun observeCurrentUser(): Flow<User?> =
        local.observeUser().map { entity -> entity?.toDomain() }

    override suspend fun login(email: String, password: String): Result<User> {
        return when (val result = remote.login(email, password)) {
            is NetworkResult.Success -> {
                val user = result.data.toDomain()
                local.saveUser(user.toEntity())     // persist to local first
                Result.success(user)
            }
            is NetworkResult.HttpError -> Result.failure(
                Exception(result.message ?: "HTTP ${result.code}")
            )
            is NetworkResult.NetworkError -> Result.failure(result.exception)
        }
    }

    override suspend fun logout() {
        local.clearUser()
        // Fire-and-forget remote logout — local clear is what matters for UX
        runCatching { remote.logout() }
    }

    override suspend fun refreshSession(): Result<User> {
        return when (val result = remote.refreshSession()) {
            is NetworkResult.Success -> {
                val user = result.data.toDomain()
                local.saveUser(user.toEntity())
                Result.success(user)
            }
            is NetworkResult.HttpError,
            is NetworkResult.NetworkError -> Result.failure(Exception("Refresh failed"))
        }
    }
}
```

### RPC client boundary pattern

If the feature uses RPC or a dedicated HTTP client, keep the client wrapper in `:data`
and make the call site a private `service()` function, not a cached property. That keeps
auth headers fresh and the boundary explicit.

```kotlin
class BookingRpcClient(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val userSession: UserSession,
) : BookingRequestRepository {

    private fun service(): BookingRpcService =
        httpClient.rpc("$serverUrl/rpc/booking") {
            rpcConfig { serialization { json() } }
            bearerAuth(userSession)
        }.withService()
}
```

### In-memory repository (no backend yet)

Use when scaffolding a new feature — or a whole new project — before the real API exists:
implement the repository interface with an in-memory store instead of `AuthRepositoryImpl`'s
remote+local wiring. Same public contract, so the ViewModel and UI never know the difference,
and swapping in the real implementation later touches only the Koin module, not any caller.

```kotlin
// :feature:auth:data/src/commonMain/kotlin/GROUP_ID/feature/auth/data/InMemoryAuthRepository.kt
package GROUP_ID.feature.auth.data

import GROUP_ID.feature.auth.api.AuthRepository
import GROUP_ID.feature.auth.api.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stands in for [AuthRepositoryImpl] until the real backend exists. Same interface, no
 * network/database dependency — the app runs and demos end to end before the API does.
 * Swap-in point: flip the `single<AuthRepository>` binding in AuthDataModule to
 * AuthRepositoryImpl once the backend is ready; no caller changes needed.
 */
class InMemoryAuthRepository : AuthRepository {
    private val currentUser = MutableStateFlow<User?>(null)

    override fun observeCurrentUser(): Flow<User?> = currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        val user = User(id = "local-1", email = email, displayName = email.substringBefore("@"), avatarUrl = null)
        currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        currentUser.value = null
    }

    override suspend fun refreshSession(): Result<User> =
        currentUser.value?.let { Result.success(it) } ?: Result.failure(Exception("Not authenticated"))
}
```

```kotlin
// Swap point — one line, no caller touches this
val authDataModule = module {
    single { AuthRemoteDataSource(get()) }
    single { AuthLocalDataSource(get()) }
    single<AuthRepository> {
        if (BuildConfig.BACKEND_READY) AuthRepositoryImpl(get(), get())
        else InMemoryAuthRepository()
    }
}
```

**Naming**: `InMemory<Feature>Repository`, not `Mock*`/`Fake*` — those names are reserved for
test doubles (see `kmp-unit-testing`'s fake-over-mock rule and the `Testing`
section below). An in-memory repository runs the real app for real users during bring-up; a
fake only ever runs inside a test. Same shape, different purpose — keep the names distinct so
a real in-memory implementation never gets deleted by someone cleaning up "test-only" code.

**Swap-out discipline**: gate the swap behind a single flag or build config value (not a
scattered `if` in each repository), and track it as an open item — an in-memory repository
left wired past the point the backend exists is a silent data-loss bug (nothing persists,
nothing syncs), not a cosmetic one.

### Mock-vs-real DI wiring (tests/previews)

The same branch-in-the-module technique also applies to test/preview-only mocks — keep the
branch in the DI module, not the ViewModel or UI:

```kotlin
val bookingDataModule = module {
    single<BookingRequestRepository> {
        if (AuthConfig.USE_MOCK_AUTH) MockBookingRequestRepository(rideRepository = get())
        else BookingRpcClient(httpClient = get(named("rpc")), serverUrl = AuthConfig.SERVER_URL, userSession = get())
    }
}
```

This keeps tests and previews simple: the feature code depends on the repository interface,
and the module decides whether that interface is backed by a fake or a real remote source.

---

