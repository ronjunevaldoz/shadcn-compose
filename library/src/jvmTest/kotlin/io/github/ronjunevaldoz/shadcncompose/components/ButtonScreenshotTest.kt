@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
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
}
