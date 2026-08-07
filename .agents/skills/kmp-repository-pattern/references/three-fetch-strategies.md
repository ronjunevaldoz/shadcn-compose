# The Three Fetch Strategies

Part of `kmp-repository-pattern`. Load this file when working on: the three fetch strategies.

---

### Network-First

Read from network, fall back to cache on failure. Use when data must be fresh (e.g., payment status):

```kotlin
override fun observeProducts(): Flow<List<Product>> = flow {
    // 1. Emit cached data immediately (non-blocking first response)
    val cached = local.getProducts()
    if (cached.isNotEmpty()) emit(cached.map { it.toDomain() })

    // 2. Fetch from network
    when (val result = remote.getProducts()) {
        is NetworkResult.Success -> {
            local.saveProducts(result.data.map { it.toEntity() })
            emit(result.data.map { it.toDomain() })
        }
        is NetworkResult.NetworkError -> {
            // Already emitted cache above — no-op, caller handles stale indicator elsewhere
        }
        is NetworkResult.HttpError -> throw Exception("HTTP ${result.code}")
    }
}
```

### Cache-First (Single Source of Truth)

The database is the single source of truth. Network writes to DB; UI observes DB only.
Use for most list/detail screens:

```kotlin
// In the repository — background refresh, UI always from local
override fun observeProducts(): Flow<List<Product>> {
    // Immediately return a Flow from local DB — UI subscribes and gets live updates
    return local.observeProducts().map { entities -> entities.map { it.toDomain() } }
}

// Called separately to trigger a refresh (e.g., pull-to-refresh, on screen open)
override suspend fun refreshProducts(): Result<Unit> {
    return when (val result = remote.getProducts()) {
        is NetworkResult.Success -> {
            local.replaceProducts(result.data.map { it.toEntity() })
            Result.success(Unit)
            // observeProducts() Flow above will emit automatically — SQLDelight invalidates the query
        }
        is NetworkResult.NetworkError -> Result.failure(result.exception)
        is NetworkResult.HttpError    -> Result.failure(Exception("HTTP ${result.code}"))
    }
}
```

The ViewModel calls `observeProducts()` once (to get the Flow) and `refreshProducts()`
on user pull-to-refresh or screen entry. SQLDelight's Flow automatically emits when the DB changes.

### Offline-First (Sync Queue)

Write operations are stored locally first and synced in background. Use for create/update/delete
in apps that must work without connectivity:

```kotlin
override suspend fun createNote(title: String, body: String): Result<Note> {
    // 1. Save locally with a temporary ID and "pending_sync" flag
    val tempId  = randomUUID()
    val entity  = NoteEntity(id = tempId, title = title, body = body, syncStatus = "pending")
    local.insertNote(entity)

    // 2. Return immediately — UI sees the new note without waiting for network
    val domain = entity.toDomain()

    // 3. Attempt network sync in background (fire-and-forget, retried by SyncWorker)
    syncQueue.enqueue(SyncOperation.CreateNote(tempId, title, body))

    return Result.success(domain)
}
```

---

