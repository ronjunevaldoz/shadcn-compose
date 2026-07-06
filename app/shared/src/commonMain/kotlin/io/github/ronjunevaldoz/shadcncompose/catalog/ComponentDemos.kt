@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant

@Composable
internal fun ButtonDemo() {
    DemoColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnButton(onClick = {}) { ShadcnText("Default") }
            ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Outline") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnButton(onClick = {}, variant = ButtonVariant.Secondary) { ShadcnText("Secondary") }
            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Ghost") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Destructive") }
            ShadcnButton(onClick = {}, enabled = false) { ShadcnText("Disabled") }
        }
    }
}

@Composable
internal fun CardDemo() {
    DemoColumn {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth(),
            header = { ShadcnCardHeader(title = "Account", description = "Manage your account settings") },
            footer = { ShadcnButton(onClick = {}) { ShadcnText("Save") } },
        ) {
            ShadcnText("Card body content goes here.")
        }
    }
}

@Composable
internal fun BadgeDemo() {
    DemoColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnBadge { ShadcnText("Default") }
            ShadcnBadge(variant = BadgeVariant.Secondary) { ShadcnText("Secondary") }
            ShadcnBadge(variant = BadgeVariant.Destructive) { ShadcnText("Error") }
            ShadcnBadge(variant = BadgeVariant.Outline) { ShadcnText("Draft") }
        }
    }
}

@Composable
internal fun ChipDemo() {
    var selected by remember { mutableStateOf(false) }
    DemoColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnChip(label = "Selectable", selected = selected, onClick = { selected = !selected })
            ShadcnChip(label = "Outline", variant = ChipVariant.Outline, onClick = {})
            ShadcnChip(label = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Composable
internal fun TextFieldDemo() {
    var value by remember { mutableStateOf("") }
    DemoColumn {
        ShadcnTextField(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            placeholder = "you@example.com",
        )
        ShadcnTextField(
            value = "bad-email",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            isError = true,
            supportingText = "Please enter a valid email address",
        )
    }
}

@Composable
internal fun TextDemo() {
    DemoColumn {
        ShadcnText("Title Large", style = ShadcnTextStyle.TitleLarge)
        ShadcnText("Body Medium", style = ShadcnTextStyle.BodyMedium)
        ShadcnText("Muted body", style = ShadcnTextStyle.BodyMedium, muted = true)
        ShadcnText("Label Small", style = ShadcnTextStyle.LabelSmall)
    }
}

@Composable
private fun DemoColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}
