package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Vertically stacks a group of [ShadcnField]s, matching real shadcn/ui's `field.tsx`
 * `FieldGroup` (and `FieldSet`, which this library doesn't distinguish since Compose has
 * no native `<fieldset>` semantics to attach to).
 *
 * Real shadcn's `form.tsx` is a thin wrapper of these same `Field*` primitives around
 * `react-hook-form` + `zod` for validation wiring -- since this library has no
 * form-state-management dependency (every other component here is already
 * caller-hoisted state, no exception for forms), there is no separate `ShadcnForm`;
 * compose `ShadcnField`/[ShadcnFieldGroup] directly with your own hoisted state.
 *
 * Usage:
 * ```
 * ShadcnFieldGroup {
 *     ShadcnField {
 *         ShadcnFieldLabel("Email")
 *         ShadcnTextField(value = email, onValueChange = { email = it })
 *         ShadcnFieldDescription("We'll never share your email.")
 *     }
 * }
 * ```
 */
@Composable
fun ShadcnFieldGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxl),
        content = content,
    )
}

enum class ShadcnFieldOrientation { Vertical, Horizontal }

/** One labeled control + its description/validation message, matching real shadcn's `Field`. */
@Composable
fun ShadcnField(
    modifier: Modifier = Modifier,
    orientation: ShadcnFieldOrientation = ShadcnFieldOrientation.Vertical,
    content: @Composable () -> Unit,
) {
    if (orientation == ShadcnFieldOrientation.Vertical) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        ) {
            content()
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

/** A [ShadcnField]'s label -- an alias over [ShadcnLabel] so field composition reads consistently. */
@Composable
fun ShadcnFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false,
) {
    ShadcnLabel(text = text, modifier = modifier, required = required, disabled = disabled)
}

/** Helper text under a [ShadcnField]'s control, matching real shadcn's `FieldDescription`. */
@Composable
fun ShadcnFieldDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, muted = true, modifier = modifier)
}

/** A validation-error message under a [ShadcnField]'s control, matching real shadcn's `FieldError`/`FormMessage`. */
@Composable
fun ShadcnFieldError(
    text: String,
    modifier: Modifier = Modifier,
) {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, color = theme.colors.error, modifier = modifier)
}

/** A horizontal divider between [ShadcnField]s within a [ShadcnFieldGroup], with an optional centered label. */
@Composable
fun ShadcnFieldSeparator(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    if (label == null) {
        ShadcnSeparator(modifier = modifier)
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeparatorLine()
            ShadcnText(label, style = ShadcnTextStyle.LabelSmall, muted = true)
            SeparatorLine()
        }
    }
}

@Composable
private fun RowScope.SeparatorLine() {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(shadcnTheme.colors.border),
    )
}
