@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    // Components like ShadcnButton/ShadcnBadge set an ambient `contentColor` via their own
    // Style block (styleable() + contentColor(...)) so that a plain, colorless ShadcnText
    // slotted into them (e.g. a button label) inherits the right per-variant color -- that
    // inheritance is a Compose Foundation Style API framework behavior: a descendant
    // BasicText with no *nearer* styleable()+contentColor() of its own always takes the
    // color of the closest ancestor Style node that sets one, regardless of what its own
    // TextStyle.color says. When a caller actually asks for a specific color here (`muted`
    // or an explicit `color`), that intent must win over any such ancestor instead of being
    // silently swallowed -- so only those cases get their own styleable()+contentColor()
    // node, which (being nearer) overrides the ancestor's. The default/uncolored case stays
    // unwrapped so existing ambient-color inheritance (buttons, badges, chips, alerts, ...)
    // is untouched.
    val hasColorOverride = color != Color.Unspecified || muted
    val textColor =
        when {
            color != Color.Unspecified -> color
            muted -> theme.colors.onSurfaceVariant
            else -> theme.colors.onSurface
        }

    if (hasColorOverride) {
        val styleState = remember { MutableStyleState(interactionSource = null) }
        val contentColorStyle = remember(textColor) { Style { contentColor(textColor) } }
        BasicText(
            text = text,
            modifier = modifier.styleable(styleState, contentColorStyle),
            style = resolvedStyle.copy(color = textColor),
            maxLines = maxLines,
            overflow = overflow,
        )
    } else {
        BasicText(
            text = text,
            modifier = modifier,
            style = resolvedStyle.copy(color = textColor),
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
