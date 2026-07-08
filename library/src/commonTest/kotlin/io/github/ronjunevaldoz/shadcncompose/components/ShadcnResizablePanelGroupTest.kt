package io.github.ronjunevaldoz.shadcncompose.components

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnResizablePanelGroupTest {
    private fun assertApproximately(
        expected: Float,
        actual: Float,
    ) {
        assertTrue(abs(expected - actual) < 0.0001f, "expected $expected but was $actual")
    }

    @Test
    fun dragging_right_increases_the_first_panels_share() {
        // 50% split in a 400px container; drag the handle 40px to the right.
        val fraction = resizablePanelFraction(0.5f, dragDeltaPx = 40f, containerExtentPx = 400f, 0.15f, 0.85f)
        assertApproximately(0.6f, fraction)
    }

    @Test
    fun dragging_left_decreases_the_first_panels_share() {
        val fraction = resizablePanelFraction(0.5f, dragDeltaPx = -40f, containerExtentPx = 400f, 0.15f, 0.85f)
        assertApproximately(0.4f, fraction)
    }

    @Test
    fun dragging_past_the_minimum_clamps_instead_of_collapsing_the_first_panel() {
        val fraction = resizablePanelFraction(0.5f, dragDeltaPx = -1000f, containerExtentPx = 400f, 0.15f, 0.85f)
        assertEquals(0.15f, fraction)
    }

    @Test
    fun dragging_past_the_maximum_clamps_instead_of_collapsing_the_second_panel() {
        val fraction = resizablePanelFraction(0.5f, dragDeltaPx = 1000f, containerExtentPx = 400f, 0.15f, 0.85f)
        assertEquals(0.85f, fraction)
    }

    @Test
    fun dragging_before_the_container_is_measured_is_a_no_op_not_a_divide_by_zero() {
        val fraction = resizablePanelFraction(0.5f, dragDeltaPx = 40f, containerExtentPx = 0f, 0.15f, 0.85f)
        assertEquals(0.5f, fraction)
    }
}
