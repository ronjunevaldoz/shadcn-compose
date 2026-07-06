package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ShadcnShapes(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 6.dp,
    val lg: Dp = 8.dp,
    val xl: Dp = 12.dp,
    val xxl: Dp = 16.dp,
    val full: Dp = 9999.dp,
)
