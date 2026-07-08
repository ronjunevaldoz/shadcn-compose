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
}
