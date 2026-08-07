# Step 8: Usage patterns

Part of `kmp-compose-design-system`. Load this file when working on: step 8: usage patterns.

---

### Basic usage

```kotlin
// Default button
AppButton(onClick = { /* ... */ }) {
    AppText("Save")
}

// Variant + size
AppButton(
    onClick = { /* ... */ },
    variant = ButtonVariant.Outline,
    size = ButtonSize.Sm,
) {
    AppText("Cancel")
}

// Destructive with icon
AppButton(
    onClick = { deleteItem() },
    variant = ButtonVariant.Destructive,
) {
    Icon(Icons.Default.Delete, contentDescription = null)
    Spacer(Modifier.width(4.dp))
    AppText("Delete")
}
```

### One-off style override (escape hatch)

```kotlin
// Override just the corner radius on this specific instance
AppButton(
    onClick = {},
    style = Style { shape(CircleShape) },
) {
    AppText("Pill button")
}
```

### Style composition for custom variants

```kotlin
// Compose multiple styles — reuse without touching the design system
val accentButtonStyle = ButtonVariant.Default.style then Style {
    background(Color(0xFF7C3AED))   // brand purple
    contentColor(Color.White)
}

AppButton(onClick = {}, style = accentButtonStyle) {
    AppText("Accent")
}
```

### Card composition (shadcn-style slots)

```kotlin
AppCard(
    variant = CardVariant.Default,
    size = CardSize.Sm,
    header = {
        CardHeader(
            title = "Account",
            description = "Manage your account settings",
            action = { AppBadge(variant = BadgeVariant.Secondary) { AppText("Pro") } },
        )
    },
    footer = {
        Row(horizontalArrangement = Arrangement.End) {
            AppButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Sm) { AppText("Cancel") }
            Spacer(Modifier.width(8.dp))
            AppButton(onClick = {}) { AppText("Save") }
        }
    },
) {
    AppText("Card body content here.")
}
```

### Chips as filter group

```kotlin
val tags = listOf("Kotlin", "Swift", "Rust")
var selected by remember { mutableStateOf(setOf("Kotlin")) }

Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    tags.forEach { tag ->
        AppChip(
            label = tag,
            selected = tag in selected,
            onClick = { selected = if (tag in selected) selected - tag else selected + tag },
        )
    }
}
```

---

