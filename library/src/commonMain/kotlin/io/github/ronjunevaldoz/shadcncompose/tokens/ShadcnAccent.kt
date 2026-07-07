package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * shadcn/ui's real "theme"/accent picker (ui.shadcn.com/create): overrides only
 * `primary`/`primary-foreground` on top of whichever [ShadcnBaseColor] is selected --
 * background, border, muted, and the focus ring color are untouched, matching real
 * shadcn's behavior (verified against `apps/v4/registry/themes.ts`, commit d8ace42).
 * [Base] means "no override, use the base color's own primary."
 */
@Immutable
enum class ShadcnAccent(
    private val lightPrimary: Long?,
    private val lightOnPrimary: Long?,
    private val darkPrimary: Long?,
    private val darkOnPrimary: Long?,
) {
    Base(null, null, null, null),

    // Core Tailwind colors corrected for ideal dark background contrast
    Amber(0xFFD97706, 0xFFFFFFFF, 0xFFF59E0B, 0xFF0F172A),
    Blue(0xFF2563EB, 0xFFFFFFFF, 0xFF3B82F6, 0xFF0F172A),
    Cyan(0xFF0891B2, 0xFFFFFFFF, 0xFF06B6D4, 0xFF0F172A),
    Emerald(0xFF059669, 0xFFFFFFFF, 0xFF10B981, 0xFF0F172A),
    Fuchsia(0xFFC084FC, 0xFFFFFFFF, 0xFFD8B4FE, 0xFF0F172A),
    Green(0xFF16A34A, 0xFFFFFFFF, 0xFF22C55E, 0xFF0F172A),
    Indigo(0xFF4F46E5, 0xFFFFFFFF, 0xFF6366F1, 0xFF0F172A),
    Lime(0xFF84CC16, 0xFF0F172A, 0xFFA3E635, 0xFF0F172A),
    Orange(0xFFEA580C, 0xFFFFFFFF, 0xFFF97316, 0xFF0F172A),
    Pink(0xFFDB2777, 0xFFFFFFFF, 0xFFEC4899, 0xFF0F172A),
    Purple(0xFF9333EA, 0xFFFFFFFF, 0xFFA855F7, 0xFF0F172A),
    Red(0xFFDC2626, 0xFFFFFFFF, 0xFFEF4444, 0xFF0F172A),
    Rose(0xFFE11D48, 0xFFFFFFFF, 0xFFF43F5E, 0xFF0F172A),
    Sky(0xFF0284C7, 0xFFFFFFFF, 0xFF38BDF8, 0xFF0F172A),
    Teal(0xFF0D9488, 0xFFFFFFFF, 0xFF14B8A6, 0xFF0F172A),
    Violet(0xFF7C3AED, 0xFFFFFFFF, 0xFF8B5CF6, 0xFF0F172A),
    Yellow(0xFFCA8A04, 0xFF0F172A, 0xFFFACC15, 0xFF0F172A),
    ;

    val label: String get() = name

    /** Applies this accent's primary/onPrimary override on top of [base]. */
    fun applyTo(
        base: ShadcnColors,
        dark: Boolean,
    ): ShadcnColors {
        val primaryLong = if (dark) darkPrimary else lightPrimary
        val onPrimaryLong = if (dark) darkOnPrimary else lightOnPrimary

        if (primaryLong == null || onPrimaryLong == null) return base

        val resolvedPrimary = Color(primaryLong)

        return base.copy(
            primary = resolvedPrimary,
            onPrimary = Color(onPrimaryLong),
            // Dynamic Improvement: Focused state borders automatically match the accent
            // if an accent color is active!
            borderFocus = resolvedPrimary
        )
    }
}