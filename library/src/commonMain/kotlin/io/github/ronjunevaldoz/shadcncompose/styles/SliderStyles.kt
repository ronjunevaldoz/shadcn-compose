@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

// Matches shadcn/ui's real slider.tsx: track bg-muted, range bg-primary, thumb
// border border-primary bg-background with a ring on *both* hover and focus
// (unlike Button, which only rings on focus).
internal val sliderTrackStyle: Style =
    Style {
        background(colors.muted)
        shape(RoundedCornerShape(shapes.full))
    }

internal val sliderRangeStyle: Style =
    Style {
        background(colors.primary)
        shape(RoundedCornerShape(shapes.full))
    }

internal val sliderThumbStyle: Style =
    Style {
        background(colors.background)
        borderWidth(1.dp)
        borderColor(colors.primary)
        shape(RoundedCornerShape(shapes.full))
        hovered { dropShadow(Shadow(radius = 0.dp, spread = 4.dp, color = colors.borderFocus.copy(alpha = 0.5f))) }
        focused { dropShadow(Shadow(radius = 0.dp, spread = 4.dp, color = colors.borderFocus.copy(alpha = 0.5f))) }
        disabled { alpha(0.5f) }
    }
