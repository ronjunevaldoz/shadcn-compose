# Rule: Stable Features Are Hardened — No Casual Changes

## What "stable" means

A feature is stable when it is tagged with one of these markers in source or docs:

- Comment in code: `// STABLE: <feature-name>`
- Entry in `docs/engine_rules.md` under a "Stable" section
- Explicit statement from the user in the current session ("this is stable", "mark this stable")

When a feature is marked stable, **every change attempt is subject to the full gate below**.
No exceptions for "small fixes", "just a refactor", or "quick cleanup".

## The gate — all conditions must pass before touching stable code

### 1. Verified facts only

Do not make any claim about stable code behaviour from memory or inference. Before
proposing or making a change:

- Read the actual current source file (not a summary, not a prior session note).
- Read the test that covers the behaviour you are about to change.
- If no test exists for the specific behaviour: **stop, write the test first, confirm it
  passes, then proceed**.

If you cannot verify a fact from source or a passing test, state it as a hypothesis and
ask before acting on it.

### 2. Full unit test coverage before the change lands

Every stable feature must have a unit test that:

- Exercises the exact code path being modified.
- Asserts the specific output or side-effect the stable behaviour guarantees.
- Passes on the current code **before** your change (proves the test is not vacuous).
- Passes after your change (proves you didn't break it).

If the existing tests do not cover the path you are touching, add them first in a
separate commit. Do not bundle test additions with the behaviour change.

### 3. No scope creep

The change must be the **minimum diff** that achieves the stated goal. Do not:

- Rename unrelated symbols while fixing a bug.
- Refactor surrounding code "while you're in there".
- Add new capabilities to a stable module in the same PR as a fix.

If you notice something worth improving outside the stated scope, file a separate task or
note it to the user — do not include it in the current change.

### 4. Regression check

After making the change, explicitly run the test suite for the affected module:

```bash
./gradlew :core:llama:jvmTest        # for llama/TTS stable features
./gradlew :core:stable-diffusion:jvmTest  # for SD stable features
```

Report the result. Do not summarise as "tests should pass" — run them and report actual
output. If tests cannot be run (e.g. missing model files), state that explicitly and
list which integration tests were skipped and why.

### 5. Audit trail

Add a short comment at the change site:

```cpp
// Changed <date>: <one-line reason>. Stable behaviour preserved: <what still holds>.
```

For Kotlin, use a KDoc tag:
```kotlin
// Changed <date>: <one-line reason>. Stable: <invariant>.
```

---

## Why this rule exists

Stable features have been validated against real model outputs and confirmed correct.
A "safe-looking" change to stable code has repeatedly caused silent regressions:

- **Confirmed incident**: The OuteTTS ISTFT was refactored to clean up variable names.
  The refactor silently changed the Hann window denominator from `n` to `n-1` (symmetric
  vs periodic). No test caught it. Result: OLA ripple artifact on every render, discovered
  weeks later via audio analysis. Full analysis in `docs/audit_tts_both_engines.md`.

The cost of the gate (writing a test first, running the suite) is minutes.
The cost of a silent regression in a native audio or image pipeline is hours of
bisection across model outputs that are hard to diff programmatically.

---

## Applies To

- Any file with a `// STABLE:` comment
- Any engine listed as stable in `docs/engine_rules.md`
- Any JNI wrapper after its integration test has passed at least once with real model files
- `llama-wrapper.cpp` TTS pipeline (OuteTTS + WavTokenizer) — stable as of `e84b86d`

---

## Checklist (run through this before every change to stable code)

- [ ] Read the current source file — not a summary.
- [ ] Identified the exact test covering this behaviour. If none: wrote it first.
- [ ] Test passes on current code before change.
- [ ] Change is minimum diff — no scope creep.
- [ ] Test passes after change.
- [ ] `./gradlew :<module>:jvmTest` run and output reported.
- [ ] Audit comment added at change site.
