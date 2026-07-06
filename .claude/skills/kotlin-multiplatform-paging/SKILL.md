---
name: kotlin-multiplatform-paging
description: >-
  Paging 3 in Kotlin Multiplatform — PagingSource, Pager, PagingData, and
  collectAsLazyPagingItems for cursor-based and offset-based list loading.
  Covers layer placement (PagingSource in :data, Pager in :presenter, LazyPagingItems
  in :ui), load-state handling, RemoteMediator for network+DB, Koin wiring, and
  testing PagingSource in isolation. Keeps PagingData out of UiState.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - paging
    - Paging 3
    - PagingSource
    - Pager
    - PagingData
    - LazyPagingItems
    - collectAsLazyPagingItems
    - cursor pagination
    - offset pagination
    - infinite scroll
    - load more
    - RemoteMediator
    - LoadState
    - pagingSourceFactory
    - cachedIn
---

## When to Use This Skill

Use when:
- A screen needs to load a list in pages from a remote API or local database
- Implementing infinite scroll or "load more" behaviour
- Using cursor-based pagination (next-page token from API) or offset-based (page number)
- Combining network fetch with SQLDelight local cache via `RemoteMediator`
- Migrating a `List<T>` in `UiState` to a paged source

**Trigger keywords:** paging, Paging 3, PagingSource, Pager, PagingData, LazyPagingItems,
collectAsLazyPagingItems, infinite scroll, load more, paginate, next page, cursor pagination,
offset pagination, page token, RemoteMediator, load state, append loading, paged list KMP.

**Freshness rule:** `androidx.paging:paging-common` became multiplatform in 3.3.x —
verify the version in `libs.versions.toml` supports KMP before using it on non-Android
targets. Check the [AndroidX Paging release notes](https://developer.android.com/jetpack/androidx/releases/paging)
when upgrading. `paging-compose` lags one version behind `paging-common` — keep them in sync.

---

## Recommendation First

Put the `Pager` in the `:presenter` layer (ViewModel), not in `:ui`. Call `.cachedIn(viewModelScope)` exactly once — this prevents re-fetching on every recomposition and survives configuration changes.

**Never put `PagingData<T>` inside a `UiState` data class.** `PagingData` is not a plain value — it cannot be copied, compared, or serialized. Keep it as a separate `Flow<PagingData<T>>` property on the ViewModel alongside the regular `StateFlow<UiState>`.

---

## `libs.versions.toml` additions

```toml
[versions]
paging = "3.3.5"

[libraries]
paging-common  = { module = "androidx.paging:paging-common",  version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
```

In `commonMain` dependencies:
```kotlin
implementation(libs.paging.common)
```

In `androidMain` / `:ui` module:
```kotlin
implementation(libs.paging.compose)
```

---

## Layer placement

```
:api       — FooRepository interface exposes pagingSource(): PagingSource<Key, FooItem>
:data      — FooPagingSource implements PagingSource; FooRepositoryImpl returns it
:presenter — Pager(...) created in ViewModel; .flow.cachedIn(viewModelScope)
:ui        — collectAsLazyPagingItems(); LazyColumn with load-state slots
```

---

## Cursor-based PagingSource (API returns a next-page token)

```kotlin
// :data — FooPagingSource.kt
class FooPagingSource(
    private val api: FooApi,
) : PagingSource<String, FooItem>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FooItem> {
        return try {
            val response = api.getItems(
                cursor    = params.key,      // null on first load
                pageSize  = params.loadSize,
            )
            LoadResult.Page(
                data    = response.items,
                prevKey = null,              // cursor-based: no backwards paging
                nextKey = response.nextCursor?.takeIf { it.isNotBlank() },
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // cursor-based: no meaningful refresh key
    override fun getRefreshKey(state: PagingState<String, FooItem>): String? = null
}
```

## Offset-based PagingSource (API uses page numbers)

```kotlin
// :data — FooPagingSource.kt
private const val STARTING_PAGE = 1

class FooPagingSource(
    private val api: FooApi,
) : PagingSource<Int, FooItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FooItem> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = api.getItems(page = page, pageSize = params.loadSize)
            LoadResult.Page(
                data    = response.items,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FooItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
```

---

## Repository interface and implementation

```kotlin
// :api — FooRepository.kt
interface FooRepository {
    fun pagingSource(): PagingSource<String, FooItem>  // or Int for offset
}

// :data — FooRepositoryImpl.kt
class FooRepositoryImpl(private val api: FooApi) : FooRepository {
    override fun pagingSource() = FooPagingSource(api)
}
```

---

## ViewModel — Pager lives here

```kotlin
// :presenter — FooViewModel.kt
class FooViewModel(private val repository: FooRepository) : ViewModel() {

    // Regular MVI state — filters, loading indicators, error banners
    private val _state = MutableStateFlow(FooContract.State())
    val state: StateFlow<FooContract.State> = _state.asStateFlow()

    // Paged data — separate from UiState, never put PagingData inside State
    val items: Flow<PagingData<FooItem>> = Pager(
        config = PagingConfig(
            pageSize         = 20,
            enablePlaceholders = false,
            prefetchDistance = 3,
        ),
        pagingSourceFactory = { repository.pagingSource() }
    ).flow.cachedIn(viewModelScope)   // ← required; prevents re-fetch on recomposition

    fun onIntent(intent: FooContract.Intent) {
        when (intent) {
            is FooContract.Intent.Refresh -> _state.update { it.copy(isRefreshing = true) }
        }
    }
}
```

---

## UI — `collectAsLazyPagingItems` + load states

```kotlin
// :ui — FooScreen.kt
@Composable
fun FooScreen(viewModel: FooViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = viewModel.items.collectAsLazyPagingItems()
    FooContent(state = state, items = items, onIntent = viewModel::onIntent)
}

@Composable
fun FooContent(
    state: FooContract.State,
    items: LazyPagingItems<FooItem>,
    onIntent: (FooContract.Intent) -> Unit,
) {
    AppScaffold(
        topBar = { AppTopAppBar(title = "Foo") }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Initial full-screen load
            when (val refresh = items.loadState.refresh) {
                is LoadState.Loading -> FullScreenLoader()
                is LoadState.Error   -> FullScreenError(
                    message = refresh.error.localizedMessage.orEmpty(),
                    onRetry = { items.retry() }
                )
                is LoadState.NotLoading -> Unit
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    count = items.itemCount,
                    key   = items.itemKey { it.id },        // stable keys for animations
                ) { index ->
                    val item = items[index]
                    if (item != null) {
                        FooItemCard(
                            item    = item,
                            onClick = { onIntent(FooContract.Intent.SelectItem(item.id)) },
                        )
                    } else {
                        FooItemPlaceholder()
                    }
                }

                // Append load state — bottom of list
                item {
                    when (val append = items.loadState.append) {
                        is LoadState.Loading -> AppendLoader()
                        is LoadState.Error   -> AppendError(
                            message = append.error.localizedMessage.orEmpty(),
                            onRetry = { items.retry() }
                        )
                        is LoadState.NotLoading -> Unit
                    }
                }
            }
        }
    }
}
```

---

## Koin wiring

```kotlin
// Platform module (androidMain / iosMain / desktopMain)
single<FooRepository> { FooRepositoryImpl(get()) }

// No special Koin wiring needed for PagingSource —
// it is created by the pagingSourceFactory lambda inside the ViewModel.
// ViewModel is registered in the common module as usual:
viewModel { FooViewModel(get()) }
```

---

## RemoteMediator — network + SQLDelight cache

Use `RemoteMediator` when you want to cache pages locally and serve from DB while loading fresh data in the background. It requires SQLDelight (or Room) as the local store.

```kotlin
// :data — FooRemoteMediator.kt
class FooRemoteMediator(
    private val api: FooApi,
    private val db: FooDatabase,
) : RemoteMediator<Int, FooItem>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FooItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH  -> STARTING_PAGE
            LoadType.PREPEND  -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND   -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                // derive next page from last item or a stored cursor
                db.fooQueries.getNextPage().executeAsOneOrNull()?.toInt() ?: STARTING_PAGE
            }
        }

        return try {
            val response = api.getItems(page = page, pageSize = state.config.pageSize)
            db.transaction {
                if (loadType == LoadType.REFRESH) db.fooQueries.deleteAll()
                response.items.forEach { db.fooQueries.insert(it.toEntity()) }
                db.fooQueries.saveNextPage((page + 1).toString())
            }
            MediatorResult.Success(endOfPaginationReached = response.items.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}

// Updated Pager in ViewModel:
val items: Flow<PagingData<FooItem>> = Pager(
    config         = PagingConfig(pageSize = 20, enablePlaceholders = false),
    remoteMediator = FooRemoteMediator(api, db),
    pagingSourceFactory = { db.fooQueries.pagingSource().asPagingSourceFactory()() }
).flow.cachedIn(viewModelScope)
```

---

## Testing PagingSource

```kotlin
class FooPagingSourceTest {

    private val fakeApi = FakeFooApi(items = (1..50).map { FooItem(id = "$it") })

    @Test
    fun `first page loads correctly`() = runTest {
        val source = FooPagingSource(fakeApi)
        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key                = null,
                loadSize           = 20,
                placeholdersEnabled = false,
            )
        )
        assertTrue(result is LoadResult.Page)
        result as LoadResult.Page
        assertEquals(20, result.data.size)
        assertNotNull(result.nextKey)
        assertNull(result.prevKey)
    }

    @Test
    fun `last page sets nextKey to null`() = runTest {
        val source = FooPagingSource(FakeFooApi(items = (1..5).map { FooItem(id = "$it") }))
        val result = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        ) as LoadResult.Page
        assertNull(result.nextKey)
    }

    @Test
    fun `api error returns LoadResult Error`() = runTest {
        val source = FooPagingSource(FakeFooApi(shouldThrow = true))
        val result = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )
        assertTrue(result is LoadResult.Error)
    }
}
```

---

## Common Anti-Patterns

- **`PagingData` inside `UiState`** — `PagingData` is not a plain value; it cannot be
  copied or compared. Keep it as a separate `Flow<PagingData<T>>` on the ViewModel. Using
  it inside a `data class State(val items: PagingData<T>)` compiles but breaks MVI state diffing.
- **No `.cachedIn(viewModelScope)`** — without caching, the `Pager` re-fetches from page 1
  on every recomposition or navigation event. Always chain `.cachedIn(viewModelScope)`.
- **Creating `Pager` inside a composable** — `Pager` must be created at the ViewModel level
  so it survives recomposition. A `Pager` in a composable creates a new one on every recompose.
- **Offset pagination when API provides cursors** — offset requests can return duplicates or
  miss items if the server list changes between pages; use the cursor the API provides.
- **Missing `key` lambda in `items(...)`** — without stable keys, Compose cannot animate
  insertions/removals and focus is lost on page appends. Always provide `key = items.itemKey { it.id }`.
- **Ignoring `LoadState.refresh`** — the first load goes through `refresh`, not `append`.
  A screen that only handles `append` loading shows a blank list with no spinner on initial load.
- **Not handling `LoadState.Error` on refresh** — an API error on first load leaves the user
  with a blank screen. Always show a retry button when `loadState.refresh is LoadState.Error`.

---

## Related Skills

- `kotlin-multiplatform-network-layer` — `FooApi` interface and `safeRequest` wrapper that
  `FooPagingSource` calls; network errors map to `LoadResult.Error`
- `kotlin-multiplatform-sqldelight-setup` — required for `RemoteMediator`; SQLDelight provides
  the local paging source via `Query.asPagingSourceFactory()`
- `kotlin-multiplatform-mvi` — ViewModel still follows the MVI contract; `PagingData` flow
  lives alongside `StateFlow<UiState>`, not inside it
- `kotlin-multiplatform-unit-testing` — `PagingSource.load()` is a suspend function testable
  with `runTest`; no special paging test library required for source-level tests
- `kotlin-multiplatform-repository-pattern` — `FooRepository` interface exposes
  `pagingSource()` as a factory; `FooRepositoryImpl` owns the concrete `FooPagingSource`

---

## Output Style

When implementing paging, respond in this order:
1. **Strategy** — cursor or offset, based on the API contract
2. **TOML additions** — `paging-common` and `paging-compose` versions
3. **PagingSource** — full implementation with all `LoadResult` branches
4. **Repository wiring** — interface method + implementation
5. **ViewModel** — `Pager` with `PagingConfig`, `.cachedIn(viewModelScope)`
6. **UI** — `collectAsLazyPagingItems`, `LazyColumn` with `key`, refresh + append load states
7. **Tests** — first page, last page (nextKey null), and error case

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
