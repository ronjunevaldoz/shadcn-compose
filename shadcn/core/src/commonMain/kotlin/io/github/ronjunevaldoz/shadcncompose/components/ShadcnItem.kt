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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.focusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnItemVariant { Default, Outline, Muted }

/**
 * A single row in a list of interactive/informational rows (notification, contact,
 * settings row, ...), matching real shadcn/ui's `item.tsx`.
 *
 * Real shadcn's `Item` is only interactive when rendered `asChild` as a link/button --
 * a plain `Item` is a static row (as every current usage in this library is: an
 * informational row with its own separately-clickable `ShadcnItemActions` button, never
 * the row itself). We have no `asChild`/Slot API equivalent, so [onClick] is the closest
 * match: `null` (default) keeps this a plain, non-focusable `Row` exactly as before --
 * zero behavior change for existing call sites. Passing [onClick] opts the *whole row*
 * into being the interactive surface (matching real shadcn's `hover:bg-accent/50` and
 * `focus-visible:ring-[3px]`) for the "tap the row to view details" use case, without
 * forcing every informational-only usage to carry unused interaction machinery.
 *
 * [modifier] applies to the root row, and [variant] picks [ShadcnItemVariant.Default]/
 * [ShadcnItemVariant.Outline]/[ShadcnItemVariant.Muted] styling.
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
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnItem(
    modifier: Modifier = Modifier,
    variant: ShadcnItemVariant = ShadcnItemVariant.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val theme = shadcnTheme
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = remember { MutableStyleState(interactionSource) }
    val itemStyle =
        remember(theme) {
            Style {
                hovered { background(theme.colors.secondary.copy(alpha = 0.5f)) }
                focusRing(RoundedCornerShape(theme.shapes.md))
            }
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                // No outer `.clip()` here (unlike before this diff) -- it would clip the
                // focus ring below, which is a `dropShadow` deliberately drawn *outside*
                // the row's own bounds (see `focusRing`'s KDoc). Each variant's own
                // border()/background() call gets an explicit shape instead, so corner
                // rounding doesn't depend on an outer clip.
                .let {
                    when (variant) {
                        ShadcnItemVariant.Outline ->
                            it.border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                        ShadcnItemVariant.Muted ->
                            it.background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md))
                        ShadcnItemVariant.Default -> it
                    }
                }
                .let {
                    if (onClick != null) {
                        it
                            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                            .styleable(styleState, itemStyle)
                    } else {
                        it
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
