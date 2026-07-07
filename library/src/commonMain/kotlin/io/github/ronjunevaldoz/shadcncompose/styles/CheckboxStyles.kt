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

// Matches shadcn/ui's real checkbox.tsx: border border-input (1.dp, constant),
// data-[state=checked]:border-primary data-[state=checked]:bg-primary,
// focus-visible:border-ring. The ring-[3px] ring-ring/50 focus ring is drawn by
// Modifier.shadcnFocusRing (see ShadcnCheckbox.kt), not here.
internal val checkboxStyle: Style get() =
    Style {
        background(Color.Transparent)
        borderWidth(1.dp)
        borderColor(colors.border)
        shape(RoundedCornerShape(shapes.sm))
        triStateToggleOn {
            background(colors.primary)
            borderColor(colors.primary)
        }
        triStateToggleIndeterminate {
            background(colors.primary)
            borderColor(colors.primary)
        }
        focused { borderColor(colors.borderFocus) }
        disabled { alpha(0.5f) }
    }
