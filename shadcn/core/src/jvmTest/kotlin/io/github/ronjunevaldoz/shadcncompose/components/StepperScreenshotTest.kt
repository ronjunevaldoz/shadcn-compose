package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class StepperScreenshotTest : ShadcnScreenshotTest() {
    private val steps =
        listOf(
            ShadcnStepperStep("Details", "Enter the required details"),
            ShadcnStepperStep("Review", "Confirm your information"),
            ShadcnStepperStep("Done", "All set"),
        )

    /** currentStep = 1 shows all three states at once: completed, active, upcoming. */
    private fun midProgress(darkTheme: Boolean) {
        snapshot("stepper_mid_progress", darkTheme = darkTheme) {
            ShadcnStepper(steps = steps, currentStep = 1, modifier = Modifier.width(320.dp))
        }
    }

    private fun compact(darkTheme: Boolean) {
        snapshot("stepper_compact", darkTheme = darkTheme) {
            ShadcnStepper(steps = steps, currentStep = 1, showLabels = false, modifier = Modifier.width(320.dp))
        }
    }

    private fun vertical(darkTheme: Boolean) {
        snapshot("stepper_vertical", darkTheme = darkTheme) {
            ShadcnStepper(
                steps = steps,
                currentStep = 1,
                orientation = ShadcnStepperOrientation.Vertical,
                modifier = Modifier.width(320.dp),
            )
        }
    }

    @Test fun mid_progress_light() = midProgress(darkTheme = false)

    @Test fun mid_progress_dark() = midProgress(darkTheme = true)

    @Test fun compact_light() = compact(darkTheme = false)

    @Test fun compact_dark() = compact(darkTheme = true)

    @Test fun vertical_light() = vertical(darkTheme = false)

    @Test fun vertical_dark() = vertical(darkTheme = true)
}
