package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Which side of a [ShadcnMessage] row its avatar sits on -- the sender's own messages are usually [End]. */
enum class ShadcnMessageAlign { Start, End }

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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        if (align == ShadcnMessageAlign.Start) {
            Box(modifier = Modifier.align(Alignment.Bottom)) { avatar() }
            ShadcnMessageContent(content = content)
        } else {
            ShadcnMessageContent(content = content)
            Box(modifier = Modifier.align(Alignment.Bottom)) { avatar() }
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
    Column(
        modifier = Modifier.weight(1f, fill = false),
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

/** A metadata row (e.g. sender name) above a [ShadcnMessage]'s content. */
@Composable
fun ShadcnMessageHeader(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs), content = content)
}

/** A metadata row (e.g. timestamp) below a [ShadcnMessage]'s content. */
@Composable
fun ShadcnMessageFooter(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs), content = content)
}
