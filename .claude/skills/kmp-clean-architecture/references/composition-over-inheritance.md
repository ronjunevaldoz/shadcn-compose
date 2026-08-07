# Composition Over Inheritance in commonMain

Part of `kmp-clean-architecture`. Load this file when working on: composition over inheritance in commonmain.

---

**commonMain APIs should be called or composed, not extended.** This is a distinct rule
from the layer contract above — it applies within any single layer, to how a class
exposes itself to the code that uses it.

A recurring, real anti-pattern: an agent asked to make something "reusable" or "shared"
reaches for a public `abstract class` in `commonMain` with abstract members a consumer
must override — importing an Android/Spring-style inheritance instinct
(`Application`, `@Component` base classes) into a context where it actively works
against KMP's advantage. The base class now dictates *how* every consumer must structure
their app around it, and a CMP upgrade or a second consumer with different needs can't
easily deviate from the inheritance chain the shared code imposed.

This isn't specific to any one domain — the same shape shows up as a game engine's
`GenericGameApplication`, a network layer's `BaseApiClient`, a plugin system's
`AbstractPlugin`. The smell is the shape (`abstract class`, only abstract members, in
`commonMain`), not the name:

```kotlin
// ❌ Forces every consumer into this exact inheritance chain — commonMain shouldn't
// assume how the consumer structures their own app.
abstract class GenericGameApplication {
    abstract fun onInitialize()
    abstract fun onConfigure(): AppConfig
}

// ✓ Consumer implements and injects this — same flexibility, no inheritance chain,
// no assumption about the consumer's own class hierarchy.
interface GameLifecycle {
    fun onInitialize()
    fun onConfigure(): AppConfig
}

fun bootstrap(lifecycle: GameLifecycle) {
    lifecycle.onInitialize()
    val config = lifecycle.onConfigure()
    // ...
}
```

Wire `lifecycle` through Koin (already this collection's default DI, see
`kmp-dependency-injection`) the same way any other dependency is
injected — nothing about "the consumer must implement a contract" requires inheritance.

**Mechanical detector:** Detekt's real `AbstractClassCanBeInterface` rule (wired in
`kmp-code-quality`) flags exactly this shape — "abstract class with only
abstract members should be an interface instead" — for any project with Detekt already
configured. `kmp-audit`'s `_detect_extensible_abstract_class_in_common`
is a project-independent backstop for the same signal, scoped specifically to
`commonMain` (an abstract class with only abstract members in a platform source set —
`androidMain`, `iosMain` — is often a genuine platform requirement, not this smell).

**When an abstract class in commonMain is fine:** it has at least one concrete member —
a real template-method pattern with genuinely shared logic (e.g., `BaseRepository`'s
`cachedFetch()` calling an abstract `fetch()`) is not this anti-pattern. The rule targets
*pure* templates with nothing shared at all — those provide zero benefit over an
interface and only cost the consumer their inheritance slot.

---

