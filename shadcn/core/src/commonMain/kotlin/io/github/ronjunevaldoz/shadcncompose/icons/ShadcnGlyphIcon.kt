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
 * [tint] has no default -- the Compose Foundation Style API's `contentColor` ambient
 * inheritance that [io.github.ronjunevaldoz.shadcncompose.components.ShadcnText] relies on is
 * text-component-specific (confirmed directly against its source: `StyleScope.contentColor`'s
 * own doc says it "primarily affect[s] text color... inherited by child text components"; no
 * `LocalContentColor`-equivalent exists for non-text content in this Compose version), so a
 * plain `Image` never inherits it automatically. Every call site must pass the same color its
 * neighboring text already resolves to in that context.
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
