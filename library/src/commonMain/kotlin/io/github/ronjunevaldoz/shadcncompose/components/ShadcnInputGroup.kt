@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Set by [ShadcnInputGroup] so an inner [ShadcnTextField] (or [ShadcnTextarea]) knows
 * the group container owns the border and focus ring, and suppresses its own -- the
 * same contract as real shadcn's `InputGroupInput` (`border-0 focus-visible:ring-0`).
 */
internal val LocalInsideInputGroup = compositionLocalOf { false }

/**
 * A bordered container grouping a text field with leading/trailing addons (icons,
 * static text, buttons), matching real shadcn/ui's `input-group.tsx` model:
 *
 * - the **container** owns the single border and rounded shape -- an inner
 *   [ShadcnTextField] detects it's inside a group and drops its own border,
 *   background, and focus ring automatically (no `variant` juggling needed);
 * - focus is tracked *on the container*: when the inner field gains focus, the
 *   group's border swaps to the focus color and the focus ring draws around the
 *   whole group (real shadcn's `has-[:focus-visible]:border-ring` +
 *   `has-[:focus-visible]:ring-[3px]`), not around the inner field;
 * - the field slot takes the remaining width via `weight(1f)`, so trailing addons
 *   keep their intrinsic width instead of being squeezed out by the field.
 *
 * Usage:
 * ```
 * ShadcnInputGroup(leading = { ShadcnInputGroupText("$") }) {
 *     ShadcnTextField(value = amount, onValueChange = { amount = it })
 * }
 * ```
 */
@Composable
fun ShadcnInputGroup(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    var hasFocusWithin by remember { mutableStateOf(false) }

    // Container style deliberately has no contentPadding: the inner field keeps its
    // own variant padding (real shadcn: the container is unpadded, `InputGroupInput`
    // keeps standard input padding), so text inset stays identical to a standalone field.
    val containerStyle =
        Style {
            background(shadcnTheme.colors.background)
            borderWidth(1.dp)
            borderColor(if (hasFocusWithin) shadcnTheme.colors.borderFocus else shadcnTheme.colors.border)
            shape(RoundedCornerShape(shadcnTheme.shapes.lg))
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusEvent { hasFocusWithin = it.hasFocus }
                .shadcnFocusRing(
                    focused = hasFocusWithin,
                    color = shadcnTheme.colors.borderFocus.copy(alpha = shadcnTheme.ring.opacity),
                    width = shadcnTheme.ring.width,
                    offset = shadcnTheme.ring.offset,
                    cornerRadius = shadcnTheme.shapes.lg,
                )
                .styleable(styleState, containerStyle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalInsideInputGroup provides true) {
            if (leading != null) {
                Box(modifier = Modifier.padding(start = shadcnTheme.spacing.md)) { leading() }
            }
            Box(modifier = Modifier.weight(1f)) { content() }
            if (trailing != null) {
                Box(modifier = Modifier.padding(end = shadcnTheme.spacing.md)) { trailing() }
            }
        }
    }
}

/** A static, non-interactive label used as an addon inside [ShadcnInputGroup]. */
@Composable
fun ShadcnInputGroupText(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.BodyMedium, muted = true)
}
