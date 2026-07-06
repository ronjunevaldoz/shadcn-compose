---
name: kotlin-multiplatform-analytics
description: >-
  Shared Analytics interface for Kotlin Multiplatform — sealed event types in
  commonMain, platform implementations for Firebase Analytics (Android) and
  equivalent (iOS/Desktop), automatic screen tracking, Koin wiring with platform
  modules, and testing with a fake analytics recorder.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - analytics
    - Firebase Analytics
    - event tracking
    - screen tracking
    - sealed events
    - AnalyticsTracker
    - KMP analytics
    - platform analytics
    - event schema
    - A/B analytics
---

## When to Use This Skill

Use when:
- Adding event tracking, screen views, or user property recording to a KMP project
- Defining a shared event schema that multiple platforms report identically
- Swapping analytics backends (Firebase → Amplitude → custom) without changing feature code
- Testing feature code that fires analytics events

**Trigger keywords:** analytics, event tracking, Firebase Analytics, screen tracking,
track event, log event, user properties, AnalyticsTracker, event schema, KMP analytics,
analytics interface, analytics facade, mixpanel KMP, amplitude KMP.

**Freshness rule:** Firebase BoM version changes frequently — recheck
`com.google.firebase:firebase-bom` before pinning. iOS Firebase SDK version must match
the Android BoM equivalent. For non-Firebase backends recheck the SDK's KMP support status.

---

## Recommendation First

Define a sealed `AnalyticsEvent` hierarchy in `commonMain` and a single `Analytics`
interface. Platform modules provide the implementation — feature code never imports
a platform SDK directly.

---

## Event schema — commonMain

```kotlin
// :core:analytics:model — AnalyticsEvent.kt
sealed class AnalyticsEvent {
    abstract val name: String
    open val params: Map<String, Any> get() = emptyMap()

    // Screen views
    data class ScreenView(val screenName: String) : AnalyticsEvent() {
        override val name = "screen_view"
        override val params = mapOf("screen_name" to screenName)
    }

    // Feature events
    data class LoginSuccess(val method: String) : AnalyticsEvent() {
        override val name = "login"
        override val params = mapOf("method" to method)
    }

    data class PurchaseCompleted(val itemId: String, val price: Double) : AnalyticsEvent() {
        override val name = "purchase"
        override val params = mapOf("item_id" to itemId, "value" to price)
    }

    data class ButtonTapped(val buttonName: String, val screen: String) : AnalyticsEvent() {
        override val name = "button_tap"
        override val params = mapOf("button_name" to buttonName, "screen" to screen)
    }

    // Extend with your own events — one sealed subclass per meaningful action
}
```

---

## Analytics interface — commonMain

```kotlin
// :core:analytics:api — Analytics.kt
interface Analytics {
    fun track(event: AnalyticsEvent)
    fun setUserProperty(key: String, value: String)
    fun setUserId(userId: String?)
}
```

---

## Android implementation — Firebase

```kotlin
// :core:analytics:data (androidMain) — FirebaseAnalyticsImpl.kt
class FirebaseAnalyticsImpl(
    private val firebase: com.google.firebase.analytics.FirebaseAnalytics,
) : Analytics {

    override fun track(event: AnalyticsEvent) {
        val bundle = android.os.Bundle().apply {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Double -> putDouble(key, value)
                    is Long   -> putLong(key, value)
                    is Int    -> putInt(key, value)
                    else      -> putString(key, value.toString())
                }
            }
        }
        firebase.logEvent(event.name, bundle)
    }

    override fun setUserProperty(key: String, value: String) {
        firebase.setUserProperty(key, value)
    }

    override fun setUserId(userId: String?) {
        firebase.setUserId(userId)
    }
}
```

---

## iOS implementation

```kotlin
// :core:analytics:data (iosMain) — IosAnalyticsImpl.kt
class IosAnalyticsImpl : Analytics {
    override fun track(event: AnalyticsEvent) {
        platform.FirebaseAnalytics.FIRAnalytics.logEventWithName(
            name       = event.name,
            parameters = event.params.mapKeys { it.key } as Map<Any?, *>,
        )
    }

    override fun setUserProperty(key: String, value: String) {
        platform.FirebaseAnalytics.FIRAnalytics.setUserPropertyString(value, forName = key)
    }

    override fun setUserId(userId: String?) {
        platform.FirebaseAnalytics.FIRAnalytics.setUserID(userId)
    }
}
```

---

## Desktop / no-op stub

```kotlin
// :core:analytics:data (desktopMain) — NoOpAnalytics.kt
class NoOpAnalytics : Analytics {
    override fun track(event: AnalyticsEvent) = Unit
    override fun setUserProperty(key: String, value: String) = Unit
    override fun setUserId(userId: String?) = Unit
}
```

---

## Koin wiring

```kotlin
// androidMain
val analyticsModule = module {
    single<Analytics> {
        FirebaseAnalyticsImpl(
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(get())
        )
    }
}

// iosMain
val analyticsModule = module {
    single<Analytics> { IosAnalyticsImpl() }
}

// desktopMain
val analyticsModule = module {
    single<Analytics> { NoOpAnalytics() }
}
```

Inject into ViewModels or use cases:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val analytics: Analytics,
) : ViewModel() {

    fun onIntent(intent: LoginContract.Intent) {
        when (intent) {
            is LoginContract.Intent.Submit -> {
                viewModelScope.launch {
                    val result = loginUseCase(intent.email, intent.password)
                    if (result.isSuccess) {
                        analytics.track(AnalyticsEvent.LoginSuccess(method = "email"))
                    }
                }
            }
        }
    }
}
```

---

## Automatic screen tracking

Wire screen tracking in the NavHost using `DisposableEffect`:

```kotlin
@Composable
fun AppNavHost(navController: NavHostController, analytics: Analytics = koinInject()) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    DisposableEffect(currentRoute) {
        currentRoute?.let {
            analytics.track(AnalyticsEvent.ScreenView(screenName = it))
        }
        onDispose { }
    }

    NavHost(navController, ...) { /* routes */ }
}
```

---

## Testing with FakeAnalytics

```kotlin
class FakeAnalytics : Analytics {
    val recorded = mutableListOf<AnalyticsEvent>()
    override fun track(event: AnalyticsEvent) { recorded.add(event) }
    override fun setUserProperty(key: String, value: String) = Unit
    override fun setUserId(userId: String?) = Unit
}

@Test fun `login success fires analytics event`() = runTest {
    val analytics = FakeAnalytics()
    val viewModel = LoginViewModel(FakeLoginUseCase(success = true), analytics)
    viewModel.onIntent(LoginContract.Intent.Submit("a@b.com", "pass"))
    assertTrue(analytics.recorded.any { it is AnalyticsEvent.LoginSuccess })
}
```

---

## Common Anti-Patterns

- **Platform SDK imports in feature code** — `import com.google.firebase.analytics.FirebaseAnalytics` in a ViewModel couples feature code to Android; always import only `Analytics` from `commonMain`
- **String event names scattered in feature code** — `analytics.track("login_success", ...)` with raw strings diverges across platforms; define all events as sealed subclasses
- **Tracking inside composables** — analytics calls belong in the ViewModel `onIntent` handler, not inside `@Composable` functions; composables recompose and would double-fire
- **No no-op implementation** — Desktop and test environments without a no-op crash; always provide a `NoOpAnalytics` and use it in tests and non-production targets

---

## Related Skills

- `kotlin-multiplatform-mvi` — analytics calls go in `onIntent` after state updates, not in composables
- `kotlin-multiplatform-dependency-injection` — platform `analyticsModule` wired in platform-specific Koin setup
- `kotlin-multiplatform-unit-testing` — `FakeAnalytics` is the canonical fake; test event names and params, not SDK calls

---

## Output Style

When implementing analytics, respond in this order:
1. **Event schema** — sealed `AnalyticsEvent` hierarchy for all events in scope
2. **Interface** — `Analytics` in `commonMain`
3. **Platform impls** — Android (Firebase), iOS (FIRAnalytics), Desktop (no-op)
4. **Koin wiring** — platform modules
5. **ViewModel integration** — where `analytics.track(...)` is called
6. **Screen tracking** — `DisposableEffect` in NavHost
7. **FakeAnalytics** — for unit tests

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
