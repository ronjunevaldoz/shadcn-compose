# Header Compatibility Matrix — Deterministic `.h` Auditing

Before writing a single line of JNI or wrapper code, scan the target 3rd party header
line by line and classify every type, function, and paradigm that would cross the
JVM/native boundary. Output the matrix below. **No code generation until the matrix is
complete and every Unsupported row has a remediation path.**

The 3rd party header is read-only (see EP-9). This audit never edits it — it only
classifies what it exposes.

---

## Scanning procedure

1. List the public surface — every non-`static`, non-`detail::` declaration:
   ```bash
   grep -nE '^[^ ].*\(|^\s*(struct|class|enum|using|typedef|template)' vendor/<lib>/include/*.h
   ```
2. For each declaration, classify into one of three tiers (table below).
3. For each Conditional or Unsupported entry, name the exact JNI strategy or shim path.
4. Cross-reference `type-mapping.md` for every Supported primitive's acquire/release rule.

---

## The three tiers

| Tier | Meaning | Action |
|---|---|---|
| **Supported** | Crosses the boundary with a known, ABI-stable JNI mapping | Map directly in the bridge; follow `type-mapping.md` |
| **Conditional** | Crosses only with a specific adapter in the wrapper layer | Adapt in `*-wrapper.cpp`; never in the JNI bridge |
| **Unsupported** | No stable ABI mapping exists | HALT. Emit an Architectural Feedback Report (`architectural-feedback-schema.md`); design a C-shim |

---

## Compatibility matrix (emit this for every audit)

| C++ feature found | Tier | Risk | JNI strategy / remediation |
|---|---|---|---|
| `int`, `float`, `double`, `bool`, `char`, `int64_t` | Supported | Low | Direct: `jint`, `jfloat`, `jdouble`, `jboolean`, `jchar`, `jlong` |
| `const char*` (null-terminated) | Supported | Low | `GetStringUTFChars` / `ReleaseStringUTFChars` on every exit path (EP-6) |
| `void*` / opaque context handle | Supported | Low | Store as `jlong`: `reinterpret_cast<Ctx*>(static_cast<uintptr_t>(h))`; null-check before cast |
| POD struct (flat, trivial, primitives only) | Supported | Low | Map fields individually, or pass the whole struct as an opaque `jlong` handle |
| Fixed-size primitive array (`float[N]`) | Supported | Low | `Get/SetFloatArrayRegion`; verify N against the header constant |
| `extern "C"` free function | Supported | Low | Direct bridge call — one wrapper call per `Java_*` function |
| `enum` / `enum class` | Conditional | Low | Map to `jint`; verify every underlying value against the header (EP-3) — do not assume 0/1 |
| `std::string` (by value / `const&`) | Conditional | Medium | Copy to/from `jstring` inside the wrapper; never expose `std::string` in `*-wrapper.h` |
| `std::vector<primitive>` | Conditional | Medium | Query size, copy to a `j*Array` via `SetArrayRegion`; caller owns the JVM array |
| C++ class with clean ctor/dtor lifecycle | Conditional | Medium | Opaque `jlong` handle + explicit `nativeFree`; matching `_create`/`_free` (no leak) |
| Function pointer callback (`void(*)(...)`) | Conditional | Medium | Trampoline pattern (`wrapper-patterns.md` Pattern 3); store JVM ref as GlobalRef |
| Output parameter (`T*`, `T&` out-arg) | Conditional | Medium | Wrapper adapts to a return value or `SetArrayRegion`; no raw pointers to Kotlin |
| Pointer that must outlive the call (pinned buffer) | Conditional | High | `GetPrimitiveArrayCritical` only in a tight scope; guaranteed `ReleasePrimitiveArrayCritical`; no JNI calls in between |
| **C++ template / template metaprogramming** | **Unsupported** | **Critical** | Block direct JNI. Instantiate the concrete type behind an `extern "C"` C-shim |
| **`std::function` / capturing lambda** | **Unsupported** | **Critical** | No stable ABI. Convert to function-pointer + `void* user_data` shim, then trampoline |
| **Overloaded functions (same name)** | **Unsupported** | **High** | Name mangling differs per compiler. Expose distinct `extern "C"` shim names |
| **C++ exceptions crossing the boundary** | **Unsupported** | **Critical** | A native `throw` reaching the JVM frame is UB. Wrap in `try/catch`; translate via `ThrowNew` |
| **RAII type returned by value (non-trivial dtor)** | **Unsupported** | **High** | Lifetime ambiguous across boundary. Heap-allocate; return opaque `jlong`; explicit free |
| **Un-isolated global / static mutable state** | **Unsupported** | **Critical** | Not thread-safe under JVM threads. Serialize access in the shim or document a single-thread contract |
| **Platform-specific thread pool / TLS** | **Unsupported** | **Critical** | JVM threads lack the library's TLS. Shim must `AttachCurrentThread`; never assume thread context |
| **Multiple / virtual inheritance** | **Unsupported** | **High** | vtable layout not ABI-stable. Expose a flat C-shim over the concrete interface |
| **Header-only / `inline` logic (no symbol)** | **Unsupported** | **Medium** | Nothing to link against. Compile into a shim translation unit that emits an `extern "C"` symbol |

---

## Decision gate

After the matrix:

- **All rows Supported or Conditional** → proceed to the wrapper layer using `wrapper-patterns.md`.
- **Any row Unsupported** → HALT. Do not attempt a direct JNI map. Emit an Architectural
  Feedback Report per `architectural-feedback-schema.md` proposing the C-shim design, and
  wait for confirmation before generating code.

---

## Output rules

- Emit the matrix as the **first artifact** of any JNI task, before any code.
- Every row cites the exact header line: `your_lib.h:142`.
- Never downgrade a tier to make a task easier — an Unsupported row stays Unsupported until
  a C-shim removes the offending paradigm from the boundary.
- The matrix is evidence: it is what justifies every later acquire/release and shim decision.
