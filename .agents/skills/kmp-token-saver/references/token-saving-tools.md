# Token Saving Tools

This reference stays small on purpose. It only records when to use each tool and what
kind of setup it needs.

## Ponytail

- Use for overengineering checks, YAGNI pressure, and "smallest correct solution" reviews.
- Best when the task is code, architecture, or refactor guidance.
- **Install (verified real, low risk):** goes through Claude Code's own plugin
  marketplace — `/plugin marketplace add DietrichGebert/ponytail`, then
  `/plugin install ponytail@ponytail`. Both are slash commands the user runs
  interactively; an agent cannot invoke them on the user's behalf.
- **Real, non-interactive CLI equivalent exists** (`claude --help` lists `plugin` as a
  real subcommand — verified, not a workaround): `claude plugin marketplace add
  DietrichGebert/ponytail` then `claude plugin install ponytail@ponytail`. Default scope
  is `user` (available in every project); pass `--scope project` only if that's actually
  wanted. A real installed-plugins check on this machine found `ponytail@ponytail`
  present but scoped to a single project and disabled — installing at user scope with
  no `--scope` flag is the fix for "available in any project," not a separate step per
  project.
- `scripts/install-ponytail.sh` in this skill wraps both steps, idempotently — skips the
  marketplace step if already registered, skips the install if a user-scope enabled copy
  already exists. Still a persistent, global config change — the user runs it, not an
  agent unprompted. Run once the user gave specific confirmation naming this exact
  script: verified `ponytail@ponytail` now shows `scope: user`, `enabled: true`
  (`claude plugin list --json`) in addition to the original project-scoped, disabled
  entry — both installs coexist; the user-scope one is what makes it available in
  every project.
- No extra host setup required once installed.

Source: [DietrichGebert/ponytail](https://github.com/DietrichGebert/ponytail)

## Caveman

- Use when the agent is too verbose and should answer in fewer words.
- Best for response shaping, plan summaries, and tight implementation notes.
- **Install (verified, elevated risk):** `curl -fsSL .../install.sh | bash` (macOS/Linux)
  or the PowerShell equivalent — executes a remote script directly from a personal
  GitHub account, no package-manager verification. Recommend Caveman when it fits the
  task; do not run this installer yourself — tell the user to review and run it in
  their own terminal instead.
- **Once installed, it registers as a real Claude Code plugin** (`caveman@caveman`,
  verify with `cat ~/.claude/plugins/installed_plugins.json | grep caveman`) with its
  own slash commands — not a standalone CLI (`which caveman` finds nothing; that's
  expected, not a broken install).
- **Not active by default after install.** Two activation modes, both user-run (an
  agent cannot invoke slash commands on the user's behalf):
  - `/caveman [lite|full|ultra|wenyan]` — session-level, terse responses at the chosen
    intensity, until the session ends.
  - `/caveman-init [--dry-run|--force] [--only <agent>]` — writes a persistent, always-on
    activation rule into the current repo for every IDE agent (Cursor, Windsurf, Cline,
    Copilot, `AGENTS.md`). This command itself runs another `curl | node -` step
    internally — expected, since it's the already-installed, reviewed plugin's own
    documented mechanism invoked by the user, not an agent fetching an unreviewed script.

Source: [JuliusBrussee/caveman](https://github.com/JuliusBrussee/caveman)

## RTK

- Use when shell output is noisy and should be compressed before it reaches the model.
- Best for test logs, git output, package-manager output, and other verbose commands.
- Setup is **two phases with different authorization needs — do not treat them as one step.**

Source: [rtk-ai/rtk](https://github.com/rtk-ai/rtk)

**Phase 1 — binary install: safe to run directly.**
```bash
brew install rtk        # verified working; local package install, no config changes
rtk --version && rtk gain   # sanity check
```

`scripts/install-rtk.sh` in this skill wraps Phase 1 and then runs
`rtk init -g --dry-run` to print the Phase 2 preview below — it never applies Phase 2,
matching the authorization split documented next.

**Phase 2 — global hook wiring: requires specific, not generic, confirmation.**
`rtk init -g` patches `~/.claude/settings.json` (installs a PreToolUse hook rewriting
every Bash command) and `~/.claude/CLAUDE.md`. This is a global change that persists
across every future session, not just the current task. A generic "go ahead" is **not**
sufficient authorization for it — running `rtk init -g --auto-patch` on a general
approval gets blocked by the auto-mode classifier as unauthorized persistence (verified:
this happened in practice, not a hypothetical). Two correct paths instead:
1. Run `rtk init -g --dry-run` first, show the user the exact preview output, and get
   their confirmation of *that specific diff* — not a repeat of the earlier general
   go-ahead.
2. Tell the user to run `rtk init -g` themselves, interactively, in their own terminal —
   it prompts before touching `settings.json`, so no agent action is needed at all.

**The hook does not apply retroactively.** It only affects Bash commands in sessions
started *after* `rtk init -g` completes — verified by testing `git log` immediately
after install in the same session: output was unfiltered, exactly as expected.

**Tracking savings** (once the hook is live in a fresh session):
- `rtk gain` — summary; empty (`"No tracking data yet"`) until the hook has actually
  processed commands. `-H`/`--history` for per-command log, `-g`/`--graph` for a daily
  trend, `-a`/`--all` for full daily+weekly+monthly, `-q`/`--quota` for a $ estimate,
  `-f json`/`-f csv` for export.
- `rtk discover -p <project>` — retroactive estimate from existing Claude Code session
  history, works without the hook being live. May report 0 sessions depending on where
  the host stores transcripts — not a sign anything is broken.

## Headroom

- Use when tool output, logs, files, or RAG chunks need compression before LLM context.
- Best for heavy tool sessions where the host already supports Headroom.
- **Install (verified, heaviest setup of the four):** `pip install "headroom-ai[all]"`.
  Not a quick toggle — it runs as a local proxy (`headroom proxy --port 8787`) sitting
  between the agent and the LLM provider, requires the user's own provider API keys,
  and downloads a model from HuggingFace on first run. Never enter API keys into it on
  the user's behalf.
- `scripts/install-headroom.sh` in this skill wraps the pip install step only —
  idempotent (`pip show headroom-ai` first, skips if already present) — and
  deliberately stops there. It does not collect API keys or start the proxy; both are
  printed as next steps for the user to do themselves. The package install itself
  (`pip install`) is a normal package-manager action safe to run directly, unlike the
  credential/proxy-start steps.
- Check before assuming it's absent: `pip3 show headroom-ai` (or `pip show`) — found
  genuinely already installed on one machine during this skill's own verification,
  contradicting an earlier assumption that none of the three optional tools were
  present.
- Keep this optional until the setup exists; do not block a task on it.
- **The step this doc originally missed: routing Claude Code's own traffic through the
  proxy.** Installing the package and starting `headroom proxy --port 8787` isn't
  enough by itself — Claude Code needs an `env` block in `~/.claude/settings.json`
  pointing `ANTHROPIC_BASE_URL` at the proxy (verified against a real, working setup):
  ```json
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:8787",
    "ANTHROPIC_AUTH_TOKEN": "...",
    "ANTHROPIC_API_KEY": ""
  }
  ```
  This is the same class of change as RTK's `rtk init -g` hook wiring — a global,
  persistent `settings.json` patch. It needs the user's own edit and specific
  confirmation of the diff, not something `install-headroom.sh` does silently.
- The backend behind the proxy doesn't have to be a paid cloud provider — a real setup
  observed `ANTHROPIC_AUTH_TOKEN` set to a placeholder value routing to a local Ollama
  model, not a real provider API key. Don't assume Headroom always needs a paid key.

Source: [headroomlabs-ai/headroom](https://github.com/headroomlabs-ai/headroom)
