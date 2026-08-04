---
name: kmp-api-mimicry
description: >
  Mimics the public API *shape* (DSL entry points, chainable modifier objects, slot
  lambdas, marker annotations, scoped builders) of a well-known reference API — Jetpack
  Compose, SwiftUI, Retrofit, Room, etc. — when building a from-scratch Kotlin
  Multiplatform library on a different runtime (a custom Vulkan/Metal renderer, a
  custom RPC engine, a custom persistence engine). Does NOT cover consuming or wrapping
  the real reference library — that is normal dependency usage. Does NOT cover building
  a real Compose-compiler-style plugin; explicitly scopes to plain-function DSL mimicry
  and documents what real recomposition/skipping you give up by not writing one.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - mimic api
    - api mimicry
    - clone api shape
    - inspired by jetpack compose
    - custom dsl engine
    - from-scratch renderer
    - vulkan ui
    - metal ui
    - port api ergonomics
    - reimplement compose-like dsl
    - non-compose renderer
    - engine-agnostic dsl
    - own compiler-free dsl
    - api shape porting
---

## When to Use This Skill

Use this skill when you need to:
- Build a Kotlin Multiplatform library on a **non-standard runtime** (a custom native
  renderer, a custom transport, a custom storage engine) and want its Kotlin API to
  *feel* like a well-known reference API (Jetpack Compose, SwiftUI, Retrofit, Room, ...)
  without literally depending on or reimplementing that library's internals
- Design a chainable configuration object (a `Modifier`-equivalent) for a custom
  rendering or layout engine
- Design a slot-lambda DSL (`content: Scope.() -> Unit`) for a custom scene-graph builder
- Decide how far to take the mimicry — plain-function DSL vs. writing an actual
  Kotlin compiler plugin — before committing engineering time to either
- Document, for future maintainers and for legal clarity, exactly which parts of a
  reference API were mimicked and which were deliberately changed

Do NOT use this skill when:
- The project already depends on the real reference library (Compose Multiplatform,
  Ktor, etc.) and just needs to *use* it — that is ordinary consumption, not mimicry;
  see `kmp-compose-design-system` for building a design system **on top of**
  real Compose Multiplatform
- The goal is a real Kotlin compiler plugin providing true fine-grained recomposition —
  that is a distinct, much larger undertaking this skill deliberately does not cover;
  it only tells you when that investment is actually justified (see Decision Table below)
- The goal is copying the reference library's actual source code under a new package —
  never do this; see Guidelines

**Trigger keywords:** mimic api, api mimicry, clone api shape, inspired by jetpack
compose, custom dsl engine, from-scratch renderer, vulkan ui, metal ui, port api
ergonomics, reimplement compose-like dsl, non-compose renderer, engine-agnostic dsl,
own compiler-free dsl, api shape porting.

**Freshness rule:** the reference API you are mimicking changes between its own
releases (Jetpack Compose's `Modifier` and `CompositionLocal` surface, SwiftUI's
`ViewBuilder`, etc.) — re-read the reference API's *current* public docs before
mimicking a new version's shape. Never mimic from memory or from an old cached
understanding of the reference API; verify against its real current source or docs.

---

> **Hard rule — never violated:** `Engine` (as in `EngineModifier`, `EngineScope`,
> `EngineUiDsl`) is a **placeholder in this SKILL.md's worked example**, exactly like
> `GROUP_ID`. It must never be written to disk literally for a real project — pick your
> own project's actual prefix before writing the first mimicked primitive. Do not fuse
> your backend's brand name with the reference API's own type name either (e.g. don't
> name it `VulkanModifier` just because the backend is Vulkan and the mimicked type is
> Compose's `Modifier`) unless that fusion is genuinely your project's established
> convention — it usually isn't, and reads as if it were copied from a real, verified
> source when it's just this example's stand-in.

## Recommendation First

Default to this approach:

1. **Catalog the reference API's real public surface first** — read its actual current
   docs/source, don't guess. List the specific types/functions/annotations you intend
   to mimic.
2. **Mimic ergonomics, not internals.** A chainable `Modifier`-style object and a
   slot-lambda DSL are cheap to mimic faithfully with plain Kotlin. Recomposition,
   snapshot state, and skipping are not — they depend on the reference's own compiler
   plugin. Default to a **plain-function DSL with explicit invalidation**, not a fake
   compiler plugin.
3. **Never reuse the reference library's package namespace or literal branding.** Own
   group id, own package, own annotation name (`@EngineUiDsl`, not `@Composable`).
4. **Write a mirror map** at `docs/MIRROR_MAP.md` (not project root — this is a
   Reference-kind doc per `kmp-project-docs-maintainer`'s `docs-hygiene.md`, same
   placement rule as any other permanent, update-in-place registry) documenting each
   mimicked primitive, its inspiration, the deliberate deviation, and whether the
   reference API's own common-case convenience shorthand for that primitive was
   mirrored too (e.g. Compose's `Modifier.fillMaxSize()` is sugar over
   `.width(Max).height(Max)` — implementing only the low-level combinator and skipping
   the shorthand real consumers actually reach for is an incomplete mimicry, not a
   smaller one) — before writing the second mimicked primitive, not after the tenth.
   Mimicking more than one reference API (Compose's `Modifier` *and* a themed
   component API like shadcn-compose) in the same project? One file, add a
   **Reference API** column instead of splitting into one map per reference — keeps a
   single source of truth. If it grows past `docs-hygiene.md`'s 150-line limit for any
   `docs/` file (mechanically checked by `audit_skills_repo.py
   --docs-hygiene-only`), split by Reference API into
   `docs/reference/mirror-map-<reference>.md` files, and keep `docs/MIRROR_MAP.md`
   itself as a short index pointing to each.

Why:
- most of a reference API's *ergonomics* (chainable config, slot lambdas, scoped
  builders) come from ordinary Kotlin language features, not compiler magic — you get
  90% of the developer-experience win for a fraction of the effort
- a real compiler plugin is a permanent maintenance burden riding on your own fork of
  Kotlin compiler internals — only justified when fine-grained recomposition is a
  measured, not assumed, performance requirement
- consumers who see `@Composable`-shaped code assume `@Composable`-shaped guarantees;
  an undocumented gap between mimicked ergonomics and real behavior is where the bug
  reports come from

```kotlin
// Own annotation, own package — not androidx.compose.runtime.Composable
@DslMarker
annotation class EngineUiDsl

@EngineUiDsl
sealed interface EngineModifier {
    fun then(other: EngineModifier): EngineModifier =
        if (other is EngineModifier.None) this else CombinedEngineModifier(this, other)
    object None : EngineModifier
}
```

---

## Core Pattern: Mapping Reference Primitives to Plain Kotlin

This method applies to any reference API — the table below uses Jetpack Compose as the
running example because it has the richest primitive set (DSL, chain object, scoped
ambient values, state), not because this skill is Compose-specific. See "Other
reference APIs, same method" below for non-UI examples.

| Reference primitive (e.g. Jetpack Compose) | What actually powers it | Mimicable without a compiler plugin? | Kotlin equivalent |
|---|---|---|---|
| `@Composable` function | Compose compiler plugin (call-site tracking, skipping) | No — mimicking the *annotation* without the plugin gives you a plain function call, not real skipping | Own marker annotation (`@EngineUiDsl`) used as documentation only; be explicit it's not enforced |
| `Modifier` chain | Plain immutable linked-list-style object with `then()` | **Yes**, fully | Sealed interface + `then()`, exactly Compose's own shape — no magic involved |
| Slot lambda (`content: @Composable () -> Unit`) | Trailing lambda + receiver scope, a plain language feature | **Yes**, fully | `fun Box(content: EngineScope.() -> Unit)` |
| `CompositionLocal` | Compose runtime's ambient value propagation through the composition tree | No — depends on the runtime tracking a composition tree | Explicit scope object threaded as a parameter, or a plain `ThreadLocal`/context object if the design tolerates it |
| `remember { }` / `mutableStateOf` | Compose's snapshot state system + recomposition scheduler | No — reimplementing snapshot state from scratch is a multi-month undertaking | An explicit state holder bound to your own render loop's invalidation call (`requestRedraw()`); callers manage identity/keys themselves |
| `LaunchedEffect` / lifecycle-scoped coroutines | Compose runtime lifecycle integration | Partially | Your own scene-graph node lifecycle + `CoroutineScope` bound to node attach/detach |

### Decision Table: plain-function DSL vs. real compiler plugin

| Signal | Choose |
|---|---|
| Ergonomics (chainable modifiers, slot DSL, readable call sites) is the goal | Plain-function DSL — covers this fully |
| Fine-grained recomposition/skipping is a *measured* perf requirement, not assumed | Real compiler plugin — but treat this as its own multi-month project, not an extension of this skill |
| Team size / maintenance budget for owning Kotlin compiler plugin internals long-term | If small, plain-function DSL — a compiler plugin tracks Kotlin compiler versions forever |
| Consumers must be able to write literal `@Composable` functions and get real Compose semantics | Neither — depend on real Compose Multiplatform instead; mimicry no longer applies |

### Other reference APIs, same method

Jetpack Compose is one example, not the scope of this skill. The same catalog → map →
decide → mirror-map method applies to any reference API's ergonomics:

| Reference API mimicked | What's mimicked (ergonomics, not internals) | What's explicitly not mimicked |
|---|---|---|
| Retrofit-style declarative service interfaces | `interface`-with-annotated-methods DSL, generating a typed client at your call boundary | Retrofit's actual annotation processor / `CallAdapter` machinery — write a plain factory function instead |
| Room-style DAO annotations | `@Dao`-shaped method-per-query interface as the ergonomic surface | Room's KSP-generated implementation — back it with your own hand-written or codegen'd implementation for your storage engine |
| SwiftUI's `ViewBuilder`/declarative tree | Trailing-closure-equivalent slot lambdas, same as the Compose row above | SwiftUI's own diffing/identity system |
| Jetpack Compose (worked example below) | `Modifier` chain, slot lambdas, DSL marker annotations | Snapshot state, recomposition, `CompositionLocal` propagation |

### Worked example: Vulkan-backed UI DSL inspired by Jetpack Compose

One concrete instance of the method above — not the only valid target for this skill.
A KMP library wraps a custom Vulkan rendering backend (via `expect`/`actual` +
JNI/cinterop — see `kmp-jni-pro` and
`kmp-expect-actual`) and needs a Compose-shaped declarative UI layer on
top of it.

Naming below uses the `Engine` placeholder — see the hard rule above before writing
any of this to disk for a real project.

```kotlin
// :library/src/commonMain/kotlin/.../EngineModifier.kt
@EngineUiDsl
sealed interface EngineModifier {
    object None : EngineModifier
}

private data class CombinedEngineModifier(
    val outer: EngineModifier,
    val inner: EngineModifier,
) : EngineModifier

fun EngineModifier.padding(all: Dp): EngineModifier = this.then(PaddingModifier(all))
fun EngineModifier.size(width: Dp, height: Dp): EngineModifier = this.then(SizeModifier(width, height))

// :library/src/commonMain/kotlin/.../EngineScope.kt
class EngineScope internal constructor(private val node: SceneNode) {

    @EngineUiDsl
    fun Box(modifier: EngineModifier = EngineModifier.None, content: EngineScope.() -> Unit) {
        val child = node.addChild(SceneNode.Kind.Box, modifier)
        EngineScope(child).content()
    }

    @EngineUiDsl
    fun Text(text: String, modifier: EngineModifier = EngineModifier.None) {
        node.addChild(SceneNode.Kind.Text(text), modifier)
    }
}

// Entry point — mirrors setContent { } shape, explicit invalidation instead of
// Compose's snapshot-driven recomposition
fun EngineCanvas.setContent(content: EngineScope.() -> Unit) {
    val root = SceneNode.root()
    EngineScope(root).content()
    this.submit(root)   // hands the built scene graph to the Vulkan backend
}
```

This gives callers Compose-shaped call sites (`Box(modifier = ...) { Text("Hi") }`)
without claiming or requiring real Compose compiler behavior. State changes are the
caller's responsibility — call `setContent` again (or a narrower `invalidate()`) to
rebuild the affected subtree.

### Mimicking more than one reference API in the same project

Subpackage per reference API, not one flat package — two different reference APIs can
reuse the same primitive name for a different concept (both might have something
called `Theme` or `Scope`), and a flat package forces an awkward rename that obscures
which reference actually inspired a given type:

```
:library/src/commonMain/kotlin/.../
├── modifier/              # mirrors Jetpack Compose's Modifier chain
│   ├── EngineModifier.kt
│   └── EngineScope.kt
└── components/            # mirrors shadcn-compose's themed component API
    ├── EngineButton.kt
    └── EngineTheme.kt
```

The marker annotation (`@EngineUiDsl`) stays project-wide, one annotation, not one per
reference — it signals "not real compiler-plugin-backed tooling," which is true
regardless of which reference inspired a given primitive.

---

## Guidelines

- Never copy the reference library's actual source code under a renamed package —
  copyright and drift risk; re-derive the shape from its public docs instead
- Never use the reference library's real package prefix (`androidx.compose.*`,
  `com.squareup.retrofit2.*`) or trademarked product name in your own artifact —
  own group id, own package, own name
- Never apply the reference library's real annotation (`@Composable`) to functions
  your own compiler doesn't process — undefined/misleading behavior for any tooling
  that expects the real Compose compiler plugin
- State plainly in the library's README that it is "API-shape-inspired by X, not
  compiler-compatible with X, no interop with real X code" — this is the single
  highest-value sentence for avoiding wrong-expectation bug reports
- Keep `docs/MIRROR_MAP.md` (not project root): one row per mimicked primitive, which
  **Reference API** it mirrors (only needed once you're mimicking more than one, e.g.
  Compose's `Modifier` *and* a themed component API), its reference inspiration, the
  deliberate deviation, and a **Shorthand mirrored?** column — check whether the
  reference API also exposes a common-case convenience method built from this
  primitive, and whether that shorthand was mirrored too:

  ```markdown
  | Primitive | Reference API | Reference inspiration | Deliberate deviation | Shorthand mirrored? |
  |---|---|---|---|---|
  | `.width(Dp)` / `.height(Dp)` | Jetpack Compose | `Modifier.width`/`.height` | Same semantics, custom renderer | `fillMaxSize()` — YES, added as `.width(Dimension.Max).height(Dimension.Max)` sugar |
  | `.padding(Dp)` | Jetpack Compose | `Modifier.padding` | Single-value only, no per-side overload yet | N/A — no reference shorthand exists for this one |
  | `EngineButton(variant)` | shadcn-compose | `ShadcnButton`'s variant system | Fewer variants, no theming tokens yet | N/A |
  ```

  Drop the **Reference API** column entirely while only one reference is mimicked —
  add it the moment a second one starts.

  A primitive with no corresponding reference shorthand is fine to mark `N/A` — the
  point is to have asked the question for every row, not to force-add shorthands
  that don't exist in the reference API either.
- This is almost always a **library** project (see
  `kmp-library-publishing`), not an app — scaffold accordingly

---

## Testing

```kotlin
class EngineModifierTest {
    @Test
    fun `then chains in call order`() {
        val modifier = EngineModifier.None.padding(8.dp).size(100.dp, 100.dp)
        val flattened = modifier.flatten()   // test helper walking the CombinedEngineModifier chain
        assertEquals(listOf("padding", "size"), flattened.map { it.name })
    }
}

class EngineScopeTest {
    @Test
    fun `Box adds a child scene node with its modifier`() {
        val root = SceneNode.root()
        EngineScope(root).apply {
            Box(modifier = EngineModifier.None.padding(4.dp)) {
                Text("hello")
            }
        }
        assertEquals(1, root.children.size)
        assertEquals(SceneNode.Kind.Box, root.children.single().kind)
        assertEquals(1, root.children.single().children.size)
    }

    @Test
    fun `setContent rebuilds the scene graph on each call`() {
        val canvas = FakeEngineCanvas()
        var label = "first"
        canvas.setContent { Text(label) }
        val firstSubmit = canvas.lastSubmittedRoot
        label = "second"
        canvas.setContent { Text(label) }
        assertNotSame(firstSubmit, canvas.lastSubmittedRoot)   // explicit rebuild, not diffed
    }
}
```

---

## Common Anti-Patterns

- **Copy-pasting real Compose (or any reference library) source under a new package** —
  license and drift risk; the copied code silently diverges from real semantics as the
  original library evolves. Re-derive the shape from public docs instead.
- **Claiming `@Composable`-compatible without a real compiler plugin** — misleads
  consumers about recomposition/skipping guarantees they do not actually get; document
  the gap explicitly instead of implying parity.
- **Reimplementing Compose's full snapshot-state system "just in case"** — a
  multi-month undertaking almost never justified by the actual requirement; an explicit
  `requestRedraw()`/rebuild model covers most custom-renderer needs.
- **Mirroring only the reference API's low-level primitives, skipping its common-case
  shorthand** — e.g. implementing `.width(Dp)`/`.height(Dp)` but never `fillMaxSize()`.
  Technically the same expressive power, but every consumer has to hand-write the
  combinator every call site instead of reaching for the idiom real usage of the
  reference API actually teaches. Caught by the `MIRROR_MAP.md`'s "Shorthand
  mirrored?" column — ask the question for every primitive, not just the ones that
  felt important at the time.
- **Using the reference library's real annotation on non-reference functions** —
  applying `@Composable` to a function your own tooling processes is undefined behavior
  for any real Compose tooling that later touches the same codebase.
- **Skipping the `MIRROR_MAP.md`** — without it, consumers and future maintainers
  assume full behavioral parity with the reference API and file bugs against the wrong
  mental model; write it before the second mimicked primitive.
- **Reusing the reference library's package namespace** (`androidx.compose.*`) to
  "borrow" IDE tooling or autocomplete — breaks real Compose compiler expectations if
  the real library is ever added to the same classpath, and is a trademark/namespace
  collision risk on publish.

---

## Related Skills

- `kmp-library-publishing` — this is almost always a library project;
  publish it the same way as any other KMP library
- `kmp-jni-pro` — if the custom runtime is native (Vulkan, Metal via
  cinterop, etc.), the JNI/cinterop bridge for it lives here
- `kmp-expect-actual` — platform-specific backend wiring for the
  custom renderer
- `kmp-compose-design-system` — use this instead when the target runtime
  **is** real Compose Multiplatform; that skill builds a design system on top of the
  real thing rather than mimicking its shape
- `kmp-compose-slot-api` — slot-lambda patterns for real Compose
  Multiplatform projects; this skill's slot-lambda guidance is the non-Compose analog
- `kmp-clean-architecture` — layer boundaries still apply inside the
  library even though there's no `:ui`/`:presenter` split in the app sense
- `kmp-code-quality` — file/type/function naming conventions apply to mimicked
  primitives too; `Engine` above is a placeholder, not a naming exemption

---

## Output Style

When asked about mimicking a reference API's shape, respond in this order:
1. recommendation (plain-function DSL, not a compiler plugin, unless justified)
2. the specific reference primitives being mapped, and their Kotlin equivalents
3. code snippet (one chainable-modifier example + one slot-lambda example)
4. what real behavior is explicitly NOT being mimicked (recomposition, skipping, etc.)
5. main alternative (a real compiler plugin, or just depending on the real reference library)

Keep the snippet to one modifier chain and one slot function. Always name the specific
reference API being mimicked when the user names one — do not speak generically about
"a reference API" once a real one (Jetpack Compose, SwiftUI, ...) is named.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Added `kmp-code-quality` to Related Skills — naming conventions existed but only `kmp-mvi` cross-referenced them; mimicked primitives need real, project-specific names too, not `Engine`. |
| 2026-08-04 | Three real gaps closed from a single user thread: (1) no guidance existed for mimicking more than one reference API in the same project (e.g. Compose's `Modifier` + shadcn-compose's component API) — added a subpackage-per-reference folder structure and a **Reference API** column for `MIRROR_MAP.md`. (2) `MIRROR_MAP.md` was placed "at the library root," contradicting this collection's own `docs-hygiene.md` Reference-doc placement rule (`docs/` root, not project root) — moved to `docs/MIRROR_MAP.md` and added as a named example row in `docs-hygiene.md`; also documented the split-when-bloated path (150-line `docs/` limit, split by Reference API into `docs/reference/mirror-map-<reference>.md`). (3) the `Engine` placeholder appeared 36 times with only one easy-to-miss disclaimer sentence stated after several early uses already occurred — replaced with a `kmp-compose-design-system`-style "Hard rule — never violated" blockquote callout placed before the placeholder's first use. |
| 2026-08-04 | A user asked whether this skill catches mimicking a reference API's low-level primitives (`.width()`/`.height()`) while missing its common-case convenience shorthand (`fillMaxSize()`) — real gap: `MIRROR_MAP.md`'s row shape had no field prompting that question. Added a "Shorthand mirrored?" column with a concrete example table, and a matching anti-pattern. No mechanical detector — would need a hardcoded per-reference-API convenience-method table to check against, out of scope for a generic heuristic. |
| 2026-07-31 | Initial release. |
