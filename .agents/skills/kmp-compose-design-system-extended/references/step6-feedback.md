# Step 6: Feedback components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 6: Feedback components

### `components/AppAlert.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.AlertVariant

/**
 * Usage:
 * ```
 * AppAlert(title = "Heads up!", description = "You can add components to your app.")
 * AppAlert(
 *     variant = AlertVariant.Destructive,
 *     title = "Error",
 *     description = "Your session has expired. Please sign in again.",
 * )
 * AppAlert(
 *     variant = AlertVariant.Warning,
 *     icon = Icons.Default.Warning,
 *     title = "Warning",
 *     description = "This action cannot be undone.",
 * )
 * ```
 */
@Composable
fun AppAlert(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    variant: AlertVariant = AlertVariant.Default,
    style: Style = Style,
) {
    val styleState = remember { MutableStyleState() }

    Row(
        modifier = modifier.styleable(styleState, variant.style, style),
        verticalAlignment = if (description != null) Alignment.Top else Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(imageVector = icon, contentDescription = null, size = IconSize.Md)
            Spacer(Modifier.width(12.dp))
        }
        Column {
            AppText(text = title, style = AppTextStyle.TitleSmall)
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                AppText(text = description, style = AppTextStyle.BodySmall)
            }
        }
    }
}
```

### Toast/Snackbar system

The Toast system uses a `Scaffold` slot — **never a raw `Popup`**. `Popup` in CMP WasmJs positions relative to the parent composable, not the viewport, which causes it to appear in the wrong location. Use `AppScaffold` which renders toast at the correct viewport level.

### `components/AppToast.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme
import kotlinx.coroutines.delay
import java.util.UUID

enum class AppToastVariant { Default, Destructive, Success, Warning }

data class AppToastData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val variant: AppToastVariant = AppToastVariant.Default,
    val durationMs: Long = 3000L,
)

@Stable
class AppToastHostState {
    val toasts = mutableStateListOf<AppToastData>()

    fun show(
        title: String,
        description: String? = null,
        variant: AppToastVariant = AppToastVariant.Default,
        durationMs: Long = 3000L,
    ) {
        toasts.add(AppToastData(title = title, description = description, variant = variant, durationMs = durationMs))
    }

    fun dismiss(id: String) { toasts.removeAll { it.id == id } }
}

val LocalAppToastHostState = compositionLocalOf { AppToastHostState() }

@Composable
fun AppToastHost(
    toastHostState: AppToastHostState = LocalAppToastHostState.current,
    modifier: Modifier = Modifier,
) {
    val theme = appTheme
    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            toastHostState.toasts.takeLast(3).forEach { toast ->
                var visible by remember(toast.id) { mutableStateOf(true) }

                LaunchedEffect(toast.id) {
                    delay(toast.durationMs)
                    visible = false
                    delay(300)
                    toastHostState.dismiss(toast.id)
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it },
                ) {
                    val (bg, border, content) = when (toast.variant) {
                        AppToastVariant.Default     -> Triple(theme.colors.surface, theme.colors.border, theme.colors.onSurface)
                        AppToastVariant.Destructive -> Triple(theme.colors.destructive, theme.colors.destructive, theme.colors.onDestructive)
                        AppToastVariant.Success     -> Triple(theme.colors.success, theme.colors.success, theme.colors.onStatus)
                        AppToastVariant.Warning     -> Triple(theme.colors.warning, theme.colors.warning, theme.colors.onStatus)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .shadow(8.dp, RoundedCornerShape(theme.shapes.lg))
                            .background(bg, RoundedCornerShape(theme.shapes.lg))
                            .border(1.dp, border, RoundedCornerShape(theme.shapes.lg))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(text = toast.title, style = AppTextStyle.LabelLarge, color = content)
                            if (toast.description != null) {
                                AppText(text = toast.description, style = AppTextStyle.BodySmall, color = content.copy(alpha = 0.8f))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        AppIconButton(onClick = { toastHostState.dismiss(toast.id) }) {
                            AppText(text = "✕", style = AppTextStyle.LabelSmall, color = content)
                        }
                    }
                }
            }
        }
    }
}
```

### `components/AppScaffold.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import GROUP_ID.core.designsystem.theme.AppTheme
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Root scaffold that provides: topBar, bottomBar, AppToastHost.
 * Always use AppScaffold to get correct Toast positioning.
 *
 * Usage:
 * ```
 * val toastState = remember { AppToastHostState() }
 * AppScaffold(
 *     toastHostState = toastState,
 *     topBar = { AppTopAppBar(title = "Home") },
 *     bottomBar = { AppNavigationBar(items, selectedTab) { selectedTab = it } },
 * ) { paddingValues ->
 *     HomeScreen(modifier = Modifier.padding(paddingValues))
 * }
 *
 * // Show a toast from anywhere:
 * val toastState = LocalAppToastHostState.current
 * Button(onClick = { toastState.show("Saved!", variant = AppToastVariant.Success) }) { ... }
 * ```
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    toastHostState: AppToastHostState = remember { AppToastHostState() },
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    CompositionLocalProvider(LocalAppToastHostState provides toastHostState) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (topBar != null) topBar()
                Box(modifier = Modifier.weight(1f)) {
                    content(androidx.compose.foundation.layout.PaddingValues())
                }
                if (bottomBar != null) bottomBar()
            }
            // Toast overlay — rendered last, always on top
            AppToastHost(
                toastHostState = toastHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
```

---

