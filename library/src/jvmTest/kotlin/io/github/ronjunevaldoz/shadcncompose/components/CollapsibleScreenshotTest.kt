@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class CollapsibleScreenshotTest : ShadcnScreenshotTest() {
    private fun states(
        darkTheme: Boolean,
        expanded: Boolean,
    ) {
        val suffix = if (expanded) "_expanded" else "_collapsed"
        snapshot("collapsible$suffix", darkTheme = darkTheme) {
            ShadcnCollapsible(
                expanded = expanded,
                onExpandedChange = {},
                trigger = { isOpen, toggle ->
                    ShadcnButton(onClick = toggle, variant = ButtonVariant.Outline) {
                        ShadcnText(if (isOpen) "Hide details" else "Show details")
                    }
                },
            ) {
                ShadcnText("@peduarte starred 3 repositories")
            }
        }
    }

    @Test fun collapsed_light() = states(darkTheme = false, expanded = false)

    @Test fun collapsed_dark() = states(darkTheme = true, expanded = false)

    @Test fun expanded_light() = states(darkTheme = false, expanded = true)

    @Test fun expanded_dark() = states(darkTheme = true, expanded = true)
}
