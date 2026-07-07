package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.checkboxStyle
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Usage:
 * ```
 * var checked by remember { mutableStateOf(false) }
 * ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
 *
 * // Tri-state (e.g. "select all" driven by children):
 * ShadcnCheckbox(checked = allChecked, indeterminate = someChecked, onCheckedChange = { ... })
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    style: Style = Style,
) {
    val toggleState =
        when {
            indeterminate -> ToggleableState.Indeterminate
            checked -> ToggleableState.On
            else -> ToggleableState.Off
        }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.triStateToggle = toggleState
            it.isEnabled = enabled
        }

    Box(
        modifier =
            modifier
                .size(16.dp)
                .shadcnFocusRing(
                    focused = isFocused,
                    color = shadcnTheme.colors.borderFocus.copy(alpha = shadcnTheme.ring.opacity),
                    width = shadcnTheme.ring.width,
                    offset = shadcnTheme.ring.offset,
                    cornerRadius = shadcnTheme.shapes.sm,
                )
                .triStateToggleable(
                    state = toggleState,
                    onClick = { onCheckedChange?.invoke(toggleState != ToggleableState.On) },
                    enabled = enabled && onCheckedChange != null,
                    interactionSource = interactionSource,
                    indication = null,
                )
                .styleable(styleState, checkboxStyle, style),
        contentAlignment = Alignment.Center,
    ) {
        val markColor = shadcnTheme.colors.onPrimary
        if (toggleState != ToggleableState.Off) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val stroke = Stroke(width = size.width * 0.16f)
                if (toggleState == ToggleableState.On) {
                    val path =
                        Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.55f)
                            lineTo(size.width * 0.42f, size.height * 0.8f)
                            lineTo(size.width * 0.88f, size.height * 0.2f)
                        }
                    drawPath(path, color = markColor, style = stroke)
                } else {
                    drawLine(
                        color = markColor,
                        start = Offset(size.width * 0.2f, size.height / 2f),
                        end = Offset(size.width * 0.8f, size.height / 2f),
                        strokeWidth = stroke.width,
                        cap = stroke.cap,
                    )
                }
            }
        }
    }
}
