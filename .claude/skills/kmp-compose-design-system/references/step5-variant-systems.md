# Step 5: Variant Systems

Part of `kmp-compose-design-system`. Load this file when working on: step 5: variant systems.

---

> **Required in every style and component file:** add `@file:OptIn(ExperimentalStylesApi::class)`
> before the `package` line and `import androidx.compose.foundation.style.ExperimentalStylesApi`
> in the imports. The snippets below omit these for brevity — they are required for compilation.

### `styles/ButtonStyles.kt`

Mirrors shadcn Button: `default | outline | secondary | ghost | destructive | link`
Plus sizes: `xs | sm | md | lg | icon`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

// ── Interaction atoms (shared across variants) ────────────────────────────────

// Focus ring is COLOR-only, never width — borderWidth is reserved (transparent) or
// already present at rest per variant below, so focusing a button never re-measures
// or shifts its layout. This is the Compose equivalent of a CSS `ring` (box-shadow,
// outside the box model) instead of an animated `border` (inside the box model,
// jitters content when its width changes). See Style Rules → Ring vs border.
internal val buttonInteractionStyle = Style {
    hovered  { animate { alpha(0.90f) } }
    pressed  { animate { alpha(0.80f) } }
    disabled { animate { alpha(0.38f) } }
    focused  { animate { borderColor(colors.borderFocus) } }
}

// ── Variant styles ─────────────────────────────────────────────────────────────

sealed interface ButtonVariant : StyleVariant {
    override val style: Style

    data object Default : ButtonVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.md))
            // Reserved at rest — invisible until focused{} recolors it. Never animate
            // this width; only borderColor changes on focus (see buttonInteractionStyle).
            borderWidth(2.dp)
            borderColor(Color.Transparent)
        } then buttonInteractionStyle
    }

    data object Outline : ButtonVariant {
        override val style = Style {
            background(colors.background)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
            hovered { animate { background(colors.secondary) } }
            pressed { animate { background(colors.secondary) } }
        } then buttonInteractionStyle
    }

    data object Secondary : ButtonVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            shape(RoundedCornerShape(shapes.md))
            borderWidth(2.dp)
            borderColor(Color.Transparent)
            hovered { animate { background(colors.secondaryHover) } }
        } then buttonInteractionStyle
    }

    data object Ghost : ButtonVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.md))
            borderWidth(2.dp)
            borderColor(Color.Transparent)
            hovered { animate { background(colors.secondary) } }
            pressed { animate { background(colors.secondary) } }
        } then buttonInteractionStyle
    }

    data object Destructive : ButtonVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            shape(RoundedCornerShape(shapes.md))
            borderWidth(2.dp)
            borderColor(Color.Transparent)
            hovered { animate { background(colors.destructiveHover) } }
        } then buttonInteractionStyle
    }

    data object Link : ButtonVariant {
        override val style = Style {
            contentColor(colors.primary)
            hovered { animate { alpha(0.70f) } }
        }
    }
}

// ── Size styles ────────────────────────────────────────────────────────────────

sealed interface ButtonSize : StyleVariant {
    override val style: Style

    data object Xs : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.sm, vertical = spacing.xs)
            fontSize(12.sp)
            height(28.dp)
        }
    }

    data object Sm : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(14.sp)
            height(32.dp)
        }
    }

    data object Md : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.lg, vertical = spacing.sm)
            fontSize(14.sp)
            height(40.dp)
        }
    }

    data object Lg : ButtonSize {
        override val style = Style {
            padding(horizontal = spacing.xl, vertical = spacing.md)
            fontSize(16.sp)
            height(48.dp)
        }
    }

    data object Icon : ButtonSize {
        override val style = Style {
            padding(all = spacing.sm)
            width(40.dp)
            height(40.dp)
        }
    }
}
```

### `styles/BadgeStyles.kt`

Mirrors shadcn Badge: `default | secondary | destructive | outline | ghost`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface BadgeVariant : StyleVariant {
    override val style: Style

    data object Default : BadgeVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Secondary : BadgeVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Destructive : BadgeVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Outline : BadgeVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
            fontWeight(FontWeight.SemiBold)
        }
    }

    data object Ghost : BadgeVariant {
        override val style = Style {
            background(colors.muted)
            contentColor(colors.onMuted)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.sm, vertical = spacing.xxs)
            fontSize(12.sp)
        }
    }
}
```

### `styles/CardStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing
import GROUP_ID.core.designsystem.tokens.AppSpacing

sealed interface CardVariant : StyleVariant {
    override val style: Style

    data object Default : CardVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
        }
    }

    data object Elevated : CardVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
            // elevation via shadow — add Modifier.shadow in component
        }
    }

    data object Filled : CardVariant {
        override val style = Style {
            background(colors.surfaceVariant)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.xxl))
            padding(all = spacing.lg)
        }
    }
}

sealed interface CardSize {
    val contentPadding: androidx.compose.ui.unit.Dp
    val headerSpacing: androidx.compose.ui.unit.Dp

    data object Default : CardSize {
        override val contentPadding = AppSpacing().xxl  // 24.dp
        override val headerSpacing  = AppSpacing().sm   // 8.dp
    }
    data object Sm : CardSize {
        override val contentPadding = AppSpacing().lg   // 16.dp
        override val headerSpacing  = AppSpacing().xs   // 4.dp
    }
}
```

### `styles/ChipStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface ChipVariant : StyleVariant {
    override val style: Style

    data object Default : ChipVariant {
        override val style = Style {
            background(colors.secondary)
            contentColor(colors.onSecondary)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
            hovered { animate { background(colors.secondaryHover) } }
            pressed { animate { background(colors.secondaryHover) } }
        }
    }

    data object Selected : ChipVariant {
        override val style = Style {
            background(colors.primary)
            contentColor(colors.onPrimary)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
        }
    }

    data object Outline : ChipVariant {
        override val style = Style {
            borderWidth(1.dp)
            borderColor(colors.border)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(shapes.full))
            padding(horizontal = spacing.md, vertical = spacing.xs)
            fontSize(13.sp)
            hovered { animate { background(colors.secondary) } }
        }
    }
}
```

### `styles/TextFieldStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface TextFieldVariant : StyleVariant {
    override val style: Style

    data object Default : TextFieldVariant {
        override val style = Style {
            background(colors.background)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
            padding(horizontal = spacing.md, vertical = spacing.sm)
            fontSize(14.sp)
            // Width already reserved at rest (1.dp) — focus only recolors it, never resizes.
            focused { animate { borderColor(colors.borderFocus) } }
            disabled { animate { alpha(0.38f) } }
        }
    }

    data object Filled : TextFieldVariant {
        override val style = Style {
            background(colors.surfaceVariant)
            contentColor(colors.onSurface)
            shape(RoundedCornerShape(topStart = shapes.md, topEnd = shapes.md, bottomStart = 0.dp, bottomEnd = 0.dp))
            padding(horizontal = spacing.md, vertical = spacing.sm)
            fontSize(14.sp)
            // Reserved at rest — invisible until focused{} recolors it (no border by default).
            borderWidth(2.dp)
            borderColor(Color.Transparent)
            focused { animate { borderColor(colors.borderFocus) } }
        }
    }

    data object Ghost : TextFieldVariant {
        override val style = Style {
            contentColor(colors.onSurface)
            padding(horizontal = spacing.xs, vertical = spacing.xs)
            fontSize(14.sp)
            // Reserved at rest — invisible until focused{} recolors it (no underline by default).
            borderBottomWidth(1.dp)
            borderColor(Color.Transparent)
            focused { animate { borderColor(colors.borderFocus) } }
        }
    }
}
```

---

