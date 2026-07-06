---
name: kotlin-multiplatform-deep-linking
description: >-
  Deep linking for Kotlin Multiplatform — Android App Links (Digital Asset Links),
  iOS Universal Links (Apple App Site Association), NavHost route integration,
  deep-link route parsing in commonMain, intent handling in AndroidActivity,
  and NSUserActivity/openURL handling in the iOS app delegate.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - deep linking
    - App Links
    - Universal Links
    - deep link
    - NavHost deep link
    - AASA
    - Digital Asset Links
    - intent filter
    - route parsing
    - deep link handling
    - openURL
    - NSUserActivity
    - KMP deep link
    - URL scheme
    - custom scheme
---

## When to Use This Skill

Use when:
- Tapping a URL in email/browser should open a specific screen in the app
- Implementing App Links (Android) or Universal Links (iOS) verified against your domain
- Parsing URL parameters and mapping them to NavHost routes
- Supporting custom URL schemes (`myapp://`) as a fallback for unverified deep links
- Testing deep link entry points with unit tests

**Trigger keywords:** deep linking, App Links, Universal Links, deep link, AASA,
Digital Asset Links, intent filter, route parsing, NavHost deep link, openURL,
NSUserActivity, deep link handling, custom URL scheme, in-app linking, KMP deep link.

**Freshness rule:** App Links verification requires a valid `assetlinks.json` hosted at
`https://<yourdomain>/.well-known/assetlinks.json`. The SHA-256 fingerprint in the file
must match the app's signing certificate. On iOS, the `apple-app-site-association` file
must be hosted at `https://<yourdomain>/.well-known/apple-app-site-association` (or at
the domain root). Verify hosting via `aasa-validator` or Android App Links Assistant
before shipping.

---

## Recommendation First

Define deep-link routes as a `DeepLink` sealed class in `commonMain`. Android and iOS
entry points (Activity intent, iOS `application:openURL:` / `application:continueUserActivity:`)
parse the URL into a `DeepLink` and pass it to the NavController. The parser lives in
`commonMain` so it can be unit tested without a device.

---

## Route definition — commonMain

```kotlin
// :core:navigation — DeepLink.kt
sealed class DeepLink {
    data class ProductDetail(val productId: String) : DeepLink()
    data class OrderStatus(val orderId: String)     : DeepLink()
    object Home                                     : DeepLink()
    data class Unknown(val url: String)             : DeepLink()
}
```

---

## URL parser — commonMain (pure, testable)

```kotlin
// :core:navigation — DeepLinkParser.kt
object DeepLinkParser {
    private const val HOST = "www.example.com"

    fun parse(url: String): DeepLink {
        // Supports both https://www.example.com/... and myapp://...
        val normalized = url
            .replace("myapp://", "https://$HOST/")
            .trimEnd('/')

        return when {
            normalized.matches(Regex(".*/$HOST/products/([^/]+)")) -> {
                val id = Regex(".*/products/([^/]+)").find(normalized)?.groupValues?.get(1)
                if (id != null) DeepLink.ProductDetail(id) else DeepLink.Unknown(url)
            }
            normalized.matches(Regex(".*/$HOST/orders/([^/]+)")) -> {
                val id = Regex(".*/orders/([^/]+)").find(normalized)?.groupValues?.get(1)
                if (id != null) DeepLink.OrderStatus(id) else DeepLink.Unknown(url)
            }
            normalized.contains("$HOST/") -> DeepLink.Home
            else -> DeepLink.Unknown(url)
        }
    }
}
```

---

## NavHost route registration

```kotlin
// :ui — AppNavHost.kt
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.Home,
) {
    NavHost(navController, startDestination = startDestination) {

        composable(
            route = Routes.ProductDetail,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://www.example.com/products/{productId}" },
                navDeepLink { uriPattern = "myapp://products/{productId}" },
            ),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(productId = productId)
        }

        composable(
            route = Routes.OrderStatus,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://www.example.com/orders/{orderId}" },
                navDeepLink { uriPattern = "myapp://orders/{orderId}" },
            ),
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderStatusScreen(orderId = orderId)
        }

        composable(Routes.Home) { HomeScreen() }
    }
}

object Routes {
    const val Home          = "home"
    const val ProductDetail = "product/{productId}"
    const val OrderStatus   = "order/{orderId}"
}
```

---

## Android — AndroidManifest.xml intent filters

```xml
<activity android:name=".MainActivity" ...>

    <!-- App Links — verified HTTPS deep links -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="www.example.com" />
    </intent-filter>

    <!-- Custom scheme fallback -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" />
    </intent-filter>

</activity>
```

---

## Android — handle intent in Compose Activity

```kotlin
// androidApp/MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // Handle deep link from the launching intent
            LaunchedEffect(intent) {
                intent?.data?.toString()?.let { url ->
                    val route = DeepLinkParser.parse(url).toRoute()
                    if (route != null) navController.navigate(route) { launchSingleTop = true }
                }
            }
            AppNavHost(navController = navController)
        }
    }

    // Handle deep links while app is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Delegate to NavController via a shared state or event channel
    }
}

fun DeepLink.toRoute(): String? = when (this) {
    is DeepLink.ProductDetail -> "product/$productId"
    is DeepLink.OrderStatus   -> "order/$orderId"
    DeepLink.Home             -> Routes.Home
    is DeepLink.Unknown       -> null
}
```

---

## iOS — Universal Links and custom scheme

Add to `iosApp/iosApp.swift` (or `AppDelegate.swift`):

```swift
// Universal Links via NSUserActivity
func application(
    _ application: UIApplication,
    continue userActivity: NSUserActivity,
    restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void
) -> Bool {
    guard userActivity.activityType == NSUserActivityTypeBrowsingWeb,
          let url = userActivity.webpageURL else { return false }
    return handleDeepLink(url.absoluteString)
}

// Custom scheme
func application(
    _ app: UIApplication,
    open url: URL,
    options: [UIApplication.OpenURLOptionsKey: Any] = [:]
) -> Bool {
    return handleDeepLink(url.absoluteString)
}

private func handleDeepLink(_ url: String) -> Bool {
    // Call shared Kotlin parser via KMP bridging
    let deepLink = DeepLinkParser.shared.parse(url: url)
    // Post to a Kotlin Flow/channel consumed by the NavController
    DeepLinkHandler.shared.emit(deepLink)
    return true
}
```

---

## assetlinks.json (Android) — host at /.well-known/

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example.app",
    "sha256_cert_fingerprints": ["AA:BB:CC:..."]
  }
}]
```

---

## apple-app-site-association (iOS) — host at /.well-known/

```json
{
  "applinks": {
    "apps": [],
    "details": [{
      "appID": "TEAMID.com.example.app",
      "paths": ["/products/*", "/orders/*", "/"]
    }]
  }
}
```

---

## Testing the parser

```kotlin
class DeepLinkParserTest {
    @Test fun `product URL parses to ProductDetail`() {
        val result = DeepLinkParser.parse("https://www.example.com/products/abc-123")
        assertEquals(DeepLink.ProductDetail("abc-123"), result)
    }

    @Test fun `custom scheme falls through to ProductDetail`() {
        val result = DeepLinkParser.parse("myapp://products/xyz")
        assertEquals(DeepLink.ProductDetail("xyz"), result)
    }

    @Test fun `unknown URL maps to Unknown`() {
        val result = DeepLinkParser.parse("https://other.com/page")
        assertTrue(result is DeepLink.Unknown)
    }
}
```

---

## Common Anti-Patterns

- **Parsing URLs in the Activity/AppDelegate** — URL parsing belongs in `commonMain`
  (`DeepLinkParser`) so it can be unit tested; Activities and delegates should only relay
  the raw URL string to the parser
- **Deep link routes not registered in NavHost** — if a route isn't in `navDeepLink { uriPattern }`,
  the Compose Navigation library ignores the intent's data URI; register every path pattern
- **Missing `android:autoVerify="true"`** — without this attribute Android never initiates
  App Links verification and the link opens in a browser chooser, not the app directly
- **Trailing-slash mismatch** — `assetlinks.json` paths and NavHost `uriPattern` must agree
  on whether paths end with `/`; normalize before matching
- **Not handling `onNewIntent`** — if the app is already running and receives a deep link,
  `onCreate` is not called again; `onNewIntent` must relay the new intent to the NavController

---

## Related Skills

- `kotlin-multiplatform-navigation` — `NavHost` setup and `NavController` wiring;
  deep links are registered as `navDeepLink` blocks inside `composable { }` builders
- `kotlin-multiplatform-mvi` — deep-link handling triggers a `NavigateTo` side effect,
  not a state mutation; the ViewModel emits a `Channel`-based `Effect`
- `kotlin-multiplatform-unit-testing` — `DeepLinkParser` is a pure object with no
  platform dependencies; test it with plain JUnit `@Test` in `commonTest`

---

## Output Style

When implementing deep linking, respond in this order:
1. **DeepLink sealed class** — one subclass per linkable screen
2. **DeepLinkParser** — pure regex-based parser with tests
3. **NavHost** — `navDeepLink { uriPattern }` blocks for each route
4. **AndroidManifest** — intent filter (App Links + custom scheme)
5. **Android Activity** — `LaunchedEffect(intent)` + `onNewIntent`
6. **iOS** — `continue userActivity` + `open url` handlers
7. **Server files** — `assetlinks.json` and `apple-app-site-association` templates

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
