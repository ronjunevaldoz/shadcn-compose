---
name: kotlin-multiplatform-in-app-purchases
description: >
  In-app purchases and subscriptions in KMP — shared domain model for purchase state,
  platform implementations via expect/actual, Play Billing (Android) and StoreKit 2 (iOS),
  entitlement verification, and the MVI integration pattern for gating premium features.
  Covers: one-time purchases, auto-renewing subscriptions, receipt validation, and restore
  purchases. Zero server required for basic validation; server-side validation guidance included.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
  keywords:
    - in-app purchases
    - IAP
    - subscriptions
    - Play Billing
    - StoreKit
    - StoreKit 2
    - Google Play
    - App Store
    - purchase
    - premium feature
    - entitlement
    - receipt validation
    - restore purchases
    - billing
    - paywall
    - Kotlin Multiplatform
    - KMP
---

## When to Use This Skill

Use when you need to:
- Add in-app purchases or subscriptions to a KMP app
- Gate premium features behind a purchase check
- Handle Play Billing (Android) and StoreKit 2 (iOS) with shared domain logic
- Restore purchases across devices or after reinstall
- Validate receipts client-side or server-side

**Trigger keywords:** in-app purchases, IAP, subscriptions, Play Billing, StoreKit,
Google Play billing, App Store purchase, premium feature, paywall, entitlement,
receipt validation, restore purchases, billing, purchase state, consumable, non-consumable,
auto-renewing subscription, one-time purchase, purchase flow, unlock premium.

**Freshness rule:** Play Billing Library and StoreKit 2 APIs change between major versions —
recheck Google Play Billing and Apple StoreKit release notes before upgrading.

---

## Recommendation First

Default to **`expect/actual` for platform purchase APIs + a shared domain model for purchase state**.

Why:
- Play Billing and StoreKit 2 have fundamentally different APIs — wrapping them behind
  a shared interface prevents leaking platform details into shared business logic
- The shared domain layer owns entitlement state as a `PurchaseState` sealed class —
  the ViewModel and UI gate features on this, not on platform-specific purchase objects
- Restore purchases must be triggered explicitly by user action (App Store guideline) —
  never restore automatically on app launch

Consider a third-party SDK (RevenueCat, Adapty) when:
- You need cross-platform receipt validation without a custom server
- You need A/B testing on paywalls or pricing experiments
- The team wants to skip maintaining platform-specific billing code

---

## Shared Domain Model

Define purchase state in `commonMain` — platform implementations map their native
types to these:

```kotlin
// :feature:billing:domain/src/commonMain/kotlin/.../PurchaseState.kt
sealed interface PurchaseState {
    data object Unknown : PurchaseState        // not yet loaded
    data object NotPurchased : PurchaseState   // confirmed not purchased
    data object Pending : PurchaseState        // payment processing
    data class Purchased(
        val productId: String,
        val purchaseToken: String,             // Play: token; StoreKit: original transaction id
        val expiresAt: Long?,                  // null for non-renewing / one-time
    ) : PurchaseState
    data class Error(val message: String) : PurchaseState
}

// Product catalog defined in shared code, IDs must match Play Console / App Store Connect
enum class ProductId(val id: String) {
    PREMIUM_MONTHLY("premium_monthly"),
    PREMIUM_YEARLY("premium_yearly"),
    EXTRA_STORAGE("extra_storage_onetime"),
}
```

---

## expect/actual Interface

```kotlin
// :feature:billing:api/src/commonMain/kotlin/.../BillingRepository.kt
interface BillingRepository {
    /** Observe the current purchase state for a product. */
    fun observePurchaseState(productId: ProductId): Flow<PurchaseState>

    /** Launch the purchase flow. Returns the resulting state. */
    suspend fun purchase(productId: ProductId): PurchaseState

    /** Restore purchases from the store (call on user action only). */
    suspend fun restorePurchases(): List<PurchaseState>
}
```

---

## Android — Play Billing

```toml
# gradle/libs.versions.toml
[versions]
billing = "7.1.1"

[libraries]
billing = { module = "com.android.billingclient:billing-ktx", version.ref = "billing" }
```

```kotlin
// :feature:billing:data/src/androidMain/kotlin/.../AndroidBillingRepository.kt
class AndroidBillingRepository(
    private val context: Context,
) : BillingRepository {

    private val client = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            // handled via queryPurchasesAsync or purchase listener
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Unknown)

    override fun observePurchaseState(productId: ProductId): Flow<PurchaseState> =
        _purchaseState.asStateFlow()

    override suspend fun purchase(productId: ProductId): PurchaseState {
        ensureConnected()

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId.id)
                .setProductType(BillingClient.ProductType.SUBS)  // or INAPP for one-time
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val (billingResult, productDetailsList) = client.queryProductDetails(params)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK ||
            productDetailsList.isNullOrEmpty()
        ) {
            return PurchaseState.Error("Product not found: ${billingResult.debugMessage}")
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetailsList.first())
                        .build()
                )
            )
            .build()

        val launchResult = client.launchBillingFlow(context as Activity, flowParams)
        // Result comes asynchronously via PurchasesUpdatedListener — wire via a Channel
        return _purchaseState.first { it !is PurchaseState.Unknown && it !is PurchaseState.Pending }
    }

    override suspend fun restorePurchases(): List<PurchaseState> {
        ensureConnected()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = client.queryPurchasesAsync(params)
        return result.purchasesList.map { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                PurchaseState.Purchased(
                    productId = purchase.products.firstOrNull().orEmpty(),
                    purchaseToken = purchase.purchaseToken,
                    expiresAt = null,  // resolve via server validation for subscriptions
                )
            } else {
                PurchaseState.Pending
            }
        }
    }

    private suspend fun ensureConnected() {
        if (!client.isReady) {
            suspendCancellableCoroutine { cont ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (result.responseCode == BillingClient.BillingResponseCode.OK)
                            cont.resume(Unit)
                        else
                            cont.cancel(IllegalStateException(result.debugMessage))
                    }
                    override fun onBillingServiceDisconnected() {}
                })
            }
        }
    }
}
```

---

## iOS — StoreKit 2

```kotlin
// :feature:billing:data/src/iosMain/kotlin/.../IosBillingRepository.kt
import platform.StoreKit.*
import kotlinx.coroutines.flow.*

class IosBillingRepository : BillingRepository {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Unknown)

    override fun observePurchaseState(productId: ProductId): Flow<PurchaseState> =
        _purchaseState.asStateFlow()

    override suspend fun purchase(productId: ProductId): PurchaseState {
        val products = try {
            Product.products(setOf(productId.id))
        } catch (e: Exception) {
            return PurchaseState.Error(e.message ?: "Failed to load products")
        }

        val product = products.firstOrNull()
            ?: return PurchaseState.Error("Product not found: ${productId.id}")

        val result = try {
            product.purchase()
        } catch (e: Exception) {
            return PurchaseState.Error(e.message ?: "Purchase failed")
        }

        return when (result) {
            is Product.PurchaseResult.Success -> {
                val tx = result.transaction
                PurchaseState.Purchased(
                    productId = productId.id,
                    purchaseToken = tx.originalID.toString(),
                    expiresAt = tx.expirationDate?.timeIntervalSince1970?.toLong()?.times(1000),
                )
            }
            is Product.PurchaseResult.Pending -> PurchaseState.Pending
            is Product.PurchaseResult.UserCancelled -> PurchaseState.NotPurchased
            else -> PurchaseState.Error("Unexpected result")
        }
    }

    override suspend fun restorePurchases(): List<PurchaseState> {
        AppStore.sync()
        val entitlements = Transaction.currentEntitlements
        val states = mutableListOf<PurchaseState>()
        for (result in entitlements) {
            val tx = (result as? VerificationResult.Verified)?.value ?: continue
            if (tx.revocationDate == null) {
                states += PurchaseState.Purchased(
                    productId = tx.productID,
                    purchaseToken = tx.originalID.toString(),
                    expiresAt = tx.expirationDate?.timeIntervalSince1970?.toLong()?.times(1000),
                )
            }
        }
        return states
    }
}
```

---

## MVI Integration — Gating Premium Features

```kotlin
// :feature:billing:presenter
object PremiumContract {
    data class State(
        val purchaseState: PurchaseState = PurchaseState.Unknown,
        val isLoading: Boolean = false,
    ) {
        val isPremium: Boolean get() = purchaseState is PurchaseState.Purchased
    }

    sealed interface Intent {
        data class CheckPurchase(val productId: ProductId) : Intent
        data class Purchase(val productId: ProductId) : Intent
        data object RestorePurchases : Intent
    }

    sealed interface Effect {
        data class ShowError(val message: String) : Effect
        data object PurchaseSuccess : Effect
    }
}

class PremiumViewModel(
    private val billing: BillingRepository,
) : MviViewModel<PremiumContract.State, PremiumContract.Intent, PremiumContract.Effect>(
    initialState = PremiumContract.State(),
) {
    override suspend fun handleIntent(intent: PremiumContract.Intent) {
        when (intent) {
            is PremiumContract.Intent.CheckPurchase -> {
                billing.observePurchaseState(intent.productId)
                    .collect { updateState { copy(purchaseState = it) } }
            }
            is PremiumContract.Intent.Purchase -> purchase(intent.productId)
            PremiumContract.Intent.RestorePurchases -> restore()
        }
    }

    private suspend fun purchase(productId: ProductId) {
        updateState { copy(isLoading = true) }
        val result = billing.purchase(productId)
        updateState { copy(isLoading = false, purchaseState = result) }
        if (result is PurchaseState.Purchased) sendEffect(PremiumContract.Effect.PurchaseSuccess)
        if (result is PurchaseState.Error) sendEffect(PremiumContract.Effect.ShowError(result.message))
    }

    private suspend fun restore() {
        updateState { copy(isLoading = true) }
        val results = billing.restorePurchases()
        val purchased = results.filterIsInstance<PurchaseState.Purchased>().firstOrNull()
        updateState { copy(isLoading = false, purchaseState = purchased ?: PurchaseState.NotPurchased) }
    }
}
```

---

## Server-Side Validation (when required)

For subscriptions, validate receipts server-side to prevent tampering:

| Platform | Validation endpoint | What to send |
|---|---|---|
| Android | Google Play Developer API `purchases.subscriptions.get` | `purchaseToken` + `productId` |
| iOS | App Store Server API `/inApps/v1/subscriptions/{originalTransactionId}` | `originalTransactionId` + signed JWT |

The server verifies:
- The purchase token is genuine (not fabricated)
- The subscription is active (not expired, not refunded)
- Returns the canonical `expiresAt` timestamp

Never trust the client-provided `expiresAt` for subscription gating without server confirmation.

---

## Testing

Use a `FakeBillingRepository` to test the ViewModel in isolation — no Play Billing or StoreKit
calls in unit tests:

```kotlin
// :core:testing/src/commonMain/kotlin/.../FakeBillingRepository.kt
class FakeBillingRepository(
    private val purchaseResult: PurchaseState = PurchaseState.Purchased(
        productId = ProductId.PREMIUM_MONTHLY.id,
        purchaseToken = "fake_token",
        expiresAt = null,
    ),
) : BillingRepository {

    var purchaseCalled = false
    var restoreCalled = false

    private val _state = MutableStateFlow<PurchaseState>(PurchaseState.Unknown)

    override fun observePurchaseState(productId: ProductId): Flow<PurchaseState> = _state.asStateFlow()

    override suspend fun purchase(productId: ProductId): PurchaseState {
        purchaseCalled = true
        _state.value = purchaseResult
        return purchaseResult
    }

    override suspend fun restorePurchases(): List<PurchaseState> {
        restoreCalled = true
        return listOf(purchaseResult)
    }
}

// ViewModel test
class PremiumViewModelTest {

    @Test
    fun `purchase success updates state and sends PurchaseSuccess effect`() = runTest {
        val vm = PremiumViewModel(FakeBillingRepository())

        val effects = mutableListOf<PremiumContract.Effect>()
        val job = launch { vm.effect.collect { effects.add(it) } }

        vm.state.test {
            awaitItem()  // initial Unknown
            vm.onIntent(PremiumContract.Intent.Purchase(ProductId.PREMIUM_MONTHLY))
            val loaded = awaitItem()
            assertTrue(loaded.isPremium)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(listOf(PremiumContract.Effect.PurchaseSuccess), effects)
        job.cancel()
    }

    @Test
    fun `restore purchases updates state to Purchased`() = runTest {
        val vm = PremiumViewModel(FakeBillingRepository())

        vm.state.test {
            awaitItem()  // initial
            vm.onIntent(PremiumContract.Intent.RestorePurchases)
            val restored = awaitItem()
            assertTrue(restored.isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `purchase error sends ShowError effect`() = runTest {
        val vm = PremiumViewModel(
            FakeBillingRepository(purchaseResult = PurchaseState.Error("Payment declined"))
        )

        val effects = mutableListOf<PremiumContract.Effect>()
        val job = launch { vm.effect.collect { effects.add(it) } }

        vm.onIntent(PremiumContract.Intent.Purchase(ProductId.PREMIUM_MONTHLY))
        advanceUntilIdle()

        assertTrue(effects.any { it is PremiumContract.Effect.ShowError })
        job.cancel()
    }
}
```

---

## Common Anti-Patterns

- restoring purchases automatically on app launch — App Store guideline 3.1.1 requires restore to be explicitly triggered by the user (a visible "Restore Purchases" button)
- gating features on platform-specific purchase objects instead of the shared `PurchaseState` — leaks platform details into the ViewModel
- not acknowledging purchases on Android — Play Billing will refund unacknowledged purchases within 3 days; call `acknowledgePurchase()` after a successful `PURCHASED` state
- trusting the client-provided expiry timestamp for subscription gating without server validation — trivially spoofed
- not handling `PurchaseState.Pending` — bank redirects and parental approval create pending states that can complete days later; listen for completion via `observePurchaseState`
- not testing with the sandbox environment — always use Play billing test accounts and StoreKit sandbox before submitting

---

## Related Skills

- `kotlin-multiplatform-expect-actual` — the `BillingRepository` implementation uses expect/actual per platform
- `kotlin-multiplatform-mvi` — `PremiumViewModel` follows the full MVI contract
- `kotlin-multiplatform-dependency-injection` — `BillingRepository` is a singleton bound per platform in Koin
- `kotlin-multiplatform-network-layer` — required for server-side receipt validation

---

## Output Style

When asked about in-app purchases, respond in this order:
1. Recommend the shared domain model approach (expect/actual + `PurchaseState`)
2. Show the shared `BillingRepository` interface
3. Show the platform implementation (Android or iOS, whichever is relevant)
4. Show the MVI ViewModel integration for gating features
5. Note server-side validation requirement for subscriptions

Keep platform-specific code clearly labeled. Never mix Play Billing and StoreKit APIs in one snippet.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Initial release. |
