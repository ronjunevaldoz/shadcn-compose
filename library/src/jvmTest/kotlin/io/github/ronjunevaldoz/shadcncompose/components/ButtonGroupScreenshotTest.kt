@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class ButtonGroupScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("button_group_states", darkTheme = darkTheme) {
            ShadcnButtonGroup(
                items =
                    listOf(
                        ButtonGroupItem("Copy", onClick = {}),
                        ButtonGroupItem("Share", onClick = {}),
                    ),
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun withLabel(darkTheme: Boolean) {
        snapshot("button_group_with_label", darkTheme = darkTheme) {
            ShadcnButtonGroup {
                ShadcnButtonGroupText("https://")
                ShadcnButtonGroupSeparator()
                ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("example.com") }
            }
        }
    }

    @Test fun with_label_light() = withLabel(darkTheme = false)

    @Test fun with_label_dark() = withLabel(darkTheme = true)
}
