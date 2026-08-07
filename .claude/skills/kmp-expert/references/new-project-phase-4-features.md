# Phase 4 — Features, sprint by sprint (Step 8)

Part of `kmp-expert` — a phase of the `/kmp-new-project` pipeline.
Gated per sprint. Loop this phase until the plan's sprints are done.

Load this file when the command reaches this phase; do not load all phases up front. The command itself holds the phase index and the gates between them.

---

## Step 8 — Features (sprint by sprint, gated)

Execute the approved sprint plan one sprint at a time. **Never start the next sprint
until the user reviews and confirms the current one.**

### For each sprint:

**8a — Announce the sprint**

Print before writing any code for that sprint:

```
## Sprint <N> — <Sprint name>

Tasks: X-01 X-02 ... X-N
Goal:  <sprint goal from approved plan>
```

Then use `AskUserQuestion` — "Starting Sprint <N>, this will generate code. Proceed?"
— options: proceed / adjust tasks first. Wait for confirmation before writing any code.

**8b — Implement the sprint tasks in order**

### [App] For each task in the sprint:

1. **Implement** — load the relevant skill(s), generate all 6 layers:
   - `:model` — data classes, sealed results
   - `:api` — repository interface
   - `:domain` — use cases
   - `:data` — repository impl, mappers, SQLDelight/network calls. If the real backend
     isn't ready yet for this task, generate an `InMemory<Feature>Repository` instead
     (see `kmp-repository-pattern`'s "In-memory repository (no backend
     yet)" section) — same interface, swapped in behind one Koin binding, so the app
     runs and demos end to end without blocking on the API. Never name it `Mock*`/`Fake*`
     — those names mean test-only-safe-to-delete, and this one runs the real app.
   - `:presenter` — MVI ViewModel, UiState, UiEffect, Channel
   - `:ui` — `Screen` (wired ViewModel) + `Content` (pure, previewable)
2. **Wire DI** — add bindings to the feature's Koin module.
3. **Add navigation** — add a type-safe route to NavHost. Load
   `kmp-navigation` on first screen, reuse pattern for subsequent ones.
4. **Write tests** — for every feature:
   - `:presenter` unit tests with `runTest` + Turbine
   - Roborazzi screenshot tests — all state variants, light + dark
5. **Validate** — after each task:
   ```bash
   python3 skills/kmp-audit/scripts/audit_project.py .
   ```
   Fix any findings before moving to the next task.

### [Library] For each task in the sprint:

A library's "sprint tasks" are public API surfaces, not screens — there is no
`:presenter`/`:ui`/MVI/navigation. Each task is a class, interface, or function set the
library exposes, plus its tests and docs:

1. **Implement the public API** — a `:library` module, or a sub-package if `:library` is
   still small enough to stay one module (see `kmp-clean-architecture`'s
   6-layer contract, which `library-publishing` cross-references, once it outgrows that):
   - Design the API surface first — public interface/function signatures, before the
     implementation — since `explicitApi()` (wired in Step 4) makes every visibility
     choice deliberate and `apiCheck` (Step 5) will flag any change to it later
   - Write the KDoc for every public declaration alongside the code, not after — an
     undocumented public API is exactly what `_detect_undocumented_public_api` flags
   - Keep the library's own classes free of Koin/other framework imports — see
     `library-publishing`'s "No forced framework coupling in library internals"
2. **Write tests** — `library/src/commonTest` unit tests for the public contract; add
   platform-specific tests under `androidTest`/`iosTest` only for platform-specific
   (`expect`/`actual`) behavior.
3. **Update the API dump** — after any public API change:
   ```bash
   ./gradlew apiDump   # regenerate library/api/library.api
   git add library/api/
   ```
4. **Validate** — after each task:
   ```bash
   ./gradlew apiCheck   # confirms the dump matches, and was regenerated deliberately
   python3 skills/kmp-audit/scripts/audit_project.py .
   ```
   Fix any findings before moving to the next task.

**8c — Sprint review gate**

After all tasks in the sprint are done: check off this sprint's tasks in `PLAN.md`
(`- [ ]` → `- [x]`) — this is what makes `PLAN.md` the live source of truth instead of
a snapshot from Step 3d that immediately goes stale. Then commit both the sprint work
and the updated `PLAN.md`:

```bash
git add -A
git commit -m "feat(<sprint-name>): complete Sprint <N> — <sprint goal>"
```

Then print a summary — the third field on the `Audit:` line is `Screenshots: <N>
recorded` for `[App]` or `apiCheck: PASS` for `[Library]`; print only the one matching
`PROJECT_TYPE`, resolved to plain text, never a raw conditional marker:

```
## Sprint <N> complete

Done:
  [x] X-01 <task name>
  [x] X-02 <task name>

Audit: PASS | Tests: <N> passed | Screenshots: <N> recorded

Next up - Sprint <N+1>: <sprint name>
  Tasks: Y-01 Y-02 ...
```

Then use `AskUserQuestion` — options: continue to Sprint `<N+1>` / redo a specific task
/ add a task to this sprint before moving on / stop here (resume later with
`/kmp-implement-feature`). **Do not start the next sprint until the user responds.**

**[App]** Skills to load per common feature type:

| Feature type | Skills |
|---|---|
| List + detail | `repository-pattern`, `mvi`, `paging` (if list is large) |
| Create / edit form | `mvi`, `form-validation` |
| Settings / preferences | `datastore`, `mvi` |
| Auth / login | `ktor-auth-service`, `mvi`, `form-validation`, `biometric-auth` (if mentioned) |
| Offline list | `sqldelight-setup`, `offline-first`, `mvi` |

**[Library]** Skills to load per common API surface type:

| API surface type | Skills |
|---|---|
| Any public class/function | `clean-architecture` (once `:library` outgrows one module), `unit-testing` |
| Platform-specific behavior | `expect-actual` — common-first rule, interface injection before `expect`/`actual` |
| Native/JNI bridge | `jni-pro` |
| Kotlin/Native cinterop | `expect-actual` |
| Multi-artifact split | `library-publishing`'s BOM step (Step 4) |

---

