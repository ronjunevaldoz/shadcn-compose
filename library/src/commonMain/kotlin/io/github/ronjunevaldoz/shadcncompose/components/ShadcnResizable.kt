package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Two panes divided by a draggable handle, matching real shadcn/ui's `resizable.tsx`
 * (a thin wrapper over `react-resizable-panels`). Since there is no Compose Multiplatform
 * equivalent library, the split is driven directly here as a fraction (0f..1f) of the
 * container's own measured extent -- simpler than a full multi-panel-group API, but
 * covers the documented two-pane use case. [content] receives each pane's already-sized
 * `Modifier` (via `RowScope`/`ColumnScope` weight) plus the current split fraction so the
 * caller can wire [ShadcnResizableHandle]'s `onDrag` back into it.
 *
 * Usage:
 * ```
 * ShadcnResizablePanelGroup { first, second, onHandleDrag ->
 *     Box(first) { ShadcnText("One") }
 *     ShadcnResizableHandle(onDrag = onHandleDrag)
 *     Box(second) { ShadcnText("Two") }
 * }
 * ```
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
        if (containerExtentPx > 0f) {
            fraction = (fraction + delta / containerExtentPx).coerceIn(minFraction, maxFraction)
        }
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

/** The draggable divider between two panes in a [ShadcnResizablePanelGroup]. */
@Composable
fun ShadcnResizableHandle(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    showGrip: Boolean = true,
) {
    val dragState = rememberDraggableState(onDrag)
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
                .background(shadcnTheme.colors.border)
                .draggable(state = dragState, orientation = orientation),
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
