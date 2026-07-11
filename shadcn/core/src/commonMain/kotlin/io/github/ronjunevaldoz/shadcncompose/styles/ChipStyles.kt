@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// Chip isn't a real shadcn/ui component (shadcn only ships a static Badge), so
// there's no upstream reference here -- but it follows the same conventions
// established from Button/TextField/Checkbox/Radio/Switch/Toggle: `focusRing(...)`
// handles the focus ring for every variant.
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

    // 2. Cache the style; recalculate only if variant or theme changes. Keyed on the
    // whole `theme` (superset of colors/shapes/spacing/ring) -- previously keyed on
    // just `colors`, a real bug (see AGENTS.md's "Component styling rules" #2) that
    // would have applied to `focusRing(...)`'s ring color/width too.
    return remember(this, theme, colors, shapes, spacing) {
        when (this) {
            ChipVariant.Default ->
                Style {
                    background(colors.secondary)
                    contentColor(colors.onSecondary) // Dynamically resolves Color(0xFFFAFAFA) in dark mode!
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    focusRing(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                    fontSize(13.sp)
                    hovered { background(colors.secondaryHover) }
                    pressed { background(colors.secondaryHover) }
                    disabledDim()
                }

            ChipVariant.Selected ->
                Style {
                    background(colors.primary)
                    contentColor(colors.onPrimary)
                    borderWidth(1.dp)
                    borderColor(colors.primary)
                    focusRing(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                    fontSize(13.sp)
                    disabledDim()
                }

            ChipVariant.Outline ->
                Style {
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    contentColor(colors.onSurface)
                    focusRing(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                    fontSize(13.sp)
                    hovered { background(colors.secondary) }
                    disabledDim()
                }
        }
    }
}
