package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

enum class ShadcnTextStyle {
    DisplayLarge,
    DisplayMedium,
    TitleLarge,
    TitleMedium,
    TitleSmall,
    BodyLarge,
    BodyMedium,
    BodySmall,
    LabelLarge,
    LabelSmall,
}

/**
 * Usage:
 * ```
 * ShadcnText("Hello world")
 * ShadcnText("Title", style = ShadcnTextStyle.TitleLarge)
 * ShadcnText("Subtitle", style = ShadcnTextStyle.BodySmall, muted = true)
 * ```
 */
@Composable
fun ShadcnText(
    text: String,
    modifier: Modifier = Modifier,
    style: ShadcnTextStyle = ShadcnTextStyle.BodyMedium,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified,
) {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val resolvedStyle =
        when (style) {
            ShadcnTextStyle.DisplayLarge -> theme.typography.displayLarge
            ShadcnTextStyle.DisplayMedium -> theme.typography.displayMedium
            ShadcnTextStyle.TitleLarge -> theme.typography.titleLarge
            ShadcnTextStyle.TitleMedium -> theme.typography.titleMedium
            ShadcnTextStyle.TitleSmall -> theme.typography.titleSmall
            ShadcnTextStyle.BodyLarge -> theme.typography.bodyLarge
            ShadcnTextStyle.BodyMedium -> theme.typography.bodyMedium
            ShadcnTextStyle.BodySmall -> theme.typography.bodySmall
            ShadcnTextStyle.LabelLarge -> theme.typography.labelLarge
            ShadcnTextStyle.LabelSmall -> theme.typography.labelSmall
        }

    val textColor =
        when {
            color != Color.Unspecified -> color
            muted -> theme.colors.onSurfaceVariant
            else -> theme.colors.onSurface
        }

    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle.copy(color = textColor),
        maxLines = maxLines,
        overflow = overflow,
    )
}
