package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Usage:
 * ```
 * ShadcnTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "you@example.com")
 * ShadcnTextField(value = q, onValueChange = { q = it }, variant = TextFieldVariant.Ghost, placeholder = "Search…")
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    variant: TextFieldVariant = TextFieldVariant.Default,
    style: Style = Style,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isEnabled = enabled
        }

    // shadcnTheme.colors.error captured as a plain val before the Style{} block, not
    // read directly inside it -- see ShadcnInputGroup.kt's comment on this same
    // anti-pattern (a stale CompositionLocal snapshot that survives dark-mode toggles).
    val errorColor = shadcnTheme.colors.error
    val errorStyle = if (isError) Style { borderColor(errorColor) } else Style

    // Inside a ShadcnInputGroup the container owns the border and focus ring (real
    // shadcn's InputGroupInput is `border-0 focus-visible:ring-0`); drawing our own
    // here would double the border and put the ring on the field instead of the group.
    // dropShadow() with no arguments explicitly clears the ring set by variant's own
    // `focused { }` block earlier in the `then` chain -- Style properties are override-
    // not-additive per *declared* property, but an omitted property falls through to
    // whatever an earlier style in the chain already set, so simply not calling
    // dropShadow() here would leave the variant's ring showing underneath.
    val insideGroup = LocalInsideInputGroup.current
    val insideGroupStyle =
        if (insideGroup) {
            Style {
                background(Color.Transparent)
                borderColor(Color.Transparent)
                focused {
                    borderColor(Color.Transparent)
                    dropShadow()
                }
            }
        } else {
            Style
        }

    Column(modifier = modifier) {
        if (label != null) {
            ShadcnText(text = label, style = ShadcnTextStyle.LabelLarge)
            Spacer(Modifier.height(shadcnTheme.spacing.xxs))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            // Explicit color: BasicTextField paints its value text (and cursor) from
            // textStyle/cursorBrush, not from the ambient Style contentColor -- without
            // this the value text stays near-black in dark mode (same lesson as
            // ChipVariant.contentColor: text color must be passed explicitly).
            textStyle = shadcnTheme.typography.bodyMedium.copy(color = shadcnTheme.colors.onSurface),
            cursorBrush = SolidColor(shadcnTheme.colors.onSurface),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .styleable(styleState, variant.rememberStyle() then errorStyle then insideGroupStyle, style),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(Modifier.width(shadcnTheme.spacing.xs))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            ShadcnText(placeholder, style = ShadcnTextStyle.BodyMedium, muted = true)
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(shadcnTheme.spacing.xs))
                        trailingIcon()
                    }
                }
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(shadcnTheme.spacing.xxs))
            ShadcnText(
                text = supportingText,
                style = ShadcnTextStyle.BodySmall,
                color = if (isError) shadcnTheme.colors.error else shadcnTheme.colors.onSurfaceVariant,
            )
        }
    }
}
