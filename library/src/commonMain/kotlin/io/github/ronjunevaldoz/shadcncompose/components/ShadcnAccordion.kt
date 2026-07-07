package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
                AccordionTrigger(title = item.title, isOpen = isOpen, onClick = toggle)
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
) {
    val rotation by animateFloatAsState(if (isOpen) 180f else 0f, label = "accordion-chevron")
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            Modifier
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(vertical = shadcnTheme.spacing.md)
                .background(shadcnTheme.colors.background),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShadcnText(title, style = ShadcnTextStyle.LabelLarge, modifier = Modifier.weight(1f))
        ShadcnText("⌄", style = ShadcnTextStyle.BodyMedium, muted = true, modifier = Modifier.rotate(rotation))
    }
}
