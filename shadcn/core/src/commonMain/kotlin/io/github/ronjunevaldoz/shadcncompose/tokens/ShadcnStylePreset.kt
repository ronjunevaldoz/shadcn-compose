package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Immutable
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
 *
 * [ring] is a genuine exception to that "approximation, not parity" rule: every
 * style's real `.cn-button` (and every other focusable component) CSS was checked
 * directly against `shadcn-ui/ui`'s `apps/v4/registry/styles/style-<name>.css` files
 * (2026-07-09) -- ring width and opacity really do vary per style in the real
 * registry, not just an invented distinction:
 * `focus-visible:ring-<N> focus-visible:ring-ring/<opacity>`, `ring-offset-*` absent
 * everywhere (offset stays 0 for all 8). A prior version of this file removed `ring`
 * entirely after wrongly assuming it didn't vary by style -- that assumption was never
 * checked against the real per-style CSS files, only against a retired, unrelated
 * two-style axis (`shadcn init`'s old `Default`/`New York` split). Re-added with the
 * real values below once actually verified.
 */
@Immutable
enum class ShadcnStylePreset(
    val shapes: ShadcnShapes,
    val spacing: ShadcnSpacing,
    val typography: ShadcnTypography,
    val ring: ShadcnRing,
    val animations: ShadcnAnimations,
    val icons: ShadcnIconStyles,
) {
    /** Vega: "Clean, neutral, and familiar." Classic 16px icon boxes,
     *  balanced standard animations. `baseRadius = 8.dp` is
     *  chosen so the derived `.lg` (the step nearly every component actually renders
     *  with) lands exactly on this preset's previous hand-tuned 8.dp default. */
    Vega(
        shapes = ShadcnShapes.fromBaseRadius(baseRadius = 8.dp),
        spacing = ShadcnSpacing(),
        typography = ShadcnTypography(),
        ring = ShadcnRing(width = 3.dp, opacity = 0.5f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 200),
                visibilityTransition = TweenSpec(durationMillis = 150),
            ),
        icons = ShadcnIconStyles(standardSize = 16.dp, smallSize = 14.dp, strokeWidth = 2f),
    ),

    /** Nova: "Reduced padding and margins." Snappy animation frames,
     *  ultra-tight spacing layouts, and compact 14px icon bounds. */
    Nova(
        shapes = ShadcnShapes.fromBaseRadius(baseRadius = 8.dp),
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
        ring = ShadcnRing(width = 3.dp, opacity = 0.5f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 100),
                visibilityTransition = TweenSpec(durationMillis = 75),
            ),
        icons = ShadcnIconStyles(standardSize = 14.dp, smallSize = 12.dp, strokeWidth = 1.5f),
    ),

    /** Maia: "Rounded, with generous spacing." Fluid, bouncy transitions and
     *  larger icon touch targets. */
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
        ring = ShadcnRing(width = 3.dp, opacity = 0.5f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 300),
                visibilityTransition = TweenSpec(durationMillis = 200),
            ),
        icons = ShadcnIconStyles(standardSize = 18.dp, smallSize = 16.dp, strokeWidth = 2f),
    ),

    /** Lyra: "Boxy and sharp. For mono fonts." High-speed technical transitions,
     *  and thin-stroke icons for a blueprint aesthetic. */
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
        ring = ShadcnRing(width = 1.dp, opacity = 0.5f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 150),
                visibilityTransition = TweenSpec(durationMillis = 100),
            ),
        icons = ShadcnIconStyles(standardSize = 16.dp, smallSize = 14.dp, strokeWidth = 1f),
    ),

    /** Mira: "Made for compact interfaces." Tightest animation timings and
     *  minimal icon footprints. */
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
        ring = ShadcnRing(width = 2.dp, opacity = 0.3f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 120),
                visibilityTransition = TweenSpec(durationMillis = 80),
            ),
        icons = ShadcnIconStyles(standardSize = 14.dp, smallSize = 12.dp, strokeWidth = 1.5f),
    ),

    /** Luma: "Fluid, luminous, and soft." Slow, elegant fade transitions
     *  and standard icon weights. */
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
        ring = ShadcnRing(width = 3.dp, opacity = 0.3f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 400),
                visibilityTransition = TweenSpec(durationMillis = 250),
            ),
        icons = ShadcnIconStyles(standardSize = 16.dp, smallSize = 14.dp, strokeWidth = 1.5f),
    ),

    /** Sera: "Editorial and typographic." Standard reliable transitions
     *  with icon weights that balance the larger type. */
    Sera(
        shapes = ShadcnShapes(),
        spacing = ShadcnSpacing(),
        typography = ShadcnTypography().scaled(1.15f),
        ring = ShadcnRing(width = 2.dp, opacity = 0.3f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 200),
                visibilityTransition = TweenSpec(durationMillis = 150),
            ),
        icons = ShadcnIconStyles(standardSize = 18.dp, smallSize = 16.dp, strokeWidth = 2f),
    ),

    /** Rhea: "Like Luma but compact." Fluid Luma transitions but Nova-level
     *  compact spacing and icons. */
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
        ring = ShadcnRing(width = 3.dp, opacity = 0.3f),
        animations =
            ShadcnAnimations(
                defaultTransition = TweenSpec(durationMillis = 350),
                visibilityTransition = TweenSpec(durationMillis = 200),
            ),
        icons = ShadcnIconStyles(standardSize = 14.dp, smallSize = 12.dp, strokeWidth = 1.5f),
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
