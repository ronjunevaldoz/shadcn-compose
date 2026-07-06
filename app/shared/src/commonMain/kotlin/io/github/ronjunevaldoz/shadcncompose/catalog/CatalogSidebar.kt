package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Persistent left-hand nav rail listing every catalog entry, grouped by category --
 * stays on screen while [io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost]
 * swaps only the content pane. This is the shadcn/ui docs-site pattern, not a
 * push/pop full-screen list -- the catalog needs to scale to ~65 components without
 * losing the reader's place in the list.
 */
@Composable
fun CatalogSidebar(
    selectedId: String?,
    onEntryClick: (componentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(shadcnTheme.colors.surfaceVariant),
        contentPadding = PaddingValues(shadcnTheme.spacing.md),
    ) {
        catalogEntriesByCategory.forEach { (category, entries) ->
            item {
                ShadcnText(
                    text = category.title,
                    style = ShadcnTextStyle.LabelSmall,
                    muted = true,
                    modifier =
                        Modifier.padding(
                            horizontal = shadcnTheme.spacing.sm,
                            vertical = shadcnTheme.spacing.sm,
                        ),
                )
            }
            items(entries) { entry ->
                val isSelected = entry.id == selectedId
                val backgroundColor =
                    if (isSelected) shadcnTheme.colors.secondary else shadcnTheme.colors.surfaceVariant
                ShadcnText(
                    text = entry.title,
                    style = ShadcnTextStyle.BodyMedium,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onEntryClick(entry.id) }
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(shadcnTheme.shapes.sm),
                            )
                            .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
                )
            }
        }
    }
}
