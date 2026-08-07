# Kotlin Library & Pattern Choices

Part of `kmp-code-quality`. Load this file when working on: kotlin library & pattern choices.

---

### `kotlin-reflect` — avoid in shared code

`kotlin-reflect` is a JVM-primary API — limited or absent on Kotlin/Native and Kotlin/JS,
and a real runtime/startup cost even on JVM. Never add it to `commonMain`'s dependencies;
if a `commonMain` file imports `kotlin.reflect.*` beyond the always-available `KClass`/
`::class` literal (full reflection: `memberProperties`, `KFunction.call`, etc.), that's a
signal the platform split was skipped, not a genuine cross-platform need.

- **Fine**: JVM-only modules (a Ktor server, a desktop-only feature) that already accept
  JVM as their sole target
- **Not fine**: reaching for reflection-based serialization or object inspection in
  shared code — use `kotlinx.serialization` instead, which code-generates via a compiler
  plugin and needs no runtime reflection on any platform
- `kmp-audit`'s `_detect_kotlin_reflect_in_common` catches full-reflection
  imports in `commonMain`

### Util/extension file organization

A single `Utils.kt`/`Helpers.kt`/`Extensions.kt` file accumulating unrelated top-level
functions across different domains (string formatting next to date math next to network
retry logic) is a real smell — the file has no single responsibility, and nothing about
its name tells a reader what's actually inside. Split by what the functions are *for*:
`StringExtensions.kt`, `DateExtensions.kt`, or move the function into the module that
owns the domain it touches. A file of extension functions all sharing the same receiver
type is fine and not what this flags — the smell is unrelated functions sharing only a
generic filename. `_detect_god_utils_file` flags a `*Utils.kt`/`*Helpers.kt` file with
10+ top-level functions spanning 3+ distinct (or no) receiver types.

### Code categorization: core / helper / sugar / sample-local / deprecated

Five real categories for classifying a function or type — applies to both app and
library code, though the stakes differ (see the library-specific mapping in
`kmp-library-publishing`'s Ongoing Maintenance section).

Mechanically classified by `kmp-audit`'s `scripts/classify_declarations.py`, which reads
the table's "Kotlin mechanism" column literally — three of the five are exactly decidable
(`@Deprecated`, sample path, visibility keyword), `sugar` is a conservative heuristic
(public + single-expression body that delegates), and `core` is the residual:

```bash
python3 skills/kmp-audit/scripts/classify_declarations.py <project_root>
python3 skills/kmp-audit/scripts/classify_declarations.py <project_root> --json --strict
```

It also flags two things the categories imply but nothing checked: a `@Deprecated` with
no `ReplaceWith` (no migration path means it's dead code, not a deprecation) and a public
declaration inside a sample module. Regex-based and single-file scope — no cross-module
resolution — so `sugar` rows carry a `confidence` field and the output is a map to
review, not a verdict.

| Category | Kotlin mechanism | Meaning |
|---|---|---|
| `core` | `public` | Primary entry point — the thing callers are meant to reach for first |
| `helper` | `internal` | Implementation detail behind `core` — no caller outside the module needs it, and the compiler enforces that once `explicitApi()` is on |
| `sugar` | `public`, calls into `core` | Convenience overload/extension layered on `core` — optional, never the *only* way to do something |
| `sample-local` | Separate module/source set, never the callable module | Demo/example code — belongs in a `samples`/`demo` module, not named or tagged its way out of the real one |
| `deprecated` | `@Deprecated` | Has a real migration path and a removal plan — distinct from `unused`/dead code (nothing marks it, nothing references it, no plan needed beyond deleting it) |

**"Utils" isn't a sixth category — it's the failure mode when this classification gets
skipped.** A generic `Utils.kt`/`Helpers.kt` grab-bag (see above) is what happens when
functions never get sorted into `core`/`helper`/`sugar` in the first place. A genuine
small utility function is `sugar` (public, general-purpose, organized by receiver type —
`StringExtensions.kt`) or `helper` (internal-only plumbing) — never its own bucket.

**Redundancy check**: a `sugar` function must call the same `core` function/constructor a
caller could reach directly — never duplicate validation or defaults logic between them.
Two independent paths to build/do the same thing is the actual redundancy risk, not
having both `core` and `sugar` in the first place (that split is normal and good).

#### Worked example

Before — everything public, and `sendWithTimeout` re-implements `send` instead of
delegating, so the backoff math exists in two places:

```kotlin
class RetryingClient(private val engine: Engine) {

    fun backoffDelay(attempt: Int): Long = 1000L * (1 shl attempt)

    fun send(req: Request): Response {
        var last: Response? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            last = engine.execute(req)
            if (last.isSuccess) return last
            sleep(backoffDelay(attempt))
        }
        return last!!
    }

    fun sendWithTimeout(req: Request, timeoutMs: Long): Response {
        var last: Response? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            last = engine.execute(req.copy(timeoutMs = timeoutMs))
            if (last.isSuccess) return last
            sleep(1000L * (1 shl attempt))   // duplicated backoff math
        }
        return last!!
    }
}

@Deprecated("no longer used")
fun legacySend(req: Request): Response = RetryingClient(defaultEngine).send(req)
```

`classify_declarations.py` reports everything as `core` — no `helper`, no `sugar` — plus
one problem:

```
  core         medium  backoffDelay
  core         medium  send
  core         medium  sendWithTimeout
  deprecated   high    legacySend

  legacySend — deprecated without ReplaceWith
```

Four public entry points where the API really has one. That flat `core`-only shape is the
signal: nothing is marked as an implementation detail, and nothing delegates.

After — one real implementation, one overload delegating to it, the retry math made
`internal`, and the deprecation given a migration path:

```kotlin
class RetryingClient(private val engine: Engine) {

    internal fun backoffDelay(attempt: Int): Long = 1000L * (1 shl attempt)

    fun send(req: Request): Response = send(req, DEFAULT_TIMEOUT_MS)

    fun send(req: Request, timeoutMs: Long): Response {
        var last: Response? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            last = engine.execute(req.copy(timeoutMs = timeoutMs))
            if (last.isSuccess) return last
            sleep(backoffDelay(attempt))
        }
        return last!!
    }
}

@Deprecated("Use RetryingClient.send()", ReplaceWith("RetryingClient(defaultEngine).send(req)"))
fun legacySend(req: Request): Response = RetryingClient(defaultEngine).send(req)
```

```
  core         medium  RetryingClient
  helper       high    backoffDelay     internal visibility
  sugar        high    send             overload delegating to another send()
  core         medium  send             public, not a delegation
  deprecated   high    legacySend       @Deprecated present
```

No problems, and the published surface shrank from four functions to two. The backoff
math now exists once, so a fix to it can't miss a caller.

### DSL (type-safe builder) — when it's warranted, when it's overengineering

Verified against kotlinlang.org's own type-safe-builders page, not assumed. Real,
documented use cases: generating hierarchical markup (HTML/XML), configuring nested
routes (Ktor). The common thread — a genuinely tree-shaped structure where the nesting
itself carries meaning.

- **Use a DSL when**: the shape is hierarchical (parent-child, not a flat parameter bag),
  and a block structure reads better than a long named-argument call.
- **Overengineering when**: ~5 or fewer flat params (a plain function call already reads
  clean), single call site with no reuse as a mini-language, or no real nesting — wrapping
  a data class in `apply {}`-shaped ceremony adds indirection without adding clarity.
- **A DSL entry point is `core`, not a separate thing** — it must ultimately call the
  same constructor/factory a caller could use directly. Exposing both a DSL and a raw
  constructor as equally-promoted API, with logic that can drift between them, is the
  redundancy this whole categorization exists to prevent.
- **Real gotcha, not style**: any DSL that nests one builder inside another needs
  `@DslMarker` on the receiver types. Without it, Kotlin lets a lambda call an *outer*
  builder's methods from inside an *inner* one — e.g. `html { head { head {} } }`
  compiles when it shouldn't. `@DslMarker` restricts each lambda to its nearest receiver
  only, per Kotlin's own documented fix for exactly this scope-leak problem.

### Delegation (`by`) — two distinct mechanisms, when each earns its keep

Verified against kotlinlang.org's own delegation and delegated-properties pages, not
assumed. "Delegation" covers two different Kotlin features — don't conflate them:

**Class delegation** — implement an interface by forwarding to a held object instead of
subclassing:

```kotlin
interface Base { fun print() }
class BaseImpl(val x: Int) : Base { override fun print() { print(x) } }
class Derived(b: Base) : Base by b   // compiler generates the forwarding methods
```

This is the mechanical tool for this file's own Composition Over Inheritance principle
(see `kmp-clean-architecture`) — zero forwarding boilerplate, no `abstract class` forcing
subclassing. **Real limitation, not obvious from the syntax**: overriding one delegated
member doesn't change how the delegate's *other* methods behave — they still call the
delegate's own internals, never your override. `by` supplements the contract at the top
level; it can't intercept how the delegate calls itself.

**Delegated properties** — `by lazy`/`Delegates.observable`/`Delegates.vetoable`/
`Delegates.notNull()`, a different feature reusing the same `by` keyword:

| Delegate | Use when |
|---|---|
| `by lazy { }` | Expensive init deferred to first access. Default `LazyThreadSafetyMode.SYNCHRONIZED` is thread-safe; `NONE` is a real perf lever, but only correct if init and every access are guaranteed same-thread |
| `Delegates.observable` | React *after* a property changes (invalidate cache, re-render) |
| `Delegates.vetoable` | Reject a value *before* assignment — a validation gate, called before the write completes |
| `Delegates.notNull()` | A `var` set later, no sane dummy default, and not `lateinit`-eligible (e.g. a primitive type) |

**Overengineering**: a custom property delegate for a get/set pattern used exactly
once — that's just a getter/setter, no delegate needed. A delegate (custom or standard)
earns its keep when the same pattern repeats across multiple properties or classes.

### Regex readability

A regex used more than once, or complex enough to need explaining, must be bound to a
well-named constant — never inlined as a raw literal inside a function call:

```kotlin
// ❌ — unreadable inline, no name to signal intent, recompiled if hit in a hot path
if (Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(input)) { ... }

// ✓ — named, compiled once, self-documenting call site
private val EMAIL_RE = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
if (EMAIL_RE.matches(input)) { ... }
```

For a pattern with 2+ capture groups, prefer named groups over positional ones — a caller
reading `match.groups["year"]` doesn't need to cross-reference the pattern to know what
`match.groupValues[2]` means. Add a one-line WHY comment above any pattern using
lookaheads/lookbehinds or non-obvious escaping — what it matches should not require
mentally executing the regex. `_detect_inline_unnamed_regex` flags a `Regex(...)`/
`toRegex()` call inlined directly as a function-call argument instead of bound to a
`val`.

### Performance killers — Detekt's Performance ruleset, plus two real gaps it doesn't cover

Verified against Detekt's own docs before writing this: enable its **Performance**
ruleset (8 real rules) in `detekt.yml` alongside the sections in Detekt Setup above —
`ArrayPrimitive`, `CouldBeSequence`, `ForEachOnRange`, `SpreadOperator`,
`UnnecessaryInitOnArray`, `UnnecessaryPartOfBinaryExpression`,
`UnnecessaryTemporaryInstantiation`, `UnnecessaryTypeCasting`. None of these cover an
object constructed inside a loop with no dependency on the loop variable — a real,
common killer Detekt's own ruleset doesn't check:

```kotlin
// ❌ — SimpleDateFormat rebuilt every iteration
for (item in items) {
    val fmt = SimpleDateFormat("yyyy-MM-dd")
    results.add(fmt.format(item))
}

// ✓ — built once, before the loop
val fmt = SimpleDateFormat("yyyy-MM-dd")
for (item in items) {
    results.add(fmt.format(item))
}
```

`kmp-audit`'s `_detect_object_creation_in_loop` flags a known-expensive
constructor (`SimpleDateFormat`, `DateTimeFormatter`, `HttpClient`, `MessageDigest`,
`Gson`, `ObjectMapper`) built inside a `for`/`while` body whose arguments don't
reference the loop variable — a legitimate per-item construction (the constructor
genuinely uses the loop variable) is not flagged.

### Public mutable collection exposure

Distinct from the Compose-only unstable-collection-param check above — this is an
encapsulation concern, not a recomposition one. A public `MutableList`/`MutableMap`/
`MutableSet` property or return type lets any caller mutate your internal state through
the reference, regardless of whether Compose is involved at all:

```kotlin
// ❌ — a caller can add/remove/clear through this reference
class ItemStore {
    val items: MutableList<Item> = mutableListOf()
}

// ✓ — read-only surface, backed by a private mutable copy
class ItemStore {
    private val _items = mutableListOf<Item>()
    val items: List<Item> get() = _items
}
```

Especially relevant on an `explicitApi()` library's public surface, where this becomes
a permanent part of the contract. `_detect_public_mutable_collection` flags a
non-`private`/non-`internal` declaration exposing a `Mutable*` type directly.

### Android Context/Activity leak in a singleton

The classic Android memory leak: a `companion object` or singleton `object` caching a
`Context`/`Activity` reference. The singleton outlives the Activity, so the reference
prevents garbage collection for the process's whole lifetime — a real leak, not a style
nit. `applicationContext` (or an `Application` type) is the one safe exception — it
already lives for the process, so caching it long-term is fine:

```kotlin
// ❌ — leaks the Activity every time a new one is created
class SessionManager {
    companion object {
        var activity: Activity? = null
    }
}

// ✓ — application context is safe to hold long-term
class SessionManager {
    companion object {
        lateinit var appContext: Context  // set once, from Application.onCreate() with applicationContext
    }
}
```

Applies equally to a KMP library's Android `actual` implementation and an app —
`_detect_context_leak_in_singleton` scans both, no project-type gating.
`_detect_context_leak_in_singleton` flags a `Context`/`Activity`/`FragmentActivity`/
`AppCompatActivity`/`ComponentActivity`-typed property inside a `companion object`/
singleton scope.

### `TODO`/`FIXME` — already flagged, verify it's not silently off

Detekt's own `ForbiddenComment` rule (Style ruleset) flags `TODO:`/`FIXME:`/`STOPSHIP:`
comments **by default, active since Detekt 1.0.0** — verified against Detekt's own
docs, not assumed. Because this skill's `detekt.yml` uses `buildUponDefaultConfig =
true`, that default stays active automatically; nothing extra to enable. Worth stating
explicitly here since it was otherwise invisible — a project could have this running
the whole time with no one aware of it. Only touch it if you want to customize the
prefix list or add `allowedPatterns` exceptions.

### Patch-up fix instead of root-cause fix (hints)

"Is this a real fix or a band-aid" is a judgment call — but two specific, well-known
shapes of the pattern are mechanically detectable, as non-blocking hints (same tier as
the naming hints above):

- **Empty or log-only catch block** — silences the symptom without addressing why the
  exception was thrown. A deliberate best-effort no-op is sometimes genuinely correct
  (rare), so this stays a hint, not a blocker.
- **`@Suppress("Rule")` with no nearby comment** explaining why it's a false positive
  vs. silencing a real finding — legitimate suppressions are common, the missing
  justification is the actual signal, not the suppression itself.

```kotlin
// ❌ — hint fires: swallows the exception, no comment explaining why
try {
    risky()
} catch (e: Exception) {
}

// ✓ — real recovery, not flagged
try {
    risky()
} catch (e: IOException) {
    retryWithBackoff()
    logFailure(e)
}
```

A `TODO`/`FIXME` found in or near the flagged block is corroborating evidence, not a
requirement to fire — `_detect_empty_catch_block` and `_detect_unjustified_suppress`
both note it in the finding text when present, since a "TODO: fix properly" sitting
right next to the patch is a strong tell it's a known gap, not a considered decision.

---

