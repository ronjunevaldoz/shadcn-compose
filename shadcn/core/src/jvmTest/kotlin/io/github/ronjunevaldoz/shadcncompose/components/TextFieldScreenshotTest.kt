@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasRequestFocusAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

class TextFieldScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("textfield_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(TextFieldVariant.Default, TextFieldVariant.Filled, TextFieldVariant.Ghost).forEach { variant ->
                    ShadcnTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = variant::class.simpleName,
                        variant = variant,
                        modifier = Modifier.width(200.dp),
                    )
                }
                ShadcnTextField(
                    value = "Invalid value",
                    onValueChange = {},
                    isError = true,
                    supportingText = "This field is required",
                    modifier = Modifier.width(200.dp),
                )
                ShadcnTextField(
                    value = "Disabled",
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.width(200.dp),
                )
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("textfield_focused", focusTag = "tf", darkTheme = false) {
            ShadcnTextField(value = "", onValueChange = {}, modifier = Modifier.width(200.dp).testTag("tf"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("textfield_focused", focusTag = "tf", darkTheme = true) {
            ShadcnTextField(value = "", onValueChange = {}, modifier = Modifier.width(200.dp).testTag("tf"))
        }
    }

    /**
     * [io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRing.enabled] = false must NOT
     * silence the ring here -- unlike every other focusable component (which calls
     * `focusRing`), `TextFieldStyles.kt` calls `focusRingAlways`, since the ring is the
     * primary visual cue that a field is the one currently receiving keystrokes.
     */
    @Test
    fun focused_with_ring_disabled_light() {
        composeRule.setContent {
            ShadcnTheme(ring = ShadcnStylePreset.Vega.ring.copy(enabled = false)) {
                Box(modifier = Modifier.background(shadcnTheme.colors.background).padding(24.dp)) {
                    ShadcnTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.width(200.dp).testTag("tf-ring-disabled"),
                    )
                }
            }
        }
        val focusTag = "tf-ring-disabled"
        val focusable = hasRequestFocusAction() and (hasTestTag(focusTag) or hasAnyAncestor(hasTestTag(focusTag)))
        composeRule.onNode(focusable, useUnmergedTree = true).requestFocus()
        composeRule.waitForIdle()
        captureNamed("textfield_focused_with_ring_disabled", darkTheme = false)
    }
}
