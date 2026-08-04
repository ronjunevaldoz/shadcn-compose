# Engine Rules Template

> Copy this file to `docs/engine_rules.md` in your project and fill in the sections.
> The `jni-kotlin-pro` skill reads `docs/engine_rules.md` if it exists.

## Architecture flow

```
Kotlin engine class (jvmMain)
  ↓
JNI binding  (native/jni/*-jni.cpp)
  ↓
C wrapper    (native/jni/*-wrapper.cpp)
  ↓
*.cpp core   (llama.cpp / stable-diffusion.cpp / your-native-lib)
```

Never bypass the C wrapper. Never call the core engine directly from JNI.

## JNI layer rules

- Stateless: no inference logic, no model ownership, no global state beyond the native handle
- Only: type conversion (`jstring` → `std::string`, `jfloat[]` → `float*`) + wrapper call + return
- Throw `RuntimeException` with a message on every failure — never return null silently

## C wrapper rules

- Owns context lifecycle: every `*_create` has a matching `*_free`
- RAII-guard all allocations; no early returns without cleanup
- Error message stored in context struct, retrievable via `get_error()` accessor

## Memory rules

- `jstring` → always `GetStringUTFChars` / `ReleaseStringUTFChars` in the same scope
- `jbyteArray` / `jfloatArray` → use `JNI_ABORT` on release (no write-back needed for reads)
- `DeleteLocalRef` inside any loop that creates JNI objects

## Threading

- All JNI calls are blocking — wrap in `withContext(Dispatchers.IO)` on the Kotlin side
- Never call JNI on the main thread

## API stability

Stable ABI surface — extend by adding new functions, never change existing signatures:

| Module | Stable symbols |
|--------|---------------|
| `your-jni` | `nativeCreate`, `nativeFree`, `nativeGenerate` |

## Stable engines

Features listed here require the full change gate in
`references/stable-feature-guard.md` before any modification.

| Feature | Files | Stable since | Invariants |
|---|---|---|---|
| _(add entries here when features are production-validated)_ | | | |
