package io.github.ronjunevaldoz.shadcncompose.interaction

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertEquals

class RovingFocusTest {
    // -- rovingFocusAction: pure key -> action mapping --

    @Test
    fun arrow_down_moves_forward_in_a_vertical_group() {
        val action = rovingFocusAction(Key.DirectionDown, KeyEventType.KeyDown, RovingFocusOrientation.Vertical)
        assertEquals(RovingFocusAction.MoveForward, action)
    }

    @Test
    fun arrow_up_moves_backward_in_a_vertical_group() {
        val action = rovingFocusAction(Key.DirectionUp, KeyEventType.KeyDown, RovingFocusOrientation.Vertical)
        assertEquals(RovingFocusAction.MoveBackward, action)
    }

    @Test
    fun arrow_right_moves_forward_in_a_horizontal_group() {
        val action = rovingFocusAction(Key.DirectionRight, KeyEventType.KeyDown, RovingFocusOrientation.Horizontal)
        assertEquals(RovingFocusAction.MoveForward, action)
    }

    @Test
    fun arrow_left_moves_backward_in_a_horizontal_group() {
        val action = rovingFocusAction(Key.DirectionLeft, KeyEventType.KeyDown, RovingFocusOrientation.Horizontal)
        assertEquals(RovingFocusAction.MoveBackward, action)
    }

    @Test
    fun vertical_arrows_are_ignored_in_a_horizontal_group() {
        assertEquals(
            RovingFocusAction.None,
            rovingFocusAction(Key.DirectionDown, KeyEventType.KeyDown, RovingFocusOrientation.Horizontal),
        )
        assertEquals(
            RovingFocusAction.None,
            rovingFocusAction(Key.DirectionUp, KeyEventType.KeyDown, RovingFocusOrientation.Horizontal),
        )
    }

    @Test
    fun home_jumps_to_first_regardless_of_orientation() {
        assertEquals(
            RovingFocusAction.JumpToFirst,
            rovingFocusAction(Key.MoveHome, KeyEventType.KeyDown, RovingFocusOrientation.Vertical),
        )
        assertEquals(
            RovingFocusAction.JumpToFirst,
            rovingFocusAction(Key.MoveHome, KeyEventType.KeyDown, RovingFocusOrientation.Horizontal),
        )
    }

    @Test
    fun end_jumps_to_last_regardless_of_orientation() {
        assertEquals(
            RovingFocusAction.JumpToLast,
            rovingFocusAction(Key.MoveEnd, KeyEventType.KeyDown, RovingFocusOrientation.Vertical),
        )
    }

    @Test
    fun key_up_events_are_ignored_even_for_navigation_keys() {
        val action = rovingFocusAction(Key.DirectionDown, KeyEventType.KeyUp, RovingFocusOrientation.Vertical)
        assertEquals(RovingFocusAction.None, action)
    }

    @Test
    fun unrelated_keys_are_ignored() {
        val action = rovingFocusAction(Key.A, KeyEventType.KeyDown, RovingFocusOrientation.Vertical)
        assertEquals(RovingFocusAction.None, action)
    }

    // -- RovingFocusIndex: pure wrap-around index math --

    @Test
    fun next_from_the_middle_of_a_list_moves_forward_by_one() {
        assertEquals(3, RovingFocusIndex.next(current = 2, count = 5))
    }

    @Test
    fun next_from_the_last_index_wraps_to_zero() {
        assertEquals(0, RovingFocusIndex.next(current = 4, count = 5))
    }

    @Test
    fun previous_from_the_middle_of_a_list_moves_backward_by_one() {
        assertEquals(1, RovingFocusIndex.previous(current = 2, count = 5))
    }

    @Test
    fun previous_from_the_first_index_wraps_to_the_last() {
        assertEquals(4, RovingFocusIndex.previous(current = 0, count = 5))
    }

    @Test
    fun first_is_always_zero_for_a_non_empty_list() {
        assertEquals(0, RovingFocusIndex.first(count = 5))
    }

    @Test
    fun last_is_count_minus_one() {
        assertEquals(4, RovingFocusIndex.last(count = 5))
    }

    @Test
    fun an_empty_list_returns_no_valid_index() {
        assertEquals(-1, RovingFocusIndex.next(current = 0, count = 0))
        assertEquals(-1, RovingFocusIndex.previous(current = 0, count = 0))
        assertEquals(-1, RovingFocusIndex.first(count = 0))
    }
}
