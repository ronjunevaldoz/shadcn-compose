# /kmm-audit-adaptive $ARGUMENTS

**KMM Agent Skills** — check every screen for adaptive layout coverage gaps and
redundant title violations across Compact (phone), Medium (tablet), and Expanded (desktop).

Target project: `$ARGUMENTS` (defaults to `.` if empty)

---

## Step 1 — Run the audit

```bash
python3 ~/.claude/skills/kotlin-multiplatform-audit/scripts/audit_project.py "${ARGUMENTS:-.}"
```

Filter the output for these two finding types and collect them:
- `redundant screen title [MEDIUM]` — title shown in topBar AND duplicated in body
- `adaptive coverage [LOW]` — Screen composable missing `windowSizeClass` param

Also collect any `hardcoded spacing` findings — those often accompany layout inconsistencies.

---

## Step 2 — Redundant title report

For each `redundant screen title` finding, read the flagged file and show:

```
─────────────────────────────────────────────────────
REDUNDANT TITLE: <file>
  topBar:   AppTopAppBar(title = "<extracted title text or '?'>")
  in-body:  AppText("<same text>", style = AppTextStyle.H1)  ← REMOVE THIS

  Fix: delete the in-body heading. The topBar already provides the page title.
  If you need a section label below the topBar, use AppTextStyle.H3 or smaller
  and rename the variable to reflect it's a *section* title, not the page title.
─────────────────────────────────────────────────────
```

If zero findings: print `✅ No redundant titles found.`

---

## Step 3 — Adaptive coverage: Screen params

For each `adaptive coverage` finding (missing `windowSizeClass` param):

```
─────────────────────────────────────────────────────
MISSING ADAPTIVE PARAM: <Screen.kt file>
  The screen has no windowSizeClass: WindowSizeClass param.
  Project uses WindowSizeClass elsewhere — this screen is not breakpoint-aware.

  Fix:
    @Composable
    fun FooScreen(
        windowSizeClass: WindowSizeClass,   // ← add this
        onBack: () -> Unit,
        viewModel: FooViewModel = koinViewModel(),
    ) {
        // pass down to FooContent
    }

    @Composable
    fun FooContent(
        state: FooContract.State,
        windowSizeClass: WindowSizeClass,   // ← add this
        onIntent: (FooContract.Intent) -> Unit,
    ) {
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        // branch layout:
        if (isExpanded) FooTwoPaneLayout(state, onIntent)
        else FooSinglePaneLayout(state, onIntent)
    }
─────────────────────────────────────────────────────
```

If zero findings: print `✅ All screens have windowSizeClass param.`

---

## Step 4 — Adaptive coverage: Roborazzi breakpoint tests

For each Screen that *does* have `windowSizeClass`, find its Roborazzi test file:

```
find . -name "*<ScreenName>*Test*.kt" -o -name "*<ScreenName>*Robo*.kt"
```

Open the test file and check whether it captures at all three breakpoints:

| Breakpoint | Signal to look for |
|---|---|
| Compact (phone) | `Compact` in size param, `width = 360.dp` / `width = 411.dp`, or `WindowWidthSizeClass.Compact` |
| Medium (tablet) | `Medium` in size param, `width = 600.dp` / `width = 840.dp`, or `WindowWidthSizeClass.Medium` |
| Expanded (desktop) | `Expanded` in size param, `width = 1280.dp` or `WindowWidthSizeClass.Expanded` |

For each screen missing one or more breakpoints:

```
─────────────────────────────────────────────────────
MISSING ROBORAZZI BREAKPOINT: <ScreenName>
  Test file: <path or "not found">
  Missing:   Compact | Medium | Expanded  (cross out what's present)

  Fix — add to <ScreenName>Test.kt:
    @Test
    fun fooScreen_compact() = runScreenshotTest(
        windowSizeClass = WindowSizeClass.compute(360f, 800f, Density(1f)),
    ) { FooContent(state = FooContract.State(), windowSizeClass = it, onIntent = {}) }

    @Test
    fun fooScreen_medium() = runScreenshotTest(
        windowSizeClass = WindowSizeClass.compute(700f, 1024f, Density(1f)),
    ) { FooContent(state = FooContract.State(), windowSizeClass = it, onIntent = {}) }

    @Test
    fun fooScreen_expanded() = runScreenshotTest(
        windowSizeClass = WindowSizeClass.compute(1280f, 800f, Density(1f)),
    ) { FooContent(state = FooContract.State(), windowSizeClass = it, onIntent = {}) }
─────────────────────────────────────────────────────
```

Also flag screens with NO test file at all:
```
❌ NO TEST FILE: <Screen> — create <ScreenName>Test.kt in jvmTest/ with at least 3 breakpoint captures
```

---

## Step 5 — Summary

```
═══════════════════════════════════════════════════════
ADAPTIVE AUDIT COMPLETE
  Redundant titles:           <N>
  Missing windowSizeClass:    <N> screens
  Missing Roborazzi breakpoints:
    Compact:   <N> screens
    Medium:    <N> screens
    Expanded:  <N> screens
  No test file at all:        <N> screens

PRIORITY FIXES:
  1. Remove redundant in-body titles (visual duplication — instant polish win)
  2. Add windowSizeClass param to unaware screens
  3. Add missing Roborazzi captures so CI enforces layout at every size
═══════════════════════════════════════════════════════
```

---

## Notes

- Run this from inside the consumer project root, not from kmm-agent-skills
- "Compact" ≈ phone portrait; "Medium" ≈ tablet portrait / phone landscape;
  "Expanded" ≈ tablet landscape / desktop window
- Redundant title detection requires a `topBar = { AppTopAppBar(...) }` scaffold AND
  a heading-style `AppText`/`Text` with style H1/H2/Heading/HeadlineLarge/TitleLarge/DisplaySmall
  in the same file — false positives are rare but possible for intentionally dual-titled designs
- If the project has no `WindowSizeClass` usage anywhere, Step 3 and 4 are skipped (not applicable)
