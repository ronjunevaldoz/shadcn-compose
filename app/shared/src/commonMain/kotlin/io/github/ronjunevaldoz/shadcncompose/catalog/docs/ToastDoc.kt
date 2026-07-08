@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToastVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToaster
import io.github.ronjunevaldoz.shadcncompose.components.rememberShadcnToastState

val toastDoc =
    ComponentDoc(
        id = "toast",
        title = "Toast",
        description =
            "A stackable, auto-dismissing notification queue. Real shadcn/ui's sonner.tsx wraps the separate " +
                "`sonner` npm library; since there's no Compose Multiplatform equivalent, ShadcnToastState + " +
                "ShadcnToaster reimplement that queue directly.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            val toastState = rememberShadcnToastState()
            ShadcnButton(onClick = { toastState.show("Saved", variant = ShadcnToastVariant.Success) }) {
                ShadcnText("Save")
            }
            ShadcnToaster(state = toastState)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        val toastState = rememberShadcnToastState()
                        ShadcnButton(
                            onClick = {
                                toastState.show(
                                    title = "Event created",
                                    description = "Monday, March 12 at 9:00 AM",
                                    variant = ShadcnToastVariant.Success,
                                )
                            },
                        ) { ShadcnText("Show toast") }
                        ShadcnToaster(state = toastState)
                        """.trimIndent(),
                    preview = {
                        val toastState = rememberShadcnToastState()
                        Column {
                            ShadcnButton(
                                onClick = {
                                    toastState.show(
                                        title = "Event created",
                                        description = "Monday, March 12 at 9:00 AM",
                                        variant = ShadcnToastVariant.Success,
                                    )
                                },
                            ) { ShadcnText("Show toast") }
                            ShadcnToaster(state = toastState)
                        }
                    },
                ),
            ),
    )
