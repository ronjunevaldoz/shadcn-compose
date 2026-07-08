package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachment
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentState
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val attachmentDoc =
    ComponentDoc(
        id = "attachment",
        title = "Attachment",
        description = "A file-attachment chip for a chat composer's upload tray, with upload/processing/error states.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnAttachmentGroup {
                ShadcnAttachment(state = ShadcnAttachmentState.Done) {
                    ShadcnAttachmentMedia { ShadcnText("📄") }
                    ShadcnAttachmentContent {
                        ShadcnAttachmentTitle("report.pdf")
                        ShadcnAttachmentDescription("2.4 MB")
                    }
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "States",
                    code =
                        """
                        ShadcnAttachmentGroup {
                            ShadcnAttachment(state = ShadcnAttachmentState.Done) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("report.pdf")
                                    ShadcnAttachmentDescription("2.4 MB")
                                }
                            }
                            ShadcnAttachment(state = ShadcnAttachmentState.Uploading) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("uploading.zip", state = ShadcnAttachmentState.Uploading)
                                    ShadcnAttachmentDescription("Uploading…")
                                }
                            }
                            ShadcnAttachment(state = ShadcnAttachmentState.Error) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("failed.csv")
                                    ShadcnAttachmentDescription("Upload failed", isError = true)
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnAttachmentGroup {
                            ShadcnAttachment(state = ShadcnAttachmentState.Done) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("report.pdf")
                                    ShadcnAttachmentDescription("2.4 MB")
                                }
                            }
                            ShadcnAttachment(state = ShadcnAttachmentState.Uploading) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("uploading.zip", state = ShadcnAttachmentState.Uploading)
                                    ShadcnAttachmentDescription("Uploading…")
                                }
                            }
                            ShadcnAttachment(state = ShadcnAttachmentState.Error) {
                                ShadcnAttachmentMedia { ShadcnText("📄") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("failed.csv")
                                    ShadcnAttachmentDescription("Upload failed", isError = true)
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Vertical orientation",
                    code =
                        """
                        ShadcnAttachmentGroup {
                            ShadcnAttachment(orientation = ShadcnAttachmentOrientation.Vertical) {
                                ShadcnAttachmentMedia { ShadcnText("🖼️") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("photo.png")
                                    ShadcnAttachmentDescription("1.1 MB")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnAttachmentGroup {
                            ShadcnAttachment(orientation = ShadcnAttachmentOrientation.Vertical) {
                                ShadcnAttachmentMedia { ShadcnText("🖼️") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("photo.png")
                                    ShadcnAttachmentDescription("1.1 MB")
                                }
                            }
                        }
                    },
                ),
            ),
    )
