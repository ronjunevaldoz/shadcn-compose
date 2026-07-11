@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Matches shadcn/ui's real input.tsx: border border-input (1.dp, always visible),
// focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 --
// `focusRingAlways(...)` grows the border to `theme.ring.width` on focus. Uses the
// "always" variant, not the ring-toggle-respecting `focusRing`, since the ring is the
// primary visual cue that a field is the one currently receiving keystrokes.
sealed interface TextFieldVariant {
    data object Default : TextFieldVariant

    data object Filled : TextFieldVariant

    data object Ghost : TextFieldVariant
}

@Composable
fun TextFieldVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            TextFieldVariant.Default ->
                Style {
                    background(colors.background)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                    fontSize(14.sp)
                    focusRingAlways(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }

            TextFieldVariant.Filled ->
                Style {
                    background(colors.surfaceVariant)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(Color.Transparent)
                    contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                    fontSize(14.sp)
                    focusRingAlways(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }

            TextFieldVariant.Ghost ->
                Style {
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(Color.Transparent)
                    contentPadding(horizontal = spacing.xs, vertical = spacing.xs)
                    fontSize(14.sp)
                    focusRingAlways(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }
        }
    }
