# Agent scaffold templates — `.claude/AGENTS.md` and `CLAUDE.md`

Part of `kmp-expert`. The literal file bodies `/kmp-setup-agents` writes into a project.

These live in a **skill** reference, not in the command or a plugin-root `assets/`
directory, for a concrete reason: `/kmp-setup-agents` is consumer-facing (README tells
users to run it in any existing KMP project), and `update-consumer-skills.sh
--install-commands` copies a command as a single bare `.md` file. Skills, by contrast,
are always deployed. A template referenced from `assets/` would resolve in this repo and
be missing in every consumer project.

Path when reading this from a consumer project: `.claude/skills/kmp-expert/references/`
(or `.agents/skills/kmp-expert/references/` on a non-Claude client). Path in this repo:
`skills/kmp-expert/references/`. Same dual-path convention `kmp-layout-system` already
uses for its bundled script.

Fill every `<placeholder>` from the detected project before writing — never emit a
placeholder literally.

---

## `.claude/AGENTS.md`

### For APP projects

```markdown
# AGENTS.md — <project name>

This project uses [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

<1–2 sentences describing what the app does, its platforms, and group ID>

## Project persona

<1 short paragraph describing the app-specific agent identity>

Examples:
- Todo app: Task Steward — optimize for fast capture, clear prioritization, low-friction completion, and zero clutter.
- Habit app: Coach — keep streaks visible, reduce shame-heavy language, and make progress obvious.
- Finance app: Steward — prioritize clarity, trust, and careful review over flashy automation.

## Skill routing

| Topic | Skill |
|---|---|
| New feature end-to-end | `kmp-feature-scaffold` → `kmp-clean-architecture` → `kmp-mvi` |
| ViewModel / screen state | `kmp-mvi` |
| Navigation | `kmp-navigation` |
| Dependency injection | `kmp-dependency-injection` |
| Code quality / linting | `kmp-code-quality` |
| Unit tests | `kmp-unit-testing` |
| Android CLI / emulator / deploy | `kmp-android-cli` |
| Project docs / onboarding | `kmp-project-docs-maintainer` |
<include only skills detected in Step 2c>
| Auth / login | `kmp-ktor-auth-service` |
| Local database | `kmp-sqldelight-setup` |
| REST API / network | `kmp-network-layer` |
| Key-value settings | `kmp-datastore` |
| Screenshot tests | `kmp-roborazzi` |
| Design system | `kmp-compose-design-system` |
| Architecture audit | `kmp-audit` |
| Harvest consumer lessons | `kmp-audit` (`--harvest` mode via `/kmp-harvest-lessons`) |
</end detected skills>

## Module graph

<list each module group>
| Module | Purpose |
|---|---|
| :composeApp | CMP entry point (Android / iOS / Desktop / Web) |
| :core:common | Shared utilities |
| :feature:auth | Auth feature (domain / data / presenter / ui) |
...

## Commands installed

See `.claude/commands/kmp-*.md` for available slash commands.
Key commands:
- `/kmp-implement-feature <name>` — plan → implement → validate → review
- `/kmp-run-audit` — architecture audit with per-finding remediation
- `/kmp-harvest-lessons` — collect good patterns to upstream to skills
- `/kmp-verify` — full validation pipeline
- `/kmp-execute-ticket <id>` — implement a GitHub issue end-to-end
- `/kmp-fix-design` — scan and fix design system violations
```

### For LIBRARY projects

```markdown
# AGENTS.md — <library name>

This project uses [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

<1–2 sentences: what the library does, target consumers, Maven coordinates>
Group ID: <groupId>   Artifact: <artifactId>   Published to: Maven Central | GitHub Packages

## Skill routing

| Topic | Skill |
|---|---|
| Publishing to Maven Central | `kmp-library-publishing` |
| iOS / SPM distribution | `kmp-xcframework-spm` |
| API surface management | `kmp-library-publishing` (apiCheck / apiDump) |
| Platform-specific implementations | `kmp-expect-actual` |
| Unit / integration tests | `kmp-unit-testing` |
| Code quality (detekt, ktlint) | `kmp-code-quality` |
| CI automation | `kmp-ci-github-actions` |
| Android CLI / emulator / deploy | `kmp-android-cli` |
| Project docs / onboarding | `kmp-project-docs-maintainer` |
| Architecture audit | `kmp-audit` |
| Harvest consumer lessons | `kmp-audit` (`--harvest` mode via `/kmp-harvest-lessons`) |
<add only if detected>
| Screenshot / visual tests | `kmp-roborazzi` |
| BOM / multi-artifact | `kmp-library-publishing` (Step 4) |
| Library mimics a reference API's shape (Modifier/slot DSL) on a custom runtime | `kmp-api-mimicry` |
| Bridges to an existing 3rd-party C/C++ library | `kmp-jni-pro` |
| Authors brand-new first-party C/C++ source (not bridging to an existing library) | `kmp-native-authoring` |
</end>

## Published artifacts

| Artifact | Module |
|---|---|
| <groupId>:<artifactId> | :library |
| <groupId>:<artifactId>-testing | :library-testing (if present) |
| <groupId>:<artifactId>-bom | :bom (if present) |

## API surface rules

- Never remove or rename public symbols without a major version bump
- Run `./gradlew apiDump` after any public API change; commit the `.api` file
- `./gradlew apiCheck` runs in CI and blocks merge if API diff is uncommitted
- Mark internal symbols with `@InternalApi` to exclude from the dump

## Commands installed

See `.claude/commands/kmp-*.md` for available slash commands.
Key commands:
- `/kmp-run-audit` — architecture audit with per-finding remediation
- `/kmp-harvest-lessons` — collect patterns to upstream to skills
- `/kmp-verify` — full validation pipeline (build + test + apiCheck)
- `/kmp-check-updates` — check for skill updates
```

---

---

## `CLAUDE.md`

Only written if `CLAUDE.md` does not already exist in the project root — it stays thin
and just bootstraps Claude Code into the generated runtime:

```markdown
### Claude Code Project Profile

### Load skills context on initialization
--system-prompt-file=".claude/AGENTS.md"

### Default flags
--compact
--verbose=false

### Canonical project-owned agent sources
- docs/reference/ai-collaboration.md
- docs/reference/agent-catalog.md
- agents/
- rules/     (optional overlays only)
- hooks/
- commands/
- skills/

### Ignore generated and vendor directories
--ignore="**/build/**"
--ignore="**/.gradle/**"
--ignore="**/vendor/**"
--ignore="**/third_party/**"
```
