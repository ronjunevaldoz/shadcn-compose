# Step 7: Overlay components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 7: Overlay components

### `components/AppDialog.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * if (showDialog) {
 *     AppDialog(
 *         onDismiss = { showDialog = false },
 *         title = "Edit Profile",
 *         description = "Make changes to your profile here.",
 *         confirmButton = { AppButton(onClick = { save(); showDialog = false }) { AppText("Save") } },
 *         dismissButton = { AppButton(onClick = { showDialog = false }, variant = ButtonVariant.Ghost) { AppText("Cancel") } },
 *     ) {
 *         AppTextField(value = name, onValueChange = { name = it }, placeholder = "Name")
 *     }
 * }
 * ```
 */
@Composable
fun AppDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
) {
    val theme = appTheme
    val shape = RoundedCornerShape(theme.shapes.xxl)

    Dialog(onDismissRequest = onDismiss, properties = properties) {
        Column(
            modifier = modifier
                .widthIn(min = 280.dp, max = 480.dp)
                .shadow(16.dp, shape)
                .background(theme.colors.surface, shape)
                .padding(24.dp),
        ) {
            if (title != null) {
                AppText(text = title, style = AppTextStyle.TitleMedium)
            }
            if (description != null) {
                Spacer(Modifier.height(8.dp))
                AppText(text = description, style = AppTextStyle.BodyMedium, muted = true)
            }
            if (content != null) {
                Spacer(Modifier.height(16.dp))
                content()
            }
            if (confirmButton != null || dismissButton != null) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    dismissButton?.invoke()
                    confirmButton?.invoke()
                }
            }
        }
    }
}

/** Convenience destructive confirmation dialog */
@Composable
fun AppAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    description: String,
    confirmText: String = "Continue",
    dismissText: String = "Cancel",
) {
    AppDialog(
        onDismiss = onDismiss,
        title = title,
        description = description,
        confirmButton = {
            AppButton(onClick = onConfirm, variant = ButtonVariant.Destructive) {
                AppText(confirmText)
            }
        },
        dismissButton = {
            AppButton(onClick = onDismiss, variant = ButtonVariant.Ghost) {
                AppText(dismissText)
            }
        },
    )
}
```

### `components/AppSheet.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Bottom sheet modal.
 *
 * Usage:
 * ```
 * if (showSheet) {
 *     AppSheet(onDismiss = { showSheet = false }, title = "Options") {
 *         AppButton(onClick = { share() }) { AppText("Share") }
 *         AppButton(onClick = { delete() }, variant = ButtonVariant.Destructive) { AppText("Delete") }
 *     }
 * }
 * ```
 */
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val theme = appTheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            // Sheet
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        color = theme.colors.surface,
                        shape = RoundedCornerShape(topStart = theme.shapes.xxl, topEnd = theme.shapes.xxl),
                    )
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 24.dp)
                    .align(Alignment.BottomCenter),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(theme.colors.border, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(16.dp))
                if (title != null) {
                    AppText(
                        text = title,
                        style = AppTextStyle.TitleSmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    content()
                }
            }
        }
    }
}
```

### `components/AppTooltip.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToPx
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import GROUP_ID.core.designsystem.theme.appTheme
import kotlinx.coroutines.delay

/**
 * Hover tooltip — primarily for Desktop target.
 * On touch (iOS/Android), tooltip is never shown (hover not available).
 *
 * Usage:
 * ```
 * AppTooltip(tooltip = "Delete this item") {
 *     AppIconButton(onClick = { delete() }) {
 *         AppIcon(Icons.Default.Delete, contentDescription = "Delete")
 *     }
 * }
 * ```
 */
@Composable
fun AppTooltip(
    tooltip: String,
    modifier: Modifier = Modifier,
    delayMillis: Long = 500L,
    content: @Composable () -> Unit,
) {
    val theme = appTheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showTooltip by remember { mutableStateOf(false) }

    // Debounced and decoupled from raw isHovered — showing the Popup the instant
    // isHovered flips true is what causes the classic tooltip blink: popupContentSize
    // is IntSize.Zero on the Popup's first frame, so the position calculation briefly
    // places it at/near the anchor's own bounds, which un-hovers the anchor, hides the
    // Popup, re-hovers the anchor, and repeats. A short delay before committing to
    // `showTooltip = true` breaks that race entirely.
    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(delayMillis)
            showTooltip = true
        } else {
            showTooltip = false
        }
    }

    Box(modifier = modifier.hoverable(interactionSource)) {
        content()
        if (showTooltip) {
            // PopupPositionProvider centres the tooltip horizontally above the anchor.
            // Popup(alignment = ...) is wrong here — Alignment has no import and positions
            // relative to the parent bounds rather than above it.
            val density = LocalDensity.current
            val positionProvider = remember(density) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize,
                    ): IntOffset = IntOffset(
                        x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2,
                        y = anchorBounds.top - popupContentSize.height - with(density) { 4.dp.roundToPx() },
                    )
                }
            }
            Popup(
                popupPositionProvider = positionProvider,
                // focusable = false — the Popup must never steal focus/hover away from
                // the anchor; if it did, the anchor's hoverable() would immediately
                // report isHovered = false and the tooltip would disappear on its own.
                properties = PopupProperties(focusable = false),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = theme.colors.onSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(theme.shapes.sm),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    AppText(
                        text = tooltip,
                        style = AppTextStyle.BodySmall,
                        color = theme.colors.background,
                    )
                }
            }
        }
    }
}
```

### `components/AppPopover.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.styles.ButtonVariant

/**
 * Click-triggered popover with custom content.
 *
 * Usage:
 * ```
 * AppPopover(
 *     trigger = { expanded ->
 *         AppButton(onClick = { expanded.toggle() }) { AppText("Open") }
 *     }
 * ) {
 *     AppText("Popover content here")
 * }
 * ```
 */
class PopoverState {
    var isOpen by mutableStateOf(false)
        private set
    fun toggle() { isOpen = !isOpen }
    fun open()   { isOpen = true }
    fun close()  { isOpen = false }
}

@Composable
fun AppPopover(
    modifier: Modifier = Modifier,
    trigger: @Composable (state: PopoverState) -> Unit,
    content: @Composable () -> Unit,
) {
    val theme = appTheme
    val shape = RoundedCornerShape(theme.shapes.lg)
    val state = remember { PopoverState() }

    Box(modifier = modifier) {
        trigger(state)
        if (state.isOpen) {
            Popup(onDismissRequest = { state.close() }) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, shape)
                        .background(theme.colors.surface, shape)
                        .border(1.dp, theme.colors.border, shape)
                        .padding(16.dp),
                ) {
                    content()
                }
            }
        }
    }
}
```

---

