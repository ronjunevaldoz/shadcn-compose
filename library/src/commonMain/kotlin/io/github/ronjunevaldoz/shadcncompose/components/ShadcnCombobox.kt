@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A searchable select. Matches real shadcn/ui's `combobox.tsx` recipe (a `Popover`
 * trigger button + `Command` search list, with a checkmark on the selected row) --
 * unlike plain [ShadcnSelect], the option list is filterable by typing.
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
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(options, query) { options.filter { label(it).contains(query, ignoreCase = true) } }

    Box(modifier = modifier) {
        ShadcnButton(
            onClick = { expanded = true },
            variant = ButtonVariant.Outline,
            modifier = Modifier.width(200.dp),
        ) {
            ShadcnText(
                value?.let(label) ?: placeholder,
                muted = value == null,
            )
            ShadcnText("⌄", style = ShadcnTextStyle.LabelSmall, muted = true)
        }

        ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(shadcnTheme.spacing.md)) {
                        ShadcnText("No results found.", style = ShadcnTextStyle.BodySmall, muted = true)
                    }
                } else {
                    filtered.forEach { option ->
                        ComboboxRow(
                            label = label(option),
                            selected = option == value,
                            onClick = {
                                onValueChange(option)
                                expanded = false
                                query = ""
                            },
                        )
                    }
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
        ShadcnText(if (selected) "✓" else " ", style = ShadcnTextStyle.BodySmall)
        ShadcnText(label, style = ShadcnTextStyle.BodySmall)
    }
}
