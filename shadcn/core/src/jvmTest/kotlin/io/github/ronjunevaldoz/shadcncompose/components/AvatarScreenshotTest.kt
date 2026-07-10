package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class AvatarScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("avatar_states", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShadcnAvatar(size = ShadcnAvatarSize.Sm) { ShadcnAvatarFallback("SM") }
                ShadcnAvatar(size = ShadcnAvatarSize.Default) { ShadcnAvatarFallback("MD") }
                ShadcnAvatar(size = ShadcnAvatarSize.Lg) { ShadcnAvatarFallback("LG") }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
