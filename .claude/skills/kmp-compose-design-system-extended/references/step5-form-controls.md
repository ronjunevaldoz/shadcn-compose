# Step 5: Form controls

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 5: Form controls

### `components/AppCheckbox.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
 * AppCheckbox(checked = isChecked, onCheckedChange = { isChecked = it }, label = "Remember me")
 * ```
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val theme = appTheme
    val interactionSource = remember { MutableInteractionSource() }
    val checkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(150),
        label = "checkAlpha",
    )

    val rowModifier = if (label != null) {
        modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onClick = { onCheckedChange(!checked) },
        )
    } else modifier

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled && label == null,
                    role = Role.Checkbox,
                    onClick = { onCheckedChange(!checked) },
                )
        ) {
            val cornerRadius = 3.dp.toPx()
            if (checked) {
                drawRoundRect(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                )
                // Draw checkmark
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.5f)
                    lineTo(size.width * 0.42f, size.height * 0.72f)
                    lineTo(size.width * 0.78f, size.height * 0.28f)
                }
                drawPath(
                    path = path,
                    color = theme.colors.onPrimary.copy(alpha = checkAlpha),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            } else {
                drawRoundRect(
                    color = Color.Transparent,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                )
                drawRoundRect(
                    color = if (enabled) theme.colors.border else theme.colors.primaryDisabled,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

### `components/AppRadioButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * Column {
 *     AppRadioButton(selected = selected == "a", onClick = { selected = "a" }, label = "Option A")
 *     AppRadioButton(selected = selected == "b", onClick = { selected = "b" }, label = "Option B")
 * }
 * ```
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val theme = appTheme
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "radioDot",
    )

    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val r = size.minDimension / 2
            drawCircle(
                color = if (enabled) theme.colors.border else theme.colors.primaryDisabled,
                radius = r,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
            )
            if (dotScale > 0f) {
                drawCircle(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    radius = r * 0.5f * dotScale,
                )
                drawCircle(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    radius = r,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

### `components/AppSwitch.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.SwitchDefaults
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppSwitch(checked = enabled, onCheckedChange = { enabled = it })
 * AppSwitch(checked = enabled, onCheckedChange = { enabled = it }, label = "Dark mode")
 * ```
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val colors = SwitchDefaults.colors()
    val trackColor = if (checked) colors.trackChecked else colors.trackUnchecked
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "thumbOffset",
    )

    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.Switch,
            onClick = { onCheckedChange(!checked) },
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .clip(CircleShape)
                .background(if (enabled) trackColor else appTheme.colors.primaryDisabled),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = thumbOffset, y = 2.dp)
                    .clip(CircleShape)
                    .background(colors.thumb),
            )
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

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

