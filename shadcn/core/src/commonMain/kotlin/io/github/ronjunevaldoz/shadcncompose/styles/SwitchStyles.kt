@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

// Matches shadcn/ui's real switch.tsx: border border-transparent (1.dp, constant),
// data-[state=unchecked]:bg-input (not muted), data-[state=checked]:bg-primary,
// focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50.
// No hover state defined.
@Composable
fun rememberSwitchTrackStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return remember(theme, theme.colors, theme.shapes) {
        Style {
            background(theme.colors.border)
            shape(RoundedCornerShape(theme.shapes.full))
            borderWidth(1.dp)
            borderColor(Color.Transparent)
            checked { background(theme.colors.primary) }
            focused {
                borderColor(theme.colors.borderFocus)
                dropShadow(theme.focusRingShadow())
            }
            disabled { alpha(0.5f) }
        }
    }
}
