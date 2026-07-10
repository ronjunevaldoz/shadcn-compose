@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnModalOverlay
import io.github.ronjunevaldoz.shadcncompose.styles.focusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A modal dialog. Matches real shadcn/ui's `dialog.tsx` shell (`rounded-lg border
 * bg-background p-6 shadow-lg`, `sm:max-w-lg`), built on [ShadcnModalOverlay] (a
 * `Popup` + scrim, not [androidx.compose.ui.window.Dialog] -- see that overlay's own
 * doc comment for why).
 *
 * Usage:
 * ```
 * var open by remember { mutableStateOf(false) }
 * ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
 *     ShadcnDialogHeader {
 *         ShadcnDialogTitle("Edit profile")
 *         ShadcnDialogDescription("Make changes to your profile here.")
 *     }
 *     ShadcnDialogFooter {
 *         ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    // A plain glyph placeholder -- this library has no icon-library dependency (see
    // README), so it doesn't ship a real X-mark vector. Override with any icon set (e.g.
    // this repo's own demo app passes a real heroicons-outline XMark here -- see
    // DialogDoc.kt).
    closeIcon: @Composable () -> Unit = { ShadcnText("✕", style = ShadcnTextStyle.LabelSmall, muted = true) },
    content: @Composable ColumnScope.() -> Unit,
) {
    ShadcnModalOverlay(
        visible = visible,
        onDismissRequest = onDismissRequest,
        dismissOnClickOutside = dismissOnClickOutside,
    ) {
        Box(
            modifier =
                modifier
                    .width(400.dp)
                    .background(shadcnTheme.colors.background, RoundedCornerShape(shadcnTheme.shapes.lg))
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.lg))
                    .padding(shadcnTheme.spacing.xxl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg)) {
                content()
            }
            if (showCloseButton) {
                DialogCloseButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.TopEnd),
                    icon = closeIcon,
                )
            }
        }
    }
}

@Composable
private fun DialogCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val theme = shadcnTheme
    val styleState = remember { MutableStyleState(interactionSource) }
    val closeButtonStyle =
        remember(theme) {
            Style {
                focusRing(RoundedCornerShape(theme.shapes.sm))
            }
        }
    Box(
        modifier =
            modifier
                .styleable(styleState, closeButtonStyle)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        icon()
    }
}

/** The title + description block at the top of a [ShadcnDialog]. */
@Composable
fun ShadcnDialogHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
        content()
    }
}

@Composable
fun ShadcnDialogTitle(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.TitleMedium)
}

@Composable
fun ShadcnDialogDescription(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, muted = true)
}

/** The action-button row at the bottom of a [ShadcnDialog], right-aligned. */
@Composable
fun ShadcnDialogFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm, Alignment.End),
    ) {
        content()
    }
}
