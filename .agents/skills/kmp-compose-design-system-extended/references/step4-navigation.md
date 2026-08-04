# Step 4: Navigation components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 4: Navigation components

### `components/AppTopAppBar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Usage:
 * ```
 * AppTopAppBar(
 *     title = "Settings",
 *     navigationIcon = {
 *         AppIconButton(onClick = { navBack() }) {
 *             AppIcon(Icons.Default.ArrowBack, contentDescription = "Back")
 *         }
 *     },
 *     actions = {
 *         AppIconButton(onClick = { openMenu() }) {
 *             AppIcon(Icons.Default.MoreVert, contentDescription = "More")
 *         }
 *     }
 * )
 * ```
 */
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    backgroundColor: Color = appTheme.colors.background,
    contentColor: Color = appTheme.colors.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            navigationIcon()
        } else {
            Spacer(Modifier.padding(start = 12.dp))
        }
        AppText(
            text = title,
            style = AppTextStyle.TitleSmall,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        if (actions != null) {
            actions()
        }
    }
}
```

### `components/AppNavigationBar.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

data class NavBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val contentDescription: String? = null,
)

/**
 * Usage:
 * ```
 * val items = listOf(
 *     NavBarItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
 *     NavBarItem("Search", Icons.Outlined.Search),
 *     NavBarItem("Profile", Icons.Outlined.Person, Icons.Filled.Person),
 * )
 * AppNavigationBar(items = items, selectedIndex = currentTab, onItemSelected = { tab = it })
 * ```
 */
@Composable
fun AppNavigationBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = appTheme.colors.background,
) {
    val theme = appTheme
    Column(modifier = modifier.fillMaxWidth().background(backgroundColor)) {
        AppSeparator()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onItemSelected(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AppIcon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.contentDescription ?: item.label,
                        size = IconSize.Md,
                        tint = if (selected) theme.colors.primary else theme.colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    AppText(
                        text = item.label,
                        style = AppTextStyle.LabelSmall,
                        color = if (selected) theme.colors.primary else theme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
```

### `components/AppTabs.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.styles.TabDefaults
import GROUP_ID.core.designsystem.styles.TabVariant
import GROUP_ID.core.designsystem.theme.appTheme
import GROUP_ID.core.designsystem.theme.shapes

/**
 * Usage:
 * ```
 * val tabs = listOf("Overview", "Activity", "Settings")
 * AppTabs(
 *     tabs = tabs,
 *     selectedIndex = selectedTab,
 *     onTabSelected = { selectedTab = it },
 *     variant = TabVariant.Line,
 * ) { index ->
 *     when (index) {
 *         0 -> OverviewContent()
 *         1 -> ActivityContent()
 *         else -> SettingsContent()
 *     }
 * }
 * ```
 */
@Composable
fun AppTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: TabVariant = TabVariant.Line,
    content: (@Composable (selectedIndex: Int) -> Unit)? = null,
) {
    val theme = appTheme
    val colors = TabDefaults.colors()

    Column(modifier = modifier) {
        // Tab row
        when (variant) {
            TabVariant.Line -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        tabs.forEachIndexed { index, title ->
                            val selected = index == selectedIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onTabSelected(index) },
                                        role = Role.Tab,
                                    )
                                    .padding(bottom = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AppText(
                                    text = title,
                                    style = AppTextStyle.LabelLarge,
                                    color = if (selected) colors.selected else colors.unselected,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            if (selected) colors.indicator else androidx.compose.ui.graphics.Color.Transparent
                                        ),
                                )
                            }
                        }
                    }
                    AppSeparator(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }

            TabVariant.Pill -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.colors.secondary, RoundedCornerShape(theme.shapes.full))
                        .padding(4.dp),
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = index == selectedIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(theme.shapes.full))
                                .background(if (selected) theme.colors.background else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(index) },
                                    role = Role.Tab,
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppText(
                                text = title,
                                style = AppTextStyle.LabelLarge,
                                color = if (selected) colors.selected else colors.unselected,
                            )
                        }
                    }
                }
            }

            TabVariant.Enclosed -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.colors.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = index == selectedIndex
                        Column(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(index) },
                                    role = Role.Tab,
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AppText(
                                text = title,
                                style = AppTextStyle.LabelLarge,
                                color = if (selected) colors.selected else colors.unselected,
                            )
                        }
                    }
                }
            }
        }

        // Content area with crossfade
        if (content != null) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "tabContent",
            ) { index ->
                content(index)
            }
        }
    }
}
```

---

