---
name: kmp-refactor
description: >
  Decides whether a rename/move/copy/delete is safe to do as a textual sweep
  (markdown, config, scripts, skill directories) or must be delegated to a real
  symbol-aware tool (Android Studio / IntelliJ Refactor, for Kotlin source). Covers
  the verified textual-rename procedure (git mv, frontmatter/header fix, bulk regex
  sweep, residue check, full test+gate run) and the module-move checklist against
  this collection's 6-layer clean-architecture contract. Does NOT implement an
  AST-level Kotlin rename engine — that is what the IDE's refactor tooling is for.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-08-02'
  keywords:
    - refactor
    - rename
    - rename symbol
    - move file
    - move class
    - move package
    - move module
    - copy class
    - safe delete
    - rename skill
    - rename command
    - package rename
    - IntelliJ refactor
    - Android Studio refactor
    - extract module
    - dangling reference
---

## When to Use This Skill

Use this skill when:
- renaming, moving, or copying a file, directory, skill, command, class, package, or
  module inside a KMP project or this skills collection
- deciding whether a repo-wide textual rename is safe, or whether it must be done
  through the IDE's semantic refactor tools instead
- moving a class or module across this collection's 6-layer boundary
  (`:model`/`:api`/`:domain`/`:data`/`:presenter`/`:ui`) and needing to know what
  else has to change
- deleting a symbol, file, or skill and needing to confirm nothing still references it

Do NOT use this skill when:
- the task is a new feature or bug fix with no identifier change — that's normal
  implementation work, not a refactor
- the user can just run Android Studio's Refactor > Rename/Move directly and hasn't
  asked for a decision on which tool to use — don't insert process where none was asked for

**Trigger keywords:** refactor, rename, rename symbol, move file, move class, move
package, move module, copy class, safe delete, rename skill, rename command, package
rename, IntelliJ refactor, Android Studio refactor, extract module, dangling reference.

**Freshness rule:** IDE refactor menus (Android Studio / IntelliJ) reshuffle occasionally
between releases — if a named menu item (Refactor > Rename, Safe Delete, Move) doesn't
match what's on screen, check the current IDE's Refactor menu rather than assuming this
skill's naming is stale.

---

## Recommendation First

Classify the target before touching anything — the correct tool depends entirely on
what's being renamed, not on how the rename is phrased:

| Target | Do this | Why |
|---|---|---|
| Markdown, JSON, YAML, shell/Python scripts, skill/command directories | Textual sweep (this skill's procedure below) | No hidden semantics — every match is a real reference, verifiable by grep + test suite |
| Kotlin class, function, property, or file in a consumer project's source | Android Studio / IntelliJ **Refactor > Rename** (or **Safe Delete**, **Move**) | Real symbol resolution — a textual sweep can't tell a real usage from a string template, a `@SerialName` override, a shadowed local, or an unrelated identical word |
| Package or module move | IDE **Move** refactor for the Kotlin side, plus the manual module-boundary checklist below for Gradle wiring and visibility | Moving files alone doesn't fix `settings.gradle.kts`, module dependencies, or `internal`/`public` visibility across the new boundary |

If the target is a mix (e.g. renaming a skill that also has example Kotlin snippets
inside its `SKILL.md`), do the textual sweep for the skill's own identifiers and treat
embedded Kotlin snippets as prose — don't run a Kotlin-aware rename on markdown content.

---

## Textual Rename Procedure

This is the exact sequence used to rename this collection itself (kmm-agent-skills →
kmp-agent-skills, kotlin-multiplatform-* → kmp-*, and the kmp-compose-* regrouping) —
generalized. Follow it in order; skipping the verification steps is how a rename ships
half-done.

1. **Move first, with the VCS-aware command** — `git mv old new` for every file/directory,
   not a manual delete+recreate. This preserves history and lets `git diff --stat`
   show clean renames instead of adds+deletes.
2. **Fix the self-referential parts** — a moved file's own header/frontmatter often
   repeats its own name (`# /old-command-name`, `name: old-skill-name` in YAML
   frontmatter). Fix these in the same pass, per file, before the cross-reference sweep.
3. **Bulk cross-reference sweep** — write a one-off script (Python, not committed) that
   walks the repo and replaces every literal occurrence of the old identifier with the
   new one, across every text extension that could reference it (`.md`, `.py`, `.sh`,
   `.json`, `.toml`, `.yml`). Exclude `.git/`, `__pycache__/`, and any changelog-style
   file that is a historical record, not a live reference.
   - Order replacements **longest-identifier-first** when one old name is a substring of
     another (e.g. `foo-extended` must be replaced before `foo`, or the longer name gets
     mangled by the shorter pattern running first).
   - Re-run the script a second time — it should report zero further changes. If it
     doesn't, something wasn't idempotent (usually a pattern that's too broad).
4. **Residue check** — `grep -rli "<old-identifier>" --include="*.md" --include="*.py"
   --include="*.sh" --include="*.json" .` (excluding `.git/` and changelog files) must
   return nothing unexpected. Investigate every hit — it's either a legitimate historical
   reference or a miss the sweep script needs to cover.
5. **Full verification** — run every test in the suite and every repo-hygiene gate this
   collection ships (`audit_skills_repo.py`, `scan_skill_issues.py`,
   `scan_command_shell_portability.py`, `validate_skill_map.py`,
   `validate_keyword_routing.py`, `check_compat_matrix.py`, `check_redundancy.py`).
   A rename that passes the sweep but breaks a hardcoded literal in a script or test
   fixture is a real, common failure mode — the gates catch it before it ships.
6. **Commit and version honestly** — if consumers already depend on the old name (a
   deployed skill directory, a slash command, a marker filename), the rename is a
   breaking change. Mark it as one (a `BREAKING CHANGE` footer or `!` per this repo's
   commit convention) rather than downgrading it to a patch/minor bump to avoid the
   version number going up — the version number should describe what changed, not what's
   convenient.

---

## Module Move Checklist (crossing the 6-layer boundary)

Moving a class between `:model`/`:api`/`:domain`/`:data`/`:presenter`/`:ui` (see
`kmp-clean-architecture`) needs more than moving the file:

1. Move the file via the IDE's **Move** refactor, not a manual cut-paste — this updates
   every import automatically.
2. Check visibility: does the class need to become `internal` (staying inside its new
   module) or does it need an `:api` counterpart (a public contract other modules
   depend on)? Moving `:data` internals into `:domain` without this check is the most
   common violation this collection's Detekt architecture rules catch.
3. Update `settings.gradle.kts` and the module's `build.gradle.kts` dependencies if the
   move changes which modules depend on which.
4. Run `kmp-code-quality`'s Detekt architecture fitness functions (from
   `kmp-clean-architecture`) after the move — a clean compile does not mean the layer
   contract still holds.

---

## Safe Delete (zero-dangling-reference check)

Before deleting a symbol, file, or skill, confirm nothing still references it:

```bash
grep -rl "<identifier-to-delete>" --include="*.md" --include="*.py" --include="*.sh" \
  --include="*.kt" --include="*.kts" . 2>/dev/null | grep -v "\.git/\|__pycache__"
```

Zero hits (outside the file being deleted itself) means it's safe. Any hit is either a
real remaining dependency (fix the caller first, don't delete out from under it) or a
stale reference that should have been caught by an earlier rename's residue check.

---

## Testing a Rename Is Actually Done

A rename is not done when the sweep script reports zero further changes — that only
proves the script's own patterns are idempotent, not that they covered everything. Two
independent checks before calling it complete:

1. **Full test suite** — if the corpus has one (this collection's `pytest` suite, a
   consumer project's unit tests), run it. A hardcoded literal in a test fixture that the
   sweep script's patterns didn't anticipate is the single most common way a rename ships
   broken — the test suite catches it, a clean `grep` alone does not.
2. **Independent residue grep**, run by hand rather than trusting the sweep script's own
   report:
   ```bash
   grep -rli "<old-identifier>" --include="*.md" --include="*.py" --include="*.sh" \
     --include="*.json" . 2>/dev/null | grep -v "\.git/\|__pycache__"
   ```
   For a Kotlin symbol renamed via the IDE, the equivalent check is **Find Usages** on
   the old name before the rename, not after — the IDE's rename refactor already updates
   every real usage; a post-rename grep for the old name is only useful to catch string
   literals and comments the refactor tool intentionally leaves alone (which may or may
   not be correct — judge each hit, don't blindly re-sweep them).

---

## Common Anti-Patterns

- **Regex-renaming Kotlin source** — running a repo-wide sed across `.kt` files to
  rename a class. It will silently corrupt a `@SerialName("OldName")` that was meant to
  stay `"OldName"` for wire-format compatibility, a string template that happens to
  contain the old name as user-facing text, or a shadowed local variable with the same
  name. Use the IDE's Rename refactor for actual Kotlin symbols.
- **Skipping the residue check** — trusting the sweep script's own "0 changes on second
  run" without independently grepping for the old identifier. The sweep script can only
  find what its own patterns cover; a manual grep with a broader net catches what the
  patterns missed.
- **Downgrading a breaking rename to a patch bump** — renaming a consumer-facing
  directory or command and marking the commit `fix:` or `chore:` to avoid a major
  version bump. The version number exists to tell consumers whether it's safe to update
  blindly; gaming it defeats the point.
- **Renaming without a migration path for existing installs** — a hard cutover is fine,
  but leaving zero tooling to clean up a consumer's now-orphaned old-named files (because
  a plain re-sync only adds new files, it doesn't remove orphaned ones) turns one rename
  into a support burden. Ship a migration/cleanup script alongside the rename.

---

## Related Skills

- `kmp-clean-architecture` — defines the 6-layer boundary a module move must respect
- `kmp-code-quality` — the Detekt architecture fitness functions that verify a module
  move didn't break the layer contract
- `kmp-audit` — the repo-hygiene gates (`audit_skills_repo.py`, `scan_skill_issues.py`,
  etc.) that a textual rename must pass before shipping
- `kmp-migration` — for a larger structural migration (monolith → multi-module, MVVM →
  MVI) that involves more than a rename

---

## Output Style

When asked to plan or execute a rename/move/copy/delete, respond in this order:
1. classification (textual sweep vs IDE refactor vs both)
2. the affected file/identifier list
3. the procedure steps that apply, in order
4. verification command(s) to run before calling it done

Keep responses focused on the rename mechanics — this skill covers *how* to do the
rename safely, not the architectural judgment call of *whether* to rename (that's a
decision the user makes, or `kmp-audit` flags).

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-02 | Initial skill — codifies the textual-rename procedure used to rename this collection itself (kmm-agent-skills → kmp-agent-skills, kotlin-multiplatform-* → kmp-*, kmp-compose-* regrouping), the classification table for textual sweep vs IDE refactor, the module-move checklist, and the safe-delete dangling-reference check. |
