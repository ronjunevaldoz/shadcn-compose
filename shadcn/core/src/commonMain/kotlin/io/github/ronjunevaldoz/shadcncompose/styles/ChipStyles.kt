@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
fun ChipVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
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
