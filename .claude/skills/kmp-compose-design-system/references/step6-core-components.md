# Step 6: Core Components

Part of `kmp-compose-design-system`. Load this file when working on: step 6: core components.

---

> **Skill-owned.** Components are updateable via `/update-design-system`. Avoid deep
> customisations here — put brand-specific variants in project-level composables that
> wrap these primitives instead.

| Component | Stability | Notes |
|---|---|---|
| `AppButton` | **Stable** | 6 variants, 5 sizes |
| `AppBadge` | **Stable** | 5 variants |
| `AppCard` | **Stable** | 3 variants, 2 sizes |
| `AppChip` | **Stable** | 3 variants, selected state |
| `AppTextField` | **Stable** | label, placeholder, leading/trailing icon, error state |
| `AppText` | **Stable** | `AppTextStyle` enum, muted mode |

### `components/AppButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ButtonSize
import GROUP_ID.core.designsystem.styles.ButtonVariant
import GROUP_ID.core.designsystem.theme.rememberStyle

/**
 * shadcn-inspired AppButton.
 *
 * Usage:
 * ```
 * AppButton(onClick = {}) { Text("Click me") }
 * AppButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Sm) { Text("Outline") }
 * AppButton(onClick = {}, variant = ButtonVariant.Destructive) { Text("Delete") }
 * // One-off style override:
 * AppButton(onClick = {}, style = Style { shape(CircleShape) }) { Text("Pill") }
 * ```
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Md,
    style: Style = Style,        // ← empty; DO NOT set a default Style here
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // rememberUpdatedStyleState keeps isEnabled current across recomposition without
    // recreating the StyleState — the sanctioned pattern from the official Styles API docs.
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }
    // rememberStyle memoizes the merged descriptor on (variant, size) — rebuilt only
    // when one of them actually changes, not on every recomposition.
    val defaultStyle = rememberStyle(variant, size)

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,          // no ripple — use Style animations
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .styleable(styleState, defaultStyle, style),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
```

### `components/AppBadge.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.styles.BadgeVariant

/**
 * Label/tag component. Maps to shadcn Badge.
 *
 * Usage:
 * ```
 * AppBadge { Text("New") }
 * AppBadge(variant = BadgeVariant.Destructive) { Text("Error") }
 * AppBadge(variant = BadgeVariant.Outline) { Text("Draft") }
 * ```
 */
@Composable
fun AppBadge(
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    // Non-interactive — no interaction source needed, use a static StyleState
    val styleState = remember { MutableStyleState() }

    Box(
        modifier = modifier.styleable(styleState, variant.style, style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

### `components/AppCard.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.styles.CardSize
import GROUP_ID.core.designsystem.styles.CardVariant

/**
 * Maps to shadcn Card with slots: header, content, footer.
 *
 * Usage:
 * ```
 * AppCard(
 *     header = { CardHeader(title = "Title", description = "Subtitle") },
 *     footer = { AppButton(onClick = {}) { Text("Action") } }
 * ) {
 *     Text("Card body content")
 * }
 * ```
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    size: CardSize = CardSize.Default,
    style: Style = Style,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val styleState = remember { MutableStyleState() }

    Column(
        modifier = modifier.styleable(styleState, variant.style, style),
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(size.headerSpacing))
        }
        content()
        if (footer != null) {
            Spacer(Modifier.height(size.headerSpacing))
            footer()
        }
    }
}

@Composable
fun CardHeader(
    title: String,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            AppText(text = title, style = AppTextStyle.TitleSmall)
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                AppText(text = description, style = AppTextStyle.BodySmall, muted = true)
            }
        }
        if (action != null) {
            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)) {
                action()
            }
        }
    }
}
```

### `components/AppChip.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ChipVariant

/**
 * Selectable chip / filter tag.
 *
 * Usage:
 * ```
 * AppChip(label = "Kotlin", selected = true, onClick = { toggle() })
 * AppChip(label = "Swift", variant = ChipVariant.Outline, onClick = {})
 * ```
 */
@Composable
fun AppChip(
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    variant: ChipVariant = if (selected) ChipVariant.Selected else ChipVariant.Default,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    } else Modifier

    Row(
        modifier = modifier
            .then(clickableModifier)
            .styleable(styleState, variant.style, style),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
    ) {
        AppText(text = label)
    }
}
```

### `components/AppTextField.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import GROUP_ID.core.designsystem.styles.TextFieldVariant
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.colors

/**
 * Usage:
 * ```
 * AppTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "you@example.com")
 * AppTextField(value = pwd, onValueChange = { pwd = it }, label = "Password", visualTransformation = PasswordVisualTransformation())
 * AppTextField(value = q, onValueChange = { q = it }, variant = TextFieldVariant.Ghost, placeholder = "Search…")
 * AppTextField(value = bio, onValueChange = { bio = it }, singleLine = false, label = "Bio")
 * ```
 */
@OptIn(ExperimentalStylesApi::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    variant: TextFieldVariant = TextFieldVariant.Default,
    style: Style = Style,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    val errorStyle = if (isError) Style { borderColor(colors.error) } else Style

    Column(modifier = modifier) {
        if (label != null) {
            AppText(text = label, style = AppTextStyle.LabelLarge)
            Spacer(Modifier.height(appTheme.spacing.xxs))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .styleable(styleState, variant.style then errorStyle, style),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(Modifier.width(appTheme.spacing.xs))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            AppText(placeholder, style = AppTextStyle.BodyMedium, muted = true)
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(appTheme.spacing.xs))
                        trailingIcon()
                    }
                }
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(appTheme.spacing.xxs))
            AppText(
                text = supportingText,
                style = AppTextStyle.BodySmall,
                color = if (isError) appTheme.colors.error else appTheme.colors.onSurfaceVariant,
            )
        }
    }
}
```

---

### `components/AppText.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import GROUP_ID.core.designsystem.theme.appTheme

enum class AppTextStyle {
    DisplayLarge, DisplayMedium,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelSmall,
}

/**
 * Usage:
 * ```
 * AppText("Hello world")
 * AppText("Title", style = AppTextStyle.TitleLarge)
 * AppText("Subtitle", style = AppTextStyle.BodySmall, muted = true)
 * ```
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.BodyMedium,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified,
) {
    val theme = appTheme
    val resolvedStyle = when (style) {
        AppTextStyle.DisplayLarge  -> theme.typography.displayLarge
        AppTextStyle.DisplayMedium -> theme.typography.displayMedium
        AppTextStyle.TitleLarge    -> theme.typography.titleLarge
        AppTextStyle.TitleMedium   -> theme.typography.titleMedium
        AppTextStyle.TitleSmall    -> theme.typography.titleSmall
        AppTextStyle.BodyLarge     -> theme.typography.bodyLarge
        AppTextStyle.BodyMedium    -> theme.typography.bodyMedium
        AppTextStyle.BodySmall     -> theme.typography.bodySmall
        AppTextStyle.LabelLarge    -> theme.typography.labelLarge
        AppTextStyle.LabelSmall    -> theme.typography.labelSmall
    }

    val textColor = when {
        color != Color.Unspecified -> color
        muted                       -> theme.colors.onSurfaceVariant
        else                        -> theme.colors.onSurface
    }

    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle.copy(color = textColor),
        maxLines = maxLines,
        overflow = overflow,
    )
}
```

---

