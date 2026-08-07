# Step 7: Wire AppTheme in platform entry points

Part of `kmp-compose-design-system`.

---

### Android — `androidApp/src/main/kotlin/.../MainActivity.kt`

```kotlin
setContent {
    AppTheme {          // isSystemInDarkTheme() is the default — no argument needed
        AppNavHost()
    }
}
```

### Desktop — `desktopApp/src/jvmMain/kotlin/main.kt`

```kotlin
application {
    Window(onCloseRequest = ::exitApplication, title = "App") {
        AppTheme {      // isSystemInDarkTheme() reads OS dark mode on JVM via AWT
            AppNavHost()
        }
    }
}
```

### iOS — `shared/src/iosMain/kotlin/AppView.kt`

```kotlin
@Composable
fun AppView() {
    AppTheme {          // isSystemInDarkTheme() reads UITraitCollection on iOS
        AppNavHost()
    }
}
```

### In-app theme toggle (settings screen)

To let users override the system theme, wrap `AppTheme` with `LocalAppDarkTheme`:

```kotlin
// Read the user's preference from DataStore / shared prefs:
val userDarkMode: Boolean? by viewModel.darkModePreference.collectAsStateWithLifecycle()

CompositionLocalProvider(LocalAppDarkTheme provides userDarkMode) {
    AppTheme {
        AppNavHost()
    }
}
```

`null` = follow system, `true` = always dark, `false` = always light.

---

