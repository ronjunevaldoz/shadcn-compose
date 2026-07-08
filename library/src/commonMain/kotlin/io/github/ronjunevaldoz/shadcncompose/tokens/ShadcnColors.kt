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
    // Per-container-role tokens, matching real shadcn's `--card`/`--popover`/`--sidebar*`
    // CSS variable families (see ui.shadcn.com/r/colors/zinc.json). Real shadcn keeps
    // these as their own roles -- distinct from `surface`/`background`/`muted` -- so a
    // theme can recolor "just the sidebar" or "just floating panels" without that change
    // leaking into every other surface. Values below default to the same hex as their
    // closest existing token (since this project's default palette already has
    // `surface == background`), but exist as independent fields so a future
    // ShadcnStylePreset can diverge them.
    val card: Color,
    val onCard: Color,
    val popover: Color,
    val onPopover: Color,
    val sidebar: Color,
    val onSidebar: Color,
    val sidebarPrimary: Color,
    val onSidebarPrimary: Color,
    val sidebarAccent: Color,
    val onSidebarAccent: Color,
    val sidebarBorder: Color,
    val sidebarRing: Color,
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
        // card/popover verified against zinc.json's oklch(1 0 0)/oklch(0.141 0.005
        // 285.823) -- identical to background/onSurface in this palette (real shadcn's
        // default zinc theme keeps them equal too; only the dark palette diverges them).
        card = Color(0xFFFFFFFF),
        onCard = Color(0xFF09090B),
        popover = Color(0xFFFFFFFF),
        onPopover = Color(0xFF09090B),
        // sidebar verified against oklch(0.985 0 0) -- a hair lighter than onPrimary's
        // near-white, distinct from the pure-white background so a sidebar rail reads as
        // a faintly shaded panel rather than blending into the page.
        sidebar = Color(0xFFFAFAFA),
        onSidebar = Color(0xFF09090B),
        sidebarPrimary = Color(0xFF171717),
        onSidebarPrimary = Color(0xFFFAFAFA),
        sidebarAccent = Color(0xFFF4F4F5),
        onSidebarAccent = Color(0xFF171717),
        sidebarBorder = Color(0xFFE4E4E7),
        sidebarRing = Color(0xFFA1A1AA),
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
        // card/popover/sidebar verified against zinc.json's dark oklch(0.21 0.006
        // 285.885) -- unlike the light palette, these diverge from `background`
        // (#09090B): real shadcn's dark theme raises cards/popovers/the sidebar to a
        // lighter panel tone so they read as distinct surfaces against the near-black page.
        card = Color(0xFF18181B),
        onCard = Color(0xFFFAFAFA),
        popover = Color(0xFF18181B),
        onPopover = Color(0xFFFAFAFA),
        sidebar = Color(0xFF18181B),
        onSidebar = Color(0xFFFAFAFA),
        // Real shadcn's own zinc.json gives dark `sidebar-primary` a hardcoded blue
        // (oklch(0.488 0.243 264.376)) unrelated to this theme's neutral primary -- an
        // artifact of their theme generator, not a deliberate two-tier design (their own
        // light palette keeps sidebar-primary == primary). Aliased to this palette's own
        // primary/onPrimary instead of importing an arbitrary blue with no connection to
        // this project's ShadcnStylePreset accent system.
        sidebarPrimary = Color(0xFFE5E5E5),
        onSidebarPrimary = Color(0xFF171717),
        sidebarAccent = Color(0xFF27272A),
        onSidebarAccent = Color(0xFFFAFAFA),
        sidebarBorder = Color(0xFF27272A),
        sidebarRing = Color(0xFF71717A),
        isLight = false,
    )
