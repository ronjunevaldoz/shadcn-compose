package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One row in a [ShadcnDropdownMenu]. */
data class ShadcnDropdownMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val destructive: Boolean = false,
)

/**
 * An anchored list of actions. Matches real shadcn/ui's `dropdown-menu.tsx`
 * (`min-w-32 rounded-md border bg-popover p-1 shadow-md`, items
 * `rounded-sm px-2 py-1.5 text-sm`, destructive items in `text-destructive`).
 *
 * Usage:
 * ```
 * var open by remember { mutableStateOf(false) }
 * Box {
 *     ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
 *     ShadcnDropdownMenu(
 *         expanded = open,
 *         onDismissRequest = { open = false },
 *         items = listOf(
 *             ShadcnDropdownMenuItem("Edit", onClick = {}),
 *             ShadcnDropdownMenuItem("Delete", onClick = {}, destructive = true),
 *         ),
 *     )
 * }
 * ```
 */
@Composable
fun ShadcnDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<ShadcnDropdownMenuItem>,
    modifier: Modifier = Modifier,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Bottom,
) {
    ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = onDismissRequest, placement = placement) {
        Column(
            modifier =
                modifier
                    .width(160.dp)
                    .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                    .padding(shadcnTheme.spacing.xxs),
        ) {
            items.forEach { item ->
                DropdownMenuRow(item = item, onDismissRequest = onDismissRequest)
            }
        }
    }
}

@Composable
internal fun DropdownMenuRow(
    item: ShadcnDropdownMenuItem,
    onDismissRequest: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = item.enabled,
                    onClick = {
                        item.onClick()
                        onDismissRequest()
                    },
                )
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.sm))
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
    ) {
        ShadcnText(
            item.label,
            style = ShadcnTextStyle.BodySmall,
            color =
                when {
                    item.destructive -> shadcnTheme.colors.error
                    !item.enabled -> shadcnTheme.colors.onSurfaceVariant
                    else -> shadcnTheme.colors.onPopover
                },
        )
    }
}

/** A thin divider between rows in a [ShadcnDropdownMenu]. */
@Composable
fun ShadcnDropdownMenuSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 4.dp)
                .background(shadcnTheme.colors.border),
    )
}
