# Phase 5 — Verify, agent setup, summary (Steps 9-11)

Part of `kmp-expert` — a phase of the `/kmp-new-project` pipeline.
Run last. Ends with the printed project summary.

Load this file when the command reaches this phase; do not load all phases up front. The command itself holds the phase index and the gates between them.

---

## Step 9 — Record goldens + run `/kmp-verify` [App]

After all sprints are complete, record Roborazzi golden images first — screenshot tests
always fail on a fresh project if goldens haven't been recorded yet:

```bash
./gradlew recordRoborazziJvm
git add src/**/roborazzi/**/*.png
git commit -m "test: record initial Roborazzi screenshot goldens"
```

Then run the full validation pipeline:

```bash
/kmp-verify .
```

This runs:
- Architecture audit
- ktlint + detekt
- jvmTest (unit tests + Roborazzi diffs against the just-recorded goldens)
- Visual design audit on screenshot goldens

Fix any blockers. Do not mark the project complete until `/kmp-verify` reports `RESULT: PASS`.

## Step 9 — API dump + local publish smoke test [Library]

After all sprints are complete, generate the initial API dump if not already committed
from Step 8's per-task `apiDump` calls, then confirm the library actually resolves as a
Maven artifact before calling it done — the release checklist's own
`./gradlew publishToMavenLocal` smoke test, run now instead of waiting for the first
real release:

```bash
./gradlew apiDump
git add library/api/
git commit -m "chore: initial API dump" --allow-empty

./gradlew build test apiCheck
./gradlew publishToMavenLocal
```

Then verify a throwaway consumer project can actually resolve it from `mavenLocal()`
before reporting success — an artifact that only builds in isolation but never resolves
as a real dependency isn't actually done. Fix any blockers. Do not mark the project
complete until `apiCheck`/`build`/`test` all pass and the local-publish resolve succeeds.

---

## Step 10 — Generate agent setup

After verify passes, set up the agent scaffold — then write the two things that are
specific to a brand-new project and have no equivalent in an existing one.

### 10a — Delegate the agent setup to `/kmp-setup-agents`

**Run `/kmp-setup-agents` rather than re-implementing it here.** That command already
owns, and is the single source of truth for, every piece of the agent scaffold:

| What it writes | Its step |
|---|---|
| `.claude/AGENTS.md` (App and Library variants) — body lives in `kmp-expert`'s `references/agents-md-templates.md` | Step 4 |
| `agents/`, `rules/`, `hooks/`, `commands/`, `skills/` scaffold + `CLAUDE.md` + `docs/reference/ai-collaboration.md` + `docs/reference/agent-catalog.md` | Step 5 |
| Consumer command set into `.claude/commands/` | Step 6 |
| Codex / Gemini translation (opt-in, never silent) | Step 6a |
| `.agents/pipeline-context.json` | Step 7a |
| `.claude/settings.json` Bash allowlist | Step 9 |
| Skills into `.claude/skills/` + the `.agents/skills/` cross-client mirror | Step 8 |

Pass it the values already collected in Step 1 (`PROJECT_NAME`, `GROUP_ID`,
`PROJECT_TYPE`, `PLATFORMS`) plus the module graph and skill list this run actually
produced, so its generated `AGENTS.md` reflects the real project instead of re-deriving
it.

> **Why delegation, not a copy:** this step previously inlined its own duplicate of
> those templates. The two copies drifted — the `[Library]` `AGENTS.md` written here
> had lost five skill-routing rows (`kmp-roborazzi`, `kmp-api-mimicry`, `kmp-jni-pro`,
> `kmp-native-authoring`, BOM) and used different placeholder names (`<PROJECT_NAME>`
> vs `<artifactId>`) than the one `/kmp-setup-agents` writes. A library scaffolded
> through this command silently got a worse `AGENTS.md` than the same project set up
> through that one. One template, one owner, no drift.

### 10b — Write the project `README.md`

`/kmp-setup-agents` deliberately does not touch a project's own `README.md` — it runs
against existing projects that already have one. A new project doesn't, so write it here.

**[App]:**

```markdown
# <PROJECT_NAME>

<WHAT_IT_DOES>

## Platforms

<list platforms from intake>

## Build

```bash
./gradlew :app:androidApp:assembleDebug              # Android APK
./gradlew :app:shared:assembleSharedReleaseXCFramework  # iOS XCFramework (if iOS target) — open app/iosApp/iosApp.xcodeproj in Xcode to build/run the app itself
./gradlew jvmTest                                     # All tests
```

## Architecture

6-layer clean architecture per feature: `:model` → `:api` → `:domain` → `:data` → `:presenter` → `:ui`

See `docs/architecture.md` for the full structure.

## Agent workflows

Install [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills), then:
- `/kmp-implement-feature <name>` — add a feature end-to-end
- `/kmp-run-audit` — check architecture health
- `/kmp-verify` — full validation pipeline
```

**[Library]:**

```markdown
# <PROJECT_NAME>

<WHAT_IT_DOES>

## Install

```kotlin
// build.gradle.kts
dependencies {
    implementation("<GROUP_ID>:<PROJECT_NAME>:<VERSION>")
}
```

Published to <PUBLISH_TARGET>. See [releases](../../releases) for the latest version.

## Platforms

<list platforms from intake>

## Build

```bash
./gradlew build test           # build + unit tests, all targets
./gradlew apiCheck              # verify public API surface is unchanged
./gradlew publishToMavenLocal   # smoke-test a local consumer can resolve it
```

## API stability

This library uses `explicitApi()` and `binary-compatibility-validator` — every public
declaration is deliberate, and `library/api/library.api` tracks the full surface.
See `kmp-library-publishing`'s semver classification table before bumping versions.

## Agent workflows

Install [kmp-agent-skills](https://github.com/ronjunevaldoz/kmp-agent-skills), then:
- `/kmp-run-audit` — check architecture health
- `/kmp-verify` — full validation pipeline (build + test + apiCheck)
```

### 10c — [App] Finalize `docs/`

`docs/architecture.md` and `docs/layout-system/` already exist from Step 4's F-03
(written immediately after confirmation, not deferred to here). Append `## Features` and
`## Stack` now that implementation is complete — the sprint plan and
`libs.versions.toml` weren't final back at Step 4:

```markdown
## Features

<list of features from the sprint plan>

## Stack

<key libraries and versions from libs.versions.toml>
```

Then write the ADRs, which only exist once the tech choices are actually made:

```
docs/decisions/          — Architecture Decision Records (ADRs), written now
  001-mvi-pattern.md
  002-sqldelight-vs-room.md   (if SQLDelight was chosen)
  003-koin-di.md
```

---

## Step 11 — Summary

Print a summary of everything generated. The two templates below are **fully separate**
— pick the one matching `PROJECT_TYPE` and print it as-is with placeholders filled in.
**Never print an `<if>`/`</if>` tag itself** — those markers exist only in this
command's source to keep one file instead of two; the actual terminal output must be
plain text, fully resolved, with consistent column alignment (pad labels to the widest
one in each block).

**`[App]` template:**

```
## Project complete

App:       <name> — <one-line description>
Platforms: <platforms from intake>

Features: <N> implemented
  [x] F-01  Project scaffold
  [x] F-02  Clean architecture
  [x] F-03  <feature>

Generated:
  Modules:      <N> Gradle modules
  Source files: <N> .kt files
  Tests:        <N> unit tests, <N> Roborazzi screenshot tests
  Screenshots:  <N> PNG goldens (<N> light, <N> dark)

Docs:
  README.md              — project overview, build commands, architecture link
  PLAN.md                — MVP scope + delivery plan, checked off as sprints complete
  docs/architecture.md   — 6-layer rules, module map, stack
  docs/decisions/        — ADRs for key tech choices
  docs/layout-system/    — ASCII wireframes per screen

Agent setup:
  agents/ rules/ hooks/ commands/ skills/  — project-owned source scaffold
  docs/reference/ai-collaboration.md       — canonical cross-agent policy
  CLAUDE.md                                — thin bootstrap into .claude/AGENTS.md
  .claude/AGENTS.md                        — skill routing + feature module table
  .claude/commands/kmp-*.md                — <N> slash commands installed
  .claude/skills/ + .agents/skills/        — <N> skills deployed to both (cross-client)
  .agents/pipeline-context.json            — project context for the planner agent
  .claude/settings.json                    — Bash allowlist + hook wiring home
  (if deployed) .codex/agents/             — <N> subagents translated to TOML
  (if deployed) .gemini/commands/          — <N> commands translated to TOML

Verify:      PASS
Skills used: <list>

Not yet wired: git/CI architecture hooks (pre-commit audit, PostToolUse validation).
Run /kmp-setup-hooks now to add them — recommended for every team project.

Next steps:
  ./gradlew :app:androidApp:assembleDebug                 — build Android APK (if Android in platforms)
  ./gradlew :app:shared:assembleSharedReleaseXCFramework   — build iOS XCFramework, then open app/iosApp/iosApp.xcodeproj (if iOS in platforms)
  ./gradlew :app:desktopApp:run                            — run Desktop app (if Desktop in platforms)
  ./gradlew jvmTest                                        — run all tests
  /kmp-setup-hooks                                         — wire git/CI architecture hooks
  /kmp-implement-feature <name>                            — add your next feature
```

**`[Library]` template:**

```
## Project complete

Library:   <name> — <one-line description>
Platforms: <platforms from intake>

API surfaces: <N> implemented
  [x] F-01  Project scaffold (library-publishing structure)
  [x] F-02  <public API task>

Generated:
  Modules:      <N> Gradle modules
  Source files: <N> .kt files
  Tests:        <N> unit tests
  API dump:     library/api/library.api (<N> public declarations)

Docs:
  README.md   — install instructions, API stability notes
  PLAN.md     — MVP scope + delivery plan, checked off as sprints complete

Agent setup:
  agents/ rules/ hooks/ commands/ skills/  — project-owned source scaffold
  docs/reference/ai-collaboration.md       — canonical cross-agent policy
  CLAUDE.md                                — thin bootstrap into .claude/AGENTS.md
  .claude/AGENTS.md                        — skill routing + published artifacts + API surface rules
  .claude/commands/kmp-*.md                — <N> slash commands installed
  .claude/skills/ + .agents/skills/        — <N> skills deployed to both (cross-client)
  .agents/pipeline-context.json            — project context for the planner agent
  .claude/settings.json                    — Bash allowlist + hook wiring home
  (if deployed) .codex/agents/             — <N> subagents translated to TOML
  (if deployed) .gemini/commands/          — <N> commands translated to TOML

Verify:      PASS
Skills used: <list>

Not yet wired: git/CI architecture hooks (pre-commit audit, PostToolUse validation).
Run /kmp-setup-hooks now to add them — recommended for every team project.

Next steps:
  ./gradlew apiCheck              — verify public API surface
  ./gradlew publishToMavenLocal   — smoke-test local resolution
  /kmp-setup-hooks                — wire git/CI architecture hooks
  /kmp-implement-feature <name>   — add your next API surface
```

---

