@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real slider.tsx: track bg-muted, range bg-primary, thumb
// border border-primary bg-background, ring on *both* hover and focus (unlike
// Button, which only rings on focus) -- real shadcn's own `hover:ring-4
// focus-visible:ring-4` on this specific component, two separate rules with the
// same properties (we don't have a per-component ring width override, so this
// still uses the shared theme.ring.width).
@Composable
fun rememberSliderTrackStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return remember(theme.colors, theme.shapes) {
        Style {
            background(theme.colors.muted)
            shape(RoundedCornerShape(theme.shapes.full))
        }
    }
}

@Composable
fun rememberSliderRangeStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return remember(theme.colors, theme.shapes) {
        Style {
            background(theme.colors.primary)
            shape(RoundedCornerShape(theme.shapes.full))
        }
    }
}

@Composable
fun rememberSliderThumbStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return remember(theme, theme.colors, theme.shapes) {
        Style {
            background(theme.colors.background)
            borderWidth(1.dp)
            borderColor(theme.colors.primary)
            hovered { dropShadow(theme.focusRingShadow()) }
            focusRing(RoundedCornerShape(theme.shapes.full))
            disabledDim()
        }
    }
}
