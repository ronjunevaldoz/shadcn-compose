# Testing

Part of `kmp-mvi`. Load this file when working on: testing.

---

### Test state transitions

Use Turbine to test `StateFlow` emissions as a sequence:

```kotlin
// :feature:auth:ui/src/commonTest/kotlin/.../AuthViewModelTest.kt
class AuthViewModelTest {

    @Test
    fun `login success transitions Loading then clears state and sends NavigateToHome effect`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.state.test {
            // Initial state
            assertEquals(AuthContract.State(), awaitItem())

            viewModel.onIntent(AuthContract.Intent.LoginClicked)

            // Loading
            assertEquals(AuthContract.State(isLoading = true), awaitItem())

            // Cleared
            assertEquals(AuthContract.State(isLoading = false), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure resets isLoading and sends ShowError effect`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(failsWith = "Invalid credentials"))

        // Collect effects alongside state
        val effects = mutableListOf<AuthContract.Effect>()
        val effectJob = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.state.test {
            awaitItem()  // initial
            viewModel.onIntent(AuthContract.Intent.LoginClicked)
            awaitItem()  // loading = true
            val errorState = awaitItem()  // loading = false, error set
            assertFalse(errorState.isLoading)
            assertEquals("Invalid credentials", errorState.error)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(
            listOf(AuthContract.Effect.ShowError("Invalid credentials")),
            effects,
        )
        effectJob.cancel()
    }

    @Test
    fun `email change updates state and clears error`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.state.test {
            awaitItem()  // initial

            viewModel.onIntent(AuthContract.Intent.EmailChanged("new@example.com"))

            val updated = awaitItem()
            assertEquals("new@example.com", updated.email)
            assertNull(updated.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Test content composable independently

```kotlin
class AuthContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `login button disabled when loading`() {
        composeTestRule.setContent {
            AuthContent(
                state = AuthContract.State(isLoading = true),
                onIntent = {},
            )
        }
        composeTestRule.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    @Test
    fun `error message shown when error in state`() {
        composeTestRule.setContent {
            AuthContent(
                state = AuthContract.State(error = "Invalid credentials"),
                onIntent = {},
            )
        }
        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
}
```

### Fake repository pattern

```kotlin
// :core:testing/src/commonMain/kotlin/GROUP_ID/core/testing/fakes/FakeAuthRepository.kt
class FakeAuthRepository(
    private val failsWith: String? = null,
) : AuthRepository {

    val loginCalls = mutableListOf<Pair<String, String>>()

    override suspend fun login(email: String, password: String): LoginResult {
        loginCalls.add(email to password)
        return if (failsWith != null) LoginResult.Error(failsWith)
        else LoginResult.Success(FakeUser)
    }
}
```

---

