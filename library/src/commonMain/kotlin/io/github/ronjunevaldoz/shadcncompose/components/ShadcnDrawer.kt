package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnModalOverlay
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.launch

/** Which edge a [ShadcnDrawer] slides in from and can be swiped back toward to dismiss. */
enum class ShadcnDrawerDirection { Top, Bottom, Start, End }

/**
 * Decides whether a drawer drag should complete as a dismiss or spring back open, as a
 * plain function with no Compose dependency -- the actual swipe-to-dismiss feature real
 * shadcn's `vaul`-based `Drawer` provides (the one thing [ShadcnSheet]'s `Bottom` side
 * doesn't). [dragOffsetPx] is always `>= 0` here (how far dragged *toward* the closing
 * edge; dragging the other way is clamped to 0 by the caller, there is nothing to
 * decide). `contentExtentPx <= 0` (not measured yet) can never legitimately clear the
 * threshold.
 */
internal fun shouldDismissDrawer(
    dragOffsetPx: Float,
    contentExtentPx: Float,
    thresholdFraction: Float,
): Boolean {
    if (contentExtentPx <= 0f) return false
    return dragOffsetPx / contentExtentPx >= thresholdFraction
}

/**
 * A modal panel that slides in from a screen edge and can be swiped back toward that
 * edge to dismiss -- matches real shadcn/ui's `drawer.tsx` (a `vaul` wrapper). Distinct
 * from [ShadcnSheet]: same sliding-panel shape, but this is the one with actual
 * drag-to-dismiss physics; reach for [ShadcnSheet] when you don't need the gesture.
 *
 * Testing this: the drag *math* -- given how far dragged and the content's own measured
 * extent, should this count as a dismiss -- is [shouldDismissDrawer], a pure function
 * unit tested directly (`ShadcnDrawerTest`), not by simulating a live drag gesture (see
 * `resizablePanelFraction`'s doc comment for why that's avoided in this project's test
 * suite). The open/closed *rendering* is screenshot-tested normally.
 *
 * Usage:
 * ```
 * ShadcnDrawer(visible = open, onDismissRequest = { open = false }) {
 *     ShadcnDialogHeader { ShadcnDialogTitle("Edit profile") }
 * }
 * ```
 */
@Composable
fun ShadcnDrawer(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    direction: ShadcnDrawerDirection = ShadcnDrawerDirection.Bottom,
    dismissThresholdFraction: Float = 0.3f,
    content: @Composable ColumnScope.() -> Unit,
) {
    val alignment =
        when (direction) {
            ShadcnDrawerDirection.Top -> Alignment.TopCenter
            ShadcnDrawerDirection.Bottom -> Alignment.BottomCenter
            ShadcnDrawerDirection.Start -> Alignment.CenterStart
            ShadcnDrawerDirection.End -> Alignment.CenterEnd
        }
    val orientation =
        when (direction) {
            ShadcnDrawerDirection.Top, ShadcnDrawerDirection.Bottom -> Orientation.Vertical
            ShadcnDrawerDirection.Start, ShadcnDrawerDirection.End -> Orientation.Horizontal
        }
    // The sign a raw drag delta needs so that a positive result always means "moving
    // toward the closing edge" -- e.g. for a Bottom drawer, dragging the finger *down*
    // (a positive screen-space delta) is what should close it.
    val closingSign =
        when (direction) {
            ShadcnDrawerDirection.Top, ShadcnDrawerDirection.Start -> -1f
            ShadcnDrawerDirection.Bottom, ShadcnDrawerDirection.End -> 1f
        }

    ShadcnModalOverlay(visible = visible, onDismissRequest = onDismissRequest, contentAlignment = alignment) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        var contentExtentPx by remember { mutableFloatStateOf(0f) }
        val closingOffsetPx = remember { Animatable(0f) }

        LaunchedEffect(visible) {
            if (!visible) closingOffsetPx.snapTo(0f)
        }

        val dragState =
            rememberDraggableState { rawDeltaPx ->
                val next = (closingOffsetPx.value + rawDeltaPx * closingSign).coerceAtLeast(0f)
                scope.launch { closingOffsetPx.snapTo(next) }
            }

        val sizeModifier =
            when (direction) {
                ShadcnDrawerDirection.Top, ShadcnDrawerDirection.Bottom -> Modifier.fillMaxWidth()
                ShadcnDrawerDirection.Start, ShadcnDrawerDirection.End -> Modifier.fillMaxHeight().width(320.dp)
            }
        val offsetPx = closingOffsetPx.value.toInt()
        val translateModifier =
            Modifier.offset {
                when (direction) {
                    ShadcnDrawerDirection.Top -> IntOffset(0, -offsetPx)
                    ShadcnDrawerDirection.Bottom -> IntOffset(0, offsetPx)
                    ShadcnDrawerDirection.Start -> IntOffset(-offsetPx, 0)
                    ShadcnDrawerDirection.End -> IntOffset(offsetPx, 0)
                }
            }

        Column(
            modifier =
                modifier
                    .then(sizeModifier)
                    .then(translateModifier)
                    .onSizeChanged {
                        contentExtentPx =
                            with(density) {
                                if (orientation == Orientation.Vertical) it.height.toFloat() else it.width.toFloat()
                            }
                    }
                    .background(shadcnTheme.colors.background, drawerShape(direction, shadcnTheme.shapes.xl))
                    .draggable(
                        state = dragState,
                        orientation = orientation,
                        onDragStopped = {
                            if (shouldDismissDrawer(closingOffsetPx.value, contentExtentPx, dismissThresholdFraction)) {
                                onDismissRequest()
                            } else {
                                closingOffsetPx.animateTo(0f, animationSpec = tween(200))
                            }
                        },
                    )
                    .padding(shadcnTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
        ) {
            if (direction == ShadcnDrawerDirection.Bottom) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 100.dp, height = 4.dp)
                            .background(shadcnTheme.colors.muted, CircleShape),
                )
            }
            content()
        }
    }
}

/** Rounds only the edge opposite the slide-in direction, matching real shadcn's `rounded-t-lg`/`rounded-b-lg`. */
private fun drawerShape(
    direction: ShadcnDrawerDirection,
    radius: Dp,
) = RoundedCornerShape(
    topStart = if (direction == ShadcnDrawerDirection.Bottom) radius else 0.dp,
    topEnd = if (direction == ShadcnDrawerDirection.Bottom) radius else 0.dp,
    bottomStart = if (direction == ShadcnDrawerDirection.Top) radius else 0.dp,
    bottomEnd = if (direction == ShadcnDrawerDirection.Top) radius else 0.dp,
)
