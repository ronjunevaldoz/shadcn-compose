package io.github.ronjunevaldoz.shadcncompose.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Renders one of this package's bundled default-icon vectors, sized from [shadcnTheme]'s
 * `ShadcnIconStyles` tokens rather than a hardcoded dp value. Internal: this is the rendering
 * primitive backing `:shadcn:core`'s own default icon slots (`ShadcnSidebar`'s hamburger,
 * `ShadcnDialog`'s close X, etc.) -- callers who want to override those slots with their own
 * icon set (e.g. this repo's catalog app using `heroicons-outline` via `DocIcon`) supply their
 * own composable instead of this one.
 *
 * [tint] has no default -- the Compose Foundation Style API's `contentColor` is write-only
 * (`StyleScope.contentColor(value: Color)` has no public reader; it's resolved internally by the
 * Style API's own text-rendering pipeline), so a plain `Image` can't inherit it the way
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnText] does. Every call site here must
 * pass the same color its neighboring text already resolves to in that context.
 *
 * Public consumers wanting ambient tinting should use
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon] instead, which reads
 * [io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnContentColor] -- a CompositionLocal
 * this library provides itself precisely because the Style API doesn't expose one. This
 * internal util predates that and stays explicit-tint-only since every one of its call sites
 * already computes the right color directly from theme tokens.
 */
@Composable
internal fun ShadcnGlyphIcon(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val size = if (small) shadcnTheme.icons.smallSize else shadcnTheme.icons.standardSize
    Image(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}
