@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

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
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.icons.ChevronRight
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.styles.focusRingShadow
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** A plain Gregorian calendar date -- the library has no `kotlinx-datetime` dependency, so this stays a minimal value type. */
data class ShadcnCalendarDate(val year: Int, val month: Int, val day: Int) : Comparable<ShadcnCalendarDate> {
    override fun compareTo(other: ShadcnCalendarDate): Int {
        if (year != other.year) return year - other.year
        if (month != other.month) return month - other.month
        return day - other.day
    }
}

/**
 * A `start`/`end` pair for [ShadcnCalendarRange], matching real shadcn/ui's `DateRange`
 * (`react-day-picker`'s range-mode value). Both null before any pick; `start` only once
 * the first day is picked; both set once a full range is picked.
 */
data class ShadcnCalendarDateRange(
    val start: ShadcnCalendarDate? = null,
    val end: ShadcnCalendarDate? = null,
)

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
    // Real shadcn's calendar (react-day-picker) tracks a roving keyboard-focus cursor
    // separately from the selected date -- the ring you see on a freshly-opened
    // calendar is that cursor, not a "selected" style (`data-selected` is a plain
    // `bg-primary` fill with no ring at all; the ring is `group-data-[focused=true]/day:
    // ring-[3px] ring-ring/50`). DayPicker seeds the cursor at the selected date (or
    // today) on mount via its own `useEffect(() => { if (modifiers.focused)
    // ref.current?.focus() })`, and moves it whenever a day is clicked. `remember(selected)`
    // re-seeds this on every external `selected` change, matching that mount effect;
    // this doesn't yet wire arrow-key roving navigation (a separate keyboard-a11y feature).
    var focusedDate by remember(selected) { mutableStateOf(selected ?: today) }

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
                        isFocused = cell.date == focusedDate,
                        onClick = {
                            onSelectedChange(cell.date)
                            focusedDate = cell.date
                        },
                    )
                }
            }
        }
    }
}

/**
 * A range-select month grid, matching real shadcn/ui's `calendar.tsx` `mode="range"` --
 * tapping picks a start day, then an end day, with the days between filled as a connected
 * band (rounded caps at the start/end, flat in between). A third tap starts a new range.
 *
 * [numberOfMonths] mirrors real Calendar's own prop of the same name -- real shadcn's
 * actual date-range-picker recipe passes `numberOfMonths={2}` so both the start and end
 * month are visible without navigating (see [io.github.ronjunevaldoz.shadcncompose.catalog.docs.dateRangePickerDoc]
 * in the catalog app); the plain single-month default (`1`) matches Calendar's own real
 * default so single-month range selection still works with no extra config. When more
 * than one month is shown, only the leftmost gets a "previous" chevron and only the
 * rightmost gets a "next" chevron -- the months in between are caption-only, matching
 * real shadcn's dual-calendar layout exactly.
 *
 * Real shadcn/Radix also live-previews the in-progress range on pointer hover before the
 * end day is picked; this only implements the tap-to-pick range itself, not that hover
 * preview -- a deliberate simplification (hover has no touch/mobile equivalent anyway,
 * and per-cell hover tracking across 42 cells isn't worth it for a preview-only effect).
 *
 * Usage:
 * ```
 * var year by remember { mutableStateOf(2026) }
 * var month by remember { mutableStateOf(3) }
 * var range by remember { mutableStateOf(ShadcnCalendarDateRange()) }
 * ShadcnCalendarRange(
 *     year = year, month = month,
 *     onMonthChange = { y, m -> year = y; month = m },
 *     range = range, onRangeChange = { range = it },
 *     numberOfMonths = 2,
 * )
 * ```
 */
@Composable
fun ShadcnCalendarRange(
    year: Int,
    month: Int,
    onMonthChange: (year: Int, month: Int) -> Unit,
    range: ShadcnCalendarDateRange,
    onRangeChange: (ShadcnCalendarDateRange) -> Unit,
    modifier: Modifier = Modifier,
    today: ShadcnCalendarDate? = null,
    numberOfMonths: Int = 1,
) {
    var focusedDate by remember(range.start, range.end) { mutableStateOf(range.end ?: range.start ?: today) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg)) {
        var monthCursor = year to month
        for (index in 0 until numberOfMonths) {
            val (monthYear, monthNum) = monthCursor
            RangeCalendarMonth(
                year = monthYear,
                month = monthNum,
                showPrevButton = index == 0,
                showNextButton = index == numberOfMonths - 1,
                onPrevClick = {
                    val (y, m) = previousMonth(year, month)
                    onMonthChange(y, m)
                },
                onNextClick = {
                    val (y, m) = nextMonth(year, month)
                    onMonthChange(y, m)
                },
                range = range,
                today = today,
                focusedDate = focusedDate,
                onDayClick = { date ->
                    focusedDate = date
                    onRangeChange(nextRange(range, date))
                },
            )
            monthCursor = nextMonth(monthYear, monthNum)
        }
    }
}

@Composable
private fun RangeCalendarMonth(
    year: Int,
    month: Int,
    showPrevButton: Boolean,
    showNextButton: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    range: ShadcnCalendarDateRange,
    today: ShadcnCalendarDate?,
    focusedDate: ShadcnCalendarDate?,
    onDayClick: (ShadcnCalendarDate) -> Unit,
) {
    Column(
        modifier = Modifier.padding(shadcnTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        RangeCalendarHeader(year, month, showPrevButton, showNextButton, onPrevClick, onNextClick)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.size(CELL_SIZE * 7, CELL_SIZE)) {
            WEEKDAY_LABELS.forEach { label ->
                Box(modifier = Modifier.size(CELL_SIZE), contentAlignment = Alignment.Center) {
                    ShadcnText(label, style = ShadcnTextStyle.LabelSmall, muted = true)
                }
            }
        }
        val weeks = remember(year, month) { buildMonthGrid(year, month) }
        weeks.forEach { week ->
            // The absolute start/end day is its own isolated, fully-rounded, solid-filled
            // badge (same treatment as a plain single-day selection) -- it is NOT part of the
            // flush muted band. Every *other* in-range day joins a flush, touching band that's
            // rounded only at each row's own local left/right edge of its contiguous run
            // (real shadcn rounds per visible week-row segment, not just the whole range's
            // absolute ends -- confirmed directly against a real screenshot, not assumed).
            val isMiddle =
                week.map { cell ->
                    range.start != null && range.end != null && cell.date > range.start && cell.date < range.end
                }
            Row {
                week.forEachIndexed { index, cell ->
                    val date = cell.date
                    val isRangeStart = range.start != null && date == range.start
                    val isRangeEnd = range.end != null && date == range.end
                    CalendarDayCell(
                        cell = cell,
                        isSelected = isRangeStart || isRangeEnd,
                        isToday = today != null && date == today,
                        isFocused = date == focusedDate,
                        isInRange = isMiddle[index],
                        isRunLeftEdge = isMiddle[index] && (index == 0 || !isMiddle[index - 1]),
                        isRunRightEdge = isMiddle[index] && (index == week.lastIndex || !isMiddle[index + 1]),
                        onClick = { onDayClick(date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeCalendarHeader(
    year: Int,
    month: Int,
    showPrevButton: Boolean,
    showNextButton: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = Modifier.size(CELL_SIZE * 7, CELL_SIZE),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPrevButton) {
            CalendarNavButton(pointingLeft = true, onClick = onPrevClick)
        } else {
            Box(modifier = Modifier.size(28.dp))
        }
        ShadcnText("${MONTH_NAMES[month - 1]} $year", style = ShadcnTextStyle.LabelLarge)
        if (showNextButton) {
            CalendarNavButton(pointingLeft = false, onClick = onNextClick)
        } else {
            Box(modifier = Modifier.size(28.dp))
        }
    }
}

private fun nextRange(
    current: ShadcnCalendarDateRange,
    clicked: ShadcnCalendarDate,
): ShadcnCalendarDateRange =
    if (current.start == null || current.end != null) {
        // No range in progress, or a full range already picked -- start a new one.
        ShadcnCalendarDateRange(start = clicked, end = null)
    } else {
        // One end already picked -- complete the range, normalizing so start <= end
        // regardless of which day the second tap lands on.
        ShadcnCalendarDateRange(start = minOf(current.start, clicked), end = maxOf(current.start, clicked))
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
        CalendarNavButton(pointingLeft = true) {
            val (y, m) = previousMonth(year, month)
            onMonthChange(y, m)
        }
        ShadcnText("${MONTH_NAMES[month - 1]} $year", style = ShadcnTextStyle.LabelLarge)
        CalendarNavButton(pointingLeft = false) {
            val (y, m) = nextMonth(year, month)
            onMonthChange(y, m)
        }
    }
}

@Composable
private fun CalendarNavButton(
    pointingLeft: Boolean,
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
        ShadcnGlyphIcon(
            ChevronRight,
            tint = shadcnTheme.colors.onSurface,
            modifier = if (pointingLeft) Modifier.rotate(180f) else Modifier,
            small = true,
        )
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell,
    isSelected: Boolean,
    isToday: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit,
    isInRange: Boolean = false,
    isRunLeftEdge: Boolean = false,
    isRunRightEdge: Boolean = false,
) {
    val theme = shadcnTheme
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = remember { MutableStyleState(interactionSource) }
    // The absolute start/end day (isSelected) is an isolated, fully-rounded, padded badge --
    // the same shape/gap as a plain single-day selection, not part of the flush band at all.
    // Every other in-range day (isInRange) joins a flush, touching muted band per week-row,
    // rounded only at that row's own local left/right edge of its contiguous run (confirmed
    // against a real screenshot: real shadcn rounds per visible row segment, not just the
    // range's absolute ends).
    val cellShape =
        when {
            isSelected -> RoundedCornerShape(theme.shapes.md)
            isInRange -> rowRunShape(theme.shapes.md, isRunLeftEdge, isRunRightEdge)
            else -> RoundedCornerShape(theme.shapes.md)
        }
    val cellStyle =
        Style {
            shape(cellShape)
            if (isSelected) {
                background(theme.colors.primary)
            } else if (isInRange) {
                background(theme.colors.muted)
            } else if (isToday) {
                borderWidth(1.dp)
                borderColor(theme.colors.border)
            }
            if (isFocused) {
                borderWidth(1.dp)
                borderColor(theme.colors.borderFocus)
                dropShadow(theme.focusRingShadow())
            }
        }
    Box(
        modifier =
            Modifier
                .size(CELL_SIZE)
                .padding(if (isInRange) 0.dp else 2.dp)
                .clip(cellShape)
                .styleable(styleState, cellStyle)
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

private fun rowRunShape(
    radius: Dp,
    isRunLeftEdge: Boolean,
    isRunRightEdge: Boolean,
): RoundedCornerShape =
    when {
        isRunLeftEdge && isRunRightEdge -> RoundedCornerShape(radius) // isolated single-day run within this row
        isRunLeftEdge -> RoundedCornerShape(topStart = radius, bottomStart = radius, topEnd = 0.dp, bottomEnd = 0.dp)
        isRunRightEdge -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
