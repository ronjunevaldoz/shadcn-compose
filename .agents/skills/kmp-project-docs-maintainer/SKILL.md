---
name: kmp-project-docs-maintainer
description: >
  Maintains downstream consumer-facing KMP project documentation only: README,
  GETTING_STARTED, INSTALL, RELEASING, docs/reference pages, onboarding guides,
  architecture notes, and architecture diagrams. Use this skill when project docs need to
  match the actual code, commands, config, folder layout, or app/library structure.
  Does NOT cover consumer release-note generation, per-skill changelogs, or
  skills-repo documentation.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-11'
  references:
    - references/docs-hygiene.md
  keywords:
    - project docs
    - consumer docs
    - README
    - getting started
    - install guide
    - releasing guide
    - docs reference
    - onboarding docs
    - architecture docs
    - architecture diagram
    - docs drift
    - documentation maintainer
    - project documentation
    - repo docs
    - docs sync
    - libraries catalog
    - testing coverage doc
    - docs/libraries.md
    - docs/testing.md
    - docs/demos.md
---

## When to Use This Skill

Use this skill when you need to:
- update a project README so it matches the current code, commands, or modules
- refresh onboarding docs such as GETTING_STARTED, INSTALL, or RELEASING
- keep `docs/` and `docs/reference*` content aligned with the project structure
- fix stale links, command names, screenshots, diagrams, or architecture notes
- reconcile docs after a code change, refactor, or release

Do NOT use this skill when:
- you are writing consumer release notes or per-skill changelog tables
- you are maintaining the skills repo's own README, agents, commands, or skill docs
- you are implementing product code instead of updating documentation

If the target is this repository, route to `docs-maintainer` instead.

**Trigger keywords:** project docs, consumer docs, README, getting started, install,
releasing, docs reference, onboarding docs, architecture docs, architecture diagram,
docs drift, documentation maintainer, project documentation, repo docs, library docs,
app docs, clean docs, clean up docs, tidy docs, docs cleanup, update docs, fix docs,
refresh docs, docs out of date, stale docs, docs are wrong.

**Freshness rule:** project docs drift whenever code, commands, config, or folder names
change — re-read the live project README, the touched docs, and the relevant source files
before editing. Re-check docs that mention version numbers, command names, or module
paths after each code change.

---

## Recommendation First

Default to this sequence:
1. Read the live docs and the source files they describe.
2. Update the smallest docs surface that is now stale.
3. Keep terminology, command names, module names, and links consistent across all touched files.
4. Re-run any repo-specific validation or link checks before handing the docs back.

Why:
- project docs are only useful when they match the code people actually run
- stale onboarding or README guidance causes more confusion than missing guidance
- keeping one canonical phrasing across README, onboarding, and reference docs avoids drift

### Architecture Diagram Rule

If the downstream project is an app or a library, include a simple architecture diagram
in the README or `docs/architecture.md` that shows the major modules, layers, or
runtime flow.

Use the diagram to answer "what is the shape of this project?" at a glance:
- app projects should show user-facing entry points, feature modules, and shared layers
- library projects should show public API surface, internal implementation modules, and
  the main integration points
- keep the diagram short enough that it stays useful in the README, then expand details
  in `docs/architecture.md` or reference pages when needed

Update the diagram whenever a module, boundary, or release flow changes.

### Default Docs Topology

If a downstream project does not already have a clear docs layout, use this structure as
the default. Keep the top-level docs visible, then branch active work into purpose-built
folders:

```text
docs/
├── tasks.md
├── roadmap.md
├── architecture.md
├── deployment.md
├── libraries.md
├── testing.md
├── reference/
├── mvp/
│   └── 0-mvp/
│       ├── 0-mvp.md
│       └── 0-phase/
│           ├── 0-phase.md
│           └── tasks/
│               ├── 0-task.md
│               └── 1-task.md
└── bugs/
    └── 0-bug.md
```

Use it like this:
- `docs/tasks.md` — single source of truth for current work, active decisions, and links
  into active plan or bug lanes
- `docs/roadmap.md` — consolidated planning, including integration and project planning
- `docs/architecture.md` — system design, kept as the stable long-form architecture doc
- `docs/deployment.md` — consolidated deployment and publishing flow
- `docs/libraries.md` — catalog of every library this project publishes: name, Maven
  coordinate, current version, publish status (stable/SNAPSHOT), link to its README.
  Cross-referenced from `kmp-library-publishing`'s release checklist so
  a release has somewhere durable to point to, instead of nowhere
- `docs/testing.md` — overview/index of test coverage: which modules have unit tests vs.
  Roborazzi screenshot coverage, where goldens live, how to run the full suite locally.
  Doesn't duplicate `kmp-unit-testing`/`kmp-roborazzi`'s
  content — answers "what's actually covered" at a glance, the same relationship
  `deployment.md` already has to the CI/release skills
- `docs/reference/` — searchable technical audits, model setup notes, and deep references
- `docs/mvp/` — structured MVP planning records, phase notes, and task breakdowns
- `docs/bugs/` — active bug threads; start with one file (`0-bug.md`) and add a folder only
  if the bug lane needs multiple files
- `docs/demos.md` — **conditional, not default.** Only add this if the project has a
  runnable demo/sample/catalog app module. A project with no demo module should not have
  this file at all — don't scaffold a demos page (or a demo module) just to have one

Use the active lanes like this:
- `docs/mvp/0-mvp/0-mvp.md` — the current MVP summary
- `docs/mvp/0-mvp/0-phase/0-phase.md` — the current phase plan
- `docs/mvp/0-mvp/0-phase/tasks/0-task.md` and `1-task.md` — individual task notes
- `docs/bugs/0-bug.md` — the active bug note for a single tracked issue

If the project needs chronological task history as well, keep `docs/tasks/` as the
archive lane for dated phase notes and pointers, but do not force every active doc there.

### Fix Maturity Lanes

Use one of these lanes for every fix note in `docs/tasks/`:

| Lane | Meaning | Where it lives |
|---|---|---|
| Dev | Investigating, changing fast, or not yet validated | `docs/tasks/` |
| Beta | Tested enough to share, but still under observation | `docs/tasks/` with a `beta` marker |
| Stable | Accepted and durable enough for the wider project | `docs/architecture.md`, `docs/deployment.md`, or `docs/reference/` |

Lifecycle rules:
- keep dev fixes in the dated task notes while they are still moving
- mark beta fixes clearly in `docs/tasks.md` or the dated note so they do not get mistaken for final guidance
- once a fix becomes stable, promote the final guidance into durable docs and leave the task note as history
- if a stable fix later regresses, open a new dated task note instead of rewriting history

Rules:
- make `docs/tasks.md` the primary entrypoint for day-to-day updates
- treat the layout as agile-friendly: backlog/current work lives in `docs/tasks.md`, execution
  history lives in dated task notes, and stable decisions graduate into architecture/docs
- put detailed phase history, approvals, and dated execution notes in `docs/tasks/`
- use `docs/mvp/` for the numbered MVP/phase/task tree when the project wants a visible
  planning hierarchy instead of flat task notes
- use `docs/bugs/0-bug.md` for a single active bug thread; only create `docs/bugs/0-bug/`
  when that bug needs multiple related files
- when a phase or task is complete, move its dated note into `docs/tasks/archive/` and keep
  a short index line or backlink in `docs/tasks.md`
- use date-stamped filenames in the archive (`YYYY-MM-DD-...`) so old work remains sortable
- if a note is still needed for current work, keep it in `docs/tasks/`; if it is done but
  still relevant to search, move it to `docs/tasks/archive/`
- if a note contains durable design or operating guidance, promote that guidance into
  `docs/architecture.md`, `docs/deployment.md`, or `docs/reference/` instead of leaving
  it only in task logs
- consolidate overlapping planning or deployment docs instead of duplicating them
- keep `docs/reference/` out of the main flow; it supports the core docs, it does not replace them
- link from README or onboarding docs to `docs/tasks.md` if the project has a lot of moving parts
- if older task files exist at the root, consolidate them into `docs/tasks/` and leave a pointer
  from `docs/tasks.md` instead of keeping parallel task indexes

### Project Doc Change Checklist

| Change | Update |
|---|---|
| New module, command, or phase | README, onboarding docs, `docs/tasks.md`, and any `docs/reference*` page that mentions it |
| Renamed command or path | Every docs mention, code sample, and navigation link |
| Architecture shift | README plus the affected reference pages, diagrams, and architecture notes |
| Release or setup change | RELEASING, INSTALL, and any onboarding checklist that relies on it |

## Project Doc Workflow

### 1) Read the current sources

Always inspect the live files first:
- the docs files you expect to change
- the code or config that those docs describe
- any linked reference docs that those files depend on

### 2) Edit the docs

Keep the docs narrow and accurate:
- prefer one canonical description over repeated paraphrases
- update examples to match the current repo shape
- remove references to deleted files, commands, or options
- if the user asks to "write a doc in docs", classify it first:
  - durable project guidance → `docs/architecture.md`, `docs/deployment.md`, or `docs/reference/`
  - current planning / MVP work → `docs/tasks.md` or `docs/mvp/0-mvp/0-phase/`
  - active bug tracking → `docs/bugs/0-bug.md` (or a `docs/bugs/0-bug/` folder only if the bug lane needs multiple files)
  - if the path is ambiguous, choose the narrowest durable home and explain the placement

### 2a) Use the default task template

When creating a new downstream project, start `docs/tasks.md` with this structure:

```markdown
# Tasks

## Current Objective

## Active Phase

## Open Questions

## Fix Lanes

- Dev:
- Beta:
- Stable:

## Task Log

- [YYYY-MM-DD-phase-1](tasks/YYYY-MM-DD-phase-1.md)
- [YYYY-MM-DD-phase-2](tasks/YYYY-MM-DD-phase-2.md)

## Archive Index

- [YYYY](tasks/archive/YYYY/)
```

### 2b) Use dated task filenames

Use short, action-first filenames for the dated notes:
- `YYYY-MM-DD-plan.md`
- `YYYY-MM-DD-refine.md`
- `YYYY-MM-DD-build.md`
- `YYYY-MM-DD-change-request.md`
- `YYYY-MM-DD-approval.md`
- `YYYY-MM-DD-retro.md`

Keep the filename focused on the phase or decision, not the whole feature name.

### 2c) Promote or archive

When a dated note is finished:
- archive it if it is still useful as a historical record
- promote stable guidance into `architecture.md`, `deployment.md`, or `reference/`
- leave a backlink in `docs/tasks.md` so the current work page still points to the history
- keep beta notes in the active task trail until they either stabilize or are discarded

### 3) Validate

Use the project's own checks when the docs mention build steps, commands, or generated
output. For pure text edits, verify links and filenames by inspection.

If the docs live in this skills repo, also run:

```bash
python3 scripts/scan_skill_issues.py
python3 skills/kmp-audit/scripts/audit_skills_repo.py .
```

## Testing

Use this validation matrix for project docs:

| Case | Expected |
|---|---|
| README mentions a module or command | The referenced file or command exists |
| `docs/tasks/` updates | `docs/tasks.md` links to the dated record and the dated record has a date-stamped filename |
| `docs/tasks/archive/` updates | Completed notes retain date-stamped filenames and `docs/tasks.md` still points to them |
| Task history becomes dense | The oldest active notes move to `docs/tasks/archive/` and durable guidance moves to architecture/reference docs |
| Dev/Beta/Stable fix lanes | Lane markers stay visible until a fix is promoted into durable docs |
| `docs/reference*` updates | Links resolve and match the code or configuration it documents |
| Onboarding docs change | The setup steps match the current project workflow |
| Release/setup docs change | Version numbers, paths, and commands reflect the current repo |
| Benchmark or performance comparison tables | Write the canonical table in `docs/reference/benchmark-matrix.md` (or the nearest durable `docs/reference/` page), and keep task-note summaries short with a link back |
| `docs/libraries.md` updates | Every listed Maven coordinate/version matches what's actually published (cross-check `gradle.properties`/`libs.versions.toml`, not just what the page claims) |
| `docs/testing.md` updates | Every module claimed to have coverage actually has test files under it — don't list a module as covered because it should be |
| `docs/demos.md` present | A real demo/sample/catalog module exists at the path the page references — this file should not exist at all if there's no demo module |

## Doc Classification and Hygiene

Read `references/docs-hygiene.md` before any clean-up task. It covers:
- Three-kind classification (Reference / Task / Non-doc) with examples
- `docs/` root vs `docs/reference/` placement rule
- Clean-up sequence (classify → check references → update links → move → consolidate → validate)
- Consolidation rule for task files scattered at the `docs/` root
- Naming convention (kebab-case; snake_case is flagged by the audit script)
- Hygiene limits table (line limits, lesson backlog, stale lessons, non-doc files)
- Lesson lifecycle and hygiene check commands

---

## Common Anti-Patterns

- Leaving a stale command name in README or onboarding docs after a rename.
- Updating one doc page and forgetting the linked reference page that explains it.
- Copying code snippets that no longer compile or run.
- Mixing consumer release-note content into general project docs.
- Scaffolding `docs/demos.md` (or a demo module) when the project has no runnable demo app — this page is conditional, not part of the default topology.
- Reusing this skill's internal `docs/` folder as the source for a public GitHub Pages developer guide — see `kmp-docs-site`, which uses a separate `website/` folder specifically to avoid leaking task notes/roadmap to a public site.

## Related Skills

- `kmp-audit` — catches doc drift when the docs repo or consumer project needs a health check.
- `kmp-release` — use when project docs need to explain versioning or publishing flow.
- `kmp-legal-docs` — use when the docs are specifically about privacy, terms, or compliance.
- `kmp-library-publishing` — owns the Maven Central pipeline `docs/libraries.md` catalogs; its release checklist should point here.
- `kmp-unit-testing` / `kmp-roborazzi` — own the actual test coverage `docs/testing.md` indexes; this page doesn't duplicate their content.
- `kmp-docs-site` — the public, GitHub-Pages-deployed developer guide for a published library; a separate concern from this skill's internal `docs/` — never share the same source folder.

## Output Style

When asked to update project docs, respond in this order:
1. files changed
2. source-of-truth files consulted
3. validations run
4. follow-up docs that should be updated next

Keep the response focused on the project's docs surface and the source files it mirrors.

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Added a "Delete vs Archive" section to `references/docs-hygiene.md` — real gap: the only prior guidance was a blanket "Never delete — archive," but that only made sense for the Consolidation Rule's task-kind files. Git history already preserves every version regardless of delete/archive, so the actual test is whether a human should be able to browse the old content again without going to git log — archive covers that case (task/bug resolution history); a fully-superseded reference doc, a `docs/` copy of a file already moved to its real home, or a leftover pre-rename file has nothing left to browse to and should just be deleted. Also registered `MIRROR_MAP.md` (from `kmp-api-mimicry`) as a named classification example. |
| 2026-07-11 | Added `docs/libraries.md` (Maven coordinate/version/publish-status catalog, cross-referenced from `library-publishing`) and `docs/testing.md` (test coverage index, cross-referenced from `unit-testing`/`roborazzi`) to the default topology — closing a real gap where "libraries" only had architecture-diagram guidance and "tests"/"demos" had nothing. `docs/demos.md` added as explicitly **conditional**, not default — only when a real demo module exists. Cross-referenced the new `kmp-docs-site` skill (public GitHub Pages developer guide) and drew an explicit boundary: never share this skill's internal `docs/` folder as that site's source. 2 new anti-patterns, 3 new validation-matrix rows. |
| 2026-06-27 | Extracted classification + hygiene into references/docs-hygiene.md. Added: docs/ root vs reference/ placement rule, clean-up sequence, consolidation rule, naming convention (kebab-case), non-doc file detection. Slimmed SKILL.md to a pointer. |
| 2026-06-27 | Added cleanup-intent trigger keywords: clean docs, tidy docs, docs cleanup, update docs, fix docs, stale docs, docs are wrong. |
| 2026-06-24 | Added fix maturity lanes for dev, beta, and stable fixes, plus a task template section for tracking them in `docs/tasks.md`. |
| 2026-06-24 | Added task lifecycle guidance: default task template, dated filename convention, archive/promotion rules, and agile-friendly lifecycle flow. |
| 2026-06-24 | Added archive policy: completed task and phase notes move to `docs/tasks/archive/` with date-stamped filenames and a backlink from `docs/tasks.md`. |
| 2026-06-24 | Expanded the default topology to include `docs/tasks/` for dated task and phase logs, with `docs/tasks.md` as the entrypoint. |
| 2026-06-24 | Added default docs topology: `docs/tasks.md`, `docs/roadmap.md`, `docs/architecture.md`, `docs/deployment.md`, and `docs/reference/` as the preferred organization for downstream projects. |
| 2026-06-24 | Added architecture-diagram guidance for downstream app and library projects so the README or `docs/architecture.md` shows the major modules and flow. |
| 2026-06-24 | Initial release — consumer-facing project docs workflow, onboarding and reference-doc sync, link hygiene, and validation guidance. |
