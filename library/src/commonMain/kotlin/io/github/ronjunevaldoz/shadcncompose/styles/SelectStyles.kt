@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

sealed interface SelectVariant {
    data object Default : SelectVariant
}

@Composable
fun SelectVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(this, colors, shapes) {
        Style {
            background(colors.surface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
        }
    }
}

@Composable
fun rememberSelectItemStyle(isSelected: Boolean): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(isSelected, colors, shapes) {
        Style {
            background(if (isSelected) colors.secondary else colors.surface)
            contentColor(if (isSelected) colors.onSecondary else colors.onSurface)
            shape(RoundedCornerShape(shapes.sm))
        }
    }
}
