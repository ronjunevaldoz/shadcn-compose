# Implementing a ViewModel

Part of `kmp-mvi`. Load this file when working on: implementing a viewmodel.

---

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthViewModel.kt
package GROUP_ID.feature.auth.ui

import GROUP_ID.core.mvi.MviViewModel
import GROUP_ID.feature.auth.domain.AuthRepository
import GROUP_ID.feature.auth.domain.LoginResult

class AuthViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
    initialState = AuthContract.State(),
) {

    override suspend fun handleIntent(intent: AuthContract.Intent) {
        when (intent) {
            is AuthContract.Intent.EmailChanged ->
                updateState { copy(email = intent.value, error = null) }

            is AuthContract.Intent.PasswordChanged ->
                updateState { copy(password = intent.value, error = null) }

            is AuthContract.Intent.LoginClicked -> login()

            is AuthContract.Intent.ForgotPasswordClicked ->
                sendEffect(AuthContract.Effect.NavigateToForgotPassword)
        }
    }

    private suspend fun login() {
        val current = state.value
        if (current.isLoading) return   // guard — debounce rapid taps

        updateState { copy(isLoading = true, error = null) }

        when (val result = authRepository.login(current.email, current.password)) {
            is LoginResult.Success -> {
                updateState { copy(isLoading = false) }
                sendEffect(AuthContract.Effect.NavigateToHome)
            }
            is LoginResult.Error -> {
                // ✓ Always reset isLoading on error — forgetting this is a common bug
                updateState { copy(isLoading = false, error = result.message) }
                sendEffect(AuthContract.Effect.ShowError(result.message))
            }
        }
    }
}
```

---

