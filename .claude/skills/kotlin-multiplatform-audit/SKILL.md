---
name: kotlin-multiplatform-audit
description: >
  KMP project audit skill for reviewing an existing Kotlin Multiplatform codebase.
  Use this skill to inspect architecture, module boundaries, state handling, repository
  and network layering, Compose patterns, expect/actual usage, shared resources,
  design system usage, test coverage, platform readiness, and the skills repo itself.
  Produces findings, risk levels, and a fix sequence instead of implementation code.
  Pair with kotlin-multiplatform-expert to route any follow-up work to the right
  domain skills.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-25'
  keywords:
    - KMP audit
    - project audit
    - architecture review
    - boundary review
    - architecture drift
    - clean architecture audit
    - module audit
    - state audit
    - repository audit
    - Compose audit
    - expect actual audit
    - KMP review
    - project health check
    - readiness review
    - freshness audit
    - deprecation audit
    - script audit
    - skills repo audit
    - issue draft
    - question draft
    - kmm-agent-skills
    - kmm-skills
    - KMM agent skills
    - skill collection
    - skills index
---

## When to Use This Skill

Use this skill when you need to:
- Review an existing KMP repo for architecture drift or missing boundaries
- Check whether a feature or module is in the right place
- Validate MVI, repository, Compose, and `expect/actual` choices
- Produce a fix order before making code changes
- Compare the project against this collection's recommended KMP patterns
- Audit the skills repo for missing references, examples, scripts, rules, and freshness
- Turn confirmed findings into GitHub issue drafts or question drafts when the user
  wants work items instead of just findings

**Trigger keywords:** audit repo, review architecture, project health, boundary check,
module review, KMP audit, clean architecture review, readiness review, architecture drift,
what is wrong with this project, inspect this repo, audit skills repo, script hygiene,
freshness check, deprecation risk, references audit, governance, CI enforcement,
governance check, enforce skills, compliance, fail on violation, .kmm-skills.

**Freshness rule:** the audit checklist references Compose, MVI, network, and database patterns —
recheck the `kotlin-multiplatform-expert` skill map and this collection's PLAN.md before auditing
against a new version baseline.

---

## Recommendation First

Default to **running the bundled scripts first, then reviewing findings manually against the
checklist in this skill**.

Why:
- `audit_project.py` catches mechanical smells (effect replay, state-copy races, UI/data leaks)
  faster than manual review
- scripts produce evidence-backed findings that are easier to convert to issue drafts
- the manual checklist catches architectural problems the scripts cannot detect

Do not skip the scripts and go straight to manual review — you will miss mechanical issues
that automation finds reliably.

---

## Audit Flow

1. Read the project docs first: `AGENTS.md`, `README.md`, architecture notes, and any
   module-specific guidance.
2. Inspect the module graph and dependency direction.
3. Check data flow boundaries: UI, domain, data, network, database, platform code.
4. Check Compose patterns: MVI, state hoisting, slots, state containers, design system.
5. Check multiplatform choices: `expect/actual`, shared resources, platform targets.
6. Report findings with severity, evidence, and the recommended fix order.

This skill does **not** implement fixes by default. It is the review surface that tells
the user and the other skills what to do next.

---

## What to Inspect

### 1) Module boundaries
- UI must not import `:data`
- Domain must not know about DTOs or SQLDelight entities
- Repository interfaces should live in `:api`, implementations in `:data`
- Shared UI primitives should live in the design system, not feature modules

### 2) State and MVI
- Screen state should be immutable
- One-shot effects should not be replayed
- Prefer `Screen` / `Content` split for testability
- Check for the wrong state container in ephemeral UI state

### 3) Data layer
- DTOs and entities stay inside `:data`
- NetworkResult should not leak into UI state
- Repositories should own mapping and fetch strategy
- Offline support should be explicit, not accidental

### 4) Multiplatform code
- Prefer shared code in `commonMain`
- Prefer a pure `commonMain` implementation before abstractions; only split to an
  interface or `expect/actual` when shared code cannot express the behavior cleanly
- Use `expect/actual` only when platform behavior is genuinely different
- Flag JVM-only utilities in `commonMain` such as `String.format`, `DecimalFormat`, or
  `SimpleDateFormat`; keep the shared API in common code and move the implementation to
  the platform that owns it
- Check platform target coverage against the product goal

### 5) Design system
- Verify tokens, palette rules, and typography are consistent
- Check whether components use the right pattern for the repo's chosen UI system
- Flag hardcoded colors, sizes, and text styles
- Flag hardcoded user-facing strings in Compose; route to `kotlin-multiplatform-shared-resources`
  and require `values/strings.xml` / `stringResource()` instead
- Require a preview stub for each `*Content.kt` in a feature `ui/` module so the
  preview workflow stays part of the scaffold, not a manual afterthought
- **Layout pattern consistency** — every `*Content.kt` in the same feature `ui/` dir must use the same top-level layout pattern (flat `Column`/`LazyColumn`, card-sectioned `AppCard`, or tabbed `TabRow`+`HorizontalPager`); mixed patterns are a `layout_inconsistency` violation. Run `scan_design_violations.py <project_root>` — it detects this cross-screen.

### 6) Native / JNI boundary (only if `*-jni.cpp`, `*-wrapper.cpp`, or `CMakeLists.txt` exist)
- 3rd-party C++ (`vendor/`, `third_party/`, submodules, `FetchContent`) is **read-only** —
  flag ANY edit to a vendored `.cpp`/`.h`. Hand off to `kotlin-multiplatform-jni-pro`.
- Every opaque native handle stored as a Kotlin `Long` has a matching `dispose()`/`close()`
  → JNI `_free`. Flag any `_create` with no `_free` (memory leak).
- Every `GetStringUTFChars`/`Get*ArrayElements` has a release on all exit paths.
- JNI bridge contains type-conversion only — flag native logic or reimplemented library
  algorithms (route to `kotlin-multiplatform-jni-pro` Phase 0 discovery).
- Complex headers (templates, `std::function`, overloads, exceptions) are wrapped via a
  flat `extern "C"` C-shim, not mapped directly. Full gate: `kotlin-multiplatform-jni-pro`.

### 8) Agent & consumer setup
- **`CLAUDE.md` missing** → HIGH — no `--system-prompt-file` configured; skills context never loads
- **`.claude/AGENTS.md` missing** → HIGH — agent has no skill routing, feature table, or module map; run `/kmm-setup-agents`
- **`.claude/commands/` missing or empty** → MEDIUM — consumer commands (`/kmm-run-audit`, `/kmm-implement-feature`, `/kmm-verify`) not installed
- **`.claude/skills/` missing or empty** → MEDIUM — skills not deployed; trigger keywords won't activate skill content
- **`AGENTS.md` covers only one surface of a multi-surface project** → MEDIUM — e.g., engine-only AGENTS.md in a project that also has Studio/UI modules; the active development surface has no routing
- **`MviViewModel` base class defined in a feature module** → MEDIUM — should live in `:shared:core` or `:core:mvi` so future features can extend it without cross-feature imports
- **Theme composable wraps `MaterialTheme`** → MEDIUM — blocks custom token ownership and `StyleScope` integration; use `CompositionLocalProvider` with `AppTheme` instead
- **`darkTheme = false` hardcoded in theme composable** → MEDIUM — system dark mode never applied; replace with `isSystemInDarkTheme()` default
- **Multiple parallel token files** (`*Tokens.kt`, `*ColorTokens.kt`) with different types (e.g., `ULong` constants vs `Color` values) → LOW — two token systems with no shared access pattern; consolidate under a single `AppColors` data class

### 7) Skills repo hygiene
- Ensure every skill has `name`, `description`, and `metadata.last-updated`
- Ensure trigger guidance is explicit enough to fire in practice
- Prefer references for fast-moving topics and keep examples only when they clarify
- Check that scripts are executable, deterministic, and covered by tests when practical
- Flag skills that depend on fast-moving libraries without a freshness note or docs link
- Flag scripts that encode assumptions about deprecated or unstable APIs
- Ensure new-project scaffold guidance names the `Kotlin/kmp-wizard` `all-targets`
  branch when the goal is Android, iOS, Web, Desktop, and Server
- Ensure KMM projects route plugin and dependency versions through `build-logic/`
  convention plugins and `gradle/libs.versions.toml` instead of scattering versions
  across module build files

---

## Output Format

When auditing, return:
- `Findings` first, ordered by severity
- `Evidence` for each finding, with file paths when available
- `Recommended fix order`
- `Skills to use next`
- `Optional issue drafts` when the user wants findings turned into GitHub-ready work items

Keep implementation advice short and actionable. If a finding maps cleanly to an existing skill,
name that skill so the follow-up path is obvious.

## From Finding to Issue

If the user wants repo work items, convert each confirmed finding into one of two things:
- a **GitHub issue draft** when the problem is actionable and should be tracked
- a **question draft** when the finding needs product or architecture confirmation first

Ask before creating any issue draft. Do not auto-file issues from an audit without
explicit confirmation from the user.

Every draft should include:
- a title following the format `[category] short problem description` — see categories below
- the evidence that triggered it (file path, line, or script output)
- the recommended fix or follow-up skill
- an attribution footer such as `Suggested by kotlin-multiplatform-audit`

### Issue Title Format

Use `[category] short problem description`. Keep titles under 72 characters.
The description names the symptom, not the fix.

| Category | Use for |
|---|---|
| `[arch]` | Layer boundary violations, wrong module placement |
| `[mvi]` | Effect replay, state copy race, wrong state container |
| `[presenter]` | ViewModel has Compose import, wrong scope, missing test |
| `[data]` | Pass-through repository, DTO escaping layer, no cache |
| `[ui]` | Stateless composable violates, missing Preview stub, design drift |
| `[di]` | Koin module scope wrong, missing factory/viewModel registration |
| `[build]` | Convention plugin misconfiguration, version drift |
| `[test]` | Missing test coverage, mock instead of fake, wrong scope |

**Examples:**
```
[arch] DTO from :data escapes to :feature:todo:ui
[mvi] Effect replayed on recomposition in TodoListScreen
[presenter] ViewModel imports Compose in :feature:todo:presenter
[data] Repository is pass-through — no local cache
[ui] AddTodoContent missing Preview stub for error state
[di] TodoListViewModel registered as factory instead of viewModel
```

## Common Anti-Patterns

- reporting findings before reading `AGENTS.md` and `README.md` — misses project-specific constraints
- producing implementation code during an audit instead of findings + fix order — audit and implement are separate steps
- auto-filing issues without user confirmation — always ask before creating GitHub issue drafts
- mapping every finding to the same skill — route each finding to the most specific applicable skill
- flagging style preferences as architecture violations — only flag boundary or correctness problems

An audit should produce findings that are actionable. If a finding doesn't map to a specific skill or fix, reclassify it as a question draft.

---

## Governance & CI Enforcement

Run the governance check in a consumer project's CI so violations block the build automatically — no manual audit required.

### Step 1 — Add a `.kmm-skills` version file to the consumer project root

```json
{
  "skills_repo": "ronjunevaldoz/kmm-agent-skills",
  "version": "1.24.1"
}
```

Commit this file. It declares which skills collection version the project targets and
must pin a release tag, not a mutable ref like `main`. The governance check prints it
on every run and fails if the file is missing or the version is not tag-pinned.

### Step 2 — Wire the reusable workflow

Create `.github/workflows/governance.yml` in the consumer project:

```yaml
name: KMM Governance

on:
  pull_request:
  push:
    branches: [main]

jobs:
  kmm-governance:
    uses: ronjunevaldoz/kmm-agent-skills/.github/workflows/kmm-audit.yml@main
    with:
      project_root: .
      fail_on: HIGH
      skills_ref: v1.24.1   # pin to a tag for reproducibility
```

That is the complete consumer setup — no scripts to copy, no dependencies to install beyond Python 3.12 (provided by the workflow).

### What the governance check runs

| Scanner | Detects | Severity |
|---|---|---|
| `scan_design_violations.py` | Hardcoded colors, dp literals, Material theme usage, TextStyle construction, nested containers, layout inconsistency | HIGH (error), MEDIUM (warning) |
| `audit_project.py` | State copy races, SharedFlow replay effects, NetworkResult in UI state, DTO import in UI layer, magic color literals, hardcoded spacing, missing preview stubs | HIGH |
| `validate_module_graph.py` | Missing feature module files, missing `androidApp` UI link, missing `*ContentPreview.kt` stub beside feature UI content | HIGH |

Findings at or above `fail_on` exit non-zero and fail the CI job. Findings below the threshold are reported but do not fail.

### Threshold guide

| `fail_on` value | When to use |
|---|---|
| `HIGH` | Default. Fails only on correctness violations and architecture boundary breaks. |
| `MEDIUM` | Stricter. Also fails on design-token warnings and layout inconsistencies. Recommended once the project is stable. |
| `LOW` | Full enforcement. Fails on any finding. Use for highly regulated or greenfield projects. |

### Running locally before pushing

```bash
# From inside the skills repo (development)
python3 skills/kotlin-multiplatform-audit/scripts/governance_check.py /path/to/consumer/project

# From a consumer project with the skills repo checked out alongside it
python3 ../kmm-agent-skills/skills/kotlin-multiplatform-audit/scripts/governance_check.py .
```

---

## Bundled Script

- `scripts/governance_check.py` — CI enforcement orchestrator. Runs both scanners, reads
  `.kmm-skills` for version pinning, fails on missing or mutable pins, and exits non-zero
  on findings at or above the threshold.
  Used by the reusable workflow at `.github/workflows/kmm-audit.yml`.
- `scripts/audit_project.py` — runs a lightweight scan for common KMP architecture
  smells such as effect replay bugs, state copy races, and obvious UI/data boundary leaks.
  Supports three modes:
  - default — prints `FINDINGS:` list, exits 1 if any found
  - `--roadmap` — prints a prioritized adoption plan
  - `--harvest` — prints JSON `{ findings, lessons }` where `lessons` are positive patterns
    the consumer does right that could be upstreamed to skills (run `/kmm-harvest-lessons`)
- `scripts/validate_module_graph.py` — checks an existing project’s feature module layout and
  requires a preview stub for each `*Content.kt` in `:feature:*:ui`.
- `scripts/audit_skills_repo.py` — checks the skills repo for metadata, freshness, scripts,
  and documentation gaps.
- `scripts/draft_issue.py` — renders a GitHub-ready issue or question draft with an
  attribution footer.

---

## Related Skills

- `docs/reference/compatibility-matrix.md` — version compatibility table and conflict zones; check before bumping any library
- `kotlin-multiplatform-expert` — use before running the audit; the expert skill identifies which domain skills apply and what build order to follow
- `kotlin-multiplatform-clean-architecture` — defines the 6-layer boundary rules the audit script enforces
- `kotlin-multiplatform-mvi` — most `state copy race` and `sharedflow replay effect` findings require this skill to fix correctly
- `kotlin-multiplatform-roborazzi` — replacement for `manual screen capture` findings
- `kotlin-multiplatform-design-system` — replacement for `magic color literal` and `hardcoded spacing` findings
- `kotlin-multiplatform-jni-pro` — owns every native/JNI finding (3rd-party C++ immutability, opaque-handle cleanup, C-shim wrapping); hand off section 6 findings here

---

## Output Style

When asked to audit a project or the skills repo, respond in this order:
1. run the bundled scripts and report any automated findings
2. work through the manual checklist sections (module boundaries, state, data layer, etc.)
3. findings ordered by severity (critical → high → medium → low)
4. evidence for each finding (file paths, grep output, or line references)
5. recommended fix order
6. skills to use next

Ask before converting findings to issue drafts. Keep implementation advice minimal — this skill routes work, it doesn't implement it.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Added section 8 (Agent & Consumer Setup) to audit checklist. Added three new detectors to `audit_project.py`: `_detect_agent_setup` (missing AGENTS.md, commands, skills, CLAUDE.md, single-surface AGENTS.md in multi-surface project), `_detect_mvi_placement` (MviViewModel in feature module instead of shared/core), `_detect_design_system_wiring` (MaterialTheme wrapping, hardcoded darkTheme=false, parallel ULong token files). |
| 2026-06-24 | Added a skills-version pin guard to governance: `.kmm-skills` must exist and must point at a release tag, not `main` or another mutable ref. |
| 2026-06-23 | Added "Governance & CI Enforcement" section: governance_check.py, reusable workflow, .kmm-skills version file, threshold guide. |
| 2026-06-22 | Added "Native / JNI boundary" inspection section (#6): 3rd-party C++ immutability, opaque-handle cleanup, acquire/release pairing, C-shim wrapping — closes the cross-skill enforcement gap for the immutability rule. Hands off to kotlin-multiplatform-jni-pro. |
| 2026-06-21 | GitHub issue title format defined: `[category] short description`. Category table added with 8 categories (`[arch]`, `[mvi]`, `[presenter]`, `[data]`, `[ui]`, `[di]`, `[build]`, `[test]`). |
| 2026-06-18 | Initial release — architecture audit checklist, `audit_project.py`, `audit_skills_repo.py`, `draft_issue.py`. |
