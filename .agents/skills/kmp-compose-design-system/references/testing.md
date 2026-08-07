# Testing

Part of `kmp-compose-design-system`. Load this file when working on: testing.

---

```kotlin
// Design system testing is primarily visual — Roborazzi screenshot pairs (light + dark)
// for every token category and component, plus interaction tests for interactive tokens.

@Test fun `color_tokens_light screenshot`() {
    captureRoboImage("ds_color_tokens_light.png") {
        AppTheme(darkTheme = false) {
            val t = appTheme
            Column(modifier = Modifier.padding(t.spacing.lg)) {
                Box(Modifier.size(48.dp).background(t.colors.primary))
                Box(Modifier.size(48.dp).background(t.colors.secondary))
                Box(Modifier.size(48.dp).background(t.colors.surface))
                Box(Modifier.size(48.dp).background(t.colors.error))
            }
        }
    }
}

@Test fun `color_tokens_dark screenshot`() {
    captureRoboImage("ds_color_tokens_dark.png") {
        AppTheme(darkTheme = true) {
            val t = appTheme
            Column(modifier = Modifier.padding(t.spacing.lg)) {
                Box(Modifier.size(48.dp).background(t.colors.primary))
                Box(Modifier.size(48.dp).background(t.colors.secondary))
                Box(Modifier.size(48.dp).background(t.colors.surface))
                Box(Modifier.size(48.dp).background(t.colors.error))
            }
        }
    }
}

@Test fun `typography_scale screenshot`() {
    captureRoboImage("ds_typography_scale.png") {
        AppTheme {
            Column(modifier = Modifier.padding(appTheme.spacing.lg)) {
                AppText("Display Large",  style = AppTextStyle.DisplayLarge)
                AppText("Display Medium", style = AppTextStyle.DisplayMedium)
                AppText("Body Large",     style = AppTextStyle.BodyLarge)
                AppText("Label Small",    style = AppTextStyle.LabelSmall)
            }
        }
    }
}

@Test fun `spacing tokens match expected dp values`() {
    // Assert the compile-time constants — catches accidental token renames
    assertEquals(16.dp, AppSpacing().lg)
    assertEquals(8.dp,  AppSpacing().sm)
    assertEquals(4.dp,  AppSpacing().xs)
}
```

---

