package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSliderRangeStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSliderThumbStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSliderTrackStyle

private val THUMB_SIZE = 16.dp
private val TRACK_HEIGHT = 6.dp

/**
 * Usage:
 * ```
 * var volume by remember { mutableStateOf(50f) }
 * ShadcnSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
 * ```
 *
 * Press anywhere on the slider (not only the thumb) and drag: the thumb's centre follows the pointer. [steps] snaps to
 * that many evenly spaced values between the ends (like Radix's `step`), and the arrow keys, Page Up/Down and Home/End
 * move a focused slider. [onValueChangeFinished] runs once when a drag or key press ends.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    style: Style = Style,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val rangeSpan = valueRange.endInclusive - valueRange.start
    val fraction = if (rangeSpan == 0f) 0f else ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    // read inside long-lived gesture/key handlers, so they always see this composition's values
    val currentValue by rememberUpdatedState(value)
    val currentRange by rememberUpdatedState(valueRange)
    val currentSteps by rememberUpdatedState(steps)
    val onChange by rememberUpdatedState(onValueChange)
    val onFinished by rememberUpdatedState(onValueChangeFinished)

    fun valueAt(fraction: Float): Float {
        val range = currentRange
        val span = range.endInclusive - range.start
        val snapped = if (currentSteps > 0) {
            val intervals = currentSteps + 1
            (fraction.coerceIn(0f, 1f) * intervals).roundToInt().toFloat() / intervals
        } else {
            fraction.coerceIn(0f, 1f)
        }
        return (range.start + snapped * span).coerceIn(range.start, range.endInclusive)
    }

    val interactionSource = remember { MutableInteractionSource() }
    // None of the three styles read isEnabled -- disabled dimming is the single
    // Modifier.alpha() below, not a per-style disabled { } rule -- so these StyleStates
    // exist only to satisfy styleable()'s required parameter; track/range don't need a
    // real interaction source either, since neither reads hover/press/focus.
    val thumbStyleState = rememberUpdatedStyleState(interactionSource)
    val trackStyleState = rememberUpdatedStyleState(null)
    val rangeStyleState = rememberUpdatedStyleState(null)

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(THUMB_SIZE)
                // A single group-level alpha, not each child dimming itself independently
                // (disabledDim() on all three, tried first) -- that made the thumb's own
                // now-translucent background blend with the *also-translucent* fill sitting
                // directly behind it, instead of one cleanly occluding the other, so the
                // fill kept showing through the thumb. Compositing the whole control first
                // (children stay fully opaque relative to each other, thumb still fully
                // covers the fill beneath it) and dimming that one flat result afterward
                // matches how CSS's disabled:opacity-50 behaves on a real DOM subtree.
                .alpha(if (enabled) 1f else 0.5f)
                // One handler for the whole control: a press anywhere moves the thumb's centre there, and dragging
                // keeps it under the pointer. Positions are absolute (the pointer's x), not accumulated deltas --
                // deltas drifted whenever the caller rounded the value (a percent slider overshot by a third), and
                // the old per-child handlers kept a stale width after a resize.
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val thumbPx = THUMB_SIZE.toPx()
                        fun moveTo(x: Float) {
                            val usable = size.width - thumbPx
                            if (usable <= 0f) return
                            val newValue = valueAt((x - thumbPx / 2) / usable)
                            if (newValue != currentValue) onChange(newValue)
                        }
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        moveTo(down.position.x)
                        drag(down.id) { change ->
                            change.consume()
                            moveTo(change.position.x)
                        }
                        onFinished?.invoke()
                    }
                },
        contentAlignment = Alignment.CenterStart,
    ) {
        val usableWidth = maxWidth - THUMB_SIZE

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(TRACK_HEIGHT)
                    .styleable(trackStyleState, rememberSliderTrackStyle()),
        )
        Box(
            modifier =
                Modifier
                    // Ends at the thumb's *center* (usableWidth * fraction is its left-edge
                    // offset, same as the thumb Box below), not fillMaxWidth(fraction) -- that
                    // sized this against the full track width instead of the thumb-aware
                    // usableWidth, so the fill stopped short of the thumb entirely at every
                    // fraction other than 0, leaving a visible gap of muted track color
                    // between the fill's end and the thumb (worst at fraction=1).
                    .width((usableWidth * fraction + THUMB_SIZE / 2).coerceAtLeast(0.dp))
                    .height(TRACK_HEIGHT)
                    .styleable(rangeStyleState, rememberSliderRangeStyle()),
        )
        Box(
            modifier =
                Modifier
                    .offset(x = usableWidth * fraction)
                    .size(THUMB_SIZE)
                    // Arrow keys step like Radix's slider: one step (or 1% of the range without steps), Page Up/Down
                    // ten of them, Home/End the ends.
                    .onPreviewKeyEvent { event ->
                        if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        val range = currentRange
                        val span = range.endInclusive - range.start
                        val step = if (currentSteps > 0) span / (currentSteps + 1) else span / 100f
                        val target = when (event.key) {
                            Key.DirectionRight, Key.DirectionUp -> currentValue + step
                            Key.DirectionLeft, Key.DirectionDown -> currentValue - step
                            Key.PageUp -> currentValue + step * 10
                            Key.PageDown -> currentValue - step * 10
                            Key.MoveHome -> range.start
                            Key.MoveEnd -> range.endInclusive
                            else -> return@onPreviewKeyEvent false
                        }
                        val newValue = if (span == 0f) range.start else valueAt((target - range.start) / span)
                        if (newValue != currentValue) onChange(newValue)
                        onFinished?.invoke()
                        true
                    }
                    .focusable(enabled = enabled, interactionSource = interactionSource)
                    .styleable(thumbStyleState, rememberSliderThumbStyle(), style),
        )
    }
}
