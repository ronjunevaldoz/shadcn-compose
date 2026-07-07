@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.selected
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real radio-group.tsx: border border-input (1.dp, constant,
// no hover state defined), focus-visible:border-ring. The ring-[3px] ring-ring/50
// focus ring is drawn by Modifier.shadcnFocusRing (see ShadcnRadioButton.kt).
@Composable
fun rememberRadioButtonStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(colors, shapes) {
        Style {
            background(Color.Transparent)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.full))
            selected {
                background(colors.primary)
                borderColor(colors.primary)
            }
            focused { borderColor(colors.borderFocus) }
            disabled { alpha(0.5f) }
        }
    }
}
