@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// Matches shadcn/ui's real input.tsx: border border-input (1.dp, always visible),
// focus-visible:border-ring (color swap only). The ring-[3px] ring-ring/50 focus
// ring is drawn by Modifier.shadcnFocusRing (see ShadcnTextField.kt), not here --
// border width never changes, so focusing never reflows layout.
sealed interface TextFieldVariant {
    val style: Style

    data object Default : TextFieldVariant {
        override val style get() =
            Style {
                background(colors.background)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.lg))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }
    }

    data object Filled : TextFieldVariant {
        override val style get() =
            Style {
                background(colors.surfaceVariant)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.lg))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }
    }

    data object Ghost : TextFieldVariant {
        override val style get() =
            Style {
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(Color.Transparent)
                contentPadding(horizontal = spacing.xs, vertical = spacing.xs)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            }
    }
}
