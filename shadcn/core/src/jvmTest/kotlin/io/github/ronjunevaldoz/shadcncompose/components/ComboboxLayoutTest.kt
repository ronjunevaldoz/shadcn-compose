@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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

class ComboboxLayoutTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun fillMaxWidthFillsAndTheDefaultStays200dp() {
        rule.setContent {
            ShadcnTheme {
                Box(Modifier.width(360.dp)) {
                    ShadcnCombobox(value = "A", options = listOf("A"), onValueChange = {
                    }, modifier = Modifier.fillMaxWidth().testTag("full"))
                }
                Box(Modifier.width(360.dp)) {
                    ShadcnCombobox(
                        value = "A",
                        options = listOf("A"),
                        onValueChange = {},
                        modifier = Modifier.testTag("default"),
                    )
                }
            }
        }
        rule.onNodeWithTag("full").getBoundsInRoot().let { assertEquals(360.dp, it.right - it.left) }
        rule.onNodeWithTag("default").getBoundsInRoot().let { assertEquals(200.dp, it.right - it.left) }
    }
}
