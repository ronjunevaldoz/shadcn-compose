---
name: kotlin-multiplatform-feature-flags
description: >-
  Feature flag evaluation for Kotlin Multiplatform — a FeatureFlag enum and
  FeatureFlagProvider interface in commonMain, Firebase Remote Config as the
  default backend, A/B variant types, offline fallback defaults, flag evaluation
  in the :domain layer, and kill-switch support for fast disablement.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - feature flags
    - remote config
    - Firebase Remote Config
    - feature toggle
    - A/B testing
    - kill switch
    - flag evaluation
    - FeatureFlagProvider
    - FeatureFlag
    - offline fallback
    - experiment
    - variant
    - KMP feature flags
    - LaunchDarkly KMP
---

## When to Use This Skill

Use when:
- Enabling or disabling a feature without a new app release
- Running an A/B experiment where users see variant A or B
- Adding a kill switch to roll back a buggy feature from the server
- Controlling feature rollout percentage (0–100%)
- Deferring a feature to a date-based or user-segment condition

**Trigger keywords:** feature flags, feature toggle, remote config, Firebase Remote Config,
A/B test, experiment, kill switch, flag evaluation, FeatureFlagProvider, variant,
rollout, LaunchDarkly KMP, feature flag KMP, flag provider, remote feature flag.

**Freshness rule:** Firebase Remote Config fetch interval is 12 hours in production;
call `activate()` after `fetchAndActivate()` to apply fetched values in the current session.
For LaunchDarkly or other providers, check the Kotlin SDK's KMP support status — as of 2026
LaunchDarkly's Kotlin SDK is JVM/Android only, not KMP; use a thin `expect/actual` wrapper
if targeting iOS natively.

---

## Recommendation First

Define flags as a `FeatureFlag` enum in `commonMain` with typed defaults. Evaluation happens
in the `:domain` layer via `FeatureFlagProvider` — never read flags directly from a composable
or ViewModel. This keeps flag-gated logic testable with a fake provider and decouples feature
code from the backend (Firebase, LaunchDarkly, local overrides).

---

## Feature flag definitions — commonMain

```kotlin
// :core:flags:model — FeatureFlag.kt
enum class FeatureFlag(
    val key: String,
    val defaultValue: FlagValue,
) {
    NewCheckoutFlow("new_checkout_flow", FlagValue.Bool(false)),
    ChatSupport("chat_support_enabled", FlagValue.Bool(false)),
    MaxItemsInCart("max_cart_items", FlagValue.Int(10)),
    WelcomeBannerVariant("welcome_banner_variant", FlagValue.String("control")),
    PricingTierEnabled("pricing_tier", FlagValue.Bool(false)),
}

sealed class FlagValue {
    data class Bool(val default: Boolean)      : FlagValue()
    data class Int(val default: kotlin.Int)    : FlagValue()
    data class String(val default: kotlin.String) : FlagValue()
    data class Double(val default: kotlin.Double) : FlagValue()
}
```

---

## FeatureFlagProvider interface — commonMain

```kotlin
// :core:flags:api — FeatureFlagProvider.kt
interface FeatureFlagProvider {
    suspend fun fetchAndActivate()
    fun getBoolean(flag: FeatureFlag): Boolean
    fun getInt(flag: FeatureFlag): Int
    fun getString(flag: FeatureFlag): String
    fun getDouble(flag: FeatureFlag): Double
}

// Typed extension helpers
fun FeatureFlagProvider.isEnabled(flag: FeatureFlag): Boolean = getBoolean(flag)
fun FeatureFlagProvider.variant(flag: FeatureFlag): String = getString(flag)
```

---

## Firebase Remote Config implementation — Android

```kotlin
// :core:flags:data (androidMain) — FirebaseFeatureFlagProvider.kt
class FirebaseFeatureFlagProvider : FeatureFlagProvider {

    private val remoteConfig = Firebase.remoteConfig.apply {
        val defaults = FeatureFlag.values().associate { flag ->
            flag.key to when (val d = flag.defaultValue) {
                is FlagValue.Bool   -> d.default
                is FlagValue.Int    -> d.default.toLong()
                is FlagValue.String -> d.default
                is FlagValue.Double -> d.default
            }
        }
        setDefaultsAsync(defaults)
        setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3600L
            }
        )
    }

    override suspend fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().await()
    }

    override fun getBoolean(flag: FeatureFlag): Boolean =
        remoteConfig.getBoolean(flag.key)

    override fun getInt(flag: FeatureFlag): Int =
        remoteConfig.getLong(flag.key).toInt()

    override fun getString(flag: FeatureFlag): String =
        remoteConfig.getString(flag.key)

    override fun getDouble(flag: FeatureFlag): Double =
        remoteConfig.getDouble(flag.key)
}
```

---

## No-op / hardcoded provider for iOS and Desktop

```kotlin
// commonMain or iosMain — DefaultsFeatureFlagProvider.kt
class DefaultsFeatureFlagProvider : FeatureFlagProvider {

    override suspend fun fetchAndActivate() = Unit  // no remote source

    override fun getBoolean(flag: FeatureFlag): Boolean =
        (flag.defaultValue as? FlagValue.Bool)?.default ?: false

    override fun getInt(flag: FeatureFlag): Int =
        (flag.defaultValue as? FlagValue.Int)?.default ?: 0

    override fun getString(flag: FeatureFlag): String =
        (flag.defaultValue as? FlagValue.String)?.default ?: ""

    override fun getDouble(flag: FeatureFlag): Double =
        (flag.defaultValue as? FlagValue.Double)?.default ?: 0.0
}
```

---

## Flag evaluation in use cases — never in composables

```kotlin
// :domain — CheckoutUseCase.kt
class CheckoutUseCase(
    private val flags: FeatureFlagProvider,
    private val cartRepository: CartRepository,
    private val checkoutRepository: CheckoutRepository,
) {
    suspend operator fun invoke(cartId: String): Result<Order> {
        return if (flags.isEnabled(FeatureFlag.NewCheckoutFlow)) {
            checkoutRepository.checkoutV2(cartId)
        } else {
            checkoutRepository.checkoutV1(cartId)
        }
    }
}
```

---

## A/B variant example

```kotlin
// :presenter — HomeViewModel.kt
class HomeViewModel(
    private val flags: FeatureFlagProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            flags.fetchAndActivate()
            val variant = flags.variant(FeatureFlag.WelcomeBannerVariant)
            _state.update { it.copy(welcomeVariant = variant) }
        }
    }
}

// In UiState and composable:
// variant == "control" → show original banner
// variant == "treatment_a" → show new banner design
```

---

## Kill switch pattern

```kotlin
// :domain — ChatFeature.kt
class OpenChatUseCase(private val flags: FeatureFlagProvider) {
    operator fun invoke(): Result<Unit> {
        if (!flags.isEnabled(FeatureFlag.ChatSupport)) {
            return Result.failure(FeatureDisabledException("Chat is temporarily unavailable"))
        }
        // ... open chat
        return Result.success(Unit)
    }
}
```

---

## App startup — fetch on launch

```kotlin
// In Application.onCreate (Android) or KoinApplication initializer:
scope.launch {
    get<FeatureFlagProvider>().fetchAndActivate()
}
```

---

## Koin wiring

```kotlin
// androidMain
val featureFlagsModule = module {
    single<FeatureFlagProvider> { FirebaseFeatureFlagProvider() }
}

// iosMain / desktopMain
val featureFlagsModule = module {
    single<FeatureFlagProvider> { DefaultsFeatureFlagProvider() }
}
```

---

## Testing with FakeFeatureFlagProvider

```kotlin
class FakeFeatureFlagProvider(
    private val flags: Map<FeatureFlag, Any> = emptyMap(),
) : FeatureFlagProvider {
    override suspend fun fetchAndActivate() = Unit
    override fun getBoolean(flag: FeatureFlag) =
        (flags[flag] as? Boolean) ?: (flag.defaultValue as? FlagValue.Bool)?.default ?: false
    override fun getInt(flag: FeatureFlag) =
        (flags[flag] as? Int) ?: (flag.defaultValue as? FlagValue.Int)?.default ?: 0
    override fun getString(flag: FeatureFlag) =
        (flags[flag] as? String) ?: (flag.defaultValue as? FlagValue.String)?.default ?: ""
    override fun getDouble(flag: FeatureFlag) =
        (flags[flag] as? Double) ?: (flag.defaultValue as? FlagValue.Double)?.default ?: 0.0
}

@Test fun `checkout uses v2 when flag is enabled`() = runTest {
    val flags = FakeFeatureFlagProvider(mapOf(FeatureFlag.NewCheckoutFlow to true))
    val useCase = CheckoutUseCase(flags, FakeCartRepository(), FakeCheckoutRepository())
    useCase.invoke("cart-123")
    assertTrue(FakeCheckoutRepository.v2WasCalled)
}
```

---

## Common Anti-Patterns

- **Reading flags in composables** — `if (flags.isEnabled(FeatureFlag.X))` in a `@Composable`
  bypasses the ViewModel and cannot be tested; evaluate flags in use cases or ViewModels and
  pass the result as part of `UiState`
- **No offline fallback** — if `fetchAndActivate()` fails silently, code using the flag
  should fall back to the hardcoded default in `FeatureFlag.defaultValue`; never crash
- **Adding flag keys as raw strings** — `remoteConfig.getBoolean("new_checkout_flow")` in
  feature code creates hidden coupling; always read via the `FeatureFlag` enum
- **Fetching on every ViewModel init** — `fetchAndActivate()` should be called once on
  app startup and stored; fetching per ViewModel adds unnecessary latency and Firebase quota
- **Not cleaning up stale flags** — flags that are 100% enabled and will never roll back
  should be removed from the enum, the remote config, and all call sites; stale flags
  accumulate and make the code harder to reason about

---

## Related Skills

- `kotlin-multiplatform-analytics` — log which variant a user sees immediately after
  `fetchAndActivate()` so the analytics team can correlate behavior with experiment buckets
- `kotlin-multiplatform-dependency-injection` — `FeatureFlagProvider` is a singleton in
  the Koin module; it is injected into use cases and ViewModels via constructor
- `kotlin-multiplatform-unit-testing` — `FakeFeatureFlagProvider` allows testing both
  branches of any flag-gated use case without a Firebase connection

---

## Output Style

When implementing feature flags, respond in this order:
1. **FeatureFlag enum** — key + typed default for each flag in scope
2. **FeatureFlagProvider interface** — standard methods
3. **Firebase implementation** — Android; defaults-only stub for iOS/Desktop
4. **Flag evaluation** — use case or ViewModel that branches on the flag
5. **Startup fetch** — `fetchAndActivate()` on app launch
6. **Koin wiring** — platform modules
7. **FakeFeatureFlagProvider + test** — both branches covered

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
