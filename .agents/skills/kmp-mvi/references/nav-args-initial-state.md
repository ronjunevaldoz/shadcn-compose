# Nav Args as Initial State

Part of `kmp-mvi`. Load this file when working on: nav args as initial state.

---

Route arguments (e.g. `userId`, `orderId`) must reach the ViewModel as constructor
parameters — not as `Intent`. They are identity, not user input.

```kotlin
// commonMain nav route
@Serializable
data class UserProfileRoute(val userId: String)

// ViewModel receives the arg directly
class UserProfileViewModel(
    private val userId: String,           // from NavBackStackEntry via Koin
    private val repo: UserProfileRepository,
) : MviViewModel<UserProfileContract.State, ...>(UserProfileContract.State.Loading) {

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { UserProfileContract.State.Loading }
            repo.getProfile(userId).fold(
                onSuccess = { updateState { UserProfileContract.State.Success(it) } },
                onFailure = { updateState { UserProfileContract.State.Error(it.message.orEmpty()) } },
            )
        }
    }
}
```

Wire the arg through Koin using `getNavArguments()` or a `SavedStateHandle`:

```kotlin
// :feature:profile:ui/di
val profileUiModule = module {
    viewModel { params ->
        UserProfileViewModel(userId = params.get(), repo = get())
    }
}

// Screen — passes arg at call site
@Composable
fun UserProfileScreen(
    route: UserProfileRoute,
    onBack: () -> Unit,
    viewModel: UserProfileViewModel = koinViewModel(parameters = { parametersOf(route.userId) }),
) { ... }
```

**Rule:** never pass identity args as `Intent.Load(id)` — the ViewModel would need to
guard against double-loads and the arg would not survive process death.

---

