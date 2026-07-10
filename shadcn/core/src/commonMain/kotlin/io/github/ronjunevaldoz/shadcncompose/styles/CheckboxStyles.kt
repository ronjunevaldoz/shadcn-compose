@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.triStateToggleIndeterminate
import androidx.compose.foundation.style.triStateToggleOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real checkbox.tsx: border border-input (1.dp, constant),
// data-[state=checked]:border-primary data-[state=checked]:bg-primary,
// focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50.
@Composable
fun rememberCheckboxStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(theme, colors, shapes) {
        Style {
            background(Color.Transparent)
            borderWidth(1.dp)
            borderColor(colors.border)
            triStateToggleOn {
                background(colors.primary)
                borderColor(colors.primary)
            }
            triStateToggleIndeterminate {
                background(colors.primary)
                borderColor(colors.primary)
            }
            focusRing(RoundedCornerShape(shapes.sm))
            disabled { alpha(0.5f) }
        }
    }
}
