package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageScroller
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val messageScrollerDoc =
    ComponentDoc(
        id = "message-scroller",
        title = "Message Scroller",
        description =
            "A chat-transcript scroll container that follows new messages to the bottom -- but only while " +
                "the reader hasn't scrolled away -- with a floating jump-to-bottom button when they have.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnMessageScroller(modifier = Modifier.fillMaxSize()) {
                messages.forEach { message ->
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                        ShadcnText(message.text)
                    }
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        // modifier must constrain the main-axis size (here: a fixed height) --
                        // this composable fills whatever it's given.
                        ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(200.dp)) {
                            repeat(10) { index ->
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnText("Message number ${'$'}index")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(200.dp)) {
                            repeat(10) { index ->
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnText("Message number $index")
                                }
                            }
                        }
                    },
                ),
            ),
    )
