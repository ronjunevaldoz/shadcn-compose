@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmojiText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmpty
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

val emptyDoc =
    ComponentDoc(
        id = "empty",
        title = "Empty",
        description = "A centered placeholder for an empty list, search result, or state.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnEmpty {
                ShadcnEmptyHeader {
                    ShadcnEmptyMedia { ShadcnEmojiText("📭") }
                    ShadcnEmptyTitle("No results")
                    ShadcnEmptyDescription("Try a different search term.")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnEmpty {
                            ShadcnEmptyHeader {
                                ShadcnEmptyMedia { ShadcnEmojiText("📭") }
                                ShadcnEmptyTitle("No results found")
                                ShadcnEmptyDescription("Try adjusting your search or filters.")
                            }
                            ShadcnEmptyContent {
                                ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Clear filters") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnEmpty {
                            ShadcnEmptyHeader {
                                ShadcnEmptyMedia { ShadcnEmojiText("📭") }
                                ShadcnEmptyTitle("No results found")
                                ShadcnEmptyDescription("Try adjusting your search or filters.")
                            }
                            ShadcnEmptyContent {
                                ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) {
                                    ShadcnText("Clear filters")
                                }
                            }
                        }
                    },
                ),
            ),
    )
