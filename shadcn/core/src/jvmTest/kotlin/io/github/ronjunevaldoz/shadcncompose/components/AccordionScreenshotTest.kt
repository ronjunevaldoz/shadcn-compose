package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class AccordionScreenshotTest : ShadcnScreenshotTest() {
    private fun states(
        darkTheme: Boolean,
        expandedIds: Set<String>,
    ) {
        val suffix = if (expandedIds.isEmpty()) "_collapsed" else "_expanded"
        snapshot("accordion$suffix", darkTheme = darkTheme) {
            ShadcnAccordion(
                items =
                    listOf(
                        ShadcnAccordionItem("item-1", "Is it accessible?") {
                            ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                        },
                        ShadcnAccordionItem("item-2", "Is it styled?") {
                            ShadcnText("Yes. It comes with default styles.")
                        },
                    ),
                expandedIds = expandedIds,
                onExpandedIdsChange = {},
            )
        }
    }

    @Test fun collapsed_light() = states(darkTheme = false, expandedIds = emptySet())

    @Test fun collapsed_dark() = states(darkTheme = true, expandedIds = emptySet())

    @Test fun expanded_light() = states(darkTheme = false, expandedIds = setOf("item-1"))

    @Test fun expanded_dark() = states(darkTheme = true, expandedIds = setOf("item-1"))

    /**
     * Regression guard: the trigger's focus-ring `Style` block used to omit `shape(...)`,
     * so its `dropShadow` ring fell back to the Style API's default `RectangleShape`
     * (sharp corners) instead of following `theme.shapes.md`, the rounding every other
     * focusable component in this library rings with (matches real shadcn's
     * `AccordionTrigger` `rounded-md` class).
     */
    private fun focused(darkTheme: Boolean) {
        snapshotFocused("accordion_trigger_focused", focusTag = "accordion-item-1", darkTheme = darkTheme) {
            ShadcnAccordion(
                modifier = Modifier.testTag("accordion-item-1"),
                items =
                    listOf(
                        ShadcnAccordionItem("item-1", "Is it accessible?") {
                            ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                        },
                    ),
                expandedIds = emptySet(),
                onExpandedIdsChange = {},
            )
        }
    }

    @Test fun trigger_focused_light() = focused(darkTheme = false)

    @Test fun trigger_focused_dark() = focused(darkTheme = true)
}
