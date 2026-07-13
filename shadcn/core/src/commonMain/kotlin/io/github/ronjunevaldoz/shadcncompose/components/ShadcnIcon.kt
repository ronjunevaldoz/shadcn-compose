package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnContentColor
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Renders an [ImageVector] icon from any icon set (heroicons, custom vectors, ...), tinted to
 * match its surroundings the same way [ShadcnText] does.
 *
 * [tint] resolution, in priority order: an explicit [tint] always wins; otherwise this reads
 * [LocalShadcnContentColor] (provided by [ShadcnButton] and other content-color-setting
 * components); if neither applies, falls back to `colors.onSurface` -- the same default
 * [ShadcnText] uses.
 *
 * Usage:
 * ```
 * ShadcnButton(onClick = {}) {
 *     ShadcnIcon(Heroicons.Outline.Bolt)
 *     ShadcnText("Generate")
 * }
 * ```
 */
@Composable
fun ShadcnIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    small: Boolean = false,
    contentDescription: String? = null,
) {
    val theme = shadcnTheme
    val resolvedTint =
        when {
            tint != Color.Unspecified -> tint
            LocalShadcnContentColor.current != Color.Unspecified -> LocalShadcnContentColor.current
            else -> theme.colors.onSurface
        }
    val size = if (small) theme.icons.smallSize else theme.icons.standardSize
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(resolvedTint),
    )
}
