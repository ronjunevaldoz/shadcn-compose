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

### Classification examples

| File | Kind | Action |
|---|---|---|
| `architecture.md` | Reference (root) | Keep — primary, README-linked |
| `deployment.md` | Reference (root) | Keep — primary, README-linked |
| `stable-feature-rules.md` | Reference (root) | Keep — stable registry, updated in place |
| `auth-flow-internals.md` | Reference (reference/) | Move to `docs/reference/` — subsystem deep-dive |
| `reference/*.md` | Reference (reference/) | Keep in `docs/reference/` |
| `known-blockers.md` | Task | Rename to `YYYY-MM-DD-known-blockers.md`, move to `docs/tasks/`; archive when resolved |
| `milestone-tracker.md` | Task | Rename + move to `docs/tasks/`; archive when milestone ships |
| `q3-gap-plan.md` | Task | Rename + move to `docs/tasks/`; archive when plan completes |
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
| Unprocessed lessons in `docs/lessons/` | 20 files | Harvest via `kotlin-multiplatform-skill-harvester` |
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
python3 skills/kotlin-multiplatform-audit/scripts/audit_skills_repo.py . --docs-hygiene-only

# Full audit including docs hygiene
python3 skills/kotlin-multiplatform-audit/scripts/audit_skills_repo.py .
```
