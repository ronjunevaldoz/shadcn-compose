# /review-changes

**KMM Agent Skills** — review everything in the current working tree against the
6-layer contract, Koin wiring rules, MVI contracts, and testTag coverage.

---

## Step 1 — Find changed files

```bash
git diff --name-only HEAD
git diff --name-only --cached
```

Bucket each file into its layer:

| Path pattern | Layer |
|---|---|
| `*/model/**` | `:model` |
| `*/api/**` | `:api` |
| `*/domain/**` | `:domain` |
| `*/data/**` | `:data` |
| `*/presenter/**` | `:presenter` |
| `*/ui/**` | `:ui` |
| `build-logic/**`, `*.gradle.kts` | build |
| `.github/**`, `*.yml` | CI |

---

## Step 2 — Load only the skills that apply

| Layers changed | Skills to load |
|---|---|
| `:model`, `:api`, `:domain` | `clean-architecture` |
| `:data` | `repository-pattern` + whichever of `network-layer`, `sqldelight-setup`, `datastore` applies |
| `:presenter` | `presenter-module`, `mvi` |
| `:ui` | `mvi`, `design-system`, `roborazzi` |
| `build-logic/` | `feature-scaffold` |
| `.github/` | `ci-github-actions` |

---

## Step 3 — Run the architecture audit

```bash
python3 skills/kotlin-multiplatform-audit/scripts/audit_project.py .
```

Any finding is an automatic blocker.

---

## Step 4 — Review

Load `agents/reviewer.md` and check all changed files for:

- Layer boundary violations (imports crossing the dependency rule)
- Koin wiring gaps (unbound interfaces, unregistered ViewModels)
- MVI contract violations (`_state.value.copy` race, `SharedFlow` for effects)
- Missing `testTag` on interactive or assertable composables
- Stale Roborazzi goldens (if `:ui` composables changed shape)

---

## Step 5 — Output

```
CHANGED: <N> files across <layers>
SKILLS:  <list loaded>
AUDIT:   PASS | FAIL (<N> findings)

BLOCKERS: <count>
WARNINGS: <count>
VERDICT:  APPROVE | NEEDS_FIXES

<full reviewer output>
```

If `NEEDS_FIXES`, list exact required changes. Do not apply automatically — present to user first.
