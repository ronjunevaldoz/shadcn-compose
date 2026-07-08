@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class AttachmentScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("attachment_states", darkTheme = darkTheme) {
            ShadcnAttachmentGroup(modifier = Modifier.height(56.dp)) {
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
        }
    }

    private fun vertical(darkTheme: Boolean) {
        snapshot("attachment_vertical", darkTheme = darkTheme) {
            ShadcnAttachmentGroup {
                ShadcnAttachment(orientation = ShadcnAttachmentOrientation.Vertical) {
                    ShadcnAttachmentMedia { ShadcnText("🖼️") }
                    ShadcnAttachmentContent {
                        ShadcnAttachmentTitle("photo.png")
                        ShadcnAttachmentDescription("1.1 MB")
                    }
                }
            }
        }
    }

    private fun composerUploadTray(darkTheme: Boolean) {
        snapshot("attachment_composer_upload_tray", darkTheme = darkTheme) {
            Column(
                modifier = Modifier.width(320.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShadcnAttachmentGroup {
                    ShadcnAttachment(
                        orientation = ShadcnAttachmentOrientation.Vertical,
                        actions = { ShadcnAttachmentActions { RemoveButton() } },
                    ) {
                        ShadcnAttachmentMedia { ShadcnText("🏢") }
                        ShadcnAttachmentContent {
                            ShadcnAttachmentTitle("workspace.png")
                            ShadcnAttachmentDescription("PNG · 820 KB")
                        }
                    }
                    ShadcnAttachment(
                        orientation = ShadcnAttachmentOrientation.Vertical,
                        actions = { ShadcnAttachmentActions { RemoveButton() } },
                    ) {
                        ShadcnAttachmentMedia { ShadcnText("🌿") }
                        ShadcnAttachmentContent {
                            ShadcnAttachmentTitle("desk-reference-photo.jpg")
                            ShadcnAttachmentDescription("JPG · 1.1 MB")
                        }
                    }
                    ShadcnAttachment(
                        orientation = ShadcnAttachmentOrientation.Vertical,
                        actions = { ShadcnAttachmentActions { RemoveButton() } },
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
                    actions = { ShadcnAttachmentActions { RemoveButton() } },
                ) {
                    ShadcnAttachmentMedia { ShadcnSpinner() }
                    ShadcnAttachmentContent {
                        ShadcnAttachmentTitle("sales-dashboard.pdf", state = ShadcnAttachmentState.Uploading)
                        ShadcnAttachmentDescription("Uploading · 64%")
                    }
                }
                ShadcnAttachment(
                    actions = { ShadcnAttachmentActions { RemoveButton() } },
                ) {
                    ShadcnAttachmentMedia { ShadcnText("</>") }
                    ShadcnAttachmentContent {
                        ShadcnAttachmentTitle("message-renderer.tsx")
                        ShadcnAttachmentDescription("TypeScript · 12 KB")
                    }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun vertical_light() = vertical(darkTheme = false)

    @Test fun vertical_dark() = vertical(darkTheme = true)

    @Test fun composer_upload_tray_light() = composerUploadTray(darkTheme = false)

    @Test fun composer_upload_tray_dark() = composerUploadTray(darkTheme = true)
}

@androidx.compose.runtime.Composable
private fun RemoveButton() {
    ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Xs) {
        ShadcnText("✕")
    }
}
