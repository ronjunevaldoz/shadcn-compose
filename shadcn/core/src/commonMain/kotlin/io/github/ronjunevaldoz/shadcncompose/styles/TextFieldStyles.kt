@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real input.tsx: border border-input (1.dp, always visible),
// focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 --
// `focusRing(...)` grows the border to `theme.ring.width` on focus.
sealed interface TextFieldVariant {
    data object Default : TextFieldVariant

    data object Filled : TextFieldVariant

    data object Ghost : TextFieldVariant
}

@Composable
fun TextFieldVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes
    val spacing = theme.spacing

    return remember(this, theme, colors, shapes, spacing) {
        when (this) {
            TextFieldVariant.Default ->
                Style {
                    background(colors.background)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                    fontSize(14.sp)
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabled { alpha(0.5f) }
                }

            TextFieldVariant.Filled ->
                Style {
                    background(colors.surfaceVariant)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(Color.Transparent)
                    contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                    fontSize(14.sp)
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabled { alpha(0.5f) }
                }

            TextFieldVariant.Ghost ->
                Style {
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(Color.Transparent)
                    contentPadding(horizontal = spacing.xs, vertical = spacing.xs)
                    fontSize(14.sp)
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabled { alpha(0.5f) }
                }
        }
    }
}
