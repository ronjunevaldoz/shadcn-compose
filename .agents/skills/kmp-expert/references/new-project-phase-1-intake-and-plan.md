# Phase 1 — Intake, inference, and plan (Steps 1-3)

Part of `kmp-expert` — a phase of the `/kmp-new-project` pipeline.
Run first. Ends at a gated plan the user confirms and that is persisted to `PLAN.md`.

Load this file when the command reaches this phase; do not load all phases up front. The command itself holds the phase index and the gates between them.

---

## Step 1 — Read the description and collect the intake

If `$ARGUMENTS` is empty, ask first:
```
What does your app do? (describe in one sentence or a few words)
```
Wait for the answer and use it as the description before continuing.

If `$ARGUMENTS` ends in `.md` and the file exists, read it as the full project spec.
Otherwise treat `$ARGUMENTS` as the raw description.

Print:
```
PROJECT: <description summary, one line>
TARGET:  <current working directory>
```

If the working directory is not empty (contains files other than `.git`, `.claude`, `README.md`),
print a warning:
```
WARNING: directory is not empty — scaffolding will add files alongside existing content.
         If you want a clean project, run this from an empty directory.
```
Do not stop — continue regardless.

Then collect the project intake. Ask for the minimum information needed to start a
consumer project cleanly:

The intake is intentionally short and modal-friendly. If a UI renders it as popup
questions, ask one field at a time in this order and keep the same defaults.

| Field | Ask | Default if omitted |
|---|---|---|
| `PROJECT_TYPE` | Is this an app, or a library other projects will depend on? | Inferred from the description (`library`/`SDK`/`package`/"other projects can use this" → Library; otherwise App) |
| `PROJECT_NAME` | What is the app/project name? | Derived from the description |
| `GROUP_ID` | What package/group ID should the project use? | `com.example.<project>` |
| `APP_TYPE` | What kind of app is this? (App only) | Derived from the description |
| `WHAT_IT_DOES` | What does the app/library do in one sentence? | Derived from the description |
| `PLATFORMS` | Which targets should we scaffold? | Android + iOS |
| `MIN_SDK` | Minimum Android SDK version? | 26 |
| `IOS_TARGET` | Minimum iOS deployment target? | 16.0 |
| `PERSISTENCE` | Does it need local storage, settings, or neither? (App only) | Inferred |
| `BACKEND` | Does it talk to an API, auth service, or server? (App only) | none |
| `AUTH` | Does it have login / sign-in / identity? (App only) | none |
| `DI_APPROACH` | Annotated or manual DI? | annotated |
| `DISTRIBUTION` | Where will the app be distributed? (App only) | Play Store + App Store |
| `PUBLISH_TARGET` | Maven Central, GitHub Packages, or both? (Library only) | Maven Central |
| `CI_CD` | Wire GitHub Actions now, or skip and rely on running scripts locally for now? | yes |

Distribution options: `Play Store + App Store` · `Internal / enterprise` · `Open source / side project`
This affects signing config, ProGuard aggressiveness, and whether the CI release lane includes store upload.

**`PROJECT_TYPE = Library` changes the rest of this pipeline significantly** — no UI,
no screens, no design system. Steps 3 (wireframes), 6 (design system), and 7 (design
previews) are skipped entirely; Step 4's foundation uses
`kmp-library-publishing`'s project structure instead of the kmp-wizard
app clone; Step 8's "features" become public API surfaces instead of screens. Each
step below is marked `[App]`, `[Library]`, or unmarked (applies to both) — resolve
`PROJECT_TYPE` before Step 2 and follow only the steps that apply.

`CI_CD = no` is a legitimate choice, not a shortcut being discouraged — `./gradlew detekt`/
`ktlintCheck`/`test` and `audit_project.py` all run identically from a local terminal;
GitHub Actions automates *when* they run, it doesn't change what they check. Skipping it
doesn't skip code quality or testing (`kmp-code-quality`/`unit-testing`
stay in the always-included list below) — it only skips the automation wrapper around
running them.

If the user omits a field, state the assumption before proceeding and keep moving.

---

## Step 2 — Infer feature set and load expert routing

Pass the description and intake answers to `kmp-expert` to identify which skills are needed.

From the description, extract:
- **App type** (todo, social feed, e-commerce, etc.)
- **Platforms** — default: Android + iOS if not stated
- **Features** — derive from the app type; list each as a ticket-sized unit
- **Data layer** — default: SQLDelight (offline-first) if persistence is implied; DataStore if settings/preferences only
- **Backend** — default: none (local-only) unless the description mentions API, server, sync, or auth
- **Auth** — default: none unless mentioned

⛔ **No code is written during Steps 1–3.** Planning and implementation are strictly
separated. The first line of code is written only after the user confirms the plan in Step 3c.

Print the raw feature list:

```
## Inferred features (planning only — no code yet)

Platforms: <platforms from intake>
Features (raw):
  F-01  Project scaffold
  F-02  Clean architecture
  F-03  <feature name>
  ...
Data:    SQLDelight (offline-first)
Backend: none (local-only)
Auth:    none

Proceeding to planning phase (Steps 3-6a). Implementation starts at Step 4 after approval.
```

---

## Step 3 — Plan: MVP, delivery slices, tasks

This is the gate before any code is written. Always produce one **compact drafted plan**
with recommendations pre-selected — never present a blank template or split the plan
into competing roadmap/task/sprint systems. The user should only need to say
"looks good" or tweak one item.

### 3a — Draft the MVP scope (always recommend first)

Apply these rules to auto-select the MVP cut, then label each choice as recommended:

- Every MVP needs: navigation shell + at least one data-bearing screen
- Auth is MVP only if the app cannot work anonymously (otherwise post-MVP)
- Nice-to-haves (settings, profile, onboarding, notifications) → post-MVP by default
- No analytics, crash reporting, or push notifications in MVP unless explicitly stated

Print a plain numbered list so items can be referenced by number:

```
## Draft: MVP scope

MVP (Sprint 1-N):
  1. <feature> — <why it's core> (recommended)
  2. <feature> — <why it's core> (recommended)
  3. <feature> — included because auth is required to function

Post-MVP (after first release):
  4. <feature> — nice-to-have, not blocking
  5. <feature> — can ship without it
```

### 3b — Draft the delivery slices (roadmap + tasks together)

Always generate concrete slice names, not placeholders. Use the app type to name them
meaningfully (e.g. "Alpha — browse products" not "Milestone 1").

```
## Draft: delivery plan

Slice 1 — Foundation (~1 week)
  Outcome: clean build, CI green, design system renders
  Tasks: F-01 F-02 F-03 F-04 F-05 F-06 F-07

Slice 2 — <First MVP feature> (~1 week)
  Outcome: <feature> works end-to-end
  Tasks: A-01 A-02 A-03 A-04 A-05 A-06 A-07 A-08 A-09 A-10

Slice 3 — <Second MVP feature> (~1 week)
  Outcome: <feature> works end-to-end
  Tasks: B-01 ... B-10

Slice N — Polish + QA (~1 week)
  Outcome: ready for internal alpha release
  Tasks: layout wireframes reviewed, accessibility pass, Roborazzi goldens, release build validation

Estimated MVP: <N> sprints (~<N> weeks)
```

### 3c — Confirm the plan

Print 3a and 3b's drafts together, then use `AskUserQuestion` (not a printed prompt
waiting for free text) to gate the decision. Offer these options:

- **Looks good** — accept the plan as drafted, proceed to persisting it (3d)
- **Move a task to a different slice** — ask which task and which slice
- **Add or remove a feature from MVP** — ask which one
- **Split a slice** — ask which slice and how

**Do not proceed to Step 3d until the user confirms.** After any change: re-print only
the affected section with the change highlighted, then ask again via `AskUserQuestion`.
Never re-print the entire plan after a minor edit — just the delta.

### 3d — Persist the plan to `PLAN.md`

Immediately after confirmation — before any code is written — write `PLAN.md` at the
project root. This is the actual, durable record of the plan; the printed draft in 3a/3b
is not sufficient on its own; a session that stops after this point must not lose the
plan. Mirror this format (checkbox per task, one section per slice):

```markdown
# <PROJECT_NAME> — Development Plan

<WHAT_IT_DOES>

## Status key

| Symbol | Meaning |
|---|---|
| [ ] | Not started |
| [x] | Done |

## MVP scope

- [ ] <feature 1> — <why it's core>
- [ ] <feature 2> — <why it's core>

## Post-MVP

- <feature> — nice-to-have, not blocking

## Delivery plan

### Slice 1 — Foundation (~1 week)
Outcome: clean build, CI green, design system renders
- [ ] F-01 <task>
- [ ] F-02 <task>

### Slice 2 — <First MVP feature> (~1 week)
Outcome: <feature> works end-to-end
- [ ] A-01 <task>
- [ ] A-02 <task>

### Slice N — Polish + QA (~1 week)
Outcome: ready for internal alpha release
- [ ] Layout wireframes reviewed
- [ ] Accessibility pass
- [ ] Roborazzi goldens
- [ ] Release build validation
```

Step 8c checks off each sprint's tasks in this file as they complete — `PLAN.md` is
the live source of truth for what's done, not the chat transcript.

---

