package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarMenuItem
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Persistent left-hand nav rail listing every catalog entry, grouped by category --
 * stays on screen while [io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost]
 * swaps only the content pane. This is the shadcn/ui docs-site pattern, not a
 * push/pop full-screen list.
 *
 * Built from this library's own [ShadcnSidebarGroup]/[ShadcnSidebarMenu] rather than a
 * hand-rolled list -- the catalog app should dogfood its own component library for its
 * chrome, not just document it.
 */
@Composable
fun CatalogSidebar(
    selectedId: String?,
    onEntryClick: (componentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(240.dp)
                .fillMaxHeight()
                // colors.sidebar/sidebarAccent, matching ShadcnSidebar/ShadcnSidebarMenu's
                // own semantic tokens -- not surfaceVariant/secondary, which weren't
                // designed to contrast against each other the way this pair is.
                .background(shadcnTheme.colors.sidebar)
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md),
    ) {
        catalogEntriesByCategory.forEach { (category, entries) ->
            ShadcnSidebarGroup(label = category.title) {
                ShadcnSidebarMenu(
                    items = entries.map { ShadcnSidebarMenuItem(id = it.id, label = it.title) },
                    activeId = selectedId,
                    onItemClick = onEntryClick,
                )
            }
        }
    }
}
