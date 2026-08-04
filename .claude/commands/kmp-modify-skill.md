# /kmp-modify-skill $ARGUMENTS

**KMP Agent Skills** — modify an existing skill safely: add or update a section, fix a
pattern, bump library versions, or resolve a flagged quality gap.

Skill name: **$ARGUMENTS** (e.g. `repository-pattern`, `design-system`)

---

## Rules — read before touching any SKILL.md

These protect the collection's coherence. Violating them breaks the audit and pipeline.

### 1. Never remove required sections

Every SKILL.md must keep all of these — removing any one will fail `audit_skills_repo.py`:

| Section / field | Where it lives |
|---|---|
| YAML frontmatter (`name`, `description`, `metadata`, `last-updated`) | Top of file |
| `## When to Use This Skill` | Body |
| `**Trigger keywords:**` | Inside "When to Use" |
| `**Freshness rule:**` | Inside "When to Use" or early body |
| `## Recommendation First` | Body |
| `## Common Anti-Patterns` | Body |
| `## Related Skills` | Body |
| `## Output Style` | Body |

### 2. Always bump `last-updated`

Any substantive edit must update `last-updated: '<YYYY-MM-DD>'` in the frontmatter.
Typo fixes may skip this. When in doubt, update it.

### 3. Keep code snippets compilable

Every Kotlin snippet must be syntactically valid. Do not write pseudocode or partial
imports. If you can't write a complete snippet, write a prose description instead.

### 4. Prefer adding to anti-patterns over deleting patterns

If a pattern is superseded, mark it as such with a comment rather than deleting it.
Deletion loses the institutional memory of why the pattern was wrong.

### 5. Testing section format

```markdown
## Testing

```kotlin
class FakeXxx : XxxInterface {
    // minimal implementation for unit tests
}

@Test fun `happy path description`() = runTest {
    // ...
}
```
```

A Testing section must include:
- At least one `Fake` implementation for the skill's main abstraction
- At least two `@Test` functions (happy path + error/edge case)
- Integration test pattern if the skill wraps a real driver or platform API

### 6. Do not change skill names or directory names

The `name:` field in frontmatter and the directory name under `skills/` must match
the entry in `skills.json`. Renaming without updating `skills.json` breaks routing.

### 8. Update tests when modifying a bundled script

If the modification touches any `.py` file under `skills/kmp-$ARGUMENTS/scripts/`
or any file under the top-level `scripts/`, update the matching `tests/test_<script-name>.py`
(tests are one file per script under `tests/`) in the same commit. The pre-commit hook
(`hooks/pre-commit-audit.sh`) will block the commit otherwise.

The test update must:
- Add a test for every new function introduced
- Update or remove tests for any function signature that changed
- Verify all new `main()` exit code paths are covered

### 7. Do not merge two skills

Each SKILL.md covers one concern. If a skill feels too broad, add a `## Related Skills`
pointer to the closely related skill — do not fold one into the other.

---

## Step 1 — Identify the skill

```bash
ls skills/ | grep $ARGUMENTS
```

If ambiguous, show a list and ask the user to confirm.

---

## Step 2 — Check current quality gaps

```bash
python3 scripts/scan_skill_issues.py | python3 -c "
import json, sys
d = json.load(sys.stdin)
issues = [i for i in d['issues'] if '$ARGUMENTS' in i['skill_dir']]
for i in issues:
    print(f\"{i['severity']}: {i['check']} — {i['detail']}\")
"
```

Show any open issues for this skill before making changes.

---

## Step 3 — Make the edit

Apply the change following the rules above. Common modifications:

| Change type | What to do |
|---|---|
| Add a Testing section | Add `## Testing` before `## Common Anti-Patterns` |
| Update a library version | Update the version in TOML snippets AND check Freshness rule |
| Add an anti-pattern | Add a bullet under `## Common Anti-Patterns`; include a before/after code pair |
| Fix a code snippet | Replace only the affected snippet; do not touch surrounding prose |
| Add a new section | Place it before `## Common Anti-Patterns` unless it is a terminal section |

---

## Step 4 — Verify the edit

```bash
python3 skills/kmp-audit/scripts/audit_skills_repo.py .
```

Zero findings expected. If findings appear, fix them before proceeding.

---

## Step 5 — Update `last-updated` and commit

1. Set `last-updated: '<today>'` in the skill's frontmatter
2. Commit:
   ```bash
   git add skills/kmp-$ARGUMENTS/SKILL.md
   git commit -m "docs(<skill>): <what changed>"
   ```
3. Cut a patch release:
   ```bash
   python3 scripts/release.py patch
   ```

---

## Notes

- If the edit closes a gap from `/summarize-issues`, re-run the scanner after to confirm
  the issue count dropped: `python3 scripts/scan_skill_issues.py | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['total_issues'], 'issues remaining')"`
- If you're updating library versions, also update the skill's `Freshness rule:` text to
  reflect the new minimum version and the date rechecked.
- Do NOT modify `skills.json` directly — it is managed by `scripts/release.py`.
