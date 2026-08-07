# Cross-Feature Navigation

Part of `kmp-clean-architecture`. Load this file when working on: cross-feature navigation.

---

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

