@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class InputGroupScreenshotTest : ShadcnScreenshotTest() {
    /**
     * The trailing-addon row is a regression guard: `ShadcnTextField` hardcodes
     * `fillMaxWidth()`, which used to eat the whole group row and squeeze the trailing
     * addon into one-character-per-line vertical text before the group wrapped the
     * field slot in `weight(1f)`.
     */
    private fun states(darkTheme: Boolean) {
        snapshot("input_group_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    leading = { ShadcnInputGroupText("$") },
                ) {
                    ShadcnTextField(value = "0.00", onValueChange = {})
                }
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    trailing = { ShadcnInputGroupText(".com") },
                ) {
                    ShadcnTextField(value = "example", onValueChange = {})
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    /**
     * Focusing the inner field must ring/highlight the *group container* (real shadcn's
     * `has-[:focus-visible]:ring-[3px]` on `InputGroup`), with no inner border or ring
     * on the field itself.
     */
    private fun focused(darkTheme: Boolean) {
        snapshotFocused("input_group_focused", focusTag = "ig", darkTheme = darkTheme) {
            ShadcnInputGroup(
                modifier = Modifier.width(260.dp).testTag("ig"),
                leading = { ShadcnInputGroupText("$") },
            ) {
                ShadcnTextField(value = "0.00", onValueChange = {})
            }
        }
    }

    @Test fun focused_light() = focused(darkTheme = false)

    @Test fun focused_dark() = focused(darkTheme = true)
}
