@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.icons.Check
import io.github.ronjunevaldoz.shadcncompose.icons.ChevronRight
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.icons.X
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * One heading + item bucket inside a [ShadcnCombobox] option list -- the same shape as
 * [ShadcnCommandGroup] (a group with no items left after filtering is dropped entirely).
 * Build one directly for an already-grouped option list, or pass `groupOf` to a
 * [ShadcnCombobox] overload and let it bucket a flat `options` list for you.
 */
data class ShadcnComboboxGroup<T>(val items: List<T>, val heading: String? = null)

// A self-generated ImageVector (ChevronRight rotated 90deg), not a third-party icon-set
// dependency (this library still takes none -- see README) -- a plain text glyph doesn't
// render on WasmJS (Skia has no browser emoji-font fallback). Override with any icon set
// (e.g. this repo's own demo app passes a real heroicons-outline ChevronDown -- see
// ComboboxDoc.kt).
private val defaultComboboxIcon: @Composable () -> Unit = {
    ShadcnGlyphIcon(
        ChevronRight,
        tint = shadcnTheme.colors.onSurfaceVariant,
        modifier = Modifier.rotate(90f),
        small = true,
    )
}

/**
 * A searchable select. Real shadcn/ui's current `combobox.tsx` (Base UI's `Combobox`
 * primitive, not the old Popover+Command recipe) supports multi-select chips, a clear
 * button and item grouping -- this overload is the single-select shape, matching
 * [ShadcnSelect]'s nullable-`value` convention (this library's existing precedent for
 * "pick one of N"). For multi-select, use the [values]/`onValuesChange` overload below.
 *
 * Usage:
 * ```
 * var framework by remember { mutableStateOf<String?>(null) }
 * ShadcnCombobox(
 *     value = framework,
 *     options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
 *     onValueChange = { framework = it },
 * )
 * ```
 */
@Composable
fun <T> ShadcnCombobox(
    value: T?,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    placeholder: String = "Select...",
    searchPlaceholder: String = "Search...",
    emptyText: String = "No results found.",
    // Real shadcn groups options via ComboboxGroup/ComboboxLabel. Optional: return a
    // heading per option (null = ungrouped) and options are bucketed by first-seen key.
    groupOf: ((T) -> String?)? = null,
    // Real shadcn's `showClear` prop. Null (default) renders no clear button -- opt in
    // by supplying a callback that clears the caller's own state.
    onClear: (() -> Unit)? = null,
    icon: @Composable () -> Unit = defaultComboboxIcon,
) {
    var expanded by remember { mutableStateOf(false) }
    val groups = remember(options, groupOf) { groupOptions(options, groupOf) }

    Box(modifier = modifier) {
        ComboboxTrigger(
            expanded = expanded,
            onExpand = { expanded = true },
            onClear = onClear?.takeIf { value != null },
            icon = icon,
        ) {
            ShadcnText(value?.let(label) ?: placeholder, muted = value == null)
        }

        ComboboxPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            groups = groups,
            label = label,
            searchPlaceholder = searchPlaceholder,
            emptyText = emptyText,
            isSelected = { it == value },
            onSelect = { option ->
                onValueChange(option)
                expanded = false
            },
        )
    }
}

/**
 * The multi-select shape of real shadcn/ui's current `combobox.tsx` (`multiple` prop +
 * `ComboboxChips`) -- [values] holds every currently-picked option, rendered as removable
 * chips ([ShadcnBadge]) in the trigger instead of a single label. Single-select stays the
 * default entry point (the overload above) since that's this library's existing
 * [ShadcnSelect] convention and the common case; opt into multi-select explicitly by
 * calling this overload instead of flipping a boolean on the single-select one, so a
 * caller's `value: T?` vs `values: List<T>` state shape is obvious at the call site.
 * [options] is the full pickable list, and [onValuesChange] fires with the updated
 * selection on every toggle.
 *
 * The popup stays open after a pick (real Base UI multi-select combobox behaves the same
 * way) so multiple options can be toggled in one session; dismiss by clicking outside or
 * picking the trigger again.
 *
 * Usage:
 * ```
 * var frameworks by remember { mutableStateOf(emptyList<String>()) }
 * ShadcnCombobox(
 *     values = frameworks,
 *     options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
 *     onValuesChange = { frameworks = it },
 * )
 * ```
 */
@Composable
fun <T> ShadcnCombobox(
    values: List<T>,
    options: List<T>,
    onValuesChange: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    placeholder: String = "Select...",
    searchPlaceholder: String = "Search...",
    emptyText: String = "No results found.",
    groupOf: ((T) -> String?)? = null,
    onClear: (() -> Unit)? = null,
    icon: @Composable () -> Unit = defaultComboboxIcon,
) {
    var expanded by remember { mutableStateOf(false) }
    val groups = remember(options, groupOf) { groupOptions(options, groupOf) }

    Box(modifier = modifier) {
        ComboboxTrigger(
            expanded = expanded,
            onExpand = { expanded = true },
            onClear = onClear?.takeIf { values.isNotEmpty() },
            icon = icon,
        ) {
            if (values.isEmpty()) {
                ShadcnText(placeholder, muted = true)
            } else {
                // Chips scroll horizontally rather than wrap to a second line -- keeps
                // the trigger's height fixed like every other control in this library.
                // ponytail: no multi-line wrap, swap to FlowRow if a caller needs it.
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    values.forEach { option ->
                        ComboboxChip(
                            label = label(option),
                            onRemove = { onValuesChange(values - option) },
                        )
                    }
                }
            }
        }

        ComboboxPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            groups = groups,
            label = label,
            searchPlaceholder = searchPlaceholder,
            emptyText = emptyText,
            isSelected = { it in values },
            onSelect = { option ->
                onValuesChange(if (option in values) values - option else values + option)
            },
        )
    }
}

private fun <T> groupOptions(
    options: List<T>,
    groupOf: ((T) -> String?)?,
): List<ShadcnComboboxGroup<T>> =
    if (groupOf == null) {
        listOf(ShadcnComboboxGroup(options))
    } else {
        options.groupBy(groupOf).map { (heading, items) -> ShadcnComboboxGroup(items, heading) }
    }

@Composable
private fun ComboboxTrigger(
    expanded: Boolean,
    onExpand: () -> Unit,
    onClear: (() -> Unit)?,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val triggerInteractionSource = remember { MutableInteractionSource() }
    val triggerStyleState = rememberUpdatedStyleState(triggerInteractionSource) { it.isEnabled = true }

    // Not ShadcnButton: its content Row centers children as a tight group
    // (Arrangement.spacedBy(Alignment.CenterHorizontally)), which leaves the chevron
    // floating next to the label instead of pinned to the trigger's right edge like real
    // shadcn's `justify-between` combobox trigger. A custom Row with
    // Arrangement.SpaceBetween (same fix already applied to ShadcnSelect's trigger) gets
    // the chevron to the correct spot.
    Box(
        modifier =
            Modifier
                .width(200.dp)
                .clickable(
                    interactionSource = triggerInteractionSource,
                    indication = null,
                    role = Role.Button,
                ) { onExpand() }
                .styleable(
                    triggerStyleState,
                    ButtonVariant.Outline.rememberStyle() then ButtonSize.Md.rememberStyle(),
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) { content() }
            Row(
                horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onClear != null) {
                    Box(
                        modifier =
                            Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onClear,
                                ),
                    ) {
                        ShadcnGlyphIcon(X, tint = shadcnTheme.colors.onSurfaceVariant, small = true)
                    }
                }
                icon()
            }
        }
    }
}

@Composable
private fun ComboboxChip(
    label: String,
    onRemove: () -> Unit,
) {
    ShadcnBadge(variant = BadgeVariant.Secondary) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnText(label, style = ShadcnTextStyle.BodySmall)
            Box(
                modifier =
                    Modifier
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onRemove()
                        },
            ) {
                ShadcnGlyphIcon(X, tint = shadcnTheme.colors.onSurfaceVariant, small = true)
            }
        }
    }
}

@Composable
private fun <T> ComboboxPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    groups: List<ShadcnComboboxGroup<T>>,
    label: (T) -> String,
    searchPlaceholder: String,
    emptyText: String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredGroups =
        remember(groups, query) {
            groups
                .map {
                        group ->
                    group.copy(items = group.items.filter { label(it).contains(query, ignoreCase = true) })
                }
                .filter { it.items.isNotEmpty() }
        }

    ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = onDismissRequest) {
        Column(
            modifier =
                Modifier
                    .width(200.dp)
                    .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                    .padding(shadcnTheme.spacing.sm),
        ) {
            ShadcnTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = searchPlaceholder,
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
                    group.items.forEach { option ->
                        ComboboxRow(
                            label = label(option),
                            selected = isSelected(option),
                            onClick = { onSelect(option) },
                        )
                    }
                    if (index != filteredGroups.lastIndex) ShadcnDropdownMenuSeparator()
                }
            }
        }
    }
}

@Composable
private fun ComboboxRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.sm))
                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // A fixed-width slot, not a conditionally-emitted composable directly -- the
        // previous ShadcnText(" ") placeholder reserved this width via font metrics; an
        // empty `if` branch reserves none, which would shift unselected rows' labels left.
        Box(modifier = Modifier.size(shadcnTheme.icons.smallSize)) {
            if (selected) ShadcnGlyphIcon(Check, tint = shadcnTheme.colors.onSurface, small = true)
        }
        ShadcnText(label, style = ShadcnTextStyle.BodySmall)
    }
}
