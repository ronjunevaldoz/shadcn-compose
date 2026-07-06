---
name: kotlin-multiplatform-form-validation
description: >-
  Declarative form validation for Kotlin Multiplatform — field-level validation
  rules in commonMain, synchronous and async validators, error state integrated
  into MVI UiState, submit gating, and reusable field components that display
  inline errors. No third-party validation library required.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - form validation
    - field validation
    - validation rules
    - async validation
    - submit gating
    - inline errors
    - form state
    - ValidationResult
    - FieldState
    - KMP form
---

## When to Use This Skill

Use when:
- A screen has a form with required fields, format rules, or async checks (username availability)
- Validation errors need to appear inline under each field as the user types or on submit
- Submit button must be disabled until all fields are valid
- Validation logic should be tested independently of the UI

**Trigger keywords:** form validation, field validation, required field, email validation,
inline error, validation rule, form error, submit disabled, async validation, validate form,
ValidationResult, FieldState, form state KMP, input validation, field error message,
form, validate, validation, form field, input validation, form handling, form state,
error message, check input, validate input, form submission.

**Freshness rule:** No external library is required — this pattern uses only `kotlinx.coroutines`
and Compose. If you choose to add a validation library (Valiktor, Konform), verify its KMP
support before adding to `libs.versions.toml`.

---

## Recommendation First

Define validation as pure functions that return `ValidationResult` — they are trivially testable
with no UI setup. Keep validation rules in `:domain`, field state in the ViewModel's `UiState`,
and error display in `:ui` composables. Never validate inside a composable directly.

---

## Core types — commonMain

```kotlin
// :core:validation — ValidationResult.kt
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

val ValidationResult.isValid: Boolean get() = this is ValidationResult.Valid
val ValidationResult.errorOrNull: String? get() = (this as? ValidationResult.Invalid)?.message
```

---

## Validation rules — pure functions in commonMain

```kotlin
// :core:validation — Validators.kt
object Validators {

    fun required(value: String, fieldName: String = "This field"): ValidationResult =
        if (value.isBlank()) ValidationResult.Invalid("$fieldName is required")
        else ValidationResult.Valid

    fun email(value: String): ValidationResult =
        if (Regex("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$").matches(value))
            ValidationResult.Valid
        else ValidationResult.Invalid("Enter a valid email address")

    fun minLength(value: String, min: Int): ValidationResult =
        if (value.length >= min) ValidationResult.Valid
        else ValidationResult.Invalid("Must be at least $min characters")

    fun maxLength(value: String, max: Int): ValidationResult =
        if (value.length <= max) ValidationResult.Valid
        else ValidationResult.Invalid("Must be $max characters or fewer")

    fun matches(value: String, other: String, fieldName: String = "Fields"): ValidationResult =
        if (value == other) ValidationResult.Valid
        else ValidationResult.Invalid("$fieldName do not match")

    // Combine multiple rules — first failure wins
    fun all(value: String, vararg rules: (String) -> ValidationResult): ValidationResult =
        rules.firstNotNullOfOrNull { rule ->
            rule(value).takeIf { it is ValidationResult.Invalid }
        } ?: ValidationResult.Valid
}
```

---

## Field state in UiState

```kotlin
// :presenter — RegistrationContract.kt
data class FieldState(
    val value: String = "",
    val error: String? = null,      // null = untouched or valid
    val isTouched: Boolean = false,
)

data class State(
    val email: FieldState    = FieldState(),
    val password: FieldState = FieldState(),
    val confirm: FieldState  = FieldState(),
    val isSubmitting: Boolean = false,
) {
    val isFormValid: Boolean
        get() = email.error == null && email.value.isNotBlank() &&
                password.error == null && password.value.isNotBlank() &&
                confirm.error == null && confirm.value.isNotBlank()
}

sealed class Intent {
    data class EmailChanged(val value: String)    : Intent()
    data class PasswordChanged(val value: String) : Intent()
    data class ConfirmChanged(val value: String)  : Intent()
    object Submit : Intent()
}
```

---

## ViewModel — validate on change and on submit

```kotlin
// :presenter — RegistrationViewModel.kt
class RegistrationViewModel(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationContract.State())
    val state: StateFlow<RegistrationContract.State> = _state.asStateFlow()

    fun onIntent(intent: RegistrationContract.Intent) {
        when (intent) {
            is RegistrationContract.Intent.EmailChanged -> {
                val error = Validators.all(intent.value,
                    { Validators.required(it, "Email") },
                    { Validators.email(it) },
                ).errorOrNull
                _state.update { it.copy(email = FieldState(intent.value, error, isTouched = true)) }
            }
            is RegistrationContract.Intent.PasswordChanged -> {
                val error = Validators.all(intent.value,
                    { Validators.required(it, "Password") },
                    { Validators.minLength(it, 8) },
                ).errorOrNull
                _state.update { it.copy(password = FieldState(intent.value, error, isTouched = true)) }
            }
            is RegistrationContract.Intent.ConfirmChanged -> {
                val error = Validators.matches(
                    intent.value, _state.value.password.value, "Passwords"
                ).errorOrNull
                _state.update { it.copy(confirm = FieldState(intent.value, error, isTouched = true)) }
            }
            RegistrationContract.Intent.Submit -> submit()
        }
    }

    private fun submit() {
        // Touch all fields to reveal any untouched errors
        _state.update { s ->
            s.copy(
                email    = s.email.copy(isTouched = true,
                    error = Validators.all(s.email.value,
                        { Validators.required(it, "Email") }, { Validators.email(it) }).errorOrNull),
                password = s.password.copy(isTouched = true,
                    error = Validators.all(s.password.value,
                        { Validators.required(it, "Password") }, { Validators.minLength(it, 8) }).errorOrNull),
                confirm  = s.confirm.copy(isTouched = true,
                    error = Validators.matches(s.confirm.value, s.email.value, "Passwords").errorOrNull),
            )
        }
        if (!_state.value.isFormValid) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            registerUseCase(_state.value.email.value, _state.value.password.value)
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
```

---

## Async validation (username availability)

```kotlin
// In ViewModel — debounce async check
is RegistrationContract.Intent.UsernameChanged -> {
    _state.update { it.copy(username = FieldState(intent.value, null)) }
    usernameCheckJob?.cancel()
    usernameCheckJob = viewModelScope.launch {
        delay(400)   // debounce
        val taken = checkUsernameUseCase(intent.value)
        val error = if (taken) "Username is already taken" else null
        _state.update { it.copy(username = FieldState(intent.value, error, isTouched = true)) }
    }
}
private var usernameCheckJob: Job? = null
```

---

## UI — validated field composable

```kotlin
// :ui — ValidatedTextField.kt
@Composable
fun ValidatedTextField(
    field: FieldState,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        AppTextField(
            value            = field.value,
            onValueChange    = onValueChange,
            label            = label,
            isError          = field.isTouched && field.error != null,
            keyboardOptions  = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
        )
        if (field.isTouched && field.error != null) {
            Text(
                text  = field.error,
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelSmall,
                modifier = Modifier.padding(start = AppTheme.spacing.sm, top = AppTheme.spacing.xxs),
            )
        }
    }
}
```

```kotlin
// :ui — RegistrationContent.kt
@Composable
fun RegistrationContent(
    state: RegistrationContract.State,
    onIntent: (RegistrationContract.Intent) -> Unit,
) {
    AppScaffold(topBar = { AppTopAppBar(title = "Create account") }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        ) {
            ValidatedTextField(
                field         = state.email,
                label         = "Email",
                onValueChange = { onIntent(RegistrationContract.Intent.EmailChanged(it)) },
                keyboardType  = KeyboardType.Email,
            )
            ValidatedTextField(
                field               = state.password,
                label               = "Password",
                onValueChange       = { onIntent(RegistrationContract.Intent.PasswordChanged(it)) },
                visualTransformation = PasswordVisualTransformation(),
            )
            ValidatedTextField(
                field               = state.confirm,
                label               = "Confirm password",
                onValueChange       = { onIntent(RegistrationContract.Intent.ConfirmChanged(it)) },
                visualTransformation = PasswordVisualTransformation(),
            )
            AppButton(
                text      = if (state.isSubmitting) "Creating…" else "Create account",
                onClick   = { onIntent(RegistrationContract.Intent.Submit) },
                enabled   = state.isFormValid && !state.isSubmitting,
                modifier  = Modifier.fillMaxWidth(),
            )
        }
    }
}
```

---

## Testing validators

```kotlin
class ValidatorsTest {
    @Test fun `email rejects malformed address`() {
        assertTrue(Validators.email("not-an-email") is ValidationResult.Invalid)
    }
    @Test fun `email accepts valid address`() {
        assertEquals(ValidationResult.Valid, Validators.email("user@example.com"))
    }
    @Test fun `minLength passes at boundary`() {
        assertEquals(ValidationResult.Valid, Validators.minLength("12345678", 8))
    }
    @Test fun `all returns first failure`() {
        val result = Validators.all("",
            { Validators.required(it) },
            { Validators.email(it) },
        )
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).message.contains("required"))
    }
}
```

---

## Common Anti-Patterns

- **Validation inside a composable** — `if (!email.isValid()) { ... }` in `@Composable` runs on every recomposition and cannot be unit tested; move all validation to the ViewModel
- **`isFormValid` computed in the ViewModel function** — compute it as a property on `UiState` so the submit button binding is declarative and always in sync
- **Showing errors before the user has touched a field** — use `isTouched` to gate error display; showing "Email is required" on an empty field before the user has focused it is jarring
- **Async validation without debounce** — firing a network check on every keystroke hammers the API; always `delay(300-400ms)` + cancel the previous job before issuing a new one
- **Duplicating validation logic in the backend** — the frontend validators are for UX only; always validate server-side as well

---

## Related Skills

- `kotlin-multiplatform-mvi` — `FieldState` lives inside `UiState`; validation intent handling follows the standard MVI pattern
- `kotlin-multiplatform-design-system` — `AppTextField` and error text styling come from the design system; use `AppTheme.colors.error` not hardcoded red
- `kotlin-multiplatform-unit-testing` — `Validators` functions are pure; test them directly with `runTest` or plain `@Test`

---

## Output Style

When implementing form validation, respond in this order:
1. **Validation rules** — `Validators` object with the rules this form needs
2. **FieldState and UiState** — `isFormValid` computed property
3. **ViewModel intents** — one intent per field change + Submit
4. **Async validators** — if any field needs a network check, add debounce pattern
5. **ValidatedTextField** — reusable composable with inline error display
6. **Tests** — validator unit tests and a ViewModel submit test

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
