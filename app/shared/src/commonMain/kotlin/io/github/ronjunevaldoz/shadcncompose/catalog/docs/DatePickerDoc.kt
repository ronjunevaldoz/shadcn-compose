@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDate
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.heroicons.outline.Calendar as CalendarIcon

private val monthNames =
    listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

private fun formatDate(date: ShadcnCalendarDate): String = "${monthNames[date.month - 1]} ${date.day}, ${date.year}"

/**
 * Not a standalone registry component in real shadcn/ui either -- their own docs
 * describe it as *"built using a composition of the `<Popover />` and the `<Calendar />`
 * components"*. This page mirrors that recipe with this library's own Popover/Calendar.
 */
val datePickerDoc =
    ComponentDoc(
        id = "date-picker",
        title = "Date Picker",
        description =
            "A date picker built by composing Popover and Calendar -- not a standalone component in real " +
                "shadcn/ui either.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            var open by remember { mutableStateOf(false) }
            var selected by remember { mutableStateOf<ShadcnCalendarDate?>(null) }
            Box {
                ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                    ShadcnText(selected?.let(::formatDate) ?: "Pick a date", muted = selected == null)
                }
                ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                    ShadcnCalendar(
                        year = year, month = month, onMonthChange = { y, m -> year = y; month = m },
                        selected = selected, onSelectedChange = { selected = it; open = false },
                    )
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(2026) }
                        var month by remember { mutableStateOf(1) }
                        var selected by remember { mutableStateOf<ShadcnCalendarDate?>(null) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                ShadcnText(selected?.let(::formatDate) ?: "Pick a date", muted = selected == null)
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnCalendar(
                                    year = year,
                                    month = month,
                                    onMonthChange = { y, m -> year = y; month = m },
                                    selected = selected,
                                    onSelectedChange = {
                                        selected = it
                                        open = false
                                    },
                                )
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(2026) }
                        var month by remember { mutableStateOf(1) }
                        var selected by remember { mutableStateOf<ShadcnCalendarDate?>(null) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                ShadcnText(selected?.let(::formatDate) ?: "Pick a date", muted = selected == null)
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnCalendar(
                                    year = year,
                                    month = month,
                                    onMonthChange = { y, m ->
                                        year = y
                                        month = m
                                    },
                                    selected = selected,
                                    onSelectedChange = {
                                        selected = it
                                        open = false
                                    },
                                )
                            }
                        }
                    },
                ),
            ),
    )
