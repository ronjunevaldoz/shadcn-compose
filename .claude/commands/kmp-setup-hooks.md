# /kmp-setup-hooks

**KMP Agent Skills** — wire the provided hooks into your project so the pipeline
enforces architecture rules automatically, without requiring you to remember to run them.

There are two independent integration points: **git hooks** (for your local repo) and
**Claude Code hooks** (for the AI agent). Both are optional but strongly recommended.

---

## The hooks

| Hook file | What it does | When it runs |
|---|---|---|
| `hooks/pre-commit-audit.sh` | Runs `audit_project.py` before any commit touching `.kt`/`.kts` files. Blocks the commit if architecture smells are found. | `git commit` |
| `hooks/commit-msg` | Enforces Conventional Commit format and rejects any commit carrying a `Co-Authored-By:` trailer (commits — including an AI agent's — aren't attributed to the agent). | `git commit`, wired per Option A |
| `hooks/validate-architecture.sh` | Runs `audit_project.py` after any file edit. Surfaces findings inline in the agent's output. | After every `Edit`/`Write` |
| `hooks/check-skill-freshness.sh` | Warns when a skill's `last-updated` is >90 days old. Non-blocking. | Manually or scheduled CI |
| `hooks/session-start-check-updates.sh` | Wraps `scripts/check_updates.py` — warns the agent up front if this repo's skills are behind `origin/main`. Non-blocking, always exits 0. | Every Claude Code session start (kmp-agent-skills clone only) |
| `scripts/check-installed-skills-version.sh` | Compares a *deployed* `skills/` copy's version marker against the latest GitHub Release. Non-blocking when wrapped per Option E. | Every Claude Code session start (any consumer project with a deployed copy) |
| `gitleaks` (via `pre-commit` framework) | Scans staged changes for API keys/passwords/tokens before they're committed. Platform-independent — scans source text, not compiled output. Blocks the commit if a secret is found. | `git commit`, wired per Option F |
| `hooks/block-computer-use-for-compose.sh` | Blocks `mcp__computer-use__*` tool calls in a Compose Multiplatform project — forces the agent onto Roborazzi/`runComposeUiTest` instead of manually driving the app. | Before any `mcp__computer-use__*` call, wired per Option G |
| `hooks/block-edit-vendored-skills.sh` | Blocks `Edit`/`Write` calls targeting a deployed skill mirror (`.claude/skills/`, `.agents/skills/`, `.codex/skills/`, `.gemini/skills/`) instead of the real source. | Before any `Edit`/`Write`, wired per Option H |

---

## Option A — Git pre-commit + commit-msg hooks (local repo)

Wires both `pre-commit-audit.sh` (architecture smells) and `commit-msg` (Conventional
Commit format, no `Co-Authored-By` trailer) into your project's `.git/hooks/` in one
step — this is exactly what `scripts/install-hooks.sh` does, so use it instead of
symlinking each hook by hand (a manual `ln -sf` only wires whichever one you remember
to run — this repo's own `.git/hooks/` went unwired for `commit-msg` for a while for
exactly that reason):

```bash
# Run from your KMP project root (not the skills repo root):
bash "<path-to-skills-repo>/scripts/install-hooks.sh"
```

This symlinks `pre-commit-audit.sh` → `pre-commit`, `validate-architecture.sh` →
`post-rewrite`, and `commit-msg` → `commit-msg`, and `chmod +x`s each source file.

Test it:
```bash
# pre-commit: should print "Running architecture audit..." and exit 0 on a clean project
git commit --allow-empty -m "test: hook check"

# commit-msg: should be rejected — no Co-Authored-By trailer allowed
git commit --allow-empty -m "$(printf 'chore: test\n\nCo-Authored-By: Claude <noreply@anthropic.com>')"
```

To remove:
```bash
rm .git/hooks/pre-commit .git/hooks/post-rewrite .git/hooks/commit-msg
```

If you only want the pre-commit architecture check without the commit-msg format
enforcement (or vice versa), symlink that one file directly instead of running the
installer:
```bash
ln -sf "<path-to-skills-repo>/hooks/pre-commit-audit.sh" .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

---

## Option B — Claude Code PostToolUse hook (agent auto-check)

Configures Claude Code to run `validate-architecture.sh` after every file edit. This
means architecture issues appear in the agent's output immediately after writing code,
not only when a commit is made.

Add to your Claude Code `settings.json` (open via **Claude Code → Settings → Hooks**):

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "<path-to-skills-repo>/hooks/validate-architecture.sh \"$CLAUDE_TOOL_INPUT_FILE_PATH\""
          }
        ]
      }
    ]
  }
}
```

Replace `<path-to-skills-repo>` with the absolute path to the skills repo on your machine.

**Effect:** after the agent edits any `.kt`, `.kts`, or `.md` file, the audit runs
automatically. If it finds a smell, the result appears in the tool output and the agent
will address it before continuing.

---

## Option C — CI scheduled check (skill freshness)

Adds `check-skill-freshness.sh` to your CI pipeline as a weekly scheduled job:

```yaml
# .github/workflows/skill-freshness.yml
name: Skill Freshness Check
on:
  schedule:
    - cron: '0 9 * * 1'   # Every Monday at 09:00 UTC
  workflow_dispatch:

jobs:
  freshness:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Check skill freshness
        run: bash hooks/check-skill-freshness.sh
```

This is non-blocking by default — it prints warnings but does not fail the workflow.
To make it blocking, add `set -e` at the top of `check-skill-freshness.sh`.

---

## Option D — Claude Code SessionStart hook (skill update check)

Only applicable when you're working from a clone of `kmp-agent-skills` itself (or a fork
tracking it as `origin`) — `check_updates.py` compares your local checkout against
`origin/main`, so it has nothing to compare against inside a project that only has a
deployed `skills/` copy. Already wired into this repo's own `.claude/settings.json`:

```json
{
  "hooks": {
    "SessionStart": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "bash hooks/session-start-check-updates.sh"
          }
        ]
      }
    ]
  }
}
```

**Effect:** every new session opened in this repo prints an up-to-date/behind status
before any work starts, instead of relying on a maintainer to remember `/check-updates`.
Always exits 0 — a stale-skills warning should never block starting a session.

---

## Option E — Claude Code SessionStart hook (installed skills version check, consumer projects)

Applicable to any project with a **deployed** (non-git) `skills/` copy — one synced via
`sync-local-assistant-skills.sh` or `update-consumer-skills.sh`, either of which writes a
`.kmp-agent-skills-version` marker file. Unlike Option D (which needs a git clone tracking
`origin/main`), this works from just the deployed copy plus network access — one `curl`
call against the latest GitHub Release, no git required:

```json
{
  "hooks": {
    "SessionStart": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "bash <path-to-skills-repo>/scripts/check-installed-skills-version.sh .claude/skills; exit 0"
          }
        ]
      }
    ]
  }
}
```

Pass the project's actual deployed skills path as the argument (`.claude/skills` for a
project-local deploy, or omit it entirely to check the default global
`~/.claude/skills`). The trailing `; exit 0` matters — the script's own exit codes (1 =
update available, 2 = no marker or unreachable) are meaningful for a human running it
directly, but a SessionStart hook must never fail the session over a stale-skills
warning, so the wrapper always succeeds regardless.

**Why SessionStart, not a per-skill-invocation interceptor:** a version check needs one
network round-trip: cheap once per session, wasteful (and slow) if repeated on every
single skill load within that session — skills don't go stale mid-session. SessionStart
gives the same "notice a stale install early" value at a fraction of the cost, and
degrades safely offline (exit 2, printed, session continues).

---

## Option F — Secrets scan before commit (gitleaks, all platforms)

Catches an API key, password, or token about to be committed — before it ever reaches
git history. Platform-independent by nature: it scans git source content (regex over
text), not compiled output, so one scan covers Android (`google-services.json`),
iOS (`Info.plist`, provisioning profiles), Desktop, and Web/Wasm equally — nothing
platform-specific to configure.

Real tool, verified against its own docs: [gitleaks](https://github.com/gitleaks/gitleaks)
(`brew install gitleaks`), exits non-zero when a secret is found (gates the commit), and
supports a `.gitleaks.toml` allowlist for known false positives.

**Wired via the `pre-commit` framework** (a separate real tool — `pip install
pre-commit`), not a raw script symlink like Option A. Reason: gitleaks' own CLI flags
have changed across versions (its old `--staged` flag was deprecated), but its
`pre-commit` framework integration is stable and documented, and the framework itself
correctly scopes the scan to staged changes without needing to track gitleaks' CLI
directly. This also means Option A's raw `.git/hooks/pre-commit` symlink and this option
**cannot both own the hook file** — pick one approach per project, or fold your own
`pre-commit-audit.sh` into the same `.pre-commit-config.yaml` as a `local` hook so both
run together:

```yaml
# .pre-commit-config.yaml — project root
repos:
  - repo: https://github.com/gitleaks/gitleaks
    rev: v8.24.2
    hooks:
      - id: gitleaks

  - repo: local
    hooks:
      - id: kmp-architecture-audit
        name: KMP architecture audit
        entry: <path-to-skills-repo>/hooks/pre-commit-audit.sh
        language: script
        pass_filenames: false
```

```bash
pip install pre-commit
pre-commit install   # wires .git/hooks/pre-commit to run everything in the config above
```

Test it:
```bash
echo 'API_KEY = "sk-test-1234567890"' >> gradle.properties
git add gradle.properties
git commit -m "test secrets scan"   # should be blocked by gitleaks
git reset HEAD gradle.properties && git checkout gradle.properties
```

To remove: `pre-commit uninstall`.

---

## Option G — Block computer-use for Compose UI verification (Claude Code PreToolUse hook)

Stops the agent from reaching for `mcp__computer-use__*` (manually launching and clicking
through the app) to "verify this looks right" on a Compose Multiplatform screen — the real
verification path is Roborazzi + `runComposeUiTest` (see
[kmp-roborazzi](../skills/kmp-roborazzi/SKILL.md)):
deterministic, runs on plain JVM, produces a committable golden image. Computer-use
screenshots are none of those things, and they require a running device/emulator the agent
often doesn't have visual access to in the first place.

Without this hook, avoiding computer-use for Compose UI is advisory only — it depends on the
agent recalling that guidance for this specific session. This hook makes it structural: the
tool call is refused before it runs, with the correct alternative in the error message.

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "mcp__computer-use__.*",
        "hooks": [
          {
            "type": "command",
            "command": "<path-to-skills-repo>/hooks/block-computer-use-for-compose.sh \"$PWD\""
          }
        ]
      }
    ]
  }
}
```

**Effect:** in a Compose Multiplatform project (detected by `org.jetbrains.compose` /
`compose-multiplatform` in any `build.gradle.kts` or `libs.versions.toml`), any
`mcp__computer-use__*` call is blocked and the agent is told to use Roborazzi instead.
In a non-Compose project (or no Gradle project at all), the hook allows the call through —
computer-use is still the right tool for native desktop apps, cross-app workflows, or a
project with no test harness at all.

Test it:
```bash
# From a Compose Multiplatform project root — should print "Blocked: ..." and exit 2
bash <path-to-skills-repo>/hooks/block-computer-use-for-compose.sh .
echo $?
```

To remove: delete the `PreToolUse` entry from `settings.json`.

---

## Option H — Block edits to vendored skill mirrors (Claude Code PreToolUse hook)

Stops an agent from directly editing a *deployed* skill copy — `.claude/skills/`,
`.agents/skills/`, `.codex/skills/`, or `.gemini/skills/` — instead of the real source.
Those directories are sync targets, not sources: a deployed `kmp-*` skill is a mirror of
this repo's own `skills/<name>/SKILL.md`, and a deployed project-owned custom skill is a
mirror of the consumer project's own root `skills/<name>/SKILL.md`. An edit made directly
in the deployed copy is silently overwritten by the next sync, or — worse — diverges
unnoticed until `audit_project.py`'s agent-setup drift check catches it after the fact.

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "<path-to-skills-repo>/hooks/block-edit-vendored-skills.sh \"$CLAUDE_TOOL_INPUT_FILE_PATH\""
          }
        ]
      }
    ]
  }
}
```

**Effect:** any `Edit`/`Write` targeting a path under `.claude/skills/`, `.agents/skills/`,
`.codex/skills/`, or `.gemini/skills/` is blocked, with a message pointing at the real
source — upstream `kmp-agent-skills` for a bundled skill, or the project's own root
`skills/<name>/SKILL.md` for a project-owned custom skill. Every other edit is unaffected.

If you already have a `PreToolUse` hook on `Edit|Write` (e.g. Option B), add this as an
additional entry in the same `hooks` array rather than a second `PreToolUse` key — Claude
Code runs every matching hook, not just the first.

Test it:
```bash
# Should print "Blocked: ..." and exit 2
bash <path-to-skills-repo>/hooks/block-edit-vendored-skills.sh .claude/skills/kmp-mvi/SKILL.md
echo $?

# Should exit 0 — this is the real source, not a mirror
bash <path-to-skills-repo>/hooks/block-edit-vendored-skills.sh skills/kmp-mvi/SKILL.md
echo $?
```

To remove: delete the `PreToolUse` entry from `settings.json`.

---

## Recommended setup for most projects

```
Option A  (pre-commit) — always set up
Option B  (PostToolUse) — set up if you use Claude Code regularly for KMP work
Option C  (CI freshness) — set up once the skills collection stabilises (v2.0+)
Option D  (SessionStart update check) — kmp-agent-skills clone/fork only, already wired here
Option E  (SessionStart installed-version check) — any consumer project with a deployed skills/ copy
Option F  (secrets scan) — always set up; real security risk, zero cost once wired
Option G  (block computer-use for Compose) — set up for any Compose Multiplatform project
Option H  (block edits to vendored skills) — always set up; prevents silent drift
```

---

## Verify everything is wired

```bash
# Test pre-commit + commit-msg hooks
git stash && git commit --allow-empty -m "chore: hook test" && git stash pop

# Test Claude Code hook — make any edit via the agent and check the tool output
# for "OK: no lightweight architecture smells matched the current scan"

# Test freshness script manually
bash hooks/check-skill-freshness.sh

# Test the SessionStart update check manually
bash hooks/session-start-check-updates.sh
```
