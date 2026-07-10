package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.ronjunevaldoz.tailwind.core.Oklch
import io.github.ronjunevaldoz.tailwind.core.TwColors
import io.github.ronjunevaldoz.tailwind.core.toColor

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
        // Every field below is a named TwColors shade -- verified byte-for-byte
        // identical to this file's previous hex literals via a scratch test comparing
        // each field against the full 26-hue/11-shade table before migrating (same
        // methodology as ShadcnBaseColor.kt). primary/onSecondary match real shadcn's
        // oklch(0.205 0 0) (neutral900) -- see docs/shadcn-parity.md.
        primary = TwColors.neutral900,
        primaryHover = TwColors.zinc800,
        primaryPressed = TwColors.zinc700,
        primaryDisabled = TwColors.zinc300,
        onPrimary = TwColors.zinc50,
        secondary = TwColors.zinc100,
        secondaryHover = TwColors.zinc200,
        onSecondary = TwColors.neutral900,
        // Verified against oklch(0.577 0.245 27.325) -- see docs/shadcn-parity.md.
        destructive = TwColors.red600,
        // Not a named-shade match (deliberately hand-tuned, same as ShadcnAccent's
        // colors) -- kept as a literal hex rather than snapped to the nearest shade,
        // which would silently shift the actual color.
        destructiveHover = Color(0xFFB91C1C),
        // Real button/badge .tsx hardcode `text-white` for the destructive variant
        // rather than a themed foreground token.
        onDestructive = TwColors.white,
        background = TwColors.white,
        surface = TwColors.white,
        surfaceVariant = TwColors.zinc100,
        onSurface = TwColors.zinc950,
        onSurfaceVariant = TwColors.zinc500,
        border = TwColors.zinc200,
        // shadcn's "ring" token is a distinct mid-gray (oklch(0.705 0.015 286.067) =
        // zinc400), not primary -- it's a focus indicator color, not a brand color.
        borderFocus = TwColors.zinc400,
        muted = TwColors.zinc100,
        onMuted = TwColors.zinc500,
        // Not named-shade matches either -- same hand-tuned note as destructiveHover.
        success = Color(0xFF16A34A),
        warning = Color(0xFFD97706),
        // Real shadcn has no separate "error" token -- form validation reuses
        // `destructive` directly (`aria-invalid:border-destructive`), so this matches
        // `destructive` above.
        error = TwColors.red600,
        onStatus = TwColors.white,
        // card/popover/sidebar computed directly from zinc.json's real oklch(...) triples
        // via tailwind-compose's Oklch(...).toColor() -- traces 1:1 to shadcn's actual CSS
        // source values rather than a separately hand-verified hex literal. card/popover
        // are identical to background/onSurface in this palette (real shadcn's default
        // zinc theme keeps them equal too; only the dark palette diverges them). sidebar
        // is a hair lighter than pure white, distinct from the page background so a
        // sidebar rail reads as a faintly shaded panel rather than blending into the page.
        card = Oklch(1f, 0f, 0f).toColor(),
        onCard = Oklch(0.141f, 0.005f, 285.823f).toColor(),
        popover = Oklch(1f, 0f, 0f).toColor(),
        onPopover = Oklch(0.141f, 0.005f, 285.823f).toColor(),
        sidebar = Oklch(0.985f, 0f, 0f).toColor(),
        onSidebar = Oklch(0.141f, 0.005f, 285.823f).toColor(),
        sidebarPrimary = TwColors.neutral900,
        onSidebarPrimary = TwColors.zinc50,
        sidebarAccent = TwColors.zinc100,
        onSidebarAccent = TwColors.neutral900,
        sidebarBorder = TwColors.zinc200,
        sidebarRing = TwColors.zinc400,
        isLight = true,
    )

val ShadcnDarkColors =
    ShadcnColors(
        // primary/onPrimary verified against ui.shadcn.com/docs/theming's dark
        // oklch(0.922 0 0)/oklch(0.205 0 0) -- see docs/shadcn-parity.md. Was previously
        // near-white (#FAFAFA); real shadcn's dark-mode primary is a light gray, not
        // pure near-white.
        primary = TwColors.neutral200,
        primaryHover = TwColors.zinc200,
        primaryPressed = TwColors.zinc300,
        primaryDisabled = TwColors.zinc700,
        onPrimary = TwColors.neutral900,
        secondary = TwColors.zinc800,
        secondaryHover = TwColors.zinc700,
        onSecondary = TwColors.zinc50,
        // Verified against dark oklch(0.704 0.191 22.216) -- see docs/shadcn-parity.md.
        // Real shadcn's dark-mode destructive is a *brighter* coral than light mode (for
        // contrast against a dark background); this token was previously a dark maroon,
        // the opposite direction.
        destructive = TwColors.red400,
        // Not a named-shade match (deliberately hand-tuned) -- see the light palette's
        // matching note on destructiveHover above.
        destructiveHover = Color(0xFF991B1B),
        onDestructive = TwColors.white,
        background = TwColors.zinc950,
        surface = TwColors.zinc950,
        surfaceVariant = TwColors.zinc900,
        onSurface = TwColors.zinc50,
        onSurfaceVariant = TwColors.zinc400,
        border = TwColors.zinc800,
        // shadcn's "ring" token in dark mode (oklch(0.552 0.016 285.938) = zinc500).
        borderFocus = TwColors.zinc500,
        muted = TwColors.zinc800,
        onMuted = TwColors.zinc400,
        // Not named-shade matches -- see the light palette's matching note above.
        success = Color(0xFF15803D),
        warning = Color(0xFFB45309),
        error = TwColors.red400,
        onStatus = TwColors.white,
        // card/popover/sidebar computed from zinc.json's real dark oklch(0.21 0.006
        // 285.885) via Oklch(...).toColor() -- unlike the light palette, these diverge
        // from `background` (oklch(0.141 0.005 285.823)): real shadcn's dark theme raises
        // cards/popovers/the sidebar to a lighter panel tone so they read as distinct
        // surfaces against the near-black page.
        card = Oklch(0.21f, 0.006f, 285.885f).toColor(),
        onCard = Oklch(0.985f, 0f, 0f).toColor(),
        popover = Oklch(0.21f, 0.006f, 285.885f).toColor(),
        onPopover = Oklch(0.985f, 0f, 0f).toColor(),
        sidebar = Oklch(0.21f, 0.006f, 285.885f).toColor(),
        onSidebar = Oklch(0.985f, 0f, 0f).toColor(),
        // Real shadcn's own zinc.json gives dark `sidebar-primary` a hardcoded blue
        // (oklch(0.488 0.243 264.376)) unrelated to this theme's neutral primary -- an
        // artifact of their theme generator, not a deliberate two-tier design (their own
        // light palette keeps sidebar-primary == primary). Aliased to this palette's own
        // primary/onPrimary instead of importing an arbitrary blue with no connection to
        // this project's ShadcnStylePreset accent system.
        sidebarPrimary = TwColors.neutral200,
        onSidebarPrimary = TwColors.neutral900,
        sidebarAccent = TwColors.zinc800,
        onSidebarAccent = TwColors.zinc50,
        sidebarBorder = TwColors.zinc800,
        sidebarRing = TwColors.zinc500,
        isLight = false,
    )
