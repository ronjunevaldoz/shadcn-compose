---
name: kotlin-multiplatform-offline-first
description: >
  Offline-first architecture for Kotlin Multiplatform apps — local-first reads,
  background sync, optimistic updates, conflict resolution, and sync state tracking.
  Covers: single-source-of-truth via SQLDelight, a SyncManager pattern using
  WorkManager/BGTaskScheduler, optimistic mutation with rollback, and a SyncState
  sealed class surfaced to the UI via MVI. Does NOT cover FCM-triggered sync
  (see push-notifications) or RPC streaming (see kotlin-rpc).
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - offline first
    - offline-first
    - local first
    - sync
    - optimistic update
    - conflict resolution
    - background sync
    - single source of truth
    - cache
    - network-cache-flow
---

## When to Use This Skill

Use this skill when:
- The app must show data even with no network connection
- The user should see their own changes immediately, before the server confirms them
- A background job needs to push local changes to the server when connectivity returns
- You need to surface sync status (syncing, synced, error, conflict) in the UI

Do NOT use this skill when:
- The app is network-only and an error state is acceptable with no connectivity
- Real-time streaming is the requirement — use `kotlin-rpc` or WebSockets instead

> **Opt-in skill — only when explicitly requested.** Do NOT activate for generic data terms
> like "cache", "data consistency", "single source of truth", "sync", or "data redundancy" —
> those belong to `repository-pattern` (data layer + fetch strategy) or `sqldelight-setup`
> (local persistence). Offline-first adds the heavier `SyncManager` + `WorkManager`/`BGTaskScheduler`
> machinery and must be **named explicitly** before it is selected.

**Trigger keywords:** offline first, offline-first, local first, optimistic update,
conflict resolution, conflict handling, background sync, background data sync,
cache-then-network, SyncManager, SyncState.

**Freshness rule:** `WorkManager` and `BGTaskScheduler` APIs change — recheck
[WorkManager releases](https://developer.android.com/jetpack/androidx/releases/work)
and [BGTaskScheduler docs](https://developer.apple.com/documentation/backgroundtasks)
before implementing. Also verify the SQLDelight version in `libs.versions.toml` matches
the one in the `kotlin-multiplatform-sqldelight-setup` skill.

---

## Recommendation First

Default to **local-first reads with background sync**:

1. `:data` always reads from the local SQLDelight database — never directly from network
2. A `SyncManager` fetches from the network and writes to the database; the UI observes
   the database `Flow` and gets the update automatically
3. Mutations are written locally first (optimistic), then synced in the background

```kotlin
// ✅ Correct — read from DB, sync in background
class UserRepositoryImpl(
    private val db: AppDatabase,
    private val api: UserApiService,
    private val sync: SyncManager,
) : UserRepository {
    override fun observeUser(id: UserId): Flow<User> =
        db.userQueries.selectById(id.value).asFlow().mapToOne(Dispatchers.IO)
            .onStart { sync.requestSync(SyncTag.USER) }

    override suspend fun updateName(id: UserId, name: String) {
        db.userQueries.updateName(name = name, id = id.value)   // optimistic
        sync.requestSync(SyncTag.USER)                           // background push
    }
}
```

---

## Sync State

Track sync status as a first-class value in `commonMain`:

```kotlin
// :model
sealed class SyncState {
    data object Idle     : SyncState()
    data object Syncing  : SyncState()
    data object Synced   : SyncState()
    data class  Error(val message: String, val retryable: Boolean) : SyncState()
    data class  Conflict(val localVersion: Any, val remoteVersion: Any) : SyncState()
}

enum class SyncTag(val id: String) {
    USER("sync:user"),
    FEED("sync:feed"),
    ALL("sync:all"),
}
```

Expose it through the ViewModel's `UiState`:

```kotlin
data class ProfileUiState(
    val user: User? = null,
    val syncState: SyncState = SyncState.Idle,
    val isLoading: Boolean = false,
)
```

The UI shows a subtle sync indicator — never block the user waiting for network.

---

## SyncManager Pattern

```kotlin
// :api
interface SyncManager {
    fun requestSync(tag: SyncTag)
    fun observeSyncState(tag: SyncTag): Flow<SyncState>
}

// :data — Android (WorkManager)
class AndroidSyncManager(private val workManager: WorkManager) : SyncManager {
    override fun requestSync(tag: SyncTag) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .addTag(tag.id)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(tag.id, ExistingWorkPolicy.KEEP, request)
    }

    override fun observeSyncState(tag: SyncTag): Flow<SyncState> =
        workManager.getWorkInfosByTagFlow(tag.id).map { infos ->
            when (infos.firstOrNull()?.state) {
                WorkInfo.State.RUNNING    -> SyncState.Syncing
                WorkInfo.State.SUCCEEDED  -> SyncState.Synced
                WorkInfo.State.FAILED     -> SyncState.Error("Sync failed", retryable = true)
                else                      -> SyncState.Idle
            }
        }
}

// SyncWorker
class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        // Fetch from network and write to DB
        val users = get<UserApiService>().getUsers()
        get<AppDatabase>().userQueries.transaction {
            users.forEach { get<AppDatabase>().userQueries.upsert(it.toEntity()) }
        }
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}
```

---

## Optimistic Updates with Rollback

```kotlin
override suspend fun updateName(id: UserId, name: String) {
    val previous = db.userQueries.selectById(id.value).executeAsOneOrNull()
    db.userQueries.updateName(name = name, id = id.value)   // optimistic write
    try {
        api.updateUser(id, UpdateUserRequest(name = name))  // confirm with server
    } catch (e: Exception) {
        // Roll back on network failure
        previous?.let { db.userQueries.updateName(name = it.name, id = id.value) }
        throw e
    }
}
```

---

## Conflict Resolution

When the server returns a version that differs from local, surface the conflict to the user:

```kotlin
suspend fun syncUser(id: UserId) {
    val local  = db.userQueries.selectById(id.value).executeAsOneOrNull() ?: return
    val remote = api.getUser(id)
    if (remote.updatedAt > local.updatedAt) {
        db.userQueries.upsert(remote.toEntity())   // server wins on time
    } else if (remote.updatedAt < local.updatedAt) {
        // local is newer — push to server
        api.updateUser(id, local.toRequest())
    }
    // Equal timestamps → no conflict, skip
}
```

For user-facing conflict (e.g. both sides edited the same field), emit `SyncState.Conflict`
and let the user decide — never silently overwrite user input.

---

## Koin Wiring

```kotlin
// androidMain platform module
single<SyncManager> { AndroidSyncManager(get()) }
single { WorkManager.getInstance(androidContext()) }

// iosMain platform module (BGTaskScheduler — see workmanager skill)
single<SyncManager> { IosSyncManager(get()) }

// commonMain
single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
```

---

## Testing

```kotlin
class FakeSyncManager : SyncManager {
    val requested = mutableListOf<SyncTag>()
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)

    override fun requestSync(tag: SyncTag) { requested += tag }
    override fun observeSyncState(tag: SyncTag): Flow<SyncState> = _state

    fun emitSyncing() { _state.value = SyncState.Syncing }
    fun emitSynced()  { _state.value = SyncState.Synced }
    fun emitError(msg: String) { _state.value = SyncState.Error(msg, retryable = true) }
}

@Test fun `updateName writes locally then requests sync`() = runTest {
    val db = AppDatabase(testDriver())
    db.userQueries.insert(id = "1", name = "Alice")
    val sync = FakeSyncManager()
    val repo = UserRepositoryImpl(db, FakeUserApiService(), sync)

    repo.updateName(UserId("1"), "Bob")

    assertEquals("Bob", db.userQueries.selectById("1").executeAsOne().name)
    assertEquals(listOf(SyncTag.USER), sync.requested)
}

@Test fun `optimistic update rolls back on network error`() = runTest {
    val db = AppDatabase(testDriver())
    db.userQueries.insert(id = "1", name = "Alice")
    val api = FakeUserApiService().apply { throwOnUpdate = true }
    val repo = UserRepositoryImpl(db, api, FakeSyncManager())

    runCatching { repo.updateName(UserId("1"), "Bob") }

    assertEquals("Alice", db.userQueries.selectById("1").executeAsOne().name)
}

@Test fun `sync state flows to viewmodel ui state`() = runTest {
    val sync = FakeSyncManager()
    val vm = ProfileViewModel(FakeUserRepository(), sync)
    vm.state.test {
        assertEquals(SyncState.Idle, awaitItem().syncState)
        sync.emitSyncing()
        assertEquals(SyncState.Syncing, awaitItem().syncState)
        sync.emitSynced()
        assertEquals(SyncState.Synced, awaitItem().syncState)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Common Anti-Patterns

- **Reading from the network in the Repository's `observe` function** — the UI flow will
  stall waiting for network. Always read from the local DB; trigger network sync separately.
- **Optimistic update without rollback** — if the network call fails and there's no rollback,
  the local state diverges from the server permanently. Always store the previous value before
  the optimistic write.
- **Blocking the UI on sync completion** — `requestSync` should fire-and-forget. Never `await`
  on sync inside a composable or ViewModel's `init` block.
- **One SyncWorker for all data types** — a single slow sync blocks faster syncs. Use separate
  `SyncTag` entries and enqueue them independently.
- **Conflict resolution with silent server-wins** — silently overwriting user edits breaks trust.
  Surface `SyncState.Conflict` and let the user choose.

---

## Related Skills

- `kotlin-multiplatform-sqldelight-setup` — local database; required for single-source-of-truth reads
- `kotlin-multiplatform-repository-pattern` — the Repository interface that SyncManager plugs into
- `kotlin-multiplatform-workmanager` — platform-specific background scheduling for Android/iOS sync
- `kotlin-multiplatform-network-layer` — network calls the SyncWorker makes to the server
- `kotlin-multiplatform-mvi` — SyncState surfaces through UiState; MVI pattern keeps it clean

---

## Output Style

When asked about offline-first or sync patterns, respond in this order:
1. recommendation (local-first read + background sync manager)
2. the SyncState sealed class and where it lives
3. repository snippet showing optimistic write + sync trigger
4. rollback pattern for the error case
5. testing approach (FakeSyncManager)

Lead with the data flow direction: DB → UI (read), UI → DB → network (write).
Never suggest reading from the network in the observe path.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
