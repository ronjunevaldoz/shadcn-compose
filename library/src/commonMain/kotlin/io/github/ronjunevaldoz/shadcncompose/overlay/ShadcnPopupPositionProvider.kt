package io.github.ronjunevaldoz.shadcncompose.overlay

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Preferred side of the anchor an anchored popup should open on. */
enum class ShadcnPopupPlacement { Top, Bottom, Start, End }

/**
 * Positions a [androidx.compose.ui.window.Popup]'s content relative to its anchor
 * (the trigger element), matching real shadcn/ui's Radix-based `Popover`/`DropdownMenu`/
 * `Tooltip`/etc: prefer [placement], flip to the opposite side if there isn't room, and
 * always clamp within the window so the popup never renders off-screen.
 *
 * This is the one positioning primitive every anchored overlay component
 * (Popover, DropdownMenu, Tooltip, ContextMenu, HoverCard, Combobox, Menubar,
 * NavigationMenu) shares -- see [ShadcnAnchoredPopup].
 */
class ShadcnPopupPositionProvider(
    private val placement: ShadcnPopupPlacement,
    private val offsetPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        var x: Int
        var y: Int

        when (placement) {
            ShadcnPopupPlacement.Bottom, ShadcnPopupPlacement.Top -> {
                x = anchorBounds.left
                y =
                    if (placement == ShadcnPopupPlacement.Bottom) {
                        anchorBounds.bottom + offsetPx
                    } else {
                        anchorBounds.top - popupContentSize.height - offsetPx
                    }
                // Flip vertically if there's no room on the preferred side.
                val overflowsBottom = y + popupContentSize.height > windowSize.height
                val overflowsTop = y < 0
                if (placement == ShadcnPopupPlacement.Bottom && overflowsBottom) {
                    val flipped = anchorBounds.top - popupContentSize.height - offsetPx
                    if (flipped >= 0) y = flipped
                } else if (placement == ShadcnPopupPlacement.Top && overflowsTop) {
                    val flipped = anchorBounds.bottom + offsetPx
                    if (flipped + popupContentSize.height <= windowSize.height) y = flipped
                }
            }

            ShadcnPopupPlacement.Start, ShadcnPopupPlacement.End -> {
                y = anchorBounds.top
                x =
                    if (placement == ShadcnPopupPlacement.End) {
                        anchorBounds.right + offsetPx
                    } else {
                        anchorBounds.left - popupContentSize.width - offsetPx
                    }
                val overflowsEnd = x + popupContentSize.width > windowSize.width
                val overflowsStart = x < 0
                if (placement == ShadcnPopupPlacement.End && overflowsEnd) {
                    val flipped = anchorBounds.left - popupContentSize.width - offsetPx
                    if (flipped >= 0) x = flipped
                } else if (placement == ShadcnPopupPlacement.Start && overflowsStart) {
                    val flipped = anchorBounds.right + offsetPx
                    if (flipped + popupContentSize.width <= windowSize.width) x = flipped
                }
            }
        }

        // Always clamp within the window on the cross axis, regardless of flip outcome --
        // real shadcn's Radix positioning does the same "collision padding" clamp.
        x = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        y = y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}
