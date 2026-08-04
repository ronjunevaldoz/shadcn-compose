# /kmp-release-notes $ARGUMENTS

**KMP Agent Skills** — generate consumer-facing release notes and update per-skill
`## Changelog` sections.

Arguments (all optional):
- `<skill-name>` — generate notes for one skill only (e.g. `release-notes kmp-mvi`)
- `<vX.Y.Z>` — generate notes for a specific released version
- _(blank)_ — generate notes for everything since the last git tag

---

## Step 1 — Determine mode

```bash
# What is the last released tag?
git describe --tags --abbrev=0

# What changed since then?
python3 scripts/generate_release_notes.py --since $(git describe --tags --abbrev=0)
```

If `$ARGUMENTS` is a skill name:
```bash
python3 scripts/generate_release_notes.py \
  --since $(git describe --tags --abbrev=0) \
  --skill $ARGUMENTS
```

If `$ARGUMENTS` is a version tag (starts with `v`):
```bash
# Find the tag before it
PREV=$(git tag --sort=-version:refname | grep -A1 "$ARGUMENTS" | tail -1)
python3 scripts/generate_release_notes.py --since $PREV --until $ARGUMENTS
```

---

## Step 2 — Load the changelog agent

Load `agents/changelog.md` and follow its steps to:

1. Categorize the raw JSON output into `Breaking / New / Improved / Fixed`
2. Write consumer-facing prose for each entry
3. Assemble the release note document
4. Update per-skill `## Changelog` tables
5. Save to `docs/release-notes/` and update `CHANGELOG.md`

---

## Step 3 — Skill-only mode output

If `$ARGUMENTS` is a skill name, skip the collection release note. Instead:

1. Show the skill's current `## Changelog` table
2. Show commits that touched `skills/$ARGUMENTS/` since last tag
3. Draft new row(s) for the table in consumer language
4. Ask: _"Add these entries to the skill's `## Changelog`?"_
5. On confirmation: edit the SKILL.md and commit

Output format for the draft:

```
## Proposed Changelog entries for kmp-<name>

| Date | Change |
|---|---|
| YYYY-MM-DD | <consumer-facing description> |

Add to skills/kmp-<name>/SKILL.md? [y/n]
```

---

## Step 4 — Full release mode output

Show a preview of the generated release note and ask for confirmation before writing
any files. The preview should clearly show:

- The version number (inferred from the next bump, or ask the user)
- Count of breaking / new / improved / fixed entries
- Any skills whose `## Changelog` will be updated

```
Release notes preview: v1.15.0

  Breaking:  1 entry
  New:       2 entries
  Improved:  4 entries
  Fixed:     1 entry

Skills to update: feature-scaffold, expert, audit (3 of 47)

Write docs/release-notes/v1.15.0.md and update CHANGELOG.md? [y/n]
```

Always confirm before writing files.

---

## Notes

- Never auto-push. The release notes commit must be reviewed before pushing.
- If there are zero changes since the last tag, report that and exit — do not create
  an empty release note file.
- If `$ARGUMENTS` is blank and the `[Unreleased]` section of `CHANGELOG.md` is empty,
  use git log to infer what changed — the changelog may just not have been updated yet.
