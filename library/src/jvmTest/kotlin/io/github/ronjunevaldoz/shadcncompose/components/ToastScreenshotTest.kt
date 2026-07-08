package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.runtime.remember
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ToastScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("toast_states", darkTheme = darkTheme) {
            val state =
                remember {
                    ShadcnToastState().apply {
                        show(
                            title = "Event created",
                            description = "Monday, March 12 at 9:00 AM",
                            variant = ShadcnToastVariant.Success,
                        )
                    }
                }
            ShadcnToaster(state = state)
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
