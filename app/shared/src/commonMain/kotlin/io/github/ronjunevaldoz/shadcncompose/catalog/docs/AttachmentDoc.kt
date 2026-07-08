@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachment
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentState
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAttachmentTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSpinner
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.heroicons.outline.XMark

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
                    title = "Composer upload tray",
                    code =
                        """
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnAttachmentGroup {
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🏢") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("workspace.png")
                                        ShadcnAttachmentDescription("PNG · 820 KB")
                                    }
                                }
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🌿") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("desk-reference-photo.jpg")
                                        ShadcnAttachmentDescription("JPG · 1.1 MB")
                                    }
                                }
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🪑") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("office-reference.jpg")
                                        ShadcnAttachmentDescription("JPG · 940 KB")
                                    }
                                }
                            }
                            ShadcnAttachment(
                                state = ShadcnAttachmentState.Uploading,
                                actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                            ) {
                                ShadcnAttachmentMedia { ShadcnSpinner() }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("sales-dashboard.pdf", state = ShadcnAttachmentState.Uploading)
                                    ShadcnAttachmentDescription("Uploading · 64%")
                                }
                            }
                            ShadcnAttachment(
                                actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                            ) {
                                ShadcnAttachmentMedia { ShadcnText("</>") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("message-renderer.tsx")
                                    ShadcnAttachmentDescription("TypeScript · 12 KB")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        Column(
                            modifier = Modifier.width(320.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ShadcnAttachmentGroup {
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🏢") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("workspace.png")
                                        ShadcnAttachmentDescription("PNG · 820 KB")
                                    }
                                }
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🌿") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("desk-reference-photo.jpg")
                                        ShadcnAttachmentDescription("JPG · 1.1 MB")
                                    }
                                }
                                ShadcnAttachment(
                                    orientation = ShadcnAttachmentOrientation.Vertical,
                                    actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                                ) {
                                    ShadcnAttachmentMedia { ShadcnText("🪑") }
                                    ShadcnAttachmentContent {
                                        ShadcnAttachmentTitle("office-reference.jpg")
                                        ShadcnAttachmentDescription("JPG · 940 KB")
                                    }
                                }
                            }
                            ShadcnAttachment(
                                state = ShadcnAttachmentState.Uploading,
                                actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                            ) {
                                ShadcnAttachmentMedia { ShadcnSpinner() }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle(
                                        "sales-dashboard.pdf",
                                        state = ShadcnAttachmentState.Uploading,
                                    )
                                    ShadcnAttachmentDescription("Uploading · 64%")
                                }
                            }
                            ShadcnAttachment(
                                actions = { ShadcnAttachmentActions { AttachmentRemoveButton() } },
                            ) {
                                ShadcnAttachmentMedia { ShadcnText("</>") }
                                ShadcnAttachmentContent {
                                    ShadcnAttachmentTitle("message-renderer.tsx")
                                    ShadcnAttachmentDescription("TypeScript · 12 KB")
                                }
                            }
                        }
                    },
                ),
            ),
    )

@Composable
private fun AttachmentRemoveButton() {
    ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Xs) {
        Image(
            imageVector = XMark,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(shadcnTheme.colors.onSurface),
        )
    }
}
