# shadcn-compose

[![CI](https://github.com/ronjunevaldoz/shadcn-compose/actions/workflows/ci.yml/badge.svg)](https://github.com/ronjunevaldoz/shadcn-compose/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
![Platforms](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-blue.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ronjunevaldoz/shadcn-compose.svg)](https://central.sonatype.com/artifact/io.github.ronjunevaldoz/shadcn-compose)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**shadcn-compose** is a [shadcn/ui](https://ui.shadcn.com)-inspired component library for **Compose Multiplatform** (Android, iOS, Desktop, and Web). 

Features 70+ components built on token-based theming with zero Material dependencies.

🚀 **[Live Demo](https://ronjunevaldoz.github.io/shadcn-compose/)** | 📚 **[Component Catalog](docs/components.md)**

---

## Quick Start

### 1. Add Dependency

```toml
# gradle/libs.versions.toml
[versions]
shadcn-compose = "0.2.7"

[libraries]
shadcn-compose = { module = "io.github.ronjunevaldoz:shadcn-compose", version.ref = "shadcn-compose" }
```

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.shadcn.compose)
        }
    }
}
```

> [!NOTE]
> **Compatibility**: Requires **Compose Multiplatform 1.11.1+** and **Kotlin 2.4.0+**. Add `@file:OptIn(ExperimentalFoundationStyleApi::class)` to files referencing component styles.

### 2. Usage Example

```kotlin
@Composable
fun App() {
    ShadcnTheme {
        ShadcnButton(onClick = { /* ... */ }) {
            ShadcnText("Click Me")
        }
    }
}
```

---

## Claude Code / AI Agent Skills

Using `shadcn-compose` with an AI coding assistant? Install our source-verified skills from [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills):

```bash
npx skills add ronjunevaldoz/kmp-agent-skills
```

- **`kmp-shadcn-compose`**: Component signatures & theme setup.
- **`kmp-shadcn-compose-layouts`**: Page layouts, form patterns & layout gap audit.

---

## Project Structure

- [`/shadcn/core`](./shadcn/core/src) — Published component library (`Shadcn*`).
- [`/app/shared`](./app/shared/src) — Catalog and documentation web/app UI.
- [`/app/desktopApp`](./app/desktopApp), [`/app/androidApp`](./app/androidApp), [`/app/webApp`](./app/webApp), [`/app/iosApp`](./app/iosApp) — Platform targets.

---

## Development

```bash
./gradlew build                          # Build all targets
./gradlew :app:desktopApp:run            # Run catalog app (Desktop)
./gradlew :shadcn:core:jvmTest           # Run unit & Roborazzi screenshot tests
```

---

## License

[Apache License 2.0](LICENSE).
