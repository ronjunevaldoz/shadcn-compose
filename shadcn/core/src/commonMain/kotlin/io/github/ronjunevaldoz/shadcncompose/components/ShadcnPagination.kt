@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.icons.ChevronRight
import io.github.ronjunevaldoz.shadcncompose.icons.MoreHorizontal
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Represents one slot in a [ShadcnPagination] row: a page number, or an ellipsis gap. */
sealed interface ShadcnPaginationItem {
    data class Page(val number: Int) : ShadcnPaginationItem

    data object Ellipsis : ShadcnPaginationItem
}

/**
 * A page-number navigation row. Matches real shadcn/ui's `pagination.tsx`: outlined pill
 * for the active page, ghost (no background) for the rest, plus Previous/Next controls.
 *
 * Usage:
 * ```
 * var page by remember { mutableStateOf(1) }
 * ShadcnPagination(
 *     items = listOf(
 *         ShadcnPaginationItem.Page(1), ShadcnPaginationItem.Page(2),
 *         ShadcnPaginationItem.Ellipsis, ShadcnPaginationItem.Page(10),
 *     ),
 *     currentPage = page,
 *     onPageChange = { page = it },
 * )
 * ```
 */
@Composable
fun ShadcnPagination(
    items: List<ShadcnPaginationItem>,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hasPrevious: Boolean = currentPage > 1,
    hasNext: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaginationControl(
            pointingLeft = true,
            enabled = hasPrevious,
            onClick = { onPageChange(currentPage - 1) },
        )
        items.forEach { item ->
            when (item) {
                is ShadcnPaginationItem.Ellipsis -> PaginationEllipsis()
                is ShadcnPaginationItem.Page ->
                    PaginationPageButton(
                        page = item.number,
                        isActive = item.number == currentPage,
                        onClick = { onPageChange(item.number) },
                    )
            }
        }
        PaginationControl(
            pointingLeft = false,
            enabled = hasNext,
            onClick = { onPageChange(currentPage + 1) },
        )
    }
}

/**
 * Delegates to [ButtonVariant.Outline] (active page) / [ButtonVariant.Ghost] (inactive
 * pages), matching real shadcn's `buttonVariants({ variant: isActive ? "outline" :
 * "ghost", size })` -- this also gives every page button the shared button focus ring
 * for free instead of hand-rolling one.
 */
@Composable
private fun PaginationPageButton(
    page: Int,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = remember { MutableStyleState(interactionSource) }
    val style = if (isActive) ButtonVariant.Outline.rememberStyle() else ButtonVariant.Ghost.rememberStyle()
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .styleable(styleState, style)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnText(page.toString(), style = ShadcnTextStyle.LabelLarge)
    }
}

@Composable
private fun PaginationControl(
    pointingLeft: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = remember { MutableStyleState(interactionSource) }
    val style = ButtonVariant.Ghost.rememberStyle()
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .styleable(styleState, style)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnGlyphIcon(
            ChevronRight,
            tint = if (enabled) shadcnTheme.colors.onSurface else shadcnTheme.colors.onSurfaceVariant,
            modifier = if (pointingLeft) Modifier.rotate(180f) else Modifier,
        )
    }
}

@Composable
private fun PaginationEllipsis() {
    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        ShadcnGlyphIcon(MoreHorizontal, tint = shadcnTheme.colors.onSurfaceVariant)
    }
}
