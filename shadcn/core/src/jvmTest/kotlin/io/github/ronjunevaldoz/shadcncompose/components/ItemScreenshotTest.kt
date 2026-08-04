package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ItemScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("item_states", darkTheme = darkTheme) {
            ShadcnItemGroup {
                ShadcnItem(variant = ShadcnItemVariant.Outline) {
                    ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnEmojiText("📦") }
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

    /**
     * A clickable row (`onClick` non-null) picking up a real keyboard focus ring --
     * mirrors [ButtonScreenshotTest.focused_light]'s `snapshotFocused` pattern. No
     * hover-simulated golden here: `performMouseInput { moveTo(...) }` is documented
     * (see [TooltipScreenshotTest], [ContextMenuScreenshotTest]) to hang the JVM test
     * worker in this suite, so hover is verified live/visually instead, same as those.
     */
    @Test
    fun focused_light() {
        snapshotFocused("item_focused", focusTag = "item", darkTheme = false) {
            ShadcnItem(variant = ShadcnItemVariant.Outline, onClick = {}, modifier = Modifier.testTag("item")) {
                ShadcnItemContent {
                    ShadcnItemTitle("Jane Doe")
                    ShadcnItemDescription("jane@example.com")
                }
            }
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("item_focused", focusTag = "item", darkTheme = true) {
            ShadcnItem(variant = ShadcnItemVariant.Outline, onClick = {}, modifier = Modifier.testTag("item")) {
                ShadcnItemContent {
                    ShadcnItemTitle("Jane Doe")
                    ShadcnItemDescription("jane@example.com")
                }
            }
        }
    }
}
