@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import io.github.ronjunevaldoz.shadcncompose.icons.ChevronRight
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.styles.focusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One collapsible section in a [ShadcnAccordion]. */
data class ShadcnAccordionItem(
    val id: String,
    val title: String,
    val content: @Composable () -> Unit,
)

enum class ShadcnAccordionType { Single, Multiple }

/**
 * A vertically-stacked set of collapsible sections. Matches real shadcn/ui's
 * `accordion.tsx`: each item separated by a bottom border (except the last), a chevron
 * that rotates 180deg when open, and animated expand/collapse.
 *
 * Usage:
 * ```
 * var expanded by remember { mutableStateOf(setOf("item-1")) }
 * ShadcnAccordion(
 *     items = listOf(
 *         ShadcnAccordionItem("item-1", "Is it accessible?") { ShadcnText("Yes.") },
 *         ShadcnAccordionItem("item-2", "Is it styled?") { ShadcnText("Yes.") },
 *     ),
 *     expandedIds = expanded,
 *     onExpandedIdsChange = { expanded = it },
 * )
 * ```
 */
@Composable
fun ShadcnAccordion(
    items: List<ShadcnAccordionItem>,
    expandedIds: Set<String>,
    onExpandedIdsChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    type: ShadcnAccordionType = ShadcnAccordionType.Single,
    // A self-generated ImageVector (ChevronRight rotated) that rotates 180deg on open --
    // not a third-party icon-set dependency (this library still takes none -- see README),
    // just a plain text glyph doesn't render on WasmJS (Skia has no browser emoji-font
    // fallback). Receives isOpen so an override can drive its own rotation/animation too
    // (e.g. this repo's own demo app passes a real heroicons-outline ChevronDown here --
    // see AccordionDoc.kt). Base rotation is 90deg (pointing down, closed) / 270deg
    // (pointing up, open) since the underlying shape points right at 0deg, not down.
    icon: @Composable (isOpen: Boolean) -> Unit = { isOpen ->
        val rotation by animateFloatAsState(if (isOpen) 270f else 90f, label = "accordion-chevron")
        ShadcnGlyphIcon(
            ChevronRight,
            tint = shadcnTheme.colors.onSurfaceVariant,
            modifier = Modifier.rotate(rotation),
            small = true,
        )
    },
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isOpen = item.id in expandedIds
            val toggle = {
                val next =
                    when {
                        type == ShadcnAccordionType.Single && isOpen -> emptySet()
                        type == ShadcnAccordionType.Single -> setOf(item.id)
                        isOpen -> expandedIds - item.id
                        else -> expandedIds + item.id
                    }
                onExpandedIdsChange(next)
            }
            Column {
                AccordionTrigger(title = item.title, isOpen = isOpen, onClick = toggle, icon = icon)
                AnimatedVisibility(visible = isOpen) {
                    Column(modifier = Modifier.padding(bottom = shadcnTheme.spacing.md)) {
                        item.content()
                    }
                }
                if (index != items.lastIndex) {
                    ShadcnSeparator()
                }
            }
        }
    }
}

@Composable
private fun AccordionTrigger(
    title: String,
    isOpen: Boolean,
    onClick: () -> Unit,
    icon: @Composable (isOpen: Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val theme = shadcnTheme
    val styleState = remember { MutableStyleState(interactionSource) }
    val triggerStyle =
        remember(theme) {
            Style {
                background(theme.colors.background)
                focusRing(RoundedCornerShape(theme.shapes.md))
            }
        }
    Row(
        modifier =
            Modifier
                .styleable(styleState, triggerStyle)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(vertical = shadcnTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShadcnText(title, style = ShadcnTextStyle.LabelLarge, modifier = Modifier.weight(1f))
        icon(isOpen)
    }
}
