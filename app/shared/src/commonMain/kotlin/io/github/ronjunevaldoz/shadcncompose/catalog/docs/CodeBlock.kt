@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

private val KOTLIN_KEYWORDS =
    setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return", "true", "false", "null",
        "is", "as", "in", "this", "super", "override", "private", "internal", "public",
        "protected", "companion", "data", "sealed", "enum", "open", "abstract", "final",
        "by", "try", "catch", "finally", "throw", "suspend", "inline", "noinline",
        "crossinline", "reified", "out", "vararg", "typealias", "annotation", "const",
        "init", "constructor", "where", "actual", "expect", "get", "set",
    )

// Capture groups: 1 line comment, 2 double-quoted string, 3 annotation, 4 number, 5 identifier/keyword.
private val TOKEN_REGEX =
    Regex(
        "(//[^\\n]*)" +
            "|(\"(?:\\\\.|[^\"\\\\])*\")" +
            "|(@\\w+)" +
            "|\\b(\\d+(?:\\.\\d+)?[fFlL]?)\\b" +
            "|\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b",
    )

/**
 * Minimal Kotlin syntax highlighter -- regex-tokenized, no external dependency.
 * Good enough for short catalog code samples; not a general-purpose lexer.
 */
private fun highlightKotlin(
    code: String,
    isLight: Boolean,
): AnnotatedString {
    val keywordColor = if (isLight) Color(0xFF7C3AED) else Color(0xFFC792EA)
    val stringColor = if (isLight) Color(0xFF15803D) else Color(0xFFA1D490)
    val commentColor = if (isLight) Color(0xFF71717A) else Color(0xFF6B7280)
    val annotationColor = if (isLight) Color(0xFFD97706) else Color(0xFFE5C07B)
    val numberColor = if (isLight) Color(0xFF2563EB) else Color(0xFF79B8FF)

    return buildAnnotatedString {
        var lastIndex = 0
        for (match in TOKEN_REGEX.findAll(code)) {
            if (match.range.first > lastIndex) {
                append(code.substring(lastIndex, match.range.first))
            }
            val text = match.value
            val style =
                when {
                    match.groups[1] != null -> SpanStyle(color = commentColor)
                    match.groups[2] != null -> SpanStyle(color = stringColor)
                    match.groups[3] != null -> SpanStyle(color = annotationColor)
                    match.groups[4] != null -> SpanStyle(color = numberColor)
                    match.groups[5] != null && text in KOTLIN_KEYWORDS -> SpanStyle(color = keywordColor)
                    else -> null
                }
            if (style != null) {
                withStyle(style) { append(text) }
            } else {
                append(text)
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < code.length) {
            append(code.substring(lastIndex))
        }
    }
}

/** A syntax-highlighted, copyable Kotlin code block used throughout the catalog. */
@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val highlighted = highlightKotlin(code, shadcnTheme.colors.isLight)

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(shadcnTheme.shapes.lg))
                .background(shadcnTheme.colors.surfaceVariant),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnBadge(variant = BadgeVariant.Ghost) { ShadcnText("Kotlin", style = ShadcnTextStyle.LabelSmall) }
            ShadcnButton(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Xs,
            ) {
                ShadcnText("Copy", style = ShadcnTextStyle.LabelSmall)
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(shadcnTheme.spacing.md),
        ) {
            SelectionContainer {
                BasicText(
                    text = highlighted,
                    style =
                        shadcnTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = shadcnTheme.colors.onSurface,
                        ),
                )
            }
        }
    }
}
