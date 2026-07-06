# AGENTS.md — shadcn-compose

This project uses [kmm-agent-skills](https://github.com/ronjunevaldoz/kmm-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

A shadcn/ui-inspired Compose Multiplatform component library built on the Compose
Styles API (`@ExperimentalFoundationStyleApi`) -- token-based theming, sealed variant
systems, zero Material dependency. Targets Android, iOS (arm64 + simulator), Desktop
(JVM), and Web (JS + WasmJS).

Group ID: `io.github.ronjunevaldoz`   Artifact: `shadcn-compose`   Published to: Maven Central (dry-run wired, not yet released)

## Planned dependencies

- **tailwind-compose** (sibling project at `/Users/ronvaldoz/StudioProjects/tailwind-compose`) —
  shadcn-compose is planned to depend on this once it exists. Intended as a
  utility-class/atomic styling layer (Tailwind-style utility modifiers for spacing,
  layout, etc.) sitting *underneath* or *alongside* shadcn-compose's component-level
  Style API variants — not a replacement for the sealed variant system. Same
  relationship as Tailwind CSS + shadcn/ui on the web: utilities handle one-off
  layout/spacing, the component library handles semantic variants (`ButtonVariant`,
  `CardVariant`, etc.).
  - As of now, tailwind-compose is **not implemented** (empty scaffold, no code, unclear
    whether it will even use the Compose Styles API).
  - Do not add a real Gradle dependency until tailwind-compose has actual published code
    to depend on.
  - When it's ready: add it to `:library`'s `commonMain` dependencies and evaluate using
    its utility modifiers for internal component layout/spacing — without changing the
    existing `Shadcn*` component API surface or the `*Variant`/`Style` pattern.

## Skill routing

| Topic | Skill |
|---|---|
| New design system component | `kotlin-multiplatform-design-system` / `kotlin-multiplatform-design-system-extended` |
| Publishing to Maven Central | `kotlin-multiplatform-library-publishing` |
| iOS / SPM distribution | `kotlin-multiplatform-xcframework-spm` |
| API surface management | `kotlin-multiplatform-library-publishing` (apiCheck / apiDump — not yet wired, see Notes) |
| Platform-specific implementations | `kotlin-multiplatform-expect-actual` |
| Catalog app navigation | `kotlin-multiplatform-navigation` |
| Unit / integration tests | `kotlin-multiplatform-unit-testing` (not yet wired, see Notes) |
| Code quality (detekt, ktlint) | `kotlin-multiplatform-code-quality` |
| CI automation | `kotlin-multiplatform-ci-github-actions` |
| Architecture audit | `kotlin-multiplatform-audit` |
| Harvest consumer lessons | `kotlin-multiplatform-audit` (`--harvest` mode via `/kmm-harvest-lessons`) |

## Module graph

| Module | Purpose |
|---|---|
| `:library` | Published artifact (`io.github.ronjunevaldoz:shadcn-compose`) -- tokens, `ShadcnTheme`, components, styles |
| `:core` | Small shared utility module (currently minimal, stock demo code) |
| `:app:shared` | Catalog/docs app shared code -- navigation, sidebar, per-component doc pages (not published) |
| `:app:androidApp` / `:app:desktopApp` / `:app:webApp` | Catalog app platform entry points (not published) |
| `:app:iosApp` | Xcode project for the catalog app on iOS (not published) |

## Published artifacts

| Artifact | Module |
|---|---|
| `io.github.ronjunevaldoz:shadcn-compose` | `:library` |

## API surface rules

- No `binary-compatibility-validator` wired yet -- consider adding it via
  `kotlin-multiplatform-library-publishing` before the first real Maven Central release,
  so accidental public API breaks are caught in CI.
- Until then, treat any signature change to a public `Shadcn*` component or `styles/*Variant`
  sealed interface as a breaking change requiring a version bump.

## Notes for future sessions

- **The Compose Styles API does not match what the `kotlin-multiplatform-design-system`
  skill assumes.** The real annotation in Compose Multiplatform 1.11.1 is
  `androidx.compose.foundation.style.ExperimentalFoundationStyleApi`, not
  `ExperimentalStylesApi`. `padding()` does not exist on `StyleScope` -- use
  `contentPadding(...)`. Verify any new Style API usage against the real jar
  (`~/.gradle/caches/modules-2/files-2.1/org.jetbrains.compose.foundation/`) with a
  compile spike before writing component code, the same way `library/build.gradle.kts`'s
  history did -- don't trust the skill's code samples verbatim for this API surface.
- **Focus rings are drop shadows, not border-width changes.** shadcn's real focus
  indicator (`focus-visible:ring-[3px] ring-ring/50`) is a paint-only box-shadow that
  never affects layout size. Every interactive component composes the shared
  `styles/FocusRingStyle.kt` (`focusRingStyle`) via `then` for this -- do not reintroduce
  a border-width-reservation hack to fix focus-related resizing bugs.
- **Colors and hover semantics are checked against real shadcn/ui source**, not memory --
  see `ui.shadcn.com/docs/theming` for token values and
  `github.com/shadcn-ui/ui/blob/main/apps/v4/registry/new-york-v4/ui/*.tsx` for exact
  per-variant Tailwind classes (hover treatment, border presence, disabled opacity).
  When adding a new component, fetch its real source first rather than approximating.
- No test infrastructure (`kotlin-multiplatform-unit-testing`, Roborazzi) is wired yet --
  the project has shipped ~12 components across two sprints without automated tests
  beyond compile + lint gates and manual desktop smoke runs. Worth prioritizing before
  the component count grows much further.

## Commands installed

See `.claude/commands/kmm-*.md` for available slash commands.
Key commands:
- `/kmm-run-audit` — architecture audit with per-finding remediation
- `/kmm-harvest-lessons` — collect patterns to upstream to skills
- `/kmm-verify` — full validation pipeline (build + test + apiCheck)
- `/kmm-check-updates` — check for skill updates
