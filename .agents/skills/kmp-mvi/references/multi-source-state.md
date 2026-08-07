# Multi-Source State and Flow Operators

Part of `kmp-mvi`. Load this file when working on: multi-source state and flow operators.

---

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

