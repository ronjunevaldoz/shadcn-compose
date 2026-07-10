@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

class InputGroupScreenshotTest : ShadcnScreenshotTest() {
    /**
     * The trailing-addon row is a regression guard: `ShadcnTextField` hardcodes
     * `fillMaxWidth()`, which used to eat the whole group row and squeeze the trailing
     * addon into one-character-per-line vertical text before the group wrapped the
     * field slot in `weight(1f)`.
     */
    private fun states(darkTheme: Boolean) {
        snapshot("input_group_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    leading = { ShadcnInputGroupText("$") },
                ) {
                    ShadcnTextField(value = "0.00", onValueChange = {})
                }
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    trailing = { ShadcnInputGroupText(".com") },
                ) {
                    ShadcnTextField(value = "example", onValueChange = {})
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    // Exact reproduction of MessageScrollerDoc.kt's InteractiveChatPanel: ShadcnCard
    // 320dp x 420dp, header with title/description/reset button, footer with the real
    // ShadcnInputGroup (Ghost leading "+" button, Default trailing send button),
    // unfocused (matching a static @Preview render, no cursor visible).
    private fun chatPanelLayout(darkTheme: Boolean) {
        snapshot("chat_panel_layout", darkTheme = darkTheme) {
            ShadcnCard(
                modifier = Modifier.width(320.dp).height(420.dp),
                header = {
                    ShadcnCardHeader(
                        title = "New Chat",
                        description = "How can I help you today?",
                        action = {
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Icon) {
                                ShadcnText("↻")
                            }
                        },
                    )
                },
                footer = {
                    ShadcnInputGroup(
                        leading = {
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Icon) {
                                ShadcnText("+")
                            }
                        },
                        trailing = {
                            ShadcnButton(
                                onClick = {},
                                variant = ButtonVariant.Default,
                                size = ButtonSize.Icon,
                            ) {
                                ShadcnText("➤", color = shadcnTheme.colors.onPrimary)
                            }
                        },
                    ) {
                        ShadcnTextField(
                            value = "this is single",
                            onValueChange = {},
                            placeholder = "Message MessageScroller…",
                            variant = TextFieldVariant.Ghost,
                            singleLine = false,
                        )
                    }
                },
            ) {
                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                    ShadcnText("Hi! Ask me anything about MessageScroller.")
                }
            }
        }
    }

    @Test fun chat_panel_layout_light() = chatPanelLayout(darkTheme = false)

    @Test fun chat_panel_layout_dark() = chatPanelLayout(darkTheme = true)

    // Preview matching the user's screenshots exactly: Ghost leading "+" icon button,
    // filled circular send button, focused state -- one two-line value, one single-line
    // value, to compare the ring/border directly against the reported screenshots.
    @androidx.compose.runtime.Composable
    private fun previewComposer(
        darkTheme: Boolean,
        value: String,
    ) {
        ShadcnInputGroup(
            modifier = Modifier.width(250.dp).testTag("preview-composer"),
            leading = {
                ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Icon) {
                    ShadcnText("+")
                }
            },
            trailing = {
                ShadcnButton(
                    onClick = {},
                    variant = ButtonVariant.Default,
                    size = ButtonSize.Icon,
                    style = Style { shape(CircleShape) },
                ) {
                    ShadcnText("➤", color = shadcnTheme.colors.onPrimary)
                }
            },
        ) {
            ShadcnTextField(
                modifier = Modifier.testTag("preview-field"),
                value = value,
                onValueChange = {},
                variant = TextFieldVariant.Ghost,
                singleLine = false,
            )
        }
    }

    private fun previewTwoLineFocused(darkTheme: Boolean) {
        snapshotFocused("preview_composer_two_line_focused", focusTag = "preview-field", darkTheme = darkTheme) {
            previewComposer(darkTheme, "this is two line of words")
        }
    }

    @Test fun preview_composer_two_line_focused_light() = previewTwoLineFocused(darkTheme = false)

    private fun previewSingleLineFocused(darkTheme: Boolean) {
        snapshotFocused("preview_composer_single_line_focused", focusTag = "preview-field", darkTheme = darkTheme) {
            previewComposer(darkTheme, "this is single line")
        }
    }

    @Test fun preview_composer_single_line_focused_light() = previewSingleLineFocused(darkTheme = false)

    /**
     * Regression guard for a real bug: the leading/trailing addon gap used to come
     * entirely from the inner field's own `contentPadding`, which only holds while the
     * field's value is short enough to leave slack in its `weight(1f)` box. Once the
     * value is long enough to fill it, that inset stopped keeping the addon clear of the
     * text -- "$" or ".com" would render flush against the value with no gap at all. The
     * group now owns an explicit minimum gap of its own (`shadcnTheme.spacing.xs`) so
     * this holds regardless of how full the field is.
     */
    private fun longValue(darkTheme: Boolean) {
        snapshot("input_group_long_value", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    leading = { ShadcnInputGroupText("$") },
                ) {
                    ShadcnTextField(value = "123456789012345678901234", onValueChange = {})
                }
                ShadcnInputGroup(
                    modifier = Modifier.width(260.dp),
                    trailing = { ShadcnInputGroupText(".com") },
                ) {
                    ShadcnTextField(value = "areallylongsubdomainname", onValueChange = {})
                }
            }
        }
    }

    @Test fun long_value_light() = longValue(darkTheme = false)

    @Test fun long_value_dark() = longValue(darkTheme = true)

    /**
     * Focusing the inner field must ring/highlight the *group container* (real shadcn's
     * `has-[:focus-visible]:ring-[3px]` on `InputGroup`), with no inner border or ring
     * on the field itself.
     */
    private fun focused(darkTheme: Boolean) {
        snapshotFocused("input_group_focused", focusTag = "ig", darkTheme = darkTheme) {
            ShadcnInputGroup(
                modifier = Modifier.width(260.dp).testTag("ig"),
                leading = { ShadcnInputGroupText("$") },
            ) {
                ShadcnTextField(value = "0.00", onValueChange = {})
            }
        }
    }

    @Test fun focused_light() = focused(darkTheme = false)

    @Test fun focused_dark() = focused(darkTheme = true)

    /**
     * The chat-composer layout: [ShadcnInputGroup.bottomBar] (real shadcn's
     * `InputGroupAddon align="block-end"`) puts a full-width button toolbar below a
     * growing multi-line field instead of an inline leading/trailing addon beside it.
     */
    private fun bottomBar(darkTheme: Boolean) {
        snapshot("input_group_bottom_bar", darkTheme = darkTheme) {
            ShadcnInputGroup(
                modifier = Modifier.width(280.dp),
                bottomBar = {
                    ShadcnButton(
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                        style = Style { shape(CircleShape) },
                    ) {
                        ShadcnText("+")
                    }
                    Spacer(Modifier.weight(1f))
                    ShadcnButton(
                        onClick = {},
                        size = ButtonSize.Icon,
                        style = Style { shape(CircleShape) },
                    ) {
                        ShadcnText("↑", color = shadcnTheme.colors.onPrimary)
                    }
                },
            ) {
                ShadcnTextField(
                    value = "I'm building a chat for our app and the scroll behavior is driving me nuts.",
                    onValueChange = {},
                    singleLine = false,
                )
            }
        }
    }

    @Test fun bottom_bar_light() = bottomBar(darkTheme = false)

    @Test fun bottom_bar_dark() = bottomBar(darkTheme = true)
}
