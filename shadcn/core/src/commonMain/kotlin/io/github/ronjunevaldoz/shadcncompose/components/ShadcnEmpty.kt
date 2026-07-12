package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A centered placeholder for an empty list/search-result/state, matching real
 * shadcn/ui's `empty.tsx`.
 *
 * Usage:
 * ```
 * ShadcnEmpty {
 *     ShadcnEmptyHeader {
 *         ShadcnEmptyMedia { ShadcnEmojiText("📭") }
 *         ShadcnEmptyTitle("No results")
 *         ShadcnEmptyDescription("Try a different search term.")
 *     }
 *     ShadcnEmptyContent { ShadcnButton(onClick = {}) { ShadcnText("Clear search") } }
 * }
 * ```
 */
@Composable
fun ShadcnEmpty(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(shadcnTheme.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxl),
        content = content,
    )
}

@Composable
fun ColumnScope.ShadcnEmptyHeader(content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        content = content,
    )
}

/** A circular icon/emoji badge above an [ShadcnEmptyTitle], matching real shadcn's `EmptyMedia` icon variant. */
@Composable
fun ShadcnEmptyMedia(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.lg)),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
fun ShadcnEmptyTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.TitleMedium, modifier = modifier)
}

@Composable
fun ShadcnEmptyDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, muted = true, modifier = modifier)
}

@Composable
fun ColumnScope.ShadcnEmptyContent(content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        content = content,
    )
}
