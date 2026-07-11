package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnThemeData

/**
 * Every `rememberStyle()` in this package re-read [ShadcnTheme.LocalShadcnTheme] and
 * re-listed `colors`/`shapes`/`spacing` in its own `remember(...)` key by hand -- redundant,
 * since [ShadcnThemeData] is itself an `@Immutable data class`: keying on [theme] alone
 * already captures every one of its fields changing, so there is nothing a separately-listed
 * `colors`/`shapes`/`spacing` key adds. Centralizing this also removes the risk of a caller
 * forgetting to list a field its body actually reads (see the "keyed on just colors" bug
 * noted in this package's git history).
 *
 * [keys] are any additional, non-theme keys the caller's body depends on -- typically `this`,
 * the sealed variant/size being switched on. [factory] runs with [ShadcnThemeData] as its
 * receiver, so `colors`/`shapes`/`spacing`/etc. are in scope directly, the same as they were
 * as local `val`s before.
 */
@Composable
fun <T> rememberShadcnStyle(
    vararg keys: Any?,
    factory: ShadcnThemeData.() -> T,
): T {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return remember(theme, *keys) { theme.factory() }
}
