package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.switchTrackStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

private val TRACK_WIDTH = 40.dp
private val TRACK_HEIGHT = 24.dp
private val THUMB_SIZE = 18.dp
private val THUMB_INSET = 3.dp

/**
 * Usage:
 * ```
 * var enabled by remember { mutableStateOf(false) }
 * ShadcnSwitch(checked = enabled, onCheckedChange = { enabled = it })
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isChecked = checked
            it.isEnabled = enabled
        }
    val thumbOffset by animateDpAsState(if (checked) TRACK_WIDTH - THUMB_SIZE - THUMB_INSET else THUMB_INSET)
    val thumbColor = shadcnTheme.colors.background

    Box(
        modifier =
            modifier
                .size(width = TRACK_WIDTH, height = TRACK_HEIGHT)
                .toggleable(
                    value = checked,
                    onValueChange = { onCheckedChange?.invoke(it) },
                    enabled = enabled && onCheckedChange != null,
                    interactionSource = interactionSource,
                    indication = null,
                )
                .styleable(styleState, switchTrackStyle, style),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(THUMB_SIZE)
                    .background(thumbColor, CircleShape),
        )
    }
}
