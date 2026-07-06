@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.focused
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.colors

/**
 * shadcn's real focus indicator (`focus-visible:ring-[3px] focus-visible:ring-ring/50`)
 * is a box-shadow-style ring drawn *outside* the layout box -- CSS box-shadow never
 * participates in layout sizing. [dropShadow] is the Style API's equivalent: it's a
 * paint-only effect, so composing this into any component's style is what actually
 * fixes the "resizes on focus" bug, not reserving extra border width like earlier
 * components did.
 */
internal val focusRingStyle: Style =
    Style {
        focused {
            dropShadow(Shadow(radius = 0.dp, spread = 3.dp, color = colors.borderFocus.copy(alpha = 0.5f)))
        }
    }
