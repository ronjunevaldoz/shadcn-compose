# Testing

Part of `kmp-compose-slot-api`. Load this file when working on: testing.

---

```kotlin
@get:Rule val composeRule = createComposeRule()

@Test fun `header slot renders caller-provided content`() {
    composeRule.setContent {
        AppCard(
            header = { Text("Card Title", modifier = Modifier.testTag("slot_header")) },
            content = { Text("Body") },
        )
    }
    composeRule.onNodeWithTag("slot_header").assertExists()
    composeRule.onNodeWithTag("slot_header").assertTextEquals("Card Title")
}

@Test fun `empty slot lambda does not crash`() {
    composeRule.setContent {
        AppCard(header = {}, content = {})
    }
    // Composable must accept empty slots gracefully — no exception thrown
}

@Test fun `action slot fires callback on click`() {
    var clicked = false
    composeRule.setContent {
        AppCard(
            header = { Text("Title") },
            content = { Text("Body") },
            action = { AppButton(text = "OK", onClick = { clicked = true }) },
        )
    }
    composeRule.onNodeWithText("OK").performClick()
    assertTrue(clicked)
}

// Roborazzi screenshot for visual contract
@Test fun `app_card_default_light screenshot`() {
    captureRoboImage("slot_card_default_light.png") {
        AppTheme(darkTheme = false) {
            AppCard(
                header = { Text("Card Header") },
                content = { Text("Card body text.") },
            )
        }
    }
}
```

---

