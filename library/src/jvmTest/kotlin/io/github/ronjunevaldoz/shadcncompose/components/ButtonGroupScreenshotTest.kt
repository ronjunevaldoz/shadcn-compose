@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

class ButtonGroupScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("button_group_states", darkTheme = darkTheme) {
            ShadcnButtonGroup(
                items =
                    listOf(
                        ButtonGroupItem("Copy", onClick = {}),
                        ButtonGroupItem("Share", onClick = {}),
                    ),
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun withLabel(darkTheme: Boolean) {
        snapshot("button_group_with_label", darkTheme = darkTheme) {
            // Mirrors app:shared's ButtonGroupDoc.kt "With label" example exactly.
            val rounded = shadcnTheme.shapes.lg
            ShadcnButtonGroup {
                ShadcnButtonGroupText("https://", topStart = rounded, bottomStart = rounded)
                ShadcnButtonGroupSeparator()
                CompositionLocalProvider(
                    LocalGroupCorners provides ShadcnGroupCorners(topEnd = rounded, bottomEnd = rounded),
                ) {
                    ShadcnButton(
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                        style = Style { shape(RoundedCornerShape(0.dp, rounded, rounded, 0.dp)) },
                    ) { ShadcnText("example.com") }
                }
            }
        }
    }

    @Test fun with_label_light() = withLabel(darkTheme = false)

    @Test fun with_label_dark() = withLabel(darkTheme = true)

    private fun withLabelFocused(darkTheme: Boolean) {
        snapshotFocused("button_group_with_label_focused", focusTag = "group-button", darkTheme = darkTheme) {
            val rounded = shadcnTheme.shapes.lg
            ShadcnButtonGroup {
                ShadcnButtonGroupText("https://", topStart = rounded, bottomStart = rounded)
                ShadcnButtonGroupSeparator()
                CompositionLocalProvider(
                    LocalGroupCorners provides ShadcnGroupCorners(topEnd = rounded, bottomEnd = rounded),
                ) {
                    ShadcnButton(
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                        style = Style { shape(RoundedCornerShape(0.dp, rounded, rounded, 0.dp)) },
                        modifier = Modifier.testTag("group-button"),
                    ) { ShadcnText("example.com") }
                }
            }
        }
    }

    @Test fun with_label_focused_light() = withLabelFocused(darkTheme = false)

    @Test fun with_label_focused_dark() = withLabelFocused(darkTheme = true)
}
