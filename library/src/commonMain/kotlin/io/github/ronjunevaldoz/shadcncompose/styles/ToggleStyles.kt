@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.then
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// Matches shadcn/ui's real toggle.tsx: shared hover:bg-muted, data-[state=on]:bg-accent
// (our `secondary` token stands in for `accent`, since they're the same value in
// the reference theme). `checked` is declared after `hovered` so the pressed-state
// color wins if both interaction states are active at once, same as CSS cascade order.
sealed interface ToggleVariant {
    val style: Style

    data object Default : ToggleVariant {
        override val style =
            Style {
                background(Color.Transparent)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                fontSize(14.sp)
                hovered { background(colors.muted) }
                checked { background(colors.secondary) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Outline : ToggleVariant {
        override val style =
            Style {
                background(Color.Transparent)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                fontSize(14.sp)
                hovered { background(colors.secondary) }
                checked { background(colors.secondary) }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }
}
