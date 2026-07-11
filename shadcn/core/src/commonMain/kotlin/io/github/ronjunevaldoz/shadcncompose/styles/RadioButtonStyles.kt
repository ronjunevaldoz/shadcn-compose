@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.selected
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Matches shadcn/ui's real radio-group.tsx: border border-input (1.dp, constant,
// no hover state defined), focus-visible:border-ring focus-visible:ring-[3px]
// focus-visible:ring-ring/50.
@Composable
fun rememberRadioButtonStyle(): Style =
    rememberShadcnStyle {
        Style {
            background(Color.Transparent)
            border(colors.border)
            selected {
                background(colors.primary)
                borderColor(colors.primary)
            }
            focusRing(RoundedCornerShape(shapes.full))
            disabledDim()
        }
    }
