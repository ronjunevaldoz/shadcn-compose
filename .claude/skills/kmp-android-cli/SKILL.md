---
name: kmp-android-cli
description: >
  KMP-specific integration guide for Google's official `android` CLI. The CLI's own
  command reference already exists as a real, officially maintained skill at
  github.com/android/skills/tree/main/devtools/android-cli (installable via
  `android skills add android-cli`) — this skill does not re-document that surface.
  It covers what's specific to a Kotlin Multiplatform project: locating the Android
  target inside a multi-module 6-layer graph, why `android create` doesn't apply once
  `kmp-feature-scaffold` has run, and CI wiring alongside the existing
  Android/iOS/Desktop/Web matrix.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-19'
  keywords:
    - Android CLI
    - android-cli
    - android init
    - android skills add
    - emulator management
    - AVD
    - android run
    - android sdk
    - Android Studio agent tools
---

## When to Use This Skill

Use when you need to:
- Point the official `android` CLI at a KMP project's Android target (not a fresh
  Android-only project — that's `android create`'s use case, not this one)
- Decide whether a build/deploy/emulator/SDK task belongs to the official `android-cli`
  skill's command surface or to a KMP-specific skill (`feature-scaffold`,
  `ci-github-actions`)
- Wire the CLI's stable commands into this collection's existing CI matrix

**Requires:** `kmp-feature-scaffold` project structure (or any Gradle
project with an Android target module) and the `android` CLI installed.

**Trigger keywords:** android cli, android-cli, android init, android skills add,
create android virtual device, AVD from terminal, start emulator cli, android run apk,
install apk cli, android sdk install, build and run android app, deploy to emulator,
run on device from terminal, launch android app terminal, run KMP android target,
test android target locally.

**Freshness rule:** the command reference lives upstream, not here — always defer to
`github.com/android/skills/tree/main/devtools/android-cli`'s current `SKILL.md` for exact
flags. Re-verify before relying on any command below; this repo doesn't control that
release cadence.

---

## Recommendation First

**Install the real, official skill instead of re-learning the CLI from scratch:**

```bash
android skills find android-cli   # confirm it's available
android skills add android-cli    # installs the real command reference as a skill
```

That skill (Google LLC, `github.com/android/skills/tree/main/devtools/android-cli`) is
the canonical source for every `android` subcommand — `create`, `describe`, `emulator`,
`run`, `sdk`, `docs`, `screen`, `layout`, `studio`, `skills`, `info`, `init`, `update`.
This skill does not duplicate that reference — a second, hand-maintained copy of a
CLI's flags is exactly the kind of thing that silently drifts stale (verified against
the real upstream `SKILL.md` while writing this: several details an earlier draft of
this skill guessed — `create`'s flag names, whether `skills add` takes an `--all` flag —
were wrong). Read the upstream skill for command syntax; read this one only for how it
applies to a KMP project's module graph.

---

## Installation

```bash
# Linux
curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install.sh | bash
# macOS Apple Silicon
curl -fsSL https://dl.google.com/android/cli/latest/darwin_arm64/install.sh | bash
# macOS Intel
curl -fsSL https://dl.google.com/android/cli/latest/darwin_x86_64/install.sh | bash
# Windows
curl -fsSL https://dl.google.com/android/cli/latest/windows_x86_64/install.cmd -o "%TEMP%\i.cmd" && "%TEMP%\i.cmd"

# Verify
android info
android update
```

---

## Where this differs from the official skill: KMP project layout

The official skill's `create`/`describe`/`run` examples assume a single-module Android
project. A KMP project scaffolded by `kmp-feature-scaffold` is not that
shape — apply the same commands with these adjustments:

**Don't use `android create` on an existing KMP project.** It creates a fresh,
Android-only project from a template (`empty-activity`, etc.) — it has no concept of the
6-layer `:model`/`:api`/`:domain`/`:data`/`:presenter`/`:ui` module graph
`kmp-feature-scaffold` builds. Use that skill to add the Android target
instead.

**`android describe --project_dir=<KMP root>`** still works — it walks the Gradle build
and reports every target's build/output paths, including the Android target buried
inside a multi-module graph. Use its output to find the APK path for `android run`
instead of guessing `androidApp/build/outputs/apk/debug/...` by hand — that path shape
isn't guaranteed stable across AGP versions.

**`android run --apks=<path from describe>`** deploys and launches — this replaces the
`./gradlew installDebug && adb shell am start -n ...` two-step agents otherwise chain
manually.

**Emulator and SDK management** (`android emulator create/start/stop`, `android sdk
install`) are project-shape-agnostic — no KMP-specific adjustment needed, use them as the
upstream skill documents.

**`android studio *` (IDE-integration subcommands)** need a running, compatible Android
Studio — `android studio check` first. If it fails (headless agent session, CI runner,
no qualifying Studio version), fall back to `kmp-roborazzi`'s
`runComposeUiTest`/screenshot tests instead of `render-compose-preview`, and grep/read
source instead of `find-declaration`/`find-usages`.

---

## Wiring into CI

```yaml
# .github/workflows/android-cli-smoke.yml — stable commands only, no Studio dependency
name: Android CLI Smoke
on: [pull_request]
jobs:
  smoke:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Install Android CLI
        run: curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install.sh | bash
      - name: Describe project
        run: android describe --project_dir=.
      - name: Create + start emulator, run instrumented smoke test
        run: |
          android emulator create --name smoke --package "system-images/android-34/google_apis/x86_64"
          android emulator start smoke
          android sdk install "platforms;android-34" "build-tools;34.0.0"
          ./gradlew connectedDebugAndroidTest
```

Don't add `android studio *` steps to CI — they need a running Android Studio instance
CI runners don't have. Re-check flag names against the upstream skill before relying on
this job; the exact `emulator create`/`sdk install` argument shapes above are illustrative,
not verified against a specific CLI version.

---

## Related Skills

- `kmp-feature-scaffold` — the project structure `android describe`/
  `android run` operate on; use that skill to add the Android target itself, never
  `android create`
- `kmp-ci-github-actions` — where the CI smoke job above belongs
  alongside the existing Android/iOS/Desktop/Web test matrix
- `kmp-roborazzi` — the fallback for `android studio
  render-compose-preview` when a qualifying Studio install isn't present
- `kmp-audit` — run after any Android CLI-driven scaffold change to
  confirm the 6-layer module contract still holds

---

## Common Anti-Patterns

- re-documenting the `android` CLI's command reference instead of pointing at
  `github.com/android/skills/tree/main/devtools/android-cli` — a hand-copied reference
  drifts stale the moment the upstream skill updates; this exact mistake shipped in an
  earlier draft of this skill (wrong `create` flags, an invented `skills add --all` flag)
- using `android create` to add Android to an already-scaffolded KMP project — it
  targets a fresh Android-only project, not an existing multi-target module graph; use
  `kmp-feature-scaffold` instead
- guessing the APK output path by hand instead of reading it from `android describe`'s
  output — the path shape isn't guaranteed stable across AGP versions
- running `android studio *` commands in CI or a headless agent session without first
  checking `android studio check`
- scripting raw `adb`/`sdkmanager`/`emulator` calls once `android run`/`android sdk`/
  `android emulator` already wrap the same operations through one consistent interface

---

## Output Style

When asked about Android CLI usage for a KMP project, respond in this order:
1. Whether `android` is installed (`android info`) and whether the official
   `android-cli` skill is installed (`android skills find android-cli`)
2. Whether the task is KMP-specific (module layout, APK path inside a multi-module
   graph) — if so, answer from this skill; otherwise defer to the upstream skill's
   command reference
3. Whether the task actually needs `android studio *` — if so, gate on `android studio
   check` first and name the fallback
4. CI wiring, only if asked

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-19 | Corrected against the real upstream source: found `github.com/android/skills/tree/main/devtools/android-cli` — a genuine, Google-maintained skill in the same frontmatter format — after having claimed "no official skill exists" (the original research only checked developer.android.com, never searched GitHub for an actual skills repo). Rewrote this skill from a full command-reference duplicate (which was already wrong in places — invented `create --template-name=`/`skills add --all` flags that don't exist in the real CLI) into a thin KMP-integration layer that defers to the upstream skill for command syntax and only documents what's genuinely KMP-specific (module-graph-aware `describe`/`run` usage, why `create` doesn't apply post-scaffold). |
| 2026-07-19 | Broadened Trigger keywords with generic task phrasing ("build and run android app", "deploy to emulator", "run KMP android target") — the initial keyword set required knowing the tool's name ("android cli") up front, so a natural "how do I run this on an emulator" ask wouldn't have surfaced it. Also cross-referenced from `kmp-feature-scaffold`'s Related Skills so it's discoverable from project-foundation work, not only from its own literal keywords. |
| 2026-07-19 | Initial skill. Real gap found: the user asked for Android CLI (`developer.android.com/tools/agents`) as a mandatory skill; verified no official Claude Code skill exists for it and this repo had zero references. Covers the stable command surface (`init`/`skills add`, `create`/`describe`, `emulator`, `run`, `sdk`) as the default, and scopes `android studio *` IDE-integration commands as Preview (verified: requires Android Studio Quail 2 Canary 1+, known Windows emulator/PowerShell issues) rather than a default dependency. |
