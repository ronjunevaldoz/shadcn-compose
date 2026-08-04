# /kmp-clean-comments

Refactors code documentation across all architectural levels — classes, functions,
extension functions, and inline blocks — per `kmp-code-quality`'s
Comment & KDoc Conventions ("By architectural level" section). Fixes file-by-file with
per-file confirmation, the same pattern as `/fix-design`.

**What it fixes:**
- `class_trivial_kdoc` — a class/interface KDoc that restates the name instead of stating responsibility/architectural role
- `function_missing_kdoc` — a complex public function/method with no KDoc (`@param`/`@return`/`@throws`)
- `function_mechanics_kdoc` — a KDoc that narrates line-by-line mechanics instead of inputs/outputs/edge cases
- `extension_missing_receiver_scope` — an extension function's KDoc that doesn't state the receiver scope or calling context (`@receiver`)
- `what_comment_in_control_flow` — a `//` comment inside a loop/conditional that narrates WHAT the block does

**What it never touches:**
- KDoc on private members — flagged for removal (rename the member instead), never added
- A `//` that already explains a non-obvious workaround or business-logic WHY
- Public API KDoc that's already correct — don't rewrite something that isn't wrong

---

## Step 1 — Locate the scope

If the user ran `/clean-comments` without an argument, ask:

```
What should I clean up?
  [1] A specific file or directory path
  [2] The current git diff (staged + unstaged)
  [3] The whole project (path required)
```

Set `SCOPE_PATH` and `SCOPE_MODE` (`file`, `diff`, or `project`) from the answer.
Set `SKILLS_ROOT` to the directory containing this skills collection (parent of `commands/`).

---

## Step 2 — Run the scanner

For `project` scope, run the audit detector first to get verifiable evidence before
reading any file in full:

```bash
python3 "$SKILLS_ROOT/skills/kmp-audit/scripts/audit_project.py" "$SCOPE_PATH" --json
```

Filter results to `what-comment in control flow` findings — this is the only one of the
four categories with an automated detector (regex heuristic, not AST; expect some false
negatives and rare false positives). The other three categories (class/function/extension
KDoc) have no static detector — read the file(s) in scope directly and apply the rules
below by hand.

For `diff` scope, get the changed `.kt` files via `git diff --name-only` (staged +
unstaged) and read each one instead of running the project-wide scanner.

If nothing is found and the manual read of the file(s) in scope shows no violations:
```
✅ No comment/documentation issues found. Nothing to clean.
```
Stop here.

**Print a summary before starting fixes:**
```
Found N issues across M files:
  ⚠️  what_comment_in_control_flow      X  (extract a named function/variable)
  ⚠️  function_missing_kdoc             X  (add KDoc: inputs/outputs/edge cases)
  ⚠️  extension_missing_receiver_scope  X  (add @receiver: scope/precondition)
  ⚠️  class_trivial_kdoc                X  (state responsibility, not the name)

Processing files one at a time. You'll confirm each before I apply changes.
```

---

## Step 3 — Fix each file

For each file with issues:

### 3a. Show the issues

```
── core/pricing/src/commonMain/kotlin/PriceCalculator.kt  (2 issues) ──
  ⚠️ L 12  [function_missing_kdoc]            calculateDiscount(...)
  ⚠️ L 34  [what_comment_in_control_flow]     "// Loop through items and apply discount"
```

### 3b. Read the file and apply the rule for each level

| Level | Rule | Fix |
|---|---|---|
| Class/interface | KDoc states responsibility + architectural role only | Rewrite the opening sentence to say what it owns and why it exists as a separate type — delete restatements of the class name |
| Function/method | Complex public members get `@param`/`@return`/`@throws` for inputs, outputs, edge cases — never mechanics | Add or rewrite KDoc; trivial one-liners (getters, pure delegates) get one sentence, not a full tag breakdown |
| Extension function | `@receiver` states the scope/precondition the receiver must satisfy | Add `@receiver` describing *when* this extension applies, not just what it returns |
| Inline block | No `//` narrating WHAT a loop/conditional does | Extract a named function or variable so the code reads as its own explanation; if the comment is actually a WHY (workaround, business rule), leave it and mark the finding as intentional |

**Never invent behavior while refactoring an inline block.** Extracting a function must
be a pure rename/lift of the existing block — same logic, same order of operations, only
a name added. If the block is too tangled to name cleanly, that's a sign it needs a real
refactor beyond this command's scope — flag it and move on rather than guessing.

### 3c. Confirm before writing

Show a unified diff of the proposed changes:

```diff
- // Loop through items and apply discount
- for (item in items) {
-     val price = item.price * (1 - item.discountRate)
-     total += price
- }
+ for (item in items) {
+     total += discountedPrice(item)
+ }
+
+ private fun discountedPrice(item: LineItem): Double =
+     item.price * (1 - item.discountRate)
```

Ask:
```
Apply these changes to PriceCalculator.kt? [yes / skip / show full file]
```

- **yes** → write the changes
- **skip** → move to next file
- **show full file** → print the full proposed file, then ask again

---

## Step 4 — Verify

After all files in scope are processed:

```bash
./gradlew ktlintCheck detekt
```

`UndocumentedPublicClass`/`UndocumentedPublicFunction` confirm the added KDoc satisfies
the CI gate; `DocumentationOverPrivateFunction`/`DocumentationOverPrivateProperty` confirm
no KDoc was added to a private member. If Detekt is not wired into the project, skip this
step and note: "Detekt not detected — lint verification skipped."

---

## Step 5 — Summary

```
/clean-comments complete

  Files fixed:    X
  Files skipped:  X
  Lint verified:  ✅ / skipped

Next steps:
  1. Review the diffs above — extracted functions need sensible names, not just "helper1"
  2. Run ./gradlew ktlintCheck detekt  (if not run automatically above)
  3. Commit: git add -p  (stage only the comment cleanup)
  4. Run /kmp-run-audit to confirm what-comment findings are gone
```

---

## Rules

- **Never** bulk-apply all fixes without per-file confirmation.
- **Never** add KDoc to a private member — flag it for a rename instead (Detekt's
  `DocumentationOverPrivateFunction`/`DocumentationOverPrivateProperty` catch this).
- **Never** delete a `//` comment without checking it isn't a WHY — a workaround note,
  a business-logic constraint, or a non-obvious "why would breaking this seem safe"
  explanation must stay, per `kmp-code-quality`'s "Two real mistakes"
  section.
- **Always** re-run lint after applying fixes, when Detekt is available.
- When extracting a function from a WHAT-comment block, name it for what the extracted
  code *is*, not a generic `helper`/`process` — the whole point is that the name replaces
  the comment.
