@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A segmented month/day/year date entry field, matching real shadcn/ui's community
 * `date-range-picker-for-shadcn` reference (`date-input.tsx`) -- verified directly against
 * its source, not assumed: three linked numeric fields (M/D/D capped at 2 digits, Y capped
 * at 4), Left/Right arrow keys move focus between fields at the text boundary, Up/Down
 * arrow keys increment/decrement the focused field (day rolls into the next/previous month,
 * month rolls the year), and an invalid value reverts to the last valid one on blur. Calls
 * [onValueChange] as soon as all three fields form a valid calendar date -- not gated behind
 * a separate "confirm" step, same as the real reference.
 *
 * Real shadcn/ui itself has no such component (this pattern isn't part of the base
 * registry) -- it's specific to that popular community date-range-picker recipe, which this
 * library's own "Compare" date-range-picker example (see the catalog app) is modeled on for
 * exactly the same reason: a calendar grid alone can't be scoped to "only edit the
 * comparison range" the way a separate typed field naturally can.
 *
 * Usage:
 * ```
 * var date by remember { mutableStateOf(ShadcnCalendarDate(2026, 3, 15)) }
 * ShadcnDateInput(value = date, onValueChange = { date = it })
 * ```
 */
@Composable
fun ShadcnDateInput(
    value: ShadcnCalendarDate,
    onValueChange: (ShadcnCalendarDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var monthField by remember(value) { mutableStateOf(TextFieldValue(value.month.toString())) }
    var dayField by remember(value) { mutableStateOf(TextFieldValue(value.day.toString())) }
    var yearField by remember(value) { mutableStateOf(TextFieldValue(value.year.toString())) }

    val monthFocus = remember { FocusRequester() }
    val dayFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }

    fun commitIfValid(
        month: String,
        day: String,
        year: String,
    ) {
        val m = month.toIntOrNull()
        val d = day.toIntOrNull()
        val y = year.toIntOrNull()
        if (m != null && d != null && y != null && m in 1..12 && y in 1000..9999 && d in 1..daysInMonth(y, m)) {
            onValueChange(ShadcnCalendarDate(y, m, d))
        }
    }

    fun setFields(
        month: Int,
        day: Int,
        year: Int,
    ) {
        monthField = TextFieldValue(month.toString(), TextRange(0, month.toString().length))
        dayField = TextFieldValue(day.toString(), TextRange(0, day.toString().length))
        yearField = TextFieldValue(year.toString(), TextRange(0, year.toString().length))
        commitIfValid(month.toString(), day.toString(), year.toString())
    }

    // Real's exact cascade: day rollover bumps month (and year on Dec/Jan wrap).
    fun bumpDay(delta: Int) {
        val m = monthField.text.toIntOrNull() ?: value.month
        val d = dayField.text.toIntOrNull() ?: value.day
        val y = yearField.text.toIntOrNull() ?: value.year
        var newDay = d + delta
        var newMonth = m
        var newYear = y
        if (newDay < 1) {
            newMonth -= 1
            if (newMonth < 1) {
                newMonth = 12
                newYear -= 1
            }
            newDay = daysInMonth(newYear, newMonth)
        } else if (newDay > daysInMonth(newYear, newMonth)) {
            newDay = 1
            newMonth += 1
            if (newMonth > 12) {
                newMonth = 1
                newYear += 1
            }
        }
        setFields(newMonth, newDay, newYear)
    }

    // Real's exact cascade: month rollover bumps year.
    fun bumpMonth(delta: Int) {
        val m = monthField.text.toIntOrNull() ?: value.month
        val d = dayField.text.toIntOrNull() ?: value.day
        val y = yearField.text.toIntOrNull() ?: value.year
        var newMonth = m + delta
        var newYear = y
        if (newMonth < 1) {
            newMonth = 12
            newYear -= 1
        } else if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }
        val newDay = d.coerceAtMost(daysInMonth(newYear, newMonth))
        setFields(newMonth, newDay, newYear)
    }

    fun bumpYear(delta: Int) {
        val m = monthField.text.toIntOrNull() ?: value.month
        val d = dayField.text.toIntOrNull() ?: value.day
        val y = (yearField.text.toIntOrNull() ?: value.year) + delta
        val newDay = d.coerceAtMost(daysInMonth(y, m))
        setFields(m, newDay, y)
    }

    Row(
        modifier =
            modifier
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .background(shadcnTheme.colors.background, RoundedCornerShape(shadcnTheme.shapes.md))
                .padding(horizontal = shadcnTheme.spacing.xs, vertical = shadcnTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateInputSegment(
            field = monthField,
            onFieldChange = {
                monthField = it
                commitIfValid(it.text, dayField.text, yearField.text)
            },
            maxLength = 2,
            onIncrement = { bumpMonth(1) },
            onDecrement = { bumpMonth(-1) },
            focusRequester = monthFocus,
            nextFocusRequester = dayFocus,
            width = 20.dp,
            placeholder = "M",
        )
        ShadcnText("/", style = ShadcnTextStyle.BodySmall, muted = true)
        DateInputSegment(
            field = dayField,
            onFieldChange = {
                dayField = it
                commitIfValid(monthField.text, it.text, yearField.text)
            },
            maxLength = 2,
            onIncrement = { bumpDay(1) },
            onDecrement = { bumpDay(-1) },
            focusRequester = dayFocus,
            previousFocusRequester = monthFocus,
            nextFocusRequester = yearFocus,
            width = 22.dp,
            placeholder = "D",
        )
        ShadcnText("/", style = ShadcnTextStyle.BodySmall, muted = true)
        DateInputSegment(
            field = yearField,
            onFieldChange = {
                yearField = it
                commitIfValid(monthField.text, dayField.text, it.text)
            },
            maxLength = 4,
            onIncrement = { bumpYear(1) },
            onDecrement = { bumpYear(-1) },
            focusRequester = yearFocus,
            previousFocusRequester = dayFocus,
            width = 38.dp,
            placeholder = "YYYY",
        )
    }
}

@Composable
private fun DateInputSegment(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    maxLength: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    focusRequester: FocusRequester,
    width: androidx.compose.ui.unit.Dp,
    placeholder: String,
    previousFocusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
) {
    var lastValidText by remember { mutableStateOf(field.text) }

    BasicTextField(
        value = field,
        onValueChange = { new ->
            val digitsOnly = new.text.filter { it.isDigit() }.take(maxLength)
            onFieldChange(new.copy(text = digitsOnly, selection = TextRange(digitsOnly.length)))
        },
        modifier =
            Modifier
                .width(width)
                .focusRequester(focusRequester)
                .onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        // Real's exact blur rule: empty or invalid-for-this-field reverts to
                        // the last value that was valid, rather than staying blank/malformed.
                        if (field.text.isEmpty() || field.text.toIntOrNull() == null) {
                            onFieldChange(field.copy(text = lastValidText))
                        } else {
                            lastValidText = field.text
                        }
                    }
                }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionUp -> {
                            onIncrement()
                            true
                        }
                        Key.DirectionDown -> {
                            onDecrement()
                            true
                        }
                        Key.DirectionLeft -> {
                            val atStart = field.selection.start == 0 && field.selection.end == 0
                            if (atStart && previousFocusRequester != null) {
                                previousFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionRight -> {
                            val atEnd =
                                field.selection.start == field.text.length && field.selection.end == field.text.length
                            if (atEnd && nextFocusRequester != null) {
                                nextFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                },
        textStyle =
            shadcnTheme.typography.bodySmall.copy(
                color = shadcnTheme.colors.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            ),
        singleLine = true,
        cursorBrush = SolidColor(shadcnTheme.colors.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            if (field.text.isEmpty()) {
                ShadcnText(placeholder, style = ShadcnTextStyle.BodySmall, muted = true)
            }
            innerTextField()
        },
    )
}
