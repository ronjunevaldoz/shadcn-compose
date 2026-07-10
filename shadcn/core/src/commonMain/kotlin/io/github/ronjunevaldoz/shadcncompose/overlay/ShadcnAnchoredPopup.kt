package io.github.ronjunevaldoz.shadcncompose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * The shared building block for every anchor-positioned overlay (Popover, DropdownMenu,
 * Tooltip, ContextMenu, HoverCard, Combobox/Select dropdowns, Menubar, NavigationMenu):
 * a [Popup] placed relative to the composable it's called from (its "anchor"), flipped
 * to stay on-screen via [ShadcnPopupPositionProvider], dismissed on outside click or
 * Escape by default -- matching real shadcn/ui's shared Radix `Popper` primitive.
 *
 * Call this from inside the anchor's own composition scope (e.g. right after the
 * trigger `Row`/`Box` in the same parent) so the [Popup] measures relative to it.
 *
 * Usage:
 * ```
 * var open by remember { mutableStateOf(false) }
 * Box {
 *     ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
 *     ShadcnAnchoredPopup(expanded = open, onDismissRequest = { open = false }) {
 *         ShadcnText("Popover content")
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnAnchoredPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    placement: ShadcnPopupPlacement = ShadcnPopupPlacement.Bottom,
    offset: Dp = 4.dp,
    dismissOnClickOutside: Boolean = true,
    // Tooltip/HoverCard pass false: a hover-triggered popup has nothing worth tabbing
    // into and must never take keyboard focus away from whatever the user was actually
    // interacting with. Worse, a focusable popup competes with the trigger's own
    // hoverable() for pointer/focus ownership the instant it opens -- on desktop this
    // observably flickers the popup open/closed in a loop, because taking focus can
    // itself perturb the hover state that decided to open the popup in the first
    // place. Popover/DropdownMenu/Dialog/etc. still default to true: those are
    // click-triggered and their content (menu items, form fields) genuinely needs
    // keyboard focus and Escape-to-dismiss.
    focusable: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!expanded) return

    val offsetPx = with(LocalDensity.current) { offset.roundToPx() }
    val positionProvider = remember(placement, offsetPx) { ShadcnPopupPositionProvider(placement, offsetPx) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties =
            PopupProperties(
                focusable = focusable,
                dismissOnBackPress = focusable,
                dismissOnClickOutside = dismissOnClickOutside,
            ),
        content = content,
    )
}
