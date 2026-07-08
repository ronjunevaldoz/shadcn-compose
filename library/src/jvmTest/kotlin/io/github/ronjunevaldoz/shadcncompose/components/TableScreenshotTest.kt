package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class TableScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("table_states", darkTheme = darkTheme) {
            ShadcnTable {
                ShadcnTableHeaderRow {
                    ShadcnTableHeadCell("Invoice", Modifier.weight(1f))
                    ShadcnTableHeadCell("Status")
                }
                ShadcnTableRow {
                    ShadcnTableCell("INV001", Modifier.weight(1f))
                    ShadcnTableCell("Paid", muted = true)
                }
                ShadcnTableRow(isLast = true) {
                    ShadcnTableCell("INV002", Modifier.weight(1f))
                    ShadcnTableCell("Pending", muted = true)
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
