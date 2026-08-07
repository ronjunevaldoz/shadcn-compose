# rememberSaveable {} — Rotation-Proof Local State

Part of `kmp-compose-state-container`. Load this file when working on: remembersaveable {} — rotation-proof local state.

---

`rememberSaveable` writes the value to a `Bundle` on config change and restores it.
Works automatically for Bundle-safe types: `Boolean`, `Int`, `Long`, `Float`, `Double`,
`String`, and anything `@Parcelize`/`Serializable`.

```kotlin
// ✓ Form that survives rotation
@Composable
fun SearchScreen() {
    var query by rememberSaveable { mutableStateOf("") }   // survives rotation

    Column {
        AppTextField(value = query, onValueChange = { query = it })
        AppButton(onClick = { performSearch(query) }) { AppText("Search") }
    }
}
```

### Custom Saver for non-Bundle types

When the type isn't Bundle-safe, write a `Saver`:

```kotlin
data class FilterState(
    val category: String?,
    val priceRange: IntRange,
    val sortOrder: SortOrder,
)

val FilterStateSaver = Saver<FilterState, Map<String, Any>>(
    save = { state ->
        mapOf(
            "category"   to (state.category ?: ""),
            "priceMin"   to state.priceRange.first,
            "priceMax"   to state.priceRange.last,
            "sortOrder"  to state.sortOrder.name,
        )
    },
    restore = { map ->
        FilterState(
            category   = (map["category"] as String).ifEmpty { null },
            priceRange = (map["priceMin"] as Int)..(map["priceMax"] as Int),
            sortOrder  = SortOrder.valueOf(map["sortOrder"] as String),
        )
    },
)

// Usage
var filterState by rememberSaveable(stateSaver = FilterStateSaver) {
    mutableStateOf(FilterState(category = null, priceRange = 0..1000, sortOrder = SortOrder.Newest))
}
```

**Limits of `rememberSaveable`:** Bundles have a size cap (~1 MB total). Don't store lists
of items, images, or large collections — use a ViewModel with `SavedStateHandle` for those
(store only the IDs, reload the data from repository).

---

