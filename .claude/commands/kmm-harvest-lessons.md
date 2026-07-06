# /kmm-harvest-lessons $ARGUMENTS

**KMM Agent Skills** — scan a consumer KMP project for positive patterns and propose
skill improvements to upstream back into kmm-agent-skills.

Target project: `$ARGUMENTS` (defaults to `.` if empty)

---

## What this does

The audit normally finds what's *wrong*. This command finds what's *right* —
patterns the consumer project does well that the skills don't yet teach.
Each finding becomes a concrete proposal: which skill to update, what to add, and why.

---

## Step 1 — Run harvest mode

```bash
python3 ~/.claude/skills/kotlin-multiplatform-audit/scripts/audit_project.py \
  --harvest "${ARGUMENTS:-.}"
```

Parse the JSON output. It has two keys:
- `findings` — architecture violations (same as `--audit`); show these as a summary only
- `lessons` — positive patterns detected; these are the focus of this command

If the script is not at `~/.claude/skills/`, try:
```bash
python3 skills/kotlin-multiplatform-audit/scripts/audit_project.py --harvest "${ARGUMENTS:-.}"
```

---

## Step 2 — Print the harvest summary

```
═══════════════════════════════════════════════════════
HARVEST REPORT: <project path>
───────────────────────────────────────────────────────
FINDINGS (violations):  <N>   (<N_HIGH> HIGH, <N_MED> MEDIUM)
LESSONS  (good patterns): <N>
═══════════════════════════════════════════════════════
```

If `lessons` is empty, output:
```
No positive patterns detected above what the skills already teach.
The project may be at an early stage or may not yet use KMP best practices.
```
and stop.

---

## Step 3 — For each lesson, check the target skill

For every entry in `lessons`:
1. Read `~/.claude/skills/<skill>/SKILL.md` (or `skills/<skill>/SKILL.md` if working inside kmm-agent-skills)
2. Search for the `pattern` and key terms from `description` in the skill text
3. Classify:
   - **NEW** — pattern is absent from the skill entirely → propose adding it
   - **ENHANCEMENT** — skill mentions the topic but lacks the specific detail → propose expanding
   - **ALREADY_COVERED** — skill already documents this → skip, note it's covered

Only propose changes for NEW and ENHANCEMENT lessons.

---

## Step 4 — Output one proposal per actionable lesson

Format each proposal as:

```
────────────────────────────────────────────────────────
LESSON: <pattern name>
Skill:  <skill slug>
Status: NEW | ENHANCEMENT
────────────────────────────────────────────────────────
WHY IT MATTERS:
  <1–2 sentences from description>

EVIDENCE IN CONSUMER:
  <evidence field from harvest JSON>

PROPOSED SKILL ADDITION:
  Section: <which section of the SKILL.md to add/update>
  Content:
    <concrete text or code snippet to add to the skill,
     written as if it were already in the skill doc>

HOW TO APPLY:
  From inside kmm-agent-skills:
    /kmm-modify-skill <skill slug>
  Then paste the proposed content above into the appropriate section.
────────────────────────────────────────────────────────
```

---

## Step 5 — Final summary

```
═══════════════════════════════════════════════════════
HARVEST COMPLETE
  Lessons found:       <N>
  Actionable (NEW):    <N>
  Enhancements:        <N>
  Already covered:     <N>

NEXT STEPS:
  1. Review proposals above
  2. Run /kmm-modify-skill <skill> for each NEW/ENHANCEMENT
  3. After updating skills, run /kmm-release-notes and release
═══════════════════════════════════════════════════════
```

---

## Step 6 — Submit lessons as GitHub issues (automatic prompt)

After the final summary, for each actionable lesson (status NEW or ENHANCEMENT), prompt the
user interactively — one lesson at a time:

```
─────────────────────────────────────────────────────
LESSON: <pattern name>  →  <skill slug>
<description one-liner>
Evidence: <evidence field>

Would you like to submit this as an improvement proposal?
[y] Submit   [n] Skip   [v] View draft first
─────────────────────────────────────────────────────
```

If the user says **y** or **v then y**, run:

```bash
python3 ~/.claude/skills/kotlin-multiplatform-audit/scripts/draft_issue.py \
  --title "Lesson: <pattern name> — <skill slug>" \
  --evidence "<evidence field from lesson dict>" \
  --recommendation "<proposed addition summary from Step 4>" \
  --skill "<skill slug>" \
  --submit \
  --repo ronjunevaldoz/kmm-agent-skills
```

If `gh` is not installed or not authenticated, print the draft body and instruct the user
to open `https://github.com/ronjunevaldoz/kmm-agent-skills/issues/new` manually.

After all lessons are processed, print:
```
DONE — <N> issues submitted, <N> skipped.
```

---

## Notes

- This command is read-only — it proposes changes, it does not apply them
- Run from inside a consumer project (not from kmm-agent-skills itself)
- If running from kmm-agent-skills, the skill paths are relative (`skills/<slug>/SKILL.md`)
- Lessons from multiple consumer runs accumulate; run harvest on Bytesweep, Graphyn,
  and lordnine-admin to build a full picture before modifying skills
- **Harvest compares against LOCAL skill files** (`~/.claude/skills/`), not the live GitHub
  repo. Run `/kmm-update-skills` first to ensure the comparison reflects the latest released
  skills — otherwise patterns already shipped in a new version may appear as NEW
