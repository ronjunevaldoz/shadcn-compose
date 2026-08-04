# Step 8: Expandable components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 8: Expandable components

### `components/AppAccordion.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

data class AccordionItem(
    val title: String,
    val content: @Composable () -> Unit,
)

/**
 * Expandable accordion. Supports single or multiple expanded sections.
 *
 * Usage:
 * ```
 * AppAccordion(
 *     items = listOf(
 *         AccordionItem("What is KMP?") { AppText("Kotlin Multiplatform...") },
 *         AccordionItem("How to install?") { AppText("Add to your gradle...") },
 *     )
 * )
 * ```
 */
@Composable
fun AppAccordion(
    items: List<AccordionItem>,
    modifier: Modifier = Modifier,
    multiExpand: Boolean = false,
) {
    val theme = appTheme
    val expandedIndices = remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isExpanded = index in expandedIndices.value
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(200),
                label = "chevron",
            )

            Column {
                if (index > 0) AppSeparator()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                expandedIndices.value = if (isExpanded) {
                                    expandedIndices.value - index
                                } else {
                                    if (multiExpand) expandedIndices.value + index else setOf(index)
                                }
                            },
                        )
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        text = item.title,
                        style = AppTextStyle.LabelLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    AppText(
                        text = "▼",
                        style = AppTextStyle.LabelSmall,
                        muted = true,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(tween(200)),
                    exit = shrinkVertically(tween(200)),
                ) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        item.content()
                    }
                }
            }
        }
        AppSeparator()
    }
}
```

### `components/AppScrollArea.kt`

**Platform reality check first:** `androidx.compose.foundation.VerticalScrollbar` +
`rememberScrollbarAdapter` is **Desktop/Web only** — there is no Android/iOS
implementation in the Compose Multiplatform foundation library as of the CMP version
this repo targets (`1.11.1`). This is not a guess: a real-world CMP app
([recstar](https://github.com/sdercolin/recstar)) uses exactly this shape — `desktopMain`
wires the real `VerticalScrollbar`, `iosMain`'s `actual` is an intentional no-op with the
comment `// no scrollbar on mobile platforms`. Touch platforms already show a transient
system scroll indicator during a fling; they don't need (and Compose doesn't provide) a
persistent draggable thumb.

```kotlin
// :core:designsystem/src/commonMain/kotlin/GROUP_ID/core/designsystem/components/AppScrollArea.kt
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AppVerticalScrollbar(modifier: Modifier = Modifier, scrollState: ScrollState)

@Composable
expect fun AppVerticalScrollbar(modifier: Modifier = Modifier, lazyListState: LazyListState)
```

```kotlin
// :core:designsystem/src/desktopMain/kotlin/GROUP_ID/core/designsystem/components/AppScrollArea.desktop.kt
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun AppVerticalScrollbar(modifier: Modifier, scrollState: ScrollState) {
    VerticalScrollbar(modifier = modifier.width(8.dp), adapter = rememberScrollbarAdapter(scrollState))
}

@Composable
actual fun AppVerticalScrollbar(modifier: Modifier, lazyListState: LazyListState) {
    VerticalScrollbar(modifier = modifier.width(8.dp), adapter = rememberScrollbarAdapter(lazyListState))
}
```

```kotlin
// :core:designsystem/src/androidMain + iosMain/.../AppScrollArea.<platform>.kt
// No visible thumb — touch platforms already show a transient system scroll indicator.
@Composable
actual fun AppVerticalScrollbar(modifier: Modifier, scrollState: ScrollState) {}

@Composable
actual fun AppVerticalScrollbar(modifier: Modifier, lazyListState: LazyListState) {}
```

**Positioning — the most common real bug:** the scrollbar must be a sibling of the
scrollable content inside a `Box`, aligned to the trailing edge, with
`fillMaxHeight()`. A scrollbar placed as a normal member of the same `Column`/`Row` as
the content (instead of overlaid via `Box`) renders in the wrong place or pushes content
over:

```kotlin
@Composable
fun AppScrollArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier) {
        Column(modifier = Modifier.verticalScroll(scrollState)) { content() }
        AppVerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            scrollState = scrollState,
        )
    }
}
```

If a project genuinely needs a draggable thumb on mobile too (rare — confirm this is a
real requirement, not a reflex port of the web version), build it as a custom
`pointerInput` + `detectDragGestures` overlay the same way as `AppResizablePanelGroup`
below, translating drag delta into `scrollState.scrollBy()` — don't wait for a future
`VerticalScrollbar` multiplatform release; the pattern is straightforward to hand-roll.

### `components/AppResizablePanelGroup.kt`

Drag a divider to resize two adjacent panes. Same `pointerInput` reasoning as `AppSlider`
(`draggable` only tracks one axis and lacks tap-to-seek) — a resize divider additionally
needs to clamp the result to a min/max range, which `Modifier.draggable` doesn't give you
directly either.

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.colors

/**
 * Two resizable panes split by a draggable divider.
 *
 * Usage:
 * ```
 * AppResizablePanelGroup(
 *     start = { FileTree() },
 *     end = { Editor() },
 * )
 * ```
 */
@Composable
fun AppResizablePanelGroup(
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialStartWeight: Float = 0.3f,
    minWeight: Float = 0.15f,
    maxWeight: Float = 0.85f,
) {
    var startWeight by remember { mutableFloatStateOf(initialStartWeight) }

    Row(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(startWeight)) { start() }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .pointerInput(Unit) {
                    // Drag delta only, in px — convert to a fraction of this Row's total
                    // width so the divider tracks the pointer 1:1 regardless of container size.
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaFraction = dragAmount.x / size.width
                        startWeight = (startWeight + deltaFraction).coerceIn(minWeight, maxWeight)
                    }
                }
                .background(colors.border),
        )

        Box(modifier = Modifier.weight(1f - startWeight)) { end() }
    }
}
```

**Why `dragAmount.x / size.width` and not a fixed dp-per-pixel constant:** the divider's
own `size.width` is only 4dp — the *Row's* width is what the weight fraction is relative
to, but `pointerInput`'s `size` refers to the node it's attached to (the divider), not the
Row. This example intentionally uses the divider's own size as an approximation that
works when the divider is thin relative to the panes; for exact 1:1 tracking, measure the
Row's width via `Modifier.onSizeChanged {}` on the `Row` itself and divide by that instead.

---

