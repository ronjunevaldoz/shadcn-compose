package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

/**
 * shadcn/ui's real "Style" picker (ui.shadcn.com/create) -- Vega, Nova, Maia, Lyra,
 * Mira, Luma, Sera, Rhea -- ships as ~500KB of per-component CSS overrides per style
 * (individually restyling every component's Tailwind classes), which isn't something
 * we can or should replicate 1:1 in a token-based system. This models the *intent*
 * behind each style's real description as our existing shape/spacing/typography
 * tokens instead: a deliberate approximation, not per-component CSS parity.
 */
enum class ShadcnStylePreset(
    val shapes: ShadcnShapes,
    val spacing: ShadcnSpacing,
    val typography: ShadcnTypography,
    val ring: ShadcnRing = ShadcnRing(),
) {
    /** "Clean, neutral, and familiar" -- shadcn-compose's existing defaults. Also real
     *  shadcn/ui's historical "Default" style, so it gets that style's classic ring
     *  (`ring-2 ring-offset-2`) rather than today's unified `ring-[3px]`. */
    Vega(ShadcnShapes(), ShadcnSpacing(), ShadcnTypography(), ShadcnRing.Default),

    /** "Reduced padding and margins." Real shadcn/ui's direct successor to "New York"
     *  (which historically also meant "tighter/compact"), so it gets that style's
     *  classic flush ring (`ring-1`, no offset) instead of the unified default. */
    Nova(
        shapes = ShadcnShapes(),
        spacing =
            ShadcnSpacing(
                xxs = 1.dp,
                xs = 3.dp,
                sm = 6.dp,
                md = 9.dp,
                lg = 12.dp,
                xl = 15.dp,
                xxl = 18.dp,
                xxxl = 24.dp,
            ),
        typography = ShadcnTypography(),
        ring = ShadcnRing.NewYork,
    ),

    /** "Rounded, with generous spacing." */
    Maia(
        shapes =
            ShadcnShapes(
                none = 0.dp,
                xs = 4.dp,
                sm = 8.dp,
                md = 12.dp,
                lg = 16.dp,
                xl = 20.dp,
                xxl = 28.dp,
                full = 9999.dp,
            ),
        spacing =
            ShadcnSpacing(
                xxs = 3.dp,
                xs = 6.dp,
                sm = 10.dp,
                md = 16.dp,
                lg = 21.dp,
                xl = 26.dp,
                xxl = 32.dp,
                xxxl = 42.dp,
            ),
        typography = ShadcnTypography(),
    ),

    /** "Boxy and sharp. For mono fonts." -- zero radius everywhere except functionally
     *  circular controls (radio dot, switch thumb), plus a monospace type scale. */
    Lyra(
        shapes =
            ShadcnShapes(
                none = 0.dp,
                xs = 0.dp,
                sm = 0.dp,
                md = 0.dp,
                lg = 0.dp,
                xl = 0.dp,
                xxl = 0.dp,
                full = 9999.dp,
            ),
        spacing = ShadcnSpacing(),
        typography = ShadcnTypography().withFontFamily(FontFamily.Monospace),
    ),

    /** "Made for compact interfaces." -- tighter than Nova on both shape and spacing,
     *  plus a slightly smaller type scale. */
    Mira(
        shapes =
            ShadcnShapes(
                none = 0.dp,
                xs = 1.dp,
                sm = 2.dp,
                md = 4.dp,
                lg = 6.dp,
                xl = 8.dp,
                xxl = 10.dp,
                full = 9999.dp,
            ),
        spacing =
            ShadcnSpacing(
                xxs = 1.dp,
                xs = 2.dp,
                sm = 5.dp,
                md = 8.dp,
                lg = 10.dp,
                xl = 13.dp,
                xxl = 16.dp,
                xxxl = 21.dp,
            ),
        typography = ShadcnTypography().scaled(0.9f),
    ),

    /** "Fluid, luminous, and soft." -- generously rounded shapes, standard spacing. */
    Luma(
        shapes =
            ShadcnShapes(
                none = 0.dp,
                xs = 6.dp,
                sm = 10.dp,
                md = 14.dp,
                lg = 18.dp,
                xl = 22.dp,
                xxl = 26.dp,
                full = 9999.dp,
            ),
        spacing = ShadcnSpacing(),
        typography = ShadcnTypography(),
    ),

    /** "Editorial and typographic." -- standard shape/spacing, a bumped-up type scale. */
    Sera(
        shapes = ShadcnShapes(),
        spacing = ShadcnSpacing(),
        typography = ShadcnTypography().scaled(1.15f),
    ),

    /** "Like Luma but compact." -- Luma's rounded shapes, Mira's tight spacing. */
    Rhea(
        shapes =
            ShadcnShapes(
                none = 0.dp,
                xs = 6.dp,
                sm = 10.dp,
                md = 14.dp,
                lg = 18.dp,
                xl = 22.dp,
                xxl = 26.dp,
                full = 9999.dp,
            ),
        spacing =
            ShadcnSpacing(
                xxs = 1.dp,
                xs = 2.dp,
                sm = 5.dp,
                md = 8.dp,
                lg = 10.dp,
                xl = 13.dp,
                xxl = 16.dp,
                xxxl = 21.dp,
            ),
        typography = ShadcnTypography(),
    ),
    ;

    val label: String get() = name
}

private fun ShadcnTypography.withFontFamily(family: FontFamily): ShadcnTypography =
    ShadcnTypography(
        displayLarge = displayLarge.copy(fontFamily = family),
        displayMedium = displayMedium.copy(fontFamily = family),
        titleLarge = titleLarge.copy(fontFamily = family),
        titleMedium = titleMedium.copy(fontFamily = family),
        titleSmall = titleSmall.copy(fontFamily = family),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family),
        labelSmall = labelSmall.copy(fontFamily = family),
    )

private fun ShadcnTypography.scaled(factor: Float): ShadcnTypography =
    ShadcnTypography(
        displayLarge = displayLarge.scaled(factor),
        displayMedium = displayMedium.scaled(factor),
        titleLarge = titleLarge.scaled(factor),
        titleMedium = titleMedium.scaled(factor),
        titleSmall = titleSmall.scaled(factor),
        bodyLarge = bodyLarge.scaled(factor),
        bodyMedium = bodyMedium.scaled(factor),
        bodySmall = bodySmall.scaled(factor),
        labelLarge = labelLarge.scaled(factor),
        labelSmall = labelSmall.scaled(factor),
    )

private fun TextStyle.scaled(factor: Float): TextStyle =
    copy(
        fontSize = fontSize.scaled(factor),
        lineHeight = lineHeight.scaled(factor),
    )

private fun TextUnit.scaled(factor: Float): TextUnit = if (isSpecified) this * factor else this
