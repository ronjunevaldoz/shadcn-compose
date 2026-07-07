package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.ui.graphics.Color

/**
 * shadcn/ui's real "theme"/accent picker (ui.shadcn.com/create): overrides only
 * `primary`/`primary-foreground` on top of whichever [ShadcnBaseColor] is selected --
 * background, border, muted, and the focus ring color are untouched, matching real
 * shadcn's behavior (verified against `apps/v4/registry/themes.ts`, commit d8ace42).
 * [Base] means "no override, use the base color's own primary."
 */
enum class ShadcnAccent(
    private val lightPrimary: Long?,
    private val lightOnPrimary: Long?,
    private val darkPrimary: Long?,
    private val darkOnPrimary: Long?,
) {
    Base(null, null, null, null),
    Amber(0xFFBB4D00, 0xFFFFFBEB, 0xFF973C00, 0xFFFFFBEB),
    Blue(0xFF1447E6, 0xFFEFF6FF, 0xFF193CB8, 0xFFEFF6FF),
    Cyan(0xFF007595, 0xFFECFEFF, 0xFF005F78, 0xFFECFEFF),
    Emerald(0xFF007A55, 0xFFECFDF5, 0xFF006045, 0xFFECFDF5),
    Fuchsia(0xFFA800B7, 0xFFFDF4FF, 0xFF8A0194, 0xFFFDF4FF),
    Green(0xFF008236, 0xFFF0FDF4, 0xFF016630, 0xFFF0FDF4),
    Indigo(0xFF432DD7, 0xFFEEF2FF, 0xFF372AAC, 0xFFEEF2FF),
    Lime(0xFF9AE600, 0xFF35530E, 0xFF7CCF00, 0xFF35530E),
    Orange(0xFFCA3500, 0xFFFFF7ED, 0xFF9F2D00, 0xFFFFF7ED),
    Pink(0xFFC6005C, 0xFFFDF2F8, 0xFFA3004C, 0xFFFDF2F8),
    Purple(0xFF8200DB, 0xFFFAF5FF, 0xFF6E11B0, 0xFFFAF5FF),
    Red(0xFFC10007, 0xFFFEF2F2, 0xFF9F0712, 0xFFFEF2F2),
    Rose(0xFFC70036, 0xFFFFF1F2, 0xFFA50036, 0xFFFFF1F2),
    Sky(0xFF0069A8, 0xFFF0F9FF, 0xFF00598A, 0xFFF0F9FF),
    Teal(0xFF00786F, 0xFFF0FDFA, 0xFF005F5A, 0xFFF0FDFA),
    Violet(0xFF7008E7, 0xFFF5F3FF, 0xFF5D0EC0, 0xFFF5F3FF),
    Yellow(0xFFFDC700, 0xFF733E0A, 0xFFF0B100, 0xFF733E0A),
    ;

    val label: String get() = name

    /** Applies this accent's primary/onPrimary override on top of [base], if any. */
    fun applyTo(
        base: ShadcnColors,
        dark: Boolean,
    ): ShadcnColors {
        val primary = if (dark) darkPrimary else lightPrimary
        val onPrimary = if (dark) darkOnPrimary else lightOnPrimary
        if (primary == null || onPrimary == null) return base
        return base.copy(primary = Color(primary), onPrimary = Color(onPrimary))
    }
}
