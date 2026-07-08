@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Part of shadcn's "AI Elements" family: the visual treatment of a [ShadcnBubbleContent] box. */
sealed interface ShadcnBubbleVariant {
    data object Default : ShadcnBubbleVariant

    data object Secondary : ShadcnBubbleVariant

    data object Muted : ShadcnBubbleVariant

    data object Outline : ShadcnBubbleVariant

    data object Ghost : ShadcnBubbleVariant

    data object Destructive : ShadcnBubbleVariant
}

@Composable
private fun ShadcnBubbleVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(this, colors, shapes) {
        when (this) {
            ShadcnBubbleVariant.Default ->
                Style {
                    background(colors.primary)
                    contentColor(colors.onPrimary)
                    shape(RoundedCornerShape(shapes.xl))
                }

            ShadcnBubbleVariant.Secondary ->
                Style {
                    background(colors.secondary)
                    contentColor(colors.onSecondary)
                    shape(RoundedCornerShape(shapes.xl))
                }

            ShadcnBubbleVariant.Muted ->
                Style {
                    background(colors.muted)
                    contentColor(colors.onMuted)
                    shape(RoundedCornerShape(shapes.xl))
                }

            ShadcnBubbleVariant.Outline ->
                Style {
                    background(colors.background)
                    contentColor(colors.onSurface)
                    borderWidth(1.dp)
                    borderColor(colors.border)
                    shape(RoundedCornerShape(shapes.xl))
                }

            ShadcnBubbleVariant.Ghost ->
                Style {
                    contentColor(colors.onSurface)
                }

            ShadcnBubbleVariant.Destructive ->
                Style {
                    background(colors.destructive.copy(alpha = 0.1f))
                    contentColor(colors.destructive)
                    shape(RoundedCornerShape(shapes.xl))
                }
        }
    }
}

/**
 * A single chat message bubble, part of shadcn's "AI Elements" family. Matches real
 * shadcn/ui's `bubble.tsx` (`max-w-[80%]`, self-aligned per [align]).
 *
 * A [ColumnScope] extension (not a bare `@Composable`) specifically so it can call
 * `Modifier.align(Alignment.End)` on itself for [ShadcnMessageAlign.End] -- that's a
 * `ColumnScope`-only modifier, and [ShadcnBubble] is always used inside one (directly
 * inside [ShadcnBubbleGroup], or inside [ShadcnMessage]'s content column).
 *
 * [content] receives [BoxScope], not `ColumnScope` -- real shadcn's `BubbleReactions` is
 * an absolutely-positioned *sibling* of `BubbleContent` inside `Bubble` (CSS
 * `position: relative` on the bubble, `position: absolute` on the reactions pill), not
 * nested inside the content box. [ShadcnBubbleReactions] is itself a `BoxScope`
 * extension for exactly this reason -- it needs to render layered on top of
 * [ShadcnBubbleContent], not stacked below it in normal flow.
 *
 * Usage:
 * ```
 * ShadcnBubbleGroup {
 *     ShadcnBubble(align = ShadcnMessageAlign.End) {
 *         ShadcnBubbleContent { ShadcnText("Hey, how's it going?") }
 *         ShadcnBubbleReactions { ShadcnText("👍") }
 *     }
 * }
 * ```
 */
@Composable
fun ColumnScope.ShadcnBubble(
    modifier: Modifier = Modifier,
    align: ShadcnMessageAlign = ShadcnMessageAlign.Start,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .align(if (align == ShadcnMessageAlign.End) Alignment.End else Alignment.Start)
                .fillMaxWidth(0.8f),
        contentAlignment = if (align == ShadcnMessageAlign.End) Alignment.TopEnd else Alignment.TopStart,
        content = content,
    )
}

/** Vertically stacks a transcript of [ShadcnBubble]s. */
@Composable
fun ShadcnBubbleGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm), content = content)
}

/** The actual colored bubble box inside a [ShadcnBubble]. */
@Composable
fun ShadcnBubbleContent(
    modifier: Modifier = Modifier,
    variant: ShadcnBubbleVariant = ShadcnBubbleVariant.Default,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    Box(
        modifier =
            modifier
                .styleable(styleState, variant.rememberStyle(), style)
                .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.sm),
    ) {
        content()
    }
}

/**
 * A small pill of reaction icons/counts, overlaid so it peeks out past a [ShadcnBubble]'s
 * bottom edge -- matches real shadcn's `BubbleReactions`, which sits outside the normal
 * flex flow (`translate-y-3/4`) rather than nested inside the bubble's own padding.
 */
@Composable
fun BoxScope.ShadcnBubbleReactions(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomEnd,
    content: @Composable () -> Unit,
) {
    Row(
        modifier =
            modifier
                .align(alignment)
                .offset(y = shadcnTheme.spacing.sm)
                .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.full))
                .padding(horizontal = shadcnTheme.spacing.xs, vertical = shadcnTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
    ) {
        content()
    }
}
