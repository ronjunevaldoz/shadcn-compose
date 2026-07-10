package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.focusRingShadow
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A one-time-passcode input: [length] boxed slots that fill left-to-right, matching
 * real shadcn/ui's `input-otp.tsx` visual (that component wraps the `input-otp` npm
 * package for its per-slot caret/paste/focus-chaining logic). There is no Compose
 * Multiplatform equivalent, so this uses the standard Compose OTP-input trick instead:
 * one real (invisible) [BasicTextField] captures all typing/paste/backspace natively,
 * overlaid on top of the visual slot boxes underneath -- avoids manually chaining focus
 * across N separate fields, which is fragile across platforms (especially backspace-on-
 * empty-slot behavior).
 *
 * Usage:
 * ```
 * var code by remember { mutableStateOf("") }
 * ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
 * ```
 */
@Composable
fun ShadcnInputOTP(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
            repeat(length) { index ->
                val isActive = isFocused && index == value.length
                OtpSlot(char = value.getOrNull(index), isActive = isActive)
            }
        }
        BasicTextField(
            value = value,
            onValueChange = { new -> onValueChange(new.filter { it.isDigit() }.take(length)) },
            enabled = enabled,
            modifier = Modifier.matchParentSize(),
            interactionSource = interactionSource,
            textStyle = shadcnTheme.typography.bodyMedium.copy(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
    }
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun OtpSlot(
    char: Char?,
    isActive: Boolean,
) {
    val theme = shadcnTheme
    // `isActive` is a computed boolean from the parent, not a real per-slot focus
    // event, so this uses a plain conditional inside Style { } rather than a
    // focused { } predicate -- same pattern as ShadcnInputGroup's hasFocusWithin.
    val slotStyle =
        Style {
            background(theme.colors.background)
            borderWidth(1.dp)
            borderColor(if (isActive) theme.colors.borderFocus else theme.colors.border)
            shape(RoundedCornerShape(theme.shapes.md))
            if (isActive) dropShadow(theme.focusRingShadow())
        }

    Box(
        modifier =
            Modifier
                .size(36.dp)
                .styleable(remember { MutableStyleState(MutableInteractionSource()) }, slotStyle),
        contentAlignment = Alignment.Center,
    ) {
        ShadcnText(char?.toString() ?: "", style = ShadcnTextStyle.TitleMedium)
    }
}
