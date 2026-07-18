@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

/**
 * Not a standalone registry component in real shadcn/ui either -- the catalog's own
 * `DatePickerDoc` documents it as a Popover+Calendar composition, so this captures that same
 * recipe's rendered result rather than a dedicated `ShadcnDatePicker` composable (there isn't
 * one). No calendar icon on the button here (unlike the catalog doc's version) -- `:shadcn:core`
 * takes zero icon-library dependencies, and heroicons-outline is `app:shared`-only.
 */
class DatePickerScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("date_picker_states", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) {
                    ShadcnText("Pick a date", muted = true)
                }
                ShadcnPopover(expanded = true, onDismissRequest = {}) {
                    ShadcnCalendar(
                        year = 2026,
                        month = 1,
                        onMonthChange = { _, _ -> },
                        selected = ShadcnCalendarDate(2026, 1, 15),
                        onSelectedChange = {},
                    )
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
