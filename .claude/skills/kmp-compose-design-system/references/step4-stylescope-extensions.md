# Step 4: StyleScope Extensions

Part of `kmp-compose-design-system`. Load this file when working on: step 4: stylescope extensions.

---

These are the **only** correct way to read `CompositionLocal` values inside a `Style`. Styles run outside Composition, so you cannot call `AppTheme.LocalAppTheme.current` directly.

### `theme/StyleScopeExtensions.kt`

```kotlin
package GROUP_ID.core.designsystem.theme

import androidx.compose.foundation.style.StyleScope
import androidx.compose.ui.ExperimentalComposeUiApi
import GROUP_ID.core.designsystem.tokens.AppColors
import GROUP_ID.core.designsystem.tokens.AppShapes
import GROUP_ID.core.designsystem.tokens.AppSpacing
import GROUP_ID.core.designsystem.tokens.AppTypography

// Note: @ExperimentalStylesApi — check actual annotation in your CMP version.
// In CMP 1.11.x this may be @OptIn(ExperimentalStylesApi::class)

val StyleScope.appTheme: AppTheme
    get() = AppTheme.LocalAppTheme.currentValue

val StyleScope.colors: AppColors
    get() = AppTheme.LocalAppTheme.currentValue.colors

val StyleScope.typography: AppTypography
    get() = AppTheme.LocalAppTheme.currentValue.typography

val StyleScope.shapes: AppShapes
    get() = AppTheme.LocalAppTheme.currentValue.shapes

val StyleScope.spacing: AppSpacing
    get() = AppTheme.LocalAppTheme.currentValue.spacing
```

> **Critical rule**: Never capture token values before the Style block:
> ```kotlin
> // ❌ WRONG — stale at creation time
> val color = AppTheme.LocalAppTheme.current.colors.primary
> val myStyle = Style { background(color) }
>
> // ✅ CORRECT — read at consume time via StyleScope extension
> val myStyle = Style { background(colors.primary) }
> ```

### `theme/RememberStyle.kt` — memoized variant resolution

> **Hard Rule**: Every sealed variant interface (`ButtonVariant`, `BadgeVariant`,
> `CardVariant`, `ChipVariant`, `TextFieldVariant`, and their `*Size` counterparts)
> extends the common `StyleVariant` marker below. Variant objects stay flat and
> stateless — `val style: Style`, no hardcoded/pre-resolved `Color`/`Dp` literals, no
> mutable state. Resolve a variant to its `Style` at the call site with `rememberStyle()`,
> never `variant.style then size.style` inlined directly in the modifier chain — that
> rebuilds the merged descriptor on every recomposition instead of once per variant/size
> change.

```kotlin
package GROUP_ID.core.designsystem.theme

import androidx.compose.foundation.style.ExperimentalStylesApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@OptIn(ExperimentalStylesApi::class)
sealed interface StyleVariant {
    val style: Style
}

/**
 * Resolves one or more [StyleVariant]s (variant, size, ...) into a single merged
 * [Style], memoized on the variants themselves so the `then` chain is rebuilt only
 * when a variant or size actually changes — not on every recomposition.
 *
 * Usage: `val resolved = rememberStyle(variant, size)`
 */
@OptIn(ExperimentalStylesApi::class)
@Composable
fun rememberStyle(vararg variants: StyleVariant): Style =
    remember(variants.toList()) {
        variants.map { it.style }.reduce { acc, style -> acc then style }
    }
```

`StyleVariant` is a marker interface, not a base class with defaults — each variant
object still declares its own `override val style` exactly as in Step 5 below. The only
change is `sealed interface ButtonVariant : StyleVariant { ... }` instead of
`sealed interface ButtonVariant { ... }`, so `rememberStyle()` accepts it.

### Custom context-aware modifiers (outside the Style system)

For a one-off `Modifier` extension that needs a theme default but isn't a full variant
system (a divider color, a shimmer tint, a custom draw effect) — fetch the default
**inside** the modifier via `Modifier.composed { }`, never as a caller-supplied parameter
with a hardcoded fallback:

```kotlin
// ❌ WRONG — forces every call site to know or hardcode the color
fun Modifier.appDivider(color: Color = Color(0xFFE4E4E7)): Modifier =
    drawBehind { drawLine(color, ...) }

// ✅ CORRECT — reads the live theme internally; call site stays parameter-free
fun Modifier.appDivider(): Modifier = composed {
    val color = AppTheme.LocalAppTheme.current.colors.border
    drawBehind { drawLine(color, ...) }
}
```

Call sites stay clean: `Modifier.appDivider()`, no color threading. Reserve an explicit
`color: Color?` override parameter only for genuine one-off escape hatches, and default it
to `null` (resolve internally when unset) — never to a hardcoded literal.

---

