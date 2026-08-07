# The Hoist-Until-Shared Rule in Practice

Part of `kmp-compose-state-hoisting`. Load this file when working on: the hoist-until-shared rule in practice.

---

### Level 1: State local to one composable

State that only one composable reads and writes — leave it there:

```kotlin
@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }   // only this composable cares

    Column {
        Row(modifier = Modifier.clickable { expanded = !expanded }) {
            AppText(title)
            AppIcon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore)
        }
        if (expanded) content()
    }
}
```

No reason to hoist — no other composable needs `expanded`.

### Level 2: Hoist to parent when siblings share state

```kotlin
// ❌ Each tab owns its selected state — can't coordinate
@Composable
fun TabRow() {
    Tab1()   // has its own selected state internally
    Tab2()   // has its own selected state internally
    // how do we know which tab is selected to show its content?
}

// ✓ Hoist to parent — parent can show the right content
@Composable
fun TabsWithContent() {
    var selectedTab by remember { mutableStateOf(0) }

    Column {
        AppTabs(
            tabs = listOf("Overview", "Activity"),
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )
        when (selectedTab) {
            0 -> OverviewContent()
            1 -> ActivityContent()
        }
    }
}
```

### Level 3: Hoist to ViewModel when state needs async, persistence, or cross-screen sharing

```kotlin
// Form data that survives navigation, or must be validated against a repository
class ProfileViewModel(private val repo: ProfileRepository) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onSave() {
        viewModelScope.launch {
            repo.updateProfile(_state.value)
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileForm(
        name = state.name,
        onNameChanged = viewModel::onNameChanged,
        onSave = viewModel::onSave,
    )
}
```

---

