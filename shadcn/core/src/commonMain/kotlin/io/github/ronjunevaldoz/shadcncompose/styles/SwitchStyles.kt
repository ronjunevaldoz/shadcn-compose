@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Matches shadcn/ui's real switch.tsx: border border-transparent (1.dp, constant),
// data-[state=unchecked]:bg-input (not muted), data-[state=checked]:bg-primary,
// focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50.
// No hover state defined.
@Composable
fun rememberSwitchTrackStyle(): Style =
    rememberShadcnStyle {
        Style {
            background(colors.border)
            border(Color.Transparent)
            checked { background(colors.primary) }
            focusRing(RoundedCornerShape(shapes.full))
            disabledDim()
        }
    }
