package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class InputOTPScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("input_otp_states", darkTheme = darkTheme) {
            ShadcnInputOTP(value = "12", onValueChange = {}, length = 6)
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
