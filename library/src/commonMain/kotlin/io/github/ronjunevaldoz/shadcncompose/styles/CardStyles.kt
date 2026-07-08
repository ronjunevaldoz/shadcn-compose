@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

sealed interface CardVariant {
    data object Default : CardVariant

    data object Elevated : CardVariant

    data object Filled : CardVariant
}

@Composable
fun CardVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes
    val spacing = theme.spacing

    return remember(this, colors, shapes, spacing) {
        when (this) {
            CardVariant.Default ->
                Style {
                    background(colors.card)
                    contentColor(colors.onCard)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    shape(RoundedCornerShape(shapes.xxl))
                    contentPadding(spacing.lg)
                }

            CardVariant.Elevated ->
                Style {
                    background(colors.card)
                    contentColor(colors.onCard)
                    shape(RoundedCornerShape(shapes.xxl))
                    contentPadding(spacing.lg)
                }

            CardVariant.Filled ->
                Style {
                    background(colors.surfaceVariant)
                    contentColor(colors.onCard)
                    shape(RoundedCornerShape(shapes.xxl))
                    contentPadding(spacing.lg)
                }
        }
    }
}

sealed interface CardSize {
    data object Default : CardSize

    data object Sm : CardSize
}

@Composable
fun CardSize.headerSpacing(): Dp {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return when (this) {
        CardSize.Default -> theme.spacing.sm
        CardSize.Sm -> theme.spacing.xs
    }
}
