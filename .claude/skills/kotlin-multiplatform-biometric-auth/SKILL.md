---
name: kotlin-multiplatform-biometric-auth
description: >-
  Biometric authentication for Kotlin Multiplatform — a sealed BiometricResult type
  in commonMain, expect/actual BiometricAuthenticator, Android BiometricPrompt with
  CryptoObject, iOS LocalAuthentication (LAContext.evaluatePolicy), Koin wiring,
  and graceful fallback to device PIN/password when biometrics are unavailable.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - biometric
    - biometric auth
    - fingerprint
    - face ID
    - BiometricPrompt
    - LocalAuthentication
    - LAContext
    - biometric result
    - expect actual
    - CryptoObject
    - biometric fallback
    - KMP biometric
    - device credential
---

## When to Use This Skill

Use when:
- Protecting a sensitive action (payment, delete, settings change) with biometric authentication
- Logging in with fingerprint or Face ID instead of a password
- Unlocking stored credentials from the device keystore after biometric verification
- Falling back to device PIN/password when biometrics are unavailable or not enrolled

**Trigger keywords:** biometric, biometric auth, fingerprint, Face ID, Touch ID,
BiometricPrompt, LocalAuthentication, LAContext, biometric result, device credential,
biometric unlock, secure auth KMP, expect actual biometric, CryptoObject, biometric fallback.

**Freshness rule:** `androidx.biometric:biometric` 1.2+ supports `BiometricManager` for
capability checking before showing the prompt. The `BiometricPrompt` API is in
`androidx.fragment`; the `CryptoObject` path requires a `FragmentActivity`. iOS 17+
deprecated `kLAPolicyDeviceOwnerAuthenticationWithBiometrics` for some flows — verify
against the latest Apple docs when targeting iOS 17+.

---

## Recommendation First

Define `BiometricResult` in `commonMain` and use `expect/actual BiometricAuthenticator`
to isolate platform APIs. The ViewModel calls the authenticator and reacts to the result —
it never imports `BiometricPrompt` or `LAContext`. Always check availability before
showing a biometric prompt; show a clear error if neither biometrics nor device credentials
are enrolled.

---

## Core types — commonMain

```kotlin
// :core:biometric:model — BiometricResult.kt
sealed class BiometricResult {
    object Success             : BiometricResult()
    object Cancelled           : BiometricResult()   // user dismissed prompt
    object Fallback            : BiometricResult()   // user chose PIN/password
    data class Error(val message: String) : BiometricResult()
    object NotAvailable        : BiometricResult()   // no hardware
    object NotEnrolled         : BiometricResult()   // hardware present but no biometrics enrolled
}

enum class BiometricStrength { Strong, Weak }
```

---

## expect/actual BiometricAuthenticator

```kotlin
// commonMain — BiometricAuthenticator.kt
expect class BiometricAuthenticator {
    suspend fun canAuthenticate(): Boolean
    suspend fun authenticate(
        title: String,
        subtitle: String,
        cancelLabel: String,
        strength: BiometricStrength = BiometricStrength.Strong,
        allowDeviceCredential: Boolean = true,
    ): BiometricResult
}
```

---

## Android implementation

```kotlin
// androidMain — BiometricAuthenticator.android.kt
actual class BiometricAuthenticator(private val activity: FragmentActivity) {

    actual suspend fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        cancelLabel: String,
        strength: BiometricStrength,
        allowDeviceCredential: Boolean,
    ): BiometricResult = suspendCoroutine { cont ->
        val authenticators = when {
            allowDeviceCredential ->
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            strength == BiometricStrength.Strong ->
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            else ->
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .apply {
                if (!allowDeviceCredential) setNegativeButtonText(cancelLabel)
            }
            .build()

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    cont.resume(BiometricResult.Success)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    cont.resume(
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_CANCELED     -> BiometricResult.Cancelled
                            BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricResult.NotEnrolled
                            BiometricPrompt.ERROR_HW_NOT_PRESENT,
                            BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometricResult.NotAvailable
                            else -> BiometricResult.Error(errString.toString())
                        }
                    )
                }
                override fun onAuthenticationFailed() {
                    // Not final — user can retry; ignore here
                }
            }
        )
        prompt.authenticate(promptInfo)
    }
}
```

---

## iOS implementation

```kotlin
// iosMain — BiometricAuthenticator.ios.kt
actual class BiometricAuthenticator {

    actual suspend fun canAuthenticate(): Boolean {
        val context = LAContext()
        val error = platform.Foundation.NSErrorVar()
        return context.canEvaluatePolicy(
            kLAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = error
        )
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        cancelLabel: String,
        strength: BiometricStrength,
        allowDeviceCredential: Boolean,
    ): BiometricResult = suspendCoroutine { cont ->
        val context = LAContext()
        context.localizedCancelTitle = cancelLabel
        context.localizedFallbackTitle = if (allowDeviceCredential) "Use Passcode" else ""

        val policy = if (allowDeviceCredential)
            kLAPolicyDeviceOwnerAuthentication
        else
            kLAPolicyDeviceOwnerAuthenticationWithBiometrics

        context.evaluatePolicy(policy, localizedReason = title) { success, error ->
            when {
                success         -> cont.resume(BiometricResult.Success)
                error == null   -> cont.resume(BiometricResult.Cancelled)
                else            -> {
                    val laError = error as? LAError
                    cont.resume(when (laError?.code?.toInt()) {
                        LAErrorUserCancel    -> BiometricResult.Cancelled
                        LAErrorUserFallback  -> BiometricResult.Fallback
                        LAErrorBiometryNotEnrolled -> BiometricResult.NotEnrolled
                        LAErrorBiometryNotAvailable -> BiometricResult.NotAvailable
                        else -> BiometricResult.Error(error.localizedDescription ?: "Auth failed")
                    })
                }
            }
        }
    }
}
```

---

## ViewModel

```kotlin
// :presenter — SecureActionViewModel.kt
class SecureActionViewModel(
    private val biometric: BiometricAuthenticator,
    private val performAction: PerformSecureActionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SecureActionContract.State())
    val state: StateFlow<SecureActionContract.State> = _state.asStateFlow()

    fun onIntent(intent: SecureActionContract.Intent) {
        when (intent) {
            SecureActionContract.Intent.AuthenticateAndProceed -> authenticate()
        }
    }

    private fun authenticate() {
        viewModelScope.launch {
            if (!biometric.canAuthenticate()) {
                _state.update { it.copy(error = "Biometric authentication is not available") }
                return@launch
            }
            _state.update { it.copy(isAuthenticating = true) }
            val result = biometric.authenticate(
                title    = "Confirm action",
                subtitle = "Use your biometric or device PIN to continue",
                cancelLabel = "Cancel",
            )
            _state.update { it.copy(isAuthenticating = false) }
            when (result) {
                BiometricResult.Success   -> performAction()
                BiometricResult.Cancelled -> Unit
                BiometricResult.Fallback  -> Unit
                BiometricResult.NotEnrolled ->
                    _state.update { it.copy(error = "No biometrics enrolled. Set up in Settings.") }
                BiometricResult.NotAvailable ->
                    _state.update { it.copy(error = "Biometric hardware not available.") }
                is BiometricResult.Error ->
                    _state.update { it.copy(error = result.message) }
            }
        }
    }

    private suspend fun performAction() {
        _state.update { it.copy(isLoading = true) }
        performAction.invoke()
        _state.update { it.copy(isLoading = false, isSuccess = true) }
    }
}
```

---

## Koin wiring

```kotlin
// androidMain
val biometricModule = module {
    single { BiometricAuthenticator(get<FragmentActivity>()) }
}

// iosMain
val biometricModule = module {
    single { BiometricAuthenticator() }
}
```

---

## Testing

```kotlin
class FakeBiometricAuthenticator(
    private val result: BiometricResult = BiometricResult.Success,
    private val canAuth: Boolean = true,
) : BiometricAuthenticator() {   // or extract an interface
    override suspend fun canAuthenticate() = canAuth
    override suspend fun authenticate(
        title: String, subtitle: String, cancelLabel: String,
        strength: BiometricStrength, allowDeviceCredential: Boolean,
    ) = result
}

@Test fun `success triggers secure action`() = runTest {
    val performAction = FakePerformSecureActionUseCase()
    val vm = SecureActionViewModel(
        FakeBiometricAuthenticator(BiometricResult.Success),
        performAction,
    )
    vm.onIntent(SecureActionContract.Intent.AuthenticateAndProceed)
    assertTrue(vm.state.value.isSuccess)
    assertTrue(performAction.wasCalled)
}

@Test fun `not available sets error state`() = runTest {
    val vm = SecureActionViewModel(
        FakeBiometricAuthenticator(canAuth = false),
        FakePerformSecureActionUseCase(),
    )
    vm.onIntent(SecureActionContract.Intent.AuthenticateAndProceed)
    assertNotNull(vm.state.value.error)
}
```

---

## Common Anti-Patterns

- **Calling `BiometricPrompt` directly in a ViewModel** — ViewModel is in `commonMain`;
  `BiometricPrompt` is Android-only; use the `expect/actual BiometricAuthenticator`
- **Not checking `canAuthenticate()` before showing the prompt** — on devices without
  enrolled biometrics the prompt either shows nothing or shows an error; check first and
  fall back gracefully
- **Blocking the main thread in iOS `evaluatePolicy`** — `evaluatePolicy` calls its
  completion block on a background queue; `suspendCoroutine` handles this correctly, but
  never wrap the callback in a `runBlocking`
- **Using `BiometricStrength.Weak` for sensitive actions** — class 2 (weak) biometrics
  (face unlock on low-end Android) should not gate payment or credential flows; use
  `BiometricStrength.Strong` for any security-sensitive prompt
- **No fallback for `NotEnrolled`** — users who haven't set up biometrics need a clear
  message directing them to device settings, not a silent failure

---

## Related Skills

- `kotlin-multiplatform-mvi` — `BiometricResult` handling belongs in `onIntent`; the
  authenticate intent triggers the authenticator and updates `UiState`
- `kotlin-multiplatform-dependency-injection` — `BiometricAuthenticator` requires a
  platform context (`FragmentActivity` on Android); bind it in the platform Koin module
- `kotlin-multiplatform-permissions` — biometric authentication does not require a
  manifest permission (`USE_BIOMETRIC` is a normal permission, not dangerous, but is
  still declared in `AndroidManifest.xml`)

---

## Output Style

When implementing biometric auth, respond in this order:
1. **BiometricResult** — sealed class (Camera→Biometric mapping if already present)
2. **expect/actual BiometricAuthenticator** — with `canAuthenticate` + `authenticate`
3. **Android impl** — `BiometricPrompt` with `AuthenticationCallback`
4. **iOS impl** — `LAContext.evaluatePolicy` via `suspendCoroutine`
5. **ViewModel** — `canAuthenticate` check, `authenticate` call, result exhaustive `when`
6. **Koin** — platform module single
7. **Fake + tests** — success case and not-available case

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
