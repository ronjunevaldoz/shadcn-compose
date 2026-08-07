# Step 3: AppTheme + CompositionLocals

Part of `kmp-compose-design-system`. Load this file when working on: step 3: apptheme + compositionlocals.

---

### `theme/AppTheme.kt`

```kotlin
package GROUP_ID.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import GROUP_ID.core.designsystem.tokens.AppColors
import GROUP_ID.core.designsystem.tokens.AppShapes
import GROUP_ID.core.designsystem.tokens.AppSpacing
import GROUP_ID.core.designsystem.tokens.AppTypography
import GROUP_ID.core.designsystem.tokens.DarkColors
import GROUP_ID.core.designsystem.tokens.LightColors

@Immutable
data class AppTheme(
    val colors: AppColors,
    val typography: AppTypography,
    val shapes: AppShapes,
    val spacing: AppSpacing,
) {
    companion object {
        val LocalAppTheme: ProvidableCompositionLocal<AppTheme> =
            staticCompositionLocalOf { AppTheme.light() }

        fun light(
            colors: AppColors         = LightColors,
            typography: AppTypography = AppTypography(),
            shapes: AppShapes         = AppShapes(),
            spacing: AppSpacing       = AppSpacing(),
        ) = AppTheme(colors, typography, shapes, spacing)

        fun dark(
            colors: AppColors         = DarkColors,
            typography: AppTypography = AppTypography(),
            shapes: AppShapes         = AppShapes(),
            spacing: AppSpacing       = AppSpacing(),
        ) = AppTheme(colors, typography, shapes, spacing)
    }
}

/**
 * Holds an in-app dark-mode override (true/false) set by a user settings toggle.
 * Null means "follow the system". Read via [LocalAppDarkTheme.current].
 *
 * Usage:
 * ```
 * // In your settings screen, persist and surface the override:
 * CompositionLocalProvider(LocalAppDarkTheme provides userPrefersDark) {
 *     AppTheme { ... }
 * }
 * ```
 */
val LocalAppDarkTheme = compositionLocalOf<Boolean?> { null }

/**
 * Root theme wrapper. Defaults to system dark-mode on all platforms.
 * An in-app override can be injected via [LocalAppDarkTheme].
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = LocalAppDarkTheme.current ?: isSystemInDarkTheme(),
    theme: AppTheme = if (darkTheme) AppTheme.dark() else AppTheme.light(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        AppTheme.LocalAppTheme provides theme,
        content = content,
    )
}

// Convenience accessor in Composable scope
val appTheme: AppTheme
    @Composable get() = AppTheme.LocalAppTheme.current
```

---

