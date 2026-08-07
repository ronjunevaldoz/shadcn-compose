# Testing

Part of `kmp-shared-resources`.

---

```kotlin
// Compose Resources provides runtime resolution — test via a real Compose scope
@get:Rule val composeRule = createComposeRule()

@Test fun `app_name string resource resolves without crash`() {
    composeRule.setContent {
        val name = stringResource(Res.string.app_name)
        Text(name, modifier = Modifier.testTag("app_name"))
    }
    // If the resource is missing from any platform bundle, this throws at runtime
    composeRule.onNodeWithTag("app_name").assertExists()
}

@Test fun `plural resource selects correct form for count one`() {
    composeRule.setContent {
        Text(
            pluralStringResource(Res.plurals.items_count, 1, 1),
            modifier = Modifier.testTag("plural_one"),
        )
    }
    composeRule.onNodeWithTag("plural_one").assertTextEquals("1 item")
}

@Test fun `plural resource selects correct form for count many`() {
    composeRule.setContent {
        Text(
            pluralStringResource(Res.plurals.items_count, 3, 3),
            modifier = Modifier.testTag("plural_many"),
        )
    }
    composeRule.onNodeWithTag("plural_many").assertTextEquals("3 items")
}
```

> String resource tests run on JVM via Roborazzi / `createComposeRule()` — no emulator needed. Add any missing string keys to `commonMain/composeResources/values/strings.xml` and re-run.

---

