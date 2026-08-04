# Docs Hygiene Reference

Full rules for classifying, cleaning, and keeping `docs/` thin.
Read this before any clean-up task on a consumer project's `docs/` directory.

---

## Doc Classification

Every file in `docs/` is one of three kinds. Classify before acting.

| Kind | Test question | Lifetime | Location |
|---|---|---|---|
| **Reference** | "How does this work?" | Permanent — update in place | `docs/` root or `docs/reference/` |
| **Task** | "What are we doing right now?" | Temporary — active while work runs | `docs/tasks/` → `docs/tasks/archive/` when done |
| **Non-doc** | "Is this a fixture, spec, or generated file?" | Belongs elsewhere entirely | `tests/fixtures/`, `api/`, `spec/`, project root |

### docs/ root vs docs/reference/

Both hold reference docs — the distinction is scope:

| Goes in `docs/` root | Goes in `docs/reference/` |
|---|---|
| Primary reference that the whole project relies on (`architecture.md`, `deployment.md`) | Supporting deep-dives that back up a primary doc |
| Entry-point docs linked from README | Technical audits, model setup notes, subsystem specifics |
| One per major concern — stays short and navigable | Can be numerous; readers come here by following a link, not browsing |

Rule of thumb: if you'd link to it from README, it belongs in `docs/` root. If you'd link to it from `architecture.md` or `deployment.md`, it belongs in `docs/reference/`.

### Active work lanes

Use these lanes when the project keeps planning or bug history in nested folders:

| Lane | Purpose | Example location |
|---|---|---|
| Tasks index | current work, cross-links, state summary | `docs/tasks.md` |
| MVP lane | structured MVP plan, phases, task notes | `docs/mvp/0-mvp/0-mvp.md` |
| Phase lane | one active phase under the MVP | `docs/mvp/0-mvp/0-phase/0-phase.md` |
| Task lane | individual task notes under a phase | `docs/mvp/0-mvp/0-phase/tasks/0-task.md` |
| Bug lane | active bug thread, one bug per file | `docs/bugs/0-bug.md` |

Keep these lanes short-lived and promote stable guidance out of them into
`docs/architecture.md`, `docs/deployment.md`, or `docs/reference/`.

### Classification examples

| File | Kind | Action |
|---|---|---|
| `architecture.md` | Reference (root) | Keep — primary, README-linked |
| `deployment.md` | Reference (root) | Keep — primary, README-linked |
| `stable-feature-rules.md` | Reference (root) | Keep — stable registry, updated in place |
| `MIRROR_MAP.md` (from `kmp-api-mimicry`) | Reference (root) | Keep at `docs/MIRROR_MAP.md`, not project root — a permanent, update-in-place registry of mimicked API primitives; split by Reference API into `docs/reference/mirror-map-<reference>.md` once past the 150-line limit below |
| `auth-flow-internals.md` | Reference (reference/) | Move to `docs/reference/` — subsystem deep-dive |
| `reference/*.md` | Reference (reference/) | Keep in `docs/reference/` |
| `known-blockers.md` | Task | Rename to `YYYY-MM-DD-known-blockers.md`, move to `docs/tasks/` or `docs/bugs/`; archive when resolved |
| `milestone-tracker.md` | Task | Rename + move to `docs/tasks/` or `docs/mvp/`; archive when milestone ships |
| `q3-gap-plan.md` | Task | Rename + move to `docs/tasks/` or `docs/mvp/`; archive when plan completes |
| `0-bug.md` | Task lane | Keep active bug thread here; add a folder only if multiple bug files are needed |
| `0-mvp/` | Task lane | Keep active MVP plan here; archive or promote when stable |
| `tasks.md` | Task (entrypoint) | Keep at `docs/tasks.md` |
| `fixtures/*.json` | Non-doc | Move to `tests/fixtures/` or `src/test/resources/` |
| `openapi.json` | Non-doc | Move to `api/` or `spec/` at project root |

### Ambiguity test

**Will this still be useful and accurate six months from now without edits?**
- Yes → Reference.
- No → Task. Archive when done.
- Neither → Non-doc. Move it out of `docs/`.

---

## Clean-up Sequence

When cleaning a messy `docs/`, always follow this order to avoid breaking references:

1. **Classify every file** — apply the three-kind test to each file before touching anything.
2. **Grep for internal references** — before moving any file, find every doc that links to it:
   ```bash
   grep -r "filename-without-extension" docs/ README.md
   ```
3. **Update references first** — rewrite all links to point at the new location before moving the file.
4. **Move or archive** — rename to kebab-case + date prefix if needed, then move to the correct location.
5. **Consolidate task content** — if a task-kind file exists outside `docs/tasks/`, extract its active content into `docs/tasks.md` or a dated task note, then archive the original.
6. **Move non-docs out** — relocate fixtures, specs, and generated files to their proper homes outside `docs/`.
7. **Validate** — run the hygiene check and verify no links are broken.

---

## Consolidation Rule

If a task-kind file (blockers, gap plan, milestone tracker) exists at the `docs/` root:

1. Open `docs/tasks.md` and add a summary entry for the work it tracks.
2. Move detailed content into a dated file: `docs/tasks/YYYY-MM-DD-slug.md`.
3. Archive the original file: `docs/tasks/archive/YYYY-MM-DD-original-name.md`.
4. Leave a backlink in `docs/tasks.md` pointing to the dated archive entry.

Never delete — archive. The history is evidence.

If the project uses `docs/mvp/` or `docs/bugs/`, keep those lanes as the active
working surface and use `docs/tasks.md` as the index that links into them.

---

## Delete vs Archive

Git history already preserves every version of every file — archiving isn't what makes a
file recoverable, `git log`/`git show` does that regardless. Archiving is for one specific
case: a human should be able to stumble on the old content again *without* going to git
history. Ask which case applies:

| Case | Action | Why |
|---|---|---|
| Task-kind file, work is done (bug fixed, milestone shipped, plan completed) | Archive (`docs/tasks/archive/`, `docs/lessons/archive/`) | Future readers browsing `docs/` may want the resolution history without digging through git log |
| Reference doc now fully superseded, zero unique information left | Delete | Nothing left to browse to — keeping it around just recreates the clutter this checklist exists to prevent; git history covers "what did this used to say" |
| Non-doc file after it's been moved to its real home (`tests/fixtures/`, `api/`, project root) | Delete the `docs/` copy | The file lives on at its new path; leaving a stale copy in `docs/` is drift, not history |
| File superseded by a rename (old path, content unchanged) | Delete old path | `git mv`/rename already carries the history forward; a leftover old-named file is a duplicate, not an archive |

If in doubt whether a reference doc still has unique information, don't guess — grep for
inbound links (see Clean-up Sequence step 2) and check whether anything still points at it
before deleting.

---

## Naming Convention

All `docs/` files use **kebab-case**. Snake_case is a violation.

| Wrong | Correct |
|---|---|
| `auth_flow_internals.md` | `auth-flow-internals.md` |
| `milestone_tracker.md` | `2026-06-27-milestone-tracker.md` (task) or `milestone-tracker.md` (reference) |
| `q3_gap_plan.md` | `2026-06-27-q3-gap-plan.md` |

Task files also get a `YYYY-MM-DD-` prefix. Reference files do not need a date prefix.

The audit script (`audit_skills_repo.py --docs-hygiene-only`) flags snake_case filenames automatically.

---

## Hygiene Limits (enforced by audit script)

| Rule | Limit | Action |
|---|---|---|
| Any `docs/` file (outside `archive/`) | 150 lines | Split or archive completed sections |
| Unprocessed lessons in `docs/lessons/` | 20 files | Harvest via `kmp-skill-harvester` |
| Lesson file age without harvest | 30 days | Harvest or archive |
| Task file marked `status: done` in active `docs/tasks/` | 0 | Move to `docs/tasks/archive/` immediately |
| Non-doc file (`.json`, `.yaml`, etc.) directly in `docs/` | 0 | Move to purpose-specific directory |
| Snake_case filename in `docs/` | 0 | Rename to kebab-case |

### Lesson lifecycle

```
docs/lessons/YYYY-MM-DD-slug.md
    ↓  harvested + skill amended
docs/lessons/archive/YYYY-MM-DD-slug.md
```

### Running the hygiene check

```bash
# Docs hygiene only (fast)
python3 skills/kmp-audit/scripts/audit_skills_repo.py . --docs-hygiene-only

# Full audit including docs hygiene
python3 skills/kmp-audit/scripts/audit_skills_repo.py .
```
