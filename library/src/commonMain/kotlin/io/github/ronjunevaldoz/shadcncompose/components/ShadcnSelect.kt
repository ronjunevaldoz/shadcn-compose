@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.SelectVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSelectItemStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A minimal dropdown select, built from [Popup] rather than a Material component
 * (this library has no Material dependency). Not a full port of shadcn/ui's
 * Radix-based Select -- no keyboard nav or typeahead yet -- but covers the common
 * "pick one of N labeled options" case.
 *
 * Usage:
 * ```
 * ShadcnSelect(
 *     value = preset,
 *     options = ShadcnStylePreset.entries,
 *     onValueChange = { preset = it },
 *     label = { it.label },
 * )
 * ```
 */
@Composable
fun <T> ShadcnSelect(
    value: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    variant: SelectVariant = SelectVariant.Default,
    style: Style = Style,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ShadcnButton(
            onClick = { expanded = true },
            variant = ButtonVariant.Outline,
            size = ButtonSize.Sm,
        ) {
            ShadcnText(label(value))
            ShadcnText("▾", style = ShadcnTextStyle.LabelSmall, muted = true)
        }

        if (expanded) {
            val popupStyle = variant.rememberStyle()
            val popupStyleState = remember { MutableStyleState(MutableInteractionSource()) }

            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 40),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(min = 140.dp)
                            .styleable(popupStyleState, popupStyle, style)
                            .padding(shadcnTheme.spacing.xxs)
                            .verticalScroll(rememberScrollState()),
                ) {
                    options.forEach { option ->
                        val isSelected = option == value
                        val itemStyle = rememberSelectItemStyle(isSelected)
                        val itemStyleState = remember { MutableStyleState(MutableInteractionSource()) }

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(option)
                                        expanded = false
                                    }
                                    .styleable(itemStyleState, itemStyle)
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
}
