@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectLayoutTest {
    @get:Rule val rule = createComposeRule()

    // issue #4: in a centred Row next to a button or input, the Select has the same height and centre
    @Test
    fun selectLinesUpWithAButtonAndAnInputInARow() {
        rule.setContent {
            ShadcnTheme {
                Row(Modifier.height(80.dp).testTag("row"), verticalAlignment = Alignment.CenterVertically) {
                    ShadcnButton(onClick = {}, modifier = Modifier.testTag("button")) { ShadcnText("Go") }
                    ShadcnSelect(
                        value = "A",
                        options = listOf("A", "B"),
                        onValueChange = {},
                        modifier = Modifier.testTag("select"),
                    )
                    ShadcnTextField(value = "x", onValueChange = {}, modifier = Modifier.width(120.dp).testTag("input"))
                }
            }
        }
        val button = rule.onNodeWithTag("button").getBoundsInRoot()
        val select = rule.onNodeWithTag("select").getBoundsInRoot()
        val input = rule.onNodeWithTag("input").getBoundsInRoot()
        assertEquals(button.bottom - button.top, select.bottom - select.top, "select height = button height")
        assertEquals(input.bottom - input.top, select.bottom - select.top, "select height = input height")
        assertEquals((button.top + button.bottom) / 2, (select.top + select.bottom) / 2, "same centre")
    }
}
