package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.icons.Check
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.interaction.RovingFocusOrientation
import io.github.ronjunevaldoz.shadcncompose.interaction.rovingFocusGroup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPopupPlacement
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Scope for composing a [ShadcnDropdownMenu]'s content -- callers freely mix
 * [ShadcnDropdownMenuItem]s, [ShadcnDropdownMenuCheckboxItem]s, [ShadcnDropdownMenuRadioGroup]s,
 * [ShadcnDropdownMenuLabel]s, and [ShadcnDropdownMenuSeparator]s in any order, matching real
 * shadcn/ui's `DropdownMenuContent`/`DropdownMenuGroup` composition model (e.g. a "My Account"
 * label, a group of items, a separator, then a destructive item).
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
                    .padding(shadcnTheme.spacing.xxs)
                    .rovingFocusGroup(RovingFocusOrientation.Vertical),
        ) {
            val scope = remember(onDismissRequest) { ShadcnDropdownMenuScope(onDismissRequest) }
            scope.content()
        }
    }
}

/**
 * Shared row layout backing every interactive row in this file ([ShadcnDropdownMenuItem],
 * [ShadcnDropdownMenuCheckboxItem], [ShadcnDropdownMenuRadioItem]) -- real shadcn's
 * `dropdown-menu.tsx` gives every one of these the same `pl-8`-reserved leading slot (for a
 * checkmark/dot that may or may not be present) and the same right-aligned, muted
 * `DropdownMenuShortcut` trailing text, so one row shape serves all three instead of drifting.
 */
@Composable
private fun ShadcnDropdownMenuScope.ShadcnDropdownMenuRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    inset: Boolean = false,
    shortcut: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
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
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Reserved even when empty -- an unchecked/unselected row must still line its label up
        // with its checked/selected siblings, the same reason ShadcnCombobox's row reserves this.
        if (inset || leading != null) {
            Box(modifier = Modifier.size(shadcnTheme.icons.smallSize), contentAlignment = Alignment.Center) {
                leading?.invoke()
            }
        }
        ShadcnText(
            label,
            style = ShadcnTextStyle.BodySmall,
            modifier = Modifier.weight(1f),
            color =
                when {
                    destructive -> shadcnTheme.colors.error
                    !enabled -> shadcnTheme.colors.onSurfaceVariant
                    else -> shadcnTheme.colors.onPopover
                },
        )
        if (shortcut != null) {
            ShadcnText(shortcut, style = ShadcnTextStyle.LabelSmall, muted = true)
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
    inset: Boolean = false,
    shortcut: String? = null,
) {
    ShadcnDropdownMenuRow(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        destructive = destructive,
        inset = inset,
        shortcut = shortcut,
    )
}

/**
 * A togglable row with a checkmark leading slot, matching real shadcn's `DropdownMenuCheckboxItem`.
 * Selecting the row (like every other row in this menu) also dismisses the menu -- real only keeps
 * it open when the caller explicitly prevents the default select behavior, which this library's
 * String/callback-based API has no equivalent hook for.
 */
@Composable
fun ShadcnDropdownMenuScope.ShadcnDropdownMenuCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shortcut: String? = null,
) {
    ShadcnDropdownMenuRow(
        label = label,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        enabled = enabled,
        shortcut = shortcut,
        leading = {
            if (checked) ShadcnGlyphIcon(Check, tint = shadcnTheme.colors.onPopover, small = true)
        },
    )
}

/**
 * Groups a set of mutually-exclusive [ShadcnDropdownMenuRadioItem]s, matching real shadcn's
 * `DropdownMenuRadioGroup`. Real's version threads `value`/`onValueChange` through React context;
 * this holds the same pair on a scope object instead since Compose has no anonymous-context
 * equivalent for a plain function-scoped `content` lambda.
 */
class ShadcnDropdownMenuRadioGroupScope internal constructor(
    internal val menuScope: ShadcnDropdownMenuScope,
    internal val value: String,
    internal val onValueChange: (String) -> Unit,
)

@Composable
fun ShadcnDropdownMenuScope.ShadcnDropdownMenuRadioGroup(
    value: String,
    onValueChange: (String) -> Unit,
    content: @Composable ShadcnDropdownMenuRadioGroupScope.() -> Unit,
) {
    val groupScope =
        remember(this, value, onValueChange) { ShadcnDropdownMenuRadioGroupScope(this, value, onValueChange) }
    groupScope.content()
}

/** One row in a [ShadcnDropdownMenuRadioGroup], selected when its [value] matches the group's. */
@Composable
fun ShadcnDropdownMenuRadioGroupScope.ShadcnDropdownMenuRadioItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shortcut: String? = null,
) {
    val selected = value == this.value
    with(menuScope) {
        ShadcnDropdownMenuRow(
            label = label,
            onClick = { onValueChange(value) },
            modifier = modifier,
            enabled = enabled,
            shortcut = shortcut,
            leading = {
                if (selected) {
                    Box(modifier = Modifier.size(6.dp).background(shadcnTheme.colors.onPopover, CircleShape))
                }
            },
        )
    }
}

/**
 * A non-interactive small heading row, matching real shadcn's `DropdownMenuLabel` (e.g. "My Account").
 * [inset] lines the text up with sibling [ShadcnDropdownMenuCheckboxItem]/[ShadcnDropdownMenuRadioItem]
 * rows' labels, matching real's `inset` prop.
 */
@Composable
fun ShadcnDropdownMenuScope.ShadcnDropdownMenuLabel(
    text: String,
    modifier: Modifier = Modifier,
    inset: Boolean = false,
) {
    ShadcnText(
        text,
        style = ShadcnTextStyle.LabelSmall,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs)
                .let { if (inset) it.padding(start = shadcnTheme.icons.smallSize + shadcnTheme.spacing.xs) else it },
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
