package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * A form-field label. Purely presentational -- Compose has no HTML `for` attribute
 * to wire up, so association with a field is just visual (place it directly above
 * the field it describes).
 *
 * Usage:
 * ```
 * ShadcnLabel("Email", required = true)
 * ShadcnTextField(value = email, onValueChange = { email = it })
 * ```
 */
@Composable
fun ShadcnLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false,
) {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    Row(modifier = modifier) {
        ShadcnText(
            text = text,
            style = ShadcnTextStyle.LabelLarge,
            muted = disabled,
        )
        if (required) {
            ShadcnText(
                text = " *",
                style = ShadcnTextStyle.LabelLarge,
                color = theme.colors.error,
            )
        }
    }
}
