package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnItemVariant { Default, Outline, Muted }

/**
 * A single row in a list of interactive/informational rows (notification, contact,
 * settings row, ...), matching real shadcn/ui's `item.tsx`.
 *
 * Usage:
 * ```
 * ShadcnItemGroup {
 *     ShadcnItem(variant = ShadcnItemVariant.Outline) {
 *         ShadcnItemMedia { ShadcnAvatar(...) }
 *         ShadcnItemContent {
 *             ShadcnItemTitle("Jane Doe")
 *             ShadcnItemDescription("jane@example.com")
 *         }
 *         ShadcnItemActions { ShadcnButton(onClick = {}) { ShadcnText("View") } }
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnItem(
    modifier: Modifier = Modifier,
    variant: ShadcnItemVariant = ShadcnItemVariant.Default,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .let {
                    when (variant) {
                        ShadcnItemVariant.Outline ->
                            it.border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                        ShadcnItemVariant.Muted -> it.background(shadcnTheme.colors.muted)
                        ShadcnItemVariant.Default -> it
                    }
                }
                .padding(shadcnTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Vertically stacks a list of [ShadcnItem]s with a hairline separator between each. */
@Composable
fun ShadcnItemGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

/** A hairline between two [ShadcnItem]s inside a [ShadcnItemGroup]. */
@Composable
fun ShadcnItemSeparator(modifier: Modifier = Modifier) {
    ShadcnSeparator(modifier = modifier)
}

enum class ShadcnItemMediaVariant { Default, Icon }

/** A leading icon/avatar/image slot in a [ShadcnItem]. */
@Composable
fun RowScope.ShadcnItemMedia(
    modifier: Modifier = Modifier,
    variant: ShadcnItemMediaVariant = ShadcnItemMediaVariant.Default,
    content: @Composable () -> Unit,
) {
    if (variant == ShadcnItemMediaVariant.Icon) {
        Box(
            modifier =
                modifier
                    .size(32.dp)
                    .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.sm)),
            contentAlignment = Alignment.Center,
            content = { content() },
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center, content = { content() })
    }
}

/** The flexible title/description column in a [ShadcnItem]. */
@Composable
fun RowScope.ShadcnItemContent(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
        content = content,
    )
}

@Composable
fun ShadcnItemTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.LabelLarge, modifier = modifier)
}

@Composable
fun ShadcnItemDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, muted = true, maxLines = 2, modifier = modifier)
}

/** Trailing actions (buttons, icons) in a [ShadcnItem]. */
@Composable
fun RowScope.ShadcnItemActions(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm), content = content)
}
