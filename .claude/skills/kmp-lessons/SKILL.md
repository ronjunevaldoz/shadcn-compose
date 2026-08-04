---
name: kmp-lessons
description: >-
  Consumer-side skill for capturing lessons learned from real KMP project work.
  Writes structured lesson files to docs/lessons/ whenever a skill's guidance
  was wrong, incomplete, or led to a bug — so that knowledge flows back into
  the skills collection. Use this skill whenever you fix a bug caused by
  following skill guidance, discover a better pattern than what the skill
  teaches, find a gap in skill coverage, or notice that a skill's dependency
  reference is stale. Trigger it proactively after any non-trivial fix —
  don't wait for the user to ask.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-12'
  keywords:
    - lessons learned
    - skill feedback
    - knowledge capture
    - consumer training
    - skill amendment
    - docs/lessons
    - pattern discovery
    - correction
    - skill gap
    - auto file github issue
    - proactive issue draft
    - report skill bug automatically
---

## When to Use This Skill

Use whenever you:
- Fix a bug that was caused (or not prevented) by following skill guidance
- Discover a better pattern than what the skill currently teaches
- Hit a gap — the skill had no guidance for what you needed
- Find that a skill's version reference or API example is stale
- Work around a known limitation that isn't documented anywhere

**Trigger keywords:** write a lesson, capture lesson, document what we learned,
skill was wrong, skill gap, better pattern, correction, docs/lessons,
what we should have done, upstream this, feed back to skills.

**Trigger automatically (no user prompt needed):** after any fix where you
deviated from skill guidance, or where the skill gave you no guidance at all. For a
`high`-severity `correction` (a genuine skill bug), also proactively draft a GitHub
issue alongside the lesson — see "Proactively Offering to File a GitHub Issue" below.

---

## Creating a Lesson — one file per finding

**Always create lessons with the script** — it writes exactly ONE file per finding to
`docs/lessons/YYYY-MM-DD-<slug>.md` and never appends to or overwrites an existing file:

```bash
python3 ~/.claude/skills/kmp-lessons/scripts/create_lesson.py \
  --skill kmp-mvi \
  --type correction --severity high \
  --title "Effect replayed on nav back" \
  --followed "…" --broke "…" --correct "…" --evidence "file:line" --proposed "…"
```

(From inside kmp-agent-skills, use `skills/kmp-lessons/scripts/create_lesson.py`.)

**One finding = one invocation = one file.** If you have three findings, run the script
three times — never hand-write a single file containing multiple lessons, and never append
a second lesson to an existing file. The harvester reads `docs/lessons/*.md` as one Lesson
per file; combining them breaks grouping and per-lesson review.

Body fields are optional — omit them and the script inserts a `_TODO_` placeholder to fill in.
A repeated title gets a numeric suffix so an existing lesson is never overwritten.

---

## Lesson File Format

Each lesson is its own file at `docs/lessons/YYYY-MM-DD-short-slug.md` in the consumer project.

```markdown
---
skill: kmp-mvi
date: 2026-06-26
severity: high
type: correction
---

## What we followed

Brief description of what the skill said to do, or what it was silent about.

## What broke / what we discovered

What actually happened in this project. Be specific — include the symptom,
not just the root cause.

## Correct pattern

The right way to do it. Include a minimal code example if the fix is non-obvious.

## Evidence

File paths, error messages, test output, or commit references that back this up.

## Proposed skill change

Where in the skill this should land:
- Section name (e.g. "Common Anti-Patterns", "error-patterns.md")
- Whether it is a new entry, a correction to existing guidance, or a new section
```

---

## Field Reference

### `skill`
The exact skill name from the kmp-agent-skills collection. Use the directory
name (e.g. `kmp-mvi`, `kmp-network-layer`).
Use `unknown` if the lesson doesn't map to a specific skill — the harvester
will route it.

### `severity`
How badly the gap or error affected the project.

| Value | Meaning |
|---|---|
| `high` | Caused a bug in production or required significant rework |
| `medium` | Caused wasted time or a non-trivial workaround |
| `low` | Minor inconvenience; edge case |

### `type`
What kind of finding this is.

| Value | Meaning |
|---|---|
| `correction` | Skill guidance was wrong — following it caused a problem |
| `gap` | Skill had no guidance for this scenario |
| `better-pattern` | Skill was correct but a better approach exists |
| `deprecation` | Skill references a stale API, version, or library |
| `confirmation` | Skill guidance worked well — worth reinforcing |

---

## Writing Good Lessons

**Be specific about the symptom.** "The MVI effect was replayed" is better
than "the UI was broken." The harvester needs enough context to know which
section of the skill to amend.

**Include evidence.** A file path, a stack trace excerpt, or a before/after
code snippet makes the lesson actionable. Without evidence, the harvester
cannot verify the finding.

**Propose where it lands.** You don't need to write the skill amendment
yourself — but naming the section (e.g. "add to Common Anti-Patterns") saves
the harvester a reasoning step.

**One finding per file.** If you have three corrections, write three files.
The harvester groups by skill and date, so separate files are easier to
process and review individually.

---

## Example Lessons

### Correction — effect replayed on nav back

```markdown
---
skill: kmp-mvi
date: 2026-06-20
severity: high
type: correction
---

## What we followed

The skill said to emit one-shot effects via SharedFlow. We did that.

## What broke / what we discovered

On navigating back and returning to the screen, the effect was replayed
because the ViewModel was not cleared — it was scoped to the NavBackStackEntry
but the SharedFlow had replay=1 set from a previous copy-paste.

## Correct pattern

SharedFlow for effects must use replay=0. Any value above 0 replays the
effect on new collectors, which re-triggers navigation or dialogs.

\```kotlin
private val _effects = MutableSharedFlow<TodoEffect>(replay = 0)
\```

## Evidence

`feature/todo/presenter/src/commonMain/kotlin/TodoListViewModel.kt` line 34.
Bug reproduced in instrumented test `TodoListScreenTest#navigateBackAndReturn`.

## Proposed skill change

Add to `references/error-patterns.md` as EP-10: SharedFlow replay > 0 causes
effect re-delivery on new collectors. Add to "Common Anti-Patterns" table.
```

### Gap — no guidance for paging with offline-first

```markdown
---
skill: kmp-paging
date: 2026-06-22
severity: medium
type: gap
---

## What we followed

The paging skill covers RemoteMediator setup but assumes network-only data.

## What broke / what we discovered

When combining paging with the offline-first pattern (SQLDelight local source +
Ktor remote source), the RemoteMediator's loadType logic conflicts with the
local cache invalidation strategy from the offline-first skill. Neither skill
addresses the combined case.

## Correct pattern

RemoteMediator should not invalidate the full cache on REFRESH when offline —
it should check connectivity first and skip the network call entirely.

## Evidence

`core/feed/data/src/commonMain/kotlin/FeedRemoteMediator.kt`.
Network errors surfaced as empty paging state instead of cached data.

## Proposed skill change

Add a "Paging + Offline-First" section to kmp-paging, or add
a cross-skill note in kmp-offline-first under "Known Gaps".
```

---

## Proactively Offering to File a GitHub Issue

Writing the lesson file is not the end of the workflow for a genuine bug. **When the
lesson is `severity: high` and `type: correction`** — the skill's guidance was wrong and
it actually broke something — don't just write the file and stop. In the same turn,
also draft a GitHub issue for `ronjunevaldoz/kmp-agent-skills` using
`/report-skill-issue`'s Step 4 template (populated from the same `followed`/`broke`/
`correct`/`evidence` content you just used for the lesson), show the user the full
draft, and ask whether to submit it — the same confirmation gate that command already
uses. Don't wait for the user to separately remember to run `/report-skill-issue`
themselves; the lesson and the issue draft come from the same discovery, so drafting
both is barely more work than drafting one.

**Filing the issue itself still requires the user's explicit "yes."** This only makes
the *suggestion and draft* proactive — creating a GitHub issue is a real, visible action
on someone else's repo, not something to submit silently even when the bug is obvious.

For `gap`, `better-pattern`, `deprecation`, or `confirmation` lessons, or `correction`
lessons below `high` severity, the local lesson file is enough — those are fine to batch
into a later harvest via `kmp-skill-harvester` rather than filing
immediately.

---

## Directory Convention

```
docs/
└── lessons/
    ├── 2026-06-20-mvi-effect-replay-shared-flow.md
    ├── 2026-06-22-paging-offline-first-gap.md
    └── 2026-06-25-sqldelight-migration-stale-schema.md
```

Commit lesson files alongside the fix that prompted them. This keeps the
lesson co-located with the evidence in git history.

---

**Freshness rule:** the lesson format and harvester schema may evolve — recheck
`kmp-skill-harvester` before writing a batch of lessons to confirm
the frontmatter fields haven't changed.

---

## Recommendation First

Write a lesson immediately after discovering a pattern mismatch — while the context is still fresh. One lesson per finding. Use the `correct` field to capture the fix, not just the problem.

If you are unsure whether a finding warrants a lesson: if it took more than one lookup or correction to resolve, it warrants a lesson.

---

## Common Anti-Patterns

- Writing a lesson that describes the symptom but not the root cause — the `what_broke` field should explain _why_, not just _what_
- Using vague titles like `fix` or `update` — the title is the primary search key; make it specific enough to find without reading the body
- Filing a lesson for a one-off project quirk rather than a repeatable pattern — lessons are only useful if the finding could recur in another project
- Skipping `evidence` — without a file path or line reference, the lesson cannot be verified or acted on by the harvester
- Combining multiple findings into one file, or appending a new lesson to an existing file — the harvester reads one Lesson per file, so this breaks grouping and review. Run `create_lesson.py` once per finding instead — caught by the audit's `combined lesson file [HIGH]` if it happens anyway (e.g. a hand-written or merged file)
- Writing a `high`/`correction` lesson and stopping there — waiting for the user to remember `/report-skill-issue` themselves means a real bug sits unreported indefinitely; draft the issue in the same turn and offer to submit it
- Actually filing the GitHub issue without the user's explicit "yes" — the draft can be proactive, the submission cannot be

---

## Testing

Lessons are structured markdown files, not code. Validation is structural:

- Frontmatter has `skill`, `date`, `severity` (high/medium/low), `type`
  (correction/gap/better-pattern/deprecation/confirmation)
- Body has the sections: `What we followed`, `What broke / what we discovered`,
  `Correct pattern`, `Evidence`, `Proposed skill change`
- `skill` value matches a directory name under `skills/` in the skills repo (or `unknown`)
- `Evidence` contains at least one file path or code excerpt
- **Exactly one lesson per file**, named `YYYY-MM-DD-kebab-case-title.md` in `docs/lessons/`

Using `create_lesson.py` guarantees the frontmatter, section skeleton, naming, and the
one-file-per-lesson rule. Run the harvester to confirm a batch parses:
`python3 ~/.claude/skills/kmp-skill-harvester/scripts/harvest_lessons.py .`

`kmp-audit`'s `combined lesson file [HIGH]` detector is the backstop
for a lesson file that bypassed the script (hand-written, merged, or copy-pasted) —
it flags any `docs/lessons/*.md` file containing more than one `## What we followed`
section. Run `/kmp-run-audit` to catch this on an existing project.

---

## Output Style

When writing a lesson, respond with the complete lesson file content — no surrounding explanation. The file is the output. Only add prose if the user asks for a summary or context.

---

## Related Skills

- `kmp-skill-harvester` — reads all lessons in a project (or
  across projects) and proposes amendments to the source skills
- `kmp-audit` — runs after lessons accumulate to check whether
  the proposed amendments have been applied upstream
- `/kmp-report-skill-issue` — the draft template and submission flow this skill reuses when a `high`/`correction` lesson warrants filing immediately instead of waiting for a harvest

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-12 | Added "Proactively Offering to File a GitHub Issue" — previously, filing a GitHub issue for a skill bug required the user to separately remember `/report-skill-issue`, even after this skill had just proactively written a lesson describing the exact same bug. Now a `severity: high`, `type: correction` lesson also drafts the issue in the same turn, reusing `/report-skill-issue`'s Step 4 template — actually submitting it still requires the user's explicit confirmation, unchanged. Cross-referenced both ways with `/kmp-report-skill-issue`. 2 new anti-patterns. |
| 2026-07-09 | The one-lesson-per-file rule was documented but had no enforcement beyond `create_lesson.py` refusing to overwrite — a hand-written or merged file could still violate it silently. New `kmp-audit` detector `combined lesson file [HIGH]` flags any `docs/lessons/*.md` file with more than one `## What we followed` section. |
| 2026-06-30 | Added create_lesson.py — deterministic one-file-per-finding creator (auto date/slug, never appends or overwrites) + lesson-template.md. Hardened the one-lesson-per-file rule, fixed the Testing section to match the real frontmatter schema, new anti-pattern against combined/appended lesson files. |
| 2026-06-26 | Initial release — lesson format, field reference, examples, directory convention. |
