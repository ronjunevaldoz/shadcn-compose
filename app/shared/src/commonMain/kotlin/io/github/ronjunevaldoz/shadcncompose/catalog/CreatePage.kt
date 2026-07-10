@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.CodeBlock
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.SelectVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberPanelStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberSelectItemStyle
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

/**
 * Mirrors the *spirit* of real shadcn/ui's `ui.shadcn.com/create` -- pick a Style/Base
 * Color/Accent from a left-hand preset sidebar, see components re-themed live, copy the
 * setup code -- rather than its literal CLI-scaffolding flow, since this library has no
 * install step to reproduce (it's a Compose dependency, not a generated project). The
 * sidebar's row layout (label caption + bold value + preview swatch, opening a dropdown
 * on click) matches real shadcn's own `/create` settings panel; only the 3 axes this
 * library actually has (Style/Base Color/Accent) are represented -- no Chart Color or
 * Heading/Font rows, since this library has no corresponding tokens for those yet.
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
    val clipboardManager = LocalClipboardManager.current

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(shadcnTheme.colors.sidebar)
                    .verticalScroll(rememberScrollState())
                    .padding(shadcnTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        ) {
            ShadcnText("Theme", style = ShadcnTextStyle.TitleSmall)
            PresetSelectorRow(
                label = "Style",
                value = stylePreset.label,
                options = ShadcnStylePreset.entries,
                onValueChange = onStylePresetChange,
                optionLabel = { it.label },
                swatch = {
                    Box(
                        modifier =
                            Modifier
                                .size(24.dp)
                                .background(shadcnTheme.colors.onSurface, RoundedCornerShape(stylePreset.shapes.md)),
                    )
                },
                optionSwatch = { option ->
                    Box(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .background(shadcnTheme.colors.onSurface, RoundedCornerShape(option.shapes.md)),
                    )
                },
            )
            PresetSelectorRow(
                label = "Base Color",
                value = baseColor.label,
                options = ShadcnBaseColor.entries,
                onValueChange = onBaseColorChange,
                optionLabel = { it.label },
                swatch = {
                    Box(modifier = Modifier.size(24.dp).background(baseColor.light.primary, CircleShape))
                },
                optionSwatch = { option ->
                    Box(modifier = Modifier.size(16.dp).background(option.light.primary, CircleShape))
                },
            )
            PresetSelectorRow(
                label = "Accent",
                value = accent.label,
                options = ShadcnAccent.entries,
                onValueChange = onAccentChange,
                optionLabel = { it.label },
                swatch = {
                    val swatchColor =
                        remember(accent, baseColor) { accent.applyTo(baseColor.light, dark = false).primary }
                    Box(modifier = Modifier.size(24.dp).background(swatchColor, CircleShape))
                },
                optionSwatch = { option ->
                    val swatchColor =
                        remember(option, baseColor) { option.applyTo(baseColor.light, dark = false).primary }
                    Box(modifier = Modifier.size(16.dp).background(swatchColor, CircleShape))
                },
            )
            ShadcnButton(
                onClick = { clipboardManager.setText(AnnotatedString(snippet)) },
                modifier = Modifier.fillMaxWidth().padding(top = shadcnTheme.spacing.sm),
            ) {
                ShadcnText("Get Code")
            }
        }
        Box(
            modifier =
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(shadcnTheme.colors.border),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
}

/**
 * A sidebar preset row matching real shadcn/ui's own `/create` settings panel: a label
 * caption over a bold current value, a small preview [swatch] on the trailing edge, and
 * a dropdown of [options] on click. Built directly on [ShadcnAnchoredPopup] plus the
 * library's own `SelectVariant` trigger/panel/item styles (the same ones
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect] itself uses) rather
 * than extending `ShadcnSelect`, which has no slot for a two-line label+value trigger --
 * this keeps the shared component's API surface untouched for a single, catalog-only
 * layout need.
 */
@Composable
private fun <T> PresetSelectorRow(
    label: String,
    value: String,
    options: List<T>,
    onValueChange: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    swatch: @Composable () -> Unit,
    optionSwatch: (@Composable (T) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val triggerStyleState = remember { MutableStyleState(interactionSource) }

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .styleable(triggerStyleState, SelectVariant.Default.rememberStyle())
                    .clickable(interactionSource = interactionSource, indication = null) { expanded = true }
                    .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                ShadcnText(label, style = ShadcnTextStyle.LabelSmall, muted = true)
                ShadcnText(value, style = ShadcnTextStyle.TitleSmall)
            }
            swatch()
        }

        ShadcnAnchoredPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            val panelStyleState = remember { MutableStyleState(MutableInteractionSource()) }
            Column(
                modifier =
                    Modifier
                        .widthIn(min = 140.dp, max = 280.dp)
                        .styleable(panelStyleState, SelectVariant.Default.rememberPanelStyle())
                        .padding(shadcnTheme.spacing.xxs)
                        .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    val isSelected = optionLabel(option) == value
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    val itemStyleState = remember { MutableStyleState(itemInteractionSource) }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(interactionSource = itemInteractionSource, indication = null) {
                                    onValueChange(option)
                                    expanded = false
                                }
                                .styleable(itemStyleState, rememberSelectItemStyle(isSelected))
                                .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (optionSwatch != null) {
                            optionSwatch(option)
                        }
                        ShadcnText(optionLabel(option), style = ShadcnTextStyle.BodyMedium)
                    }
                }
            }
        }
    }
}
