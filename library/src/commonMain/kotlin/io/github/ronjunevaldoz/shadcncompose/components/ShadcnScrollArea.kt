package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnScrollAreaOrientation { Vertical, Horizontal, Both }

/**
 * A scrollable region with a slim custom scrollbar thumb, matching real shadcn/ui's
 * `scroll-area.tsx` -- which is itself a custom Radix thumb, not the browser-native
 * scrollbar, so a hand-drawn thumb here is actually the correct parity match rather
 * than a simplification. The thumb is purely visual (drag-to-scroll isn't wired) --
 * scrolling happens via the normal scroll/fling gesture on [content] itself.
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
                value = verticalState.value,
                maxValue = verticalState.maxValue,
                viewportSize = verticalState.viewportSize,
                vertical = true,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        if (scrollsHorizontally) {
            ScrollThumb(
                value = horizontalState.value,
                maxValue = horizontalState.maxValue,
                viewportSize = horizontalState.viewportSize,
                vertical = false,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ScrollThumb(
    value: Int,
    maxValue: Int,
    viewportSize: Int,
    vertical: Boolean,
    modifier: Modifier,
) {
    val trackExtent = maxValue + viewportSize
    if (maxValue <= 0 || trackExtent <= 0) return
    val thumbFraction = (viewportSize.toFloat() / trackExtent).coerceIn(0.05f, 1f)
    val offsetFraction = value.toFloat() / trackExtent

    val thickness = 6.dp
    val thumbColor = shadcnTheme.colors.border
    val thumbShape = RoundedCornerShape(shadcnTheme.shapes.full)
    BoxWithConstraints(modifier = modifier) {
        if (vertical) {
            val trackHeight = maxHeight
            Box(
                modifier =
                    Modifier
                        .offset(y = trackHeight * offsetFraction)
                        .height(trackHeight * thumbFraction)
                        .width(thickness)
                        .background(thumbColor, thumbShape),
            )
        } else {
            val trackWidth = maxWidth
            Box(
                modifier =
                    Modifier
                        .offset(x = trackWidth * offsetFraction)
                        .width(trackWidth * thumbFraction)
                        .height(thickness)
                        .background(thumbColor, thumbShape),
            )
        }
    }
}
