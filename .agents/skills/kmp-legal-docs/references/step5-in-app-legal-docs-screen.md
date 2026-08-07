# Step 5: In-App Legal Docs Screen

Part of `kmp-legal-docs`. Load this file when working on: step 5: in-app legal docs screen.

---

Add a `LegalDocsScreen` to `:core:ui` or `:feature:settings:ui`. The screen loads the
documents from a remote URL (so they can be updated without an app release) with an
embedded fallback.

### `LegalDocsScreen.kt` — in `:feature:settings:ui` or `:core:ui`

```kotlin
enum class LegalDocType { PRIVACY_POLICY, TERMS_AND_CONDITIONS }

@Composable
fun LegalDocsScreen(
    docType: LegalDocType,
    onBack: () -> Unit,
) {
    val title = when (docType) {
        LegalDocType.PRIVACY_POLICY        -> "Privacy Policy"
        LegalDocType.TERMS_AND_CONDITIONS  -> "Terms & Conditions"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LegalDocsContent(
            docType = docType,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun LegalDocsContent(
    docType: LegalDocType,
    modifier: Modifier = Modifier,
) {
    // Load from remote URL; fall back to embedded markdown
    val url = when (docType) {
        LegalDocType.PRIVACY_POLICY       -> AppConfig.privacyPolicyUrl
        LegalDocType.TERMS_AND_CONDITIONS -> AppConfig.termsUrl
    }

    // Use expect/actual WebView on each platform, or a simple scrollable Text for simple docs
    PlatformWebView(url = url, modifier = modifier.fillMaxSize())
}
```

### `AppConfig` additions

```kotlin
// Add to AppConfig in commonMain:
object AppConfig {
    // ... existing fields ...
    val privacyPolicyUrl: String  get() = BuildKonfig.PRIVACY_POLICY_URL
    val termsUrl: String          get() = BuildKonfig.TERMS_URL
}
```

### `gradle.properties` additions

```properties
PRIVACY_POLICY_URL=https://example.com/privacy
TERMS_URL=https://example.com/terms
```

### `buildkonfig {}` additions

```kotlin
defaultConfigs {
    // ... existing fields ...
    buildConfigField(STRING, "PRIVACY_POLICY_URL", project.property("PRIVACY_POLICY_URL") as String)
    buildConfigField(STRING, "TERMS_URL", project.property("TERMS_URL") as String)
}
```

### First-launch consent gate

**What it is:** a "Before you continue" screen shown to the user before they can use the app. It displays links to both legal documents and has a single "I agree — Continue" button.

**Why version-pin it:** The naïve approach stores a `true/false` flag — shown once, never again. The problem: when you update your Privacy Policy (e.g. you add analytics), existing users who already accepted the old policy need to see and accept the new one. If you store only `true`, you have no way to re-show the screen.

The solution: store the *version string of the policy they accepted* (e.g. `"1.2"`). At launch, compare it against the current policy version from `BuildKonfig`. If they match → skip. If they differ → show the gate again. Users only see it when the policy actually changes, not on every launch.

```
First launch:          stored = ""       current = "1.0"  → show gate
After accept:          stored = "1.0"    current = "1.0"  → skip
After policy update:   stored = "1.0"    current = "1.1"  → show gate again
After re-accept:       stored = "1.1"    current = "1.1"  → skip
```

**`gradle.properties`** — add the policy version alongside the URLs:
```properties
PRIVACY_POLICY_URL=https://example.com/privacy
TERMS_URL=https://example.com/terms
POLICY_VERSION=1.0
```

**`buildkonfig {}`** additions:
```kotlin
buildConfigField(STRING, "POLICY_VERSION", project.property("POLICY_VERSION") as String)
```

**`AppConfig`** additions:
```kotlin
val privacyPolicyVersion: String get() = BuildKonfig.POLICY_VERSION
```

**`ConsentScreen.kt`:**

```kotlin
@Composable
fun ConsentScreen(
    onAccept: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPrivacy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Before you continue", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "By using this app you agree to our Terms & Conditions and Privacy Policy.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            Row {
                TextButton(onClick = onViewTerms)   { Text("Terms & Conditions") }
                Text("  and  ", modifier = Modifier.align(Alignment.CenterVertically))
                TextButton(onClick = onViewPrivacy) { Text("Privacy Policy") }
            }
        }

        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("I agree — Continue")
        }
    }
}
```

**`ConsentViewModel.kt`** — checks whether the gate should show, and records acceptance:

```kotlin
class ConsentViewModel(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val POLICY_VERSION_KEY = stringPreferencesKey("accepted_policy_version")

    val shouldShowConsent: StateFlow<Boolean> = dataStore.data
        .map { prefs ->
            val accepted = prefs[POLICY_VERSION_KEY] ?: ""
            accepted != AppConfig.privacyPolicyVersion
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun acceptPolicy() {
        viewModelScope.launch {
            dataStore.edit { it[POLICY_VERSION_KEY] = AppConfig.privacyPolicyVersion }
        }
    }
}
```

**When to bump `POLICY_VERSION`:** any time the policy changes in a way that is material to the user (new data collected, new third-party shared with, new jurisdiction section). Typo fixes do not need a bump.

---

