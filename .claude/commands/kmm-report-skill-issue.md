# /report-skill-issue $ARGUMENTS

**KMM Agent Skills** — file a bug, improvement request, or feature request against the
`ronjunevaldoz/kmm-agent-skills` repo directly from your current session.

Works from **any project** — you do not need the skills repo checked out locally.
Requires `gh` CLI authenticated (`gh auth status`).

Issue description: **$ARGUMENTS** (or nothing — will collect interactively)

---

## Step 0 — Triage: skill issue or project issue?

**Ask this before doing anything else.**

| Question | Answer → action |
|---|---|
| Did a skill give you wrong guidance, miss a case, or produce broken code? | **Skill issue** — continue with this command |
| Did you apply correct skill guidance and something still broke in your project? | **Project issue** — file in your own repo, not here |
| Does the skill work but your project has an architecture violation? | **Project issue** — run `/run-audit` first |
| Are you unsure whether the skill or your code is at fault? | Ask Claude: "Is this a skill problem or a project problem?" before filing |

**Hard stop — do NOT continue if:**
- The bug is in your project code and the skill guidance was correctly applied
- The issue is about a 3rd-party library the skill references (file upstream, not here)
- The problem only happens in your specific project environment (not reproducible from the skill guidance alone)

If the user is unsure, ask: *"Did following the skill's guidance cause this, or did something else in your project go wrong?"*

Only continue past Step 0 if the answer is clearly: **the skill itself is wrong or incomplete**.

---

## Step 1 — Classify

Determine the type before writing anything:

| Type | Label | When to use |
|---|---|---|
| `skill-bug` | `skill-bug` | A snippet, example, or rule in a skill is wrong or won't compile |
| `skill-gap` | `improvement` | A skill is missing a section, anti-pattern, or edge case |
| `kmm-new-skill` | `enhancement` | A KMP / JNI / Android concern is not covered by any existing skill |
| `improvement` | `improvement` | Guidance exists but could be clearer or more complete |
| `pipeline-bug` | `skill-bug` | Claude activated the wrong skill, or produced wrong output for a trigger phrase |

If `$ARGUMENTS` is non-empty, infer the type from the description. Otherwise ask the user.

---

## Step 2 — Check for duplicates

```bash
gh issue list --repo ronjunevaldoz/kmm-agent-skills --state open --search "$ARGUMENTS"
```

If a duplicate exists, show the title and URL and ask:
**Add a comment to the existing issue / Open a new issue / Cancel**

On "Add a comment": run `gh issue comment <number> --repo ronjunevaldoz/kmm-agent-skills --body "<body>"` and stop.

---

## Step 3 — Collect details

Ask the user for anything not already provided in `$ARGUMENTS`:

| Field | Question |
|---|---|
| **Skill name** | Which skill triggered this? (e.g. `kotlin-multiplatform-jni-pro`) |
| **Trigger phrase** | What did you type that activated it? |
| **What happened** | What did the skill produce or say? |
| **Expected** | What should it have said or done instead? |
| **Severity** | Blocking your work (HIGH) / workaround exists (MEDIUM) / cosmetic (LOW) |

If any detail is already clear from `$ARGUMENTS`, skip that question.

---

## Step 4 — Draft the issue body

Use exactly this template:

```markdown
## Summary

<One sentence: what is wrong or missing, in which skill.>

## Trigger phrase

`<what the user typed to activate the skill>`

## What happened

<What the skill produced — paste the relevant output or guidance.>

## Expected behaviour

<What the skill should have said or done.>

## Affected skill

`skills/<skill-name>/SKILL.md`

## Severity

HIGH / MEDIUM / LOW

## Environment

- Skills version: <from CHANGELOG.md or unknown>
- Project type: <KMP / Android / JNI / other>

---
*Filed via /report-skill-issue from a consumer session.*
```

---

## Step 5 — Confirm and submit

Show the full draft to the user:

```
Repo:    ronjunevaldoz/kmm-agent-skills
Title:   <title>
Labels:  <labels>
Body:    (shown above)

Submit? [yes / edit / cancel]
```

On **yes**:

```bash
gh issue create \
  --repo ronjunevaldoz/kmm-agent-skills \
  --title "<title>" \
  --label "<labels>" \
  --body "$(cat <<'EOF'
<body>
EOF
)"
```

Print the returned issue URL. Done.

On **edit**: show each field for inline correction, then re-show the full draft.
On **cancel**: discard and stop.

---

## Notes

- Never file an issue if `$ARGUMENTS` describes something already in CHANGELOG.md — check first:
  `gh api repos/ronjunevaldoz/kmm-agent-skills/contents/CHANGELOG.md --jq '.content' | base64 -d | head -60`
- For a bug you can fix yourself: apply the fix in your project, then file the issue referencing what you changed so the maintainer can upstream it.
- Label `priority: high` if the issue is blocking — add it alongside the type label.
