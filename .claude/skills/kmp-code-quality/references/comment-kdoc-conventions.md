# Comment & KDoc Conventions

Part of `kmp-code-quality`. Load this file when working on: comment & kdoc conventions.

---

Kotlin-specific — for the C++/CPP side of a JNI or Kotlin/Native bridge (header
declaration vs `.cpp`/`.mm` implementation comments), see `kmp-native-authoring`'s
"Header vs implementation comments" section. Same underlying principle (declaration =
what a caller needs, implementation = what a maintainer needs), different syntax.

### Whether to write a comment at all

Ask in this order — stop at the first "yes":

1. **Does removing it lose zero information?** (the code/naming already says it) — don't
   write it. This is the single most common comment mistake: narrating WHAT instead of
   explaining WHY.
2. **Is it a public API contract another module or consumer relies on** (parameters,
   return value, thrown errors, a receiver precondition)? — KDoc `/** */`.
3. **Is it a non-obvious WHY** — a workaround, a constraint from outside this file, a
   reason simplifying this would break something? — single-line `//`.
4. Otherwise — don't write it. A comment that answers neither "what's the contract"
   nor "why is this not the obvious way" isn't pulling its weight.

**A single-line addition (one dependency declaration, one config value, one import)
almost never earns a comment**, even when the reasoning behind it is real and non-obvious.
Put that reasoning in the commit message — it's discoverable via `git blame` exactly when
someone needs it (touching that line), and doesn't sit in the file forever for every
reader who doesn't. The one exception: a gotcha a future maintainer will independently
re-trip on regardless of how they got there (a platform quirk, a version pin that looks
removable but isn't) — that earns a single terse line, not a paragraph.

**Watch for a WHY-shaped comment that's actually a leftover justification trail.** A
genuine WHY comment states a fact a future reader needs and stops — it doesn't defend the
choice. Tell: does it argue for the decision ("despite the name", "isn't just X", "to be
clear") the way you'd explain it to a reviewer mid-PR, rather than simply stating the
constraint to someone who already trusts the line works? That defensive tone is the
signal it's process narration that survived into the file, not documentation the codebase
needs. Cut it to the one sentence a maintainer actually needs, or drop it into the commit
message instead. `kmp-audit`'s `_detect_justification_comment_above_single_statement`
catches the mechanical shape of this (a 3+ line comment directly above one dependency
line in a Gradle build file) — but the tone test above is what to apply by hand, since it
generalizes past Gradle files.

Two comment types, two jobs — never mix them:

| | Single-line `//` | Multi-line `/** ... */` (KDoc) |
|---|---|---|
| Documents | Internal WHY — a workaround, a non-obvious constraint | Public API contract — `@param`/`@return`/`@throws`/`@sample` |
| Never used for | Restating WHAT the code does (good naming covers that) | Private members — rename instead (Detekt's `DocumentationOverPrivateFunction`/`Property` flags this) |
| Visible to | Nobody outside the source file | Dokka + IDE quick-docs |
| Grows past ~4 lines? | Split: keep the one-sentence WHY inline, move the rest to `docs/reference/` with a pointer comment (see below) — mechanically checked by `kmp-audit`'s `_detect_long_stacked_comment_block` (5+ consecutive `//` lines, no `docs/reference/` pointer, not a leading license header, not a block reading as genuine WHY — see that detector's own docs for the signal-word heuristic) | N/A — KDoc doesn't accumulate this way; if a class needs paragraphs, that's what `docs/reference/` is for too |
| Nests? | N/A | KDoc does **not** nest. Plain block comments (`/* */`) do, unlike Java/C |

### Formatting

Verified against Kotlin's own official coding conventions (kotlinlang.org), not invented:

- **`//`**: exactly one space after the slashes — `// like this`, not `//like this`.
- **KDoc, short**: a single line is fine when the whole comment fits — `/** This is a short documentation comment. */`. Don't force a one-sentence KDoc onto three lines for symmetry.
- **KDoc, long**: opening `/**` alone on its own line, every following line starts with a single space then `*`, closing `*/` alone on its own line:
  ```kotlin
  /**
   * This is a documentation comment
   * on multiple lines.
   */
  ```
- **`@param`/`@return`**: the official guidance is to *avoid* these tags generally —
  weave the parameter/return description into the main text instead, with `[paramName]`
  links wherever it's mentioned, and use `@param`/`@return` only when the description is
  long enough that it doesn't fit the flow of the prose. This repo's own tag table below
  lists them as available tags, not as the default shape every KDoc should take.
- **Coverage is all-or-nothing, never partial.** The choice above is about *form*
  (inline `[name]` vs an `@param` tag), never about which parameters get addressed at
  all. If a function has 3 parameters and the KDoc documents 1 of them, that's a real
  defect — either say something about all 3 (mixing inline mentions and `@param` tags on
  the same declaration is fine) or write a plain single-line summary with no parameter
  detail at all. A KDoc block that looks thorough but silently skips 2 of 3 parameters is
  worse than no KDoc — it reads as complete and isn't. `kmp-audit`'s
  `_detect_partial_param_documentation` catches this mechanically.
- **KDoc supports Markdown** — verified against kotlinlang.org, not assumed. Inline markup
  inside `/** */` is regular Markdown (bold/italic, lists, links), plus a KDoc-specific
  shorthand for linking to another declaration:
  ```kotlin
  /**
   * Wraps [HttpClient] with retry logic. Use [retryPipeline] instead of calling this
   * directly — see [this][GROUP_ID.core.network.NetworkResult] for the result shape.
   *
   * - Retries transient failures up to `times`
   * - Never retries a 4xx response
   */
  ```
  `[declaration]` resolves the same way a reference inside the documented element would —
  no full qualification needed if it's already imported in the file. A fenced code block
  (` ``` `) works too, for a short usage snippet that doesn't warrant a full `@sample`.

### By architectural level

The table above sorts by comment *type*; this sorts by *where in the code* it lives —
use both together when reviewing or refactoring documentation.

| Level | Rule |
|---|---|
| Classes & interfaces | KDoc states the class's responsibility and architectural role only. Skip trivial openers ("Represents a X") — say what it owns and why it exists as a separate type, not what its name already tells you. |
| Functions & methods | KDoc only for complex public members, using the tag table below. Document inputs, outputs, and edge cases — never mechanics. `UndocumentedPublicFunction` requires *something*, so trivial one-liners (a getter, a pure delegate) get a single sentence, not a full `@param` breakdown. |
| Extension functions | State the receiver scope and calling context — *when* to reach for this extension, not just what it returns. Use `@receiver` for any precondition the receiver must satisfy (e.g. "must be called from inside an active `viewModelScope`"). This is the one KDoc case where "when to use it" outranks "what it does," because the same signature can exist as a member on an unrelated type. |
| Inline blocks (loops, conditionals) | No `//` that explains WHAT a block does — extract a named function or variable so the code reads as its own explanation. Keep `//` only for a non-obvious workaround or a business-logic WHY. |

```kotlin
/**
 * Retries [block] with exponential backoff, but only while this scope's job is active.
 * @receiver Must be a scope tied to a UI lifecycle (e.g. `viewModelScope`) — cancels
 * in-flight retries when the receiver is cancelled instead of leaking a delay loop.
 */
suspend fun <T> CoroutineScope.retryWhileActive(times: Int, block: suspend () -> T): T { ... }
```

### Two real mistakes this caught

**A `//` on the same line as code can swallow what follows it** — it runs to end-of-line,
including a needed closing `)`/`{`. Shipped in `kmp-imagevector-generator`'s
own codegen until a test caught it:

```kotlin
// ❌ WRONG — the // comments out the rest of the line, including `) {`
path(fill = SolidColor(Color.Black)  // tint at call site) {

// ✅ CORRECT — the call is syntactically complete before the comment starts
path(fill = SolidColor(Color.Black)) {  // tint at call site
```

**A `//` block that keeps growing is a sign two audiences got merged into one comment.**
Keep only the sentence that answers "why would someone break this by simplifying it?" —
move everything else (mechanism detail, rejected alternatives, exact version numbers) to
`docs/reference/` (the lane `kmp-project-docs-maintainer` already
defines for deep references), with a one-line pointer left behind:

```kotlin
// Composite build (not include()): root's apply false on org.jetbrains.compose locks
// that plugin ID to 1.11.1 build-wide. This module needs 1.12.0-beta01 for an
// experimental Compose Foundation Style API not available in the stable line.
// Full rationale: docs/reference/composite-build-style-experimental.md
includeBuild("tailwind/style-experimental")
```

### KDoc: code definition, params, samples

| Tag | Purpose |
|---|---|
| `@param` | Official guidance: avoid — describe the parameter inline in the main text with a `[name]` link instead. Reach for `@param` only when that description is too long to weave into the flow |
| `@return` | Same as `@param` — inline by default, tag only for a lengthy description. Skip entirely for `Unit` |
| `@throws` | A failure mode that's part of the contract, not every possible exception |
| `@see` | Cross-reference to a related declaration |
| `@sample` | Points at an actual, compiled function elsewhere as the usage example |
| `@property` / `@receiver` / `@constructor` | Constructor property / extension receiver / primary constructor, documented separately from the class summary |
| `@suppress` | Hides a technically-public declaration from generated docs |

**An example is warranted only when usage isn't obvious from the signature** (a builder, a
DSL) — never required per function or per file, same "why not what" rule as `//`. When one
is warranted, use `@sample`, not a pasted code block: it points at a real compiled
function, so it's type-checked and can't silently drift stale.

**When more than one tag is used on the same declaration, the required order is**
`@constructor`, `@receiver`, `@param`, `@property`, `@return`, `@throws`, `@see` — per the
[Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)'s Block
tags rule. A tag never appears with an empty description; skip it entirely instead.

```kotlin
/**
 * Builds a [Result] pipeline that retries on transient failures.
 * @sample GROUP_ID.samples.retryPipelineSample
 */
fun <T> retryPipeline(times: Int, block: suspend () -> T): Flow<T> { ... }
```

Module/package-level docs (describing a whole module, not one declaration) are a separate
Dokka mechanism — `Module.md`/`Package.md` — not a KDoc tag.

### Detekt enforcement

```yaml
comments:
  UndocumentedPublicClass:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']
  UndocumentedPublicFunction:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']
  UndocumentedPublicProperty:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']
  DocumentationOverPrivateFunction:
    active: true
  DocumentationOverPrivateProperty:
    active: true
  OutdatedDocumentation:
    active: true
```

`UndocumentedPublic*` requires KDoc on every public declaration; `DocumentationOverPrivate*`
forbids it on private ones; `OutdatedDocumentation` catches KDoc whose `@param`/signature
no longer matches the declaration after a refactor.

**`UndocumentedPublicProperty` is easy to leave out and was** — this block enabled only
the `Class` and `Function` variants while the sentence above claimed "every public
declaration", and `kmp-audit`'s `_detect_undocumented_public_api` backstop matched only
`class`/`interface`/`object`/`fun` too. A public `val` on a library's API surface was
covered by neither. Both now include properties. It matters most under `explicitApi()`,
where a public property is a permanent part of the published surface that
`binary-compatibility-validator` will track whether or not anyone documented it.

### TODO / FIXME — deferred work, not a substitute for tracking it

Verified against Google's Java Style Guide §4.8.6.2 (the real, commonly-cited source for
this format, still applicable to Kotlin — kotlinlang.org's own conventions don't cover
it) and IntelliJ/Android Studio's built-in TODO tool window, which recognizes `TODO` and
`FIXME` case-insensitively by default:

```kotlin
// TODO: github.com/org/repo/issues/456 - remove once the upstream fix ships
// FIXME: github.com/org/repo/issues/789 - retry loop can spin forever on a 5xx
```

- Always a link to a tracked issue, never a bare name — Google's guide explicitly warns
  against using an individual's name as the only context; people leave, the comment
  outlives them.
- `TODO` = planned follow-up work; `FIXME` = known-broken code shipped anyway. Informal
  distinction — tooling treats both identically, so don't rely on the word alone to
  signal severity, say it in the note.
- **This is not where "known issues" documentation belongs.** A `TODO`/`FIXME` is a
  pointer to a tracked issue, not the issue's write-up — if there's no real issue behind
  it yet, that's the actual gap (file one, or use `kmp-project-docs-maintainer`'s
  `docs/bugs/` lane for an actively-worked one), not a long comment standing in for
  tracking.
- **Real gotcha, not a nice-to-have detail:** Detekt's `ForbiddenComment` rule (Style
  ruleset) is active by default and forbids the literal strings `TODO:`/`FIXME:`/
  `STOPSHIP:` outright — see `kotlin-library-pattern-choices.md`'s own section on this.
  It does not distinguish a linked, well-formed `TODO` from a bare one; both fail. A
  project that wants the format above to actually compile needs an `allowedPatterns`
  regex on that rule exempting lines that match it (e.g. a line containing an issue-
  tracker URL) — otherwise write the reasoning some other way (commit message, or the
  `docs/bugs/` lane) since the marker itself won't pass CI.

### License headers — situational, not a default

Per-file license headers were standard in the AOSP/Apache-Software-Foundation era and are
still worth it for **libraries redistributed externally** (Detekt ships
`AbsentOrWrongFileLicense`, off by default). Skip them for app code — redundant with the
root `LICENSE` file. See `kmp-library-publishing`'s "Per-file license
headers" for the rule config and template.

---

