---
name: kotlin-multiplatform-shared-resources
description: >
  Sets up Compose Multiplatform Resources (compose-resources) for sharing strings,
  plurals, images, fonts, and raw files across all KMP targets (Android, iOS,
  Desktop, Web). Covers: resource directory structure, string localization,
  plurals, image assets, custom fonts, accessing resources from Composables and
  non-Composable code, and resource module setup. Assumes the project was scaffolded
  with kotlin-multiplatform-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-25'
  keywords:
    - Compose Resources
    - KMP resources
    - strings
    - localization
    - fonts
    - images
    - Kotlin Multiplatform
    - Compose Multiplatform
    - plurals
---

## Overview

Compose Multiplatform Resources is the standard way to share assets across all KMP targets.
No platform-specific resource systems needed — one `composeResources/` directory works everywhere.

```
:core:ui (or any module applying composeResources)
  src/commonMain/composeResources/
    drawable/         # SVG, PNG, WebP images
    drawable-dark/    # Dark theme variants
    drawable-xxhdpi/  # Density-specific rasters (Android-style)
    font/             # .ttf / .otf fonts
    values/           # strings.xml, plurals.xml
    values-es/        # Localized strings (Spanish)
  files/            # Raw files (JSON, certificates, etc.)
```

## When to Use This Skill

Use this skill when you need to:
- Share strings, plurals, images, fonts, or raw files across KMP targets
- Set up `Res` for a Compose Multiplatform UI module
- Localize content or package shared fonts and icons
- Recheck Compose Resources docs before changing the resource layout

**Trigger keywords:** shared resources, compose resources, strings, strings.xml, hardcoded strings, stringResource, plurals, fonts,
images, localization, resource module, Res, resource packaging,
i18n, l10n, internationalization, translations, localize app, app strings KMP,
string resources KMP, multiplatform images, drawable KMP, translate strings.

**Freshness rule:** recheck the current Compose Multiplatform Resources docs before
upgrading the plugin or moving resource directories.

---

## Recommendation First

Default to **CMP Resources (`composeResources/`) in `commonMain` for strings, images, and fonts**.

Why:
- a single `Res.string.xxx` accessor works on all targets — no platform-specific resource files
- the Compose Resources plugin generates type-safe accessors at build time
- localization is handled through standard `values-xx/strings.xml`-style directories

Use platform resource bundles only when you need to share resources with non-Compose targets
(e.g., a pure-Kotlin server module that also needs the same string definitions).

---

## Prerequisites

- Project scaffolded with `kotlin-multiplatform-feature-scaffold`
- Module applies `GROUP_ID.feature.ui` or `GROUP_ID.core` convention plugin with Compose enabled
- `compose-multiplatform` plugin applied (`id("org.jetbrains.compose")`)

---

## Version Reference

Compose Resources ships with Compose Multiplatform — no extra dependency needed.

```toml
[versions]
compose-multiplatform = "1.11.1"   # already in libs.versions.toml
```

The `compose.components.resources` accessor (already in `GROUP_ID.feature.ui` convention plugin) enables the resource system.

---

## Step 1: Enable resources in the module

In the module that owns shared resources (e.g. `:core:ui`), confirm `compose.components.resources` is in `commonMain`:

```kotlin
// GROUP_ID.feature.ui.gradle.kts or :core:ui/build.gradle.kts
sourceSets {
    commonMain.dependencies {
        implementation(compose.components.resources)
    }
}
```

Optionally set a custom package for the generated `Res` accessor:

```kotlin
// :core:ui/build.gradle.kts
compose.resources {
    publicResClass = true          // make Res accessible from other modules
    packageOfResClass = "GROUP_ID.core.ui.resources"
    generateResClass = always      // always, auto (default), never
}
```

---

## Step 2: Strings

Create `src/commonMain/composeResources/values/strings.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">My App</string>
    <string name="welcome_message">Welcome, %1$s!</string>
    <string name="retry">Retry</string>
    <string name="error_generic">Something went wrong. Please try again.</string>
    <string name="empty_state_title">Nothing here yet</string>
    <string name="empty_state_body">Pull down to refresh or check back later.</string>
</resources>
```

**Localized strings** — create `values-<locale>/strings.xml`:

`src/commonMain/composeResources/values-es/strings.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Mi App</string>
    <string name="welcome_message">¡Bienvenido, %1$s!</string>
    <string name="retry">Reintentar</string>
    <string name="error_generic">Algo salió mal. Inténtalo de nuevo.</string>
</resources>
```

---

## Step 3: Plurals

`src/commonMain/composeResources/values/plurals.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <plurals name="item_count">
        <item quantity="one">%1$d item</item>
        <item quantity="other">%1$d items</item>
    </plurals>

    <plurals name="minutes_ago">
        <item quantity="one">%1$d minute ago</item>
        <item quantity="other">%1$d minutes ago</item>
    </plurals>
</resources>
```

---

## Step 4: Images

Place images in `src/commonMain/composeResources/drawable/`:

- Use **SVG** for icons (scales perfectly, smallest size)
- Use **PNG/WebP** for photos or complex rasters
- Dark variants: `drawable-dark/` folder with same filenames
- Density rasters: `drawable-mdpi/`, `drawable-hdpi/`, `drawable-xhdpi/`, `drawable-xxhdpi/`

```
composeResources/
  drawable/
    ic_logo.svg
    img_onboarding.png
  drawable-dark/
    ic_logo.svg    ← dark variant, same name
```

---

## Step 5: Fonts

Place `.ttf` or `.otf` files in `src/commonMain/composeResources/font/`:

```
composeResources/
  font/
    inter_regular.ttf
    inter_medium.ttf
    inter_bold.ttf
```

---

## Step 6: Raw files

Place arbitrary files in `src/commonMain/composeResources/files/`:

```
composeResources/
  files/
    config.json
    certificates/
      root_ca.pem
```

---

## Step 7: Using resources in Composables

The Gradle plugin generates a `Res` object. Import from the package set in `compose.resources {}`:

```kotlin
import GROUP_ID.core.ui.resources.Res
import GROUP_ID.core.ui.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.Font

// Strings
@Composable
fun WelcomeScreen(userName: String) {
    Text(stringResource(Res.string.welcome_message, userName))
}

// Plurals
@Composable
fun ItemCount(count: Int) {
    Text(pluralStringResource(Res.plurals.item_count, count, count))
}

// Images
@Composable
fun AppLogo() {
    Image(
        painter = painterResource(Res.drawable.ic_logo),
        contentDescription = stringResource(Res.string.app_name)
    )
}

// Fonts
val InterRegular = FontFamily(Font(Res.font.inter_regular))
val InterBold    = FontFamily(Font(Res.font.inter_bold))

val AppTypography = Typography(
    bodyLarge = TextStyle(fontFamily = InterRegular, fontSize = 16.sp),
    titleLarge = TextStyle(fontFamily = InterBold, fontSize = 22.sp),
)
```

---

## Step 8: Using resources outside Composables

For non-Composable code (ViewModels, repositories), use the suspend API:

```kotlin
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getStringArray

// In a ViewModel / use case:
suspend fun getErrorMessage(): String =
    getString(Res.string.error_generic)

suspend fun getItemCountLabel(count: Int): String =
    pluralString(Res.plurals.item_count, count, count)
```

---

## Step 9: Raw file access

```kotlin
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
suspend fun loadConfig(): String {
    val bytes = Res.readBytes("files/config.json")
    return bytes.decodeToString()
}
```

---

## Step 10: Theme wiring

Create `src/commonMain/kotlin/GROUP_ID/core/ui/theme/AppTheme.kt`:

```kotlin
package GROUP_ID.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import GROUP_ID.core.ui.resources.Res
import GROUP_ID.core.ui.resources.*
import org.jetbrains.compose.resources.Font

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = Typography(
            bodyLarge = TextStyle(fontFamily = FontFamily(Font(Res.font.inter_regular)), fontSize = 16.sp),
            titleLarge = TextStyle(fontFamily = FontFamily(Font(Res.font.inter_bold)), fontSize = 22.sp),
        ),
        content = content
    )
}
```

---

## Step 11: Share Res across modules

By default, `Res` is internal to the module it's generated in. To share across modules:

```kotlin
// :core:ui/build.gradle.kts
compose.resources {
    publicResClass = true
    packageOfResClass = "GROUP_ID.core.ui.resources"
}
```

Then in consuming modules:

```kotlin
// :feature:home:ui/build.gradle.kts
sourceSets {
    commonMain.dependencies {
        implementation(projects.core.ui)   // gets access to :core:ui Res
    }
}
```

---

## Guidelines

- Keep all shared resources in a single `:core:ui` or `:core:resources` module — avoid scattering across feature modules
- Always provide at least a default locale (`values/strings.xml`) — never rely on locale-only files
- Treat `values/strings.xml` as the source of truth for user-facing copy in Compose screens
- Use SVG for all icons — PNG is only for photos or assets that need per-density control
- Avoid hardcoded strings in Composables — always use `stringResource(Res.string.*)` or a resource-backed API
- Font files should be subset to only the characters your app uses — reduces binary size
- Use `files/` for config or data assets; never put secrets there

---

## Verification

1. `./gradlew :core:ui:generateCommonMainResourceAccessors` — `Res` object generated
2. `./gradlew :core:ui:compileKotlinMetadata` — resource accessors compile in commonMain
3. Launch Android — verify strings, images, and fonts render correctly
4. Launch Desktop (`./gradlew :desktopApp:run`) — verify same resources render
5. Change device language to a supported locale — verify localized strings appear

---

## Testing

```kotlin
// Compose Resources provides runtime resolution — test via a real Compose scope
@get:Rule val composeRule = createComposeRule()

@Test fun `app_name string resource resolves without crash`() {
    composeRule.setContent {
        val name = stringResource(Res.string.app_name)
        Text(name, modifier = Modifier.testTag("app_name"))
    }
    // If the resource is missing from any platform bundle, this throws at runtime
    composeRule.onNodeWithTag("app_name").assertExists()
}

@Test fun `plural resource selects correct form for count one`() {
    composeRule.setContent {
        Text(
            pluralStringResource(Res.plurals.items_count, 1, 1),
            modifier = Modifier.testTag("plural_one"),
        )
    }
    composeRule.onNodeWithTag("plural_one").assertTextEquals("1 item")
}

@Test fun `plural resource selects correct form for count many`() {
    composeRule.setContent {
        Text(
            pluralStringResource(Res.plurals.items_count, 3, 3),
            modifier = Modifier.testTag("plural_many"),
        )
    }
    composeRule.onNodeWithTag("plural_many").assertTextEquals("3 items")
}
```

> String resource tests run on JVM via Roborazzi / `createComposeRule()` — no emulator needed. Add any missing string keys to `commonMain/composeResources/values/strings.xml` and re-run.

---

## Common Anti-Patterns

- hardcoding string literals in composables instead of using `Res.string` — blocks localization
- putting user-facing copy directly in `Text("...")`, `AppText(text = "...")`, or `contentDescription = "..."`
- storing images outside `composeResources/` — they won't be picked up by the resource accessor generator
- using platform-specific string files (`strings.xml` on Android only) for shared strings
- importing `Res` from a non-Compose module — resource accessors require a Compose-enabled source set
- forgetting to add new locales to `gradle/libs.versions.toml` locale list — localized strings are silently ignored

If `Res.string.xxx` is unresolved, run `./gradlew generateCommonMainResourceAccessors` to regenerate accessors.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — convention plugins that enable `composeResources/` in each module
- `kotlin-multiplatform-design-system` — fonts and icons distributed via `Res` accessors inside the design system
- `kotlin-multiplatform-preview-driven-development` — Desktop previews that consume `Res.string` and `Res.drawable`
- `kotlin-multiplatform-expect-actual` — platform-specific resource loading when CMP `Res` is insufficient

---

## Output Style

When asked about shared resources or localization, respond in this order:
1. recommendation (CMP Resources, strings/images/fonts in commonMain)
2. project structure (`:core:ui/src/commonMain/composeResources/`)
3. code snippet (one string or image accessor)
4. why shared resources are preferred over platform-specific resource bundles
5. main alternative (expect/actual resource loading, platform bundles)

Keep the snippet to one resource type. Map to the user's actual resource names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-06 | Initial release. |
