# Bundled Script

Part of `kmp-feature-scaffold`. Load this file when working on: bundled script.

---

- `scripts/validate_module_graph.py` — checks a target project for the expected
  `:model/:api/:domain/:data/:presenter/:ui` feature module files, the `androidApp`
  feature UI link, and the required preview stub for each `*Content.kt` in `:feature:*:ui`.

### Turbine usage pattern

```kotlin
// commonTest — testing a ViewModel or use case that emits a Flow
@Test
fun `state emits Loading then Success`() = runTest {
    val viewModel = AuthViewModel(FakeGetUserUseCase())
    viewModel.uiState.test {
        assertEquals(AuthUiState.Loading, awaitItem())
        assertEquals(AuthUiState.Success(fakeUser), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Shared fakes pattern in `:core:testing`

```
src/commonMain/kotlin/GROUP_ID/core/testing/
    fakes/
        FakeTokenStorage.kt
        FakeNetworkClient.kt
    builders/
        UserBuilder.kt          ← test data builders with defaults
    rules/
        MainCoroutineRule.kt    ← TestCoroutineDispatcher setup
```

Example fake:
```kotlin
class FakeTokenStorage : TokenStorage {
    var accessToken: String? = "test-access-token"
    var refreshToken: String? = "test-refresh-token"
    override suspend fun getAccessToken() = accessToken
    override suspend fun getRefreshToken() = refreshToken
    override suspend fun saveTokens(access: String, refresh: String) {
        accessToken = access; refreshToken = refresh
    }
    override suspend fun clearTokens() { accessToken = null; refreshToken = null }
}
```

---

