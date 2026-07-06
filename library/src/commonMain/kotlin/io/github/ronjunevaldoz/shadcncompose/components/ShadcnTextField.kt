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
import androidx.compose.ui.text.input.VisualTransformation
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
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

    val errorStyle = if (isError) Style { borderColor(shadcnTheme.colors.error) } else Style

    Column(modifier = modifier) {
        if (label != null) {
            ShadcnText(text = label, style = ShadcnTextStyle.LabelLarge)
            Spacer(Modifier.height(shadcnTheme.spacing.xxs))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .styleable(styleState, variant.style then errorStyle, style),
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
