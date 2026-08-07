# Step 2: Design Tokens

Part of `kmp-compose-design-system`. Load this file when working on: step 2: design tokens.

---

> **Project-owned.** Customize `tokens/` and `theme/` freely — `/update-design-system`
> will never modify these files. This is your brand layer.

### Palette guidance

- Prefer neutral tokens for most text, surfaces, borders, and disabled UI.
- Reserve saturated palette colors for brand accents, primary actions, and semantic states.
- If the project brief does not name a palette, propose one that fits the product:
  - enterprise / admin: zinc, slate, neutral
  - modern consumer: blue, indigo, violet
  - creative / playful: violet, fuchsia, rose
  - trust / finance: blue, teal, emerald

### `tokens/AppColors.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    // Brand
    val primary: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val primaryDisabled: Color,
    val onPrimary: Color,

    // Secondary
    val secondary: Color,
    val secondaryHover: Color,
    val onSecondary: Color,

    // Destructive
    val destructive: Color,
    val destructiveHover: Color,
    val onDestructive: Color,

    // Surface
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,

    // Border
    val border: Color,
    val borderFocus: Color,

    // Ghost / muted
    val muted: Color,
    val onMuted: Color,

    // Status
    val success: Color,
    val warning: Color,
    val error: Color,
    val onStatus: Color,

    // State overlays
    val hoverOverlay: Color,
    val pressedOverlay: Color,

    val isLight: Boolean,
)

val LightColors = AppColors(
    primary          = Color(0xFF09090B),
    primaryHover     = Color(0xFF27272A),
    primaryPressed   = Color(0xFF3F3F46),
    primaryDisabled  = Color(0xFFD4D4D8),
    onPrimary        = Color(0xFFFAFAFA),

    secondary        = Color(0xFFF4F4F5),
    secondaryHover   = Color(0xFFE4E4E7),
    onSecondary      = Color(0xFF09090B),

    destructive      = Color(0xFFDC2626),
    destructiveHover = Color(0xFFB91C1C),
    onDestructive    = Color(0xFFFEF2F2),

    background       = Color(0xFFFFFFFF),
    surface          = Color(0xFFFFFFFF),
    surfaceVariant   = Color(0xFFF4F4F5),
    onSurface        = Color(0xFF09090B),
    onSurfaceVariant = Color(0xFF71717A),

    border           = Color(0xFFE4E4E7),
    borderFocus      = Color(0xFF09090B),

    muted            = Color(0xFFF4F4F5),
    onMuted          = Color(0xFF71717A),

    success          = Color(0xFF16A34A),
    warning          = Color(0xFFD97706),
    error            = Color(0xFFDC2626),
    onStatus         = Color(0xFFFFFFFF),

    hoverOverlay     = Color(0x0A000000),
    pressedOverlay   = Color(0x1A000000),

    isLight          = true,
)

val DarkColors = AppColors(
    primary          = Color(0xFFFAFAFA),
    primaryHover     = Color(0xFFE4E4E7),
    primaryPressed   = Color(0xFFD4D4D8),
    primaryDisabled  = Color(0xFF3F3F46),
    onPrimary        = Color(0xFF09090B),

    secondary        = Color(0xFF27272A),
    secondaryHover   = Color(0xFF3F3F46),
    onSecondary      = Color(0xFFFAFAFA),

    destructive      = Color(0xFF7F1D1D),
    destructiveHover = Color(0xFF991B1B),
    onDestructive    = Color(0xFFFEF2F2),

    background       = Color(0xFF09090B),
    surface          = Color(0xFF09090B),
    surfaceVariant   = Color(0xFF18181B),
    onSurface        = Color(0xFFFAFAFA),
    onSurfaceVariant = Color(0xFFA1A1AA),

    border           = Color(0xFF27272A),
    borderFocus      = Color(0xFFFAFAFA),

    muted            = Color(0xFF27272A),
    onMuted          = Color(0xFFA1A1AA),

    success          = Color(0xFF15803D),
    warning          = Color(0xFFB45309),
    error            = Color(0xFF7F1D1D),
    onStatus         = Color(0xFFFFFFFF),

    hoverOverlay     = Color(0x0AFFFFFF),
    pressedOverlay   = Color(0x1AFFFFFF),

    isLight          = false,
)
```

### `tokens/AppTypography.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class AppTypography(
    val displayLarge: TextStyle  = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold,   lineHeight = 44.sp, letterSpacing = (-0.5).sp),
    val displayMedium: TextStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold,   lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    val titleLarge: TextStyle    = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    val titleMedium: TextStyle   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    val titleSmall: TextStyle    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    val bodyLarge: TextStyle     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal,  lineHeight = 24.sp),
    val bodyMedium: TextStyle    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,  lineHeight = 20.sp),
    val bodySmall: TextStyle     = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,  lineHeight = 16.sp),
    val labelLarge: TextStyle    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium,  lineHeight = 20.sp, letterSpacing = 0.1.sp),
    val labelSmall: TextStyle    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium,  lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
```

### `tokens/AppShapes.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val none: Dp    = 0.dp,
    val xs: Dp      = 2.dp,
    val sm: Dp      = 4.dp,
    val md: Dp      = 6.dp,
    val lg: Dp      = 8.dp,
    val xl: Dp      = 12.dp,
    val xxl: Dp     = 16.dp,
    val full: Dp    = 9999.dp,
)
```

### `tokens/AppSpacing.kt`

```kotlin
package GROUP_ID.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp  = 4.dp,
    val sm: Dp  = 8.dp,
    val md: Dp  = 12.dp,
    val lg: Dp  = 16.dp,
    val xl: Dp  = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
)
```

---

