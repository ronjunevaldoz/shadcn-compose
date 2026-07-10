@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real toggle.tsx: shared hover:bg-muted, data-[state=on]:bg-accent
// (our `secondary` token stands in for `accent`, since they're the same value in
// the reference theme). `checked` is declared after `hovered` so the pressed-state
// color wins if both interaction states are active at once, same as CSS cascade order.
sealed interface ToggleVariant {
    data object Default : ToggleVariant

    data object Outline : ToggleVariant
}

@Composable
fun ToggleVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes
    val spacing = theme.spacing

    return remember(this, theme, colors, shapes, spacing) {
        when (this) {
            ToggleVariant.Default ->
                Style {
                    background(Color.Transparent)
                    contentColor(colors.onSurface)
                    shape(RoundedCornerShape(shapes.lg))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                    height(36.dp)
                    minWidth(36.dp)
                    fontSize(14.sp)
                    hovered { background(colors.muted) }
                    checked { background(colors.secondary) }
                    // dropShadow follows the *final* resolved shape() -- including a
                    // later ShadcnToggleGroup style-param override with a corner-stripped
                    // shape -- not just whatever shape() this block itself declares, same
                    // as real CSS box-shadow always following the element's own
                    // border-radius regardless of which rule set it.
                    focused { dropShadow(theme.focusRingShadow()) }
                    disabled { alpha(0.5f) }
                }

            ToggleVariant.Outline ->
                Style {
                    background(Color.Transparent)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    shape(RoundedCornerShape(shapes.lg))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                    height(36.dp)
                    minWidth(36.dp)
                    fontSize(14.sp)
                    hovered { background(colors.secondary) }
                    checked { background(colors.secondary) }
                    focused {
                        borderColor(colors.borderFocus)
                        dropShadow(theme.focusRingShadow())
                    }
                    disabled { alpha(0.5f) }
                }
        }
    }
}
