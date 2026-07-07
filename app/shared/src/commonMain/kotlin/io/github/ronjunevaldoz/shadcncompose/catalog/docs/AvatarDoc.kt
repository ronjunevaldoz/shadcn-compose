package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatarFallback
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatarSize

val avatarDoc =
    ComponentDoc(
        id = "avatar",
        title = "Avatar",
        description = "A circular user/entity image container with an initials fallback.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatar
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatarFallback

            ShadcnAvatar { ShadcnAvatarFallback("CN") }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = "ShadcnAvatar { ShadcnAvatarFallback(\"CN\") }",
                    preview = { ShadcnAvatar { ShadcnAvatarFallback("CN") } },
                ),
                ComponentExample(
                    title = "Sizes",
                    code =
                        """
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnAvatar(size = ShadcnAvatarSize.Sm) { ShadcnAvatarFallback("SM") }
                            ShadcnAvatar(size = ShadcnAvatarSize.Default) { ShadcnAvatarFallback("MD") }
                            ShadcnAvatar(size = ShadcnAvatarSize.Lg) { ShadcnAvatarFallback("LG") }
                        }
                        """.trimIndent(),
                    preview = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnAvatar(size = ShadcnAvatarSize.Sm) { ShadcnAvatarFallback("SM") }
                            ShadcnAvatar(size = ShadcnAvatarSize.Default) { ShadcnAvatarFallback("MD") }
                            ShadcnAvatar(size = ShadcnAvatarSize.Lg) { ShadcnAvatarFallback("LG") }
                        }
                    },
                ),
            ),
    )
