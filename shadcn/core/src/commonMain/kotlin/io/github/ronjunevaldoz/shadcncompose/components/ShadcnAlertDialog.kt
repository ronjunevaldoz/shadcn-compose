package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A [ShadcnDialog] that can't be dismissed by clicking outside or a close button --
 * matches real shadcn/ui's `alert-dialog.tsx` (distinct from `Dialog` in real Radix:
 * used when the user must make an explicit choice, e.g. "Are you absolutely sure?").
 * Use [ShadcnDialogFooter] with [ShadcnButton]s for the confirm/cancel actions --
 * real shadcn's `AlertDialogAction`/`AlertDialogCancel` are themselves just styled
 * `Button` wrappers, so there's nothing extra to model here.
 *
 * Usage:
 * ```
 * ShadcnAlertDialog(visible = open, onDismissRequest = { open = false }) {
 *     ShadcnDialogHeader {
 *         ShadcnDialogTitle("Are you absolutely sure?")
 *         ShadcnDialogDescription("This action cannot be undone.")
 *     }
 *     ShadcnDialogFooter {
 *         ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) { ShadcnText("Cancel") }
 *         ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Destructive) { ShadcnText("Continue") }
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnAlertDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ShadcnDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        showCloseButton = false,
        dismissOnClickOutside = false,
        content = content,
    )
}
