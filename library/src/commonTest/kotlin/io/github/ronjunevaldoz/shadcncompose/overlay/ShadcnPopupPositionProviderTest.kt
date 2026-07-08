package io.github.ronjunevaldoz.shadcncompose.overlay

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnPopupPositionProviderTest {
    private val window = IntSize(800, 600)
    private val ltr = LayoutDirection.Ltr

    @Test
    fun bottom_placement_sits_below_and_aligned_to_anchor_start() {
        val anchor = IntRect(100, 100, 180, 130) // 80x30 anchor
        val provider = ShadcnPopupPositionProvider(ShadcnPopupPlacement.Bottom, offsetPx = 4)
        val result = provider.calculatePosition(anchor, window, ltr, IntSize(120, 60))
        assertEquals(IntOffset(100, 134), result) // x = anchor.left, y = anchor.bottom + offset
    }

    @Test
    fun bottom_placement_flips_to_top_when_no_room_below() {
        val anchor = IntRect(100, 550, 180, 580) // near the bottom edge of a 600-tall window
        val provider = ShadcnPopupPositionProvider(ShadcnPopupPlacement.Bottom, offsetPx = 4)
        val result = provider.calculatePosition(anchor, window, ltr, IntSize(120, 100))
        // 580 + 4 + 100 = 684 > 600 (window height) -> flips above the anchor instead
        assertEquals(100, result.x)
        assertEquals(446, result.y) // anchor.top(550) - popupHeight(100) - offset(4)
    }

    @Test
    fun end_placement_flips_to_start_when_no_room_on_the_right() {
        val anchor = IntRect(700, 100, 780, 130)
        val provider = ShadcnPopupPositionProvider(ShadcnPopupPlacement.End, offsetPx = 4)
        val result = provider.calculatePosition(anchor, window, ltr, IntSize(150, 40))
        // 780 + 4 + 150 = 934 > 800 (window width) -> flips to the left of the anchor
        assertEquals(546, result.x) // anchor.left(700) - popupWidth(150) - offset(4)
        assertEquals(100, result.y)
    }

    @Test
    fun result_is_always_clamped_within_the_window() {
        val anchor = IntRect(-50, -50, -10, -20) // anchor mostly off the top-left edge
        val provider = ShadcnPopupPositionProvider(ShadcnPopupPlacement.Bottom, offsetPx = 4)
        val result = provider.calculatePosition(anchor, window, ltr, IntSize(120, 60))
        assertEquals(0, result.x) // clamped, would otherwise be negative
    }

    @Test
    fun pointPositionProvider_offsets_from_anchor_origin() {
        val anchor = IntRect(200, 200, 400, 400)
        val provider = ShadcnPointPositionProvider(IntOffset(30, 45))
        val result = provider.calculatePosition(anchor, window, ltr, IntSize(160, 80))
        assertEquals(IntOffset(230, 245), result)
    }
}
