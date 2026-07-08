package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ItemScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("item_states", darkTheme = darkTheme) {
            ShadcnItemGroup {
                ShadcnItem(variant = ShadcnItemVariant.Outline) {
                    ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnText("📦") }
                    ShadcnItemContent {
                        ShadcnItemTitle("Order #1234")
                        ShadcnItemDescription("Shipped on March 12")
                    }
                    ShadcnItemActions { ShadcnText("Track") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
