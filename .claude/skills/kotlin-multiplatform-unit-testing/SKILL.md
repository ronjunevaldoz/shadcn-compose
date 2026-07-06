---
name: kotlin-multiplatform-unit-testing
description: >
  Unit testing patterns for KMP: runTest + Turbine for Flow assertions, fake-over-mock
  rule, :core:testing shared fixtures module, and ViewModel tests that run on plain JVM
  via the :presenter module with no Android dependency.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-18'
  keywords:
    - unit testing
    - KMP
    - Kotlin Multiplatform
    - runTest
    - Turbine
    - Flow testing
    - ViewModel test
    - fake
    - :core:testing
    - coroutines test
    - JVM test
---

## When to Use This Skill

Use when you need to:
- Test a ViewModel or use case that emits a `Flow`
- Set up `runTest` + Turbine in a KMP module
- Create a `:core:testing` module with shared fakes and builders
- Write a fake repository instead of a mock
- Run ViewModel tests on plain JVM without a device or emulator

**Trigger keywords:** unit test, runTest, Turbine, Flow test, ViewModel test, fake repository,
:core:testing, coroutines test, JVM test, fake over mock, test fixtures, test builders,
test, write test, test code, coverage, test ViewModel, test logic, testing, write tests,
unit testing, test this, how to test, test the screen.

**Freshness rule:** Turbine API changes between minor versions — recheck `awaitItem()` vs
`awaitComplete()` semantics when upgrading. Kotlin coroutines test API is stable but
`TestCoroutineScheduler` replaced `TestCoroutineDispatcher` in 1.6+.

---

## Recommendation First

Default to **fake over mock + `runTest` + Turbine for all Flow-emitting code**.

Why:
- fakes implement the real interface — they catch refactors that mocks silently ignore
- `runTest` gives a real `CoroutineScope` with virtual time — no `Thread.sleep`, no flakiness
- Turbine provides structured `awaitItem()` assertions — cleaner than `collect {}` with a list
- `:presenter` with no Compose dep = ViewModel tests compile and run on JVM in `./gradlew jvmTest`

Use MockK only for final classes you cannot subclass. Prefer fakes everywhere else.

---

## `:core:testing` Module

### `settings.gradle.kts`

```kotlin
include(":core:testing")
```

### `core/testing/build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.core")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.core.testing"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}
```

All deps are `api()` — any module that depends on `:core:testing` gets them transitively
without re-declaring them.

### Directory layout

```
core/testing/src/commonMain/kotlin/GROUP_ID/core/testing/
    fakes/
        FakeTokenStorage.kt
        FakeNetworkClient.kt
        FakeAuthRepository.kt
    builders/
        UserBuilder.kt          ← sensible defaults, override per test
    rules/
        MainCoroutineRule.kt
```

---

## Fake Pattern

Fakes implement the real interface and expose mutable state for test setup:

```kotlin
// :core:testing — FakeAuthRepository.kt
class FakeAuthRepository : AuthRepository {
    var currentUser: User? = null
    var shouldThrow: Throwable? = null

    override fun getUser(id: String): Flow<User> = flow {
        shouldThrow?.let { throw it }
        currentUser?.let { emit(it) } ?: throw NoSuchElementException("user not found")
    }

    override suspend fun signOut() {
        currentUser = null
    }
}
```

Usage in tests:
```kotlin
val repo = FakeAuthRepository().apply { currentUser = UserBuilder().build() }
val useCase = GetUserUseCase(repo)
```

---

## `runTest` + Turbine

```kotlin
@Test
fun `state emits Loading then Success`() = runTest {
    val repo = FakeAuthRepository().apply { currentUser = UserBuilder().build() }
    val viewModel = AuthViewModel(GetUserUseCase(repo))

    viewModel.uiState.test {
        assertEquals(AuthUiState.Loading, awaitItem())
        val success = awaitItem() as AuthUiState.Success
        assertEquals(repo.currentUser, success.user)
        cancelAndIgnoreRemainingEvents()
    }
}

@Test
fun `state emits Error on repository failure`() = runTest {
    val repo = FakeAuthRepository().apply { shouldThrow = RuntimeException("network error") }
    val viewModel = AuthViewModel(GetUserUseCase(repo))

    viewModel.uiState.test {
        awaitItem()  // Loading
        val error = awaitItem() as AuthUiState.Error
        assertEquals("network error", error.message)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### `cancelAndIgnoreRemainingEvents()`

Always call this at the end of a `test {}` block unless you expect the flow to complete.
Failing to call it causes the test to hang waiting for more items.

---

## Virtual Time — `TestCoroutineScheduler`

Use `advanceUntilIdle()` or `advanceTimeBy()` when code uses `delay()`:

```kotlin
@Test
fun `debounced search emits after 300ms`() = runTest {
    val viewModel = SearchViewModel(FakeSearchRepository())
    viewModel.onIntent(SearchUiIntent.Query("kotlin"))

    advanceTimeBy(299)
    viewModel.uiState.test { expectNoEvents() }

    advanceTimeBy(1)  // trigger debounce
    viewModel.uiState.test {
        val result = awaitItem() as SearchUiState.Results
        assertTrue(result.items.isNotEmpty())
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Builder Pattern

```kotlin
// :core:testing — UserBuilder.kt
class UserBuilder(
    var id: String = "test-user-id",
    var name: String = "Test User",
    var email: String = "test@example.com",
) {
    fun build() = User(id = id, name = name, email = email)
}
```

Override only what matters per test — all other fields default to safe values.

---

## Running Tests

```bash
# All common (JVM) tests — fast, no device needed
./gradlew jvmTest

# Specific module
./gradlew :feature:auth:presenter:jvmTest

# Full test suite
./gradlew allTests
```

ViewModel tests in `:presenter` run via `jvmTest` because `:presenter` declares `jvm()` in its convention plugin.

---

## Related Skills

- `kotlin-multiplatform-presenter-module` — the `:presenter` module whose ViewModels are tested here
- `kotlin-multiplatform-clean-architecture` — the layer split that makes JVM ViewModel tests possible
- `kotlin-multiplatform-roborazzi` — screenshot tests complement unit tests for the `:ui` layer
- `kotlin-multiplatform-feature-scaffold` — wires `:core:testing` into the project

---

## Common Anti-Patterns

- using MockK or Mockito instead of fakes — mocks don't catch interface refactors; fakes do
- calling `runBlocking` in tests — use `runTest`; `runBlocking` doesn't support virtual time
- forgetting `cancelAndIgnoreRemainingEvents()` — test hangs or fails with unexpected events
- testing `:ui` composables with a real ViewModel — inject `UiState` directly into `Content` composables instead
- putting test utilities in `src/androidTest/` — KMP test utilities belong in `src/commonTest/` via `:core:testing`

If a test is flaky, add `advanceUntilIdle()` before assertions — the coroutine likely hasn't
settled when the assertion runs.

---

## Output Style

When asked about testing KMP code, respond in this order:
1. the fake class (implement the interface, expose mutable state)
2. `runTest` + Turbine assertion block
3. where the test file lives (`commonTest` in `:presenter` or `:domain`)
4. how to run it (`./gradlew jvmTest`)
5. `:core:testing` module setup if shared fakes are needed

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-18 | Initial release. |
