package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class CalendarScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("calendar_states", darkTheme = darkTheme) {
            ShadcnCalendar(
                year = 2026,
                month = 3,
                onMonthChange = { _, _ -> },
                selected = ShadcnCalendarDate(2026, 3, 10),
                onSelectedChange = {},
                today = ShadcnCalendarDate(2026, 3, 15),
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun rangeStates(darkTheme: Boolean) {
        snapshot("calendar_range_states", darkTheme = darkTheme) {
            ShadcnCalendarRange(
                year = 2026,
                month = 3,
                onMonthChange = { _, _ -> },
                range =
                    ShadcnCalendarDateRange(
                        start = ShadcnCalendarDate(2026, 3, 10),
                        end = ShadcnCalendarDate(2026, 3, 15),
                    ),
                onRangeChange = {},
                today = ShadcnCalendarDate(2026, 3, 15),
            )
        }
    }

    @Test fun range_states_light() = rangeStates(darkTheme = false)

    @Test fun range_states_dark() = rangeStates(darkTheme = true)

    private fun rangeDualMonthStates(darkTheme: Boolean) {
        snapshot("calendar_range_dual_month_states", darkTheme = darkTheme) {
            ShadcnCalendarRange(
                year = 2026,
                month = 3,
                onMonthChange = { _, _ -> },
                range =
                    ShadcnCalendarDateRange(
                        start = ShadcnCalendarDate(2026, 3, 25),
                        end = ShadcnCalendarDate(2026, 4, 5),
                    ),
                onRangeChange = {},
                today = ShadcnCalendarDate(2026, 3, 15),
                numberOfMonths = 2,
            )
        }
    }

    @Test fun range_dual_month_states_light() = rangeDualMonthStates(darkTheme = false)

    @Test fun range_dual_month_states_dark() = rangeDualMonthStates(darkTheme = true)

    private fun disabledStates(darkTheme: Boolean) {
        snapshot("calendar_disabled_states", darkTheme = darkTheme) {
            ShadcnCalendar(
                year = 2026,
                month = 3,
                onMonthChange = { _, _ -> },
                selected = ShadcnCalendarDate(2026, 3, 18),
                onSelectedChange = {},
                today = ShadcnCalendarDate(2026, 3, 18),
                // Saturdays in this fixed month, matching the reference demo's every-Saturday pattern.
                disabled = { it.day in setOf(7, 14, 21, 28) },
            )
        }
    }

    @Test fun disabled_states_light() = disabledStates(darkTheme = false)

    @Test fun disabled_states_dark() = disabledStates(darkTheme = true)
}
