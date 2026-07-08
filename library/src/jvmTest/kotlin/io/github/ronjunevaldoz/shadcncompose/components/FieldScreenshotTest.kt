@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class FieldScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("field_states", darkTheme = darkTheme) {
            ShadcnFieldGroup {
                ShadcnField {
                    ShadcnFieldLabel("Email", required = true)
                    ShadcnTextField(value = "", onValueChange = {}, placeholder = "you@example.com")
                    ShadcnFieldDescription("We'll never share your email.")
                }
                ShadcnFieldSeparator(label = "OR")
                ShadcnField {
                    ShadcnFieldLabel("Username")
                    ShadcnTextField(value = "jane", onValueChange = {})
                    ShadcnFieldError("Username is already taken.")
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
