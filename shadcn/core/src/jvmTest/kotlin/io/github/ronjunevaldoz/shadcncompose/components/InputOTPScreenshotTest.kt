package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasRequestFocusAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

class InputOTPScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("input_otp_states", darkTheme = darkTheme) {
            ShadcnInputOTP(value = "12", onValueChange = {}, length = 6)
        }
    }

    private fun verifyEmail(darkTheme: Boolean) {
        snapshot("input_otp_verify_email", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                ShadcnFieldLabel("One-time password")
                ShadcnInputOTP(value = "12", onValueChange = {}, length = 6)
                ShadcnFieldDescription("Please enter the one-time password sent to your phone.")
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun verify_email_light() = verifyEmail(darkTheme = false)

    @Test fun verify_email_dark() = verifyEmail(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("input_otp_focused", focusTag = "otp", darkTheme = false) {
            ShadcnInputOTP(value = "12", onValueChange = {}, length = 6, modifier = Modifier.testTag("otp"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("input_otp_focused", focusTag = "otp", darkTheme = true) {
            ShadcnInputOTP(value = "12", onValueChange = {}, length = 6, modifier = Modifier.testTag("otp"))
        }
    }

    /**
     * [io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRing.enabled] = false must NOT
     * silence the active slot's ring here -- a PIN/OTP entry is a text-entry component
     * (see `OtpSlot`'s doc comment), so it stays exempt from the toggle just like
     * `ShadcnTextField` and `ShadcnInputGroup`.
     */
    @Test
    fun focused_with_ring_disabled_light() {
        val focusTag = "otp-ring-disabled"
        composeRule.setContent {
            ShadcnTheme(ring = ShadcnStylePreset.Vega.ring.copy(enabled = false)) {
                Box(modifier = Modifier.background(shadcnTheme.colors.background).padding(24.dp)) {
                    ShadcnInputOTP(value = "12", onValueChange = {}, length = 6, modifier = Modifier.testTag(focusTag))
                }
            }
        }
        val focusable = hasRequestFocusAction() and (hasTestTag(focusTag) or hasAnyAncestor(hasTestTag(focusTag)))
        composeRule.onNode(focusable, useUnmergedTree = true).requestFocus()
        composeRule.waitForIdle()
        captureNamed("input_otp_focused_with_ring_disabled", darkTheme = false)
    }
}
