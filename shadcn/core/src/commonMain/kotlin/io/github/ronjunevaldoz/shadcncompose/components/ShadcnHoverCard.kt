package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.delay

private const val HOVER_CARD_OPEN_DELAY_MS = 700L
private const val HOVER_CARD_CLOSE_DELAY_MS = 300L

/**
 * A hover-triggered panel for richer preview content (unlike [ShadcnTooltip], which is
 * text-only). Matches real shadcn/ui's `hover-card.tsx` (`w-64 rounded-md border
 * bg-popover p-4 shadow-md`).
 *
 * Usage:
 * ```
 * ShadcnHoverCard(trigger = { ShadcnText("@shadcn", modifier = Modifier.clickable {}) }) {
 *     ShadcnText("The React Framework – created and maintained by @vercel.")
 * }
 * ```
 */
@Composable
fun ShadcnHoverCard(
    modifier: Modifier = Modifier,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Bottom,
    trigger: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var expanded by remember { mutableStateOf(false) }

    // Real Radix/shadcn defaults: openDelay=700ms, closeDelay=300ms -- that delay is the whole
    // reason HoverCard exists as distinct from Tooltip (which opens instantly). Relaunching on
    // every isHovered flip cancels a pending open/close if the pointer moves back in time.
    LaunchedEffect(isHovered) {
        delay(if (isHovered) HOVER_CARD_OPEN_DELAY_MS else HOVER_CARD_CLOSE_DELAY_MS)
        expanded = isHovered
    }

    Box(modifier = modifier.hoverable(interactionSource)) {
        trigger()
        ShadcnAnchoredPopup(
            expanded = expanded,
            onDismissRequest = {},
            placement = placement,
            dismissOnClickOutside = false,
            // See ShadcnTooltip / ShadcnAnchoredPopup's `focusable` doc: a focusable
            // popup on a hover-triggered overlay flickers open/closed in a loop.
            focusable = false,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(256.dp)
                        .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                        .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                        .padding(shadcnTheme.spacing.lg),
            ) {
                content()
            }
        }
    }
}
