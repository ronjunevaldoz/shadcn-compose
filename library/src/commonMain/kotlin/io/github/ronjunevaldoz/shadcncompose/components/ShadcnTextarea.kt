@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant

/**
 * Multi-line text input. Matches shadcn/ui's real textarea.tsx, which shares the
 * exact same border/focus/disabled spec as Input -- this is a thin wrapper over
 * [ShadcnTextField] with `singleLine = false` and a minimum height.
 *
 * Usage:
 * ```
 * var bio by remember { mutableStateOf("") }
 * ShadcnTextarea(value = bio, onValueChange = { bio = it }, placeholder = "Tell us about yourself")
 * ```
 */
@Composable
fun ShadcnTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    variant: TextFieldVariant = TextFieldVariant.Default,
    style: Style = Style,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    ShadcnTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(minHeight = 64.dp),
        enabled = enabled,
        label = label,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        variant = variant,
        style = style,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = false,
    )
}
