package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.ronjunevaldoz.tailwind.core.TwColors

/**
 * shadcn/ui's real "base color" picker (ui.shadcn.com/create): a named neutral gray
 * family that defines the *entire* non-accent palette -- background, foreground,
 * secondary, muted, border, and ring. This is a different axis from [ShadcnAccent],
 * which only overrides primary/onPrimary on top of whichever base is selected.
 *
 * Every field below is a named `TwColors` shade (tailwind-compose's real, oklch-sourced
 * Tailwind v4 palette -- verified byte-for-byte identical to this file's previous hex
 * literals via a scratch test comparing every field against the full 26-hue/11-shade
 * table before migrating, see PR history). `background` is plain white (real shadcn's
 * `oklch(1 0 0)`, not a tinted shade). Each family follows the same real-shadcn shade
 * mapping: background=white/{f}950=foreground/{f}900=primary/{f}50=onPrimary/
 * {f}100=secondary/{f}500=onSurfaceVariant/{f}200=border/{f}400=ring, with dark mode
 * inverting light's primary/onPrimary pair and raising secondary/border to {f}800.
 */
@Immutable
enum class ShadcnBaseColor(
    val light: ShadcnColors,
    val dark: ShadcnColors,
) {
    Neutral(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.neutral950,
                primary = TwColors.neutral900,
                onPrimary = TwColors.neutral50,
                secondary = TwColors.neutral100,
                onSurfaceVariant = TwColors.neutral500,
                border = TwColors.neutral200,
                ring = TwColors.neutral400,
            ),
        dark =
            baseDark(
                background = TwColors.neutral950,
                foreground = TwColors.neutral50,
                primary = TwColors.neutral200,
                onPrimary = TwColors.neutral900,
                secondary = TwColors.neutral800,
                onSurfaceVariant = TwColors.neutral400,
                border = TwColors.neutral800,
                ring = TwColors.neutral400,
            ),
    ),

    Stone(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.stone950,
                primary = TwColors.stone900,
                onPrimary = TwColors.stone50,
                secondary = TwColors.stone100,
                onSurfaceVariant = TwColors.stone500,
                border = TwColors.stone200,
                ring = TwColors.stone400,
            ),
        dark =
            baseDark(
                background = TwColors.stone950,
                foreground = TwColors.stone50,
                primary = TwColors.stone200,
                onPrimary = TwColors.stone900,
                secondary = TwColors.stone800,
                onSurfaceVariant = TwColors.stone400,
                border = TwColors.stone800,
                ring = TwColors.stone400,
            ),
    ),

    Zinc(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.zinc950,
                primary = TwColors.zinc900,
                onPrimary = TwColors.zinc50,
                secondary = TwColors.zinc100,
                onSurfaceVariant = TwColors.zinc500,
                border = TwColors.zinc200,
                ring = TwColors.zinc400,
            ),
        dark =
            baseDark(
                background = TwColors.zinc950,
                foreground = TwColors.zinc50,
                primary = TwColors.zinc200,
                onPrimary = TwColors.zinc900,
                secondary = TwColors.zinc800,
                onSurfaceVariant = TwColors.zinc400,
                border = TwColors.zinc800,
                ring = TwColors.zinc400,
            ),
    ),

    Mauve(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.mauve950,
                primary = TwColors.mauve900,
                onPrimary = TwColors.mauve50,
                secondary = TwColors.mauve100,
                onSurfaceVariant = TwColors.mauve500,
                border = TwColors.mauve200,
                ring = TwColors.mauve400,
            ),
        dark =
            baseDark(
                background = TwColors.mauve950,
                foreground = TwColors.mauve50,
                primary = TwColors.mauve200,
                onPrimary = TwColors.mauve900,
                secondary = TwColors.mauve800,
                onSurfaceVariant = TwColors.mauve400,
                border = TwColors.mauve800,
                ring = TwColors.mauve400,
            ),
    ),

    Olive(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.olive950,
                primary = TwColors.olive900,
                onPrimary = TwColors.olive50,
                secondary = TwColors.olive100,
                onSurfaceVariant = TwColors.olive500,
                border = TwColors.olive200,
                ring = TwColors.olive400,
            ),
        dark =
            baseDark(
                background = TwColors.olive950,
                foreground = TwColors.olive50,
                primary = TwColors.olive200,
                onPrimary = TwColors.olive900,
                secondary = TwColors.olive800,
                onSurfaceVariant = TwColors.olive400,
                border = TwColors.olive800,
                ring = TwColors.olive400,
            ),
    ),

    Mist(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.mist950,
                primary = TwColors.mist900,
                onPrimary = TwColors.mist50,
                secondary = TwColors.mist100,
                onSurfaceVariant = TwColors.mist500,
                border = TwColors.mist200,
                ring = TwColors.mist400,
            ),
        dark =
            baseDark(
                background = TwColors.mist950,
                foreground = TwColors.mist50,
                primary = TwColors.mist200,
                onPrimary = TwColors.mist900,
                secondary = TwColors.mist800,
                onSurfaceVariant = TwColors.mist400,
                border = TwColors.mist800,
                ring = TwColors.mist400,
            ),
    ),

    Taupe(
        light =
            baseLight(
                background = TwColors.white,
                foreground = TwColors.taupe950,
                primary = TwColors.taupe900,
                onPrimary = TwColors.taupe50,
                secondary = TwColors.taupe100,
                onSurfaceVariant = TwColors.taupe500,
                border = TwColors.taupe200,
                ring = TwColors.taupe400,
            ),
        dark =
            baseDark(
                background = TwColors.taupe950,
                foreground = TwColors.taupe50,
                primary = TwColors.taupe200,
                onPrimary = TwColors.taupe900,
                secondary = TwColors.taupe800,
                onSurfaceVariant = TwColors.taupe400,
                border = TwColors.taupe800,
                ring = TwColors.taupe400,
            ),
    ),
    ;

    val label: String get() = name
}

// Shared semantic colors that stay constant across every base -- matches real
// shadcn, which never re-derives destructive/success/warning from the base color.

private val DESTRUCTIVE_LIGHT = TwColors.red600
private val DESTRUCTIVE_DARK = TwColors.red400

/**
 * Midpoint RGB blend of two opaque colors -- used to derive dark-mode card/popover/
 * sidebar tones as a "raised panel" between `background` and `secondary`. Verified
 * against real shadcn's zinc.json: blending Zinc's dark `background` (#09090B) and
 * `secondary` (#27272A) this way reproduces its real oklch-derived `card`/`popover`/
 * `sidebar` value (#18181B) to within one rounding unit -- close enough that deriving
 * the other six base-color families' card/popover/sidebar the same way (rather than
 * hand-verifying oklch values across 7 families x 2 modes) is a reasonable trade.
 */
private fun blend(
    a: Color,
    b: Color,
): Color {
    val ar = (a.red * 255f).toInt()
    val ag = (a.green * 255f).toInt()
    val ab = (a.blue * 255f).toInt()
    val br = (b.red * 255f).toInt()
    val bg = (b.green * 255f).toInt()
    val bb = (b.blue * 255f).toInt()
    val r = (ar + br + 1) / 2
    val g = (ag + bg + 1) / 2
    val bl = (ab + bb + 1) / 2
    return Color(0xFF000000.toInt() or (r shl 16) or (g shl 8) or bl)
}

private fun baseLight(
    background: Color,
    foreground: Color,
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    onSurfaceVariant: Color,
    border: Color,
    ring: Color,
) = ShadcnColors(
    primary = primary,
    // Dynamic Feedback: Provide clean color shifts for user states!
    primaryHover = primary.copy(alpha = 0.9f),
    primaryPressed = primary.copy(alpha = 0.8f),
    primaryDisabled = primary.copy(alpha = 0.4f),
    onPrimary = onPrimary,
    secondary = secondary,
    secondaryHover = secondary.copy(alpha = 0.9f),
    onSecondary = primary,
    destructive = DESTRUCTIVE_LIGHT,
    destructiveHover = DESTRUCTIVE_LIGHT.copy(alpha = 0.9f),
    onDestructive = TwColors.white,
    background = background,
    surface = background,
    surfaceVariant = secondary,
    onSurface = foreground,
    onSurfaceVariant = onSurfaceVariant,
    border = border,
    borderFocus = ring,
    muted = secondary,
    onMuted = onSurfaceVariant,
    // Unlike destructive (an exact match to red600/red400), success/warning are
    // deliberately hand-tuned values -- same "corrected for ideal dark background
    // contrast" adjustment as ShadcnAccent's Green/Amber (verified: neither is an exact
    // or near-exact match to any named TwColors shade, off by as much as green600's
    // real oklch value). Kept as literal hex rather than snapped to a named shade,
    // which would silently shift the actual color.
    success = Color(0xFF16A34A),
    warning = Color(0xFFD97706),
    error = DESTRUCTIVE_LIGHT,
    onStatus = TwColors.white,
    // Real shadcn's light-mode card/popover are universally == background across every
    // base-color family (only dark mode raises them to a distinct panel tone).
    card = background,
    onCard = foreground,
    popover = background,
    onPopover = foreground,
    sidebar = background,
    onSidebar = foreground,
    sidebarPrimary = primary,
    onSidebarPrimary = onPrimary,
    sidebarAccent = secondary,
    onSidebarAccent = primary,
    sidebarBorder = border,
    sidebarRing = ring,
    isLight = true,
)

private fun baseDark(
    background: Color,
    foreground: Color,
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    onSurfaceVariant: Color,
    border: Color,
    ring: Color,
) = ShadcnColors(
    primary = primary,
    // Dynamic Feedback: Lighten or transparency-shift dark components cleanly
    primaryHover = primary.copy(alpha = 0.9f),
    primaryPressed = primary.copy(alpha = 0.8f),
    primaryDisabled = primary.copy(alpha = 0.4f),
    onPrimary = onPrimary,
    secondary = secondary,
    secondaryHover = secondary.copy(alpha = 0.9f),
    onSecondary = foreground,
    destructive = DESTRUCTIVE_DARK,
    destructiveHover = DESTRUCTIVE_DARK.copy(alpha = 0.9f),
    onDestructive = TwColors.white,
    background = background,
    surface = background,
    surfaceVariant = secondary,
    onSurface = foreground,
    onSurfaceVariant = onSurfaceVariant,
    // Correctly maps your high-contrast dark border tokens!
    border = border,
    borderFocus = ring,
    muted = secondary,
    onMuted = onSurfaceVariant,
    // Same as the light-mode note above -- not a named TwColors shade, deliberately
    // hand-tuned.
    success = Color(0xFF15803D),
    warning = Color(0xFFB45309),
    error = DESTRUCTIVE_DARK,
    onStatus = TwColors.white,
    // Dark-mode card/popover/sidebar sit between background and secondary -- a raised
    // panel tone, not equal to either (see `blend`'s doc comment for verification).
    card = blend(background, secondary),
    onCard = foreground,
    popover = blend(background, secondary),
    onPopover = foreground,
    sidebar = blend(background, secondary),
    onSidebar = foreground,
    // Aliased to primary/onPrimary rather than a hardcoded accent color -- see the
    // matching comment on ShadcnDarkColors.sidebarPrimary in ShadcnColors.kt.
    sidebarPrimary = primary,
    onSidebarPrimary = onPrimary,
    sidebarAccent = secondary,
    onSidebarAccent = foreground,
    sidebarBorder = border,
    sidebarRing = ring,
    isLight = false,
)
