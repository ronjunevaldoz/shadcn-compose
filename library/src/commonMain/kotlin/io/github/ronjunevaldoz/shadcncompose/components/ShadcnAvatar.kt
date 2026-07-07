package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnAvatarSize(val boxSize: Dp, val textStyle: ShadcnTextStyle) {
    Sm(24.dp, ShadcnTextStyle.LabelSmall),
    Default(32.dp, ShadcnTextStyle.LabelSmall),
    Lg(40.dp, ShadcnTextStyle.LabelLarge),
}

/** Lets [ShadcnAvatarFallback]/[ShadcnAvatarBadge] read their enclosing [ShadcnAvatar]'s size. */
internal val LocalAvatarSize = compositionLocalOf { ShadcnAvatarSize.Default }

/**
 * A circular user/entity image container. Matches real shadcn/ui's `avatar.tsx`
 * (`size-8` default, `size-6`/`size-10` for sm/lg, `rounded-full overflow-hidden`).
 *
 * This library has no bundled image-loading dependency, so [content] is a plain slot --
 * pass your own `Image`/`AsyncImage`/etc., or [ShadcnAvatarFallback] for initials.
 *
 * Usage:
 * ```
 * ShadcnAvatar { ShadcnAvatarFallback("CN") }
 * ShadcnAvatar(size = ShadcnAvatarSize.Lg) { Image(painter, null, Modifier.fillMaxSize()) }
 * ```
 */
@Composable
fun ShadcnAvatar(
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalAvatarSize provides size) {
        Box(
            modifier = modifier.size(size.boxSize).clip(CircleShape),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

/** Initials/icon fallback shown while an [ShadcnAvatar]'s image hasn't loaded (or has none). */
@Composable
fun ShadcnAvatarFallback(
    text: String,
    modifier: Modifier = Modifier,
) {
    val size = LocalAvatarSize.current
    Box(
        modifier = modifier.fillMaxSize().background(shadcnTheme.colors.muted),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnText(text, style = size.textStyle, muted = true)
    }
}

/** A small status dot/icon anchored to an [ShadcnAvatar]'s corner (e.g. online indicator). */
@Composable
fun ShadcnAvatarBadge(modifier: Modifier = Modifier) {
    val size = LocalAvatarSize.current
    val badgeSize =
        when (size) {
            ShadcnAvatarSize.Sm -> 8.dp
            ShadcnAvatarSize.Default -> 10.dp
            ShadcnAvatarSize.Lg -> 12.dp
        }
    Box(
        modifier =
            modifier
                .size(badgeSize)
                .background(shadcnTheme.colors.primary, CircleShape)
                .border(2.dp, shadcnTheme.colors.background, CircleShape),
    )
}

/**
 * A row of [ShadcnAvatar]s (e.g. "who's in this conversation"). Real shadcn overlaps
 * them with a negative `-space-x-2` margin plus a ring matching the page background;
 * this simplified version just spaces them evenly -- wrap each avatar in
 * `Modifier.offset(x = (-8).dp * index)` at the call site for the overlapping look.
 */
@Composable
fun ShadcnAvatarGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) { content() }
}
