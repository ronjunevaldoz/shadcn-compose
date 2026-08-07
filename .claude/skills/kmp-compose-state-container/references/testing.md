# Testing

Part of `kmp-compose-state-container`. Load this file when working on: testing.

---

```kotlin
// Test ViewModel state via SavedStateHandle — verifies state survives process death
@Test fun `viewmodel restores state from savedStateHandle`() = runTest {
    val savedState = SavedStateHandle(mapOf("query" to "hello"))
    val vm = SearchViewModel(savedState)
    assertEquals("hello", vm.state.value.query)
}

// Test remember vs rememberSaveable semantics with ComposeTestRule
@get:Rule val composeRule = createComposeRule()

@Test fun `rememberSaveable counter survives recomposition`() {
    composeRule.setContent {
        var count by rememberSaveable { mutableStateOf(0) }
        Column {
            Button(
                onClick = { count++ },
                modifier = Modifier.testTag("increment"),
            ) { Text("+") }
            Text(count.toString(), modifier = Modifier.testTag("count"))
        }
    }
    composeRule.onNodeWithTag("increment").performClick()
    composeRule.onNodeWithTag("count").assertTextEquals("1")
}

@Test fun `remember resets when trigger changes`() {
    var key by mutableStateOf(0)
    composeRule.setContent {
        val value = remember(key) { key * 10 }
        Text(value.toString(), modifier = Modifier.testTag("value"))
    }
    composeRule.onNodeWithTag("value").assertTextEquals("0")
    key = 3
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("value").assertTextEquals("30")
}
```

---

