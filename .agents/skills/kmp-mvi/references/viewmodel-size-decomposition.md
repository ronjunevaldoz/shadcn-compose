# ViewModel Size and Decomposition

Part of `kmp-mvi`. Load this file when working on: viewmodel size and decomposition.

---

A ViewModel that grows beyond ~150 lines is a smell. Beyond 300 lines it is a violation —
the ViewModel has taken on responsibilities that belong elsewhere.

### God ViewModel symptoms

Stop and decompose when the ViewModel:
- calls more than two unrelated repositories directly
- has multiple `private suspend fun` blocks that each contain their own business logic branches
- mixes data fetching, validation, formatting, and navigation logic in `handleIntent`
- has a `State` data class with more than ~8 fields
- contains `if/else` or `when` chains that span more than 10–15 lines per branch

### Field count alone isn't the test — Divergent Change is

The ~8-field guideline above is a rough trigger, not the actual rule. A `State` with 8
*related* fields is normal and correct; a `State` with 3 *unrelated* fields can already be
a violation. The named smell (Fowler's *Refactoring* catalog, same family as Long
Parameter List/Primitive Obsession elsewhere in this collection) is **Divergent Change**:
one type gets modified for multiple unrelated reasons. A `State` mixing chat, project, and
session fields changes every time *any one* of those three unrelated concerns changes —
three different reasons to touch one type. The real question: **does every field belong to
one cohesive screen concern, or does the `State` span concerns that don't actually depend
on each other?**

```kotlin
// ✓ 4 fields, all part of ONE concern (search) — combining these is the whole point
// of MVI's Contract pattern, not a violation
data class SearchState(
    val query: String = "",
    val results: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val filters: SearchFilters = SearchFilters(),
)

// ❌ 3 fields, THREE unrelated concerns wearing one State — a "shell" ViewModel that
// grew past coordinating into owning chat business logic, project metadata, and
// session lifecycle all at once
data class ChatShellState(
    val messages: List<Message> = emptyList(),   // chat concern
    val projectName: String = "",                // project concern
    val sessionExpiresAt: Instant? = null,        // session concern
)
```

Litmus test: if you can name the `State` after a single noun that every field is *about*
(`SearchState`, `OrderState`) without stretching, it's cohesive. If describing what the
`State` holds needs "and" between unrelated nouns ("chat messages *and* project info *and*
session status"), split it — each concern gets its own `State`/ViewModel (`ChatViewModel`,
`ProjectSideBarViewModel`, a session-owning ViewModel), not one `State` holding all three.
This can't be mechanically checked — a field-count threshold can't tell "8 related fields"
from "8 unrelated ones" — so this is a review-time judgment call, same treatment as
`kmp-code-quality`'s Parameter Object regression.

### Decision table: what to extract

| Symptom | Extract to |
|---|---|
| `handleIntent` branch calls multiple repos in sequence | Use case (`operator fun invoke`) in `:domain` |
| Business rule lives inline (validate, calculate, transform) | Use case or domain function |
| Two unrelated screen sections share one ViewModel | Split into parent + child (shared ViewModel or separate) |
| State has a sub-group of fields that only change together | Nested data class in `State`, or separate ViewModel |
| The same logic appears in two different ViewModels | Use case extracted to `:domain`, shared via injection |

### Extracting a use case

Move logic out of the ViewModel when a `handleIntent` branch:
- calls two or more repositories
- contains branching business rules (eligibility, validation, rollback)
- is long enough to need its own test suite independent of UI state

```kotlin
// ❌ Before — business logic inline in ViewModel (god ViewModel symptom)
private suspend fun placeOrder() {
    val cart = cartRepo.getCart()
    if (cart.items.isEmpty()) {
        updateState { copy(error = "Cart is empty") }
        return
    }
    val inventory = inventoryRepo.check(cart.items)
    if (!inventory.allAvailable) {
        updateState { copy(error = "Some items out of stock") }
        return
    }
    val order = orderRepo.place(cart)
    updateState { copy(isLoading = false) }
    sendEffect(Effect.NavigateToConfirmation(order.id))
}

// ✓ After — use case owns the orchestration
class PlaceOrderUseCase(
    private val cartRepo: CartRepository,
    private val inventoryRepo: InventoryRepository,
    private val orderRepo: OrderRepository,
) {
    suspend operator fun invoke(): PlaceOrderResult {
        val cart = cartRepo.getCart()
        if (cart.items.isEmpty()) return PlaceOrderResult.EmptyCart
        val inventory = inventoryRepo.check(cart.items)
        if (!inventory.allAvailable) return PlaceOrderResult.OutOfStock(inventory.unavailable)
        val order = orderRepo.place(cart)
        return PlaceOrderResult.Success(order.id)
    }
}

// ViewModel is now thin — one call, one when
private suspend fun placeOrder() {
    updateState { copy(isLoading = true) }
    when (val result = placeOrderUseCase()) {
        is PlaceOrderResult.Success      -> sendEffect(Effect.NavigateToConfirmation(result.orderId))
        PlaceOrderResult.EmptyCart       -> updateState { copy(isLoading = false, error = "Cart is empty") }
        is PlaceOrderResult.OutOfStock   -> updateState { copy(isLoading = false, error = "Some items out of stock") }
    }
    updateState { copy(isLoading = false) }
}
```

### Splitting a ViewModel

Split into two ViewModels when the screen has two genuinely independent sections —
different data sources, different lifecycles, no shared state between them.

```kotlin
// ✓ Profile screen with independent "user info" and "activity feed" sections
// Each has its own loading state, error state, and data source

class ProfileInfoViewModel(private val userRepo: UserRepository) :
    MviViewModel<ProfileInfoContract.State, ...>(...) { ... }

class ActivityFeedViewModel(private val feedRepo: FeedRepository) :
    MviViewModel<ActivityFeedContract.State, ...>(...) { ... }

@Composable
fun ProfileScreen(...) {
    val infoVm: ProfileInfoViewModel = koinViewModel()
    val feedVm: ActivityFeedViewModel = koinViewModel()
    // each section gets its own ViewModel, no shared ViewModel needed
}
```

Split into a **shared (parent) ViewModel** only when sections share mutable state and
must stay synchronized — see the Shared ViewModel section above for the pattern.

---

