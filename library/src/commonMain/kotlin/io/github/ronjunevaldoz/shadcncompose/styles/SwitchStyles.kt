@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.then
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

// Matches shadcn/ui's real switch.tsx: border border-transparent (1.dp, constant),
// data-[state=unchecked]:bg-input (not muted), data-[state=checked]:bg-primary,
// focus-visible:border-ring + focusRingStyle's ring. No hover state defined.
internal val switchTrackStyle: Style =
    Style {
        background(colors.border)
        shape(RoundedCornerShape(shapes.full))
        borderWidth(1.dp)
        borderColor(Color.Transparent)
        checked { background(colors.primary) }
        focused { borderColor(colors.borderFocus) }
        disabled { alpha(0.5f) }
    } then focusRingStyle
