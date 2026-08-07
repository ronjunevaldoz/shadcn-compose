# Step 0: Determine the component prefix

Part of `kmp-compose-design-system`. Load this file when working on: step 0: determine the component prefix.

---

> **Hard rule — never violated:** `App` (as in `AppButton`, `AppTheme`, `AppColors`) is a
> **template placeholder in this SKILL.md**, exactly like `GROUP_ID`. It must never be
> written to disk literally for a real project. Do not generate a file named
> `AppButton.kt` containing `class AppButton` for an actual project and leave a mental
> note to rename it later — resolve the real prefix FIRST (this step), then write every
> file directly with that name the first time. There is no "rename pass" step because
> there should be nothing left to rename.
>
> The only time literal `App*` is correct output is when the resolved prefix in the
> precedence below genuinely computes to `App` (rare — only for placeholder/example
> projects with no real name yet), or when working inside this skills repo itself,
> where `App` is the documented template convention on purpose.

**Precedence (highest to lowest):**

1. `COMPONENT_PREFIX` already recorded in `docs/design-system.md`, if that file exists — an explicit, previously-confirmed choice always wins
2. A prefix the user states directly in the request ("call it Acme", "use GB as the prefix")
3. Derived from the project name via the script below
4. `App` — only if nothing else yields a usable word (e.g. a genuine placeholder/example project)

**Run the derivation script** (steps 3–4 are deterministic, not a guess):

```bash
python3 ~/.claude/skills/kmp-compose-design-system/scripts/derive_component_prefix.py <project_root>
```

If running from inside kmp-agent-skills:
```bash
python3 skills/kmp-compose-design-system/scripts/derive_component_prefix.py <project_root>
```

The script reads, in order: `settings.gradle.kts` `rootProject.name` → the Gradle group
ID's last segment → the project directory name — strips generic noise words (`app`,
`android`, `ios`, `kmp`, `shared`, `compose`, `project`, `multiplatform`, `mobile`,
`client`, `core`, `main`), PascalCases what remains, and prints the result plus which
source it came from. Example: `rootProject.name = "GuildBase"` → prefix `GuildBase` →
`GuildBaseButton`, `GuildBaseCard`, `GuildBaseTextField`.

**Confirm before generating.** Show the derived prefix and its source, then ask the user
to confirm or override — a wrong prefix means regenerating every file, not a quick rename.
Once confirmed, record it in `docs/design-system.md` (`COMPONENT_PREFIX` field) so future
sessions read it from precedence step 1 instead of re-deriving.

**Then generate directly with the resolved name.** Every code block in Steps 1–9 below
shows `App` for template readability — when you actually write a file for a real project,
substitute the resolved prefix as you write it (`AppButton.kt` → `GuildBaseButton.kt`,
`class AppTheme` → `class GuildBaseTheme`), in the same pass, not as a follow-up edit.
The audit enforces this: `design system prefix mismatch` flags any `App*` class/fun/object
declaration under `core/designsystem` when `docs/design-system.md` records a different
`COMPONENT_PREFIX` — a real mismatch, not template text, since the audit only scans
actual `.kt` files in the target project.

---

