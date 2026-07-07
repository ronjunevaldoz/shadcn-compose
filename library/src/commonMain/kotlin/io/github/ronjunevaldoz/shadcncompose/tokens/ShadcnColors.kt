package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ShadcnColors(
    val primary: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val primaryDisabled: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryHover: Color,
    val onSecondary: Color,
    val destructive: Color,
    val destructiveHover: Color,
    val onDestructive: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val border: Color,
    val borderFocus: Color,
    val muted: Color,
    val onMuted: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val onStatus: Color,
    val isLight: Boolean,
)

val ShadcnLightColors =
    ShadcnColors(
        // primary/onSecondary verified against ui.shadcn.com/docs/theming's oklch(0.205 0 0)
        // -- see docs/shadcn-parity.md. Was previously near-black (#09090B); real shadcn's
        // primary is a dark gray, not pure near-black.
        primary = Color(0xFF171717),
        primaryHover = Color(0xFF27272A),
        primaryPressed = Color(0xFF3F3F46),
        primaryDisabled = Color(0xFFD4D4D8),
        onPrimary = Color(0xFFFAFAFA),
        secondary = Color(0xFFF4F4F5),
        secondaryHover = Color(0xFFE4E4E7),
        onSecondary = Color(0xFF171717),
        // Verified against oklch(0.577 0.245 27.325) -- see docs/shadcn-parity.md.
        destructive = Color(0xFFE7000B),
        destructiveHover = Color(0xFFB91C1C),
        // Real button/badge .tsx hardcode `text-white` for the destructive variant
        // rather than a themed foreground token.
        onDestructive = Color(0xFFFFFFFF),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF4F4F5),
        onSurface = Color(0xFF09090B),
        onSurfaceVariant = Color(0xFF71717A),
        border = Color(0xFFE4E4E7),
        // shadcn's "ring" token is a distinct mid-gray (oklch(0.708 0 0), ~zinc-400),
        // not primary -- it's a focus indicator color, not a brand color.
        borderFocus = Color(0xFFA1A1AA),
        muted = Color(0xFFF4F4F5),
        onMuted = Color(0xFF71717A),
        success = Color(0xFF16A34A),
        warning = Color(0xFFD97706),
        // Real shadcn has no separate "error" token -- form validation reuses
        // `destructive` directly (`aria-invalid:border-destructive`), so this matches
        // `destructive` above.
        error = Color(0xFFE7000B),
        onStatus = Color(0xFFFFFFFF),
        isLight = true,
    )

val ShadcnDarkColors =
    ShadcnColors(
        // primary/onPrimary verified against ui.shadcn.com/docs/theming's dark
        // oklch(0.922 0 0)/oklch(0.205 0 0) -- see docs/shadcn-parity.md. Was previously
        // near-white (#FAFAFA); real shadcn's dark-mode primary is a light gray, not
        // pure near-white.
        primary = Color(0xFFE5E5E5),
        primaryHover = Color(0xFFE4E4E7),
        primaryPressed = Color(0xFFD4D4D8),
        primaryDisabled = Color(0xFF3F3F46),
        onPrimary = Color(0xFF171717),
        secondary = Color(0xFF27272A),
        secondaryHover = Color(0xFF3F3F46),
        onSecondary = Color(0xFFFAFAFA),
        // Verified against dark oklch(0.704 0.191 22.216) -- see docs/shadcn-parity.md.
        // Real shadcn's dark-mode destructive is a *brighter* coral than light mode (for
        // contrast against a dark background); this token was previously a dark maroon,
        // the opposite direction.
        destructive = Color(0xFFFF6467),
        destructiveHover = Color(0xFF991B1B),
        onDestructive = Color(0xFFFFFFFF),
        background = Color(0xFF09090B),
        surface = Color(0xFF09090B),
        surfaceVariant = Color(0xFF18181B),
        onSurface = Color(0xFFFAFAFA),
        onSurfaceVariant = Color(0xFFA1A1AA),
        border = Color(0xFF27272A),
        // shadcn's "ring" token in dark mode (oklch(0.556 0 0), ~zinc-500).
        borderFocus = Color(0xFF71717A),
        muted = Color(0xFF27272A),
        onMuted = Color(0xFFA1A1AA),
        success = Color(0xFF15803D),
        warning = Color(0xFFB45309),
        error = Color(0xFFFF6467),
        onStatus = Color(0xFFFFFFFF),
        isLight = false,
    )
