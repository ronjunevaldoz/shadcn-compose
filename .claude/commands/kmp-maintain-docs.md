# /kmp-maintain-docs $ARGUMENTS

**KMP Agent Skills** — keep repo documentation, `docs/` reference material, agent docs,
command docs, and skill routing text aligned with the actual repository.

Arguments (all optional):
- `<path-or-file>` — focus on a specific doc surface
- `<skill-name>` — focus on a skill's docs and routing text
- _(blank)_ — scan the repo docs holistically

---

## Step 1 — Identify scope

If `$ARGUMENTS` names a file or path, use that as the primary source. Otherwise inspect
`README.md`, `GETTING_STARTED.md`, `INSTALL.md`, `RELEASING.md`, `docs/`, `agents/`,
`commands/`, and the touched skill docs.

If the requested work is consumer release notes or per-skill changelog tables, hand off
to `agents/changelog.md` instead.
If the requested work is a downstream project's README or `docs/reference*` pages,
hand off to `kmp-project-docs-maintainer`.
If the requested work is organizing active planning docs or bug lanes, still use
`kmp-project-docs-maintainer` and follow its `docs/mvp/` and
single-file `docs/bugs/0-bug.md` rule unless the bug lane needs multiple files.
If the requested work is `SKILL.md` routing, freshness, or skill-map maintenance,
use `agents/docs-maintainer.md` directly; there is no separate consumer skill for it.
If the requested work changes the README skill map or architecture diagram, keep the
diagram aligned with the routing text and agent/command roles.

## Step 2 — Load the docs agent

Load `agents/docs-maintainer.md` and follow its workflow.

## Step 3 — Edit docs

Make the smallest targeted edits needed to keep the docs in sync with the repo.
Keep command names, agent roles, routing references, and `docs/reference*` pointers
consistent across all touched files.

## Step 4 — Validate

If `skills/*` docs or routing text changed, run:

```bash
python3 scripts/scan_skill_issues.py
python3 skills/kmp-audit/scripts/audit_skills_repo.py .
python3 skills/kmp-expert/scripts/validate_skill_map.py --repo-root .
python3 skills/kmp-expert/scripts/validate_keyword_routing.py --repo-root .
```

If only repo docs changed, at minimum run:

```bash
python3 scripts/scan_skill_issues.py
```

## Step 5 — Report

Show:
1. files changed
2. docs or source-of-truth files consulted
3. validations run
4. any related docs that still need a follow-up pass
