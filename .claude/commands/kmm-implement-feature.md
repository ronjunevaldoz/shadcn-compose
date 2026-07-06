# /implement-feature $ARGUMENTS

**KMM Agent Skills** — build a new KMP feature end-to-end, layer by layer, with the full
Koin 4 / Ktor 3 / SQLDelight 2 / CMP 1.11 stack wired correctly from the start.

Feature name: **$ARGUMENTS**

---

## Phase 0 — Skills freshness check

```bash
python3 scripts/check_updates.py
```

| Exit | Action |
|---|---|
| `0` | Skills are current — proceed to Phase 1 |
| `1` | Updates available — display the output, ask the user: **Pull now / Skip / View diff** (see `commands/kmm-check-updates.md`). Do not pull automatically. After the choice is made, proceed to Phase 1. |
| `2` | Remote unreachable — print `⚠️ Running with local skills (offline)` and proceed to Phase 1. |

---

## Phase 1 — Plan

Load `agents/planner.md`.

The layer planner will inspect `build-logic/`, `gradle/libs.versions.toml`, and any
existing modules under `feature/$ARGUMENTS/`. It will identify which of the 45 skills
apply and produce a build-order plan covering all 6 layers:

```
:model → :api → :domain → :data → :presenter → :ui
```

The plan includes every Koin binding, every test class, and every `libs.versions.toml`
addition needed before a line of code is written.

**Show the plan. Wait for confirmation before continuing.**

---

## Phase 2 — Implement

Load `agents/implementer.md`.

Generates complete, runnable Kotlin for each layer in build order. Every file is fully
written — no stubs, no `// TODO`. Includes:

- `build.gradle.kts` for each new module
- All Kotlin source files per layer
- Koin module wiring (platform modules for `:data`, common module for `:presenter`)
- `:presenter` unit tests with `runTest` + Turbine
- `:ui` interaction tests with `createComposeRule` + `onNodeWithTag`
- `:ui` Roborazzi screenshot tests for each meaningful visual state

---

## Phase 3 — Validate

Load `agents/validator.md`.

Runs in order — stops at the first failure:

1. Architecture audit: `python3 skills/kotlin-multiplatform-audit/scripts/audit_project.py .`
2. `commonMain` metadata compilation
3. `jvmTest` — presenter unit tests + UI tests in parallel

On failure → load `agents/fixer.md`, apply targeted fixes, re-validate.
Maximum 2 fix cycles. If still failing, stop and report to user.

---

## Phase 4 — Review

Load `agents/reviewer.md`.

Reviews layer boundaries, Koin wiring, MVI contracts, and testTag coverage on all files
created during implementation. Any blocker → load `agents/fixer.md` for one fix cycle.

---

## Phase 5 — Wrap up

Before committing, check whether any `.py` script was added or modified during implementation:

```bash
git diff --name-only HEAD | grep -E '^(scripts/|skills/.*/scripts/).*\.py$' || true
```

If any scripts changed → ensure `tests/test_skill_scripts.py` is staged in the same commit. The pre-commit hook blocks otherwise.

Update `.claude/pipeline-context.json` with patterns learned during this feature,
then commit it so the next session inherits the context:

```bash
git add .claude/pipeline-context.json
git commit -m "chore(pipeline): update context after $ARGUMENTS"
```

Only commit if the file actually changed. Skip if all values are unchanged.

Report:
```
Feature:        $ARGUMENTS
Layers built:   <list>
Files created:  <N>
Tests written:  <N> unit + <N> UI
Validation:     PASS (ktlint: PASS | NOT CONFIGURED)
Review:         APPROVE
Pipeline ctx:   committed | unchanged
```

---

## Phase 6 — Proactive issue tracking

After the report, scan this session for patterns worth tracking:

1. **Recurring blockers** — any `[BLOCKER_TYPE]` that appeared in 2+ files
2. **LOW-confidence fixes** — anything the fixer marked LOW and the user resolved manually
3. **Skill gaps discovered** — any guidance the implementer had to invent because no skill covered it

For each item found, prompt:
```
Found <N> item(s) worth tracking as GitHub issues:
  · [<TYPE>] seen in <N> files — may indicate a skill gap in <skill-name>

Create GitHub issues for these? /submit-issue is ready to pre-fill each one.
  [y] Yes — open /submit-issue for each item in turn
  [n] No  — end session
```

Skip this phase if the session had zero blockers and no LOW fixes.
