package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Component index. Deliberately a single-pane list rather than a fixed bottom nav --
 * this is a component catalog, not a phone-app-shaped product, and the list needs to
 * scale from today's 6 entries to the full ~65-component catalog without restructuring.
 */
@Composable
fun CatalogHomeScreen(onComponentClick: (componentId: String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(shadcnTheme.spacing.lg),
    ) {
        catalogEntriesByCategory.forEach { (category, entries) ->
            item {
                ShadcnText(
                    text = category.title,
                    style = ShadcnTextStyle.LabelLarge,
                    muted = true,
                    modifier = Modifier.padding(vertical = shadcnTheme.spacing.sm),
                )
            }
            items(entries) { entry ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onComponentClick(entry.id) }
                            .padding(vertical = shadcnTheme.spacing.md),
                ) {
                    ShadcnText(text = entry.title, style = ShadcnTextStyle.BodyLarge)
                }
            }
        }
    }
}
