package io.github.ronjunevaldoz.shadcncompose.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Always positions its content at the window's origin, letting it measure to full size. */
private object FullWindowPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

/**
 * A full-screen scrim + centered content container for modal surfaces (Dialog,
 * AlertDialog, Sheet, Drawer) -- matches real shadcn/ui's Radix `Overlay` + `Content`
 * pair: a `bg-black/50` backdrop that dismisses on click, with the dialog content
 * itself not propagating that click (real shadcn achieves this by the overlay and
 * content being separate DOM siblings; here the content is wrapped in its own
 * non-propagating `clickable` no-op).
 *
 * Deliberately built on [Popup], not [androidx.compose.ui.window.Dialog] --
 * `Dialog`/`DialogWindow` open a real OS-level native window on desktop (a separate
 * AWT frame), which would look and behave nothing like real shadcn's always-in-page
 * overlay, and wouldn't be visually consistent with the Android/iOS/Web targets.
 *
 * Usage:
 * ```
 * ShadcnModalOverlay(visible = open, onDismissRequest = { open = false }) {
 *     ShadcnCard { ShadcnText("Dialog content") }
 * }
 * ```
 */
@Composable
fun ShadcnModalOverlay(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    contentAlignment: Alignment = Alignment.Center,
    dismissOnClickOutside: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!visible) return

    Popup(
        popupPositionProvider = FullWindowPositionProvider,
        onDismissRequest = onDismissRequest,
        properties =
            PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                // handled manually below so the scrim itself is the hit target
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        val scrimInteractionSource = remember { MutableInteractionSource() }
        val contentInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(shadcnTheme.colors.onSurface.copy(alpha = 0.5f))
                    .then(
                        if (dismissOnClickOutside) {
                            Modifier.clickable(
                                interactionSource = scrimInteractionSource,
                                indication = null,
                                onClick = onDismissRequest,
                            )
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = contentAlignment,
        ) {
            Box(
                modifier =
                    Modifier.clickable(interactionSource = contentInteractionSource, indication = null, onClick = {}),
            ) {
                content()
            }
        }
    }
}
