package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

/** A simple "Section / Page" trail shown at the top of every detail screen. */
@Composable
fun Breadcrumb(
    vararg segments: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        segments.forEachIndexed { index, segment ->
            val isLast = index == segments.lastIndex
            ShadcnText(
                text = segment,
                style = ShadcnTextStyle.LabelSmall,
                muted = !isLast,
            )
            if (!isLast) {
                ShadcnText(text = "/", style = ShadcnTextStyle.LabelSmall, muted = true)
            }
        }
    }
}
