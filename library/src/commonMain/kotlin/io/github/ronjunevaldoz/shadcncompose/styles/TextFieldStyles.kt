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

sealed interface TextFieldVariant {
    val style: Style

    data object Default : TextFieldVariant {
        // Border width stays 2.dp in every state -- only the color changes on
        // focus -- so focusing the field never reflows surrounding layout.
        override val style =
            Style {
                background(colors.background)
                contentColor(colors.onSurface)
                borderWidth(2.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.38f) }
            }
    }

    data object Filled : TextFieldVariant {
        override val style =
            Style {
                background(colors.surfaceVariant)
                contentColor(colors.onSurface)
                borderWidth(2.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
            }
    }

    data object Ghost : TextFieldVariant {
        override val style =
            Style {
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(Color.Transparent)
                contentPadding(horizontal = spacing.xs, vertical = spacing.xs)
                fontSize(14.sp)
                focused { borderColor(colors.borderFocus) }
            }
    }
}
