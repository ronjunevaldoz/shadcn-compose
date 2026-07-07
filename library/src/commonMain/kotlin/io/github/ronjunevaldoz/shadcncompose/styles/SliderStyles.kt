@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

// Matches shadcn/ui's real slider.tsx: track bg-muted, range bg-primary, thumb
// border border-primary bg-background. The ring on *both* hover and focus (unlike
// Button, which only rings on focus) is drawn by Modifier.shadcnFocusRing in
// ShadcnSlider.kt, not here.
internal val sliderTrackStyle: Style get() =
    Style {
        background(colors.muted)
        shape(RoundedCornerShape(shapes.full))
    }

internal val sliderRangeStyle: Style get() =
    Style {
        background(colors.primary)
        shape(RoundedCornerShape(shapes.full))
    }

internal val sliderThumbStyle: Style get() =
    Style {
        background(colors.background)
        borderWidth(1.dp)
        borderColor(colors.primary)
        shape(RoundedCornerShape(shapes.full))
        disabled { alpha(0.5f) }
    }
