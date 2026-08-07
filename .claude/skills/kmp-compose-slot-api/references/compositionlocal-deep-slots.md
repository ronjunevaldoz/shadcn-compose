# CompositionLocal: The Alternative for Deep Slots

Part of `kmp-compose-slot-api`. Load this file when working on: compositionlocal: the alternative for deep slots.

---

When you need the same value available many levels deep without threading it through
every intermediate composable, use `CompositionLocal` instead of slot parameters or
Koin injection.

```kotlin
// Problem: need theme colors 5 levels deep — slot threading becomes prop drilling
@Composable
fun AppTheme(
    theme: AppThemeData,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppTheme provides theme) {
        content()
    }
}

val LocalAppTheme = compositionLocalOf { lightTheme() }

// Any descendant can access without parameter threading
@Composable
fun SomeDeepComponent() {
    val colors = LocalAppTheme.current.colors    // no parameter needed
    Box(modifier = Modifier.background(colors.primary)) { ... }
}
```

**Rule: slots vs CompositionLocal**

| Use slots when | Use CompositionLocal when |
|---|---|
| Content is positional (goes in a specific layout region) | Value needs to be available to an arbitrary subtree |
| Caller customizes for a specific instance of a component | All descendants share the same value (theme, locale, toast host) |
| The component has 1–4 distinct content regions | Threading through 3+ layers becomes unreadable |

CompositionLocal is not a global variable — it's scoped to the subtree under the
`CompositionLocalProvider`. Providers nest cleanly:

```kotlin
AppTheme(theme = darkTheme) {         // dark theme for everything inside
    AppScaffold(...) {
        AppTheme(theme = lightTheme) { // light theme override for this subtree only
            SpecialDialog()
        }
    }
}
```

### `compositionLocalOf` vs `staticCompositionLocalOf`

This choice controls how Compose handles value changes:

| | `compositionLocalOf` | `staticCompositionLocalOf` |
|---|---|---|
| Value can change at runtime | Yes — only consumers recompose | No — **entire subtree recomposes** |
| Use for | Theme, locale, toast host, user preferences | Values set once at startup and never changed (e.g., platform type, screen density) |
| Default factory required | Yes — called when no Provider is found | Yes — should `error("no provider")` if the value is always provided |

```kotlin
// ✓ Changes at runtime (dark/light switch) → compositionLocalOf
val LocalAppTheme = compositionLocalOf<AppThemeData> { lightTheme() }

// ✓ Never changes after app start → staticCompositionLocalOf
val LocalPlatform = staticCompositionLocalOf<Platform> {
    error("LocalPlatform must be provided at the root")
}
```

### Cross-cutting concerns via CompositionLocal

Use CompositionLocal for composition-scoped services that many unrelated composables need
without a shared parent (toast hosts, snackbar state, analytics trackers, in-app review):

```kotlin
// Toast host — provided at the Scaffold level, consumed anywhere below it
val LocalToastHostState = compositionLocalOf<ToastHostState> {
    error("LocalToastHostState must be provided")
}

@Composable
fun AppScaffold(content: @Composable (PaddingValues) -> Unit) {
    val toastHostState = remember { ToastHostState() }
    CompositionLocalProvider(LocalToastHostState provides toastHostState) {
        Scaffold(
            snackbarHost = { ToastHost(toastHostState) },
            content = content,
        )
    }
}

// Any composable inside AppScaffold — no parameter threading needed
@Composable
fun SomeScreen() {
    val toast = LocalToastHostState.current
    Button(onClick = { toast.show("Saved!") }) { ... }
}
```

### When NOT to use CompositionLocal

| Do not use CompositionLocal for | Why | Use instead |
|---|---|---|
| Business logic services (repositories, use cases) | Composition-scoped DI bypasses testability; ViewModel lifecycle is separate from Compose | Koin `koinInject()` or ViewModel |
| Values only needed 1–2 levels deep | Parameter threading is clearer at this depth | Explicit function parameters |
| Navigation (passing NavController down) | NavController is not a composition-scoped value — it lives in `AppNavHost` | Navigation lambdas or `AppNavigator` |
| ViewModel instances | ViewModels have their own lifecycle; use `koinViewModel()` | `koinViewModel()` |

### Testing with CompositionLocal

Override any `CompositionLocal` in your test's `setContent`:

```kotlin
@Test
fun `toast shows on save success`() {
    val fakeToast = FakeToastHostState()
    composeTestRule.setContent {
        CompositionLocalProvider(LocalToastHostState provides fakeToast) {
            SomeScreen()
        }
    }
    composeTestRule.onNodeWithText("Save").performClick()
    assertEquals("Saved!", fakeToast.lastMessage)
}
```

This is the main testability advantage of `CompositionLocal` over threading a parameter
through every composable — you override at the root of the test, not in every caller.

---

