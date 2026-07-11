package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    shape(RoundedCornerShape(shapes.lg))
                }

            AlertVariant.Destructive ->
                Style {
                    background(colors.surface)
                    contentColor(colors.error)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    shape(RoundedCornerShape(shapes.lg))
                }
        }
    }
