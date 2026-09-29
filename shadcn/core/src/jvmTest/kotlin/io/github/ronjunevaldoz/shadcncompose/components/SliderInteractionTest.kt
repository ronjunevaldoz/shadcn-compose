@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasRequestFocusAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import org.junit.Rule
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** How the slider responds to a pointer and keys; values are checked, not pixels. */
class SliderInteractionTest {
    @get:Rule val rule = createComposeRule()

    private fun assertNear(
        expected: Float,
        actual: Float,
        tolerance: Float,
    ) = assertTrue(abs(expected - actual) <= tolerance, "expected ≈$expected but was $actual")

    // a form that stores whole percents, like most real callers: the thumb must still follow the finger
    @Test
    fun draggingFollowsThePointerWhenTheCallerRoundsTheValue() {
        var percent by mutableIntStateOf(20)
        rule.setContent {
            ShadcnTheme {
                ShadcnSlider(
                    value = percent.toFloat(),
                    onValueChange = { percent = it.roundToInt() },
                    valueRange = 0f..100f,
                    modifier = Modifier.width(300.dp).testTag("slider"),
                )
            }
        }
        rule.onNodeWithTag("slider").performTouchInput {
            val y = centerY
            val thumbX = 8.dp.toPx() + (width - 16.dp.toPx()) * 0.20f // thumb centre at 20%
            down(Offset(thumbX, y))
            // many small moves: each alone is less than 1% of the track
            repeat(60) { moveBy(Offset((width - 16.dp.toPx()) * 0.005f, 0f)) } // +30% in total
            up()
        }
        rule.waitForIdle()
        assertNear(50f, percent.toFloat(), 1f)
    }

    // the thumb's centre lands where you press, including near the ends
    @Test
    fun pressingTheTrackMovesTheThumbCentreThere() {
        var value by mutableFloatStateOf(0f)
        rule.setContent {
            ShadcnTheme {
                ShadcnSlider(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.width(200.dp).testTag("slider"),
                )
            }
        }
        rule.onNodeWithTag("slider").performTouchInput {
            val usable = width - 16.dp.toPx()
            click(Offset(8.dp.toPx() + usable * 0.75f, centerY))
        }
        rule.waitForIdle()
        assertNear(0.75f, value, 0.01f)

        rule.onNodeWithTag("slider").performTouchInput { click(Offset(1f, centerY)) }
        rule.waitForIdle()
        assertEquals(0f, value)
    }

    // pressing anywhere and dragging (not only on the thumb) moves the value
    @Test
    fun pressingTheTrackAndDraggingMovesTheValue() {
        var value by mutableFloatStateOf(0f)
        var finished = 0
        rule.setContent {
            ShadcnTheme {
                ShadcnSlider(
                    value = value,
                    onValueChange = { value = it },
                    onValueChangeFinished = { finished++ },
                    modifier = Modifier.width(200.dp).testTag("slider"),
                )
            }
        }
        rule.onNodeWithTag("slider").performTouchInput {
            val usable = width - 16.dp.toPx()
            down(Offset(8.dp.toPx() + usable * 0.5f, centerY))
            moveTo(Offset(8.dp.toPx() + usable * 0.9f, centerY))
            up()
        }
        rule.waitForIdle()
        assertNear(0.9f, value, 0.01f)
        assertEquals(1, finished, "onValueChangeFinished once per gesture")
    }

    @Test
    fun stepsSnapTheValue() {
        var value by mutableFloatStateOf(0f)
        rule.setContent {
            ShadcnTheme {
                // 0, 25, 50, 75, 100
                ShadcnSlider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..100f,
                    steps = 3,
                    modifier = Modifier.width(200.dp).testTag("slider"),
                )
            }
        }
        rule.onNodeWithTag("slider").performTouchInput {
            click(Offset(8.dp.toPx() + (width - 16.dp.toPx()) * 0.62f, centerY))
        }
        rule.waitForIdle()
        assertEquals(50f, value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun arrowKeysStepTheValue() {
        var value by mutableFloatStateOf(50f)
        rule.setContent {
            ShadcnTheme {
                ShadcnSlider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.width(200.dp).testTag("slider"),
                )
            }
        }
        val thumb = rule.onNode(hasRequestFocusAction() and hasAnyAncestor(hasTestTag("slider")))
        thumb.requestFocus()
        thumb.performKeyInput {
            pressKey(Key.DirectionRight)
            pressKey(Key.DirectionRight)
        }
        rule.waitForIdle()
        assertEquals(52f, value)
        thumb.performKeyInput { pressKey(Key.MoveHome) }
        rule.waitForIdle()
        assertEquals(0f, value)
    }
}
