# Option A: Navigation Compose (KMP)

Part of `kmp-navigation`.

---

### Step 1: Add dependency to navigation host module

The nav graph usually lives in a `:shared` or `:app:navigation` module that depends on all feature UI modules.

```kotlin
// :shared/build.gradle.kts  (or wherever your AppNavHost lives)
sourceSets {
    commonMain.dependencies {
        implementation(libs.navigation.compose)
        implementation(libs.kotlinx.serialization)
    }
}
```

---

### Step 2: Define type-safe routes

Create `src/commonMain/kotlin/GROUP_ID/navigation/Routes.kt`:

```kotlin
package GROUP_ID.navigation

import kotlinx.serialization.Serializable

// Top-level destinations — no arguments
@Serializable object HomeRoute
@Serializable object ProfileRoute
@Serializable object SettingsRoute

// Destinations with arguments
@Serializable data class UserDetailRoute(val userId: String)
@Serializable data class ArticleRoute(val articleId: String, val fromDeepLink: Boolean = false)
```

> Each `@Serializable` object/class becomes a type-safe navigation route.
> Arguments are constructor parameters — no string templates, no bundles.

---

### Step 3: Build the NavHost

Create `src/commonMain/kotlin/GROUP_ID/navigation/AppNavHost.kt`:

```kotlin
package GROUP_ID.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import GROUP_ID.feature.home.ui.HomeScreen
import GROUP_ID.feature.profile.ui.ProfileScreen
import GROUP_ID.feature.userdetail.ui.UserDetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToUserDetail = { userId ->
                    navController.navigate(UserDetailRoute(userId))
                }
            )
        }

        composable<UserDetailRoute> { backStackEntry ->
            val route: UserDetailRoute = backStackEntry.toRoute()
            UserDetailScreen(
                userId = route.userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen()
        }
    }
}
```

---

### Step 4: Nested navigation graphs

Organize feature navigation into nested graphs. Create per-feature nav graph extensions:

```kotlin
// In :feature:auth:ui
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

@Serializable object AuthGraph
@Serializable object LoginRoute
@Serializable object RegisterRoute

fun NavGraphBuilder.authGraph(
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraph>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(onLoginSuccess = onLoginSuccess)
        }
        composable<RegisterRoute> {
            RegisterScreen()
        }
    }
}
```

Then wire it in `AppNavHost`:

```kotlin
NavHost(navController = navController, startDestination = AuthGraph) {
    authGraph(onLoginSuccess = { navController.navigate(HomeRoute) })
    // other graphs...
}
```

#### One NavHost + nested graphs vs. multiple NavHosts

Default to **one root NavHost with one nested graph per feature**. Reach for a second
NavHost only when two back stacks must be live *at the same time*.

| Situation | Use | Why |
|---|---|---|
| Hub → feature → back; sequential flows (e.g. a dashboard that launches feature screens) | **One NavHost + nested graph per feature** | Single unified back stack; deep links and `popUpTo` behave predictably; features stay modular |
| A feature owns several screens | **Nested graph** (`navigation<FeatureGraph>{…}`) | Encapsulates the feature; one entry route exposed to the parent |
| Bottom-navigation tabs that each keep their own history | **Multiple NavHosts** (one per tab) | Each tab needs an independent, concurrent back stack |
| List-detail / two-pane on large screens | **Multiple NavHosts** (one per pane) | Panes navigate independently and simultaneously |

**Anti-pattern: one independent NavHost per feature for sequential navigation.** It
fragments the back stack — the system back button, deep linking, and shared-element
transitions break or need manual cross-host coordination. If navigation is sequential
(open a screen, do work, go back), it belongs in **one** NavHost as a nested graph.

```kotlin
// ✓ One NavHost, a nested graph per feature, each screen owns its own ViewModel
NavHost(navController, startDestination = DashboardRoute) {
    composable<DashboardRoute> { DashboardScreen(onOpen = { navController.navigate(it) }) }
    featureGraph()   // navigation<FeatureGraph> { composable<EditorRoute>{ EditorScreen() } … }
}
```

---

### Step 5: Bottom navigation

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelRoutes.forEach { topLevel ->
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(topLevel.route::class) == true,
                        onClick = {
                            navController.navigate(topLevel.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(topLevel.icon, contentDescription = topLevel.label) },
                        label = { Text(topLevel.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) { /* destinations */ }
    }
}

data class TopLevelRoute<T : Any>(val route: T, val icon: ImageVector, val label: String)

val topLevelRoutes = listOf(
    TopLevelRoute(HomeRoute, Icons.Default.Home, "Home"),
    TopLevelRoute(ProfileRoute, Icons.Default.Person, "Profile"),
)
```

---

### Step 6: Deep links

```kotlin
composable<ArticleRoute>(
    deepLinks = listOf(
        navDeepLink<ArticleRoute>(
            basePath = "https://example.com/article"
        )
    )
) { backStackEntry ->
    val route: ArticleRoute = backStackEntry.toRoute()
    ArticleScreen(articleId = route.articleId)
}
```

Add intent filters in `AndroidManifest.xml`:

```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="example.com" />
    </intent-filter>
</activity>
```

#### Web and WasmJs browser links

Compose Multiplatform web keeps navigation in the browser fragment. When a route is
opened or generated for `wasmJsMain` or `jsMain`, the URL should start with `#` so the
app handles it as an in-app destination instead of a server path.

Example:

```text
https://example.com/#login
```

If you customize route-to-URL mapping, keep generated routes fragment-safe and use the
browser fragment as the source of truth for manual entry and copy/paste.

#### WasmJs: binding browser history to NavController (`bindToBrowserNavigation`)

Use `bindToBrowserNavigation` (from `androidx.navigation:navigation-compose`) in
`wasmJsMain/main.kt` so that the browser's URL bar, Back button, and history stack stay
in sync with the in-app NavController. This requires no server configuration — the `#`
prefix means every URL is handled client-side.

**Step 1 — annotate every route with `@SerialName`** so the fragment is a short,
stable token instead of the full qualified class name:

```kotlin
// commonMain
@Serializable @SerialName("home")         data object HomeRoute
@Serializable @SerialName("login")        data object LoginRoute
@Serializable @SerialName("profile")      data object ProfileRoute
@Serializable @SerialName("admin_dashboard") data object AdminDashboardRoute

// Routes with arguments still use @SerialName on the class
@Serializable @SerialName("accept_invite")
data class AcceptInviteRoute(val code: String)
```

**Step 2 — call `bindToBrowserNavigation` inside `onNavHostReady`** in `wasmJsMain`:

```kotlin
// wasmJsMain/main.kt
@OptIn(ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport {
        App(
            onNavHostReady = { navController ->
                // ① On first load: read the URL hash and navigate to the matching route
                val hash = window.location.hash.substringAfter('#', "")
                when {
                    hash.startsWith("accept_invite/") ->
                        navController.navigate(AcceptInviteRoute(hash.substringAfter("accept_invite/")))
                    hash == "profile" -> navController.navigate(ProfileRoute)
                    hash.startsWith("login") -> navController.navigate(LoginRoute)
                    // … other deep-link paths …
                }

                // ② Translate NavController destinations → browser URL fragments
                navController.bindToBrowserNavigation { entry ->
                    val route = entry.destination.route.orEmpty()
                    when {
                        // Parametric routes: reconstruct the fragment from the typed route
                        route.startsWith("accept_invite") ->
                            "#accept_invite/${entry.toRoute<AcceptInviteRoute>().code}"
                        // No-arg routes: @SerialName becomes the fragment directly
                        else -> {
                            val name = route.substringBefore("/").substringBefore("?")
                            if (name.isNotBlank()) "#$name" else ""
                        }
                    }
                }
            }
        )
    }
}
```

**Step 3 — add the opt-in annotation** to the file or module that calls
`bindToBrowserNavigation`:

```kotlin
@OptIn(ExperimentalBrowserHistoryApi::class)
```

**Rules:**
- Always use `#` prefix — bare paths require server-side rewrite rules
- `@SerialName` on every no-arg route gives you short, human-readable fragments
- Read `window.location.hash` before binding so users can share and reload URLs
- For argument routes, extract values via `entry.toRoute<T>()` inside the lambda

---

### Step 7: AppNavigator — cross-feature navigation from ViewModels

When a ViewModel in `:presenter` needs to navigate across features (e.g., cart → checkout),
inject `AppNavigator` from `:core:api`. Use a `NavControllerHolder` so Koin can construct
the impl at startup before the `NavController` exists in Compose:

```kotlin
// :core:api
interface AppNavigator {
    fun navigateToHome()
    fun navigateToCheckout(cartId: String)
}

// :app — holder bridges Koin startup time and Compose time
class NavControllerHolder { var current: NavController? = null }

class AppNavigatorImpl(private val holder: NavControllerHolder) : AppNavigator {
    override fun navigateToHome() =
        holder.current?.navigate(HomeRoute) ?: Unit
    override fun navigateToCheckout(cartId: String) =
        holder.current?.navigate(CheckoutRoute(cartId)) ?: Unit
}

val appModule = module {
    single { NavControllerHolder() }
    single<AppNavigator> { AppNavigatorImpl(get()) }
}

// AppNavHost — set the holder as soon as navController is ready
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val holder: NavControllerHolder = koinInject()
    DisposableEffect(navController) {
        holder.current = navController
        onDispose { holder.current = null }
    }
    NavHost(navController = navController, startDestination = HomeRoute) { ... }
}
```

---

