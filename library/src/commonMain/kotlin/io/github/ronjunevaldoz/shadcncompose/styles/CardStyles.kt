@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing

sealed interface CardVariant {
    val style: Style

    data object Default : CardVariant {
        override val style =
            Style {
                background(colors.surface)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.xxl))
                contentPadding(spacing.lg)
            }
    }

    data object Elevated : CardVariant {
        override val style =
            Style {
                background(colors.surface)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.xxl))
                contentPadding(spacing.lg)
            }
    }

    data object Filled : CardVariant {
        override val style =
            Style {
                background(colors.surfaceVariant)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.xxl))
                contentPadding(spacing.lg)
            }
    }
}

sealed interface CardSize {
    val headerSpacing: Dp

    data object Default : CardSize {
        override val headerSpacing = ShadcnSpacing().sm
    }

    data object Sm : CardSize {
        override val headerSpacing = ShadcnSpacing().xs
    }
}
