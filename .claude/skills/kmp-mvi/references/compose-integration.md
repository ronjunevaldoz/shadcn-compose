# Compose Integration: Screen / Content Split

Part of `kmp-mvi`. Load this file when working on: compose integration: screen / content split.

---

Split every screen into two composables:

- **`AuthScreen`** — wired to ViewModel, handles navigation, collects effects.
  No preview annotation.
- **`AuthContent`** — pure composable, receives `state` + `onIntent` lambda.
  Fully previewable and testable without a ViewModel.

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthScreen.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import GROUP_ID.core.designsystem.components.LocalToastHostState
import GROUP_ID.core.designsystem.components.ToastVariant
import org.koin.compose.viewmodel.koinViewModel

/**
 * Wired screen — owns navigation and side-effect handling.
 * Never use this in Compose @Preview.
 */
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toast = LocalToastHostState.current

    // Collect effects exactly once, scoped to this composable's lifecycle
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthContract.Effect.NavigateToHome ->
                    onNavigateToHome()

                is AuthContract.Effect.NavigateToForgotPassword ->
                    onNavigateToForgotPassword()

                is AuthContract.Effect.ShowError ->
                    toast.show(effect.message, variant = ToastVariant.Destructive)
            }
        }
    }

    AuthContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
```

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthContent.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.components.AppTextField
import GROUP_ID.core.designsystem.components.AppSpinner
import GROUP_ID.core.designsystem.styles.ButtonVariant
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pure composable — no ViewModel dependency, fully previewable.
 */
@Composable
fun AuthContent(
    state: AuthContract.State,
    onIntent: (AuthContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        AppTextField(
            value = state.email,
            onValueChange = { onIntent(AuthContract.Intent.EmailChanged(it)) },
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        AppTextField(
            value = state.password,
            onValueChange = { onIntent(AuthContract.Intent.PasswordChanged(it)) },
            placeholder = "Password",
            isPassword = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            AppText(text = state.error, style = TextStyle.BodySmall, color = colors.destructive)
        }

        Spacer(Modifier.height(24.dp))

        AppButton(
            onClick = { onIntent(AuthContract.Intent.LoginClicked) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) AppSpinner(color = colors.onPrimary)
            else AppText("Sign in")
        }

        Spacer(Modifier.height(8.dp))

        AppButton(
            onClick = { onIntent(AuthContract.Intent.ForgotPasswordClicked) },
            variant = ButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppText("Forgot password?")
        }
    }
}

@Preview
@Composable
private fun AuthContentPreview() {
    AuthContent(state = AuthContract.State(), onIntent = {})
}

@Preview
@Composable
private fun AuthContentLoadingPreview() {
    AuthContent(state = AuthContract.State(isLoading = true), onIntent = {})
}

@Preview
@Composable
private fun AuthContentErrorPreview() {
    AuthContent(
        state = AuthContract.State(error = "Invalid credentials"),
        onIntent = {},
    )
}
```

### `collectAsStateWithLifecycle` vs `collectAsState`

Always use `collectAsStateWithLifecycle()` in production screens. It pauses collection
when the composable's lifecycle drops below `STARTED` (screen goes to background) —
saving battery and stopping unnecessary work.

```kotlin
// ❌ collectAsState — keeps collecting even when the screen is in the background
val state by viewModel.state.collectAsState()

// ✓ collectAsStateWithLifecycle — pauses when lifecycle < STARTED
val state by viewModel.state.collectAsStateWithLifecycle()
```

| | `collectAsState` | `collectAsStateWithLifecycle` |
|---|---|---|
| Lifecycle-aware | No — always active | Yes — pauses below `STARTED` |
| Battery / CPU | Wastes work in background | Efficient |
| Use in | `@Preview`, tests | Production screens |
| Import | `androidx.compose.runtime` | `androidx.lifecycle.compose` |

Exception: inside `@Preview` composables there is no lifecycle, so `collectAsState` is
required. Never use `collectAsStateWithLifecycle` in a preview — it throws at preview time.

---

### `LaunchedEffect` vs `DisposableEffect` vs `SideEffect`

| Effect API | When it runs | Has cleanup? | Use for |
|---|---|---|---|
| `LaunchedEffect(key)` | On entry + when key changes; cancels on exit or key change | No (cancel is implicit) | Collecting flows, one-shot coroutines, side-effect on key change |
| `DisposableEffect(key)` | Synchronously on entry + when key changes; `onDispose` on exit | Yes — `onDispose {}` | Add/remove listeners, set/clear a holder, subscribe/unsubscribe resources |
| `SideEffect` | After **every** successful recomposition; no key | No | Sync Compose state to non-Compose code (analytics screen name, system UI flags) |

```kotlin
// LaunchedEffect — collect effects from ViewModel (coroutine, cancels when composable exits)
LaunchedEffect(viewModel) {
    viewModel.effect.collect { effect -> handleEffect(effect) }
}

// DisposableEffect — set/clear NavControllerHolder (synchronous, cleanup guaranteed)
DisposableEffect(navController) {
    holder.current = navController
    onDispose { holder.current = null }
}

// SideEffect — push current screen name to analytics after every recomposition
SideEffect {
    analytics.setCurrentScreen(screenName)
}
```

**Choosing between them:**
1. If you need a coroutine → `LaunchedEffect`
2. If you need guaranteed cleanup (listener, holder, resource) → `DisposableEffect`
3. If you need to push state out to non-Compose code on every frame → `SideEffect`

---

### `rememberUpdatedState` — latest lambda without restarting the effect

When a `LaunchedEffect` captures a lambda (callback from a parent) that might change
between recompositions, the effect has two bad options:
- use the lambda as the key → effect restarts on every callback change (defeats the point)
- ignore the change → effect calls a stale lambda

`rememberUpdatedState` solves both: it gives the effect a **stable reference** that always
delegates to the latest value, without restarting the coroutine.

```kotlin
@Composable
fun AutoSavingTimer(onAutoSave: () -> Unit) {
    // ✓ Always reads the latest onAutoSave without restarting the LaunchedEffect
    val currentOnAutoSave by rememberUpdatedState(onAutoSave)

    LaunchedEffect(Unit) {   // key = Unit — this coroutine never restarts
        while (true) {
            delay(30_000)
            currentOnAutoSave()   // delegates to the latest lambda
        }
    }
}
```

```kotlin
// ❌ Without rememberUpdatedState — lambda from parent may be stale after recomposition
LaunchedEffect(Unit) {
    while (true) {
        delay(30_000)
        onAutoSave()   // captured at launch time, not the latest value
    }
}

// ❌ Using the lambda as the key — effect restarts on every parent recomposition
LaunchedEffect(onAutoSave) {
    while (true) {
        delay(30_000)
        onAutoSave()   // correct value, but the timer resets on every parent recompose
    }
}
```

**When to reach for `rememberUpdatedState`:**

| Situation | Use |
|---|---|
| `LaunchedEffect(Unit)` captures a lambda that may change | `rememberUpdatedState(lambda)` |
| `LaunchedEffect(Unit)` captures a value that may change but shouldn't restart the effect | `rememberUpdatedState(value)` |
| Effect key already tracks the source of truth (e.g., `LaunchedEffect(viewModel)`) | Not needed — the effect restarts cleanly on key change |
| The lambda is stable (never reassigned after initial composition) | Not needed |

---

### Why `LaunchedEffect(viewModel)` not `LaunchedEffect(Unit)`?

`LaunchedEffect(Unit)` is started once per composition entry and cancelled when the
composable leaves the tree. `LaunchedEffect(viewModel)` ties the lifecycle to the ViewModel
instance — if the screen is re-entered with the same ViewModel (e.g., bottom nav tab
switch), the same coroutine resumes rather than starting a new one. Either works for most
cases, but `viewModel` is more correct when the ViewModel outlives a single composition.

---

