package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/**
 * The auto-scroll/button-visibility decisions ([shouldAutoScrollToBottom],
 * [isMessageScrollerNearBottom]) are unit tested directly in
 * `ShadcnMessageScrollerTest`, per this project's rule against driving decision math
 * through a live gesture in a Compose UI Test. These screenshots instead cover fixed,
 * settled render states -- including the real (non-obvious) behavior that a freshly
 * composed scroller with overflowing content auto-scrolls to the bottom before the
 * first frame is captured, so no floating button appears on initial load.
 */
class MessageScrollerScreenshotTest : ShadcnScreenshotTest() {
    private fun shortContent(darkTheme: Boolean) {
        snapshot("message_scroller_short_content", darkTheme = darkTheme) {
            ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(160.dp)) {
                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                    ShadcnText("Hi there!")
                }
            }
        }
    }

    private fun overflowingContentSettlesAtBottom(darkTheme: Boolean) {
        snapshot("message_scroller_overflow_settles_at_bottom", darkTheme = darkTheme) {
            ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(160.dp)) {
                repeat(10) { index ->
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                        ShadcnText("Message number $index")
                    }
                }
            }
        }
    }

    private fun jumpToBottomButton(darkTheme: Boolean) {
        snapshot("message_scroller_button_visible", darkTheme = darkTheme) {
            ShadcnMessageScrollerButton(visible = true, onClick = {})
        }
    }

    @Test fun short_content_light() = shortContent(darkTheme = false)

    @Test fun short_content_dark() = shortContent(darkTheme = true)

    @Test fun overflowing_content_settles_at_bottom_light() = overflowingContentSettlesAtBottom(darkTheme = false)

    @Test fun overflowing_content_settles_at_bottom_dark() = overflowingContentSettlesAtBottom(darkTheme = true)

    @Test fun jump_to_bottom_button_light() = jumpToBottomButton(darkTheme = false)
}
