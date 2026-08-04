# Compose Styles API — Official Reference (extracted for auditing)

> **Purpose:** ground truth extracted from the 9 official Android Jetpack Compose docs
> pages on the Styles API, so this skill's generated code (and any PR touching
> `styles/` or `components/`) can be audited against the real API surface without
> re-fetching the docs every time.
>
> **Source pages** (fetched 2026-07-05):
> - https://developer.android.com/develop/ui/compose/styles
> - https://developer.android.com/develop/ui/compose/styles/fundamentals
> - https://developer.android.com/develop/ui/compose/styles/state-animations
> - https://developer.android.com/develop/ui/compose/styles/styles-vs-modifiers
> - https://developer.android.com/develop/ui/compose/styles/theming
> - https://developer.android.com/develop/ui/compose/styles/performance
> - https://developer.android.com/develop/ui/compose/styles/dos-donts
> - https://developer.android.com/develop/ui/compose/styles/examples
> - https://developer.android.com/develop/ui/compose/styles/limitations
>
> **Caveat:** these are Android Jetpack Compose docs (`androidx.compose.foundation.style`,
> `1.12.0-alpha03` at fetch time). Compose Multiplatform (JetBrains) support may lag
> behind or diverge — verify the annotation/package names in your actual CMP version
> before trusting a snippet verbatim. Re-fetch the pages above if this file is more than
> a few CMP releases old.

---

## 1. What problem it solves

- Simplifies state-based styling (hover/press/focus) — declarative, less boilerplate than manual `MutableInteractionSource` + `collectIsPressedAsState()` wiring
- Built-in animated state transitions, better performance than `animate*AsState`
- Replaces multiple styling parameters (`background: Color, padding: Dp, ...`) with one `Style` parameter
- Runs in Layout/Draw phases, **skips Composition** — fewer recompositions
- CSS-like standardized styleable property set

**Not a replacement for Modifiers.** Styles replace *declarative styling parameters*;
Modifiers remain for layout, gestures, and behavior. Internally, a `Style` **is** a
specialized Modifier — everything a Style can do, a Modifier could also do, but not
the reverse.

**Status:** `@Experimental`, likely to change. Material Design support for Styles is
planned but **not yet available** — use Styles on custom (non-Material) components today.

---

## 2. Core types

| Type | Role |
|---|---|
| `Style` | Interface describing visual appearance. Properties are **not additive** — setting the same property twice keeps only the last value (CSS-cascade semantics). |
| `StyleScope` | Receiver of the `Style { }` lambda. Provides the styling functions (`background()`, `contentPadding()`, …) and access to `state` for conditional styling. |
| `StyleState` / `MutableStyleState` | Tracks interaction state (`isEnabled`, `isPressed`, `isHovered`, `isFocused`, `isSelected`, custom keys). |
| `StyleStateKey<T>` | Declares a custom state slot beyond the built-ins. |

---

## 3. Available style properties (StyleScope)

| Category | Properties |
|---|---|
| Content (inner) padding | `contentPadding()`, `contentPaddingHorizontal()`, `contentPaddingVertical()`, directional (`contentPaddingStart()`, `contentPaddingTop()`, …) |
| External (outer) padding | `externalPadding()` + horizontal/vertical/directional variants |
| Dimensions | `fillWidth()`, `fillHeight()`, `fillSize()`, `width`, `height`, `size` (Dp, DpSize, or Float) |
| Positioning | `left`, `top`, `right`, `bottom` offsets |
| Fills | `background()`, `foreground()` — `Color` or `Brush` |
| Borders | `borderWidth()`, `borderColor()`, `borderBrush()`, `border(width, color)` shorthand |
| Shape | `shape()` — **custom `Shape` implementations not yet supported** (see Limitations) |
| Shadows | `dropShadow()`, `innerShadow()` (takes a `Shadow` value: offset, radius, spread, color) |
| Transforms | `translationX/Y`, `scaleX/Y`, `rotationX/Y/Z`, `alpha`, `zIndex`, `transformOrigin` |
| Typography (inherits to children) | `textStyle()`, `fontSize()`, `fontWeight()`, `fontStyle()`, `fontFamily()`, `contentColor()`, `contentBrush()`, `lineHeight()`, `letterSpacing()`, `textAlign()`, `textDirection()`, `lineBreak()`, `hyphens()`, `textDecoration()`, `textIndent()`, `baselineShift()` |

---

## 4. Fundamentals

### Three ways to adopt Styles

1. Directly on a component's `style: Style` parameter
2. `Modifier.styleable { ... }` on a layout composable with no built-in style param
3. Custom design-system components built entirely around `Modifier.styleable { }`

### Property override, not additive

```kotlin
Style {
    background(Color.Red)
    background(TealColor)      // wins — overrides Red
    contentPadding(64.dp)
    contentPaddingTop(16.dp)   // overrides only the top inset
}
```

### Merging with `then`

```kotlin
val style1 = Style { background(TealColor) }
val style2 = Style { contentPaddingTop(16.dp) }
val merged = style1 then style2   // right-hand side wins on overlapping properties
```

### Style inheritance priority (highest → lowest)

1. Direct composable argument (`Text(color = Color.Red)`)
2. `style` parameter (`Text(style = Style { contentColor(Color.Red) })`)
3. `Modifier.styleable { }` chain
4. Parent/inherited typography-color properties

### Custom reusable properties

```kotlin
fun StyleScope.outlinedBackground(color: Color) {
    border(1.dp, color)
    background(color)
}
val myStyle = Style { outlinedBackground(Color.Blue) }
```

This composes *existing* primitives into a named group — it does not add a genuinely
new raw property to the DSL (see Limitations §2).

---

## 5. State and animation

### Built-in interaction states

`hovered { }`, `pressed { }`, `focused { }`, `selected { }`, and a queryable
`isEnabled` — plus `toggled { }` per the API overview. States **nest**:

```kotlin
Style {
    background(Color.White)
    hovered {
        background(lightPurple)
        pressed { background(lightOrange) }   // hover AND press simultaneously
    }
    pressed { background(lightRed) }          // press without hover
    focused { background(lightBlue) }
}
```

### Animating a state transition

```kotlin
Style {
    border(3.dp, Color.Black)
    pressed {
        animate { borderColor(Color.Magenta); background(Color(0xFFB39DDB)) }
        animate(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { scale(1.2f) }
    }
}
```

`animate { }` defaults to a standard spec; pass an explicit `AnimationSpec` (`tween(...)`,
`spring(...)`) as `animate(spec) { }` for custom timing.

### The sanctioned StyleState construction pattern

```kotlin
@Composable
private fun GradientButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    style: Style = Style,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled       // ← property is `isEnabled`, not `enabled`
    }
    Row(
        modifier = Modifier
            .clickable(onClick = onClick, enabled = enabled,
                interactionSource = interactionSource, indication = null)
            .styleable(styleState, baseStyle then style),
    ) { /* content */ }
}
```

`rememberUpdatedStyleState` keeps the state current across recomposition without
recreating it — prefer this over `remember(interactionSource) { MutableStyleState(interactionSource) }`
followed by manual field mutation.

### Custom states

```kotlin
enum class PlayerState { Stopped, Playing, Paused }
val playerStateKey = StyleStateKey(PlayerState.Stopped)

var MutableStyleState.playerState
    get() = this[playerStateKey]
    set(value) { this[playerStateKey] = value }

fun StyleScope.playerPlaying(block: () -> Unit) =
    state(playerStateKey, block) { key, state -> state[key] == PlayerState.Playing }
```

---

## 6. Styles vs Modifiers — decision table

| | Modifiers | Styles |
|---|---|---|
| Goal | Behavior, semantics, layout | Visual appearance, themeable properties |
| Combine logic | **Additive** (stack) | **Overwrite** (last write wins) |
| Theming | Hard to lift into a theme | Designed to be lifted into a theme; `CompositionLocal`-aware |
| Phases touched | Composition + Layout + Draw | Layout + Draw only (skips Composition) |
| Animation | Needs `animate*AsState` primitives | Built-in `animate { }` |

**Choose Styles when:** overriding a component default, doing high-performance/frequent
visual animation, or defining a theme-wide property set.
**Choose Modifiers when:** adding behavior (clickable, gestures), a one-off unique
layout, or a property that must be additive.

**Styles cannot:** handle click logic, gesture detection, or accessibility semantics —
those remain on Modifiers regardless of how much visual configuration moves to Styles.

---

## 7. Theming integration

- Build a theme object (`@Immutable class`) holding `colors`/`typography`/`shapes`,
  exposed via a `staticCompositionLocalOf`
- Define component styles as `Style` values on a theme-owned object (e.g.
  `JetsnackStyles.buttonStyle`)
- Expose `StyleScope` extension **properties** (not direct `@Composable` reads) for
  every themed value your Styles need: `val StyleScope.colors get() = LocalTheme.currentValue.colors`
- A custom design system can still wrap `MaterialTheme` underneath for typography/shapes
  while providing its own color/Style layer on top (hybrid approach) — Material-native
  Style support is not required for this
- Break large component styles into **atomic** single-purpose styles and compose with
  `then` — improves reuse and readability without changing behavior

---

## 8. Performance

Benchmarked on Compose `1.11.0-alpha06`:

| Test | Time change | Allocation change |
|---|---|---|
| Toggle a `Box` border color | **-59.91%** | **-77.22%** |
| Style-based hover/focus/press vs. manual state | -5.24% | -14.72% |
| Initial composition, 5 chained modifiers vs. 1 Style | -4.78% | -6.60% |
| 5 `BasicText` with hardcoded strings | +0.62% | +2.41% |
| Text color via Style vs. `CompositionLocalProvider` | +5.86% | +9.82% |

**Why:** Styles skip the Composition phase (only Layout/Draw invalidate on change);
animation resources are lazily allocated on first use, not at initial composition;
a Style defined once in a theme is a single shared lambda across every component
instance using it, instead of one allocated Modifier chain per instance.

**Best fit:** frequent property updates (border/color toggles), animated values,
themed components reused across many instances, and interaction-state styling.

---

## 9. Do's and Don'ts (verbatim rules)

### Do

1. Use Styles for visuals, Modifiers for behaviors — don't mix concerns
2. Expose a `style: Style = Style` parameter on every design-system component
3. Replace visual parameters (`background: Color, fontColor: Color`) with a single `style: Style`
4. Prioritize Styles over Modifiers for animated properties
5. Rely on last-write-wins to override a default border/background without extra parameters
6. **Merge defaults inside the composable**, never as the parameter default:
   ```kotlin
   fun GoodButton(style: Style = Style) {   // ✓ empty default
       val defaultStyle = Style { background(Color.Red) }
       Box(Modifier.styleable(styleState, defaultStyle, style)) { }
   }
   ```
7. Create `StyleScope` extension properties for themed `CompositionLocal` access — never read the `CompositionLocal` directly inside a Style-returning `@Composable` function
8. Create ONE style that queries themed `CompositionLocal` values dynamically for light/dark switching — don't hand-roll two Style objects
9. Swap whole Style objects only when many properties genuinely differ across brand variants (white-label apps)

### Don't

1. Put interaction/business logic (click handling, gesture detection) inside a Style
2. Provide a default **with a body**: `style: Style = Style { background(Color.Red) }` — always `style: Style = Style` (empty)
3. Add a `style: Style` parameter to a layout-level or screen-level composable — Styles are for components
4. Read a `CompositionLocal` inside a `@Composable fun somethingStyle(): Style { ... }` and return the built `Style` — the value is captured at definition time and goes stale:
   ```kotlin
   // ❌ WRONG — captured once, never updates when the theme changes
   @Composable
   fun containerStyle(): Style {
       val background = MaterialTheme.colorScheme.background
       return Style { background(background) }
   }
   ```

---

## 10. Known limitations (as of the fetched docs)

| Limitation | Detail | Workaround |
|---|---|---|
| No infinite animations | Styles cannot define unbounded/looping animations | Use `rememberInfiniteTransition()` in the component body |
| No custom style properties beyond the standard set | You can compose existing primitives into named extension functions, but cannot add a genuinely new raw property to the DSL | N/A — use a Modifier for anything outside the standard property set |
| Custom shapes unsupported | `shape()` does not yet accept arbitrary custom `Shape` implementations | Tracked for a future release; use built-in shapes for now |
| Shape animation unsupported | Animating between shapes is not yet possible | N/A |
| No `themes.xml`/`styles.xml` interop | View-system style resources cannot feed into Compose Styles | **Permanent** — will never be supported |
| Ripple/indication double-effect | Using `pressed { }` without `indication = null` on the same `clickable` shows both the Style animation AND the default ripple | Always set `indication = null` on `clickable`/`combinedClickable` when a Style handles pressed-state visuals |
| Material support | Material Design components do not yet support `Style` parameters | Planned for a future release; use Styles on custom (non-Material) components today |

Report unsupported use cases: https://issuetracker.google.com/issues/new?component=612128
