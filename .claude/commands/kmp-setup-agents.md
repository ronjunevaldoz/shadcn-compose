# /kmp-setup-agents $ARGUMENTS

**KMP Agent Skills** — initialize `.claude/` in an existing KMP project so the team
gets agent-driven workflows without running the full scaffold.

`$ARGUMENTS` is optional: a path to the project root (defaults to `.`).

Use this when:
- The project already exists and you're adding `kmp-agent-skills` for the first time
- You want to reset or regenerate the `.claude/` setup after major architecture changes
- A teammate needs to onboard to the agent workflow

Do NOT use this for brand-new projects — `/kmp-new-project` handles agent setup as part of scaffold.

This command deploys **this collection's own** skills/commands into `.claude/`, and it
also scaffolds the project-owned source locations Claude teams should keep in Git:
`agents/`, `rules/`, `hooks/`, `commands/`, `skills/`, `docs/reference/ai-collaboration.md`,
and a thin `CLAUDE.md`. For a project's **own custom** command, agent, skill, or hook —
one that doesn't come from `kmp-agent-skills` — author it in those project-owned
locations first, then deploy a copy into `.claude/`. Never author a project-specific
artifact directly into `.claude/` as its only copy.

---

## Step 1 — Locate and validate the project

Resolve `$ARGUMENTS` as the project root (default `.`). Confirm it is a KMP project by
checking for at least one of:
- `settings.gradle.kts` or `settings.gradle`
- `gradle/libs.versions.toml`
- A `build.gradle.kts` with `kotlin("multiplatform")` or `id("com.android.kotlin.multiplatform.library")`

If none of these exist, stop and tell the user this command is for KMP projects only.

Print:
```
PROJECT: <project root>
```

---

## Step 2 — Detect project type and discover the module graph

### 2a — Determine: app or library?

Read `settings.gradle.kts` and all root/module `build.gradle.kts` files.

**Library signals** (if any are present → treat as library project):
- `com.vanniktech.maven.publish` plugin applied
- `org.jetbrains.kotlinx.binary-compatibility-validator` plugin applied
- `maven-publish` plugin applied without `com.android.application` anywhere
- No module with `com.android.application` or `androidApplication` plugin
- No `:composeApp`, `:androidapp`, `:app` module that uses the application plugin

**App signals** (default if no library signals detected):
- `com.android.application` or `androidApplication` plugin present
- `:composeApp`, `:app`, or `:androidApp` module exists with application plugin

Print:
```
Project type: APP | LIBRARY
```

### 2b — Module graph

Read `settings.gradle.kts` and extract all included modules. Group them:

**For APP projects:**
```
Modules discovered:
  :app / :composeApp / :androidApp    — entry points
  :core:common, :core:network, ...    — core modules
  :feature:auth:*                     — feature layers
  ...
```

**For LIBRARY projects:**
```
Modules discovered:
  :library (or main artifact module)  — published artifact
  :library-testing                    — test helpers for consumers (if present)
  :bom                                — Bill of Materials (if present)
  :sample / :sample:androidApp        — sample app (not published)
```

### 2c — Detect active skills from `gradle/libs.versions.toml`

**Always include, regardless of detected signals** (matches `/kmp-new-project`'s Step 5
mandatory baseline — a project scaffolded via either command must end up with the same
routing, not depend on which command happened to initialize it):
- `code-quality` — Ktlint/Detekt is a baseline expectation, not library-specific
- `unit-testing` — same reasoning; don't gate this behind a `turbine` signal alone
- `android-cli` — Android target build/deploy/emulator tooling applies whenever an
  Android target exists, which every app project and most libraries have
- `project-docs-maintainer` — README/onboarding upkeep, not tied to any dependency

**App projects additionally check for:**
- `koin` → dependency-injection
- `ktor` → network-layer
- `sqldelight` → sqldelight-setup
- `androidx.datastore` → datastore
- `roborazzi` → roborazzi
- navigation libraries → navigation

**Library projects additionally check for:**
- `vanniktech` or `maven.publish` → library-publishing
- `binary-compatibility-validator` → library-publishing (apiCheck)
- `dokka` → library-publishing (Javadoc jars)
- `iosX64`, `iosArm64` targets in build files → xcframework-spm
- `@DslMarker`-annotated types with names ending in `Modifier`/`Scope`/`UiDsl`, or a
  `MIRROR_MAP.md` at the project root → api-mimicry
- `CMakeLists.txt` or `*.def` cinterop files present → check whether the referenced
  native code already exists as a 3rd-party/vendored source (→ jni-pro) or is authored
  first-party in this repo (→ native-authoring); read the file paths, don't assume

Print the detected skill set — always-included skills first, then signal-detected ones.

---

## Step 3 — Check for existing `.claude/` setup

Look for:
- `.claude/AGENTS.md` — already initialized?
- `.claude/commands/kmp-*.md` — commands already installed?
- `.claude/skills/` — skills already deployed?
- `.claude/settings.json` — permissions already set?

If any exist, print their current state and ask:
```
.claude/AGENTS.md already exists. Overwrite or skip? [overwrite/skip]
.claude/commands/ has N kmp-*.md files. Update or skip? [update/skip]
```

Proceed based on the answer. Default is `skip` if the user presses Enter.

---

## Step 4 — Generate `.claude/AGENTS.md`

Write (or overwrite) `.claude/AGENTS.md` tailored to the detected project type,
module graph, and skill set.

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

## Step 5 — Scaffold project-owned source locations (MANDATORY — do not skip)

This step is not optional and not secondary to Step 4. `.claude/` is a **deployed
runtime copy** — these root-level paths are the actual git-tracked source of truth this
whole scaffold exists to protect. A setup that only produces `.claude/` and stops has not
finished, even if `.claude/AGENTS.md` looks complete on its own.

Create these project-owned paths if they do not exist yet:

```
agents/README.md
rules/README.md
hooks/README.md
commands/README.md
skills/README.md
docs/reference/ai-collaboration.md
docs/reference/agent-catalog.md
```

Each README should say what belongs there and that `.claude/` is the deployed runtime
copy, not the only source of truth. `skills/README.md` should include a minimal
`skills/<name>/SKILL.md` starter template so the first project-owned custom skill has a
correct frontmatter shape from day one.

`docs/reference/ai-collaboration.md` should explain:
- `CLAUDE.md` is a thin bootstrap that points to `.claude/AGENTS.md`
- project-specific artifacts live in `agents/`, `rules/`, `hooks/`, `commands/`, `skills/`
- `docs/reference/ai-collaboration.md` is the canonical explanation of that layout
- `rules/` is optional for assistant-specific overlays and must not duplicate the canonical policy doc
- `docs/*` owns stable project design; `skills/*` owns repo-local execution guidance
- `.claude/settings.json` owns runtime permissions and hook wiring
- any edit to a project-owned skill must be re-deployed into **both** `.agents/skills/`
  (the cross-client target) and `.claude/skills/` (Claude's own mirror) — `update-
  consumer-skills.sh` handles both automatically, so this is one command, not two
  manual copies

`docs/reference/agent-catalog.md` should explain:
- provider-neutral model tiers such as `flagship-coding`, `balanced-coding`, `fast-utility`, `precision-review`
- provider-specific model mapping belongs in one canonical doc, not in every agent file
- `AGENTS.md` / `CLAUDE.md` / `GEMINI.md` should point back to this catalog instead of hardcoding stale model names

If any of these files already exist, print their current contents and skip unless the
user explicitly asks to overwrite them.

### Library-specific maintainer agents (project-owned, optional)

The generic `agents/*.md` roster this collection ships (`planner`, `implementer`,
`reviewer`, `fixer`, ...) is domain-agnostic — none of them own a library's own
sub-domain concept, like a mimicked UI DSL's mirror-map staying honest, or a native core
staying separated from its JNI/cinterop bridge. When a library has a real, distinct
sub-domain like that, author a project-owned maintainer agent the same way a project
authors a custom skill: `agents/<name>-maintainer.md` at the project root, deployed to
`.claude/agents/<name>-maintainer.md`.

Only do this when the sub-domain is real and ongoing — a one-off task doesn't need a
standing agent. Two concrete cases this collection's own skills already point at:

**A library that mimics a reference API's shape** (`kmp-api-mimicry`):

```markdown
# <library name> — UI DSL Maintainer

Owns: keeping the mimicked API shape honest against `MIRROR_MAP.md`.

## When to use
- Adding a new mimicked primitive (a new `*Modifier` function, a new slot composable)
- Reviewing whether a change quietly started claiming real reference-API behavior
  (recomposition, skipping) this library doesn't actually provide

## Checklist
1. Does `MIRROR_MAP.md` have a row for every mimicked primitive touched this session?
2. Does the naming avoid fusing the target runtime's name with the reference API's own
   type name (see `kmp-api-mimicry`'s naming-placeholder guidance)?
3. Does any new doc comment or README line imply real compiler-plugin behavior
   (`@Composable`-compatible, "supports recomposition") that isn't actually true?

Load `kmp-api-mimicry` for the underlying method; this agent only owns
enforcing it stays applied as the library grows.
```

**A library with a first-party native core** (`kmp-native-authoring` +
`kmp-jni-pro`):

```markdown
# <library name> — Native Core Maintainer

Owns: the boundary between this library's own C/C++ core and its JNI/cinterop bridge.

## When to use
- Adding a new native function that needs a Kotlin-side binding
- Reviewing whether JNI glue is being written before the native core's own public
  header API has stabilized (see `kmp-native-authoring`'s handoff point)

## Checklist
1. Is the new native function's public C-ABI signature in `native/include/` before any
   `external fun` is written on the Kotlin side?
2. Does the bridge stay a marshalling-only C-shim, with no reimplemented logic
   (`kmp-jni-pro`'s Phase 0 discovery + EP-1)?
3. Are native-side tests (ctest/gtest) passing independently of the Kotlin test suite?

Load `kmp-native-authoring` for authoring the C/C++ core itself, and
`kmp-jni-pro` for the bridge — this agent owns the boundary between them.
```

Both examples are starting points, not fixed templates — the real checklist should
reflect the project's actual `MIRROR_MAP.md`/native layout, not be copied verbatim.

**Gate — verify before proceeding:**

```bash
ls agents/README.md rules/README.md hooks/README.md commands/README.md skills/README.md \
   docs/reference/ai-collaboration.md docs/reference/agent-catalog.md
```

Every path must exist on disk. If any are missing, create them now — do not move to
Step 6 with a partial scaffold, and do not rely on Step 10's summary to catch it later;
that summary reports what this gate already confirmed, not a fresh check.

---

## Step 6 — Install consumer commands

Locate the `kmp-agent-skills` clone. Check in order:
1. `$ARGUMENTS/../kmp-agent-skills`
2. `~/dev/kmp-agent-skills`
3. Ask the user for the path

Copy the consumer command set to `.claude/commands/`:

```
Consumer commands (safe to install):
  kmp-implement-feature.md      — implement a new feature
  kmp-run-audit.md              — architecture audit + auto skill-gap reporting
  kmp-harvest-lessons.md        — collect positive patterns; auto-propose GitHub issues
  kmp-audit-adaptive.md         — adaptive layout coverage + redundant title check
  kmp-generate-palette.md       — generate AppColors + preview from N brand seed colors
  kmp-vectorize.md              — compile raster/SVG into Kotlin ImageVector (no PNG icons)
  kmp-review-changes.md         — review git diff against architecture rules
  kmp-verify.md                 — full validation pipeline
  kmp-execute-ticket.md         — implement a GitHub issue end-to-end
  kmp-fix-design.md             — fix design system violations
  kmp-audit-screenshots.md      — visual audit of Roborazzi goldens
  kmp-record-design-baselines.md — record new golden PNGs
  kmp-audit-design-visual.md    — cross-screen visual consistency check
  kmp-update-design-system.md   — pull latest design system components
  kmp-update-skills.md          — pull latest skills and re-deploy
  kmp-report-skill-issue.md     — file a skill bug report
  kmp-check-updates.md          — check for skill updates
```

Do NOT copy repo-internal commands: `kmp-new-skill.md`, `kmp-modify-skill.md`,
`kmp-maintain-docs.md`, `kmp-release-notes.md`, `kmp-setup-hooks.md`,
`kmp-new-project.md`, `kmp-setup-agents.md`.

For each file: if it already exists in `.claude/commands/` and the content differs,
show a one-line diff summary and ask `[update/skip]` before overwriting.

---

## Step 6a — Deploy to Codex and Gemini (ask first, project-scoped only)

Ask the user: "Also deploy this project's agents/commands to Codex CLI and/or Gemini
CLI? [codex/gemini/both/skip]" — never deploy silently, this is a persistent addition
to the project's own repo, not just this machine's home directory.

**Real, verified capability per provider — do not assume symmetry** (see
`docs/reference/ai-collaboration.md`'s Per-Provider Capability Matrix):
- Codex CLI: subagents only (`.codex/agents/*.toml`), no custom-commands mechanism
- Gemini CLI: commands only (`.gemini/commands/*.toml`), no confirmed subagent mechanism

If the user chose `codex` or `both`, for each file in `agents/*.md` (this project's
own project-owned agent sources — not `kmp-agent-skills`' own `agents/` directory,
which are internal to that repo, not deployable), translate to
`.codex/agents/<name>.toml`:

```toml
name = "<from the .md frontmatter's name field>"
description = "<from the .md frontmatter's description field>"
developer_instructions = """
<the .md file's body, verbatim>
"""
```

Only include `model` if the source frontmatter's `model:` value is a real, verified
Codex model id — check `docs/reference/agent-catalog.md`'s Mapping Rule table for the
current one rather than guessing; omit the field entirely if unverified.

If the user chose `gemini` or `both`, for each file in `commands/*.md` (this
project's own project-owned command sources), translate to
`.gemini/commands/<name>.toml`:

```toml
description = "<one-line summary — the command file's first heading/description line>"
prompt = """
<the .md file's body, with every $ARGUMENTS occurrence rewritten to {{args}}>
"""
```

**Tell the user explicitly, in the same message, that translated content may
reference Claude-specific tool names or conventions (Read/Edit/Bash/Skill) that don't
map cleanly to Codex/Gemini's own tool surface — review the generated TOML before
relying on it, this isn't a guaranteed verbatim port.**

---

## Step 7 — Deploy skills

If `.claude/skills/` does not exist, create it and copy all skills from the
`kmp-agent-skills/skills/` directory.

If `.claude/skills/` already exists, run the equivalent of `update-consumer-skills.sh`
to sync changed skills without prompting for each file (skills are passive docs).
That sync includes both the shared `kmp-agent-skills` bundle and any project-owned
custom skills under `skills/<name>/`.

**Also deploy to `.agents/skills/`** — the project-level half of agentskills.io's
cross-client convention (verified in `docs/reference/agentskills-io-standards.md`;
the global sync script covers the user-level half at `~/.agents/skills`). Mirror the
same copy into `.agents/skills/` so any agentskills.io-compliant client working in this
project sees the same skills, not just Claude Code.

---

## Step 7a — Seed `.agents/pipeline-context.json`

If `.agents/pipeline-context.json` does not already exist, write it so the `planner`
agent has project context from the first run instead of starting cold — this was
previously only seeded for brand-new projects via `/kmp-new-project`, never for a
project being initialized after the fact. Not under `.claude/`: `agents/planner.md`'s
body is copied verbatim into `.codex/agents/planner.toml` if the user opts into Codex
deployment (Step 6a above) — `.agents/` is the cross-client-neutral location:

```json
{
  "project": "<project name, from settings.gradle.kts rootProject.name>",
  "group_id": "<group ID, from gradle.properties or root build.gradle.kts>",
  "platforms": ["<platforms detected from the module graph in Step 2b>"],
  "skills_used": ["<the detected skill set from Step 2c, always-included + signal-detected>"],
  "recurring_issues": [],
  "proven_patterns": []
}
```

If it already exists, print its current contents and skip — don't overwrite a project's
accumulated `recurring_issues`/`proven_patterns` history.

---

## Step 8 — Write `CLAUDE.md`

If `CLAUDE.md` does not exist in the project root, create a minimal one that tells
Claude Code where the skills live and where the canonical project-owned agent policy
is maintained:

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

If `CLAUDE.md` already exists, print its contents and skip — do not overwrite.

---

## Step 9 — Write `.claude/settings.json`

If `.claude/settings.json` does not exist, create it with a Bash allowlist for
common read-only and build operations:

```json
{
  "permissions": {
    "allow": [
      "Bash(./gradlew *)",
      "Bash(git status)",
      "Bash(git diff*)",
      "Bash(git log*)",
      "Bash(python3 .claude/skills/kmp-audit/scripts/*)",
      "Bash(find . -name *.kt*)",
      "Bash(grep *)"
    ]
  }
}
```

If it already exists, print the current permissions and skip — do not overwrite.

---

## Step 10 — Summary

**Before printing this summary, re-verify each line — do not print `✅` from the
template blindly.** Run the same check Step 5's gate already ran
(`ls agents/README.md rules/README.md hooks/README.md commands/README.md
skills/README.md`) plus `.claude/AGENTS.md`, `.claude/commands/`, `.claude/skills/`,
`.agents/skills/`, `.agents/pipeline-context.json`, `.claude/settings.json`. Print `✅`
only for a path that actually exists on disk right now; print `❌ missing` for anything
that doesn't, and go back and create it before telling the user setup is complete. Never
print a raw `<if>`/`</if>` tag — resolve the Codex/Gemini lines to plain text, present
only when actually deployed.

```
AGENT SETUP COMPLETE
─────────────────────
Project:   <name> (<root>)
Features:  <N> detected (<list>)
Skills:    <N> deployed → .agents/skills/ (cross-client) + .claude/skills/ (mirror)

Generated:
  ✅ agents/ rules/ hooks/ commands/ skills/   — project-owned source scaffold
  ✅ docs/reference/ai-collaboration.md        — canonical cross-agent policy
  ✅ CLAUDE.md                                 — thin bootstrap into `.claude/AGENTS.md`
  ✅ .claude/AGENTS.md                         — skill routing tailored to this project
  ✅ .claude/commands/                         — <N> consumer commands installed
  ✅ .agents/skills/                           — <N> skills deployed (cross-client, primary)
  ✅ .claude/skills/                           — same <N> skills, Claude Code's own mirror
  ✅ .agents/pipeline-context.json             — project context seeded for the planner agent
  ✅ .claude/settings.json                     — Bash allowlist + hook wiring home
  ✅ .codex/agents/                            — <N> subagents translated to TOML (only if Codex was deployed)
  ✅ .gemini/commands/                         — <N> commands translated to TOML (only if Gemini was deployed)

Detected skill set:
  <list of skills matched from libs.versions.toml>

Not yet wired: git/CI architecture hooks (pre-commit audit, PostToolUse validation).
Run /kmp-setup-hooks now to add them — recommended for every team project.

Try it now:
  /kmp-setup-hooks               — wire git pre-commit + PostToolUse architecture hooks
  /kmp-run-audit                 — check architecture health (auto-reports skill gaps)
  /kmp-harvest-lessons           — collect good patterns; propose GitHub issues upstream
  /kmp-implement-feature <name>  — add a new feature
  /kmp-verify                    — run full validation pipeline
```

---

## Notes

- Run this again after major architecture changes (adding/removing features, changing
  the module graph) to regenerate `AGENTS.md` with the current structure.
- Skills are passive docs — re-running always syncs them safely.
- Commands are only overwritten with explicit `[update]` confirmation.
- `settings.json` is never overwritten — add permissions manually if needed.
- Keep project-owned artifacts in the root scaffold even if they only contain README
  placeholders today; that empty scaffold prevents future edits from drifting straight
  into `.claude/`.
