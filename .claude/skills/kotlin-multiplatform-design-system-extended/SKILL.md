---
name: kotlin-multiplatform-design-system-extended
description: >
  Extends :core:designsystem (from kotlin-multiplatform-design-system) with 26
  production-ready components using the Compose Styles API. Covers: Icon, IconButton,
  Label, Separator, Avatar, TopAppBar, NavigationBar, Tabs, Checkbox, RadioButton,
  Switch, Slider, Select/Dropdown, Progress (linear + circular), Skeleton, Spinner,
  Alert, Toast/Snackbar system (AppToastHostState + Scaffold slot), Dialog, AlertDialog,
  Sheet (BottomSheet), Tooltip, Popover, Accordion/Collapsible. All components built
  on CMP primitives (no Material3). "App" is a placeholder prefix — see the base skill's
  Step 0 for how it is resolved from the project name (scripts/derive_component_prefix.py).
  Requires kotlin-multiplatform-design-system skill.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-07-05'
  keywords:
    - design system extended
    - Dialog
    - BottomSheet
    - Toast
    - Snackbar
    - Tabs
    - TopAppBar
    - NavigationBar
    - Checkbox
    - Switch
    - Slider
    - Select
    - Dropdown
    - Progress
    - Skeleton
    - Spinner
    - Tooltip
    - Popover
    - Accordion
    - Collapsible
    - Avatar
    - Separator
    - Icon
    - Compose Styles API
    - CMP
    - no Material
---

## When to Use This Skill

Use **after** `kotlin-multiplatform-design-system` has been applied. Use when the user asks to:
- Add Dialog, BottomSheet, Toast/Snackbar, Tabs, TopAppBar, or BottomNav
- Add form controls: Checkbox, RadioButton, Switch, Slider, Select/Dropdown
- Add loading states: Progress, Skeleton, Spinner
- Add Tooltip, Popover, Accordion/Collapsible, Avatar, Separator
- Complete the design system for production use

**Trigger keywords:** dialog, bottom sheet, toast, snackbar, tabs, top app bar,
bottom navigation, checkbox, radio, switch, slider, select, dropdown, progress bar,
loading, skeleton, spinner, tooltip, popover, accordion, collapsible, avatar,
divider, separator, icon button, form label, extended design system,
redesign, visual consistency, UI components, component library, page components,
add components, component set, UI kit, component design, redesign page,
button, dialog, component, use component, add button, create component,
show dialog, show toast, loading state, empty state, error state,
circular progress, progress ring, determinate progress, indeterminate progress.

**Freshness rule:** `@ExperimentalStylesApi` and CMP primitive APIs change between releases —
recheck the Compose docs and apply the same freshness check as `kotlin-multiplatform-design-system`.

---

## Recommendation First

Default to **using a pre-built extended component before building a custom one**.

Why:
- all 27 components are built on CMP primitives with no Material dependency — they are safe to
  use alongside the base design system
- they follow the same sealed variant pattern as the core components, so the token layer stays consistent
- building a custom component takes longer and may drift from the design system tokens

Only build a custom component when none of the 27 extended components fit the design requirement,
and apply the same `@ExperimentalStylesApi` token pattern as the core system.

---

## Prerequisites

- `kotlin-multiplatform-design-system` skill already applied (tokens, AppTheme, StyleScopeExtensions, 6 core components present)
- `:core:designsystem` module exists with `GROUP_ID.core.designsystem` package
- The project's component prefix already resolved via the base skill's **Step 0** —
  `App` below is the same placeholder token (`AppIconButton` → `GuildBaseIconButton`,
  `AppToastHost` → `GuildBaseToastHost`, etc.), never a hardcoded literal

---

## Ownership Model

> **Skill-owned.** All extended components are updateable via `/kmm-update-design-system`.
> Project-owned files (`tokens/`, `theme/`) are never touched.

## Component Overview

| Group | Components | Stability |
|---|---|---|
| Primitives | `AppIcon`, `AppIconButton`, `AppLabel`, `AppSeparator` | **Stable** |
| Display | `AppAvatar`, `AppSpinner`, `AppSkeleton`, `AppProgress`, `AppCircularProgress` | **Stable** |
| Navigation | `AppTopAppBar`, `AppNavigationBar`, `AppScaffold` | **Stable** |
| Tabs | `AppTabs` | **Stable** |
| Form Controls | `AppCheckbox`, `AppRadioButton`, `AppSwitch`, `AppSlider` | **Stable** |
| Form Controls | `AppSelect` | **Experimental** — API may change |
| Feedback | `AppAlert`, `AppToastHost` (with `AppToastHostState` + `AppScaffold`) | **Stable** |
| Overlays | `AppDialog`, `AppAlertDialog`, `AppSheet` | **Stable** |
| Overlays | `AppTooltip`, `AppPopover` | **Experimental** — positioning varies by platform |
| Expandable | `AppAccordion` | **Experimental** — animation API in flux |

**Stability tiers:**
- **Stable** — API locked; breaking changes come with a migration note in the Changelog.
- **Experimental** — API may change between skill versions; review diffs before accepting updates.

### Style API coverage

Not every component should expose a `style: Style` override — see the base skill's
Component API Placement table. This is the honest per-component status so the audit
doesn't flag correctly-exempt components as gaps:

| Component | Style API status | Why |
|---|---|---|
| `AppIconButton` | ✅ Wired | Interactive leaf control — `rememberUpdatedStyleState` + `styleable` |
| `AppAvatar` | ✅ Wired | Static leaf control — `style` escape hatch for one-off overrides (e.g. status ring) |
| `AppIcon`, `AppLabel`, `AppSeparator` | ⚠️ Not yet wired | Simple data+param leaf controls; a `style` escape hatch would still be valid — candidates for a future pass |
| `AppSpinner` | ✅ Correctly exempt | Infinite rotation animation — Styles API does not support infinite animations (see `references/compose-styles-api-reference.md` §10); uses `rememberInfiniteTransition` instead, as documented in its own docstring |
| `AppSkeleton`, `AppProgress` | ⚠️ Not yet wired | Same infinite-animation constraint may apply to the shimmer/indeterminate variants — verify per-variant before wiring |
| `AppCheckbox`, `AppRadioButton`, `AppSwitch` | ⚠️ Not yet wired | Custom Canvas-drawn glyphs (checkmark, dot, thumb) sit outside the Style property set (no arbitrary path-drawing property) — per Styles-vs-Modifiers guidance this is legitimately Modifier/Canvas territory; only the container chrome (background/border color per checked/enabled state) is a real Style candidate, not yet extracted |
| `AppSlider` | ✅ Correctly exempt | Continuous drag value, not a discrete interaction state — doesn't fit the StyleState model |
| `AppSelect` | ⚠️ Not yet wired | Leaf control candidate — dropdown chrome (background/border/shape) is stylable |
| `AppTopAppBar`, `AppNavigationBar`, `AppScaffold`, `AppTabs` | ✅ Correctly exempt | Slot API / app-shell chrome per the base skill's Component API Placement table — caller owns content, shell stays fixed |
| `AppAlert`, `AppToastHost` | ⚠️ Not yet wired | Variant-driven leaf controls (info/success/warning/error) — good Style candidates |
| `AppCircularProgress` | ⚠️ Not yet wired | Same status as its sibling `AppProgress` (linear) — track/fill colors are plain params, not yet Style-driven |
| `AppDialog`, `AppAlertDialog`, `AppSheet`, `AppTooltip`, `AppPopover` | ✅ Correctly exempt | Slot API — overlay chrome, not a themed variant leaf control |
| `AppAccordion` | ⚠️ Not yet wired | Animation API is still in flux (Experimental tier); revisit once stabilized |

**Reading this table:** ✅ means the current state is correct and should not be flagged.
⚠️ means a real gap — a future pass should extract the container chrome (background,
border, shape) into a `Style` value and expose a `style: Style = Style` parameter,
following the `AppAvatar` pattern above for static components or the base skill's
`AppButton` pattern (`rememberUpdatedStyleState` + custom state keys) for interactive ones.

---

## Step 1: Add styles for new components

### `styles/CheckboxStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

sealed interface CheckboxVariant {
    val checkedStyle: Style
    val uncheckedStyle: Style

    data object Default : CheckboxVariant {
        override val checkedStyle = Style {
            background(colors.primary)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.sm))
            contentColor(colors.onPrimary)
        }
        override val uncheckedStyle = Style {
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.sm))
            hovered { animate { borderColor(colors.onSurface) } }
        }
    }

    data object Destructive : CheckboxVariant {
        override val checkedStyle = Style {
            background(colors.destructive)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.sm))
            contentColor(colors.onDestructive)
        }
        override val uncheckedStyle = Default.uncheckedStyle
    }
}
```

### `styles/SwitchStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.style.Style
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

data class SwitchColors(
    val trackChecked: androidx.compose.ui.graphics.Color,
    val trackUnchecked: androidx.compose.ui.graphics.Color,
    val thumb: androidx.compose.ui.graphics.Color,
)

object SwitchDefaults {
    @Composable
    fun colors() = SwitchColors(
        trackChecked   = AppTheme.LocalAppTheme.current.colors.primary,
        trackUnchecked = AppTheme.LocalAppTheme.current.colors.border,
        thumb          = AppTheme.LocalAppTheme.current.colors.background,
    )
}
```

### `styles/TabStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.style.Style
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.spacing

data class TabColors(
    val indicator: androidx.compose.ui.graphics.Color,
    val selected: androidx.compose.ui.graphics.Color,
    val unselected: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
)

object TabDefaults {
    @Composable
    fun colors() = TabColors(
        indicator  = AppTheme.LocalAppTheme.current.colors.primary,
        selected   = AppTheme.LocalAppTheme.current.colors.onSurface,
        unselected = AppTheme.LocalAppTheme.current.colors.onSurfaceVariant,
        background = AppTheme.LocalAppTheme.current.colors.background,
    )
}

sealed interface TabVariant {
    data object Line : TabVariant     // underline indicator (default)
    data object Pill : TabVariant     // filled pill indicator
    data object Enclosed : TabVariant // enclosed in a container
}
```

### `styles/AlertStyles.kt`

```kotlin
package GROUP_ID.core.designsystem.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes
import GROUP_ID.core.designsystem.theme.spacing

sealed interface AlertVariant {
    val style: Style

    data object Default : AlertVariant {
        override val style = Style {
            background(colors.surface)
            contentColor(colors.onSurface)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Destructive : AlertVariant {
        override val style = Style {
            background(colors.destructive)
            contentColor(colors.onDestructive)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Warning : AlertVariant {
        override val style = Style {
            background(colors.warning)
            contentColor(colors.onStatus)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }

    data object Success : AlertVariant {
        override val style = Style {
            background(colors.success)
            contentColor(colors.onStatus)
            borderWidth(0.dp)
            shape(RoundedCornerShape(shapes.lg))
            padding(all = spacing.lg)
        }
    }
}
```

---

## Step 2: Primitive components

### `components/AppIcon.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

sealed interface IconSize {
    val dp: Dp
    data object Xs  : IconSize { override val dp = 12.dp }
    data object Sm  : IconSize { override val dp = 16.dp }
    data object Md  : IconSize { override val dp = 20.dp }
    data object Lg  : IconSize { override val dp = 24.dp }
    data object Xl  : IconSize { override val dp = 32.dp }
}

/**
 * Usage:
 * ```
 * AppIcon(Icons.Default.Search)
 * AppIcon(Icons.Default.Close, size = IconSize.Sm, tint = colors.destructive)
 * ```
 */
@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.Md,
    tint: Color = Color.Unspecified,
) {
    val resolvedTint = if (tint == Color.Unspecified) appTheme.colors.onSurface else tint
    androidx.compose.foundation.Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier.size(size.dp),
        colorFilter = ColorFilter.tint(resolvedTint),
    )
}
```

### `components/AppIconButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.ButtonVariant

/**
 * Icon-only button. Defaults to Ghost variant.
 *
 * Usage:
 * ```
 * AppIconButton(onClick = { navBack() }) {
 *     AppIcon(Icons.Default.ArrowBack, contentDescription = "Back")
 * }
 * AppIconButton(onClick = { delete() }, variant = ButtonVariant.Destructive) {
 *     AppIcon(Icons.Default.Delete, contentDescription = "Delete")
 * }
 * ```
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Ghost,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .styleable(styleState, variant.style, style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

### `components/AppLabel.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Form field label with optional required indicator.
 *
 * Usage:
 * ```
 * AppLabel(text = "Email address", required = true)
 * AppLabel(text = "Bio")
 * ```
 */
@Composable
fun AppLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AppText(
            text = text,
            style = AppTextStyle.LabelLarge,
            muted = !enabled,
        )
        if (required) {
            Spacer(Modifier.width(2.dp))
            AppText(
                text = "*",
                style = AppTextStyle.LabelLarge,
                color = AppTheme.LocalAppTheme.current.colors.destructive,
            )
        }
    }
}
```

### `components/AppSeparator.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppSeparator()                           // horizontal, full width
 * AppSeparator(vertical = true)            // vertical, full height
 * AppSeparator(thickness = 2.dp)
 * ```
 */
@Composable
fun AppSeparator(
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    thickness: Dp = 1.dp,
    color: Color = appTheme.colors.border,
) {
    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .then(
                if (vertical) Modifier.width(thickness).fillMaxHeight()
                else Modifier.height(thickness).fillMaxWidth()
            )
            .background(color),
    )
}
```

### `components/AppAvatar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.colors
import GROUP_ID.core.designsystem.theme.shapes

sealed interface AvatarSize {
    val dp: Dp
    data object Sm  : AvatarSize { override val dp = 32.dp }
    data object Md  : AvatarSize { override val dp = 40.dp }
    data object Lg  : AvatarSize { override val dp = 56.dp }
    data object Xl  : AvatarSize { override val dp = 72.dp }
}

// Default chrome — background + circular shape. Consumers override via the `style`
// escape hatch (e.g. a border for an "online" ring) without touching this file.
private val avatarDefaultStyle = Style {
    background(colors.secondary)
    shape(CircleShape)
}

/**
 * Usage:
 * ```
 * AppAvatar(initials = "RV")
 * AppAvatar(initials = "RV", size = AvatarSize.Lg)
 * AppAvatar(painter = painterResource(Res.drawable.ic_user), contentDescription = "Profile")
 * // One-off override — e.g. an "online" ring:
 * AppAvatar(initials = "RV", style = Style { borderWidth(2.dp); borderColor(Color.Green) })
 * ```
 */
@Composable
fun AppAvatar(
    modifier: Modifier = Modifier,
    initials: String? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    size: AvatarSize = AvatarSize.Md,
    style: Style = Style,        // ← empty; DO NOT set a default Style here
) {
    val theme = appTheme
    val styleState = remember { MutableStyleState() }   // static — no interaction to track
    Box(
        modifier = modifier
            .size(size.dp)
            .styleable(styleState, avatarDefaultStyle, style),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(size.dp),
            )
        } else if (initials != null) {
            AppText(
                text = initials.take(2).uppercase(),
                style = if (size.dp >= 56.dp) AppTextStyle.TitleSmall else AppTextStyle.LabelLarge,
                color = theme.colors.onSecondary,
            )
        }
    }
}
```

---

## Step 3: Loading state components

### `components/AppSpinner.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

sealed interface SpinnerSize {
    val dp: Dp
    val stroke: Float
    data object Sm : SpinnerSize { override val dp = 16.dp; override val stroke = 2f }
    data object Md : SpinnerSize { override val dp = 24.dp; override val stroke = 2.5f }
    data object Lg : SpinnerSize { override val dp = 32.dp; override val stroke = 3f }
}

/**
 * Circular indeterminate spinner. Uses rememberInfiniteTransition (Styles API
 * doesn't support infinite animations).
 *
 * Usage:
 * ```
 * AppSpinner()
 * AppSpinner(size = SpinnerSize.Sm, color = colors.onPrimary)
 * ```
 */
@Composable
fun AppSpinner(
    modifier: Modifier = Modifier,
    size: SpinnerSize = SpinnerSize.Md,
    color: Color = appTheme.colors.primary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinnerRotation",
    )

    Canvas(modifier = modifier.size(size.dp)) {
        val padding = size.stroke / 2
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = size.stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = size.stroke, cap = StrokeCap.Round),
        )
    }
}
```

### `components/AppProgress.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Linear progress bar. Pass null for indeterminate.
 *
 * Usage:
 * ```
 * AppProgress(progress = 0.75f)                // 75% filled
 * AppProgress(progress = null)                 // indeterminate — sweeping bar animation
 * AppProgress(progress = 0.5f, height = 8.dp)
 * ```
 */
@Composable
fun AppProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color = appTheme.colors.primary,
    trackColor: Color = appTheme.colors.secondary,
) {
    if (progress == null) {
        // Indeterminate — animate a sweeping bar
        val infiniteTransition = rememberInfiniteTransition(label = "progress")
        val offsetFraction by infiniteTransition.animateFloat(
            initialValue = -0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Restart,
            ),
            label = "progressOffset",
        )
        // Capture the track's pixel width so graphicsLayer can translate across the full track,
        // not just across the inner indicator box (which is only 40% wide).
        var containerWidth by remember { mutableStateOf(0) }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(trackColor)
                .onSizeChanged { containerWidth = it.width },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(height)
                    .clip(CircleShape)
                    .background(color)
                    .graphicsLayer { translationX = containerWidth * offsetFraction },
            )
        }
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "progressValue",
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(height)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
```

### `components/AppCircularProgress.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Circular ring progress indicator. Pass null for indeterminate (continuously rotating
 * arc). The determinate variant is what `AppProgress` (linear) does not cover — use
 * this for compact/dashboard-style progress (upload %, download %, completion rings).
 *
 * Usage:
 * ```
 * AppCircularProgress(progress = 0.75f)                 // 75% ring
 * AppCircularProgress(progress = null)                  // indeterminate — rotating arc
 * AppCircularProgress(progress = 0.5f, size = 48.dp, strokeWidth = 4.dp)
 * ```
 */
@Composable
fun AppCircularProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = appTheme.colors.primary,
    trackColor: Color = appTheme.colors.secondary,
) {
    if (progress == null) {
        // Indeterminate — rotating partial arc, same "infinite -> rememberInfiniteTransition"
        // rule as AppSpinner: Styles API does not support infinite animations.
        val infiniteTransition = rememberInfiniteTransition(label = "circularProgress")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "circularProgressRotation",
        )
        Canvas(modifier = modifier.size(size)) {
            rotate(rotation) {
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "circularProgressValue",
        )
        Canvas(modifier = modifier.size(size)) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
```

### `components/AppSkeleton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Pulsing skeleton placeholder for loading states.
 *
 * Usage:
 * ```
 * AppSkeleton(Modifier.fillMaxWidth().height(20.dp))
 * AppSkeleton(Modifier.size(40.dp).clip(CircleShape))  // avatar skeleton
 * ```
 */
@Composable
fun AppSkeleton(
    modifier: Modifier = Modifier,
    baseColor: Color = appTheme.colors.secondary,
    highlightColor: Color = appTheme.colors.surfaceVariant,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(baseColor.copy(alpha = alpha)),
    )
}
```

---

## Step 4: Navigation components

### `components/AppTopAppBar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppTopAppBar(
 *     title = "Settings",
 *     navigationIcon = {
 *         AppIconButton(onClick = { navBack() }) {
 *             AppIcon(Icons.Default.ArrowBack, contentDescription = "Back")
 *         }
 *     },
 *     actions = {
 *         AppIconButton(onClick = { openMenu() }) {
 *             AppIcon(Icons.Default.MoreVert, contentDescription = "More")
 *         }
 *     }
 * )
 * ```
 */
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    backgroundColor: Color = appTheme.colors.background,
    contentColor: Color = appTheme.colors.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            navigationIcon()
        } else {
            Spacer(Modifier.padding(start = 12.dp))
        }
        AppText(
            text = title,
            style = AppTextStyle.TitleSmall,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        if (actions != null) {
            actions()
        }
    }
}
```

### `components/AppNavigationBar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

data class NavBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val contentDescription: String? = null,
)

/**
 * Usage:
 * ```
 * val items = listOf(
 *     NavBarItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
 *     NavBarItem("Search", Icons.Outlined.Search),
 *     NavBarItem("Profile", Icons.Outlined.Person, Icons.Filled.Person),
 * )
 * AppNavigationBar(items = items, selectedIndex = currentTab, onItemSelected = { tab = it })
 * ```
 */
@Composable
fun AppNavigationBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = appTheme.colors.background,
) {
    val theme = appTheme
    Column(modifier = modifier.fillMaxWidth().background(backgroundColor)) {
        AppSeparator()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onItemSelected(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AppIcon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.contentDescription ?: item.label,
                        size = IconSize.Md,
                        tint = if (selected) theme.colors.primary else theme.colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    AppText(
                        text = item.label,
                        style = AppTextStyle.LabelSmall,
                        color = if (selected) theme.colors.primary else theme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
```

### `components/AppTabs.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.TabDefaults
import GROUP_ID.core.designsystem.styles.TabVariant
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.shapes

/**
 * Usage:
 * ```
 * val tabs = listOf("Overview", "Activity", "Settings")
 * AppTabs(
 *     tabs = tabs,
 *     selectedIndex = selectedTab,
 *     onTabSelected = { selectedTab = it },
 *     variant = TabVariant.Line,
 * ) { index ->
 *     when (index) {
 *         0 -> OverviewContent()
 *         1 -> ActivityContent()
 *         else -> SettingsContent()
 *     }
 * }
 * ```
 */
@Composable
fun AppTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: TabVariant = TabVariant.Line,
    content: (@Composable (selectedIndex: Int) -> Unit)? = null,
) {
    val theme = appTheme
    val colors = TabDefaults.colors()

    Column(modifier = modifier) {
        // Tab row
        when (variant) {
            TabVariant.Line -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        tabs.forEachIndexed { index, title ->
                            val selected = index == selectedIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onTabSelected(index) },
                                        role = Role.Tab,
                                    )
                                    .padding(bottom = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AppText(
                                    text = title,
                                    style = AppTextStyle.LabelLarge,
                                    color = if (selected) colors.selected else colors.unselected,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            if (selected) colors.indicator else androidx.compose.ui.graphics.Color.Transparent
                                        ),
                                )
                            }
                        }
                    }
                    AppSeparator(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }

            TabVariant.Pill -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.colors.secondary, RoundedCornerShape(theme.shapes.full))
                        .padding(4.dp),
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = index == selectedIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(theme.shapes.full))
                                .background(if (selected) theme.colors.background else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(index) },
                                    role = Role.Tab,
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppText(
                                text = title,
                                style = AppTextStyle.LabelLarge,
                                color = if (selected) colors.selected else colors.unselected,
                            )
                        }
                    }
                }
            }

            TabVariant.Enclosed -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.colors.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = index == selectedIndex
                        Column(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(index) },
                                    role = Role.Tab,
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AppText(
                                text = title,
                                style = AppTextStyle.LabelLarge,
                                color = if (selected) colors.selected else colors.unselected,
                            )
                        }
                    }
                }
            }
        }

        // Content area with crossfade
        if (content != null) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "tabContent",
            ) { index ->
                content(index)
            }
        }
    }
}
```

---

## Step 5: Form controls

### `components/AppCheckbox.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
 * AppCheckbox(checked = isChecked, onCheckedChange = { isChecked = it }, label = "Remember me")
 * ```
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val theme = appTheme
    val interactionSource = remember { MutableInteractionSource() }
    val checkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(150),
        label = "checkAlpha",
    )

    val rowModifier = if (label != null) {
        modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onClick = { onCheckedChange(!checked) },
        )
    } else modifier

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled && label == null,
                    role = Role.Checkbox,
                    onClick = { onCheckedChange(!checked) },
                )
        ) {
            val cornerRadius = 3.dp.toPx()
            if (checked) {
                drawRoundRect(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                )
                // Draw checkmark
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.5f)
                    lineTo(size.width * 0.42f, size.height * 0.72f)
                    lineTo(size.width * 0.78f, size.height * 0.28f)
                }
                drawPath(
                    path = path,
                    color = theme.colors.onPrimary.copy(alpha = checkAlpha),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            } else {
                drawRoundRect(
                    color = Color.Transparent,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                )
                drawRoundRect(
                    color = if (enabled) theme.colors.border else theme.colors.primaryDisabled,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

### `components/AppRadioButton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * Column {
 *     AppRadioButton(selected = selected == "a", onClick = { selected = "a" }, label = "Option A")
 *     AppRadioButton(selected = selected == "b", onClick = { selected = "b" }, label = "Option B")
 * }
 * ```
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val theme = appTheme
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "radioDot",
    )

    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val r = size.minDimension / 2
            drawCircle(
                color = if (enabled) theme.colors.border else theme.colors.primaryDisabled,
                radius = r,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
            )
            if (dotScale > 0f) {
                drawCircle(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    radius = r * 0.5f * dotScale,
                )
                drawCircle(
                    color = if (enabled) theme.colors.primary else theme.colors.primaryDisabled,
                    radius = r,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

### `components/AppSwitch.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.SwitchDefaults
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppSwitch(checked = enabled, onCheckedChange = { enabled = it })
 * AppSwitch(checked = enabled, onCheckedChange = { enabled = it }, label = "Dark mode")
 * ```
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val colors = SwitchDefaults.colors()
    val trackColor = if (checked) colors.trackChecked else colors.trackUnchecked
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "thumbOffset",
    )

    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.Switch,
            onClick = { onCheckedChange(!checked) },
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .clip(CircleShape)
                .background(if (enabled) trackColor else appTheme.colors.primaryDisabled),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = thumbOffset, y = 2.dp)
                    .clip(CircleShape)
                    .background(colors.thumb),
            )
        }
        if (label != null) {
            AppText(text = label, style = AppTextStyle.BodyMedium, muted = !enabled)
        }
    }
}
```

### `components/AppSlider.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import kotlin.math.roundToInt

/**
 * Usage:
 * ```
 * AppSlider(value = volume, onValueChange = { volume = it }, range = 0f..1f)
 * ```
 */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    trackColor: Color = appTheme.colors.secondary,
    progressColor: Color = appTheme.colors.primary,
    thumbColor: Color = appTheme.colors.background,
) {
    val theme = appTheme
    var trackWidth by remember { mutableStateOf(0) }
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val thumbDp = 20.dp

    Box(
        modifier = modifier
            .height(thumbDp)
            .padding(horizontal = thumbDp / 2),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { trackWidth = it.width }
                .height(4.dp)
                .background(trackColor, RoundedCornerShape(2.dp))
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / trackWidth).coerceIn(0f, 1f)
                        onValueChange(range.start + newFraction * (range.endInclusive - range.start))
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(if (enabled) progressColor else theme.colors.primaryDisabled, RoundedCornerShape(2.dp)),
            )
        }

        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(((fraction * trackWidth) - thumbDp.toPx() / 2).roundToInt(), 0) }
                .size(thumbDp)
                .background(
                    color = if (enabled) theme.colors.background else theme.colors.primaryDisabled,
                    shape = CircleShape,
                )
                .then(
                    if (enabled) Modifier
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                val delta = dragAmount / trackWidth
                                val newFraction = (fraction + delta).coerceIn(0f, 1f)
                                onValueChange(range.start + newFraction * (range.endInclusive - range.start))
                            }
                        }
                    else Modifier
                ),
        ) {
            // Inner dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.Center)
                    .background(if (enabled) progressColor else theme.colors.primaryDisabled, CircleShape),
            )
        }
    }
}
```

### `components/AppSelect.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * val options = listOf("Option A", "Option B", "Option C")
 * AppSelect(
 *     options = options,
 *     selected = currentOption,
 *     onSelect = { currentOption = it },
 *     placeholder = "Select an option",
 * )
 * ```
 */
@Composable
fun AppSelect(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select…",
    enabled: Boolean = true,
) {
    val theme = appTheme
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(theme.shapes.md)

    Box(modifier = modifier.zIndex(if (expanded) 1f else 0f)) {
        // Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(theme.colors.background)
                .border(
                    width = if (expanded) 2.dp else 1.dp,
                    color = if (expanded) theme.colors.borderFocus else theme.colors.border,
                    shape = shape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    role = Role.DropdownList,
                    onClick = { expanded = !expanded },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = selected ?: placeholder,
                style = AppTextStyle.BodyMedium,
                color = if (selected != null) theme.colors.onSurface else theme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            // Chevron
            AppText(text = if (expanded) "▲" else "▼", style = AppTextStyle.LabelSmall, muted = true)
        }

        // Dropdown
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(100)) + expandVertically(tween(100)),
            exit = fadeOut(tween(80)) + shrinkVertically(tween(80)),
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, shape)
                    .background(theme.colors.background, shape)
                    .border(1.dp, theme.colors.border, shape)
                    .padding(vertical = 4.dp),
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onSelect(option)
                                    expanded = false
                                },
                            )
                            .background(
                                if (option == selected) theme.colors.secondary else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(
                            text = option,
                            style = AppTextStyle.BodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (option == selected) {
                            AppText(text = "✓", style = AppTextStyle.LabelSmall, color = theme.colors.primary)
                        }
                    }
                }
            }
        }
    }
}
```

---

## Step 6: Feedback components

### `components/AppAlert.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.AlertVariant

/**
 * Usage:
 * ```
 * AppAlert(title = "Heads up!", description = "You can add components to your app.")
 * AppAlert(
 *     variant = AlertVariant.Destructive,
 *     title = "Error",
 *     description = "Your session has expired. Please sign in again.",
 * )
 * AppAlert(
 *     variant = AlertVariant.Warning,
 *     icon = Icons.Default.Warning,
 *     title = "Warning",
 *     description = "This action cannot be undone.",
 * )
 * ```
 */
@Composable
fun AppAlert(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    variant: AlertVariant = AlertVariant.Default,
    style: Style = Style,
) {
    val styleState = remember { MutableStyleState() }

    Row(
        modifier = modifier.styleable(styleState, variant.style, style),
        verticalAlignment = if (description != null) Alignment.Top else Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(imageVector = icon, contentDescription = null, size = IconSize.Md)
            Spacer(Modifier.width(12.dp))
        }
        Column {
            AppText(text = title, style = AppTextStyle.TitleSmall)
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                AppText(text = description, style = AppTextStyle.BodySmall)
            }
        }
    }
}
```

### Toast/Snackbar system

The Toast system uses a `Scaffold` slot — **never a raw `Popup`**. `Popup` in CMP WasmJs positions relative to the parent composable, not the viewport, which causes it to appear in the wrong location. Use `AppScaffold` which renders toast at the correct viewport level.

### `components/AppToast.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import kotlinx.coroutines.delay
import java.util.UUID

enum class AppToastVariant { Default, Destructive, Success, Warning }

data class AppToastData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val variant: AppToastVariant = AppToastVariant.Default,
    val durationMs: Long = 3000L,
)

@Stable
class AppToastHostState {
    val toasts = mutableStateListOf<AppToastData>()

    fun show(
        title: String,
        description: String? = null,
        variant: AppToastVariant = AppToastVariant.Default,
        durationMs: Long = 3000L,
    ) {
        toasts.add(AppToastData(title = title, description = description, variant = variant, durationMs = durationMs))
    }

    fun dismiss(id: String) { toasts.removeAll { it.id == id } }
}

val LocalAppToastHostState = compositionLocalOf { AppToastHostState() }

@Composable
fun AppToastHost(
    toastHostState: AppToastHostState = LocalAppToastHostState.current,
    modifier: Modifier = Modifier,
) {
    val theme = appTheme
    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            toastHostState.toasts.takeLast(3).forEach { toast ->
                var visible by remember(toast.id) { mutableStateOf(true) }

                LaunchedEffect(toast.id) {
                    delay(toast.durationMs)
                    visible = false
                    delay(300)
                    toastHostState.dismiss(toast.id)
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it },
                ) {
                    val (bg, border, content) = when (toast.variant) {
                        AppToastVariant.Default     -> Triple(theme.colors.surface, theme.colors.border, theme.colors.onSurface)
                        AppToastVariant.Destructive -> Triple(theme.colors.destructive, theme.colors.destructive, theme.colors.onDestructive)
                        AppToastVariant.Success     -> Triple(theme.colors.success, theme.colors.success, theme.colors.onStatus)
                        AppToastVariant.Warning     -> Triple(theme.colors.warning, theme.colors.warning, theme.colors.onStatus)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .shadow(8.dp, RoundedCornerShape(theme.shapes.lg))
                            .background(bg, RoundedCornerShape(theme.shapes.lg))
                            .border(1.dp, border, RoundedCornerShape(theme.shapes.lg))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(text = toast.title, style = AppTextStyle.LabelLarge, color = content)
                            if (toast.description != null) {
                                AppText(text = toast.description, style = AppTextStyle.BodySmall, color = content.copy(alpha = 0.8f))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        AppIconButton(onClick = { toastHostState.dismiss(toast.id) }) {
                            AppText(text = "✕", style = AppTextStyle.LabelSmall, color = content)
                        }
                    }
                }
            }
        }
    }
}
```

### `components/AppScaffold.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.theme.AppTheme
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Root scaffold that provides: topBar, bottomBar, AppToastHost.
 * Always use AppScaffold to get correct Toast positioning.
 *
 * Usage:
 * ```
 * val toastState = remember { AppToastHostState() }
 * AppScaffold(
 *     toastHostState = toastState,
 *     topBar = { AppTopAppBar(title = "Home") },
 *     bottomBar = { AppNavigationBar(items, selectedTab) { selectedTab = it } },
 * ) { paddingValues ->
 *     HomeScreen(modifier = Modifier.padding(paddingValues))
 * }
 *
 * // Show a toast from anywhere:
 * val toastState = LocalAppToastHostState.current
 * Button(onClick = { toastState.show("Saved!", variant = AppToastVariant.Success) }) { ... }
 * ```
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    toastHostState: AppToastHostState = remember { AppToastHostState() },
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    CompositionLocalProvider(LocalAppToastHostState provides toastHostState) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (topBar != null) topBar()
                Box(modifier = Modifier.weight(1f)) {
                    content(androidx.compose.foundation.layout.PaddingValues())
                }
                if (bottomBar != null) bottomBar()
            }
            // Toast overlay — rendered last, always on top
            AppToastHost(
                toastHostState = toastHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
```

---

## Step 7: Overlay components

### `components/AppDialog.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * if (showDialog) {
 *     AppDialog(
 *         onDismiss = { showDialog = false },
 *         title = "Edit Profile",
 *         description = "Make changes to your profile here.",
 *         confirmButton = { AppButton(onClick = { save(); showDialog = false }) { AppText("Save") } },
 *         dismissButton = { AppButton(onClick = { showDialog = false }, variant = ButtonVariant.Ghost) { AppText("Cancel") } },
 *     ) {
 *         AppTextField(value = name, onValueChange = { name = it }, placeholder = "Name")
 *     }
 * }
 * ```
 */
@Composable
fun AppDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
) {
    val theme = appTheme
    val shape = RoundedCornerShape(theme.shapes.xxl)

    Dialog(onDismissRequest = onDismiss, properties = properties) {
        Column(
            modifier = modifier
                .widthIn(min = 280.dp, max = 480.dp)
                .shadow(16.dp, shape)
                .background(theme.colors.surface, shape)
                .padding(24.dp),
        ) {
            if (title != null) {
                AppText(text = title, style = AppTextStyle.TitleMedium)
            }
            if (description != null) {
                Spacer(Modifier.height(8.dp))
                AppText(text = description, style = AppTextStyle.BodyMedium, muted = true)
            }
            if (content != null) {
                Spacer(Modifier.height(16.dp))
                content()
            }
            if (confirmButton != null || dismissButton != null) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    dismissButton?.invoke()
                    confirmButton?.invoke()
                }
            }
        }
    }
}

/** Convenience destructive confirmation dialog */
@Composable
fun AppAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    description: String,
    confirmText: String = "Continue",
    dismissText: String = "Cancel",
) {
    AppDialog(
        onDismiss = onDismiss,
        title = title,
        description = description,
        confirmButton = {
            AppButton(onClick = onConfirm, variant = ButtonVariant.Destructive) {
                AppText(confirmText)
            }
        },
        dismissButton = {
            AppButton(onClick = onDismiss, variant = ButtonVariant.Ghost) {
                AppText(dismissText)
            }
        },
    )
}
```

### `components/AppSheet.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Bottom sheet modal.
 *
 * Usage:
 * ```
 * if (showSheet) {
 *     AppSheet(onDismiss = { showSheet = false }, title = "Options") {
 *         AppButton(onClick = { share() }) { AppText("Share") }
 *         AppButton(onClick = { delete() }, variant = ButtonVariant.Destructive) { AppText("Delete") }
 *     }
 * }
 * ```
 */
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val theme = appTheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            // Sheet
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        color = theme.colors.surface,
                        shape = RoundedCornerShape(topStart = theme.shapes.xxl, topEnd = theme.shapes.xxl),
                    )
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 24.dp)
                    .align(Alignment.BottomCenter),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(theme.colors.border, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(16.dp))
                if (title != null) {
                    AppText(
                        text = title,
                        style = AppTextStyle.TitleSmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    content()
                }
            }
        }
    }
}
```

### `components/AppTooltip.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToPx
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Hover tooltip — primarily for Desktop target.
 * On touch (iOS/Android), tooltip is never shown (hover not available).
 *
 * Usage:
 * ```
 * AppTooltip(tooltip = "Delete this item") {
 *     AppIconButton(onClick = { delete() }) {
 *         AppIcon(Icons.Default.Delete, contentDescription = "Delete")
 *     }
 * }
 * ```
 */
@Composable
fun AppTooltip(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = appTheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(modifier = modifier.hoverable(interactionSource)) {
        content()
        if (isHovered) {
            // PopupPositionProvider centres the tooltip horizontally above the anchor.
            // Popup(alignment = ...) is wrong here — Alignment has no import and positions
            // relative to the parent bounds rather than above it.
            val density = LocalDensity.current
            val positionProvider = remember(density) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize,
                    ): IntOffset = IntOffset(
                        x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2,
                        y = anchorBounds.top - popupContentSize.height - with(density) { 4.dp.roundToPx() },
                    )
                }
            }
            Popup(popupPositionProvider = positionProvider) {
                Box(
                    modifier = Modifier
                        .background(
                            color = theme.colors.onSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(theme.shapes.sm),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    AppText(
                        text = tooltip,
                        style = AppTextStyle.BodySmall,
                        color = theme.colors.background,
                    )
                }
            }
        }
    }
}
```

### `components/AppPopover.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.styles.ButtonVariant

/**
 * Click-triggered popover with custom content.
 *
 * Usage:
 * ```
 * AppPopover(
 *     trigger = { expanded ->
 *         AppButton(onClick = { expanded.toggle() }) { AppText("Open") }
 *     }
 * ) {
 *     AppText("Popover content here")
 * }
 * ```
 */
class PopoverState {
    var isOpen by mutableStateOf(false)
        private set
    fun toggle() { isOpen = !isOpen }
    fun open()   { isOpen = true }
    fun close()  { isOpen = false }
}

@Composable
fun AppPopover(
    modifier: Modifier = Modifier,
    trigger: @Composable (state: PopoverState) -> Unit,
    content: @Composable () -> Unit,
) {
    val theme = appTheme
    val shape = RoundedCornerShape(theme.shapes.lg)
    val state = remember { PopoverState() }

    Box(modifier = modifier) {
        trigger(state)
        if (state.isOpen) {
            Popup(onDismissRequest = { state.close() }) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, shape)
                        .background(theme.colors.surface, shape)
                        .border(1.dp, theme.colors.border, shape)
                        .padding(16.dp),
                ) {
                    content()
                }
            }
        }
    }
}
```

---

## Step 8: Expandable components

### `components/AppAccordion.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

data class AccordionItem(
    val title: String,
    val content: @Composable () -> Unit,
)

/**
 * Expandable accordion. Supports single or multiple expanded sections.
 *
 * Usage:
 * ```
 * AppAccordion(
 *     items = listOf(
 *         AccordionItem("What is KMP?") { AppText("Kotlin Multiplatform...") },
 *         AccordionItem("How to install?") { AppText("Add to your gradle...") },
 *     )
 * )
 * ```
 */
@Composable
fun AppAccordion(
    items: List<AccordionItem>,
    modifier: Modifier = Modifier,
    multiExpand: Boolean = false,
) {
    val theme = appTheme
    val expandedIndices = remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isExpanded = index in expandedIndices.value
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(200),
                label = "chevron",
            )

            Column {
                if (index > 0) AppSeparator()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                expandedIndices.value = if (isExpanded) {
                                    expandedIndices.value - index
                                } else {
                                    if (multiExpand) expandedIndices.value + index else setOf(index)
                                }
                            },
                        )
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        text = item.title,
                        style = AppTextStyle.LabelLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    AppText(
                        text = "▼",
                        style = AppTextStyle.LabelSmall,
                        muted = true,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(tween(200)),
                    exit = shrinkVertically(tween(200)),
                ) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        item.content()
                    }
                }
            }
        }
        AppSeparator()
    }
}
```

---

## Step 9: Wire AppScaffold at entry points

Replace existing entry-point `AppTheme` wrappers:

```kotlin
// androidApp/src/main/kotlin/.../MainActivity.kt
setContent {
    AppTheme(darkTheme = isSystemInDarkTheme()) {
        val toastState = remember { AppToastHostState() }
        AppScaffold(toastHostState = toastState) { _ ->
            AppNavHost()
        }
    }
}

// Anywhere in the app — show a toast:
val toastState = LocalAppToastHostState.current
LaunchedEffect(saveResult) {
    if (saveResult.isSuccess) {
        toastState.show("Saved successfully", variant = AppToastVariant.Success)
    }
}
```

---

## Step 10: Usage examples

### Form screen

```kotlin
@Composable
fun ProfileForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(true) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val toast = LocalAppToastHostState.current

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Name", required = true)
            AppTextField(value = name, onValueChange = { name = it }, placeholder = "Your name")
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Email")
            AppTextField(value = email, onValueChange = { email = it }, placeholder = "you@example.com")
        }
        AppSwitch(checked = notifications, onCheckedChange = { notifications = it }, label = "Email notifications")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLabel(text = "Digest frequency")
            AppSelect(
                options = listOf("Daily", "Weekly", "Monthly"),
                selected = frequency,
                onSelect = { frequency = it },
                placeholder = "Select frequency",
            )
        }

        AppProgress(progress = 0.65f)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(onClick = { toast.show("Profile saved", variant = AppToastVariant.Success) }) {
                AppText("Save changes")
            }
            AppButton(onClick = { showDeleteDialog = true }, variant = ButtonVariant.Destructive) {
                AppText("Delete account")
            }
        }
    }

    if (showDeleteDialog) {
        AppAlertDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { deleteAccount() },
            title = "Delete account",
            description = "This action cannot be undone. All your data will be permanently removed.",
            confirmText = "Delete account",
        )
    }
}
```

### Settings page with Accordion

```kotlin
@Composable
fun SettingsPage() {
    AppAccordion(
        items = listOf(
            AccordionItem("Privacy") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppSwitch(checked = true, onCheckedChange = {}, label = "Show profile publicly")
                    AppSwitch(checked = false, onCheckedChange = {}, label = "Allow data analytics")
                }
            },
            AccordionItem("Notifications") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppCheckbox(checked = true, onCheckedChange = {}, label = "Push notifications")
                    AppCheckbox(checked = false, onCheckedChange = {}, label = "Email digest")
                }
            },
            AccordionItem("Appearance") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText("Theme", style = AppTextStyle.LabelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("System", "Light", "Dark").forEach { opt ->
                            AppChip(label = opt, selected = opt == "System", onClick = {})
                        }
                    }
                }
            },
        )
    )
}
```

---

## Guidelines

- **`AppScaffold` is required for Toast** — never place `AppToastHost` inside a `Box` without scaffold-level z-ordering; toasts will be clipped by parent composables
- **Dialog and Sheet use `androidx.compose.ui.window.Dialog`** — works across all CMP targets; `Popup`-based overlays have WasmJs viewport positioning issues
- **Tooltip is Desktop-first** — hover state is only available on Desktop/Web pointer devices; on touch targets the tooltip is simply never shown
- **Slider uses `pointerInput`** not `Modifier.draggable` — `draggable` only tracks one axis and lacks tap-to-seek behavior
- **Checkbox/RadioButton draw their own indicator via `Canvas`** — no dependency on Material icons or drawables
- **Accordion chevron uses `graphicsLayer { rotationZ }`** — runs on the draw layer, skips Composition on rotation
- **Switch thumb uses `animateDpAsState` with spring** — spring physics handles interruptions if toggled mid-animation
- **`AppSelect` uses `AnimatedVisibility` + z-index** — not a `Popup`, so it stacks in document order; use `zIndex` on the parent if other composables need to render on top

---

## Verification

1. `./gradlew :core:designsystem:compileCommonMainKotlinMetadata` — all 27 components compile
2. Show/dismiss `AppDialog` — appears centered, scrim dismisses on outside tap
3. Show `AppSheet` — slides in from bottom, drag handle visible, scrim dismisses
4. `toastState.show("Test")` — toast appears bottom-center, auto-dismisses after 3s
5. `AppTabs` — all 3 variants render, `AnimatedContent` transitions on tab switch
6. `AppCheckbox` + `AppSwitch` — animated state changes work
7. `AppAccordion(multiExpand = false)` — only one section open at a time
8. `AppSelect` — dropdown opens/closes, selected value updates, keyboard accessible
9. Desktop hover on `AppIconButton` inside `AppTooltip` — tooltip appears above
10. `./gradlew :desktopApp:run` — all components render correctly on JVM target

---

## Testing

```kotlin
// Every component in the extended system needs: light + dark screenshots + 1 interaction test
@get:Rule val composeRule = createComposeRule()

// --- AppButton ---
@Test fun `app_button_primary_light screenshot`() {
    captureRoboImage("dsx_button_primary_light.png") {
        AppTheme(darkTheme = false) { AppButton(text = "Continue", onClick = {}) }
    }
}

@Test fun `app_button_primary_dark screenshot`() {
    captureRoboImage("dsx_button_primary_dark.png") {
        AppTheme(darkTheme = true) { AppButton(text = "Continue", onClick = {}) }
    }
}

@Test fun `app_button_fires_onclick`() {
    var clicked = false
    composeRule.setContent { AppTheme { AppButton(text = "Go", onClick = { clicked = true }) } }
    composeRule.onNodeWithText("Go").performClick()
    assertTrue(clicked)
}

// --- AppTextField ---
@Test fun `app_text_field_default screenshot`() {
    captureRoboImage("dsx_text_field_default.png") {
        AppTheme { var t by remember { mutableStateOf("") }; AppTextField(value = t, onValueChange = {}, label = "Email") }
    }
}

@Test fun `app_text_field_onValueChange fires on input`() {
    var value = ""
    composeRule.setContent {
        AppTheme { AppTextField(value = value, onValueChange = { value = it }, label = "Name") }
    }
    composeRule.onNodeWithText("Name").performTextInput("Alice")
    assertEquals("Alice", value)
}

// --- AppIcon / AppIconButton ---
@Test fun `app_icon_button_fires_onclick`() {
    var clicked = false
    composeRule.setContent {
        AppTheme {
            AppIconButton(onClick = { clicked = true }, contentDescription = "Close") {
                AppIcon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
    composeRule.onNodeWithContentDescription("Close").performClick()
    assertTrue(clicked)
}
```

---

## Common Anti-Patterns

- using an extended component before applying `kotlin-multiplatform-design-system` — tokens are missing
- overriding component internals via `Modifier` hacks instead of adding a variant — breaks the style contract
- building a custom sheet or dialog without checking `AppBottomSheet` / `AppDialog` first
- mixing Material3 components with extended design system components — creates token conflicts
- creating an `AppToastHostState` without wiring it into the `AppScaffold` slot — toasts silently do nothing

Check the component list in this skill before building a custom alternative.

---

## Related Skills

- `kotlin-multiplatform-design-system` — the token and component foundation this skill extends
- `kotlin-multiplatform-compose-slot-api` — slot APIs used by `AppDialog`, `AppBottomSheet`, and `AppScaffold`
- `kotlin-multiplatform-preview-driven-development` — Desktop previews for each extended component variant
- `kotlin-multiplatform-shared-resources` — icons and images loaded inside extended components via `Res`

---

## Output Style

When asked about extended design system components, respond in this order:
1. recommendation (which component to use and its variant)
2. code snippet (component with its required props)
3. why that component fits the use case
4. main alternative (build from scratch, use Material3)

Assume `kotlin-multiplatform-design-system` is already applied. Use the user's variant names and theme tokens when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-05 | Completeness audit found 3 real gaps: (1) the Toast/Snackbar subsystem (`ToastHost`, `ToastHostState`, `ToastData`, `ToastVariant`, `LocalToastHostState`) never carried the `App` prefix at all — renamed to `AppToastHost`/`AppToastHostState`/`AppToastData`/`AppToastVariant`/`LocalAppToastHostState` and fixed a resulting double-prefix typo in Common Anti-Patterns; (2) the skill promised "Progress (linear + circular)" but only linear existed — added `AppCircularProgress` (determinate ring + indeterminate rotating arc, same infinite-animation constraint as `AppSpinner`) and fixed `AppProgress`'s docstring, which falsely claimed it delegated to `AppSpinner` for the indeterminate case; (3) description claimed "27 components" — corrected to the accurate count (26). Added the `App`-is-a-placeholder cross-reference to Prerequisites and the frontmatter description, matching the base skill's Step 0. |
| 2026-07-05 | Added "Style API coverage" table classifying all 24 components (wired / correctly slot-API-exempt / correctly exempt due to a real limitation / not-yet-wired) so the audit's Style-compliance detectors don't flag legitimate exemptions as gaps. Wired `AppAvatar` (was importing `Style`/`MutableStyleState`/`styleable` unused — dead code from an unfinished wiring attempt): added a `style: Style = Style` escape hatch and an `avatarDefaultStyle` for its background/shape. |
| 2026-07-05 | Fixed `AppIconButton`: `styleState.enabled = enabled` used the wrong property name and a non-idiomatic construction — corrected to `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }` per the official Compose Styles API docs (see base skill's `references/compose-styles-api-reference.md`). |
| 2026-06-22 | Renamed all `TextStyle.` references → `AppTextStyle.` to align with base skill rename. |
| 2026-06-06 | Initial release. |
