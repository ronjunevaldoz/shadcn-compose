# Contributing to shadcn-compose

Thank you for your interest in contributing to **shadcn-compose**! This document provides guidelines for setting up your environment, making changes, and submitting pull requests.

---

## Development Prerequisites

- **JDK 21** (matches CI environment)
- **Android SDK** (for Android target builds)
- **Xcode** (macOS only, required for iOS target builds)

---

## Getting Started

1. **Fork and clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/shadcn-compose.git
   cd shadcn-compose
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the desktop catalog app:**
   ```bash
   ./gradlew :app:desktopApp:run
   ```

---

## Development Guidelines

### Architecture & Design Rules
- **No Material Dependencies**: `shadcn-compose` components are fully custom and built on token-based theming using the Compose Styles API (`@ExperimentalFoundationStyleApi`).
- **Shadcn Parity**: When adding or updating components, refer to [shadcn/ui](https://ui.shadcn.com)'s component specifications for visual and behavioral alignment.
- **Opt-In Annotations**: Source files referencing component styles require `@file:OptIn(ExperimentalFoundationStyleApi::class)`.

### Code Quality & Formatting
Run the code quality checks before submitting your changes:
```bash
./gradlew ktlintCheck detekt lint
./scripts/check_style_block_theme_reads.sh
```

To auto-format code according to project style:
```bash
./gradlew ktlintFormat
```

### Testing
Run unit tests and Roborazzi visual screenshot tests:
```bash
./gradlew :shadcn:core:jvmTest           # JVM unit + screenshot tests
./gradlew :shadcn:core:allTests          # All platform tests
```

If your changes intentionally alter UI layout or styling, record updated Roborazzi screenshot goldens:
```bash
./gradlew :shadcn:core:recordRoborazziJvm
```

---

## Submitting a Pull Request

1. **Create a topic branch:**
   ```bash
   git checkout -b feat/my-new-feature
   ```
2. **Commit your changes:**
   Use clear, descriptive commit messages (e.g. `feat: ...`, `fix: ...`, `docs: ...`).
3. **Push to your fork and submit a PR:**
   Ensure all CI checks pass. Provide a summary of changes and reference any related issues in the PR description.

---

## Questions and Discussions

For questions, ideas, or architectural discussions, please open a thread in [GitHub Discussions](https://github.com/ronjunevaldoz/shadcn-compose/discussions).
