package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.CardSize
import io.github.ronjunevaldoz.shadcncompose.styles.CardVariant
import io.github.ronjunevaldoz.shadcncompose.styles.headerSpacing
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.tailwind.modifiers.shadowMd

/**
 * Card with header/content/footer slots.
 *
 * Usage:
 * ```
 * ShadcnCard(
 *     header = { ShadcnCardHeader(title = "Title", description = "Subtitle") },
 *     footer = { ShadcnButton(onClick = {}) { ShadcnText("Action") } },
 * ) {
 *     ShadcnText("Card body content")
 * }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    size: CardSize = CardSize.Default,
    style: Style = Style,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    val headerSpacing = size.headerSpacing()

    // The Compose Styles API's StyleScope has no shadow/elevation property (confirmed
    // against its real surface -- see .claude/AGENTS.md), so CardVariant.Elevated's own
    // Style block can only differ from Default by *removing* the border, not by adding
    // real elevation -- it wasn't actually elevated. tailwind-compose's Modifier.shadowMd()
    // fills exactly that gap: applied *before* .styleable() (matching tailwind-compose's
    // own documented shadow-then-clip-then-background ordering rule), it draws behind
    // Style's own background/border, not instead of them -- unlike tailwind-compose's
    // twCard() combinator, which would double-paint the background this Style block
    // already handles. Same shapes.xxl passed to both so the shadow and the card's own
    // rounded corners stay concentric (a mismatched shape here casts a rectangular halo
    // past the rounded content -- the exact bug class this project has already hit twice
    // with mismatched shapes on separate draw calls).
    val elevationModifier =
        if (variant == CardVariant.Elevated) {
            Modifier.shadowMd(shape = RoundedCornerShape(shadcnTheme.shapes.xxl))
        } else {
            Modifier
        }

    Column(
        modifier = modifier.then(elevationModifier).styleable(styleState, variant.rememberStyle(), style),
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(headerSpacing))
        }
        content()
        if (footer != null) {
            Spacer(Modifier.height(headerSpacing))
            footer()
        }
    }
}

@Composable
fun ShadcnCardHeader(
    title: String,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            ShadcnText(text = title, style = ShadcnTextStyle.TitleSmall)
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                ShadcnText(text = description, style = ShadcnTextStyle.BodySmall, muted = true)
            }
        }
        if (action != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                action()
            }
        }
    }
}
