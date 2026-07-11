@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

data class ToggleGroupItem(val value: String, val label: String)

/**
 * A segmented row of [ShadcnToggle]s. Real shadcn/ui gives every item its own border
 * and drops the shared edge via CSS (`:not(:first-child)` etc.); the Style API here
 * has no per-side border control, so the group itself draws a single shared border
 * (Outline variant only) and each item is borderless -- same visual result, simpler
 * implementation. Supports single- or multi-select depending on how [selected] and
 * [onSelectedChange] are wired by the caller.
 *
 * Usage:
 * ```
 * var selected by remember { mutableStateOf(setOf("bold")) }
 * ShadcnToggleGroup(
 *     items = listOf(ToggleGroupItem("bold", "B"), ToggleGroupItem("italic", "I")),
 *     selected = selected,
 *     onSelectedChange = { value -> selected = if (value in selected) selected - value else selected + value },
 * )
 * ```
 */
@Composable
fun ShadcnToggleGroup(
    items: List<ToggleGroupItem>,
    selected: Set<String>,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: ToggleVariant = ToggleVariant.Default,
) {
    val groupShape = RoundedCornerShape(shadcnTheme.shapes.lg)
    val outerModifier =
        if (variant == ToggleVariant.Outline) {
            modifier
                .clip(groupShape)
                .border(1.dp, shadcnTheme.colors.border, groupShape)
        } else {
            modifier
        }

    Row(modifier = outerModifier) {
        items.forEachIndexed { index, item ->
            // Only the group's outer edge is rounded -- matches real shadcn's
            // `:not(:first-child)`/`:not(:last-child)` corner-dropping via CSS.
            val rounded = shadcnTheme.shapes.lg
            val none = 0.dp
            val corners =
                when {
                    items.size == 1 -> ShadcnGroupCorners(rounded, rounded, rounded, rounded)
                    index == 0 -> ShadcnGroupCorners(rounded, none, none, rounded)
                    index == items.lastIndex -> ShadcnGroupCorners(none, rounded, rounded, none)
                    else -> ShadcnGroupCorners(none, none, none, none)
                }
            val itemShape = RoundedCornerShape(corners.topStart, corners.topEnd, corners.bottomEnd, corners.bottomStart)
            ShadcnToggle(
                pressed = item.value in selected,
                onPressedChange = { onSelectedChange(item.value) },
                variant = variant,
                style =
                    Style {
                        shape(itemShape)
                        if (variant == ToggleVariant.Outline) borderWidth(0.dp)
                    },
            ) {
                ShadcnText(item.label)
            }
        }
    }
}
