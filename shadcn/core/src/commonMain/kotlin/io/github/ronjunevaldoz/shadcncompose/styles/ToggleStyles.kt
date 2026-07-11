@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.hovered
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Matches shadcn/ui's real toggle.tsx: shared hover:bg-muted, data-[state=on]:bg-accent
// (our `secondary` token stands in for `accent`, since they're the same value in
// the reference theme). `checked` is declared after `hovered` so the pressed-state
// color wins if both interaction states are active at once, same as CSS cascade order.
sealed interface ToggleVariant {
    data object Default : ToggleVariant

    data object Outline : ToggleVariant
}

@Composable
fun ToggleVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            ToggleVariant.Default ->
                Style {
                    background(Color.Transparent)
                    contentColor(colors.onSurface)
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                    height(36.dp)
                    minWidth(36.dp)
                    fontSize(14.sp)
                    hovered { background(colors.muted) }
                    checked { background(colors.secondary) }
                    // focusRing's shape() still yields to a *later* resolved shape --
                    // including a ShadcnToggleGroup style-param override with a
                    // corner-stripped shape -- since the ring always follows the
                    // *final* resolved shape(), regardless of which rule set it.
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }

            ToggleVariant.Outline ->
                Style {
                    background(Color.Transparent)
                    contentColor(colors.onSurface)
                    border(colors.border)
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                    height(36.dp)
                    minWidth(36.dp)
                    fontSize(14.sp)
                    hovered { background(colors.secondary) }
                    checked { background(colors.secondary) }
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }
        }
    }
