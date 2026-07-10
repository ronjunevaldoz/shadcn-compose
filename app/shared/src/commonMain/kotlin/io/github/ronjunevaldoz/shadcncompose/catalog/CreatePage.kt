@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.heroicons.outline.ChevronDown
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.CodeBlock
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.DocIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

/**
 * Mirrors the *spirit* of real shadcn/ui's `ui.shadcn.com/create` -- pick a Style/Base
 * Color/Accent, see components re-themed live, copy the setup code -- rather than its
 * literal CLI-scaffolding flow, since this library has no install step to reproduce (it's
 * a Compose dependency, not a generated project).
 *
 * Reads/writes the *same* [stylePreset]/[baseColor]/[accent] state
 * [io.github.ronjunevaldoz.shadcncompose.App] already lifts and threads into
 * [io.github.ronjunevaldoz.shadcncompose.catalog.CatalogTopBar]'s own pickers -- no
 * separate copy of this state, so switching here, changing a picker, and switching back
 * to "Components" shows every page re-themed instantly.
 */
@Composable
fun CreatePage(
    stylePreset: ShadcnStylePreset,
    onStylePresetChange: (ShadcnStylePreset) -> Unit,
    baseColor: ShadcnBaseColor,
    onBaseColorChange: (ShadcnBaseColor) -> Unit,
    accent: ShadcnAccent,
    onAccentChange: (ShadcnAccent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snippet =
        remember(stylePreset, baseColor, accent) {
            """
            ShadcnTheme(
                preset = ShadcnStylePreset.${stylePreset.name},
                baseColor = ShadcnBaseColor.${baseColor.name},
                accent = ShadcnAccent.${accent.name},
            ) {
                // your content
            }
            """.trimIndent()
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
            ShadcnText("Create", style = ShadcnTextStyle.TitleLarge)
            ShadcnText(
                "Pick a style, base color, and accent, then copy the setup into your app.",
                style = ShadcnTextStyle.BodyMedium,
                muted = true,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
            ShadcnSelect(
                value = stylePreset,
                options = ShadcnStylePreset.entries,
                onValueChange = onStylePresetChange,
                label = { it.label },
                icon = { DocIcon(ChevronDown) },
            )
            ShadcnSelect(
                value = baseColor,
                options = ShadcnBaseColor.entries,
                onValueChange = onBaseColorChange,
                label = { it.label },
                icon = { DocIcon(ChevronDown) },
            )
            ShadcnSelect(
                value = accent,
                options = ShadcnAccent.entries,
                onValueChange = onAccentChange,
                label = { it.label },
                icon = { DocIcon(ChevronDown) },
            )
        }

        ShadcnCard(
            modifier = Modifier.width(420.dp),
            header = { ShadcnCardHeader(title = "Preview", description = "A live sample of this theme.") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                    ShadcnButton(onClick = {}) { ShadcnText("Default") }
                    ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Outline") }
                    ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Destructive") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                    ShadcnBadge { ShadcnText("New") }
                    ShadcnBadge { ShadcnText("Beta") }
                }
                ShadcnTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Type something...",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
            ShadcnText("Setup", style = ShadcnTextStyle.TitleSmall)
            CodeBlock(code = snippet, modifier = Modifier.fillMaxWidth())
        }
    }
}
