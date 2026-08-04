---
name: kmp-native-authoring
description: >
  Scaffolds and structures brand-new, first-party C/C++ source for a Kotlin Multiplatform
  library's native core (a custom renderer, codec, or engine you are writing yourself) —
  directory layout, CMake setup, public C-ABI header design, and native-side testing.
  Produces the artifact kmp-jni-pro's Phase 0 discovery expects to find.
  Does NOT cover bridging Kotlin to native code — that is kmp-jni-pro's
  job, always used after this skill, never instead of it. Does NOT cover wrapping an
  existing 3rd-party C/C++ library — that is also jni-pro's job (its Phase 0
  library-first discovery), not this skill's.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - native core
    - first-party native code
    - author C++ library
    - CMakeLists.txt
    - native renderer
    - custom engine
    - native library scaffold
    - public C-ABI header
    - cinterop def file
    - native ctest
    - write native code from scratch
---

## When to Use This Skill

Use this skill when you need to:
- Write brand-new C/C++ source for a KMP library's native core — a custom renderer
  (Vulkan, Metal), a custom codec, a custom compute engine — where the code doesn't
  exist anywhere yet
- Decide the directory layout, build system, and public header shape for that native
  core before any Kotlin binding is written
- Set up native-side tests (ctest/gtest) independent of the Kotlin test suite

Do NOT use this skill when:
- A 3rd-party C/C++ library already exists and needs a Kotlin bridge — that's
  `kmp-jni-pro`'s Phase 0 (library-first discovery); this skill's whole
  premise is that the native code doesn't exist yet
- The task is writing the actual JNI/cinterop glue for native code that's already
  built — that's `kmp-jni-pro`, always the next skill after this one,
  never a replacement for it
- The native need is trivial (a handful of pure functions with no state, no build
  complexity) — a plain `expect`/`actual` Kotlin/Native implementation via
  `kmp-expect-actual` may cover it without a separate C/C++ core at all

**Trigger keywords:** native core, first-party native code, author C++ library,
CMakeLists.txt, native renderer, custom engine, native library scaffold, public C-ABI
header, cinterop def file, native ctest, write native code from scratch.

**Freshness rule:** CMake, Android NDK toolchain versions, and Kotlin/Native cinterop
all move independently of each other — recheck the current NDK version pinned in the
project's `local.properties`/`libs.versions.toml` and the current Kotlin/Native cinterop
docs before scaffolding; do not assume last year's CMake minimum version or `.def` file
shape still applies.

---

## Recommendation First

Default to this approach:

1. **One `native/` directory, one `CMakeLists.txt`, reused across every platform** —
   Android NDK, iOS/Kotlin-Native cinterop, and JVM/Desktop JNI all build from the same
   CMake project. Never duplicate build logic per platform.
2. **A stable public C-ABI header before any Kotlin code exists.** `native/include/` is
   the contract; `native/src/` is free to change internally as long as the header
   doesn't. This header is exactly what `kmp-jni-pro`'s Phase 0
   discovery will read once bridging starts.
3. **Plain C types at the actual boundary, even if internals are C++.** STL containers,
   exceptions, and templates don't cross a JNI/cinterop boundary reliably — the public
   header speaks structs/primitives/opaque handles; C++ classes stay internal to
   `native/src/`.
4. **Native tests run independently, via ctest, in their own CI job** — not bundled into
   the Kotlin `jvmTest` task, and not skipped because "the Kotlin side has tests."

Why:
- one CMake project avoids the classic drift where Android's build flags silently
  diverge from iOS's for the same source
- a stable header lets Kotlin-side work (this skill's output feeds directly into
  `kmp-jni-pro`) start before every native feature is finished
- a C-ABI boundary is the only boundary JNI and Kotlin/Native cinterop both actually
  support without extra shimming — designing for it from day one avoids an expensive
  C-shim retrofit later

---

## Step 1: Directory layout

```
native/
├── CMakeLists.txt
├── include/
│   └── <library>/
│       └── <library>.h        # public C-ABI surface — the only file jni-pro reads
├── src/
│   ├── <library>.cpp           # C-ABI entry points, thin — delegates to internals below
│   └── internal/
│       └── ...                 # real C++ implementation, STL/exceptions/templates OK here
└── tests/
    ├── CMakeLists.txt
    └── <library>_test.cpp      # gtest/Catch2, native-only, no JVM/Kotlin involved
```

Rules:
- `include/` is the only directory `kmp-jni-pro`'s Phase 0 discovery
  should ever need to grep — keep it small, stable, and free of internal types
- `src/internal/` is where real engineering happens — templates, exceptions, RAII,
  whatever C++ idioms fit; none of it crosses the C-ABI boundary directly
- `tests/` never depends on anything JVM/Kotlin — it must build and run with a plain
  C++ toolchain alone, so CI can run it before the Kotlin side even compiles

## Step 2: The public C-ABI header

```c
// native/include/vulkanrenderer/vulkanrenderer.h
#ifndef VULKANRENDERER_H
#define VULKANRENDERER_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct VrRenderer VrRenderer;   // opaque handle — internals never exposed

VrRenderer* vr_renderer_create(int width, int height);
void        vr_renderer_destroy(VrRenderer* renderer);   // every _create has a _destroy
int         vr_renderer_draw_frame(VrRenderer* renderer);

#ifdef __cplusplus
}
#endif
#endif
```

- `extern "C"` blocks even though the implementation is C++ — this is what makes the
  symbols callable from both JNI (`GetProcAddress`-style dynamic loading) and Kotlin/Native
  cinterop's `.def` file parsing without name-mangling issues
- opaque pointer handles (`VrRenderer*`), never a struct with C++ members exposed by value
- every `_create` paired with a `_destroy` — `kmp-jni-pro`'s own memory-safety
  discipline depends on this pairing existing at the native layer, not invented at the bridge
- **using C's `uint8_t`/`uint16_t`/`uint32_t`/`uint64_t` in the header is fine and often
  correct** (pixel formats, bit flags, checksums) — but the two Kotlin sides handle it
  differently: Kotlin/Native cinterop maps them directly to `UByte`/`UShort`/`UInt`/
  `ULong` automatically, while JNI has no unsigned primitive at all — see
  `kmp-jni-pro`'s type-mapping reference for the JNI-side conversion.
  Verified against kotlinlang.org's own unsigned-integer-types docs: don't use unsigned
  types for sizes/indices/array lengths in the header, even though C conventionally does
  for those — Kotlin's own guidance is to keep those signed

### Header vs implementation comments

Same split as `kmp-code-quality`'s Kotlin KDoc-vs-`//` convention, applied to C++ — the
principle is language-agnostic, only the syntax differs. Verified against the
[Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)'s own
Comments section:

- **Header declaration comment** (`native/include/*.h`) — WHAT it does and how to call
  it, nothing else. Read with an implied subject "This function": lead with a verb
  phrase ("Finds the window...", not "Real Cocoa call, compiled as..."). Document
  parameter/return *semantics* the signature alone doesn't convey (what `true`/`false`
  means, what a sentinel value like `nullptr` or `-1` signals) — but not the internal
  reasoning for why the function is implemented the way it is.
- **Implementation comment** (`native/src/*.cpp`/`.mm`) — the tricky HOW/WHY: why this
  approach and not an alternative, a platform limitation being worked around, the exact
  OS-level constants/behavior being relied on. This is where a comment explaining *why*
  Cocoa is being called through `NSApplication.windows` title-matching instead of a raw
  pointer cast belongs — right above the real call, not in the public header a Kotlin
  consumer reads.
- `//` preferred over `/* */`, matching this repo's own header example above and
  Google's explicit guidance.

Mixing the two — putting implementation rationale in the header — means every consumer
of the C-ABI (both the JNI bridge and Kotlin/Native cinterop) has to read past
implementation detail just to see the function's contract, and the header drifts from
the implementation the moment the *why* changes but the *what* doesn't.

## Step 3: CMakeLists.txt — one project, every platform

```cmake
cmake_minimum_required(VERSION 3.22)
project(vulkanrenderer CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(vulkanrenderer SHARED
    src/vulkanrenderer.cpp
    src/internal/renderer_impl.cpp
)
target_include_directories(vulkanrenderer PUBLIC include)

# Android NDK links against the NDK's own Vulkan loader; Desktop/iOS link the system SDK —
# branch here, not in a second CMakeLists.txt.
if(ANDROID)
    target_link_libraries(vulkanrenderer PRIVATE vulkan android log)
else()
    find_package(Vulkan REQUIRED)
    target_link_libraries(vulkanrenderer PRIVATE Vulkan::Vulkan)
endif()

enable_testing()
add_subdirectory(tests)
```

Wiring per platform:
- **Android**: `externalNativeBuild { cmake { path = file("../native/CMakeLists.txt") } }`
  in the library module's `build.gradle.kts`
- **iOS / Kotlin-Native**: build `vulkanrenderer` as a static lib, then point a `.def` file
  at `include/` — `kmp-jni-pro`'s cinterop guidance takes over from here
- **Desktop/JVM**: build the shared lib, load it via `System.loadLibrary("vulkanrenderer")`
  in the JNI bridge `kmp-jni-pro` writes

## Step 4: Handoff to `kmp-jni-pro`

Once `native/include/<library>/<library>.h` compiles and its public functions are stable,
that header is the input to `kmp-jni-pro`'s Phase 0 (library-first
discovery) — treat this native core exactly like any other 3rd-party library from that
point on: read-only from the Kotlin side, discovered via its header, wrapped via the
C-shim/wrapper patterns that skill documents. Do not start writing `external fun`
declarations before the header has stabilized — churn in the header means churn in every
JNI stub built against it.

---

## Testing

```cpp
// native/tests/vulkanrenderer_test.cpp
#include <gtest/gtest.h>
#include "vulkanrenderer/vulkanrenderer.h"

TEST(VulkanRenderer, CreateAndDestroyDoesNotCrash) {
    VrRenderer* renderer = vr_renderer_create(1920, 1080);
    ASSERT_NE(renderer, nullptr);
    vr_renderer_destroy(renderer);
}

TEST(VulkanRenderer, DrawFrameReturnsZeroOnSuccess) {
    VrRenderer* renderer = vr_renderer_create(1920, 1080);
    EXPECT_EQ(vr_renderer_draw_frame(renderer), 0);
    vr_renderer_destroy(renderer);
}
```

```cmake
# native/tests/CMakeLists.txt
find_package(GTest REQUIRED)
add_executable(vulkanrenderer_test vulkanrenderer_test.cpp)
target_link_libraries(vulkanrenderer_test PRIVATE vulkanrenderer GTest::gtest_main)
add_test(NAME vulkanrenderer_test COMMAND vulkanrenderer_test)
```

Run via `ctest` in its own CI job — independent of `./gradlew jvmTest`, so a native
regression is caught before it ever reaches the Kotlin side.

---

## Common Anti-Patterns

- **Writing JNI/cinterop glue before the native header has stabilized** — every header
  change forces a matching bridge rewrite; finish Step 2/3 first, hand off per Step 4
- **Exposing C++ STL types (`std::vector`, `std::string`) or exceptions directly at the
  `extern "C"` boundary** — neither JNI nor Kotlin/Native cinterop can cross them safely;
  convert to plain arrays/length pairs and error codes at the boundary, keep STL internal
- **A struct with C++ member functions or virtual methods passed by value across the
  boundary** — use an opaque pointer handle instead; the boundary should never need to
  know the struct's real layout
- **Duplicating CMake logic per platform** (a separate `CMakeLists-android.txt`,
  `CMakeLists-ios.txt`) instead of one project with platform branches — guarantees the
  two drift
- **No native-side test coverage, relying on the Kotlin test suite to catch native bugs**
  — a native regression should fail `ctest` before a single line of Kotlin runs
- **Treating this skill's output as done and skipping `kmp-jni-pro`** —
  a stable header is necessary but not sufficient; the actual bridge still needs that
  skill's memory-safety and type-mapping discipline

---

## Related Skills

- `kmp-jni-pro` — always the next step after this skill; bridges Kotlin
  to the native core this skill produces, and is the skill to use instead when the native
  code already exists as a 3rd-party dependency
- `kmp-expect-actual` — for native needs simple enough that a plain
  Kotlin/Native implementation covers it without a separate C/C++ core at all
- `kmp-library-publishing` — a library with a native core still ships
  through the normal Maven Central / XCFramework flow; the native build just becomes part
  of what each platform target compiles
- `kmp-api-mimicry` — if the native core also needs a mimicked
  higher-level Kotlin API shape on top (e.g. a Compose-inspired DSL over a Vulkan
  renderer), that skill covers the Kotlin-side ergonomics once this skill's core exists

---

## Output Style

When asked to scaffold or structure a native core, respond in this order:
1. recommendation (one `native/` dir, one CMakeLists.txt, C-ABI header first)
2. directory layout for the specific native core requested
3. the public header — opaque handles, `extern "C"`, paired create/destroy
4. CMakeLists.txt with the actual per-platform link requirements named
5. the explicit handoff point to `kmp-jni-pro`

Keep the header snippet to the functions actually requested — do not invent a fuller API
surface than what was asked for.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-03 | Added "Header vs implementation comments" — a user's pasted header mixed WHAT (what a function does) with deep implementation WHY (a Compose Desktop pointer-lookup workaround) in the same comment block. Verified against the Google C++ Style Guide's own Comments section: declaration comments should be verb-first WHAT+usage, implementation rationale belongs in the `.cpp`/`.mm` definition instead. Cross-referenced from `kmp-code-quality`'s Kotlin KDoc-vs-`//` section as the same principle, different syntax. |
| 2026-07-31 | Added a note on C unsigned integer types in the public header — cinterop maps them directly to Kotlin's `UByte`/`UShort`/`UInt`/`ULong`, JNI has no unsigned primitive at all (see `jni-pro`'s type-mapping reference); also flagged that sizes/indices should stay signed even where C convention uses unsigned, per kotlinlang.org's own guidance. |
| 2026-07-31 | Initial release. |
