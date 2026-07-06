---
name: kotlin-multiplatform-navigation
description: >
  Sets up type-safe navigation in a Kotlin Multiplatform Compose project using
  Jetpack Navigation Compose for KMP (org.jetbrains.androidx.navigation). Covers:
  route definitions with kotlinx.serialization, NavHost setup in a :app:shared
  or :feature:*:ui nav graph, type-safe arguments, deep links, and bottom
  navigation wiring. Works on Android, iOS, Desktop (JVM), and Web (JS/WasmJs).
  Assumes the project was scaffolded with kotlin-multiplatform-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
  keywords:
    - Navigation Compose
    - KMP navigation
    - type-safe routes
    - NavHost
    - NavController
    - Kotlin Multiplatform
    - Compose Multiplatform
    - deep links
    - bottom navigation
---

## Overview

Two options are available for KMP navigation. This skill covers both:

| Option | Library | Maturity | Best for |
|---|---|---|---|
| **Navigation Compose (KMP)** | `org.jetbrains.androidx.navigation:navigation-compose` | Stable (JetBrains) | CMP-first projects, familiar Jetpack API |
| **Decompose** | `com.arkivanov.decompose:decompose` | Stable (community) | Complex back-stack logic, lifecycle control |

**Recommendation**: Use Navigation Compose for KMP unless you need Decompose's fine-grained component lifecycle control.

## When to Use This Skill

Use this skill when you need to:
- Add type-safe navigation to a KMP Compose app
- Decide between Navigation Compose KMP and Decompose
- Wire nested graphs, bottom navigation, or deep links
- Recheck navigation API changes before upgrading the library

**Trigger keywords:** navigation, nav graph, navhost, route, deep link, bottom nav,
KMP navigation, type-safe routes, Decompose, Navigation Compose,
navigate to screen, go to screen, back stack, push screen, pop back,
navigate back, pass arguments, route arguments, nested navigation, screen transition,
web routing, browser fragment, hash navigation, wasmJs routing,
bindToBrowserNavigation, browser history sync, SerialName route, hash deep link,
navigate, routing, move between screens, switch screens, go to, page navigation,
navigate from screen, pass data between screens, link screens.

**Freshness rule:** recheck the JetBrains Navigation Compose docs before upgrading or
copying snippets into a new project.

---

## Recommendation First

Default to **JetBrains Navigation Compose with type-safe routes + one nested graph per feature**.

Why:
- type-safe routes catch destination mismatches at compile time
- nested graphs keep feature navigation encapsulated and testable in isolation
- the JetBrains fork supports all KMP targets (Android, iOS, Desktop, Web) from `commonMain`

Use Decompose only when you need platform-specific lifecycle callbacks or deep back-stack control
that Navigation Compose does not yet support on all targets.

---

## Prerequisites

- Project scaffolded with `kotlin-multiplatform-feature-scaffold`
- `kotlinx.serialization` applied (already in `GROUP_ID.feature.api` convention plugin)
- All UI modules apply `GROUP_ID.feature.ui` convention plugin

---

## Version Reference

Add to `gradle/libs.versions.toml`:

```toml
[versions]
navigation-compose = "2.9.2"    # JetBrains KMP fork; check latest at
                                 # https://github.com/JetBrains/compose-multiplatform
decompose          = "3.5.0"    # optional alternative

[libraries]
# Navigation Compose (KMP)
navigation-compose         = { module = "org.jetbrains.androidx.navigation:navigation-compose",  version.ref = "navigation-compose" }
navigation-compose-testing = { module = "org.jetbrains.androidx.navigation:navigation-testing", version.ref = "navigation-compose" }

# Decompose (optional alternative)
decompose          = { module = "com.arkivanov.decompose:decompose",               version.ref = "decompose" }
decompose-extensions-compose = { module = "com.arkivanov.decompose:extensions-compose", version.ref = "decompose" }
```

---

## Option A: Navigation Compose (KMP)

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

## Option B: Decompose (alternative)

Use Decompose when you need:
- Fine-grained component lifecycle (independent of Compose recomposition)
- Deep back-stack control (e.g., tabbed navigation with independent stacks)
- Navigation logic fully in commonMain without Compose dependency

```kotlin
// :shared/build.gradle.kts
sourceSets {
    commonMain.dependencies {
        implementation(libs.decompose)
        implementation(libs.decompose.extensions.compose)
    }
}
```

Basic Decompose root component:

```kotlin
class RootComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    sealed class Child {
        class HomeChild(val component: HomeComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Home,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    fun onNavigateToProfile() = navigation.push(Config.Profile)
    fun onBack() = navigation.pop()

    private fun createChild(config: Config, ctx: ComponentContext): Child = when (config) {
        Config.Home    -> Child.HomeChild(HomeComponent(ctx))
        Config.Profile -> Child.ProfileChild(ProfileComponent(ctx))
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Home    : Config
        @Serializable data object Profile : Config
    }
}
```

---

## MVI + NavHost + Clean Architecture: End-to-End Wiring

These three patterns only work correctly when wired together. Here is the complete picture.

### Full data flow — from user tap to screen change

```
:feature:auth:presenter       :feature:auth:ui         :app (AppNavHost)
  AuthViewModel                 AuthScreen               NavHost
    handleIntent
    (LoginClicked)                LaunchedEffect
       │                            collectEffect
       │  loginUseCase()               │
       │  → success                    │
       ▼                               │
    sendEffect(                 Effect.NavigateToHome
      Effect.NavigateToHome)           │
                                       ▼
                               onNavigateToHome()  ──────►  navController.navigate(HomeRoute)
                               (lambda from NavHost)
```

The `NavController` stays in `AppNavHost`. The ViewModel never sees it.

### Lambda vs AppNavigator — when to use which

| Scenario | Use | Why |
|---|---|---|
| Same-feature nav (login → register, within authGraph) | Lambda from NavHost | NavController is local; no interface needed |
| Cross-feature nav (cart → checkout) | `AppNavigator` in `:core:api` | Features must not depend on each other |
| ViewModel in `:presenter` triggers nav | `sendEffect(Effect.Navigate...)` → Screen → lambda | ViewModel has no Compose dependency |
| ViewModel needs cross-feature nav (no Effect needed) | Inject `AppNavigator`, call from `handleIntent` | `AppNavigator` is a pure interface, safe in `:presenter` |

**Rule:** Within one feature graph, lambdas. Across feature boundaries, `AppNavigator`.

### Feature NavGraphBuilder extension — the correct module boundary

Each feature's `:ui` module exposes a `NavGraphBuilder` extension. The host calls it. The
extension receives lambdas for graph-entry and graph-exit navigation — never the `NavController`.

```kotlin
// :feature:auth:ui
fun NavGraphBuilder.authGraph(
    onAuthComplete: () -> Unit,
) {
    navigation<AuthGraph>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToRegister = { /* internal — navController captured by NavHost closure */ },
                onLoginSuccess = onAuthComplete,
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(onBack = { /* pop internal */ })
        }
    }
}

// :app — AppNavHost wires the graph, owns the navController
NavHost(navController = navController, startDestination = AuthGraph) {
    authGraph(onAuthComplete = {
        navController.navigate(HomeRoute) {
            popUpTo<AuthGraph> { inclusive = true }   // clear auth stack
        }
    })
    homeGraph()
    profileGraph()
}
```

### Auth gate — conditional start destination

Guard the entire app with a session check. Handle both cold-start (via `startDestination`)
and runtime logout (via `LaunchedEffect`):

```kotlin
@Composable
fun AppNavHost(
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val session by sessionViewModel.state.collectAsStateWithLifecycle()

    // Runtime auth loss — navigate to auth and clear the entire back stack
    LaunchedEffect(session.isAuthenticated) {
        if (!session.isAuthenticated) {
            navController.navigate(AuthGraph) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (session.isAuthenticated) HomeRoute else AuthGraph,
    ) {
        authGraph(onAuthComplete = {
            navController.navigate(HomeRoute) {
                popUpTo<AuthGraph> { inclusive = true }
            }
        })
        homeGraph()
        profileGraph()
    }
}
```

**Rules:**
- `startDestination` covers cold-start. `LaunchedEffect` covers runtime logout.
- Always use `popUpTo(0) { inclusive = true }` on logout — never navigate to `AuthGraph`
  on top of `HomeRoute` or the user can press Back to return to authenticated content.
- `SessionViewModel` lives in `:core:presenter` — it is shared, not feature-scoped.

### Passing a result back to the previous screen

When a child screen (e.g., a picker) returns a value to its parent:

```kotlin
// NavHost — wire result through previousBackStackEntry.savedStateHandle
composable<CheckoutRoute> {
    CheckoutScreen(...)
}
composable<CityPickerRoute> {
    CityPickerScreen(
        onCitySelected = { city ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("selected_city", city)
            navController.popBackStack()
        }
    )
}

// Parent ViewModel — consume the result from SavedStateHandle
class CheckoutViewModel(
    savedStateHandle: SavedStateHandle,
) : MviViewModel<CheckoutContract.State, ...>(...) {

    init {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("selected_city", null)
                .filterNotNull()
                .collect { city ->
                    updateState { copy(city = city) }
                    savedStateHandle["selected_city"] = null  // consume once
                }
        }
    }
}
```

**Rule:** The result key is owned by the parent feature. Document it in the child screen's
`NavGraphBuilder` extension KDoc so callers know what to read.

### Scaffold + NavHost innerPadding

Apply `innerPadding` at the `NavHost` level when all screens share the same bottom bar:

```kotlin
Scaffold(
    bottomBar = { AppBottomBar(navController) },
) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = Modifier.padding(innerPadding),   // ✓ screens fill padded area
    ) {
        homeGraph()
        profileGraph()
    }
}
```

For screens that need to control their own insets (e.g., a `LazyColumn` that wants bottom
padding but not top), pass `innerPadding` as a parameter instead of applying it at the NavHost:

```kotlin
Scaffold { innerPadding ->
    NavHost(...) {
        composable<HomeRoute> {
            HomeScreen(contentPadding = innerPadding)   // screen decides how to use it
        }
    }
}
```

Never apply `innerPadding` in both the NavHost modifier AND the screen — padding doubles
and the bottom bar overlaps content.

---

## Guidelines

- Always use `@Serializable` route classes — never string-based routes
- Never navigate from a `LaunchedEffect` with a null check — use `SideEffect` or ViewModel events
- Use `launchSingleTop = true` + `restoreState = true` for bottom nav tabs
- Keep route classes in a shared `:core:navigation` or `:shared` module — not in feature UI modules
- Feature UI modules receive navigation lambdas as parameters — they do NOT hold a `NavController`
- Same-feature navigation uses lambdas; cross-feature navigation uses `AppNavigator`
- Auth gate: set `startDestination` for cold-start, use `LaunchedEffect` for runtime logout
- Always use `popUpTo(0) { inclusive = true }` when clearing the back stack on logout

---

## Verification

1. `./gradlew :shared:compileKotlinMetadata` — route classes and NavHost compile in commonMain
2. `./gradlew :shared:compileDebugKotlinAndroid` — Android navigation compiles
3. `./gradlew :shared:compileKotlinJvm` — Desktop navigation compiles
4. Launch app and navigate to each destination — verify back stack behavior
5. Test deep link: `adb shell am start -W -a android.intent.action.VIEW -d "https://example.com/article/123" GROUP_ID`

---

## Testing

```kotlin
// Add to androidTest source set — navigation-testing is Android-only
// androidTestImplementation(libs.navigation.compose.testing)
@get:Rule val composeRule = createComposeRule()

@Test fun `start destination renders home screen`() {
    composeRule.setContent {
        val navController = rememberTestNavController()
        AppNavHost(navController = navController)
    }
    composeRule.onNodeWithTag(HomeTestTags.ROOT).assertExists()
}

@Test fun `navigate to detail shows detail screen`() {
    composeRule.setContent {
        val navController = rememberTestNavController()
        AppNavHost(navController = navController)
        LaunchedEffect(Unit) { navController.navigate(UserDetailRoute(userId = "42")) }
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(DetailTestTags.ROOT).assertExists()
}

@Test fun `back stack pops on up navigation`() {
    composeRule.setContent {
        val navController = rememberTestNavController()
        AppNavHost(navController = navController)
        LaunchedEffect(Unit) {
            navController.navigate(UserDetailRoute(userId = "1"))
            navController.popBackStack()
        }
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(HomeTestTags.ROOT).assertExists()
}
```

---

## Common Anti-Patterns

- using string-based route names instead of type-safe sealed/data classes — breaks at runtime, not compile time
- putting navigation logic inside the ViewModel — ViewModels should emit navigation `Effect`, not call `navController`
- passing `navController` deep into composables — pass lambdas or use the effect pattern instead
- defining all routes in one flat `NavHost` — leads to unnavigable spaghetti; use nested graphs per feature
- sharing a `navController` between nested graphs — each graph should own its back stack
- storing navigation state in `ViewModel` as a flag — `Effect` is the correct mechanism for navigation events
- navigating to `AuthGraph` on logout without clearing the back stack — user can press Back to return to authenticated screens; always use `popUpTo(0) { inclusive = true }`
- applying `innerPadding` in both NavHost modifier and the screen — padding doubles; apply it in one place only
- setting `startDestination` only (no `LaunchedEffect`) for the auth gate — handles cold-start but ignores runtime session expiry
- passing a result back via a `State` field in the child ViewModel — use `previousBackStackEntry.savedStateHandle` instead; ViewModel state is not the back-stack result channel

If back stack is broken or navigation effects replay, audit the above list first.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — `NavHost` lives in the `:app:shared` module created by the scaffold
- `kotlin-multiplatform-mvi` — navigation events should be sent as `Effect` from a ViewModel, not as state
- `kotlin-multiplatform-presenter-module` — pure ViewModel that emits nav `Effect` without a Compose dependency
- `kotlin-multiplatform-expect-actual` — platform-specific deep link and URI handling

---

## Output Style

When asked about navigation or routing, respond in this order:
1. recommendation (type-safe routes, nested graphs per feature)
2. route definition snippet
3. NavHost wiring snippet
4. why type-safe routes over string routes
5. main alternative (Decompose, manual back stack)

Keep each snippet to one route and one composable destination. Map to the user's actual screen and feature names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Added "One NavHost + nested graphs vs. multiple NavHosts" decision table — default to one root NavHost with a nested graph per feature; reserve multiple NavHosts for concurrent back stacks (bottom nav, two-pane). New anti-pattern: one independent NavHost per feature for sequential navigation. |
| 2026-06-28 | Fixed AppNavigator Step 7: use NavControllerHolder singleton so Koin can construct AppNavigatorImpl at startup; AppNavHost sets holder.current via DisposableEffect. Added end-to-end MVI + NavHost + Clean Architecture wiring section, auth gate, result passing, innerPadding rule. Four new anti-patterns. |
| 2026-06-28 | Added `bindToBrowserNavigation` pattern with full WasmJs hash-routing example and `@SerialName` route convention. |
| 2026-06-24 | Added web/WasmJs browser fragment routing guidance and hash-navigation keywords. |
| 2026-06-06 | Initial release. |
