package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** A plain Gregorian calendar date -- the library has no `kotlinx-datetime` dependency, so this stays a minimal value type. */
data class ShadcnCalendarDate(val year: Int, val month: Int, val day: Int) : Comparable<ShadcnCalendarDate> {
    override fun compareTo(other: ShadcnCalendarDate): Int {
        if (year != other.year) return year - other.year
        if (month != other.month) return month - other.month
        return day - other.day
    }
}

private val MONTH_NAMES =
    listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
private val WEEKDAY_LABELS = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

private fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun daysInMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> error("month must be 1..12, was $month")
    }

// Sakamoto's algorithm: 0 = Sunday .. 6 = Saturday.
private val SAKAMOTO_OFFSETS = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

private fun dayOfWeek(
    year: Int,
    month: Int,
    day: Int,
): Int {
    val y = if (month < 3) year - 1 else year
    return (y + y / 4 - y / 100 + y / 400 + SAKAMOTO_OFFSETS[month - 1] + day) % 7
}

private fun previousMonth(
    year: Int,
    month: Int,
): Pair<Int, Int> = if (month == 1) year - 1 to 12 else year to month - 1

private fun nextMonth(
    year: Int,
    month: Int,
): Pair<Int, Int> = if (month == 12) year + 1 to 1 else year to month + 1

/**
 * A single-month date picker grid, matching real shadcn/ui's `calendar.tsx` layout
 * (month caption with prev/next chevrons, weekday row, 6x7 day grid with outside-month
 * days shown muted). Real shadcn wraps `react-day-picker`; since there is no KMP
 * equivalent and no `kotlinx-datetime` dependency in this library, the month grid is
 * computed here directly via Sakamoto's algorithm -- plain Gregorian arithmetic, no
 * external date library needed for what is fundamentally a fixed 42-cell grid.
 *
 * [year]/[month] (the *displayed* month, 1-indexed) are hoisted separately from
 * [selected] so navigating months doesn't require a selection.
 *
 * Usage:
 * ```
 * var year by remember { mutableStateOf(2026) }
 * var month by remember { mutableStateOf(3) }
 * var selected by remember { mutableStateOf<ShadcnCalendarDate?>(null) }
 * ShadcnCalendar(
 *     year = year, month = month,
 *     onMonthChange = { y, m -> year = y; month = m },
 *     selected = selected, onSelectedChange = { selected = it },
 * )
 * ```
 */
@Composable
fun ShadcnCalendar(
    year: Int,
    month: Int,
    onMonthChange: (year: Int, month: Int) -> Unit,
    selected: ShadcnCalendarDate?,
    onSelectedChange: (ShadcnCalendarDate) -> Unit,
    modifier: Modifier = Modifier,
    today: ShadcnCalendarDate? = null,
) {
    Column(
        modifier = modifier.padding(shadcnTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        CalendarHeader(year, month, onMonthChange)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.size(CELL_SIZE * 7, CELL_SIZE)) {
            WEEKDAY_LABELS.forEach { label ->
                Box(modifier = Modifier.size(CELL_SIZE), contentAlignment = Alignment.Center) {
                    ShadcnText(label, style = ShadcnTextStyle.LabelSmall, muted = true)
                }
            }
        }
        val weeks = remember(year, month) { buildMonthGrid(year, month) }
        weeks.forEach { week ->
            Row {
                week.forEach { cell ->
                    CalendarDayCell(
                        cell = cell,
                        isSelected = selected != null && cell.date == selected,
                        isToday = today != null && cell.date == today,
                        onClick = { onSelectedChange(cell.date) },
                    )
                }
            }
        }
    }
}

private val CELL_SIZE = 36.dp

private data class CalendarCell(val date: ShadcnCalendarDate, val inCurrentMonth: Boolean)

private fun buildMonthGrid(
    year: Int,
    month: Int,
): List<List<CalendarCell>> {
    val firstWeekday = dayOfWeek(year, month, 1)
    val daysThisMonth = daysInMonth(year, month)
    val (prevYear, prevMonth) = previousMonth(year, month)
    val daysPrevMonth = daysInMonth(prevYear, prevMonth)
    val (nextYear, nextMonth) = nextMonth(year, month)

    val cells = mutableListOf<CalendarCell>()
    for (i in firstWeekday downTo 1) {
        cells += CalendarCell(ShadcnCalendarDate(prevYear, prevMonth, daysPrevMonth - i + 1), inCurrentMonth = false)
    }
    for (day in 1..daysThisMonth) {
        cells += CalendarCell(ShadcnCalendarDate(year, month, day), inCurrentMonth = true)
    }
    var nextDay = 1
    while (cells.size < 42) {
        cells += CalendarCell(ShadcnCalendarDate(nextYear, nextMonth, nextDay), inCurrentMonth = false)
        nextDay++
    }
    return cells.chunked(7)
}

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    onMonthChange: (year: Int, month: Int) -> Unit,
) {
    Row(
        modifier = Modifier.size(CELL_SIZE * 7, CELL_SIZE),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarNavButton("‹") {
            val (y, m) = previousMonth(year, month)
            onMonthChange(y, m)
        }
        ShadcnText("${MONTH_NAMES[month - 1]} $year", style = ShadcnTextStyle.LabelLarge)
        CalendarNavButton("›") {
            val (y, m) = nextMonth(year, month)
            onMonthChange(y, m)
        }
    }
}

@Composable
private fun CalendarNavButton(
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnText(label, style = ShadcnTextStyle.BodyMedium)
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .size(CELL_SIZE)
                .padding(2.dp)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .let {
                    if (isSelected) {
                        it.background(shadcnTheme.colors.primary)
                    } else if (isToday) {
                        it.border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                    } else {
                        it
                    }
                }
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnText(
            cell.date.day.toString(),
            style = ShadcnTextStyle.BodySmall,
            muted = !cell.inCurrentMonth,
            color = if (isSelected) shadcnTheme.colors.onPrimary else Color.Unspecified,
        )
    }
}
