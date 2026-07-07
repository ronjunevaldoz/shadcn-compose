package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * shadcn/ui's real "base color" picker (ui.shadcn.com/create): a named neutral gray
 * family that defines the *entire* non-accent palette -- background, foreground,
 * secondary, muted, border, and ring. This is a different axis from [ShadcnAccent],
 * which only overrides primary/onPrimary on top of whichever base is selected.
 *
 * Values converted from the real oklch values in shadcn-ui/ui's
 * `apps/v4/registry/themes.ts` (commit d8ace42, 2026-07-06). destructive/onDestructive
 * and the semantic status colors (success/warning/error) are intentionally NOT part of
 * this axis -- real shadcn keeps those constant regardless of base color too.
 */
@Immutable
enum class ShadcnBaseColor(
    val light: ShadcnColors,
    val dark: ShadcnColors,
) {
    Neutral(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF0A0A0A,
                primary = 0xFF171717,
                onPrimary = 0xFFFAFAFA,
                secondary = 0xFFF5F5F5,
                onSecondary = 0xFF171717,
                onSurfaceVariant = 0xFF737373,
                border = 0xFFE5E5E5,
                ring = 0xFFA1A1A1,
            ),
        dark =
            baseDark(
                background = 0xFF0A0A0A,
                foreground = 0xFFFAFAFA,
                primary = 0xFFE5E5E5,
                onPrimary = 0xFF171717,
                secondary = 0xFF262626,
                onSecondary = 0xFFFAFAFA,
                onSurfaceVariant = 0xFFA1A1A1,
                border = 0xFF262626,
                ring = 0xFFA1A1A1,
            ),
    ),

    Stone(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF0C0A09,
                primary = 0xFF1C1917,
                onPrimary = 0xFFFAFAF9,
                secondary = 0xFFF5F5F4,
                onSecondary = 0xFF1C1917,
                onSurfaceVariant = 0xFF79716B,
                border = 0xFFE7E5E4,
                ring = 0xFFA6A09B,
            ),
        dark =
            baseDark(
                background = 0xFF0C0A09,
                foreground = 0xFFFAFAF9,
                primary = 0xFFE7E5E4,
                onPrimary = 0xFF1C1917,
                secondary = 0xFF292524,
                onSecondary = 0xFFFAFAF9,
                onSurfaceVariant = 0xFFA6A09B,
                border = 0xFF292524,
                ring = 0xFFA6A09B,
            ),
    ),

    Zinc(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF09090B,
                primary = 0xFF18181B,
                onPrimary = 0xFFFAFAFA,
                secondary = 0xFFF4F4F5,
                onSecondary = 0xFF18181B,
                onSurfaceVariant = 0xFF71717B,
                border = 0xFFE4E4E7,
                ring = 0xFF9F9FA9,
            ),
        dark =
            baseDark(
                background = 0xFF09090B,
                foreground = 0xFFFAFAFA,
                primary = 0xFFE4E4E7,
                onPrimary = 0xFF18181B,
                secondary = 0xFF27272A,
                onSecondary = 0xFFFAFAFA,
                onSurfaceVariant = 0xFF71717B, // Harmonised with the true theme scale
                border = 0xFF27272A, // High contrast Dark Border token
                ring = 0xFF71717B, // Crisp Focus indicator
            ),
    ),

    Mauve(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF0C090C,
                primary = 0xFF1D161E,
                onPrimary = 0xFFFAFAFA,
                secondary = 0xFFF3F1F3,
                onSecondary = 0xFF1D161E,
                onSurfaceVariant = 0xFF79697B,
                border = 0xFFE7E4E7,
                ring = 0xFFA89EA9,
            ),
        dark =
            baseDark(
                background = 0xFF0C090C,
                foreground = 0xFFFAFAFA,
                primary = 0xFFE7E4E7,
                onPrimary = 0xFF1D161E,
                secondary = 0xFF2A212C,
                onSecondary = 0xFFFAFAFA,
                onSurfaceVariant = 0xFFA89EA9,
                border = 0xFF2A212C,
                ring = 0xFFA89EA9,
            ),
    ),

    Olive(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF0C0C09,
                primary = 0xFF1D1D16,
                onPrimary = 0xFFFBFBF9,
                secondary = 0xFFF4F4F0,
                onSecondary = 0xFF1D1D16,
                onSurfaceVariant = 0xFF7C7C67,
                border = 0xFFE8E8E3,
                ring = 0xFFABAB9C,
            ),
        dark =
            baseDark(
                background = 0xFF0C0C09,
                foreground = 0xFFFBFBF9,
                primary = 0xFFE8E8E3,
                onPrimary = 0xFF1D1D16,
                secondary = 0xFF2B2B22,
                onSecondary = 0xFFFBFBF9,
                onSurfaceVariant = 0xFFABAB9C,
                border = 0xFF2B2B22,
                ring = 0xFFABAB9C,
            ),
    ),

    Mist(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF090B0C,
                primary = 0xFF161B1D,
                onPrimary = 0xFFF9FBFB,
                secondary = 0xFFF1F3F3,
                onSecondary = 0xFF161B1D,
                onSurfaceVariant = 0xFF67787C,
                border = 0xFFE3E7E8,
                ring = 0xFF9CA8AB,
            ),
        dark =
            baseDark(
                background = 0xFF090B0C,
                foreground = 0xFFF9FBFB,
                primary = 0xFFE3E7E8,
                onPrimary = 0xFF161B1D,
                secondary = 0xFF22292B,
                onSecondary = 0xFFF9FBFB,
                onSurfaceVariant = 0xFF9CA8AB,
                border = 0xFF22292B,
                ring = 0xFF9CA8AB,
            ),
    ),

    Taupe(
        light =
            baseLight(
                background = 0xFFFFFFFF,
                foreground = 0xFF0C0A09,
                primary = 0xFF1D1816,
                onPrimary = 0xFFFBFAF9,
                secondary = 0xFFF3F1F1,
                onSecondary = 0xFF1D1816,
                onSurfaceVariant = 0xFF7C6D67,
                border = 0xFFE8E4E3,
                ring = 0xFFABA09C,
            ),
        dark =
            baseDark(
                background = 0xFF0C0A09,
                foreground = 0xFFFBFAF9,
                primary = 0xFFE8E4E3,
                onPrimary = 0xFF1D1816,
                secondary = 0xFF2B2422,
                onSecondary = 0xFFFBFAF9,
                onSurfaceVariant = 0xFFABA09C,
                border = 0xFF2B2422,
                ring = 0xFFABA09C,
            ),
    ),
    ;

    val label: String get() = name
}

// Shared semantic colors that stay constant across every base -- matches real
// shadcn, which never re-derives destructive/success/warning from the base color.

private const val DESTRUCTIVE_LIGHT = 0xFFE7000B
private const val DESTRUCTIVE_DARK = 0xFFFF6467

private fun baseLight(
    background: Long,
    foreground: Long,
    primary: Long,
    onPrimary: Long,
    secondary: Long,
    onSecondary: Long,
    onSurfaceVariant: Long,
    border: Long,
    ring: Long,
) = ShadcnColors(
    primary = Color(primary),
    // Dynamic Feedback: Provide clean color shifts for user states!
    primaryHover = Color(primary).copy(alpha = 0.9f),
    primaryPressed = Color(primary).copy(alpha = 0.8f),
    primaryDisabled = Color(primary).copy(alpha = 0.4f),
    onPrimary = Color(onPrimary),
    secondary = Color(secondary),
    secondaryHover = Color(secondary).copy(alpha = 0.9f),
    onSecondary = Color(onSecondary),
    destructive = Color(DESTRUCTIVE_LIGHT),
    destructiveHover = Color(DESTRUCTIVE_LIGHT).copy(alpha = 0.9f),
    onDestructive = Color(0xFFFFFFFF),
    background = Color(background),
    surface = Color(background),
    surfaceVariant = Color(secondary),
    onSurface = Color(foreground),
    onSurfaceVariant = Color(onSurfaceVariant),
    border = Color(border),
    borderFocus = Color(ring),
    muted = Color(secondary),
    onMuted = Color(onSurfaceVariant),
    success = Color(0xFF16A34A),
    warning = Color(0xFFD97706),
    error = Color(DESTRUCTIVE_LIGHT),
    onStatus = Color(0xFFFFFFFF),
    isLight = true,
)

private fun baseDark(
    background: Long,
    foreground: Long,
    primary: Long,
    onPrimary: Long,
    secondary: Long,
    onSecondary: Long,
    onSurfaceVariant: Long,
    border: Long, // Added as a proper parameter so your enum tokens aren't ignored!
    ring: Long,
) = ShadcnColors(
    primary = Color(primary),
    // Dynamic Feedback: Lighten or transparency-shift dark components cleanly
    primaryHover = Color(primary).copy(alpha = 0.9f),
    primaryPressed = Color(primary).copy(alpha = 0.8f),
    primaryDisabled = Color(primary).copy(alpha = 0.4f),
    onPrimary = Color(onPrimary),
    secondary = Color(secondary),
    secondaryHover = Color(secondary).copy(alpha = 0.9f),
    onSecondary = Color(onSecondary),
    destructive = Color(DESTRUCTIVE_DARK),
    destructiveHover = Color(DESTRUCTIVE_DARK).copy(alpha = 0.9f),
    onDestructive = Color(0xFFFFFFFF),
    background = Color(background),
    surface = Color(background),
    surfaceVariant = Color(secondary),
    onSurface = Color(foreground),
    onSurfaceVariant = Color(onSurfaceVariant),
    border = Color(border), // Correctly maps your high-contrast dark border tokens!
    borderFocus = Color(ring),
    muted = Color(secondary),
    onMuted = Color(onSurfaceVariant),
    success = Color(0xFF15803D),
    warning = Color(0xFFB45309),
    error = Color(DESTRUCTIVE_DARK),
    onStatus = Color(0xFFFFFFFF),
    isLight = false,
)
