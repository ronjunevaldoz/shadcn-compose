@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.focusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * The entire drag-to-resize calculation, as a plain function with no Compose dependency:
 * given [currentFraction] and [dragDeltaPx] (how far the handle just moved, in px, relative
 * to [containerExtentPx]), returns the new split, clamped to [minFraction]/[maxFraction].
 * `containerExtentPx <= 0` (not measured yet) is a no-op, returning [currentFraction]
 * unchanged rather than dividing by zero.
 */
internal fun resizablePanelFraction(
    currentFraction: Float,
    dragDeltaPx: Float,
    containerExtentPx: Float,
    minFraction: Float,
    maxFraction: Float,
): Float {
    if (containerExtentPx <= 0f) return currentFraction
    return (currentFraction + dragDeltaPx / containerExtentPx).coerceIn(minFraction, maxFraction)
}

/** One keyboard nudge's worth of drag delta, in px -- matches real react-resizable-panels' step. */
private const val KEYBOARD_RESIZE_STEP_PX = 24f

/**
 * Large enough that `resizablePanelFraction`'s own `coerceIn(minFraction, maxFraction)` saturates
 * to the boundary regardless of the container's real measured extent -- reuses that existing
 * clamp instead of a separate "jump straight to min/max" code path for Home/End.
 */
private const val KEYBOARD_RESIZE_JUMP_PX = 1_000_000f

/**
 * Pure key-to-drag-delta mapping for [ShadcnResizableHandle]'s keyboard support, extracted for
 * the same reason [resizablePanelFraction] is: unit-testable without a Compose UI test. Returns
 * `null` for any key the handle doesn't claim, so the caller lets it fall through unconsumed.
 */
internal fun resizableHandleKeyboardDelta(
    key: Key,
    orientation: Orientation,
): Float? {
    val forwardKey = if (orientation == Orientation.Horizontal) Key.DirectionRight else Key.DirectionDown
    val backwardKey = if (orientation == Orientation.Horizontal) Key.DirectionLeft else Key.DirectionUp
    return when (key) {
        forwardKey -> KEYBOARD_RESIZE_STEP_PX
        backwardKey -> -KEYBOARD_RESIZE_STEP_PX
        Key.MoveEnd -> KEYBOARD_RESIZE_JUMP_PX
        Key.MoveHome -> -KEYBOARD_RESIZE_JUMP_PX
        else -> null
    }
}

/**
 * Two panes divided by a draggable handle, matching real shadcn/ui's `resizable.tsx`
 * (a thin wrapper over `react-resizable-panels`). Since there is no Compose Multiplatform
 * equivalent library, the split is driven directly here as a fraction (0f..1f) of the
 * container's own measured extent -- simpler than a full multi-panel-group API, but
 * covers the documented two-pane use case. [content] receives each pane's already-sized
 * `Modifier` (via `RowScope`/`ColumnScope` weight) plus the current split fraction so the
 * caller can wire [ShadcnResizableHandle]'s `onDrag` back into it. [modifier] applies to
 * the root container, [orientation] picks a horizontal or vertical split, and
 * [initialFraction]/[minFraction]/[maxFraction] seed and clamp the draggable split.
 *
 * Usage:
 * ```
 * ShadcnResizablePanelGroup { first, second, onHandleDrag ->
 *     Box(first) { ShadcnText("One") }
 *     ShadcnResizableHandle(onDrag = onHandleDrag)
 *     Box(second) { ShadcnText("Two") }
 * }
 * ```
 *
 * Testing this: don't simulate a live drag gesture in Compose UI Test --
 * `performMouseInput`/`performTouchInput` drag simulations against a raw
 * `Modifier.draggable` have previously hung this project's JVM test worker outright
 * (see git history). Instead this is two separately-testable halves:
 * 1. [resizablePanelFraction] is the entire drag *math*, extracted as a plain pure
 *    function with no Compose dependency at all -- unit test it directly
 *    (`ShadcnResizablePanelGroupTest`) the same way [scrollDragDeltaToContentDelta]
 *    is tested for [ShadcnScrollArea].
 * 2. The *rendering* at a given split -- does `weight(fraction)` actually produce the
 *    right pixel proportions -- is covered by screenshot-testing fixed `initialFraction`
 *    values (e.g. 0.3, 0.5, 0.7) rather than dragging to reach them.
 */
@Composable
fun ShadcnResizablePanelGroup(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    initialFraction: Float = 0.5f,
    minFraction: Float = 0.15f,
    maxFraction: Float = 0.85f,
    content: @Composable (first: Modifier, second: Modifier, onHandleDrag: (Float) -> Unit) -> Unit,
) {
    var fraction by remember { mutableFloatStateOf(initialFraction) }
    var containerExtentPx by remember { mutableFloatStateOf(0f) }

    val onHandleDrag: (Float) -> Unit = { delta ->
        fraction = resizablePanelFraction(fraction, delta, containerExtentPx, minFraction, maxFraction)
    }

    if (orientation == Orientation.Horizontal) {
        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .onSizeChanged { containerExtentPx = it.width.toFloat() },
        ) {
            content(
                Modifier.fillMaxHeight().weight(fraction),
                Modifier.fillMaxHeight().weight(1f - fraction),
                onHandleDrag,
            )
        }
    } else {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .onSizeChanged { containerExtentPx = it.height.toFloat() },
        ) {
            content(
                Modifier.fillMaxWidth().weight(fraction),
                Modifier.fillMaxWidth().weight(1f - fraction),
                onHandleDrag,
            )
        }
    }
}

/**
 * The draggable divider between two panes in a [ShadcnResizablePanelGroup]. Also keyboard
 * resizable when focused -- real react-resizable-panels' handle is a focusable, arrow-key
 * resizable control, not drag-only; matches that here via [resizableHandleKeyboardDelta]
 * feeding the same [onDrag] callback a drag gesture would, plus a focus ring like every other
 * focusable component in this library (`Modifier.focusRing`), which this handle never had before.
 */
@Composable
fun ShadcnResizableHandle(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    showGrip: Boolean = true,
) {
    val dragState = rememberDraggableState(onDrag)
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = remember { MutableStyleState(interactionSource) }
    val theme = shadcnTheme
    val handleStyle = remember(theme) { Style { focusRing(RoundedCornerShape(theme.shapes.xs)) } }
    val sizeModifier =
        if (orientation == Orientation.Horizontal) {
            Modifier.fillMaxHeight().width(1.dp)
        } else {
            Modifier.fillMaxWidth().height(1.dp)
        }
    Box(
        modifier =
            modifier
                .then(sizeModifier)
                .styleable(styleState, handleStyle)
                .background(shadcnTheme.colors.border)
                .draggable(state = dragState, orientation = orientation)
                .focusable(interactionSource = interactionSource)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    val delta = resizableHandleKeyboardDelta(event.key, orientation) ?: return@onPreviewKeyEvent false
                    onDrag(delta)
                    true
                },
        contentAlignment = Alignment.Center,
    ) {
        if (showGrip) {
            val gripSize =
                if (orientation == Orientation.Horizontal) 12.dp to 16.dp else 16.dp to 12.dp
            Box(
                modifier =
                    Modifier
                        .size(gripSize.first, gripSize.second)
                        .background(shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.xs)),
            )
        }
    }
}
