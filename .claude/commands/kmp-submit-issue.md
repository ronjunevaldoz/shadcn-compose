# /kmp-submit-issue $ARGUMENTS

**KMP Agent Skills** — raise a well-formed GitHub issue for a skill gap, bug, pattern
improvement, or new skill request. Applies structured rules so every issue is immediately
actionable.

Issue description: **$ARGUMENTS** (or nothing — will prompt)

---

## Issue classification

Determine the type before writing anything:

| Type | When to use |
|---|---|
| `skill-gap` | A skill is missing a Testing section, anti-pattern, freshness rule, or required section |
| `skill-bug` | A code snippet in a skill is wrong, outdated, or won't compile |
| `kmp-new-skill` | A KMP concern is not covered by any existing skill |
| `pipeline-bug` | An agent (planner, implementer, reviewer, fixer, validator) produces wrong output |
| `improvement` | A skill covers the topic but the guidance could be clearer or more complete |

If the description matches more than one type, pick the most specific one.

---

## Step 1 — Check for duplicates

```bash
gh issue list --label "<type>" --state open
```

Also search:
```bash
gh issue list --search "$ARGUMENTS" --state open
```

If a duplicate exists, show it and ask the user: **Add a comment / Open new / Cancel**.

---

## Step 2 — Gather evidence

Before writing the issue, collect the facts:

| Question | How to answer |
|---|---|
| Which skill is affected? | `ls skills/ \| grep <keyword>` |
| What does the scan say? | `python3 scripts/scan_skill_issues.py` |
| What does the audit say? | `python3 skills/kmp-audit/scripts/audit_skills_repo.py .` |
| What is the current `last-updated`? | Read frontmatter of the skill |
| Is there a related KNOWN_ISSUES entry? | `grep -n "<skill>" KNOWN_ISSUES.md` |

---

## Step 3 — Draft the issue

Use exactly this template:

```markdown
## Summary

<One sentence: what is wrong or missing and in which skill.>

## Evidence

<Output from scan_skill_issues.py or audit_skills_repo.py, or a specific line from
the SKILL.md that is wrong. Be concrete — paste the offending line or snippet.>

## Expected behaviour

<What the skill should say or do. If it's a missing section, describe what it should
contain. If it's a wrong snippet, show the corrected version.>

## Affected skill

`skills/<skill-directory>/SKILL.md`

## Reproduction

<Steps to reproduce, if applicable. For a scan finding: "Run python3 scripts/scan_skill_issues.py;
find the issue under <skill-name>." For a pipeline bug: the exact prompt and the wrong output.>

## Suggested fix

<Optional. One sentence or a small diff. Leave blank if you don't know — the maintainer
will fill this in.>
```

---

## Step 4 — Apply labels

| Condition | Label |
|---|---|
| Missing Testing section | `skill-gap`, `testing` |
| Wrong or outdated code | `skill-bug` |
| New skill request | `kmp-new-skill` |
| Agent produces wrong output | `pipeline-bug` |
| Clarity improvement | `improvement` |
| HIGH severity from scanner | `priority: high` |
| MEDIUM severity | `priority: medium` |
| LOW severity | `priority: low` |

Create any label that does not exist yet with `gh label create`.

---

## Step 5 — Submit

Show the drafted issue to the user for approval. Confirm before creating:

```
Title:   <title>
Labels:  <labels>
Body:    (shown above)

Submit? [yes / edit / cancel]
```

On "yes":
```bash
gh issue create \
  --title "<title>" \
  --label "<labels>" \
  --body "$(cat <<'EOF'
<body>
EOF
)"
```

Report the issue URL after creation.

---

## Step 6 — Update KNOWN_ISSUES.md (optional)

If the issue warrants tracking locally (HIGH severity, pipeline regression, or the user
asks), add it to `KNOWN_ISSUES.md` under `## Open`:

```markdown
### KI-NNN — <title>

- **Skill**: `<skill-directory>`
- **Type**: <skill-gap | skill-bug | pipeline-bug | improvement>
- **Severity**: HIGH | MEDIUM | LOW
- **GitHub**: #<number>
- **Status**: Open
```

Commit: `docs: track KI-NNN in KNOWN_ISSUES.md`

---

## Notes

- Never create an issue for something already fixed in the current local branch — verify
  by running the audit and scanner first.
- Issues for new skills must include a one-paragraph description of what the skill would
  cover, why it belongs in this collection, and which existing skills it depends on.
- If the issue is about a real bug you can fix in under 5 minutes, fix it first with
  `/modify-skill` and then submit the issue referencing the commit. This keeps the
  issue tracker clean of trivial backlog.
