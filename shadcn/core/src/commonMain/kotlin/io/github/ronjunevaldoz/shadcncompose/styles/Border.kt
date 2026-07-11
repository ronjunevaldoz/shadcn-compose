package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Every bordered component in this library uses a constant 1.dp width -- only the color
 * varies per variant/state (real shadcn/ui's own `border` utility is likewise a fixed
 * 1px everywhere; only `border-<color>` changes per component). This is the one place
 * that width lives instead of being repeated at all 18+ call sites across this package.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun StyleScope.border(color: Color) {
    borderWidth(1.dp)
    borderColor(color)
}
