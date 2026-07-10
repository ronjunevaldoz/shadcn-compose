package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A real `heroicons-outline` glyph -- catalog *examples* only, never added to `:shadcn:core`
 * (that library takes no icon-set dependency; see its README). Demonstrates the icon slots
 * several components expose (`ShadcnSelect`'s `icon`, `ShadcnDialog`'s `closeIcon`, etc.) with
 * a real vector instead of the library's own plain-glyph placeholder default.
 *
 * [tint] defaults to `onSurface` (correct for Ghost/Outline buttons, which render on a
 * light/transparent background) but MUST be passed explicitly as the surrounding button's own
 * content color for anything with a colored background (e.g. `ButtonVariant.Default`'s
 * `onPrimary`) -- otherwise the icon renders in the wrong color for its background and can end
 * up nearly invisible.
 */
@Composable
fun DocIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = shadcnTheme.colors.onSurface,
) {
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(16.dp),
        colorFilter = ColorFilter.tint(tint),
    )
}
