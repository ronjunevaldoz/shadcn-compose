@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One selectable row in a [ShadcnCommand] list. */
data class ShadcnCommandItem(
    val id: String,
    val label: String,
    val onSelect: () -> Unit,
)

/**
 * A labeled section of [ShadcnCommandItem]s, matching real shadcn/ui's
 * `CommandGroup`/`CommandGroup[heading]` (e.g. a "Suggestions" heading above a set
 * of items, separated from the next group). A group with no items left after
 * filtering is dropped entirely, matching real cmdk's behavior.
 */
data class ShadcnCommandGroup(
    val items: List<ShadcnCommandItem>,
    val heading: String? = null,
)

/**
 * A searchable/filterable action list -- real shadcn/ui's `command.tsx` (built on
 * `cmdk`), the shared building block behind both a standalone command palette and
 * (composed with a trigger + popover) a [ShadcnCombobox]. Filters each group's items
 * by case-insensitive substring match against `label` as the user types, dropping
 * groups left with no matches.
 *
 * Groups use a plain data model rather than a slot API (unlike [ShadcnDropdownMenu])
 * because filtering needs to inspect every item's label up front -- a freely-composed
 * slot API would need extra machinery to hide non-matching children mid-composition.
 *
 * Usage:
 * ```
 * ShadcnCommand(
 *     groups = listOf(
 *         ShadcnCommandGroup(
 *             heading = "Suggestions",
 *             items = listOf(ShadcnCommandItem("calendar", "Calendar", onSelect = {})),
 *         ),
 *     ),
 * )
 * ```
 */
@Composable
fun ShadcnCommand(
    groups: List<ShadcnCommandGroup>,
    modifier: Modifier = Modifier,
    placeholder: String = "Type a command or search...",
    emptyText: String = "No results found.",
) {
    var query by remember { mutableStateOf("") }
    val filteredGroups =
        remember(groups, query) {
            groups
                .map { group -> group.copy(items = group.items.filter { it.label.contains(query, ignoreCase = true) }) }
                .filter { it.items.isNotEmpty() }
        }

    Column(
        modifier =
            modifier
                .width(280.dp)
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .padding(shadcnTheme.spacing.sm),
    ) {
        ShadcnTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = placeholder,
            variant = TextFieldVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        if (filteredGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(shadcnTheme.spacing.md)) {
                ShadcnText(emptyText, style = ShadcnTextStyle.BodySmall, muted = true)
            }
        } else {
            filteredGroups.forEachIndexed { index, group ->
                if (group.heading != null) {
                    ShadcnText(
                        group.heading,
                        style = ShadcnTextStyle.LabelSmall,
                        muted = true,
                        modifier =
                            Modifier.fillMaxWidth().padding(
                                horizontal = shadcnTheme.spacing.sm,
                                vertical = shadcnTheme.spacing.xs,
                            ),
                    )
                }
                group.items.forEach { item -> CommandRow(item) }
                if (index != filteredGroups.lastIndex) ShadcnDropdownMenuSeparator()
            }
        }
    }
}

@Composable
private fun CommandRow(item: ShadcnCommandItem) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = item.onSelect)
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.sm))
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
    ) {
        ShadcnText(item.label, style = ShadcnTextStyle.BodySmall)
    }
}
