package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A click-triggered anchored panel for rich content. Matches real shadcn/ui's
 * `popover.tsx` (`w-72 rounded-md border bg-popover p-4 shadow-md`).
 *
 * [width] defaults to that same fixed `w-72`(288dp) real shadcn always uses -- but real
 * shadcn's own recipes (e.g. its date-range-picker demo) override it per-instance with
 * `className="w-auto p-0"` when the content needs to size itself (a dual-month calendar
 * range picker, for one, is wider than 288dp and would otherwise get clipped). Pass
 * `width = null` for that same wrap-content sizing.
 *
 * Usage:
 * ```
 * var open by remember { mutableStateOf(false) }
 * Box {
 *     ShadcnButton(onClick = { open = true }) { ShadcnText("Open popover") }
 *     ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
 *         ShadcnText("Place content for the popover here.")
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnPopover(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Bottom,
    width: Dp? = 288.dp,
    content: @Composable () -> Unit,
) {
    ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = onDismissRequest, placement = placement) {
        Box(
            modifier =
                modifier
                    .let { if (width != null) it.width(width) else it }
                    .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                    .padding(shadcnTheme.spacing.lg),
        ) {
            content()
        }
    }
}
