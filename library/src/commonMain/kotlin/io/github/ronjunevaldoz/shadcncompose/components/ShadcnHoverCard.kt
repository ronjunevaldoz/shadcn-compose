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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

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

    Box(modifier = modifier.hoverable(interactionSource)) {
        trigger()
        ShadcnAnchoredPopup(
            expanded = isHovered,
            onDismissRequest = {},
            placement = placement,
            dismissOnClickOutside = false,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(256.dp)
                        .background(shadcnTheme.colors.surface, RoundedCornerShape(shadcnTheme.shapes.md))
                        .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                        .padding(shadcnTheme.spacing.lg),
            ) {
                content()
            }
        }
    }
}
