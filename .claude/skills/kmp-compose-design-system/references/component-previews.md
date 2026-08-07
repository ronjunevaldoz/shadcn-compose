# Component Previews

Part of `kmp-compose-design-system`. Load this file when working on: component previews.

---

Each design system component ships with a dedicated preview file under `previews/`.
These previews serve three purposes:

1. **IDE design review** — visible in the Desktop preview panel
   (`./gradlew :desktopApp:run` or Android Studio compose preview)
2. **Roborazzi per-component goldens** — captured by
   `./gradlew :core:designsystem:jvmTest`, producing one PNG per state
3. **`/fix-design` verification** — after a theme token change, run
   `:core:designsystem:jvmTest` to confirm all components still look correct
   before running full feature tests

Feature UI modules follow the same rule: every `*Content.kt` must have a preview stub and
matching Roborazzi screenshot coverage for phone, tablet, and desktop sizes.

> **Skill-owned.** Preview files follow the same ownership rule as components —
> updateable via `/update-design-system`. Never edit preview files to reflect
> project-specific states; create separate preview composables in the feature UI module.

---

### `previews/AppThemePreviewWrapper.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.theme.AppTheme

/**
 * Shared annotation for cross-device preview coverage.
 * Generates one screenshot per size class: phone, tablet, desktop.
 * Use on light/dark base variants. State variants (disabled, error) use plain @Preview.
 */
@Preview(name = "Phone",   widthDp = 360,  heightDp = 640)
@Preview(name = "Tablet",  widthDp = 673,  heightDp = 841)
@Preview(name = "Desktop", widthDp = 1280, heightDp = 800)
annotation class MultiDevicePreview

@Composable
fun AppThemePreviewWrapper(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
```

---

### `previews/AppButtonPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.ButtonVariant

@MultiDevicePreview
@Composable
fun AppButtonDefaultLightPreview() {
    AppThemePreviewWrapper(darkTheme = false) {
        AppButton(onClick = {}) { AppText("Continue") }
    }
}

@MultiDevicePreview
@Composable
fun AppButtonDefaultDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        AppButton(onClick = {}) { AppText("Continue") }
    }
}

@Preview
@Composable
fun AppButtonDisabledPreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, enabled = false) { AppText("Continue") }
    }
}

@Preview
@Composable
fun AppButtonOutlinePreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Outline) { AppText("Cancel") }
    }
}

@Preview
@Composable
fun AppButtonDestructivePreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Destructive) { AppText("Delete account") }
    }
}

@Preview
@Composable
fun AppButtonGhostPreview() {
    AppThemePreviewWrapper {
        AppButton(onClick = {}, variant = ButtonVariant.Ghost) { AppText("Skip") }
    }
}
```

---

### `previews/AppBadgePreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppBadge
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.BadgeVariant

@MultiDevicePreview
@Composable
fun AppBadgeAllVariantsPreview() {
    AppThemePreviewWrapper {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(variant = BadgeVariant.Default)    { AppText("New") }
            AppBadge(variant = BadgeVariant.Secondary)  { AppText("Beta") }
            AppBadge(variant = BadgeVariant.Destructive){ AppText("Error") }
            AppBadge(variant = BadgeVariant.Outline)    { AppText("Draft") }
            AppBadge(variant = BadgeVariant.Ghost)      { AppText("Info") }
        }
    }
}

@MultiDevicePreview
@Composable
fun AppBadgeDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(variant = BadgeVariant.Default)    { AppText("New") }
            AppBadge(variant = BadgeVariant.Destructive){ AppText("Error") }
        }
    }
}
```

---

### `previews/AppCardPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppButton
import GROUP_ID.core.designsystem.components.AppCard
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.styles.CardVariant

@MultiDevicePreview
@Composable
fun AppCardDefaultPreview() {
    AppThemePreviewWrapper {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppText("Card body content")
        }
    }
}

@Preview
@Composable
fun AppCardWithSlotsPreview() {
    AppThemePreviewWrapper {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            header = { AppText("Card Title") },
            footer = { AppButton(onClick = {}) { AppText("Action") } },
        ) {
            AppText("This is the card body. It can be multiple lines of description text.")
        }
    }
}

@Preview
@Composable
fun AppCardElevatedPreview() {
    AppThemePreviewWrapper {
        AppCard(modifier = Modifier.fillMaxWidth(), variant = CardVariant.Elevated) {
            AppText("Elevated card")
        }
    }
}

@MultiDevicePreview
@Composable
fun AppCardDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppText("Dark mode card")
        }
    }
}
```

---

### `previews/AppChipPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppChip
import GROUP_ID.core.designsystem.styles.ChipVariant

@MultiDevicePreview
@Composable
fun AppChipStatesPreview() {
    AppThemePreviewWrapper {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppChip(label = "Default",  onClick = {})
            AppChip(label = "Selected", onClick = {}, selected = true)
            AppChip(label = "Disabled", onClick = {}, enabled = false)
            AppChip(label = "Outline",  onClick = {}, variant = ChipVariant.Outline)
        }
    }
}

@MultiDevicePreview
@Composable
fun AppChipDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppChip(label = "Default",  onClick = {})
            AppChip(label = "Selected", onClick = {}, selected = true)
        }
    }
}
```

---

### `previews/AppTextFieldPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppTextField
import GROUP_ID.core.designsystem.styles.TextFieldVariant

@Preview
@Composable
fun AppTextFieldEmptyPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Enter email",
        )
    }
}

@MultiDevicePreview
@Composable
fun AppTextFieldWithLabelAndValuePreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "hello@example.com",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            placeholder = "you@example.com",
        )
    }
}

@Preview
@Composable
fun AppTextFieldErrorPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "bad-email",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            isError = true,
            supportingText = "Please enter a valid email address",
        )
    }
}

@Preview
@Composable
fun AppTextFieldDisabledPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "readonly@example.com",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            enabled = false,
        )
    }
}

@Preview
@Composable
fun AppTextFieldGhostPreview() {
    AppThemePreviewWrapper {
        AppTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Search…",
            variant = TextFieldVariant.Ghost,
        )
    }
}

@MultiDevicePreview
@Composable
fun AppTextFieldDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Email",
                placeholder = "you@example.com",
            )
            AppTextField(
                value = "bad",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Email",
                isError = true,
                supportingText = "Invalid email",
            )
        }
    }
}
```

---

### `previews/AppTextPreview.kt`

```kotlin
@file:OptIn(ExperimentalStylesApi::class)
package GROUP_ID.core.designsystem.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import GROUP_ID.core.designsystem.components.AppText
import GROUP_ID.core.designsystem.components.AppTextStyle

@MultiDevicePreview
@Composable
fun AppTextTypescalePreview() {
    AppThemePreviewWrapper {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppText("DisplayLarge",  style = AppTextStyle.DisplayLarge)
            AppText("DisplayMedium", style = AppTextStyle.DisplayMedium)
            AppText("TitleLarge",    style = AppTextStyle.TitleLarge)
            AppText("TitleMedium",   style = AppTextStyle.TitleMedium)
            AppText("TitleSmall",    style = AppTextStyle.TitleSmall)
            AppText("BodyLarge",     style = AppTextStyle.BodyLarge)
            AppText("BodyMedium",    style = AppTextStyle.BodyMedium)
            AppText("BodySmall",     style = AppTextStyle.BodySmall)
            AppText("LabelLarge",    style = AppTextStyle.LabelLarge)
            AppText("LabelSmall",    style = AppTextStyle.LabelSmall)
        }
    }
}

@Preview
@Composable
fun AppTextMutedPreview() {
    AppThemePreviewWrapper {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("Normal text",  style = AppTextStyle.BodyMedium)
            AppText("Muted text",   style = AppTextStyle.BodyMedium, muted = true)
            AppText("Normal label", style = AppTextStyle.LabelSmall)
            AppText("Muted label",  style = AppTextStyle.LabelSmall,  muted = true)
        }
    }
}

@MultiDevicePreview
@Composable
fun AppTextDarkPreview() {
    AppThemePreviewWrapper(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("TitleLarge dark",  style = AppTextStyle.TitleLarge)
            AppText("BodyMedium dark",  style = AppTextStyle.BodyMedium)
            AppText("Muted dark",       style = AppTextStyle.BodySmall, muted = true)
        }
    }
}
```

---

