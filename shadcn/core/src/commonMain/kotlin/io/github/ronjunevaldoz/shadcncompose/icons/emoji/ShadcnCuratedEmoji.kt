package io.github.ronjunevaldoz.shadcncompose.icons.emoji

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A small, curated set of reaction emoji rendered as real [ImageVector]s (derived from
 * Twemoji -- see the README's license section) via [io.github.ronjunevaldoz.shadcncompose
 * .components.ShadcnEmojiText], not general-purpose emoji support for arbitrary text: Compose
 * Multiplatform's WasmJS target renders text through Skia, which has no browser emoji-font
 * fallback, so raw emoji characters typed/embedded as plain text don't reliably render there.
 *
 * Two entries (heart, warning) list both the base codepoint and the "️"-qualified
 * (emoji-presentation) form as separate keys pointing at the same vector, since real keyboards
 * and emoji pickers inconsistently include the variation selector for these two -- the rest of
 * the set has no text/emoji presentation ambiguity and needs only one key.
 */
val ShadcnCuratedEmoji: Map<String, ImageVector> by lazy {
    mapOf(
        "👍" to ThumbsUp,
        "👎" to ThumbsDown,
        "❤" to Heart,
        "❤️" to Heart,
        "😀" to Grinning,
        "😂" to Joy,
        "🎉" to Tada,
        "✅" to CheckMark,
        "❌" to CrossMark,
        "⚠" to Warning,
        "⚠️" to Warning,
        "😢" to Cry,
        "😮" to Astonished,
        "🔥" to Fire,
        "💯" to Hundred,
        "👏" to Clap,
        "🙏" to PrayHands,
    )
}
