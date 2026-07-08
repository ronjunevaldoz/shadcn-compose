package io.github.ronjunevaldoz.shadcncompose.styles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScrollFadeTest {
    @Test
    fun leading_edge_does_not_fade_at_the_very_start() {
        assertFalse(shouldFadeLeadingEdge(scrollValue = 0))
    }

    @Test
    fun leading_edge_fades_once_scrolled_away_from_the_start() {
        assertTrue(shouldFadeLeadingEdge(scrollValue = 1))
    }

    @Test
    fun trailing_edge_fades_while_there_is_more_content_ahead() {
        assertTrue(shouldFadeTrailingEdge(scrollValue = 0, maxValue = 100))
    }

    @Test
    fun trailing_edge_does_not_fade_once_scrolled_to_the_very_end() {
        assertFalse(shouldFadeTrailingEdge(scrollValue = 100, maxValue = 100))
    }

    @Test
    fun fade_size_is_unchanged_when_it_comfortably_fits_the_container() {
        assertEquals(24f, clampedFadeSizePx(requestedPx = 24f, containerExtentPx = 400f))
    }

    @Test
    fun fade_size_is_clamped_to_half_the_container_so_opposite_fades_never_overlap() {
        assertEquals(20f, clampedFadeSizePx(requestedPx = 100f, containerExtentPx = 40f))
    }

    @Test
    fun fade_size_never_goes_negative_on_a_zero_size_container() {
        assertEquals(0f, clampedFadeSizePx(requestedPx = 24f, containerExtentPx = 0f))
    }
}
