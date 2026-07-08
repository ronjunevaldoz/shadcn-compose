@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    private fun profileForm(darkTheme: Boolean) {
        snapshot("field_profile_form", darkTheme = darkTheme) {
            ShadcnFieldGroup(modifier = Modifier.width(320.dp)) {
                ShadcnField {
                    ShadcnFieldLabel("Name", required = true)
                    ShadcnTextField(value = "", onValueChange = {}, placeholder = "Your name")
                }
                ShadcnField {
                    ShadcnFieldLabel("Email", required = true)
                    ShadcnTextField(value = "", onValueChange = {}, placeholder = "you@example.com")
                    ShadcnFieldDescription("We'll never share your email.")
                }
                ShadcnField {
                    ShadcnFieldLabel("Bio")
                    ShadcnTextarea(value = "", onValueChange = {}, placeholder = "Tell us about yourself")
                    ShadcnFieldDescription("Shown on your public profile.")
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun profile_form_light() = profileForm(darkTheme = false)

    @Test fun profile_form_dark() = profileForm(darkTheme = true)
}
