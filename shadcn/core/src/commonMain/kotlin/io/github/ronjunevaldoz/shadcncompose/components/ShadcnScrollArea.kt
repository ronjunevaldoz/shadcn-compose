package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnScrollAreaOrientation { Vertical, Horizontal, Both }

/**
 * A scrollable region with a slim custom scrollbar thumb, matching real shadcn/ui's
 * `scroll-area.tsx` -- which is itself a custom Radix thumb, not the browser-native
 * scrollbar, so a hand-drawn thumb here is actually the correct parity match rather
 * than a simplification. The thumb is both a scroll indicator and directly draggable,
 * matching Radix's `ScrollAreaThumb`.
 *
 * Usage:
 * ```
 * ShadcnScrollArea(modifier = Modifier.height(200.dp)) {
 *     Column { repeat(50) { ShadcnText("Row $it") } }
 * }
 * ```
 */
@Composable
fun ShadcnScrollArea(
    modifier: Modifier = Modifier,
    orientation: ShadcnScrollAreaOrientation = ShadcnScrollAreaOrientation.Vertical,
    content: @Composable BoxScope.() -> Unit,
) {
    val verticalState = rememberScrollState()
    val horizontalState = rememberScrollState()
    val scrollsVertically = orientation != ShadcnScrollAreaOrientation.Horizontal
    val scrollsHorizontally = orientation != ShadcnScrollAreaOrientation.Vertical

    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .let { if (scrollsVertically) it.verticalScroll(verticalState) else it }
                    .let { if (scrollsHorizontally) it.horizontalScroll(horizontalState) else it },
            content = content,
        )
        if (scrollsVertically) {
            ScrollThumb(
                scrollState = verticalState,
                vertical = true,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        if (scrollsHorizontally) {
            ScrollThumb(
                scrollState = horizontalState,
                vertical = false,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** The thumb's length as a fraction of the track, clamped so it never shrinks to invisible on very long content. */
internal fun scrollThumbFraction(
    viewportSize: Int,
    trackExtent: Int,
): Float = (viewportSize.toFloat() / trackExtent).coerceIn(0.05f, 1f)

/** The thumb's leading-edge position as a fraction of the track, for `Modifier.offset`. */
internal fun scrollThumbOffsetFraction(
    value: Int,
    trackExtent: Int,
): Float = value.toFloat() / trackExtent

/**
 * Converts a raw pointer-drag delta (px) on the thumb into a [ScrollState.dispatchRawDelta]
 * delta (also px, but in *content* space, not *track* space) -- the thumb only travels
 * across `travelPx` (the track minus the thumb's own length) while the content scrolls
 * across the full `maxValue`, so a 1:1 pointer-to-thumb px mapping would under-scroll by
 * exactly that ratio without this conversion.
 */
internal fun scrollDragDeltaToContentDelta(
    dragDeltaPx: Float,
    maxValue: Int,
    travelPx: Float,
): Float = if (travelPx <= 0f) 0f else dragDeltaPx * maxValue / travelPx

@Composable
private fun ScrollThumb(
    scrollState: ScrollState,
    vertical: Boolean,
    modifier: Modifier,
) {
    val maxValue = scrollState.maxValue
    val viewportSize = scrollState.viewportSize
    val trackExtent = maxValue + viewportSize
    if (maxValue <= 0 || trackExtent <= 0) return
    val thumbFraction = scrollThumbFraction(viewportSize, trackExtent)
    val offsetFraction = scrollThumbOffsetFraction(scrollState.value, trackExtent)

    val thickness = 6.dp
    val thumbColor = shadcnTheme.colors.border
    val thumbShape = RoundedCornerShape(shadcnTheme.shapes.full)
    val density = LocalDensity.current

    // fillMaxHeight/Width, not left to wrap-content: BoxWithConstraints otherwise
    // reports its own size as just its (smaller, offset-unaware) thumb child's size,
    // not the full track -- the outer Alignment.CenterEnd/BottomCenter would then
    // center that smaller box first, and the thumb's own internal offset would then
    // compound on top of *that* already-centered position instead of resolving
    // against the real, full track length. Filling the track first makes maxHeight/
    // maxWidth below actually equal the track, and the internal offset the only
    // positioning math in play.
    val trackFillModifier = if (vertical) Modifier.fillMaxHeight() else Modifier.fillMaxWidth()

    BoxWithConstraints(modifier = modifier.then(trackFillModifier)) {
        val trackLength = if (vertical) maxHeight else maxWidth
        val trackPx = with(density) { trackLength.toPx() }
        val thumbLengthPx = thumbFraction * trackPx
        val travelPx = (trackPx - thumbLengthPx).coerceAtLeast(1f)
        val dragState =
            rememberDraggableState { deltaPx ->
                scrollState.dispatchRawDelta(scrollDragDeltaToContentDelta(deltaPx, maxValue, travelPx))
            }
        val thumbModifier =
            if (vertical) {
                Modifier
                    .offset(y = trackLength * offsetFraction)
                    .height(trackLength * thumbFraction)
                    .width(thickness)
            } else {
                Modifier
                    .offset(x = trackLength * offsetFraction)
                    .width(trackLength * thumbFraction)
                    .height(thickness)
            }
        Box(
            modifier =
                thumbModifier
                    .background(thumbColor, thumbShape)
                    .draggable(
                        state = dragState,
                        orientation = if (vertical) Orientation.Vertical else Orientation.Horizontal,
                    ),
        )
    }
}
