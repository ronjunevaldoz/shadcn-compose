@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDate
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDateRange
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarRange
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDateInput
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSwitch
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
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

// Local, catalog-only date arithmetic for the "Presets" example below -- shadcn/core has no
// kotlinx-datetime dependency and exposes none of this (ShadcnCalendar.kt's own version is
// private), so this small, self-contained copy mirrors ShadcnCalendar.kt's own Sakamoto-based
// day-of-week/days-in-month logic rather than pulling in a date library for one demo.
private fun isRangeLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun daysInRangeMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isRangeLeapYear(year)) 29 else 28
        else -> error("month must be 1..12, was $month")
    }

private fun addDaysToRangeDate(
    date: ShadcnCalendarDate,
    delta: Int,
): ShadcnCalendarDate {
    var year = date.year
    var month = date.month
    var day = date.day + delta
    while (day < 1) {
        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        day += daysInRangeMonth(year, month)
    }
    while (day > daysInRangeMonth(year, month)) {
        day -= daysInRangeMonth(year, month)
        month += 1
        if (month > 12) {
            month = 1
            year += 1
        }
    }
    return ShadcnCalendarDate(year, month, day)
}

private val RANGE_SAKAMOTO_OFFSETS = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

/** Sunday = 0 .. Saturday = 6, matching ShadcnCalendar.kt's own weekday grid convention. */
private fun rangeDayOfWeek(date: ShadcnCalendarDate): Int {
    val y = if (date.month < 3) date.year - 1 else date.year
    return (y + y / 4 - y / 100 + y / 400 + RANGE_SAKAMOTO_OFFSETS[date.month - 1] + date.day) % 7
}

private data class DateRangePreset(val label: String, val rangeFor: (ShadcnCalendarDate) -> ShadcnCalendarDateRange)

private val DATE_RANGE_PRESETS =
    listOf(
        DateRangePreset("Today") { today -> ShadcnCalendarDateRange(today, today) },
        DateRangePreset("Yesterday") { today -> addDaysToRangeDate(today, -1).let { ShadcnCalendarDateRange(it, it) } },
        DateRangePreset("This Week") { today ->
            ShadcnCalendarDateRange(addDaysToRangeDate(today, -rangeDayOfWeek(today)), today)
        },
        DateRangePreset("Last Week") { today ->
            val end = addDaysToRangeDate(today, -rangeDayOfWeek(today) - 1)
            ShadcnCalendarDateRange(addDaysToRangeDate(end, -6), end)
        },
        DateRangePreset("Last 7 Days") { today -> ShadcnCalendarDateRange(addDaysToRangeDate(today, -6), today) },
        DateRangePreset("This Month") { today ->
            ShadcnCalendarDateRange(ShadcnCalendarDate(today.year, today.month, 1), today)
        },
        DateRangePreset("Last Month") { today ->
            val (y, m) = if (today.month == 1) today.year - 1 to 12 else today.year to today.month - 1
            ShadcnCalendarDateRange(ShadcnCalendarDate(y, m, 1), ShadcnCalendarDate(y, m, daysInRangeMonth(y, m)))
        },
        DateRangePreset("This Year") { today ->
            ShadcnCalendarDateRange(ShadcnCalendarDate(today.year, 1, 1), today)
        },
        DateRangePreset("Last Year") { today ->
            ShadcnCalendarDateRange(
                ShadcnCalendarDate(today.year - 1, 1, 1),
                ShadcnCalendarDate(today.year - 1, 12, 31),
            )
        },
    )

private fun shiftYear(
    date: ShadcnCalendarDate,
    delta: Int,
): ShadcnCalendarDate {
    val newYear = date.year + delta
    return ShadcnCalendarDate(newYear, date.month, date.day.coerceAtMost(daysInRangeMonth(newYear, date.month)))
}

/**
 * The default comparison range shown the moment "Compare" is first toggled on -- matches the
 * real reference's convention (`date-range-picker-for-shadcn`'s `date-range-picker.tsx`).
 * After this, the calendar never edits the comparison range again -- only the [ShadcnDateInput]
 * fields below can, per that same reference's exact interaction model.
 */
private fun samePeriodLastYear(range: ShadcnCalendarDateRange): ShadcnCalendarDateRange? {
    val start = range.start ?: return null
    val end = range.end ?: return null
    return ShadcnCalendarDateRange(shiftYear(start, -1), shiftYear(end, -1))
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
                ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null, contentPadding = 0.dp) {
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
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null, contentPadding = 0.dp) {
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
                            ShadcnPopover(
                                expanded = open,
                                onDismissRequest = { open = false },
                                width = null,
                                contentPadding = 0.dp,
                            ) {
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
                ComponentExample(
                    title = "Presets",
                    code =
                        """
                        // "today" fixed for this demo -- no real device-clock dependency.
                        val today = remember { ShadcnCalendarDate(2026, 3, 15) }
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(today.year) }
                        var month by remember { mutableStateOf(today.month) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange(today, today)) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(imageVector = CalendarIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                ShadcnText(formatRange(range))
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                                    Column(
                                        modifier = Modifier.width(140.dp),
                                        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                                    ) {
                                        DATE_RANGE_PRESETS.forEach { preset ->
                                            val presetRange = preset.rangeFor(today)
                                            ShadcnButton(
                                                onClick = {
                                                    range = presetRange
                                                    presetRange.start?.let { year = it.year; month = it.month }
                                                },
                                                variant = if (range == presetRange) ButtonVariant.Secondary else ButtonVariant.Ghost,
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterStart,
                                            ) { ShadcnText(preset.label) }
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                            ShadcnSelect(
                                                value = month, options = (1..12).toList(),
                                                onValueChange = { month = it }, label = { rangeMonthNames[it - 1] },
                                            )
                                            ShadcnSelect(
                                                value = year, options = (2024..2028).toList(),
                                                onValueChange = { year = it },
                                            )
                                        }
                                        ShadcnCalendarRange(
                                            year = year, month = month,
                                            onMonthChange = { y, m -> year = y; month = m },
                                            range = range, onRangeChange = { range = it },
                                            today = today, numberOfMonths = 2,
                                        )
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        val today = remember { ShadcnCalendarDate(2026, 3, 15) }
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(today.year) }
                        var month by remember { mutableStateOf(today.month) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange(today, today)) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                ShadcnText(formatRange(range))
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                                    Column(
                                        modifier = Modifier.width(140.dp),
                                        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                                    ) {
                                        DATE_RANGE_PRESETS.forEach { preset ->
                                            val presetRange = preset.rangeFor(today)
                                            ShadcnButton(
                                                onClick = {
                                                    range = presetRange
                                                    presetRange.start?.let {
                                                        year = it.year
                                                        month = it.month
                                                    }
                                                },
                                                variant =
                                                    if (range == presetRange) {
                                                        ButtonVariant.Secondary
                                                    } else {
                                                        ButtonVariant.Ghost
                                                    },
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterStart,
                                            ) { ShadcnText(preset.label) }
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                            ShadcnSelect(
                                                value = month,
                                                options = (1..12).toList(),
                                                onValueChange = { month = it },
                                                label = { rangeMonthNames[it - 1] },
                                            )
                                            ShadcnSelect(
                                                value = year,
                                                options = (2024..2028).toList(),
                                                onValueChange = { year = it },
                                            )
                                        }
                                        ShadcnCalendarRange(
                                            year = year,
                                            month = month,
                                            onMonthChange = { y, m ->
                                                year = y
                                                month = m
                                            },
                                            range = range,
                                            onRangeChange = { range = it },
                                            today = today,
                                            numberOfMonths = 2,
                                        )
                                    }
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Compare",
                    code =
                        """
                        val today = remember { ShadcnCalendarDate(2026, 3, 15) }
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(today.year) }
                        var month by remember { mutableStateOf(today.month) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange(today, today)) }
                        var compareEnabled by remember { mutableStateOf(false) }
                        // Independent state, not derived from `range` -- once Compare is toggled
                        // on, the calendar never edits this again, only the fields below can.
                        var comparisonRange by remember { mutableStateOf<ShadcnCalendarDateRange?>(null) }

                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(imageVector = CalendarIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Column {
                                    ShadcnText(formatRange(range))
                                    comparisonRange?.let {
                                        ShadcnText("vs. " + formatRange(it), muted = true, style = ShadcnTextStyle.LabelSmall)
                                    }
                                }
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ShadcnSwitch(
                                            checked = compareEnabled,
                                            onCheckedChange = { checked ->
                                                compareEnabled = checked
                                                comparisonRange = if (checked) samePeriodLastYear(range) else null
                                            },
                                        )
                                        ShadcnText("Compare to same period last year")
                                    }
                                    comparisonRange?.let { compRange ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            ShadcnDateInput(
                                                value = compRange.start ?: today,
                                                onValueChange = { comparisonRange = compRange.copy(start = it) },
                                            )
                                            ShadcnText("-", muted = true)
                                            ShadcnDateInput(
                                                value = compRange.end ?: today,
                                                onValueChange = { comparisonRange = compRange.copy(end = it) },
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                                        Column(
                                            modifier = Modifier.width(140.dp),
                                            verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                                        ) {
                                            DATE_RANGE_PRESETS.forEach { preset ->
                                                val presetRange = preset.rangeFor(today)
                                                ShadcnButton(
                                                    onClick = {
                                                        range = presetRange
                                                        presetRange.start?.let { year = it.year; month = it.month }
                                                    },
                                                    variant = if (range == presetRange) ButtonVariant.Secondary else ButtonVariant.Ghost,
                                                    modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterStart,
                                                ) { ShadcnText(preset.label) }
                                            }
                                        }
                                        ShadcnCalendarRange(
                                            year = year, month = month,
                                            onMonthChange = { y, m -> year = y; month = m },
                                            range = range, onRangeChange = { range = it },
                                            today = today, numberOfMonths = 2,
                                            comparisonRange = comparisonRange,
                                            // Bold blue, unmistakably distinct from the
                                            // primary range's black/gray in either theme --
                                            // the default secondary color is intentionally
                                            // subtle in light mode, see ShadcnCalendarRange's
                                            // own doc comment.
                                            comparisonColor = Color(0xFF2563EB),
                                            onComparisonColor = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        val today = remember { ShadcnCalendarDate(2026, 3, 15) }
                        var open by remember { mutableStateOf(false) }
                        var year by remember { mutableStateOf(today.year) }
                        var month by remember { mutableStateOf(today.month) }
                        var range by remember { mutableStateOf(ShadcnCalendarDateRange(today, today)) }
                        var compareEnabled by remember { mutableStateOf(true) }
                        var comparisonRange by
                            remember { mutableStateOf(samePeriodLastYear(ShadcnCalendarDateRange(today, today))) }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                Image(
                                    imageVector = CalendarIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
                                )
                                Column {
                                    ShadcnText(formatRange(range))
                                    comparisonRange?.let {
                                        ShadcnText(
                                            "vs. " + formatRange(it),
                                            muted = true,
                                            style = ShadcnTextStyle.LabelSmall,
                                        )
                                    }
                                }
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }, width = null) {
                                Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ShadcnSwitch(
                                            checked = compareEnabled,
                                            onCheckedChange = { checked ->
                                                compareEnabled = checked
                                                comparisonRange = if (checked) samePeriodLastYear(range) else null
                                            },
                                        )
                                        ShadcnText("Compare to same period last year")
                                    }
                                    comparisonRange?.let { compRange ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            ShadcnDateInput(
                                                value = compRange.start ?: today,
                                                onValueChange = { comparisonRange = compRange.copy(start = it) },
                                            )
                                            ShadcnText("-", muted = true)
                                            ShadcnDateInput(
                                                value = compRange.end ?: today,
                                                onValueChange = { comparisonRange = compRange.copy(end = it) },
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                                        Column(
                                            modifier = Modifier.width(140.dp),
                                            verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
                                        ) {
                                            DATE_RANGE_PRESETS.forEach { preset ->
                                                val presetRange = preset.rangeFor(today)
                                                ShadcnButton(
                                                    onClick = {
                                                        range = presetRange
                                                        presetRange.start?.let {
                                                            year = it.year
                                                            month = it.month
                                                        }
                                                    },
                                                    variant =
                                                        if (range == presetRange) {
                                                            ButtonVariant.Secondary
                                                        } else {
                                                            ButtonVariant.Ghost
                                                        },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.CenterStart,
                                                ) { ShadcnText(preset.label) }
                                            }
                                        }
                                        ShadcnCalendarRange(
                                            year = year,
                                            month = month,
                                            onMonthChange = { y, m ->
                                                year = y
                                                month = m
                                            },
                                            range = range,
                                            onRangeChange = { range = it },
                                            today = today,
                                            numberOfMonths = 2,
                                            comparisonRange = comparisonRange,
                                            // Bold blue, unmistakably distinct from the
                                            // primary range's black/gray in either theme --
                                            // the default secondary color is intentionally
                                            // subtle in light mode, see ShadcnCalendarRange's
                                            // own doc comment.
                                            comparisonColor = Color(0xFF2563EB),
                                            onComparisonColor = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    },
                ),
            ),
    )
