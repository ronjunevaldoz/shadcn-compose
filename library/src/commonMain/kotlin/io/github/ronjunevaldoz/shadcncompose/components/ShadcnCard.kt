package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

    Column(
        modifier = modifier.styleable(styleState, variant.style, style),
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(size.headerSpacing))
        }
        content()
        if (footer != null) {
            Spacer(Modifier.height(size.headerSpacing))
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
