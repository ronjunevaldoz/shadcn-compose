# Step 2: Primitive components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 2: Primitive components

### `components/AppIcon.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

sealed interface IconSize {
    val dp: Dp
    data object Xs  : IconSize { override val dp = 12.dp }
    data object Sm  : IconSize { override val dp = 16.dp }
    data object Md  : IconSize { override val dp = 20.dp }
    data object Lg  : IconSize { override val dp = 24.dp }
    data object Xl  : IconSize { override val dp = 32.dp }
}

/**
 * Usage:
 * ```
 * AppIcon(Icons.Default.Search)
 * AppIcon(Icons.Default.Close, size = IconSize.Sm, tint = colors.destructive)
 * ```
 */
@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.Md,
    tint: Color = Color.Unspecified,
) {
    val resolvedTint = if (tint == Color.Unspecified) appTheme.colors.onSurface else tint
    androidx.compose.foundation.Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier.size(size.dp),
        colorFilter = ColorFilter.tint(resolvedTint),
    )
}
```

### `components/AppIconButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ButtonVariant

/**
 * Icon-only button. Defaults to Ghost variant.
 *
 * Usage:
 * ```
 * AppIconButton(onClick = { navBack() }) {
 *     AppIcon(Icons.Default.ArrowBack, contentDescription = "Back")
 * }
 * AppIconButton(onClick = { delete() }, variant = ButtonVariant.Destructive) {
 *     AppIcon(Icons.Default.Delete, contentDescription = "Delete")
 * }
 * ```
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Ghost,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .styleable(styleState, variant.style, style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

### `components/AppLabel.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Form field label with optional required indicator.
 *
 * Usage:
 * ```
 * AppLabel(text = "Email address", required = true)
 * AppLabel(text = "Bio")
 * ```
 */
@Composable
fun AppLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AppText(
            text = text,
            style = AppTextStyle.LabelLarge,
            muted = !enabled,
        )
        if (required) {
            Spacer(Modifier.width(2.dp))
            AppText(
                text = "*",
                style = AppTextStyle.LabelLarge,
                color = AppTheme.LocalAppTheme.current.colors.destructive,
            )
        }
    }
}
```

### `components/AppSeparator.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppSeparator()                           // horizontal, full width
 * AppSeparator(vertical = true)            // vertical, full height
 * AppSeparator(thickness = 2.dp)
 * ```
 */
@Composable
fun AppSeparator(
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    thickness: Dp = 1.dp,
    color: Color = appTheme.colors.border,
) {
    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .then(
                if (vertical) Modifier.width(thickness).fillMaxHeight()
                else Modifier.height(thickness).fillMaxWidth()
            )
            .background(color),
    )
}
```

### `components/AppAvatar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

sealed interface AvatarSize {
    val dp: Dp
    data object Sm  : AvatarSize { override val dp = 32.dp }
    data object Md  : AvatarSize { override val dp = 40.dp }
    data object Lg  : AvatarSize { override val dp = 56.dp }
    data object Xl  : AvatarSize { override val dp = 72.dp }
}

// Default chrome — background + circular shape. Consumers override via the `style`
// escape hatch (e.g. a border for an "online" ring) without touching this file.
private val avatarDefaultStyle = Style {
    background(colors.secondary)
    shape(CircleShape)
}

/**
 * Usage:
 * ```
 * AppAvatar(initials = "RV")
 * AppAvatar(initials = "RV", size = AvatarSize.Lg)
 * AppAvatar(painter = painterResource(Res.drawable.ic_user), contentDescription = "Profile")
 * // One-off override — e.g. an "online" ring:
 * AppAvatar(initials = "RV", style = Style { borderWidth(2.dp); borderColor(Color.Green) })
 * ```
 */
@Composable
fun AppAvatar(
    modifier: Modifier = Modifier,
    initials: String? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    size: AvatarSize = AvatarSize.Md,
    style: Style = Style,        // ← empty; DO NOT set a default Style here
) {
    val theme = appTheme
    val styleState = remember { MutableStyleState() }   // static — no interaction to track
    Box(
        modifier = modifier
            .size(size.dp)
            .styleable(styleState, avatarDefaultStyle, style),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(size.dp),
            )
        } else if (initials != null) {
            AppText(
                text = initials.take(2).uppercase(),
                style = if (size.dp >= 56.dp) AppTextStyle.TitleSmall else AppTextStyle.LabelLarge,
                color = theme.colors.onSecondary,
            )
        }
    }
}
```

---

