package io.github.ronjunevaldoz.shadcncompose.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnScrollThumbMathTest {
    @Test
    fun thumb_fraction_matches_viewport_share_of_the_track() {
        // 100px viewport, 300px total content (100 visible + 200 scrollable) -> thumb covers 1/3 of the track.
        val fraction = scrollThumbFraction(viewportSize = 100, trackExtent = 300)
        assertEquals(1f / 3f, fraction)
    }

    @Test
    fun thumb_fraction_is_clamped_so_it_never_disappears_on_very_long_content() {
        val fraction = scrollThumbFraction(viewportSize = 1, trackExtent = 100_000)
        assertEquals(0.05f, fraction)
    }

    @Test
    fun thumb_fraction_is_full_when_content_exactly_fills_the_viewport() {
        val fraction = scrollThumbFraction(viewportSize = 200, trackExtent = 200)
        assertEquals(1f, fraction)
    }

    @Test
    fun offset_fraction_is_zero_at_the_top_of_the_scroll_range() {
        val fraction = scrollThumbOffsetFraction(value = 0, trackExtent = 300)
        assertEquals(0f, fraction)
    }

    @Test
    fun offset_fraction_advances_proportionally_with_scroll_value() {
        // Scrolled 100px into a 300px track -> thumb should sit 1/3 of the way down.
        val fraction = scrollThumbOffsetFraction(value = 100, trackExtent = 300)
        assertEquals(1f / 3f, fraction)
    }

    @Test
    fun drag_delta_is_scaled_up_because_the_thumb_travels_less_than_the_content_scrolls() {
        // 400px track, thumb is 100px long -> thumb only travels 300px while content scrolls
        // maxValue=600px total -> dragging the thumb by 30px (1/10 of its travel) should
        // scroll the content by 60px (1/10 of maxValue).
        val delta = scrollDragDeltaToContentDelta(dragDeltaPx = 30f, maxValue = 600, travelPx = 300f)
        assertEquals(60f, delta)
    }

    @Test
    fun drag_delta_is_zero_when_there_is_no_room_for_the_thumb_to_travel() {
        // travelPx <= 0 means the thumb already fills the entire track (nothing to scroll);
        // dividing by it would be a div-by-zero, so this must short-circuit to zero instead.
        val delta = scrollDragDeltaToContentDelta(dragDeltaPx = 50f, maxValue = 0, travelPx = 0f)
        assertEquals(0f, delta)
    }

    @Test
    fun dragging_the_full_travel_distance_reaches_exactly_maxValue() {
        val delta = scrollDragDeltaToContentDelta(dragDeltaPx = 300f, maxValue = 600, travelPx = 300f)
        assertEquals(600f, delta)
    }
}
