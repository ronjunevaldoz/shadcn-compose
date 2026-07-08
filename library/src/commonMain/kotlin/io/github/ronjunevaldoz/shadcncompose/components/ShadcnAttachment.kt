@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Part of shadcn's "AI Elements" family: an [ShadcnAttachment]'s lifecycle. */
enum class ShadcnAttachmentState { Idle, Uploading, Processing, Error, Done }

enum class ShadcnAttachmentSize { Default, Sm, Xs }

enum class ShadcnAttachmentOrientation { Horizontal, Vertical }

@Composable
private fun ShadcnAttachmentState.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    // Idle/Uploading/Processing/Done all share the same neutral border; only Error
    // differs (destructive border, matching real shadcn's `data-[state=error]:border-
    // destructive/30`). Real shadcn's Idle state is additionally `border-dashed`, which
    // Compose has no primitive for -- approximated as the same solid border rather than
    // attempting a literal dash pattern.
    val borderColor = if (this == ShadcnAttachmentState.Error) colors.destructive.copy(alpha = 0.3f) else colors.border

    return remember(this, colors, shapes) {
        Style {
            background(colors.background)
            borderWidth(1.dp)
            borderColor(borderColor)
            shape(RoundedCornerShape(shapes.xl))
        }
    }
}

/**
 * A file-attachment chip (e.g. for a chat composer's upload tray), part of shadcn's "AI
 * Elements" family. Matches real shadcn/ui's `attachment.tsx` -- state-driven border
 * (dashed-looking via a lighter border color for [ShadcnAttachmentState.Idle], red for
 * [ShadcnAttachmentState.Error]; Compose has no dashed-border primitive, so Idle is
 * approximated with the same solid border at lower opacity rather than a literal dash
 * pattern) and orientation (horizontal row vs. vertical card).
 *
 * [actions] is a plain `@Composable () -> Unit` slot, not scoped to `RowScope` -- real
 * shadcn's `AttachmentActions` is a CSS sibling of the content that's laid out inline for
 * [ShadcnAttachmentOrientation.Horizontal] but absolutely-positioned over the top-right
 * corner for [ShadcnAttachmentOrientation.Vertical] (a "remove" button floating on a
 * thumbnail). [ShadcnAttachment] itself builds whichever container each case needs, so
 * [actions] never needs to know which scope it's rendered in.
 *
 * Usage:
 * ```
 * ShadcnAttachmentGroup {
 *     ShadcnAttachment(
 *         state = ShadcnAttachmentState.Done,
 *         actions = { ShadcnAttachmentActions { ShadcnButton(onClick = {}) { ShadcnText("✕") } } },
 *     ) {
 *         ShadcnAttachmentMedia { ShadcnText("📄") }
 *         ShadcnAttachmentContent {
 *             ShadcnAttachmentTitle("report.pdf")
 *             ShadcnAttachmentDescription("2.4 MB")
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnAttachment(
    modifier: Modifier = Modifier,
    state: ShadcnAttachmentState = ShadcnAttachmentState.Done,
    size: ShadcnAttachmentSize = ShadcnAttachmentSize.Default,
    orientation: ShadcnAttachmentOrientation = ShadcnAttachmentOrientation.Horizontal,
    onClick: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    val padding =
        when (size) {
            ShadcnAttachmentSize.Default -> shadcnTheme.spacing.sm
            ShadcnAttachmentSize.Sm -> shadcnTheme.spacing.xs
            ShadcnAttachmentSize.Xs -> shadcnTheme.spacing.xxs
        }
    val clickModifier =
        if (onClick != null) {
            val interactionSource = remember { MutableInteractionSource() }
            Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .styleable(styleState, state.rememberStyle(), Style)
                .then(clickModifier),
    ) {
        if (orientation == ShadcnAttachmentOrientation.Horizontal) {
            Row(
                modifier = Modifier.padding(padding),
                horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
                if (actions != null) actions()
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).width(96.dp),
                verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
            if (actions != null) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(shadcnTheme.spacing.xs)) {
                    actions()
                }
            }
        }
    }
}

/** A thumbnail/icon slot in a [ShadcnAttachment]. */
@Composable
fun ShadcnAttachmentMedia(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .background(shadcnTheme.colors.muted),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

/** The title/description column in a [ShadcnAttachment]. */
@Composable
fun ShadcnAttachmentContent(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
}

/**
 * The attachment's filename. Truncates with an ellipsis past one line, matching real
 * shadcn's CSS `truncate`. Shimmers while [state] is [ShadcnAttachmentState.Uploading] or
 * [ShadcnAttachmentState.Processing], matching real shadcn's CSS `shimmer` class --
 * approximated the same way [ShadcnSkeleton] approximates `animate-pulse`, an alpha
 * oscillation rather than a literal moving-gradient sweep (Compose has no CSS
 * `background-position` animation primitive).
 */
@Composable
fun ShadcnAttachmentTitle(
    text: String,
    modifier: Modifier = Modifier,
    state: ShadcnAttachmentState = ShadcnAttachmentState.Done,
) {
    val shimmering = state == ShadcnAttachmentState.Uploading || state == ShadcnAttachmentState.Processing
    val alphaModifier =
        if (shimmering) {
            val transition = rememberInfiniteTransition(label = "attachment-shimmer")
            val alpha by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                label = "attachment-shimmer-alpha",
            )
            Modifier.alpha(alpha)
        } else {
            Modifier
        }
    ShadcnText(
        text,
        style = ShadcnTextStyle.LabelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.then(alphaModifier),
    )
}

/** The attachment's byte size/status line. Truncates with an ellipsis, matching real shadcn's CSS `truncate`. */
@Composable
fun ShadcnAttachmentDescription(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    ShadcnText(
        text,
        style = ShadcnTextStyle.BodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = if (isError) theme.colors.destructive.copy(alpha = 0.8f) else theme.colors.onSurfaceVariant,
        modifier = modifier,
    )
}

/** A group of trailing actions (e.g. a remove button) in a [ShadcnAttachment]. */
@Composable
fun ShadcnAttachmentActions(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs)) { content() }
}

/**
 * A horizontally-scrollable row of [ShadcnAttachment]s (e.g. a composer's upload tray).
 * Content-sized by default, matching real shadcn's CSS (`flex ... overflow-x-auto`, no
 * forced height) -- does not [fillMaxHeight], since doing so inside an unconstrained
 * (wrap-content) parent expands the row to consume all available vertical space.
 */
@Composable
fun ShadcnAttachmentGroup(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        content = content,
    )
}
