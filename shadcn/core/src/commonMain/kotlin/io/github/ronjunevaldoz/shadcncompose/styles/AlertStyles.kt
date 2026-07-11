package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable

sealed interface AlertVariant {
    data object Default : AlertVariant

    data object Destructive : AlertVariant
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun AlertVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            AlertVariant.Default ->
                Style {
                    background(colors.surface)
                    contentColor(colors.onSurface)
                    border(colors.border)
                    shape(RoundedCornerShape(shapes.lg))
                }

            AlertVariant.Destructive ->
                Style {
                    background(colors.surface)
                    contentColor(colors.error)
                    border(colors.border)
                    shape(RoundedCornerShape(shapes.lg))
                }
        }
    }
