# Step 5 — Binary compatibility validator

Part of `kmp-library-publishing`. Load this file when working on: step 5 — binary compatibility validator.

---

The `binary-compatibility-validator` plugin generates `.api` dump files that track
every public symbol. A CI check (`apiCheck`) fails if a release PR accidentally removes
or changes a public API.

**One-time setup (after configuring the plugin in root build):**

```bash
./gradlew apiDump   # generates library/api/library.api
git add library/api/
git commit -m "chore: initial API dump"
```

**On every PR:**

```bash
./gradlew apiCheck  # fails if public API changed without a matching apiDump
```

**When intentionally changing the API:**

```bash
./gradlew apiDump   # regenerate the dump
git add library/api/
git commit -m "feat!: add Foo.bar() to public API"
```

**Marking internal APIs** (excluded from dump):

```kotlin
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class InternalApi
```

Add `InternalApi` to `nonPublicMarkers` in `apiValidation { }` (Step 2).

### `apiCheck` catches *that* the API changed, not *whether the version bump matches*

`apiCheck` fails on any `.api` diff, forcing a deliberate `apiDump` — but it has no
concept of semver. It passes identically whether the diff is a source-compatible
addition (minor-worthy) or a signature change/removal that breaks every consumer
(major-worthy). Nothing currently blocks tagging a *breaking* diff as a minor release.

This isn't mechanically enforceable from the `.api` file alone — the file lists symbols,
not which specific lines changed *how* between two dumps, and "is this actually
source/binary breaking" needs a real diff, not just a checksum mismatch. Treat it as a
review-time discipline instead: before tagging, `git diff` the previous `library.api`
against the new one and classify every change —

| Change | Semver bump |
|---|---|
| New public class/function/property added | Minor |
| Existing public signature changed or removed | Major |
| Internal-only change, `.api` file untouched | Patch |

Get this wrong once (a breaking change shipped as a minor) and every consumer pinned to
`^x.y` silently breaks on their next `./gradlew build` — there's no compiler error on
their side, just a runtime `NoSuchMethodError` or a build failure with no obvious cause.

---

