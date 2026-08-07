# ViewModel — Config-Change-Proof Business State

Part of `kmp-compose-state-container`. Load this file when working on: viewmodel — config-change-proof business state.

---

A ViewModel survives configuration changes because Android holds it separately from the
Activity/Fragment. In KMP, `androidx.lifecycle.ViewModel` works across Android, Desktop,
and iOS (with lifecycle support from JetBrains).

```kotlin
// ✓ Correct uses of ViewModel
class ProductListViewModel(private val repo: ProductRepository) : ViewModel() {

    // Async data load — needs viewModelScope
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = repo.getProducts()
        }
    }
}

// ✓ Shared across screens (scoped to nav graph)
class CartViewModel : ViewModel() {
    val items = mutableStateListOf<CartItem>()
    fun addItem(item: CartItem) { items.add(item) }
}
```

```kotlin
// ❌ Wrong — ViewModel for pure ephemeral UI state
class SearchViewModel : ViewModel() {
    var isDropdownOpen by mutableStateOf(false)   // no business logic — belongs in remember
    var tooltipVisible by mutableStateOf(false)   // no business logic — belongs in remember
}
```

### ViewModel + SavedStateHandle (process-death survival)

```kotlin
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SearchRepository,
) : ViewModel() {

    // Automatically restored after process death
    var query by savedStateHandle.saveable { mutableStateOf("") }

    fun onQueryChanged(newQuery: String) {
        query = newQuery
        // launch search, etc.
    }
}
```

`savedStateHandle.saveable` is the ViewModel equivalent of `rememberSaveable`. Same size
limits apply — store IDs, not full objects.

---

