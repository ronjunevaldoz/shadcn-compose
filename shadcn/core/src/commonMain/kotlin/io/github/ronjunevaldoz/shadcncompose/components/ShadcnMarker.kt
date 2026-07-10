package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Part of shadcn's "AI Elements" family, for labeling a section of a chat transcript (e.g. a date marker). */
enum class ShadcnMarkerVariant { Default, Separator, Border }

/**
 * A labeled divider/section marker. Matches real shadcn/ui's `marker.tsx`: plain by
 * default, flanked by two hairlines for [ShadcnMarkerVariant.Separator] (e.g. "Today"
 * between two lines), or underlined for [ShadcnMarkerVariant.Border].
 *
 * Usage:
 * ```
 * ShadcnMarker(variant = ShadcnMarkerVariant.Separator) {
 *     ShadcnMarkerContent { ShadcnText("Today") }
 * }
 * ```
 */
@Composable
fun ShadcnMarker(
    modifier: Modifier = Modifier,
    variant: ShadcnMarkerVariant = ShadcnMarkerVariant.Default,
    content: @Composable RowScope.() -> Unit,
) {
    val borderColor = shadcnTheme.colors.border
    val bottomBorderModifier =
        if (variant == ShadcnMarkerVariant.Border) {
            Modifier
                .padding(bottom = shadcnTheme.spacing.sm)
                .drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
        } else {
            Modifier
        }

    Row(
        modifier = modifier.then(bottomBorderModifier),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (variant == ShadcnMarkerVariant.Separator) MarkerLine()
        content()
        if (variant == ShadcnMarkerVariant.Separator) MarkerLine()
    }
}

@Composable
private fun RowScope.MarkerLine() {
    Box(modifier = Modifier.weight(1f).height(1.dp).background(shadcnTheme.colors.border))
}

/** An optional leading icon slot in a [ShadcnMarker]. */
@Composable
fun ShadcnMarkerIcon(content: @Composable () -> Unit) {
    Box(modifier = Modifier.width(16.dp)) { content() }
}

/** The marker's label text/content. */
@Composable
fun ShadcnMarkerContent(content: @Composable () -> Unit) {
    content()
}
