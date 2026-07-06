@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.hovered
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

sealed interface ToggleVariant {
    val style: Style

    data object Default : ToggleVariant {
        override val style =
            Style {
                background(Color.Transparent)
                contentColor(colors.onSurface)
                borderWidth(0.dp)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                fontSize(14.sp)
                hovered { background(colors.secondary) }
                checked { background(colors.secondary) }
                disabled { alpha(0.38f) }
            }
    }

    data object Outline : ToggleVariant {
        override val style =
            Style {
                background(Color.Transparent)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                fontSize(14.sp)
                hovered { background(colors.secondary) }
                checked { background(colors.secondary) }
                disabled { alpha(0.38f) }
            }
    }
}
