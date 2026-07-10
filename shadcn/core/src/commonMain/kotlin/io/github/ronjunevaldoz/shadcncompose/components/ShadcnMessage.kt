package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Which side of a [ShadcnMessage] row its avatar sits on -- the sender's own messages are usually [End]. */
enum class ShadcnMessageAlign { Start, End }

/**
 * The enclosing [ShadcnMessage]'s [ShadcnMessageAlign], read by [ShadcnMessageHeader] and
 * [ShadcnMessageFooter] so they self-align to match -- matches real shadcn's CSS
 * `group-data-[align=end]/message:justify-end` (a parent-state selector with no direct
 * Compose equivalent, so this is threaded down via [CompositionLocalProvider] instead).
 * Defaults to [ShadcnMessageAlign.Start] so Header/Footer degrade gracefully if ever used
 * outside a [ShadcnMessage].
 */
internal val LocalMessageAlign = compositionLocalOf { ShadcnMessageAlign.Start }

/**
 * One chat-transcript message row: avatar + content side by side, part of shadcn's "AI
 * Elements" family. Matches real shadcn/ui's `message.tsx` -- [ShadcnMessageAlign.End]
 * puts the avatar on the right, matching the classic "my messages on the right" chat
 * layout.
 *
 * Takes [avatar]/[content] as two separate slots (rather than one freeform `RowScope`
 * lambda the caller populates in a fixed order) specifically so [ShadcnMessage] itself
 * can deterministically flip their visual order for [ShadcnMessageAlign.End] -- Compose
 * has no `flex-direction: row-reverse` equivalent that reorders an opaque composable
 * lambda's already-emitted children after the fact.
 *
 * Usage:
 * ```
 * ShadcnMessageGroup {
 *     ShadcnMessage(
 *         align = ShadcnMessageAlign.Start,
 *         avatar = { ShadcnMessageAvatar { ShadcnAvatarFallback("AI") } },
 *     ) {
 *         ShadcnBubble { ShadcnBubbleContent { ShadcnText("Hello!") } }
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnMessage(
    modifier: Modifier = Modifier,
    align: ShadcnMessageAlign = ShadcnMessageAlign.Start,
    avatar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        // Real shadcn's `.message` is `w-full` -- without this, the row sizes to its
        // own content (avatar + however wide the bubble happens to be) and the parent
        // Column's default Alignment.Start then places that whole undersized row flush
        // left, regardless of [align]. ShadcnBubble's own end-alignment only resolves
        // *within* this row, so a narrower-than-full row left an End-aligned bubble
        // looking stranded short of the real right edge, with dead space after it.
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        CompositionLocalProvider(LocalMessageAlign provides align) {
            if (align == ShadcnMessageAlign.Start) {
                Box(modifier = Modifier.align(Alignment.Bottom)) { avatar() }
                ShadcnMessageContent(content = content)
            } else {
                ShadcnMessageContent(content = content)
                Box(modifier = Modifier.align(Alignment.Bottom)) { avatar() }
            }
        }
    }
}

/** A circular avatar slot for [ShadcnMessage]'s `avatar` parameter. */
@Composable
fun ShadcnMessageAvatar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.size(32.dp).background(shadcnTheme.colors.muted, CircleShape),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
private fun RowScope.ShadcnMessageContent(content: @Composable ColumnScope.() -> Unit) {
    // fill = true (the default): real shadcn's MessageContent is `w-full`, occupying
    // its full flex-1 share -- with fill = false a shrunk column (e.g. a short bubble)
    // would be positioned right after the previous sibling instead of at its slot's
    // true edge, leaving the avatar stranded short of the row's real end for
    // ShadcnMessageAlign.End, even with the outer Row now correctly filling its width.
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        content = content,
    )
}

/** Vertically stacks a transcript of [ShadcnMessage]s. */
@Composable
fun ShadcnMessageGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm), content = content)
}

/** A metadata row (e.g. sender name) above a [ShadcnMessage]'s content, self-aligned to match [LocalMessageAlign]. */
@Composable
fun ColumnScope.ShadcnMessageHeader(content: @Composable RowScope.() -> Unit) {
    val align = LocalMessageAlign.current
    Row(
        modifier = Modifier.align(if (align == ShadcnMessageAlign.End) Alignment.End else Alignment.Start),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        content = content,
    )
}

/** A metadata row (e.g. timestamp) below a [ShadcnMessage]'s content, self-aligned to match [LocalMessageAlign]. */
@Composable
fun ColumnScope.ShadcnMessageFooter(content: @Composable RowScope.() -> Unit) {
    val align = LocalMessageAlign.current
    Row(
        modifier = Modifier.align(if (align == ShadcnMessageAlign.End) Alignment.End else Alignment.Start),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        content = content,
    )
}
