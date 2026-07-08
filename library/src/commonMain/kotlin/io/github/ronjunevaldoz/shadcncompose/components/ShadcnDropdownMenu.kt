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

/**
 * Scope for composing a [ShadcnDropdownMenu]'s content -- callers freely mix
 * [ShadcnDropdownMenuItem]s, [ShadcnDropdownMenuLabel]s, and [ShadcnDropdownMenuSeparator]s
 * in any order, matching real shadcn/ui's `DropdownMenuContent`/`DropdownMenuGroup` composition
 * model (e.g. a "My Account" label, a group of items, a separator, then a destructive item).
 */
class ShadcnDropdownMenuScope internal constructor(internal val onDismissRequest: () -> Unit)

/**
 * An anchored list of actions. Matches real shadcn/ui's `dropdown-menu.tsx`
 * (`min-w-56 rounded-md border bg-popover p-1 shadow-md`, items
 * `rounded-sm px-2 py-1.5 text-sm`, destructive items in `text-destructive`).
 *
 * Usage:
 * ```
 * var open by remember { mutableStateOf(false) }
 * Box {
 *     ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
 *     ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
 *         ShadcnDropdownMenuLabel("My Account")
 *         ShadcnDropdownMenuSeparator()
 *         ShadcnDropdownMenuItem("Profile", onClick = {})
 *         ShadcnDropdownMenuItem("Billing", onClick = {})
 *         ShadcnDropdownMenuSeparator()
 *         ShadcnDropdownMenuItem("Log out", onClick = {}, destructive = true)
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Bottom,
    content: @Composable ShadcnDropdownMenuScope.() -> Unit,
) {
    ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = onDismissRequest, placement = placement) {
        Column(
            modifier =
                modifier
                    .width(224.dp)
                    .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                    .padding(shadcnTheme.spacing.xxs),
        ) {
            val scope = remember(onDismissRequest) { ShadcnDropdownMenuScope(onDismissRequest) }
            scope.content()
        }
    }
}

/** One clickable row in a [ShadcnDropdownMenu]/[ShadcnContextMenu]/[ShadcnMenubar]. */
@Composable
fun ShadcnDropdownMenuScope.ShadcnDropdownMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        onClick()
                        onDismissRequest()
                    },
                )
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.sm))
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
    ) {
        ShadcnText(
            label,
            style = ShadcnTextStyle.BodySmall,
            color =
                when {
                    destructive -> shadcnTheme.colors.error
                    !enabled -> shadcnTheme.colors.onSurfaceVariant
                    else -> shadcnTheme.colors.onPopover
                },
        )
    }
}

/** A non-interactive small heading row, matching real shadcn's `DropdownMenuLabel` (e.g. "My Account"). */
@Composable
fun ShadcnDropdownMenuScope.ShadcnDropdownMenuLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(
        text,
        style = ShadcnTextStyle.LabelSmall,
        modifier =
            modifier.fillMaxWidth().padding(
                horizontal = shadcnTheme.spacing.sm,
                vertical = shadcnTheme.spacing.xs,
            ),
    )
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
