@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
 *
 * [bottomBar] matches real shadcn's `InputGroupAddon align="block-end"` -- a full-width
 * toolbar row *below* the field instead of an inline leading/trailing addon beside it,
 * for a chat-composer layout (a growing multi-line [ShadcnTextField]/[ShadcnTextarea] on
 * top, an attach/send button row underneath). Real shadcn's container switches from a
 * single row to `flex-col` whenever a `block-end`/`block-start` addon is present
 * (`has-[>[data-align=block-end]]:flex-col`); this does the same by adding a second Row
 * below the existing leading/content/trailing one instead of replacing it, so leading/
 * trailing addons beside the field still work in combination with a bottom toolbar.
 * [bottomBar] content is left-aligned by default, same as real shadcn's `justify-start`
 * on `block-end` -- use `Modifier.weight(1f)` on a spacer or `horizontalArrangement` via
 * your own `Row` inside it to push an item to the far end, same as a chat send button.
 *
 * ```
 * ShadcnInputGroup(
 *     bottomBar = {
 *         ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Icon) { ... }
 *         Spacer(Modifier.weight(1f))
 *         ShadcnButton(onClick = ::send, size = ButtonSize.Icon) { ... }
 *     },
 * ) {
 *     ShadcnTextField(value = text, onValueChange = { text = it }, singleLine = false)
 * }
 * ```
 */
@Composable
fun ShadcnInputGroup(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    var hasFocusWithin by remember { mutableStateOf(false) }

    // colors/shapes read here, in a real @Composable position, and captured as plain
    // vals -- reading `shadcnTheme.colors.*` directly *inside* the Style{} lambda body
    // below captures a stale CompositionLocal snapshot instead (documented anti-pattern,
    // see .claude/AGENTS.md's StyleScope notes): the container would silently freeze at
    // whichever theme was active on first composition, e.g. staying light-mode white
    // forever after a dark-mode toggle even though every sibling correctly re-themes.
    val theme = shadcnTheme
    val colors = theme.colors
    val shapes = theme.shapes

    // Container style deliberately has no contentPadding: the inner field keeps its
    // own variant padding (real shadcn: the container is unpadded, `InputGroupInput`
    // keeps standard input padding), so text inset stays identical to a standalone field.
    // This component tracks focus manually via onFocusEvent (not the Style API's own
    // `focused { }` state block, since it's focus-*within*, not the container's own
    // focus), so the ring is applied the same way -- an inline conditional, not a
    // state predicate.
    val containerStyle =
        Style {
            background(colors.background)
            borderWidth(if (hasFocusWithin) theme.ring.width else 1.dp)
            borderColor(if (hasFocusWithin) colors.borderFocus else colors.border)
            shape(RoundedCornerShape(shapes.lg))
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusEvent { hasFocusWithin = it.hasFocus }
                .styleable(styleState, containerStyle),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalInsideInputGroup provides true) {
                if (leading != null) {
                    // `end = xs` is a floor, not a duplicate of the field's own start
                    // contentPadding: that inset only holds while the field's text is
                    // short enough to leave slack in its weight(1f) box. Once the value
                    // is long enough to fill it, the field's padding no longer keeps the
                    // addon clear of the text, so the group must own a minimum gap of
                    // its own instead of trusting whatever `content()` happens to be.
                    Box(modifier = Modifier.padding(start = shadcnTheme.spacing.md, end = shadcnTheme.spacing.xs)) {
                        leading()
                    }
                }
                Box(modifier = Modifier.weight(1f)) { content() }
                if (trailing != null) {
                    Box(modifier = Modifier.padding(start = shadcnTheme.spacing.xs, end = shadcnTheme.spacing.md)) {
                        trailing()
                    }
                }
            }
        }
        if (bottomBar != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                content = bottomBar,
            )
        }
    }
}

/** A static, non-interactive label used as an addon inside [ShadcnInputGroup]. */
@Composable
fun ShadcnInputGroupText(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.BodyMedium, muted = true)
}
