@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.icons.Check
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class ButtonScreenshotTest : ShadcnScreenshotTest() {
    private fun allVariants(
        darkTheme: Boolean,
        enabled: Boolean = true,
    ) {
        val suffix = if (enabled) "" else "_disabled"
        snapshot("button_variants$suffix", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    ButtonVariant.Default,
                    ButtonVariant.Secondary,
                    ButtonVariant.Outline,
                    ButtonVariant.Ghost,
                    ButtonVariant.Destructive,
                    ButtonVariant.Link,
                ).forEach { variant ->
                    ShadcnButton(onClick = {}, variant = variant, enabled = enabled) {
                        ShadcnText(variant::class.simpleName ?: "?")
                    }
                }
            }
        }
    }

    @Test fun variants_light() = allVariants(darkTheme = false)

    @Test fun variants_dark() = allVariants(darkTheme = true)

    @Test fun variants_disabled_light() = allVariants(darkTheme = false, enabled = false)

    @Test fun variants_disabled_dark() = allVariants(darkTheme = true, enabled = false)

    /**
     * [ShadcnIcon] with no explicit `tint` -- confirms it picks up each variant's ambient
     * [io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnContentColor] (provided by
     * [ShadcnButton]) instead of defaulting to a fixed color regardless of variant/theme.
     */
    private fun iconVariants(darkTheme: Boolean) {
        snapshot("button_icon_variants", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    ButtonVariant.Default,
                    ButtonVariant.Secondary,
                    ButtonVariant.Outline,
                    ButtonVariant.Ghost,
                    ButtonVariant.Destructive,
                ).forEach { variant ->
                    ShadcnButton(onClick = {}, variant = variant) {
                        ShadcnIcon(Check)
                        ShadcnText(variant::class.simpleName ?: "?")
                    }
                }
            }
        }
    }

    @Test fun icon_variants_light() = iconVariants(darkTheme = false)

    @Test fun icon_variants_dark() = iconVariants(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("button_focused", focusTag = "btn", darkTheme = false) {
            ShadcnButton(onClick = {}, modifier = Modifier.testTag("btn")) { ShadcnText("Focus me") }
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("button_focused", focusTag = "btn", darkTheme = true) {
            ShadcnButton(onClick = {}, modifier = Modifier.testTag("btn")) { ShadcnText("Focus me") }
        }
    }

    /**
     * [io.github.ronjunevaldoz.shadcncompose.styles.pressedMoveDown] nudges the button
     * down while held down -- driven by a real `down()` (held, no matching `up()`) so the
     * `pressed { }` state predicate actually fires, not a forced visual stand-in. Pairs
     * with [unpressed_baseline_light], identical content with no touch, so the two goldens
     * are directly diffable pixel-for-pixel to confirm the downward shift (not just
     * eyeballed).
     */
    @Test
    fun pressed_light() {
        setThemedContent(darkTheme = false) {
            ShadcnButton(onClick = {}, modifier = Modifier.testTag("btn")) { ShadcnText("Push me") }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("btn").performTouchInput { down(center) }
        composeRule.waitForIdle()
        captureNamed("button_pressed", darkTheme = false)
    }

    @Test
    fun unpressed_baseline_light() {
        snapshot("button_unpressed_baseline", darkTheme = false) {
            ShadcnButton(onClick = {}, modifier = Modifier.testTag("btn")) { ShadcnText("Push me") }
        }
    }
}
