package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import io.github.ronjunevaldoz.shadcncompose.icons.emoji.ShadcnCuratedEmoji
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * Like [ShadcnText], but any substring matching [ShadcnCuratedEmoji] renders as a real
 * [ImageVector] via Compose's inline-content mechanism
 * ([AnnotatedString.Builder.appendInlineContent] + [BasicText]'s `inlineContent` map) instead
 * of relying on the platform font to draw the emoji glyph -- WasmJS has no browser emoji-font
 * fallback for Skia-rendered text. Substrings *not* in the curated set are emitted as plain
 * text unchanged. This is **not** general emoji support for arbitrary text (that would need a
 * full bundled emoji font/graphic set) -- it's scoped to the small curated reaction set, for
 * spots like [ShadcnBubbleReactions].
 *
 * Deliberately a separate composable from [ShadcnText] rather than a new parameter on it:
 * [ShadcnText] is called from ~70 places and this scans/rebuilds an [AnnotatedString] on every
 * recomposition when curated emoji are present, a cost only the reaction-picker use case
 * should pay.
 *
 * Usage:
 * ```
 * ShadcnBubbleReactions {
 *     ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall)
 *     ShadcnEmojiText("🔥", style = ShadcnTextStyle.LabelSmall)
 *     ShadcnText("+2", style = ShadcnTextStyle.LabelSmall, muted = true)
 * }
 * ```
 */
@Composable
fun ShadcnEmojiText(
    text: String,
    modifier: Modifier = Modifier,
    style: ShadcnTextStyle = ShadcnTextStyle.LabelSmall,
    muted: Boolean = false,
    color: Color = Color.Unspecified,
) {
    val hasCuratedEmoji = remember(text) { ShadcnCuratedEmoji.keys.any { text.contains(it) } }
    if (!hasCuratedEmoji) {
        ShadcnText(text, modifier, style, muted, color = color)
        return
    }

    val theme = ShadcnTheme.LocalShadcnTheme.current
    val resolvedStyle = resolveShadcnTypography(theme, style)
    val textColor = resolveShadcnTextColor(theme, color, muted)

    val annotated = remember(text) { buildEmojiAnnotatedString(text) }
    val inlineContent = remember(resolvedStyle.fontSize) { emojiInlineContent(resolvedStyle.fontSize) }

    BasicText(
        text = annotated,
        modifier = modifier,
        style = resolvedStyle.copy(color = textColor),
        inlineContent = inlineContent,
    )
}

private fun buildEmojiAnnotatedString(text: String): AnnotatedString {
    val keysByLengthDescending = ShadcnCuratedEmoji.keys.sortedByDescending { it.length }
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val match = keysByLengthDescending.firstOrNull { text.startsWith(it, i) }
            if (match != null) {
                appendInlineContent(match, alternateText = match)
                i += match.length
            } else {
                append(text[i])
                i++
            }
        }
    }
}

private fun emojiInlineContent(fontSize: androidx.compose.ui.unit.TextUnit): Map<String, InlineTextContent> =
    ShadcnCuratedEmoji.mapValues { (_, vector) ->
        InlineTextContent(
            placeholder = Placeholder(fontSize, fontSize, PlaceholderVerticalAlign.TextCenter),
        ) {
            EmojiGlyph(vector)
        }
    }

@Composable
private fun EmojiGlyph(vector: ImageVector) {
    Image(imageVector = vector, contentDescription = null, modifier = Modifier.fillMaxSize())
}
