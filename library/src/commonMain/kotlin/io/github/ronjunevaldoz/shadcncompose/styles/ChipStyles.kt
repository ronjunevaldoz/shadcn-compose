@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnShapes
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing

// Chip isn't a real shadcn/ui component (shadcn only ships a static Badge), so
// there's no upstream reference here -- but it follows the same conventions
// established from Button/TextField/Checkbox/Radio/Switch/Toggle: the focus ring
// is drawn by Modifier.shadcnFocusRing (see ShadcnChip.kt), so no state change
// ever resizes the chip.
sealed interface ChipVariant {
    data object Default : ChipVariant
    data object Selected : ChipVariant
    data object Outline : ChipVariant
    // Easily inject unique modifiers per-instance if needed down the road
//    data class CustomTint(val customColor: Color) : ChipVariant
}

@Composable
fun ChipVariant.rememberStyle(): Style {
    // 1. Gather context from the active theme layer
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes
    val spacing = theme.spacing

    // 2. Cache the style; recalculate only if variant or theme changes
    return remember(this, colors) {
        when (this) {
            ChipVariant.Default -> Style {
                background(colors.secondary)
                contentColor(colors.onSecondary) // Dynamically resolves Color(0xFFFAFAFA) in dark mode!
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondaryHover) }
                pressed { background(colors.secondaryHover) }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }

            ChipVariant.Selected -> Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                borderWidth(1.dp)
                borderColor(colors.primary)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }

            ChipVariant.Outline -> Style {
                borderWidth(1.dp)
                borderColor(colors.border)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondary) }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }
        }
    }
}