@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A bordered container for grouping a text field with leading/trailing addons
 * (icons, static text, buttons) -- the container owns the border, so the inner
 * field should use [TextFieldVariant.Ghost] (borderless) via its `variant` param.
 *
 * Note: unlike real shadcn's `has-[...]:focus-visible` CSS trick, this container's
 * border does not yet react to the inner field's focus state -- a static border only.
 *
 * Usage:
 * ```
 * ShadcnInputGroup(leading = { ShadcnInputGroupText("$") }) {
 *     ShadcnTextField(value = amount, onValueChange = { amount = it }, variant = TextFieldVariant.Ghost)
 * }
 * ```
 */
@Composable
fun ShadcnInputGroup(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .styleable(styleState, TextFieldVariant.Default.style),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(start = shadcnTheme.spacing.sm)) { leading() }
        }
        content()
        if (trailing != null) {
            Box(modifier = Modifier.padding(end = shadcnTheme.spacing.sm)) { trailing() }
        }
    }
}

/** A static, non-interactive label used as an addon inside [ShadcnInputGroup]. */
@Composable
fun ShadcnInputGroupText(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.BodyMedium, muted = true)
}
