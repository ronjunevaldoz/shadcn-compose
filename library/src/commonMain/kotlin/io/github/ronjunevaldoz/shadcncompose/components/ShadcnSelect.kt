@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.styles.SelectVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberPanelStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSelectItemStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A plain (non-searchable) dropdown select, built from [ShadcnAnchoredPopup] rather than
 * a Material component (this library has no Material dependency). Matches real
 * shadcn/ui's `select.tsx` -- a bordered field trigger (`border-input bg-transparent`),
 * not a `Button` -- unlike [ShadcnCombobox], the option list is not filterable by typing.
 * Not a full port of shadcn/ui's Radix-based Select -- no keyboard nav or typeahead yet --
 * but covers the common "pick one of N labeled options" case.
 *
 * [value] is nullable so the trigger can show [placeholder] text before anything is
 * picked (real shadcn's own canonical demo starts empty); pass a non-null default when
 * the control should never be empty (e.g. this catalog's own theme-preset pickers in
 * `CatalogTopBar`, which always have a current preset).
 *
 * Usage:
 * ```
 * var theme by remember { mutableStateOf<String?>(null) }
 * ShadcnSelect(
 *     value = theme,
 *     options = listOf("Light", "Dark", "System"),
 *     onValueChange = { theme = it },
 *     placeholder = "Theme",
 * )
 * ```
 */
@Composable
fun <T> ShadcnSelect(
    value: T?,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    placeholder: String = "Select an option",
    variant: SelectVariant = SelectVariant.Default,
    style: Style = Style,
) {
    var expanded by remember { mutableStateOf(false) }
    val triggerInteractionSource = remember { MutableInteractionSource() }
    val triggerStyleState = remember { MutableStyleState(triggerInteractionSource) }

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .widthIn(min = 140.dp)
                    .styleable(triggerStyleState, variant.rememberStyle(), style)
                    .clickable(interactionSource = triggerInteractionSource, indication = null) { expanded = true }
                    .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnText(value?.let(label) ?: placeholder, style = ShadcnTextStyle.BodySmall, muted = value == null)
            ShadcnText("⌄", style = ShadcnTextStyle.LabelSmall, muted = true)
        }

        ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            val panelStyleState = remember { MutableStyleState(MutableInteractionSource()) }
            Column(
                modifier =
                    Modifier
                        // A max is required, not just min -- Popup's incoming constraints
                        // aren't bounded to the trigger's own size, so an option Row's
                        // fillMaxWidth() below would otherwise resolve against whatever
                        // unbounded/window-sized constraint the popup layer receives,
                        // blowing the panel out to the full viewport width instead of
                        // wrapping the option labels.
                        .widthIn(min = 140.dp, max = 280.dp)
                        .styleable(panelStyleState, variant.rememberPanelStyle())
                        .padding(shadcnTheme.spacing.xxs)
                        .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    val itemStyleState = remember { MutableStyleState(itemInteractionSource) }

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(interactionSource = itemInteractionSource, indication = null) {
                                    onValueChange(option)
                                    expanded = false
                                }
                                .styleable(itemStyleState, rememberSelectItemStyle(isSelected))
                                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShadcnText(label(option), style = ShadcnTextStyle.BodyMedium)
                    }
                }
            }
        }
    }
}
