---
name: kotlin-multiplatform-accessibility
description: >-
  Accessibility (a11y) for Kotlin Multiplatform Compose — semantic roles and
  mergeDescendants, contentDescription on interactive and image elements, screen
  reader traversal order, minimum touch target size, Roborazzi accessibility
  snapshot tests, reduced-motion support, and a Compose a11y audit checklist.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - accessibility
    - a11y
    - contentDescription
    - semantics
    - screen reader
    - TalkBack
    - VoiceOver
    - semantic role
    - mergeDescendants
    - touch target
    - accessibility test
    - Roborazzi a11y
    - reduced motion
    - WCAG
    - compose accessibility
---

## When to Use This Skill

Use when:
- A screen must be navigable by TalkBack (Android) or VoiceOver (iOS)
- Interactive elements (buttons, links, custom composables) need meaningful labels
- Touch targets need to meet the 48×48dp minimum size requirement
- Adding accessibility snapshot tests alongside visual screenshots
- An existing screen is failing an a11y audit

**Trigger keywords:** accessibility, a11y, TalkBack, VoiceOver, contentDescription,
semantics, screen reader, semantic role, mergeDescendants, touch target, WCAG,
accessibility test, Roborazzi a11y, traversal order, live region, role = button,
accessibility label, semantic tree, accessible, screen reader support, accessible app,
a11y compliance, accessibility audit, accessible design, make accessible.

**Freshness rule:** Roborazzi a11y snapshot support (`captureRoboImage` with
`accessibilityCapture()`) was added in Roborazzi 1.7. The Compose
`semantics { }` API is stable; `ExperimentalComposeUiApi` attributes were promoted to
stable in Compose 1.6. WCAG 2.2 is the current standard — refer to SC 1.1.1, SC 2.4.3,
and SC 1.4.3 for the rules most relevant to mobile.

---

## Recommendation First

Accessibility should be built in, not bolted on. Follow these three rules for every screen:
1. Every interactive element has a `contentDescription` or `semantics { contentDescription }` label
2. Custom clickable composables declare `role = Role.Button` (or the appropriate role)
3. Non-decorative images have `contentDescription`; purely decorative images have `contentDescription = null`

Run the Compose a11y checker and Roborazzi a11y snapshot on every screen in CI.

---

## Content descriptions

```kotlin
// Interactive icon button — must have a label
IconButton(onClick = { onIntent(Intent.DeleteItem(item.id)) }) {
    Icon(
        Icons.Default.Delete,
        contentDescription = "Delete ${item.name}",   // describes action, not icon shape
    )
}

// Decorative illustration — null = screen reader skips it
Image(
    painter            = painterResource(Res.drawable.img_success),
    contentDescription = null,   // decorative — no information loss if skipped
)

// Meaningful image
AsyncImage(
    model              = user.avatarUrl,
    contentDescription = "${user.name}'s profile photo",
)
```

---

## Semantic roles on custom composables

```kotlin
// Custom clickable card — declare it as a button
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .semantics {
                role = Role.Button
                contentDescription = "${product.name}, ${product.price}"
            }
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.spacing.sm))
            .background(AppTheme.colors.surface)
            .padding(AppTheme.spacing.md),
    ) {
        // child elements don't need individual descriptions —
        // the parent semantics covers the whole card
        Text(product.name)
        Text(product.price)
    }
}
```

---

## mergeDescendants — group related elements

```kotlin
// ListItem with icon + two text lines: group as one semantic node
Row(
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "${item.title}: ${item.subtitle}"
    }
) {
    Icon(item.icon, contentDescription = null)   // null — parent merges
    Column {
        Text(item.title)
        Text(item.subtitle)
    }
}
```

---

## Traversal order (traversalIndex)

By default, TalkBack and VoiceOver traverse composables in layout order. Override when
visual layout differs from logical reading order:

```kotlin
// Heading should be announced before the body even if visually below
Text(
    "Section title",
    modifier = Modifier.semantics { traversalIndex = -1f },
)
Text(
    "Section body",
    modifier = Modifier.semantics { traversalIndex = 0f },
)
```

---

## Minimum touch target — 48×48dp

```kotlin
// Icon that is visually 24dp must have a 48dp touch target
IconButton(
    onClick = onClose,
    modifier = Modifier.size(48.dp),   // explicit; never smaller
) {
    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(24.dp))
}

// Or use minimumInteractiveComponentSize:
Box(
    modifier = Modifier
        .minimumInteractiveComponentSize()  // enforces 48dp minimum automatically
        .clickable(onClick = onClose),
) {
    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(24.dp))
}
```

---

## Live regions — dynamic content announcements

```kotlin
// Status message that should be read aloud when it changes
val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

Text(
    text     = statusMessage,
    modifier = Modifier.semantics {
        liveRegion = LiveRegionMode.Polite   // announce change without interrupting
    },
)
```

---

## State descriptions

```kotlin
// Toggle button — readable state label
var expanded by remember { mutableStateOf(false) }
IconButton(
    onClick = { expanded = !expanded },
    modifier = Modifier.semantics {
        contentDescription = if (expanded) "Collapse section" else "Expand section"
        stateDescription   = if (expanded) "Expanded" else "Collapsed"
    }
) {
    Icon(
        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
        contentDescription = null,
    )
}
```

---

## Roborazzi accessibility snapshot

```kotlin
// :ui:test — HomeScreenA11yTest.kt
@RunWith(RobolectricTestRunner::class)
class HomeScreenA11yTest {

    @get:Rule val composeRule = createComposeRule()

    @Test fun `home screen passes accessibility audit`() {
        composeRule.setContent {
            AppTheme {
                HomeContent(
                    state = HomeContract.State(items = sampleItems),
                    onIntent = {},
                )
            }
        }
        composeRule
            .onRoot()
            .captureRoboImage(
                filePath = "src/test/snapshots/a11y/home_screen_a11y.png",
                roborazziOptions = RoborazziOptions(
                    captureType = RoborazziOptions.CaptureType.Dump(
                        explanation = AccessibilityExplanation.Default,
                    ),
                ),
            )
    }
}
```

The accessibility dump snapshot shows the semantic tree overlaid on the screen —
review it to spot missing labels, incorrect roles, and traversal order issues.

---

## Compose a11y audit checklist

Run this checklist per screen before shipping:

| Check | Rule |
|---|---|
| Every `IconButton` has a non-null `contentDescription` | WCAG 1.1.1 |
| Every `AsyncImage` / `Image` with meaningful content has a description | WCAG 1.1.1 |
| Every custom clickable has `role = Role.Button` (or Link, Checkbox, etc.) | WCAG 4.1.2 |
| Touch targets are ≥ 48×48dp | WCAG 2.5.5 |
| Color is not the only way to convey information (e.g., errors also have a label) | WCAG 1.4.1 |
| Focus order matches logical reading order | WCAG 2.4.3 |
| Dynamic content updates have `liveRegion` | WCAG 4.1.3 |
| Screen title is announced (TopAppBar title or `heading = true` on a Text) | WCAG 2.4.2 |
| Animations respect `LocalAccessibilityManager.isReduceMotionEnabled` | WCAG 2.3.3 |

---

## Screen heading announcement

```kotlin
AppTopAppBar(
    title = {
        Text(
            "Settings",
            modifier = Modifier.semantics { heading() },
        )
    },
)
```

---

## Common Anti-Patterns

- **Icon without `contentDescription`** — `Icon(Icons.Default.Share, contentDescription = null)`
  is a silent element; TalkBack reads nothing; every interactive icon must have a label
- **`contentDescription` that mirrors the visible text** — `Text("Save")` next to a button
  labeled `"Save"` with `contentDescription = "Save"` is redundant; merge with
  `semantics(mergeDescendants = true)` so the button label suffices
- **Custom composable without `role`** — a `Box` with `.clickable {}` is announced as
  "double-tap to activate" with no role; add `semantics { role = Role.Button }`
- **Relying only on color for errors** — red text without an accompanying error icon or
  label fails WCAG 1.4.1; always pair color with a label or icon
- **Unconstrained touch targets** — icon buttons sized 24dp are too small for users with
  motor impairments; always use `Modifier.size(48.dp)` or `minimumInteractiveComponentSize()`

---

## Related Skills

- `kotlin-multiplatform-roborazzi` — a11y snapshot tests extend the Roborazzi setup;
  `captureRoboImage` with `CaptureType.Dump` generates the semantic tree overlay
- `kotlin-multiplatform-compose-animation` — `respectsReducedMotion()` check is an a11y
  requirement; link both skills when adding animations to a screen
- `kotlin-multiplatform-design-system` — color contrast ratios must meet WCAG AA (4.5:1
  for normal text, 3:1 for large text); verify in the design system token definitions

---

## Output Style

When implementing accessibility, respond in this order:
1. **Audit findings** — list what is missing on the target screen using the checklist
2. **contentDescription fixes** — icons, images, interactive elements
3. **Role declarations** — custom clickable composables
4. **Touch target fixes** — `minimumInteractiveComponentSize()` or explicit `size(48.dp)`
5. **mergeDescendants** — group related row/card children
6. **Roborazzi a11y snapshot** — one test per screen
7. **Checklist** — confirm each row is satisfied

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
