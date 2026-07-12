package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.icons.ChevronRight
import io.github.ronjunevaldoz.shadcncompose.icons.MoreHorizontal
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A row of navigation links showing the current page's location in a hierarchy.
 * Matches real shadcn/ui's `breadcrumb.tsx` -- a bare `nav`/`ol`/`li` structure with no
 * styling of its own beyond the muted-link/current-page/separator treatment; there is no
 * routing here, [ShadcnBreadcrumbLink]'s `onClick` is entirely caller-driven.
 *
 * Usage:
 * ```
 * ShadcnBreadcrumb {
 *     ShadcnBreadcrumbLink("Home", onClick = {})
 *     ShadcnBreadcrumbSeparator()
 *     ShadcnBreadcrumbLink("Components", onClick = {})
 *     ShadcnBreadcrumbSeparator()
 *     ShadcnBreadcrumbPage("Breadcrumb")
 * }
 * ```
 */
@Composable
fun ShadcnBreadcrumb(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        content = content,
    )
}

/** A clickable, muted crumb -- every entry except the current page. */
@Composable
fun ShadcnBreadcrumbLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    ShadcnText(
        text,
        style = ShadcnTextStyle.BodyMedium,
        muted = true,
        modifier = modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    )
}

/** The current, non-clickable page -- always the last entry in a [ShadcnBreadcrumb]. */
@Composable
fun ShadcnBreadcrumbPage(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.BodyMedium, modifier = modifier)
}

/** A "›" divider between crumbs; insert manually where wanted, matching real shadcn's chevron default. */
@Composable
fun ShadcnBreadcrumbSeparator(modifier: Modifier = Modifier) {
    ShadcnGlyphIcon(ChevronRight, tint = shadcnTheme.colors.onSurfaceVariant, modifier = modifier, small = true)
}

/** A collapsed run of hidden crumbs, e.g. `Home › ... › Settings`. Purely presentational -- expanding it is caller-driven. */
@Composable
fun ShadcnBreadcrumbEllipsis(modifier: Modifier = Modifier) {
    ShadcnGlyphIcon(MoreHorizontal, tint = shadcnTheme.colors.onSurfaceVariant, modifier = modifier, small = true)
}
