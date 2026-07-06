---
name: kotlin-multiplatform-desktop-app
description: >
  Desktop-specific concerns for KMP Compose Multiplatform apps — window management,
  system tray, file picker, keyboard shortcuts, native menu bar, drag-and-drop, and
  packaging. Covers the Desktop target's deviations from Android/iOS: synchronous file
  I/O on the main thread is safe on Desktop, `LocalContext` does not exist, and window
  state must be managed differently than Android Activity state.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
  keywords:
    - Desktop
    - JVM desktop
    - Compose Desktop
    - Compose Multiplatform Desktop
    - window management
    - system tray
    - file picker
    - native menu bar
    - keyboard shortcuts
    - drag and drop
    - packaging
    - distributable
    - CMP Desktop
    - macOS app
    - Windows app
    - Linux app
    - rememberWindowState
    - ApplicationScope
---

## When to Use This Skill

Use when you need to:
- Add desktop-specific features to a KMP Compose Multiplatform app
- Manage window state, size, and position on Desktop
- Add a system tray icon with context menu
- Open a native file picker or directory chooser
- Implement keyboard shortcuts and a native menu bar
- Package and distribute the Desktop app (macOS `.dmg`, Windows `.msi`, Linux `.deb`)

**Trigger keywords:** Desktop target, Compose Desktop, CMP Desktop, window management,
system tray, file picker, native menu bar, keyboard shortcut Desktop, drag and drop Desktop,
packaging Desktop, distributable, macOS app, Windows app, Linux app, rememberWindowState,
ApplicationScope, singleWindow, openFileDialog, awt, Swing, JVM desktop, Desktop only.

**Freshness rule:** JetBrains CMP Desktop APIs (tray, menu, window) iterate quickly —
recheck the JetBrains CMP release notes and `androidx.compose.ui:ui-desktop` changelog
before upgrading the Compose Multiplatform version.

---

## Recommendation First

Default to **`singleWindowApplication` for simple apps, `application { Window(...) }` for
multi-window or complex lifecycle needs**.

Desktop has meaningfully different constraints from Android/iOS:
- No `Context` — use Compose's `LocalWindow` or pass platform handles explicitly
- File I/O is safe on the main thread on Desktop (no StrictMode) — but keep heavy work
  on a background dispatcher anyway for responsiveness
- `rememberWindowState()` replaces Android's saved state — not backed by process death recovery
- Packaging is handled by the Compose Multiplatform Gradle plugin, not the Android Gradle plugin

---

## Entry Point

### Simple — single window

```kotlin
// desktopApp/src/main/kotlin/main.kt
import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication(
    title = "My KMP App",
) {
    AppTheme {
        AppNavHost()
    }
}
```

### Full — multi-window, tray, lifecycle

```kotlin
// desktopApp/src/main/kotlin/main.kt
import androidx.compose.runtime.*
import androidx.compose.ui.window.*

fun main() = application {
    val windowState = rememberWindowState(
        width = 1280.dp,
        height = 800.dp,
        position = WindowPosition(Alignment.Center),
    )

    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Window(
            onCloseRequest = { isVisible = false },  // minimize to tray instead of exit
            state = windowState,
            title = "My KMP App",
        ) {
            MenuBar { AppMenuBar() }
            AppTheme { AppNavHost() }
        }
    }

    // System tray — only shown when window is hidden
    Tray(
        icon = painterResource("tray_icon.png"),
        menu = {
            Item("Show", onClick = { isVisible = true })
            Item("Quit", onClick = ::exitApplication)
        },
    )
}
```

---

## Window State

`rememberWindowState` persists through recomposition but **not** through app restart.
Save user-preferred window dimensions to `DataStore` if you want them to persist:

```kotlin
@Composable
fun ApplicationScope.MainWindow(prefs: WindowPreferences) {
    val windowState = rememberWindowState(
        width = prefs.width.dp,
        height = prefs.height.dp,
    )

    // Persist whenever window is moved or resized
    LaunchedEffect(windowState.size, windowState.position) {
        prefs.save(windowState.size.width.value, windowState.size.height.value)
    }

    Window(onCloseRequest = ::exitApplication, state = windowState, title = "App") {
        AppTheme { AppNavHost() }
    }
}
```

---

## Native Menu Bar

```kotlin
@Composable
fun MenuBarScope.AppMenuBar() {
    Menu("File") {
        Item("New", shortcut = KeyShortcut(Key.N, meta = true)) { /* new action */ }
        Item("Open…", shortcut = KeyShortcut(Key.O, meta = true)) { openFilePicker() }
        Separator()
        Item("Quit", shortcut = KeyShortcut(Key.Q, meta = true)) { exitProcess(0) }
    }
    Menu("Edit") {
        Item("Undo", shortcut = KeyShortcut(Key.Z, meta = true)) { /* undo */ }
        Item("Redo", shortcut = KeyShortcut(Key.Z, meta = true, shift = true)) { /* redo */ }
    }
}
```

Use `meta = true` for macOS Cmd key and `ctrl = true` for Windows/Linux Ctrl.
On macOS, the menu bar appears at the top of the screen (not the window).

---

## File Picker

Desktop has no Compose-native file picker — use the AWT `FileDialog` or `JFileChooser`
from a coroutine on the main thread:

```kotlin
// :core:common/src/desktopMain/kotlin/FilePicker.kt

import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Open a native file picker and return the selected path, or null if cancelled. */
fun openFilePicker(
    title: String = "Open File",
    extensions: List<String> = emptyList(),
): String? {
    val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
        if (extensions.isNotEmpty()) {
            val filter = extensions.joinToString(";") { "*.$it" }
            setFilenameFilter { _, name -> extensions.any { name.endsWith(".$it") } }
        }
        isVisible = true
    }
    return dialog.file?.let { "${dialog.directory}$it" }
}

/** Open a native directory chooser and return the selected path, or null if cancelled. */
fun openDirectoryPicker(title: String = "Choose Folder"): String? {
    val chooser = JFileChooser().apply {
        dialogTitle = title
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else null
}
```

Call from a composable via `rememberCoroutineScope`:

```kotlin
@Composable
fun ImportButton(onFileSelected: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    AppButton(onClick = {
        scope.launch(Dispatchers.Main) {   // AWT dialogs must run on the EDT
            openFilePicker(extensions = listOf("csv", "json"))?.let { onFileSelected(it) }
        }
    }) {
        AppText("Import file…")
    }
}
```

---

## Keyboard Shortcuts in Composables

For shortcuts that don't belong in the menu bar, use `onKeyEvent` on a focused component
or on the root `Window` content:

```kotlin
@Composable
fun AppNavHost() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    event.isMetaPressed &&
                    event.key == Key.K
                ) {
                    // Cmd+K — open command palette
                    true
                } else false
            }
            .focusRequester(FocusRequester.Default)
            .focusable()
    ) {
        // app content
    }
}
```

---

## Drag and Drop

Accept file drops on a window using the `onExternalDrag` modifier:

```kotlin
@Composable
fun DropZone(onFilesDropped: (List<String>) -> Unit) {
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onExternalDrag(
                onDragStart = { isDragging = true },
                onDragExit = { isDragging = false },
                onDrop = { state ->
                    isDragging = false
                    val paths = state.dragData
                        .let { it as? DragData.FilesList }
                        ?.readFiles()
                        ?.map { it.removePrefix("file://") }
                        .orEmpty()
                    onFilesDropped(paths)
                },
            ),
    ) {
        if (isDragging) {
            AppText("Drop files here", modifier = Modifier.align(Alignment.Center))
        }
    }
}
```

---

## Packaging and Distribution

The Compose Multiplatform Gradle plugin handles packaging via JPackage:

```kotlin
// desktopApp/build.gradle.kts
compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MyKMPApp"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "com.example.mykmpapp"
                iconFile.set(project.file("icons/icon.icns"))
                signing {
                    sign.set(true)
                    identity.set("Developer ID Application: Your Name (TEAMID)")
                }
                notarization {
                    appleID.set(providers.environmentVariable("APPLE_ID"))
                    password.set(providers.environmentVariable("APPLE_APP_SPECIFIC_PASSWORD"))
                }
            }

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "My KMP App"
                upgradeUuid = "YOUR-GUID-HERE"  // generate once with uuidgen
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}
```

```bash
# Build distributables
./gradlew :desktopApp:packageDmg        # macOS .dmg
./gradlew :desktopApp:packageMsi        # Windows .msi
./gradlew :desktopApp:packageDeb        # Linux .deb

# Run locally without packaging
./gradlew :desktopApp:run
```

---

## Desktop vs Android/iOS Differences

| Concern | Android | iOS | Desktop |
|---|---|---|---|
| Entry point | `Activity.setContent` | `UIHostingController` | `application { Window }` |
| Context | `LocalContext.current` | — | Not available — use `LocalWindow` |
| File I/O on main thread | ❌ StrictMode violation | ❌ | ✅ Safe (but use background for heavy work) |
| Window state persistence | `SavedStateHandle` | — | `rememberWindowState` (in-memory only) |
| System tray | ❌ | ❌ | ✅ `Tray()` |
| Native menu bar | ❌ | ❌ | ✅ `MenuBar {}` |
| File picker | `ActivityResultContracts` | `UIDocumentPicker` | AWT `FileDialog` |
| Packaging | APK/AAB | `.ipa` | `.dmg` / `.msi` / `.deb` |

---

## Testing

Desktop composables run on the JVM — use `createComposeRule()` without a device or emulator.
For file picker and tray (AWT), wrap in a fake so tests stay headless:

```kotlin
// Fake file picker for tests — returns a fixed path
class FakeFilePicker(private val path: String? = "/tmp/test.csv") {
    fun open(extensions: List<String> = emptyList()): String? = path
}

// Test a composable that uses a file picker
@get:Rule val composeRule = createComposeRule()

@Test
fun `import button calls onFileSelected with picker result`() {
    var selected: String? = null
    val fakePicker = FakeFilePicker(path = "/tmp/data.csv")

    composeRule.setContent {
        ImportButton(
            picker = fakePicker::open,
            onFileSelected = { selected = it },
        )
    }

    composeRule.onNodeWithText("Import file…").performClick()
    assertEquals("/tmp/data.csv", selected)
}

@Test
fun `import button does nothing when picker returns null`() {
    var called = false
    val fakePicker = FakeFilePicker(path = null)

    composeRule.setContent {
        ImportButton(picker = fakePicker::open, onFileSelected = { called = true })
    }

    composeRule.onNodeWithText("Import file…").performClick()
    assertFalse(called)
}
```

Window state behavior:

```kotlin
@Test
fun `window state defaults to expected size`() = runTest {
    // rememberWindowState is not testable outside composition —
    // test the ViewModel that persists the preference instead
    val prefs = FakeWindowPreferences()
    val vm = WindowPreferencesViewModel(prefs)
    assertEquals(1280, vm.state.value.width)
    assertEquals(800, vm.state.value.height)
}
```

---

## Common Anti-Patterns

- using `LocalContext.current` in shared composables — Desktop has no `Context`; use `expect/actual` to provide platform-specific values, or restructure to not need context
- not handling `onCloseRequest` — defaulting to `exitApplication` is fine for simple apps, but apps that minimize to tray must set `isVisible = false` instead
- opening `FileDialog` or `JFileChooser` from a non-main coroutine dispatcher — AWT UI dialogs must run on the Event Dispatch Thread; use `Dispatchers.Main` (EDT on Desktop)
- hardcoding `Cmd` shortcuts without `meta = true` — on Windows/Linux the key is `Ctrl`, not `Cmd`; use both `meta` and `ctrl` flags or detect the OS at runtime
- not setting `upgradeUuid` for Windows MSI — without a stable GUID, Windows installer treats each version as a separate app and leaves old versions installed
- forgetting to notarize macOS builds — Gatekeeper blocks unsigned/unnotarized `.dmg` on macOS 10.15+; always notarize before distributing outside the Mac App Store

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — Desktop is one of the scaffold targets
- `kotlin-multiplatform-datastore` — use DataStore to persist window size and user preferences on Desktop
- `kotlin-multiplatform-expect-actual` — file picker, tray, and platform handles need expect/actual
- `kotlin-multiplatform-preview-driven-development` — Desktop previews run on the JVM without a device

---

## Output Style

When asked about Desktop-specific features, respond in this order:
1. Identify which Desktop API is needed (window, tray, file picker, menu, shortcuts, packaging)
2. Note the Desktop vs Android/iOS difference if relevant
3. Provide the code snippet
4. Note any packaging or signing requirements if distribution is the goal

Keep AWT/Swing usage minimal and clearly contained — it should never leak into shared composables.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Initial release. |
