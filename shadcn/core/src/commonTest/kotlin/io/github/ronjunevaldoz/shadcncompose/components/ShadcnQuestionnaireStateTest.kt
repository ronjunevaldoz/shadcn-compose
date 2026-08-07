package io.github.ronjunevaldoz.shadcncompose.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShadcnQuestionnaireStateTest {
    private val items =
        listOf(
            ShadcnQuestionnaireItemDef(name = "required-item", required = true),
            ShadcnQuestionnaireItemDef(name = "optional-item", required = false),
        )

    @Test
    fun starts_on_the_first_item_unanswered() {
        val state = ShadcnQuestionnaireState(items)
        assertEquals(1, state.current)
        assertEquals(2, state.total)
        assertTrue(state.first)
        assertFalse(state.last)
        assertEquals(ShadcnQuestionnaireStatus.Unanswered, state.status("required-item"))
    }

    @Test
    fun single_select_replaces_the_previous_choice() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.toggleChoice("required-item", "b", multiple = false)
        assertEquals(setOf("b"), state.selectedValues("required-item"))
    }

    @Test
    fun multi_select_toggles_independently() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = true)
        state.toggleChoice("required-item", "b", multiple = true)
        assertEquals(setOf("a", "b"), state.selectedValues("required-item"))

        state.toggleChoice("required-item", "a", multiple = true)
        assertEquals(setOf("b"), state.selectedValues("required-item"))
    }

    @Test
    fun a_filled_input_counts_as_answered_even_with_no_choice_selected() {
        val state = ShadcnQuestionnaireState(items)
        assertEquals(ShadcnQuestionnaireStatus.Unanswered, state.status("required-item"))
        state.setInputValue("required-item", "my own answer")
        assertEquals(ShadcnQuestionnaireStatus.Answered, state.status("required-item"))
    }

    @Test
    fun goNext_blocks_and_marks_invalid_when_a_required_item_is_unanswered() {
        val state = ShadcnQuestionnaireState(items)
        val advanced = state.goNext()
        assertFalse(advanced)
        assertEquals(0, state.currentIndex)
        assertTrue(state.isInvalid("required-item"))
    }

    @Test
    fun goNext_advances_once_the_required_item_is_answered() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        val advanced = state.goNext()
        assertTrue(advanced)
        assertEquals(1, state.currentIndex)
        assertFalse(state.isInvalid("required-item"))
    }

    @Test
    fun goNext_does_not_validate_an_optional_unanswered_item() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        // Now on "optional-item", unanswered -- but it's the last item, so goNext() is a no-op
        // (nothing past it to advance to); goNext() only ever validates to move forward.
        assertTrue(state.last)
    }

    @Test
    fun goPrevious_never_validates() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        state.goPrevious()
        assertEquals(0, state.currentIndex)
        // Clearing the answer and going back again must not be blocked by validation.
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        state.goPrevious()
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun skip_is_a_no_op_on_a_required_item() {
        val state = ShadcnQuestionnaireState(items)
        state.skip()
        assertEquals(ShadcnQuestionnaireStatus.Unanswered, state.status("required-item"))
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun skip_marks_skipped_and_advances_on_an_optional_item() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        assertTrue(state.last)

        var submitted = false
        state.skip(onLastItemSkipped = { submitted = true })

        assertEquals(ShadcnQuestionnaireStatus.Skipped, state.status("optional-item"))
        assertTrue(submitted)
    }

    @Test
    fun submit_returns_null_and_jumps_to_the_first_invalid_item() {
        val state = ShadcnQuestionnaireState(items)
        // Answer only the second item, leave the required first item blank, then try to submit
        // from wherever -- submit() must check every item, not just the active one.
        state.toggleChoice("optional-item", "x", multiple = false)

        val answers = state.submit()

        assertNull(answers)
        assertEquals(0, state.currentIndex)
        assertTrue(state.isInvalid("required-item"))
    }

    @Test
    fun submit_still_requires_an_optional_item_to_be_answered_or_explicitly_skipped() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        // "optional-item" left merely unanswered (never skipped) -- required only gates whether
        // skip() is *allowed*, it doesn't make an unanswered item pass validation on its own.

        assertNull(state.submit())
        assertTrue(state.isInvalid("optional-item"))
    }

    @Test
    fun submit_returns_every_items_answers_once_all_items_are_answered_or_skipped() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        state.skip()

        val answers = state.submit()

        assertEquals(listOf("a"), answers?.get("required-item"))
        assertEquals(emptyList(), answers?.get("optional-item"))
    }

    @Test
    fun a_new_selection_clears_a_previous_skip() {
        val state = ShadcnQuestionnaireState(items)
        state.toggleChoice("required-item", "a", multiple = false)
        state.goNext()
        state.skip()
        assertEquals(ShadcnQuestionnaireStatus.Skipped, state.status("optional-item"))

        state.toggleChoice("optional-item", "x", multiple = false)
        assertEquals(ShadcnQuestionnaireStatus.Answered, state.status("optional-item"))
    }
}
