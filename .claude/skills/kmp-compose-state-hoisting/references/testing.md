# Testing

Part of `kmp-compose-state-hoisting`. Load this file when working on: testing.

---

```kotlin
// Stateless composables are pure functions — easy to test with ComposeTestRule
@get:Rule val composeRule = createComposeRule()

@Test fun `stateless counter renders given count`() {
    composeRule.setContent {
        StatelessCounter(count = 7, onIncrement = {}, modifier = Modifier.testTag("counter"))
    }
    composeRule.onNodeWithTag("counter").assertTextContains("7")
}

@Test fun `increment callback fires on button click`() {
    var incrementCalled = false
    composeRule.setContent {
        StatelessCounter(count = 0, onIncrement = { incrementCalled = true })
    }
    composeRule.onNodeWithContentDescription("Increment").performClick()
    assertTrue(incrementCalled)
}

@Test fun `stateful wrapper delegates increment to hoisted state`() {
    composeRule.setContent {
        StatefulCounter()
    }
    composeRule.onNodeWithText("0").assertExists()
    composeRule.onNodeWithContentDescription("Increment").performClick()
    composeRule.onNodeWithText("1").assertExists()
}

@Test fun `callback receives correct argument`() {
    var received = -1
    composeRule.setContent {
        StatelessSlider(value = 0.5f, onValueChange = { received = (it * 100).toInt() })
    }
    // Simulate a drag — check callback contract, not drag mechanics
    // Use semantics-based interaction rather than pixel coordinates
    composeRule.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { it(0.75f) }
    assertEquals(75, received)
}
```

---

