---
name: kotlin-multiplatform-compose-animation
description: >-
  Compose Multiplatform animation patterns — AnimatedVisibility with enter/exit
  transitions, animateContentSize, Crossfade for screen-level transitions,
  animateFloatAsState / animateDpAsState for property animations, and shared
  element transitions (Compose 1.7+). Covers when to use each API and how to
  keep animations accessible with reduced-motion support.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - animation
    - AnimatedVisibility
    - animateContentSize
    - Crossfade
    - AnimatedContent
    - animateFloatAsState
    - animateDpAsState
    - shared element transition
    - enter transition
    - exit transition
    - spring animation
    - tween animation
    - compose animation
    - reduced motion
    - accessibility animation
---

## When to Use This Skill

Use when:
- A UI element should fade, slide, or expand into view rather than appearing instantly
- Switching between two composables needs a visual crossfade or slide transition
- A card or list item changes size and should animate the height change
- A property (alpha, scale, offset) needs to animate from one value to another
- Shared element transitions are needed between list and detail screens (Compose 1.7+)

**Trigger keywords:** animation, AnimatedVisibility, animateContentSize, Crossfade,
AnimatedContent, animateFloatAsState, animateDpAsState, transition, enter transition,
exit transition, shared element, spring animation, tween animation, compose animation,
fade in, slide in, bounce, ease, reduced motion, motion accessibility,
animate, animated transition, smooth transition, animate appearance, animate change,
motion, visual transition, page transition, screen transition.

**Freshness rule:** Shared element transitions were stabilized in Compose 1.7 (Compose
Multiplatform 1.7). For earlier versions use `Crossfade` or a custom `AnimatedContent`.
`androidx.compose.animation:animation-graphics` (animated vector drawables) is
Android-only and not available in `commonMain`.

---

## Recommendation First

Use the simplest API that achieves the goal. In order of preference:
1. `AnimatedVisibility` — show/hide with transitions
2. `animateContentSize()` — height/width change
3. `Crossfade` / `AnimatedContent` — swap between two composables
4. `animateXAsState` — animate a single property value
5. Shared element transitions — only for connected list-detail navigation

---

## AnimatedVisibility — show/hide with enter/exit

```kotlin
@Composable
fun ExpandableCard(title: String, body: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()                  // smooth height change
            .clip(RoundedCornerShape(AppTheme.spacing.sm))
            .background(AppTheme.colors.surface)
            .clickable { expanded = !expanded }
            .padding(AppTheme.spacing.md),
    ) {
        Text(title, style = AppTheme.typography.titleMedium)

        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn() + expandVertically(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            Text(
                body,
                style    = AppTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppTheme.spacing.sm),
            )
        }
    }
}
```

---

## Crossfade — swap between two composables

```kotlin
@Composable
fun LoadingOrContent(isLoading: Boolean, content: @Composable () -> Unit) {
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(durationMillis = 300),
    ) { loading ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            content()
        }
    }
}
```

---

## AnimatedContent — slide between states

```kotlin
@Composable
fun StepCounter(step: Int) {
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            if (targetState > initialState) {
                // Advancing — slide in from right
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            } else {
                // Going back — slide in from left
                slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
            }.using(SizeTransform(clip = false))
        },
        label = "step_counter",
    ) { currentStep ->
        Text("Step $currentStep", style = AppTheme.typography.headlineMedium)
    }
}
```

---

## animateXAsState — animate a property value

```kotlin
@Composable
fun SelectableChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.primary else AppTheme.colors.surface,
        animationSpec = tween(durationMillis = 200),
        label = "chip_bg",
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale",
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
    ) {
        Text(label, color = if (selected) AppTheme.colors.onPrimary else AppTheme.colors.onSurface)
    }
}
```

---

## Transition — animate multiple properties together

```kotlin
@Composable
fun PulsingFab(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = pressed, label = "fab_press")

    val scale by transition.animateFloat(
        label = "fab_scale",
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy) }
    ) { if (it) 0.9f else 1f }

    val elevation by transition.animateDp(label = "fab_elevation") { if (it) 2.dp else 6.dp }

    FloatingActionButton(
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = elevation),
        modifier = Modifier.scale(scale).pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                },
            )
        },
    ) {
        AppIcon(Icons.Default.Add, contentDescription = "Add")
    }
}
```

---

## Shared element transitions (Compose 1.7+)

```kotlin
// List item — source
@Composable
fun ProductListItem(product: Product, onClick: () -> Unit) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: error("No SharedTransitionScope")
    val animatedContentScope = LocalNavAnimatedContentScope.current
        ?: error("No AnimatedContentScope")

    with(sharedTransitionScope) {
        Card(onClick = onClick) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .sharedElement(
                        state = rememberSharedContentState(key = "product_image_${product.id}"),
                        animatedVisibilityScope = animatedContentScope,
                    ),
            )
            Text(product.name)
        }
    }
}

// Detail screen — destination
@Composable
fun ProductDetailContent(product: Product) {
    with(LocalSharedTransitionScope.current!!) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .sharedElement(
                    state = rememberSharedContentState(key = "product_image_${product.id}"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current!!,
                ),
        )
    }
}
```

NavHost must be wrapped in `SharedTransitionLayout`:

```kotlin
SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
        NavHost(...) { /* routes */ }
    }
}
```

---

## Reduced-motion accessibility

```kotlin
@Composable
fun respectsReducedMotion(): Boolean =
    LocalAccessibilityManager.current?.isReduceMotionEnabled == true

@Composable
fun AnimatedVisibilityOrInstant(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    if (respectsReducedMotion()) {
        if (visible) content()
    } else {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            content()
        }
    }
}
```

---

## Testing

```kotlin
@get:Rule val composeRule = createComposeRule()

@Test fun `animated visibility shows content when visible`() {
    var visible by mutableStateOf(false)
    composeRule.setContent {
        AnimatedVisibility(visible = visible) {
            Text("Hello", modifier = Modifier.testTag("content"))
        }
    }
    composeRule.onNodeWithTag("content").assertDoesNotExist()
    visible = true
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("content").assertExists()
}

@Test fun `crossfade renders target state after transition`() {
    var state by mutableStateOf("A")
    composeRule.setContent {
        Crossfade(targetState = state) { s ->
            Text(s, modifier = Modifier.testTag("text"))
        }
    }
    composeRule.onNodeWithTag("text").assertTextEquals("A")
    state = "B"
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("text").assertTextEquals("B")
}

// Roborazzi — capture final (animated-in) visual state
@Test fun `animated_content_visible_screenshot`() {
    captureRoboImage("animation_visible_state.png") {
        AppTheme {
            var visible by remember { mutableStateOf(true) }
            AnimatedVisibility(visible = visible) {
                Box(Modifier.size(80.dp).background(AppTheme.colors.primary))
            }
        }
    }
}
```

---

## Common Anti-Patterns

- **Animating inside a `LazyColumn` item without a `key`** — without stable keys Compose
  recycles items and restarts animations mid-flight; always set `key = { item.id }` in
  `LazyColumn`
- **`animateContentSize()` on a `LazyColumn`** — `LazyColumn` manages its own height;
  `animateContentSize` only works on layout containers whose content fits in memory
- **Using `Crossfade` for navigation-level transitions** — Crossfade is for in-place
  state swaps; use NavHost's `enterTransition`/`exitTransition` params for screen-level nav
- **Forgetting `label` on `animateXAsState`** — the `label` is used in Android Studio's
  Animation Preview; always provide a descriptive label
- **Shared element with a missing `SharedTransitionLayout` wrapper** — `sharedElement`
  crashes if no `SharedTransitionScope` is in the composition tree; wrap the NavHost
- **No reduced-motion fallback** — users with vestibular disorders can enable "Reduce Motion"
  in system settings; check `isReduceMotionEnabled` before adding non-trivial animations

---

## Related Skills

- `kotlin-multiplatform-design-system` — animation durations and easing curves should be
  tokens in the design system (e.g., `AppTheme.motion.standard`), not hardcoded `tween(300)`
- `kotlin-multiplatform-accessibility` — reduced-motion support is an a11y requirement;
  check `LocalAccessibilityManager` before applying non-trivial animations
- `kotlin-multiplatform-navigation` — shared element transitions require the NavHost to be
  wrapped in `SharedTransitionLayout`; see the navigation skill for the full NavHost setup

---

## Output Style

When implementing animations, respond in this order:
1. **Identify the right API** — `AnimatedVisibility`, `Crossfade`, `animateXAsState`,
   shared element — based on what the UI is doing
2. **Implementation** — composable with the animation applied
3. **Reduced-motion** — `respectsReducedMotion()` check for non-trivial transitions
4. **Anti-pattern note** — call out if the chosen API has a common pitfall in this context

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
