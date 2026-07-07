@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
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
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(shadcnTheme.shapes.md)

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
                            .clip(shape)
                            .background(shadcnTheme.colors.surface, shape)
                            .border(1.dp, shadcnTheme.colors.border, shape)
                            .padding(shadcnTheme.spacing.xxs)
                            .verticalScroll(rememberScrollState()),
                ) {
                    options.forEach { option ->
                        val isSelected = option == value
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(shadcnTheme.shapes.sm))
                                    .clickable {
                                        onValueChange(option)
                                        expanded = false
                                    }
                                    .background(
                                        if (isSelected) shadcnTheme.colors.secondary else shadcnTheme.colors.surface,
                                    )
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
