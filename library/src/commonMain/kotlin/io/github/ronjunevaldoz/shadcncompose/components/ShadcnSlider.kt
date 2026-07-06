package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.sliderRangeStyle
import io.github.ronjunevaldoz.shadcncompose.styles.sliderThumbStyle
import io.github.ronjunevaldoz.shadcncompose.styles.sliderTrackStyle

private val THUMB_SIZE = 16.dp
private val TRACK_HEIGHT = 6.dp

/**
 * Usage:
 * ```
 * var volume by remember { mutableStateOf(50f) }
 * ShadcnSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
 * ```
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
) {
    val rangeSpan = valueRange.endInclusive - valueRange.start
    val fraction = if (rangeSpan == 0f) 0f else ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    val currentValue = rememberUpdatedState(value)

    val interactionSource = remember { MutableInteractionSource() }
    val thumbStyleState = rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }
    val trackStyleState = remember { MutableStyleState(MutableInteractionSource()) }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(THUMB_SIZE),
        contentAlignment = Alignment.CenterStart,
    ) {
        val usableWidth = maxWidth - THUMB_SIZE

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(TRACK_HEIGHT)
                    .styleable(trackStyleState, sliderTrackStyle)
                    .pointerInput(enabled, valueRange) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { offset ->
                            val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onValueChange(valueRange.start + newFraction * rangeSpan)
                        }
                    },
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction)
                    .height(TRACK_HEIGHT)
                    .styleable(remember { MutableStyleState(MutableInteractionSource()) }, sliderRangeStyle),
        )
        Box(
            modifier =
                Modifier
                    .offset(x = usableWidth * fraction)
                    .size(THUMB_SIZE)
                    .pointerInput(enabled, rangeSpan) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val usableWidthPx = usableWidth.toPx()
                            if (usableWidthPx <= 0f) return@detectDragGestures
                            val deltaFraction = dragAmount.x / usableWidthPx
                            val newValue =
                                (currentValue.value + deltaFraction * rangeSpan)
                                    .coerceIn(valueRange.start, valueRange.endInclusive)
                            onValueChange(newValue)
                        }
                    }
                    .styleable(thumbStyleState, sliderThumbStyle, style),
        )
    }
}
