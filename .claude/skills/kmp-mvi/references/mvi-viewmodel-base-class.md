# MviViewModel Base Class

Part of `kmp-mvi`. Load this file when working on: mviviewmodel base class.

---

Place this in `:core:common` (or `:core:ui`) so all feature ViewModels can extend it.

```kotlin
// :core:common/src/commonMain/kotlin/GROUP_ID/core/mvi/MviViewModel.kt
package GROUP_ID.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern.
 *
 * - [State]  — immutable data class representing everything the screen needs to render
 * - [Intent] — sealed interface of user actions / events
 * - [Effect] — sealed interface of one-shot side effects (navigation, toasts, dialogs)
 *
 * Usage:
 * ```
 * class AuthViewModel(private val repo: AuthRepository) :
 *     MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
 *         initialState = AuthContract.State()
 *     ) {
 *
 *     override fun handleIntent(intent: AuthContract.Intent) {
 *         when (intent) {
 *             is AuthContract.Intent.LoginClicked -> login()
 *             ...
 *         }
 *     }
 * }
 * ```
 */
abstract class MviViewModel<State : Any, Intent : Any, Effect : Any>(
    initialState: State,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    // Catches uncaught exceptions from handleIntent coroutines; subclasses may override
    // to update error state instead of crashing silently on KMP targets.
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throw throwable  // rethrow so crash reporters and tests can see it
    }

    /**
     * Called by the UI layer. Routes the intent to [handleIntent] on [viewModelScope].
     */
    fun onIntent(intent: Intent) {
        viewModelScope.launch(exceptionHandler) { handleIntent(intent) }
    }

    /**
     * Implement per-ViewModel intent handling. Runs on [viewModelScope].
     * Can be a suspend function — safe to call suspend APIs directly.
     */
    protected abstract suspend fun handleIntent(intent: Intent)

    /**
     * Atomically update state. Uses compare-and-swap — safe under concurrent intent handling.
     */
    protected fun updateState(block: State.() -> State) {
        _state.update(block)
    }

    /**
     * Send a one-shot effect. Buffered — delivered when a collector is active.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
```

---

