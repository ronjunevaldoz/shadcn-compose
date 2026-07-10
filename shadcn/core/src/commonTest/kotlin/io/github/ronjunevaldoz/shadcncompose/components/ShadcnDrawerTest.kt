package io.github.ronjunevaldoz.shadcncompose.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnDrawerTest {
    @Test
    fun a_small_drag_below_the_threshold_springs_back_open() {
        // 400px tall drawer, dragged only 50px (12.5%) toward closing -- below the 30% default.
        val dismiss = shouldDismissDrawer(dragOffsetPx = 50f, contentExtentPx = 400f, thresholdFraction = 0.3f)
        assertFalse(dismiss)
    }

    @Test
    fun a_drag_past_the_threshold_dismisses() {
        // 130px of 400px is 32.5%, past the 30% default threshold.
        val dismiss = shouldDismissDrawer(dragOffsetPx = 130f, contentExtentPx = 400f, thresholdFraction = 0.3f)
        assertTrue(dismiss)
    }

    @Test
    fun a_drag_exactly_at_the_threshold_dismisses() {
        val dismiss = shouldDismissDrawer(dragOffsetPx = 120f, contentExtentPx = 400f, thresholdFraction = 0.3f)
        assertTrue(dismiss)
    }

    @Test
    fun zero_drag_never_dismisses() {
        val dismiss = shouldDismissDrawer(dragOffsetPx = 0f, contentExtentPx = 400f, thresholdFraction = 0.3f)
        assertFalse(dismiss)
    }

    @Test
    fun an_unmeasured_drawer_never_dismisses_rather_than_dividing_by_zero() {
        val dismiss = shouldDismissDrawer(dragOffsetPx = 50f, contentExtentPx = 0f, thresholdFraction = 0.3f)
        assertFalse(dismiss)
    }

    @Test
    fun a_lower_threshold_dismisses_with_a_shorter_drag() {
        // Same 50px drag that didn't clear a 30% threshold does clear a 10% one.
        val dismiss = shouldDismissDrawer(dragOffsetPx = 50f, contentExtentPx = 400f, thresholdFraction = 0.1f)
        assertTrue(dismiss)
    }
}
