@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.then
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// Chip isn't a real shadcn/ui component (shadcn only ships a static Badge), so
// there's no upstream reference here -- but it follows the same conventions
// established from Button/TextField/Checkbox/Radio/Switch/Toggle: every variant
// reserves the same 1.dp border (invisible where not wanted) and shares the
// focusRingStyle drop-shadow ring, so no state change ever resizes the chip.
sealed interface ChipVariant {
    val style: Style

    data object Default : ChipVariant {
        override val style =
            Style {
                background(colors.secondary)
                contentColor(colors.onSecondary)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondaryHover) }
                pressed { background(colors.secondaryHover) }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Selected : ChipVariant {
        override val style =
            Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                borderWidth(1.dp)
                borderColor(colors.primary)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Outline : ChipVariant {
        override val style =
            Style {
                borderWidth(1.dp)
                borderColor(colors.border)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondary) }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }
}
