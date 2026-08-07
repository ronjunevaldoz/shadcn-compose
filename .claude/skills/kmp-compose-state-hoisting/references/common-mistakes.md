# Common Mistakes

Part of `kmp-compose-state-hoisting`. Load this file when working on: common mistakes.

---

**1. Passing state down without hoisting it up first**

If you find yourself passing the same state value through 3+ composables to reach a
deep consumer, you have a hoisting gap. Either hoist to a shared ancestor or use
`CompositionLocal` (see `kmp-compose-slot-api`).

**2. Hoisting state but keeping the write in a child**

If the parent holds the state but a deep child calls a callback that directly mutates
a shared object, the parent's state is stale. State and its mutation logic must travel
together — hoist both.

**3. Using `mutableStateOf` outside a composable without `remember`**

```kotlin
// ❌ New MutableState on every recomposition — state is lost immediately
@Composable
fun Counter() {
    val count = mutableStateOf(0)   // recreated every frame!
    AppButton(onClick = { count.value++ }) { AppText("${count.value}") }
}

// ✓ Remembered — survives recompositions
@Composable
fun Counter() {
    val count by remember { mutableStateOf(0) }
    AppButton(onClick = { count++ }) { AppText("$count") }
}
```

**4. Duplicating state (two sources of truth)**

```kotlin
// ❌ Two sources of truth — which one is correct?
class SearchViewModel : ViewModel() {
    var query by mutableStateOf("")
}

@Composable
fun SearchBar(viewModel: SearchViewModel) {
    var localQuery by remember { mutableStateOf(viewModel.query) }   // copy!
    TextField(value = localQuery, onValueChange = {
        localQuery = it           // updates local copy
        viewModel.query = it      // also updates ViewModel — duplication
    })
}

// ✓ Single source of truth — ViewModel owns it
@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    TextField(value = query, onValueChange = onQueryChanged)
}
```

---

