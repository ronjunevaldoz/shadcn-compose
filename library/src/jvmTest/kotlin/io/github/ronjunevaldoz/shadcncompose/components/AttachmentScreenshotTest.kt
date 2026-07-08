@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
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

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun vertical_light() = vertical(darkTheme = false)

    @Test fun vertical_dark() = vertical(darkTheme = true)
}
