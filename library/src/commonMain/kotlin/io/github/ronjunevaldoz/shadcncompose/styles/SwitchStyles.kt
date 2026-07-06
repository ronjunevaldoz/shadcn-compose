@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes

internal val switchTrackStyle: Style =
    Style {
        background(colors.muted)
        shape(RoundedCornerShape(shapes.full))
        borderWidth(2.dp)
        borderColor(Color.Transparent)
        checked { background(colors.primary) }
        focused { borderColor(colors.borderFocus) }
        disabled { alpha(0.38f) }
    }
