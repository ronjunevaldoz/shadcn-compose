# Step 1: Styles for new components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 1: Add styles for new components

### `styles/CheckboxStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

sealed interface CheckboxVariant {
    val checkedStyle: Style
    val uncheckedStyle: Style

    data object Default : CheckboxVariant {
        override val checkedStyle = Style {
            background(colors.primary)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.sm))
            contentColor(colors.onPrimary)
        }
        override val uncheckedStyle = Style {
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.sm))
            hovered { animate { borderColor(colors.onSurface) } }
        }
    }

    data object Destructive : CheckboxVariant {
        override val checkedStyle = Style {
            background(colors.destructive)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.sm))
            contentColor(colors.onDestructive)
        }
        override val uncheckedStyle = Default.uncheckedStyle
    }
}
```

### `styles/SwitchStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.style.Style
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

data class SwitchColors(
    val trackChecked: androidx.compose.ui.graphics.Color,
    val trackUnchecked: androidx.compose.ui.graphics.Color,
    val thumb: androidx.compose.ui.graphics.Color,
)

object SwitchDefaults {
    @Composable
    fun colors() = SwitchColors(
        trackChecked   = AppTheme.LocalAppTheme.current.colors.primary,
        trackUnchecked = AppTheme.LocalAppTheme.current.colors.border,
        thumb          = AppTheme.LocalAppTheme.current.colors.background,
    )
}
```

### `styles/TabStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.style.Style
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.spacing

data class TabColors(
    val indicator: androidx.compose.ui.graphics.Color,
    val selected: androidx.compose.ui.graphics.Color,
    val unselected: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
)

object TabDefaults {
    @Composable
    fun colors() = TabColors(
        indicator  = AppTheme.LocalAppTheme.current.colors.primary,
        selected   = AppTheme.LocalAppTheme.current.colors.onSurface,
        unselected = AppTheme.LocalAppTheme.current.colors.onSurfaceVariant,
        background = AppTheme.LocalAppTheme.current.colors.background,
    )
}

sealed interface TabVariant {
    data object Line : TabVariant     // underline indicator (default)
    data object Pill : TabVariant     // filled pill indicator
    data object Enclosed : TabVariant // enclosed in a container
}
```

### `styles/AlertStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface AlertVariant {
    val style: Style

    data object Default : AlertVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Destructive : AlertVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Warning : AlertVariant {
        override val style = Style {
            background(colors.warning)
            contentColor(colors.onStatus)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Success : AlertVariant {
        override val style = Style {
            background(colors.success)
            contentColor(colors.onStatus)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }
}
```

---

