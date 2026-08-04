# Architectural Feedback Schema — When a Header Can't Be Mapped Directly

When the compatibility matrix (`header-compatibility-matrix.md`) produces any **Unsupported**
row, do not generate JNI code. Halt and emit an Architectural Feedback Report in the exact
format below. The report proposes a C-shim that removes the unsupported paradigm from the
JVM/native boundary — it never proposes editing the 3rd party header (EP-9).

---

## Why a C-shim, not a direct map

The unsupported paradigms (templates, `std::function`, overloads, exceptions, RAII-by-value,
virtual inheritance, header-only inline) have no stable, compiler-independent ABI. JNI can
only bind to flat `extern "C"` symbols with primitive or pointer arguments. The C-shim is a
thin, project-owned translation unit that:

1. Includes the 3rd party header (read-only).
2. Instantiates the concrete type / wraps the unsupported construct.
3. Exposes a flat `extern "C"` API the JNI bridge can bind to.

The shim is **your code** — it lives in `src/main/cpp/bridge/` (or your wrapper dir), never
in `vendor/`.

---

## Report format (emit verbatim)

```
═══ ARCHITECTURAL FEEDBACK REPORT ═══

BLOCKER
  Header:        <vendor/lib/include/your_lib.h:LINE>
  Construct:     <exact declaration, e.g. template<typename T> Codec<T> make_codec();>
  Tier:          Unsupported
  Reason:        <one line — why it cannot cross the JNI boundary>
  Matrix ref:    <which matrix row this matches>

WHY A DIRECT JNI MAP FAILS
  <2–3 sentences: the specific ABI / lifetime / threading problem>

PROPOSED C-SHIM PATH
  Shim file:     src/main/cpp/bridge/<lib>-shim.h  +  <lib>-shim.cpp   (project-owned)
  Strategy:      <one of: concrete instantiation | flat re-export | exception gate |
                  trampoline adapter | thread-attach wrapper | serialize-access guard>

  Shim API (extern "C", primitives + opaque pointers only):
    <signatures the shim will expose>

  Boundary after shim:
    <which matrix tier each shimmed symbol now lands in — must be Supported/Conditional>

OPEN QUESTIONS / DECISIONS NEEDED FROM USER
  <anything that changes the shim design — concurrency contract, ownership, lifetime>

STATUS: HALTED — awaiting confirmation of shim design before code generation.
```

---

## Strategy catalogue (pick one per blocker)

| Unsupported construct | Shim strategy | What the shim emits |
|---|---|---|
| Template / metaprogramming | **Concrete instantiation** | `extern "C"` functions over each concrete `T` actually used (e.g. `codec_create_f32`, `codec_create_i16`) |
| `std::function` / capturing lambda | **Trampoline adapter** | `void(*)(const T*, void* user_data)` + a `void* user_data` slot; capture moved into a heap context |
| Overloaded functions | **Flat re-export** | One distinctly named `extern "C"` symbol per overload (`lib_open_path`, `lib_open_fd`) |
| C++ exceptions at boundary | **Exception gate** | `try { ... } catch (const std::exception& e) { *out_err = strdup(e.what()); return -1; }` |
| RAII type returned by value | **Heap + opaque handle** | `Handle* create()` returning a heap pointer; matching `void free(Handle*)` |
| Virtual / multiple inheritance | **Flat interface re-export** | `extern "C"` functions taking an opaque `void*` and dispatching to the concrete object |
| Un-isolated global state | **Serialize-access guard** | A shim-owned mutex around every entry point; or a documented single-thread contract |
| Platform thread pool / TLS | **Thread-attach wrapper** | Shim entry points that `AttachCurrentThread` / `DetachCurrentThread` around the library call |
| Header-only inline logic | **Compiled TU** | A `.cpp` that `#include`s the header and force-emits `extern "C"` wrappers as real symbols |

---

## After the shim is approved

1. The shim's flat API re-enters the compatibility matrix — every shimmed symbol must now
   classify as **Supported** or **Conditional**.
2. Proceed to the wrapper layer (`wrapper-patterns.md`); the wrapper calls the shim, the
   shim calls the library.
3. The layer stack gains one tier:
   ```
   Kotlin engine → JNI bridge → C wrapper → C-shim (extern "C") → 3rd party library (read-only)
   ```
   The shim is the only new code; the library is still never touched.

---

## Hard rules

- Never silently downgrade an Unsupported construct to force a direct map.
- Never propose editing the 3rd party header as the remediation — the shim wraps it from outside.
- The report is a **halt point**: no JNI, wrapper, or Kotlin code until the shim design is confirmed.
- Every shimmed symbol must be `extern "C"`, take only primitives / opaque pointers, and own
  no library lifetime the wrapper doesn't explicitly free.
