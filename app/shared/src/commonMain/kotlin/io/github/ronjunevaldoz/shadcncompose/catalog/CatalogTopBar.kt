@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSwitch
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

private const val GITHUB_URL = "https://github.com/ronjunevaldoz/shadcn-compose"

/**
 * App-wide top bar: brand mark on the left; Style/Base/Accent dropdown pickers +
 * GitHub link + dark mode toggle on the right -- mirroring ui.shadcn.com/create's
 * three independent theming axes. Sits above
 * [io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost]'s sidebar/content
 * row so it stays visible across every screen -- switching any picker here re-themes
 * every component on every page instantly.
 */
@Composable
fun CatalogTopBar(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    stylePreset: ShadcnStylePreset,
    onStylePresetChange: (ShadcnStylePreset) -> Unit,
    baseColor: ShadcnBaseColor,
    onBaseColorChange: (ShadcnBaseColor) -> Unit,
    accent: ShadcnAccent,
    onAccentChange: (ShadcnAccent) -> Unit,
    onMenuClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(shadcnTheme.colors.background)
                .padding(horizontal = shadcnTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onMenuClick != null) {
                ShadcnButton(onClick = onMenuClick, variant = ButtonVariant.Ghost, size = ButtonSize.Sm) {
                    ShadcnText("☰")
                }
            }
            ShadcnText("shadcn-compose", style = ShadcnTextStyle.TitleSmall)
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnSelect(
                value = stylePreset,
                options = ShadcnStylePreset.entries,
                onValueChange = onStylePresetChange,
                label = { it.label },
            )
            ShadcnSelect(
                value = baseColor,
                options = ShadcnBaseColor.entries,
                onValueChange = onBaseColorChange,
                label = { it.label },
            )
            ShadcnSelect(
                value = accent,
                options = ShadcnAccent.entries,
                onValueChange = onAccentChange,
                label = { it.label },
            )
            ShadcnButton(
                onClick = { uriHandler.openUri(GITHUB_URL) },
                variant = ButtonVariant.Ghost,
            ) {
                ShadcnText("GitHub")
            }
            ShadcnSwitch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(shadcnTheme.colors.border),
    )
}
