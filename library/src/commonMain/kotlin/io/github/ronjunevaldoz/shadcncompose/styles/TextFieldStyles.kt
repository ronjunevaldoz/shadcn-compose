@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

sealed interface TextFieldVariant {
    val style: Style

    data object Default : TextFieldVariant {
        override val style =
            Style {
                background(colors.background)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused {
                    borderWidth(2.dp)
                    borderColor(colors.borderFocus)
                }
                disabled { alpha(0.38f) }
            }
    }

    data object Filled : TextFieldVariant {
        override val style =
            Style {
                background(colors.surfaceVariant)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.md, vertical = spacing.sm)
                fontSize(14.sp)
                focused {
                    borderWidth(2.dp)
                    borderColor(colors.borderFocus)
                }
            }
    }

    data object Ghost : TextFieldVariant {
        override val style =
            Style {
                contentColor(colors.onSurface)
                contentPadding(horizontal = spacing.xs, vertical = spacing.xs)
                fontSize(14.sp)
                focused {
                    borderWidth(1.dp)
                    borderColor(colors.borderFocus)
                }
            }
    }
}
