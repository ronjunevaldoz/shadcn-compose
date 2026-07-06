@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.triStateToggleIndeterminate
import androidx.compose.foundation.style.triStateToggleOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

// Border width is constant across every state (see ButtonStyles.kt for why) --
// only the color changes, so checking/focusing never reflows surrounding layout.
internal val checkboxStyle: Style =
    Style {
        background(Color.Transparent)
        borderWidth(2.dp)
        borderColor(colors.border)
        shape(RoundedCornerShape(shapes.xs))
        triStateToggleOn {
            background(colors.primary)
            borderColor(colors.primary)
        }
        triStateToggleIndeterminate {
            background(colors.primary)
            borderColor(colors.primary)
        }
        focused { borderColor(colors.borderFocus) }
        disabled { alpha(0.38f) }
    }
