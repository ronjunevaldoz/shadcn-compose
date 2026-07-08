package io.github.ronjunevaldoz.shadcncompose.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnMessageScrollerTest {
    @Test
    fun near_bottom_within_threshold_is_true() {
        assertTrue(isMessageScrollerNearBottom(value = 936, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun exactly_at_threshold_is_near_bottom() {
        assertTrue(isMessageScrollerNearBottom(value = 936, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun scrolled_away_past_threshold_is_not_near_bottom() {
        assertFalse(isMessageScrollerNearBottom(value = 500, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun at_the_very_bottom_is_near_bottom() {
        assertTrue(isMessageScrollerNearBottom(value = 1000, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun no_scrollable_content_is_near_bottom() {
        assertTrue(isMessageScrollerNearBottom(value = 0, maxValue = 0, thresholdPx = 64))
    }

    @Test
    fun content_growing_while_reader_was_near_bottom_auto_scrolls() {
        val shouldScroll =
            shouldAutoScrollToBottom(scrollValue = 936, previousMaxValue = 1000, newMaxValue = 1200, thresholdPx = 64)
        assertTrue(shouldScroll)
    }

    @Test
    fun content_growing_while_reader_scrolled_up_into_history_does_not_yank_them_back() {
        val shouldScroll =
            shouldAutoScrollToBottom(scrollValue = 200, previousMaxValue = 1000, newMaxValue = 1200, thresholdPx = 64)
        assertFalse(shouldScroll)
    }

    @Test
    fun no_growth_never_auto_scrolls() {
        val shouldScroll =
            shouldAutoScrollToBottom(scrollValue = 936, previousMaxValue = 1000, newMaxValue = 1000, thresholdPx = 64)
        assertFalse(shouldScroll)
    }

    @Test
    fun shrinking_content_never_auto_scrolls() {
        val shouldScroll =
            shouldAutoScrollToBottom(scrollValue = 936, previousMaxValue = 1000, newMaxValue = 800, thresholdPx = 64)
        assertFalse(shouldScroll)
    }

    @Test
    fun first_content_arriving_from_empty_auto_scrolls() {
        // previousMaxValue == 0 (nothing to scroll yet) counts as "was at the bottom".
        val shouldScroll =
            shouldAutoScrollToBottom(scrollValue = 0, previousMaxValue = 0, newMaxValue = 400, thresholdPx = 64)
        assertTrue(shouldScroll)
    }
}
