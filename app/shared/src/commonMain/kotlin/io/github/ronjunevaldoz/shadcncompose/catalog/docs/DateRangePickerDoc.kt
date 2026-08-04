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
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDate
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDateRange
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarRange
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.heroicons.outline.Calendar as CalendarIcon

private val rangeMonthNames =
    listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

private fun formatRangeDate(date: ShadcnCalendarDate): String =
    "${rangeMonthNames[date.month - 1]} ${date.day}, ${date.year}"

private fun formatRange(range: ShadcnCalendarDateRange): String {
    val start = range.start
    val end = range.end
    return when {
        start != null && end != null -> "${formatRangeDate(start)} - ${formatRangeDate(end)}"
        start != null -> formatRangeDate(start)
        else -> "Pick a date range"
    }
}

/**
 * Not a standalone registry component in real shadcn/ui either -- same "Popover + Calendar"
 * composition as [datePickerDoc], with `Calendar`'s `mode="range"` instead of the default
 * single-day mode. This page mirrors that recipe with this library's own Popover/CalendarRange.
 */
val dateRangePickerDoc =
    ComponentDoc(
        id = "date-range-picker",
        referenceUrl = "https://ui.shadcn.com/docs/components/base/date-picker",
        title = "Date Range Picker",
        description =
            "A date range picker built by composing Popover and Calendar's range mode -- not a " +
                "standalone component in real shadcn/ui either.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            var open by remember { mutableStateOf(false) }
            var range by remember { mutableStateOf(ShadcnCalendarDateRange()) }
            Box {
                ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                    ShadcnText(formatRange(range), muted = range.start == null)
                }
                ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                    ShadcnCalendarRange(
                        year = year, month = month, onMonthChange = { y, m -> year = y; month = m },
                        range = range,
                        onRangeChange = { range = it; if (it.end != null) open = false },
                        numberOfMonths = 2,
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
                        var month by remember { mutableStateOf(3) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange()) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                ShadcnText(formatRange(range), muted = range.start == null)
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                ShadcnCalendarRange(
                                    year = year,
                                    month = month,
                                    onMonthChange = { y, m -> year = y; month = m },
                                    range = range,
                                    onRangeChange = {
                                        range = it
                                        if (it.end != null) open = false
                                    },
                                    numberOfMonths = 2,
                                )
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(2026) }
                        var month by remember { mutableStateOf(3) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange()) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                ShadcnText(formatRange(range), muted = range.start == null)
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                ShadcnCalendarRange(
                                    year = year,
                                    month = month,
                                    onMonthChange = { y, m ->
                                        year = y
                                        month = m
                                    },
                                    range = range,
                                    onRangeChange = {
                                        range = it
                                        if (it.end != null) open = false
                                    },
                                    numberOfMonths = 2,
                                )
                            }
                        }
                    },
                ),
            ),
    )
