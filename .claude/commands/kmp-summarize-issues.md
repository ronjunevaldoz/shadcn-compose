# /kmp-summarize-issues

**KMP Agent Skills** — scan all skills for quality gaps and generate a numbered list
of actionable fix prompts you can paste directly into the chat to trigger each fix.

Accepted aliases: `summarize kmm skills issues`, `skill issues`, `what needs fixing`

---

## Step 1 — Freshness check

```bash
python3 scripts/check_updates.py
```

If behind origin/main (exit 1) → offer Pull/Skip before scanning.

---

## Step 2 — Run the scanner

```bash
python3 scripts/scan_skill_issues.py
```

Parse the JSON output. Extract `issues`, `by_severity`, `by_check`, `open_known_issues`.

---

## Step 3 — Enrich HIGH issues

For every issue where `severity == "HIGH"` and `check == "missing_testing"`:

Read `skills/<skill_dir>/SKILL.md` and scan:
- `description` frontmatter — what the skill covers
- `keywords` frontmatter — what it's used for
- Existing code snippets — what patterns it teaches
- `## Related Skills` — what other skills depend on it

Use this context to write a **specific** testing prompt (not a generic one) that names:
1. The fake class to create (e.g. `FakeTokenStorage`, `FakeNavController`)
2. The test scenarios to cover (happy path, error path, edge case)
3. Any platform-specific test setup needed

---

## Step 4 — Format and print the report

Output in exactly this format:

```
KMP Skills Quality Report — <today's date>
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

OVERVIEW
  Skills scanned:   <N>
  Total issues:     <N>
  🔴 HIGH:          <N>   (testing gaps)
  🟡 MEDIUM:        <N>   (missing sections, stale)
  🔵 LOW:           <N>   (minor gaps)

OPEN KNOWN ISSUES
  <list from KNOWN_ISSUES.md, or "(none)">

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔴 HIGH — Testing Gaps
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[1] <skill-name>
    Gap:  <what is missing>
    Why:  <one sentence on what bugs this allows>

    ▶ Paste this prompt to fix:
    "<specific, ready-to-run prompt mentioning the fake class, test scenarios,
      and any integration test setup required>"

[2] ...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🟡 MEDIUM — Missing Sections / Stale Skills
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[N] <skill-name>
    Gap:  <what is missing>

    ▶ Paste this prompt to fix:
    "<prompt_hint from scanner, possibly enriched>"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔵 LOW — Minor Gaps
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[N] ...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SUGGESTED ACTION SEQUENCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Work through HIGH issues first — each fix follows this pattern:
  1. Paste the ▶ prompt above into the chat
  2. Agent adds the Testing section and commits
  3. Run `python3 scripts/release.py patch` to cut a patch release
  4. Repeat for the next item

Estimated effort per HIGH item: 1 chat turn + patch release.
After clearing all HIGH items, re-run /summarize-issues to confirm zero 🔴 findings.
```

---

## Step 5 — Offer actions

After printing the full report, ask:

```
What next?
  [a] Show only HIGH issues (ready to fix now)
  [b] Show by skill (all issues for one skill grouped)
  [c] Export as KNOWN_ISSUES.md additions
  [d] Create GitHub issues for all HIGH gaps
  [e] Done
```

### Option c — Export to KNOWN_ISSUES.md

For each issue not already in KNOWN_ISSUES.md:
- Assign the next `KI-NNN` ID
- Add it under `## Open` in the standard format
- Commit: `docs: add <N> quality gaps from /summarize-issues scan`

### Option d — Create GitHub issues for HIGH gaps

For each HIGH issue not already tracked on GitHub:

1. Check for an existing open issue:
   ```bash
   gh issue list --search "<skill-name> testing gap" --state open
   ```

2. If no duplicate found, draft the issue using the `/submit-issue` template:
   - **Title**: `skill-gap: <skill-name> — missing Testing section`
   - **Labels**: `skill-gap`, `testing`, `priority: high`
   - **Body**: pre-fill the evidence from the scanner output and the `prompt_hint` field

3. Show the draft and confirm before creating:
   ```
   Issue <N>/<total>: <skill-name>
   Title: skill-gap: <skill-name> — missing Testing section
   Labels: skill-gap, testing, priority: high

   Create? [y/n/skip all]
   ```

4. After creating all confirmed issues, report:
   ```
   GitHub issues created: <N>
   Skipped (already tracked or declined): <N>
   ```

Do not create issues in bulk without per-item confirmation. Never file duplicate issues.

---

## Notes

- The scan is purely static — it reads files, not runtime behaviour. It catches structural
  gaps, not logic bugs.
- Stale skills are flagged when `last-updated` is more than 6 months ago. A skill being
  "stale" doesn't mean it's wrong — it means it should be reviewed against current upstream
  library versions before using it.
- The scan intentionally skips meta/infra skills that don't need testing sections:
  `audit`, `clean-architecture`, `ci-github-actions`, `xcframework-spm`, `code-quality`,
  `preview-driven-development`, `expert`.
- Re-run after any skill update to confirm the gap is closed:
  `python3 scripts/scan_skill_issues.py | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['total_issues'], 'issues remaining')"``
