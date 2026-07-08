@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubble
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAlign
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageScroller
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One row in the interactive demo's transcript -- [text] grows in place while [isUser] is false and streaming. */
private data class ScrollerDemoMessage(
    val id: Int,
    val text: String,
    val isUser: Boolean,
)

// Cycled per reply so repeated sends in the same demo session don't all say the same thing.
private val demoReplies =
    listOf(
        "Watch this reply stream in character by character -- MessageScroller keeps you pinned to the " +
            "bottom the whole time, unless you scroll away to read something above.",
        "That's the auto-scroll in action. Try scrolling up mid-stream and it'll release -- it won't jump " +
            "back down until you press the button or send another message.",
        "A streaming reply is really just content height growing one character at a time. Nothing special " +
            "about it from MessageScroller's side -- same auto-scroll logic either way.",
    )

private const val STREAM_TICK_MILLIS = 18L

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
                ComponentExample(
                    title = "Chat panel with streaming replies",
                    code =
                        """
                        // Real shadcn's own message-scroller demo streams AI replies in with a
                        // useChat()-driven typewriter effect -- growing message content is exactly
                        // what exercises auto-scroll for real, not just a fixed transcript. This
                        // reproduces that with a plain coroutine appending one character at a time.
                        var messages by remember { mutableStateOf(listOf(ScrollerDemoMessage(0, "Hi! Ask me anything.", false))) }
                        var nextId by remember { mutableIntStateOf(1) }
                        var composerText by remember { mutableStateOf("") }
                        var isStreaming by remember { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()

                        fun send() {
                            val prompt = composerText.ifBlank { return }
                            composerText = ""
                            messages = messages + ScrollerDemoMessage(nextId++, prompt, isUser = true)
                            val replyId = nextId++
                            val reply = demoReplies[replyId % demoReplies.size]
                            messages = messages + ScrollerDemoMessage(replyId, "", isUser = false)
                            isStreaming = true
                            coroutineScope.launch {
                                for (charCount in 1..reply.length) {
                                    delay(18)
                                    messages = messages.map {
                                        if (it.id == replyId) it.copy(text = reply.take(charCount)) else it
                                    }
                                }
                                isStreaming = false
                            }
                        }

                        ShadcnCard(
                            modifier = Modifier.width(320.dp).height(420.dp),
                            header = { ShadcnCardHeader(title = "New Chat", description = "How can I help you today?") },
                            footer = {
                                Row {
                                    ShadcnTextField(
                                        value = composerText,
                                        onValueChange = { composerText = it },
                                        placeholder = "Message MessageScroller…",
                                        variant = TextFieldVariant.Ghost,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadcnButton(onClick = ::send, enabled = composerText.isNotBlank() && !isStreaming) {
                                        ShadcnText("↑")
                                    }
                                }
                            },
                        ) {
                            ShadcnMessageScroller(modifier = Modifier.weight(1f)) {
                                messages.forEach { message ->
                                    ShadcnMessage(
                                        align = if (message.isUser) ShadcnMessageAlign.End else ShadcnMessageAlign.Start,
                                        avatar = { ShadcnMessageAvatar { ShadcnText(if (message.isUser) "Me" else "AI") } },
                                    ) {
                                        ShadcnBubble(align = if (message.isUser) ShadcnMessageAlign.End else ShadcnMessageAlign.Start) {
                                            ShadcnBubbleContent(
                                                variant = if (message.isUser) ShadcnBubbleVariant.Default else ShadcnBubbleVariant.Muted,
                                            ) { ShadcnText(message.text) }
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = { InteractiveChatPanel() },
                ),
            ),
    )

@Composable
private fun InteractiveChatPanel() {
    var messages by
        remember {
            mutableStateOf(
                listOf(ScrollerDemoMessage(0, "Hi! Ask me anything about MessageScroller.", isUser = false)),
            )
        }
    var nextId by remember { mutableIntStateOf(1) }
    var composerText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun send() {
        val prompt = composerText.ifBlank { return }
        composerText = ""
        messages = messages + ScrollerDemoMessage(nextId++, prompt, isUser = true)
        val replyId = nextId++
        val reply = demoReplies[replyId % demoReplies.size]
        messages = messages + ScrollerDemoMessage(replyId, "", isUser = false)
        isStreaming = true
        coroutineScope.launch {
            for (charCount in 1..reply.length) {
                delay(STREAM_TICK_MILLIS)
                messages = messages.map { if (it.id == replyId) it.copy(text = reply.take(charCount)) else it }
            }
            isStreaming = false
        }
    }

    fun reset() {
        messages = listOf(ScrollerDemoMessage(0, "Hi! Ask me anything about MessageScroller.", isUser = false))
        nextId = 1
        composerText = ""
        isStreaming = false
    }

    ShadcnCard(
        modifier = Modifier.width(320.dp).height(420.dp),
        header = {
            ShadcnCardHeader(
                title = "New Chat",
                description = "How can I help you today?",
                action = {
                    ShadcnButton(onClick = ::reset, variant = ButtonVariant.Outline, size = ButtonSize.Icon) {
                        ShadcnText("↻")
                    }
                },
            )
        },
        footer = {
            Row(
                modifier =
                    Modifier
                        .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.full))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnTextField(
                    value = composerText,
                    onValueChange = { composerText = it },
                    placeholder = "Message MessageScroller…",
                    variant = TextFieldVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
                ShadcnButton(
                    onClick = ::send,
                    variant = ButtonVariant.Default,
                    size = ButtonSize.Icon,
                    enabled = composerText.isNotBlank() && !isStreaming,
                ) {
                    ShadcnText("↑")
                }
            }
        },
    ) {
        ShadcnMessageScroller(modifier = Modifier.weight(1f)) {
            messages.forEach { message ->
                ShadcnMessage(
                    align = if (message.isUser) ShadcnMessageAlign.End else ShadcnMessageAlign.Start,
                    avatar = {
                        ShadcnMessageAvatar { ShadcnText(if (message.isUser) "Me" else "AI") }
                    },
                ) {
                    ShadcnBubble(align = if (message.isUser) ShadcnMessageAlign.End else ShadcnMessageAlign.Start) {
                        ShadcnBubbleContent(
                            variant = if (message.isUser) ShadcnBubbleVariant.Default else ShadcnBubbleVariant.Muted,
                        ) {
                            ShadcnText(message.text)
                        }
                    }
                }
            }
        }
    }
}
