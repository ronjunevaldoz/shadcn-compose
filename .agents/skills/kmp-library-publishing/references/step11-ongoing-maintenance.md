# Step 11 — Ongoing maintenance (post-1.0)

Part of `kmp-library-publishing`. Load this file when working on: step 11 — ongoing maintenance (post-1.0).

---

Everything above covers shipping. A published library also needs a maintenance
practice — real gaps found repeatedly in libraries that only had a publish checklist:

### `core`/`helper`/`sugar` — higher stakes here than in an app

`kmp-code-quality`'s Kotlin Library & Pattern Choices defines this categorization in
full; in a published library specifically:
- `core` (`public`) and `sugar` (`public`, calling into `core`) are **both** binary-compat
  surface — `apiCheck`/`apiDump` (Step 5) track every one of them equally. Getting the
  `core` vs `helper` call wrong here breaks SemVer for real external consumers, not just
  messier internal code the way the same mistake would read in an app.
- `helper` (`internal`) is compiler-enforced once `explicitApi()` is on (Step 1) — a
  library can't accidentally leak a helper as public the way an app without
  `explicitApi()` might.
- `sample-local` is the `sample/` module below — never in the published artifact.
- `deprecated` is the real cycle in the next section, not just a naming tag.

### Deprecation cycle, not silent removal

Never delete a public symbol a consumer might already depend on. Mark it first:

```kotlin
@Deprecated(
    message = "Use fetchUserV2() — handles pagination correctly",
    replaceWith = ReplaceWith("fetchUserV2(id)"),
    level = DeprecationLevel.WARNING,
)
fun fetchUser(id: String): User
```

Cycle, tied to SemVer:
1. **This minor version** — add `@Deprecated(level = WARNING)`. `apiCheck` still passes;
   this is not a binary-breaking change.
2. **Next minor version** — bump to `level = ERROR`. Consumers must migrate to keep
   compiling, but the symbol still exists (source-compatible migration window).
3. **Next major version** — remove the symbol entirely. `apiDump` records the removal;
   `apiCheck` correctly fails until the API dump is regenerated for the major bump.

### Communicating a breaking change

A binary-incompatible change (an `apiCheck` failure you're accepting on purpose, not a
mistake to fix) needs three things before it ships, not just a version bump:
- A `CHANGELOG.md` entry naming the exact symbol and the replacement, not just "breaking changes"
- A migration note if the fix isn't mechanical (find/replace) — show the before/after
- The major version bump itself, per SemVer — a breaking change is never a minor/patch release

### Dependency upgrade cadence

A library's own dependency versions become every consumer's transitive minimum. Pin
conservatively and review on a cadence, not reactively:
- Renovate or Dependabot on the repo, scoped to `gradle/libs.versions.toml`
- Treat a transitive major-version bump (Compose Multiplatform, Kotlin itself) as its own
  reviewed change, never bundled silently into an unrelated feature release
- Keep `sample/`'s own dependency versions pinned to the library's own — a stale sample
  masks a real compatibility break until a real consumer hits it first

### Dependency vulnerability scanning

Distinct from the version-cadence review above — a dependency can be current and still
carry a disclosed CVE. Enable GitHub's own **Dependabot security alerts** (Settings →
Security → Dependabot, or a `.github/dependabot.yml` scoped to `gradle`) on the repo — it
flags a known vulnerability in a dependency independent of whether a routine upgrade PR
would have touched it. Treat an alert on a library's own dependency as higher priority
than the same alert in an app: every consumer inherits it transitively, and a library
maintainer usually doesn't know how many downstream apps are affected.

### Keep `sample/` from drifting

The sample app is the only thing that actually compiles against the library's *public*
API the way a real consumer would — an internal test suite compiles against internals
too and can miss a public-surface break. Run the sample's build as its own CI job on
every PR, not just at release time:

```bash
./gradlew :sample:compileKotlinX  # X = every registered target
```

A sample that still compiles against a symbol scheduled for removal is a signal the
deprecation cycle above hasn't actually reached consumers yet — don't remove the symbol
from the library until the sample itself has migrated off it.

---

