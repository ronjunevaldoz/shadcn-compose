package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A small hover-triggered label. Matches real shadcn/ui's `tooltip.tsx`
 * (inverse `bg-foreground`/`text-background` chip, no arrow -- Compose has no simple
 * cross-platform equivalent to Radix's `Arrow` primitive, so this is a deliberate
 * approximation).
 *
 * Real shadcn defaults `TooltipProvider`'s `delayDuration` to 0 (instant); this
 * matches that -- shows immediately on hover, no debounce.
 *
 * Usage:
 * ```
 * ShadcnTooltip(text = "Add to library") {
 *     ShadcnButton(onClick = {}) { ShadcnText("+") }
 * }
 * ```
 */
@Composable
fun ShadcnTooltip(
    text: String,
    modifier: Modifier = Modifier,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Top,
    trigger: @Composable () -> Unit,
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
                        .background(shadcnTheme.colors.onSurface, RoundedCornerShape(shadcnTheme.shapes.md))
                        .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.xs),
            ) {
                ShadcnText(text, style = ShadcnTextStyle.LabelSmall, color = shadcnTheme.colors.background)
            }
        }
    }
}
