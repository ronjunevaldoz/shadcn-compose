@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.selected
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

// Same constant-border-width rule as CheckboxStyles.kt -- only the color changes.
internal val radioButtonStyle: Style =
    Style {
        background(Color.Transparent)
        borderWidth(2.dp)
        borderColor(colors.border)
        shape(RoundedCornerShape(shapes.full))
        selected {
            background(colors.primary)
            borderColor(colors.primary)
        }
        focused { borderColor(colors.borderFocus) }
        disabled { alpha(0.38f) }
    }
