package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnModalOverlay
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Which edge a [ShadcnSheet] slides in from. */
enum class ShadcnSheetSide { Top, Bottom, Start, End }

/**
 * A modal panel that slides in from a screen edge. Matches real shadcn/ui's
 * `sheet.tsx`: full-height + fixed-width for [ShadcnSheetSide.Start]/[ShadcnSheetSide.End]
 * (`w-3/4 sm:max-w-sm`), full-width + auto-height for [ShadcnSheetSide.Top]/
 * [ShadcnSheetSide.Bottom]. Real shadcn's mobile `Drawer` (the `vaul`-based
 * bottom-sheet-with-drag-handle) is the same shape as `side = Bottom` here minus the
 * drag gesture -- use [ShadcnSheetSide.Bottom] for that case instead of a separate
 * component.
 *
 * Slide-in only animates on the way in, not the way out -- [ShadcnModalOverlay] removes
 * the whole Popup as soon as `visible` goes false, so there's no window left to animate
 * an exit inside of. A documented simplification, not an oversight.
 *
 * Usage:
 * ```
 * ShadcnSheet(visible = open, onDismissRequest = { open = false }, side = ShadcnSheetSide.End) {
 *     ShadcnDialogTitle("Edit profile")
 * }
 * ```
 */
@Composable
fun ShadcnSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: ShadcnSheetSide = ShadcnSheetSide.End,
    content: @Composable ColumnScope.() -> Unit,
) {
    val alignment =
        when (side) {
            ShadcnSheetSide.Top -> Alignment.TopCenter
            ShadcnSheetSide.Bottom -> Alignment.BottomCenter
            ShadcnSheetSide.Start -> Alignment.CenterStart
            ShadcnSheetSide.End -> Alignment.CenterEnd
        }

    ShadcnModalOverlay(visible = visible, onDismissRequest = onDismissRequest, contentAlignment = alignment) {
        AnimatedVisibility(
            visible = visible,
            enter =
                when (side) {
                    ShadcnSheetSide.Top -> slideInVertically(initialOffsetY = { -it })
                    ShadcnSheetSide.Bottom -> slideInVertically(initialOffsetY = { it })
                    ShadcnSheetSide.Start -> slideInHorizontally(initialOffsetX = { -it })
                    ShadcnSheetSide.End -> slideInHorizontally(initialOffsetX = { it })
                },
        ) {
            val sizeModifier =
                when (side) {
                    ShadcnSheetSide.Top, ShadcnSheetSide.Bottom -> Modifier.fillMaxWidth()
                    ShadcnSheetSide.Start, ShadcnSheetSide.End -> Modifier.fillMaxHeight().width(320.dp)
                }
            Column(
                modifier =
                    modifier
                        .then(sizeModifier)
                        .background(shadcnTheme.colors.background)
                        .border(1.dp, shadcnTheme.colors.border)
                        .padding(shadcnTheme.spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
            ) {
                content()
            }
        }
    }
}
