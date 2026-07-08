package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnPointPositionProvider
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Right-click-triggered menu, opened at the cursor. Matches real shadcn/ui's
 * `context-menu.tsx` styling (shares `dropdown-menu.tsx`'s visuals in the real
 * library too) -- reuses [ShadcnDropdownMenuScope]'s row composables.
 *
 * Usage:
 * ```
 * ShadcnContextMenu(menuContent = { ShadcnDropdownMenuItem("Copy", onClick = {}) }) {
 *     ShadcnCard { ShadcnText("Right-click me") }
 * }
 * ```
 */
@Composable
fun ShadcnContextMenu(
    modifier: Modifier = Modifier,
    menuContent: @Composable ShadcnDropdownMenuScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    var clickPoint by remember { mutableStateOf<IntOffset?>(null) }

    Box(
        modifier =
            modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            val position = event.changes.first().position
                            clickPoint = IntOffset(position.x.toInt(), position.y.toInt())
                        }
                    }
                }
            },
    ) {
        content()

        val point = clickPoint
        if (point != null) {
            Popup(
                popupPositionProvider = ShadcnPointPositionProvider(point),
                onDismissRequest = { clickPoint = null },
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
            ) {
                Column(
                    modifier =
                        Modifier
                            .width(224.dp)
                            .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                            .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                            .padding(shadcnTheme.spacing.xxs),
                ) {
                    val scope = remember { ShadcnDropdownMenuScope(onDismissRequest = { clickPoint = null }) }
                    scope.menuContent()
                }
            }
        }
    }
}
