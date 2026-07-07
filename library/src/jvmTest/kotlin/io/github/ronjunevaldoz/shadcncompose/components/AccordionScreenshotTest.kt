package io.github.ronjunevaldoz.shadcncompose.components

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
}
