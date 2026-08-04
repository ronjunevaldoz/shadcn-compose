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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.interaction.RovingFocusIndex
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
 * `cmdk`), used standalone as a command palette. [ShadcnCombobox] is NOT built on this:
 * real shadcn/ui moved Combobox to a separate Base UI-backed `combobox.tsx` component
 * (multi-select chips, a clear button, item grouping), and this library's
 * [ShadcnCombobox] mirrors that shape directly rather than composing `ShadcnCommand`
 * with a trigger + popover. Filters each group's items by case-insensitive substring
 * match against `label` as the user types, dropping groups left with no matches.
 *
 * Groups use a plain data model rather than a slot API (unlike [ShadcnDropdownMenu])
 * because filtering needs to inspect every item's label up front -- a freely-composed
 * slot API would need extra machinery to hide non-matching children mid-composition.
 *
 * Keyboard nav: unlike every other component in this batch, this does *not* use the shared
 * `Modifier.rovingFocusGroup` -- real cmdk keeps the search field itself focused the whole
 * time and moves a virtual "highlighted" index instead of moving actual focus between rows
 * (arrow keys would otherwise fight text-cursor movement inside the field). So this tracks
 * `highlightedIndex` directly (via [RovingFocusIndex]'s wrap-around math) over the flattened,
 * already-filtered item list, Up/Down/Home/End move it, and Enter selects the highlighted
 * row -- same wrap-around/Home-End semantics as the shared primitive, just applied to a plain
 * index instead of real focus. The highlight only renders once the user has actually pressed
 * an arrow key (never on first render), so a plain screenshot of this component is unaffected.
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
    val flatItems = remember(filteredGroups) { filteredGroups.flatMap { it.items } }

    // Reset (via the `query` remember key, not a side effect) whenever the result set changes
    // underneath the user -- a stale highlighted index pointing past the new, shorter list
    // would either select the wrong row or silently no-op.
    var highlightedIndex by remember(query) { mutableStateOf(0) }
    var keyboardActive by remember(query) { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .width(280.dp)
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .padding(shadcnTheme.spacing.sm)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || flatItems.isEmpty()) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionDown -> {
                            highlightedIndex = RovingFocusIndex.next(highlightedIndex, flatItems.size)
                            keyboardActive = true
                            true
                        }
                        Key.DirectionUp -> {
                            highlightedIndex = RovingFocusIndex.previous(highlightedIndex, flatItems.size)
                            keyboardActive = true
                            true
                        }
                        Key.MoveHome -> {
                            highlightedIndex = RovingFocusIndex.first(flatItems.size)
                            keyboardActive = true
                            true
                        }
                        Key.MoveEnd -> {
                            highlightedIndex = RovingFocusIndex.last(flatItems.size)
                            keyboardActive = true
                            true
                        }
                        Key.Enter, Key.NumPadEnter -> {
                            if (keyboardActive) flatItems[highlightedIndex].onSelect()
                            keyboardActive
                        }
                        else -> false
                    }
                },
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
            var rowIndex = 0
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
                group.items.forEach { item ->
                    val isHighlighted = keyboardActive && rowIndex == highlightedIndex
                    CommandRow(item, highlighted = isHighlighted)
                    rowIndex++
                }
                if (index != filteredGroups.lastIndex) ShadcnDropdownMenuSeparator()
            }
        }
    }
}

@Composable
private fun CommandRow(
    item: ShadcnCommandItem,
    highlighted: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = item.onSelect)
                .background(
                    if (highlighted) shadcnTheme.colors.secondary else shadcnTheme.colors.popover,
                    RoundedCornerShape(shadcnTheme.shapes.sm),
                )
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
    ) {
        ShadcnText(item.label, style = ShadcnTextStyle.BodySmall)
    }
}
