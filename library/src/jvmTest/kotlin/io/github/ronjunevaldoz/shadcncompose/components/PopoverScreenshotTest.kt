@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class PopoverScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("popover_states", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}) { ShadcnText("Open popover") }
                ShadcnPopover(expanded = true, onDismissRequest = {}) {
                    ShadcnText("Place content for the popover here.")
                }
            }
        }
    }

    private fun dimensionsForm(darkTheme: Boolean) {
        snapshot("popover_dimensions_form", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}) { ShadcnText("Open popover") }
                ShadcnPopover(expanded = true, onDismissRequest = {}) {
                    Column(modifier = Modifier.width(240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShadcnText("Dimensions", style = ShadcnTextStyle.TitleSmall)
                        ShadcnText(
                            "Set the dimensions for the layer.",
                            style = ShadcnTextStyle.BodySmall,
                            muted = true,
                        )
                        ShadcnFieldGroup {
                            ShadcnField {
                                ShadcnFieldLabel("Width")
                                ShadcnTextField(value = "100%", onValueChange = {})
                            }
                            ShadcnField {
                                ShadcnFieldLabel("Height")
                                ShadcnTextField(value = "25px", onValueChange = {})
                            }
                        }
                    }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun dimensions_form_light() = dimensionsForm(darkTheme = false)

    @Test fun dimensions_form_dark() = dimensionsForm(darkTheme = true)
}
