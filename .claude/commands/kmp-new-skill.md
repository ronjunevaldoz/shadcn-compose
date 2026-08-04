# /kmp-new-skill $ARGUMENTS

**KMP Agent Skills** — scaffold a new SKILL.md from scratch, following every structural
rule enforced by `audit_skills_repo.py` and `scan_skill_issues.py`.

Skill name (kebab-case): **$ARGUMENTS**  
Example: `kmp-new-skill kmp-offline-first`

---

## Step 1 — Validate the name

Rules for the name:
- Must start with `kmp-` (or `jni-` for native bridge skills)
- Must be kebab-case, all lowercase, no spaces
- Must not already exist: `ls skills/ | grep $ARGUMENTS`
- Must not duplicate an existing skill's concern — check `skills/kmp-expert/SKILL.md` routing map

If a duplicate concern exists, load the existing skill and use `/modify-skill` instead.

---

## Step 2 — Define the skill scope

Before writing anything, confirm the scope:

| Scope | What to do |
|---|---|
| Consumer-facing skill | Continue with this command and scaffold under `skills/` |
| Repo-internal workflow | Stop here and update `agents/`, `commands/`, `scripts/`, or repo docs instead |
| Consumer changelog / release notes | Hand off to `agents/changelog.md` and `/release-notes` |

Then answer these questions:

1. **What one problem does this skill solve?** (one sentence)
2. **What are the 3 trigger phrases** that should route to this skill?
3. **Which existing skills does it depend on?** (check the expert routing map)
4. **Which existing skills depend on it?** (will need to add a `## Related Skills` pointer)
5. **Is there a library involved?** If yes, what is the current stable version?

Show the answers and wait for user confirmation before writing the file.

---

## Step 3 — Scaffold the SKILL.md

Create `skills/kmp-$ARGUMENTS/SKILL.md` with this exact structure:

```markdown
---
name: kmp-$ARGUMENTS
description: >
  <2–3 sentence description — specific enough to match keyword routing. State what it
  covers, what library/API it uses, and what it does NOT cover.>
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '<today YYYY-MM-DD>'
  keywords:
    - <keyword 1>
    - <keyword 2>
    - <keyword 3>
    - <more as needed>
---

## When to Use This Skill

Use this skill when:
- <specific scenario 1>
- <specific scenario 2>
- <specific scenario 3>

Do NOT use this skill when:
- <counter-case 1 — what this skill does not cover>
- <counter-case 2>

**Trigger keywords:** <comma-separated list matching the keywords above>

**Freshness rule:** <library or API name> changes between releases — recheck
<where to check: docs URL or changelog> before starting any implementation.
Minimum version: `<library> = "<version>"` in `libs.versions.toml`.

---

## Recommendation First

Default to this approach:

<one paragraph — the canonical pattern. What to create, where it lives in the
6-layer structure, what to inject via Koin.>

<code snippet — the minimal viable implementation: interface + one real usage>

---

## <Core section — name it for the skill's central concept>

<The main body of the skill: patterns, code snippets, platform differences,
Koin wiring, TOML declarations. Use H3 subsections for distinct sub-topics.>

---

## Testing

<Fake implementation + 2–3 @Test cases. Follow the format from existing skills.>

---

## Common Anti-Patterns

- **<Pattern name>** — <what developers commonly do wrong>. <Why it's wrong>. <What to do instead>.
- <Add at least 3 anti-patterns>

---

## Related Skills

- `<dependency-skill>` — <one sentence on how it relates>
- `<consumer-skill>` — <one sentence>

---

## Output Style

When asked about <topic>, respond in this order:
1. recommendation (the default approach)
2. code snippet (minimal working example)
3. why this approach fits
4. main alternative

<Keep responses focused — this skill covers <X>, not <Y>.>
```

---

## Step 4 — Verify the scaffold

```bash
python3 skills/kmp-audit/scripts/audit_skills_repo.py .
```

Fix any findings before proceeding.

---

## Step 5 — Register the skill

1. Update the expert skill's routing map (`skills/kmp-expert/SKILL.md`):
   - Add a row to the Skill Invocation Map table
   - Add a node to the dependency graph
   - Update the `last-updated` date

2. Update the planner's routing table (`agents/planner.md`):
   - Add the appropriate "Feature touches → Load these skills" row

3. Update `agents/planner.md` skill count if it still references the old number.

---

## Step 6 — Add tests for any bundled script

If the skill ships a script under `skills/kmp-$ARGUMENTS/scripts/`,
add test coverage in a new `tests/test_<script-name>.py` (one file per script — use
`tests/_helpers.py`'s `load_module`/`REPO_ROOT`), following the pattern in any
existing sibling `tests/test_*.py` file.

---

## Step 7 — Commit and release

```bash
git add skills/kmp-$ARGUMENTS/ \
        skills/kmp-expert/SKILL.md \
        agents/planner.md
git commit -m "feat(<skill-name>): add new skill — <one-line description>"
python3 scripts/release.py minor
```

Minor bump because a new skill is a new capability, not a patch.

---

## Notes

- Skill descriptions must be **specific** — vague descriptions fail keyword routing.
  Test: would the expert skill route to this skill given its trigger keywords?
- Code snippets must be syntactically valid Kotlin. No pseudocode, no partial imports.
- The Testing section is required — `scan_skill_issues.py` will flag it as HIGH severity if missing.
- Do not create a skill for a concern already covered by combining two existing skills.
  The expert skill's "load these skills" pattern handles composition — a new skill
  should introduce a genuinely new domain, not re-package existing guidance.
