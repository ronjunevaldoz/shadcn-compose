---
name: kotlin-multiplatform-skill-harvester
description: >-
  Reads docs/lessons/*.md files from one or more consumer KMP projects and
  proposes concrete amendments to the source skills in kmm-agent-skills.
  Use this skill when the user wants to incorporate consumer feedback into
  the skills collection, when lessons have accumulated in a project and need
  upstreaming, or when running a skills governance review. Produces a
  structured amendment report and optionally drafts the actual skill diffs.
  Trigger when the user says "harvest lessons", "upstream the lessons",
  "incorporate feedback", "update skills from consumer projects", or
  "skills review". Also trigger proactively when you see a docs/lessons/
  directory with unprocessed files.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-26'
  keywords:
    - lesson harvester
    - skill feedback
    - consumer training
    - skill amendment
    - upstream
    - knowledge loop
    - docs/lessons
    - skill improvement
---

## When to Use This Skill

Use when you need to:
- Read accumulated `docs/lessons/*.md` from a consumer project and propose skill amendments
- Run a cross-project review to find recurring patterns across multiple lessons
- Draft actual diffs to skill SKILL.md or references/ files for review
- Identify which skills have the most consumer-reported gaps or corrections

**Trigger keywords:** harvest lessons, upstream lessons, incorporate feedback,
update skills from projects, skills review, skill amendments, process lessons,
consumer corrections, skill training, feedback loop, process lesson issues,
triage lesson reports, harvest GitHub issues.

**Freshness rule:** recheck `kotlin-multiplatform-expert` and the source skill's
changelog before proposing amendments — the gap may already be fixed in a
recent release.

---

## Recommendation First

Run `scripts/harvest_lessons.py` first to parse and group all lessons, then
review the report manually against the source skill before proposing any diff.

Why: the script catches malformed frontmatter, duplicate findings, and
already-resolved gaps faster than manual review. The manual pass catches
nuance the script cannot — whether a lesson is project-specific or truly
generalizable.

---

## Input Sources

Lessons reach the harvester through two paths:

| Source | How | Who |
|---|---|---|
| `docs/lessons/*.md` | Consumer writes a structured markdown file in their own project; we run the harvester against their repo | Developers with local access to the consumer project |
| GitHub issue (`lesson` label) | Consumer files a [Lesson Report](https://github.com/ronjunevaldoz/kmm-agent-skills/issues/new?template=lesson_report.yml) issue on this repo | Any external user — no repo access needed |

Both paths produce the same fields (`skill`, `severity`, `type`, what broke, correct pattern, evidence) and feed into the same amendment flow.

### Fetching open lesson issues

```bash
# List all open issues with the lesson label
gh issue list --repo ronjunevaldoz/kmm-agent-skills --label lesson --state open

# View a specific issue
gh issue view <number> --repo ronjunevaldoz/kmm-agent-skills
```

Treat each lesson issue the same as a `docs/lessons/*.md` file — apply the
same filter criteria (Step 2) before drafting any amendment. Close the issue
with a comment referencing the skill changelog entry once the amendment is
applied.

---

## Harvest Flow

### Step 1 — Collect lessons

```bash
# Single consumer project
python3 scripts/harvest_lessons.py /path/to/consumer-project

# Multiple projects
python3 scripts/harvest_lessons.py /path/to/project-a /path/to/project-b

# Output as JSON for programmatic review
python3 scripts/harvest_lessons.py /path/to/project --format json
```

The script:
- Finds all `docs/lessons/*.md` under each project root
- Parses YAML frontmatter (`skill`, `date`, `severity`, `type`)
- Groups findings by target skill
- Flags malformed files and unknown skill references
- Produces a grouped report ordered by severity

### Step 2 — Filter before amending

Not every lesson warrants a skill change. Apply this filter before drafting any diff:

| Lesson type | Action |
|---|---|
| `correction` (high severity) | Always amend — add to error-patterns or anti-patterns |
| `correction` (medium/low) | Amend if it would prevent a real mistake; skip if project-specific |
| `gap` (high severity, fits existing skill) | Add a new section or cross-skill note |
| `gap` (high severity, new domain) | Propose a new skill — name it `kotlin-multiplatform-<topic>` |
| `gap` (medium/low) | Add a note in "Related Skills" or defer |
| `better-pattern` | Amend only if the improvement is general, not project-specific |
| `deprecation` | Always amend — stale references mislead every consumer |
| `confirmation` | No amendment needed; record as evidence the skill is working |

**Key question:** would this lesson have helped a different team on a different
project? If yes, amend. If it only makes sense with this project's constraints,
skip or add a note in the skill's "Common Anti-Patterns" table with a caveat.

### Step 3 — Read the source skill

Before drafting any diff, read the current SKILL.md and its references/ for
the target skill. The lesson may already be addressed in a recent update.
Check `last-updated` in the frontmatter and the Changelog section.

### Step 4 — Draft amendments

For each lesson that passes the filter, produce one of:

**A) New error-pattern entry** (for `correction` type):
```markdown
| EP-N | <symptom> | <root cause> | <correct pattern> |
```
Add to `references/error-patterns.md` and cross-reference in the
"Common Anti-Patterns" table in SKILL.md.

**B) New section or subsection** (for `gap` type, fits existing skill):
Add the missing guidance directly to SKILL.md under the relevant heading,
or create a new references/ file if the content is large (>50 lines).

**E) New skill proposal** (for `gap` type, new domain — no existing skill covers it):
```
## Proposed new skill: kotlin-multiplatform-<topic>

Reason: <N> lessons report a gap that no existing skill covers.
Suggested name: kotlin-multiplatform-<topic>
Covers: <one-sentence scope>
Run: /kmm-new-skill kotlin-multiplatform-<topic>
```
The suggested name must start with `kotlin-multiplatform-`. Never suggest a bare
topic name without the prefix.

**C) Version/API update** (for `deprecation` type):
Update the version reference and add a freshness note if one is missing.

**D) Anti-pattern table row** (for `better-pattern` type):
Add a row to "Common Anti-Patterns" contrasting the old and new approach.

### Step 5 — Present the proposal

Show the user a summary before touching any skill file:

```
## Harvest Report — 2026-06-26

### kotlin-multiplatform-mvi (3 lessons)
- [HIGH correction] SharedFlow replay=1 causes effect re-delivery → add EP-10
- [MEDIUM gap] No guidance for effect ordering with multiple collectors → new subsection
- [LOW confirmation] State hoisting pattern worked correctly → no change

### kotlin-multiplatform-paging (1 lesson)
- [MEDIUM gap] Paging + offline-first combined case not covered → cross-skill note

### Unknown skill (1 lesson)
- 2026-06-24-network-timeout.md — skill field missing; needs routing

Proposed changes: 3 amendments across 2 skills.
Proceed? (y/n)
```

Only apply changes after explicit confirmation.

### Step 6 — Apply and update changelog

For each amended skill:
1. Apply the diff to SKILL.md or the relevant references/ file
2. Bump `last-updated` in frontmatter
3. Add a changelog entry: `| YYYY-MM-DD | Harvested from <project>: <summary> |`

Then commit: `docs(skills): harvest lessons from <project-name>`

---

## Bundled Script

- `scripts/harvest_lessons.py` — parses docs/lessons/, groups by skill,
  validates frontmatter, produces a text or JSON report.

### Script usage

```bash
# Text report (default)
python3 skills/kotlin-multiplatform-skill-harvester/scripts/harvest_lessons.py \
  /path/to/consumer-project

# JSON report
python3 skills/kotlin-multiplatform-skill-harvester/scripts/harvest_lessons.py \
  /path/to/consumer-project --format json

# Multiple projects
python3 skills/kotlin-multiplatform-skill-harvester/scripts/harvest_lessons.py \
  /path/to/project-a /path/to/project-b --format json

# Only high-severity lessons
python3 skills/kotlin-multiplatform-skill-harvester/scripts/harvest_lessons.py \
  /path/to/consumer-project --min-severity high
```

---

## Amendment Patterns

### Correction → error-patterns.md

```markdown
### EP-10: SharedFlow replay > 0 re-delivers effects to new collectors

**Symptom:** One-shot effects (navigation, dialogs) re-trigger when a new
collector subscribes — e.g. on navigate-back and return.

**Root cause:** `MutableSharedFlow(replay = 1)` (or higher) buffers the last
emission and delivers it to every new collector, including ones that subscribe
after the fact.

**Correct pattern:**
\```kotlin
// Always use replay=0 for one-shot UI effects
private val _effects = MutableSharedFlow<ScreenEffect>(replay = 0)
\```

**Evidence:** reported in consumer project lesson
`docs/lessons/2026-06-20-mvi-effect-replay-shared-flow.md`
```

### Gap → new subsection

```markdown
## Paging + Offline-First

When combining paging with an offline-first cache strategy, the RemoteMediator
must check connectivity before attempting a network load on REFRESH — otherwise
it invalidates the local cache and returns empty state when offline.

\```kotlin
override suspend fun load(...): MediatorResult {
    if (loadType == LoadType.REFRESH && !connectivityMonitor.isOnline()) {
        return MediatorResult.Success(endOfPaginationReached = false)
    }
    // ... normal network load
}
\```

See `kotlin-multiplatform-offline-first` for the connectivity monitor pattern.
```

### Deprecation → inline version bump + freshness note

```markdown
- **Freshness rule:** recheck the [vanniktech plugin releases](https://github.com/vanniktech/gradle-maven-publish-plugin/releases) before scaffolding — the API surface changes with minor versions.
```

---

## Common Anti-Patterns

| Anti-pattern | Rule |
|---|---|
| Amending a skill for a project-specific constraint | Only amend when the lesson generalizes across projects |
| Amending before reading the current skill | The lesson may already be addressed; always read first |
| Applying amendments without user confirmation | Always show the harvest report and get explicit approval |
| One giant amendment per skill | One diff per lesson — easier to review and revert |
| Skipping the changelog entry | Every amendment needs a dated changelog entry so consumers know when guidance changed |

---

## Testing

The harvester is a document-analysis and diff-generation tool, not runtime code. Validation is output-driven:

- Run the bundled harvest script against a sample `docs/lessons/` directory and verify the report lists only findings that match the configured filter thresholds
- Check that each proposed amendment in the report references a real `skill` directory name
- Verify the `amendment_type` field is one of: `add_section`, `update_section`, `add_example`, `add_anti_pattern`, `add_version_note`, `create_skill`
- For `create_skill` proposals: the suggested skill name must start with `kotlin-multiplatform-`
- Confirm the report excludes lessons already marked `applied: true`

Run `python3 skills/kotlin-multiplatform-audit/scripts/audit_skills_repo.py .` to catch naming and line-limit violations in harvested output files.

---

## Output Style

When running a harvest, respond in this order:
1. State how many lessons were scanned and how many findings passed the filter threshold
2. Show the harvest report (grouped by skill, then by amendment type)
3. For each HIGH-severity finding, include the proposed diff inline
4. End with a summary: `N amendments proposed, M skills affected`

Do not narrate what each amendment means unless the user asks. The report is the primary output.

---

## Related Skills

- `kotlin-multiplatform-lessons` — consumer-side skill for writing the lesson
  files this harvester reads
- `kotlin-multiplatform-audit` — run after harvesting to verify the amendments
  were applied correctly and no new gaps were introduced
- `kotlin-multiplatform-project-docs-maintainer` — owns PLAN.md and CHANGELOG
  in the skills repo; coordinate when a harvest produces a significant update

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-26 | Added GitHub Issues as a second input source alongside docs/lessons/. Added lesson_report.yml issue template. |
| 2026-06-26 | Initial release — harvest flow, filter criteria, amendment patterns, bundled script. |
