package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.delay

enum class ShadcnToastVariant { Default, Success, Error, Warning, Info }

data class ShadcnToastMessage(
    val id: Long,
    val title: String,
    val description: String? = null,
    val variant: ShadcnToastVariant = ShadcnToastVariant.Default,
    val durationMillis: Long = 4000L,
)

/**
 * Holds the queue of currently-visible toasts. Real shadcn/ui's `sonner.tsx` is a thin
 * wrapper around the separate `sonner` npm toast library, which owns the actual queue,
 * stacking, and swipe-to-dismiss; since there is no such library for Compose
 * Multiplatform, this class + [ShadcnToaster] together reimplement that queue directly.
 *
 * Usage:
 * ```
 * val toastState = rememberShadcnToastState()
 * ShadcnButton(onClick = { toastState.show("Saved", variant = ShadcnToastVariant.Success) }) { ... }
 * ShadcnToaster(state = toastState)
 * ```
 */
class ShadcnToastState {
    private val _toasts = mutableStateListOf<ShadcnToastMessage>()
    val toasts: List<ShadcnToastMessage> get() = _toasts
    private var nextId = 0L

    fun show(
        title: String,
        description: String? = null,
        variant: ShadcnToastVariant = ShadcnToastVariant.Default,
        durationMillis: Long = 4000L,
    ) {
        _toasts.add(ShadcnToastMessage(nextId++, title, description, variant, durationMillis))
    }

    fun dismiss(id: Long) {
        _toasts.removeAll { it.id == id }
    }
}

@Composable
fun rememberShadcnToastState(): ShadcnToastState = remember { ShadcnToastState() }

/** Renders [state]'s current toast queue as a bottom-aligned stack. Place once, near the root of your app. */
@Composable
fun ShadcnToaster(
    state: ShadcnToastState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        horizontalAlignment = Alignment.End,
    ) {
        state.toasts.forEach { toast ->
            key(toast.id) {
                ToastCard(toast = toast, onDismiss = { state.dismiss(toast.id) })
            }
        }
    }
}

@Composable
private fun ToastCard(
    toast: ShadcnToastMessage,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(toast.id) {
        delay(toast.durationMillis)
        visible = false
        delay(200)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
    ) {
        val theme = ShadcnTheme.LocalShadcnTheme.current
        val accentColor =
            when (toast.variant) {
                ShadcnToastVariant.Success -> theme.colors.success
                ShadcnToastVariant.Error -> theme.colors.error
                ShadcnToastVariant.Warning -> theme.colors.warning
                ShadcnToastVariant.Info -> theme.colors.borderFocus
                ShadcnToastVariant.Default -> theme.colors.border
            }
        Row(
            modifier =
                Modifier
                    .widthIn(min = 240.dp, max = 360.dp)
                    .background(theme.colors.background, RoundedCornerShape(theme.shapes.lg))
                    .border(1.dp, theme.colors.border, RoundedCornerShape(theme.shapes.lg))
                    .padding(theme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.sm),
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(accentColor, CircleShape),
            )
            Column(verticalArrangement = Arrangement.spacedBy(theme.spacing.xxs)) {
                ShadcnText(toast.title, style = ShadcnTextStyle.LabelLarge)
                if (toast.description != null) {
                    ShadcnText(toast.description, style = ShadcnTextStyle.BodySmall, muted = true)
                }
            }
        }
    }
}
