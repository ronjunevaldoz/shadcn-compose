# Step 5b: Form controls (continued) — Slider, Select

Part of `kmp-compose-design-system-extended`. Continuation of `references/step5-form-controls.md`,
split out to stay under the 500-line reference guideline. Load this file when implementing
`AppSlider`/`AppSelect`.

---

## Step 5: Form controls

### `components/AppSlider.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import kotlin.math.roundToInt

/**
 * Usage:
 * ```
 * AppSlider(value = volume, onValueChange = { volume = it }, range = 0f..1f)
 * ```
 */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    trackColor: Color = appTheme.colors.secondary,
    progressColor: Color = appTheme.colors.primary,
    thumbColor: Color = appTheme.colors.background,
) {
    val theme = appTheme
    var trackWidth by remember { mutableStateOf(0) }
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val thumbDp = 20.dp

    Box(
        modifier = modifier
            .height(thumbDp)
            .padding(horizontal = thumbDp / 2),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { trackWidth = it.width }
                .height(4.dp)
                .background(trackColor, RoundedCornerShape(2.dp))
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / trackWidth).coerceIn(0f, 1f)
                        onValueChange(range.start + newFraction * (range.endInclusive - range.start))
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(if (enabled) progressColor else theme.colors.primaryDisabled, RoundedCornerShape(2.dp)),
            )
        }

        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(((fraction * trackWidth) - thumbDp.toPx() / 2).roundToInt(), 0) }
                .size(thumbDp)
                .background(
                    color = if (enabled) theme.colors.background else theme.colors.primaryDisabled,
                    shape = CircleShape,
                )
                .then(
                    if (enabled) Modifier
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                val delta = dragAmount / trackWidth
                                val newFraction = (fraction + delta).coerceIn(0f, 1f)
                                onValueChange(range.start + newFraction * (range.endInclusive - range.start))
                            }
                        }
                    else Modifier
                ),
        ) {
            // Inner dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.Center)
                    .background(if (enabled) progressColor else theme.colors.primaryDisabled, CircleShape),
            )
        }
    }
}
```

### `components/AppSelect.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * val options = listOf("Option A", "Option B", "Option C")
 * AppSelect(
 *     options = options,
 *     selected = currentOption,
 *     onSelect = { currentOption = it },
 *     placeholder = "Select an option",
 * )
 * ```
 */
@Composable
fun AppSelect(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select…",
    enabled: Boolean = true,
) {
    val theme = appTheme
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(theme.shapes.md)

    Box(modifier = modifier.zIndex(if (expanded) 1f else 0f)) {
        // Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(theme.colors.background)
                .border(
                    width = if (expanded) 2.dp else 1.dp,
                    color = if (expanded) theme.colors.borderFocus else theme.colors.border,
                    shape = shape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    role = Role.DropdownList,
                    onClick = { expanded = !expanded },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = selected ?: placeholder,
                style = AppTextStyle.BodyMedium,
                color = if (selected != null) theme.colors.onSurface else theme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            // Chevron
            AppText(text = if (expanded) "▲" else "▼", style = AppTextStyle.LabelSmall, muted = true)
        }

        // Dropdown
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(100)) + expandVertically(tween(100)),
            exit = fadeOut(tween(80)) + shrinkVertically(tween(80)),
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, shape)
                    .background(theme.colors.background, shape)
                    .border(1.dp, theme.colors.border, shape)
                    .padding(vertical = 4.dp),
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onSelect(option)
                                    expanded = false
                                },
                            )
                            .background(
                                if (option == selected) theme.colors.secondary else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(
                            text = option,
                            style = AppTextStyle.BodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (option == selected) {
                            AppText(text = "✓", style = AppTextStyle.LabelSmall, color = theme.colors.primary)
                        }
                    }
                }
            }
        }
    }
}
```

---

