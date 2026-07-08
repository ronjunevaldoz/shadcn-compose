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
    fun content_growing_while_following_auto_scrolls() {
        val shouldScroll = shouldAutoScrollToBottom(following = true, previousMaxValue = 1000, newMaxValue = 1200)
        assertTrue(shouldScroll)
    }

    @Test
    fun content_growing_while_released_does_not_yank_the_reader_back() {
        // Sticky release: even though this looks like "content grew," a reader who
        // scrolled away must not be pulled back just because new messages arrived.
        val shouldScroll = shouldAutoScrollToBottom(following = false, previousMaxValue = 1000, newMaxValue = 1200)
        assertFalse(shouldScroll)
    }

    @Test
    fun no_growth_never_auto_scrolls_even_while_following() {
        val shouldScroll = shouldAutoScrollToBottom(following = true, previousMaxValue = 1000, newMaxValue = 1000)
        assertFalse(shouldScroll)
    }

    @Test
    fun shrinking_content_never_auto_scrolls() {
        val shouldScroll = shouldAutoScrollToBottom(following = true, previousMaxValue = 1000, newMaxValue = 800)
        assertFalse(shouldScroll)
    }

    @Test
    fun first_content_arriving_from_empty_auto_scrolls_while_following() {
        val shouldScroll = shouldAutoScrollToBottom(following = true, previousMaxValue = 0, newMaxValue = 400)
        assertTrue(shouldScroll)
    }

    @Test
    fun scrolling_away_from_the_bottom_releases_following() {
        assertTrue(shouldReleaseFollowing(value = 500, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun staying_near_the_bottom_does_not_release_following() {
        assertFalse(shouldReleaseFollowing(value = 950, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun at_the_very_bottom_does_not_release_following() {
        assertFalse(shouldReleaseFollowing(value = 1000, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun exactly_at_the_release_threshold_does_not_release() {
        // isMessageScrollerNearBottom treats exactly-at-threshold as "still near," so
        // shouldReleaseFollowing (its negation) must not release at that same boundary.
        assertFalse(shouldReleaseFollowing(value = 936, maxValue = 1000, thresholdPx = 64))
    }

    @Test
    fun one_pixel_past_the_threshold_releases() {
        assertTrue(shouldReleaseFollowing(value = 935, maxValue = 1000, thresholdPx = 64))
    }
}
