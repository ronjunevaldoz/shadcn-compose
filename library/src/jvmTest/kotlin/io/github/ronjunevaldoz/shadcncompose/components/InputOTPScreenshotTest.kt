package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
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
}
