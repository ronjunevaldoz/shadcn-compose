# JNI Type Mapping Reference

## Kotlin → JNI → C++ table

| Kotlin | JNI parameter type | C++ acquisition | C++ release | Notes |
|---|---|---|---|---|
| `Long` (native handle) | `jlong` | `reinterpret_cast<MyCtx*>(static_cast<uintptr_t>(handle))` | — | Null-check before cast; throw if null |
| `String` | `jstring` | `env->GetStringUTFChars(s, nullptr)` | `env->ReleaseStringUTFChars(s, ptr)` | `GetStringUTFChars` can return null (OOM) — check before use |
| `ByteArray` (read) | `jbyteArray` | `env->GetByteArrayElements(arr, nullptr)` | `env->ReleaseByteArrayElements(arr, ptr, JNI_ABORT)` | `JNI_ABORT` = no write-back; use `0` only when writing results back |
| `ByteArray` (write result) | `jbyteArray` | `env->NewByteArray(len)` then `env->SetByteArrayRegion(...)` | — | Allocate on JVM heap; check for null return |
| `FloatArray` (read) | `jfloatArray` | `env->GetFloatArrayElements(arr, nullptr)` | `env->ReleaseFloatArrayElements(arr, ptr, JNI_ABORT)` | Same `JNI_ABORT` rule |
| `IntArray` (out param) | `jintArray` | `jint buf[N]; ... env->SetIntArrayRegion(arr, 0, N, buf)` | — | Write via `SetIntArrayRegion`, not via pointer |
| `Boolean` | `jboolean` | direct | — | Return `JNI_TRUE` / `JNI_FALSE`, not `1` / `0` |
| `Int` | `jint` | direct | — | — |
| `Float` | `jfloat` | direct | — | — |
| `Long` (return) | `jlong` | — | — | Cast pointer: `static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr))` |

## Native handle pattern

```cpp
// Create — return handle as jlong
JNIEXPORT jlong JNICALL Java_..._nativeCreate(JNIEnv* env, jobject, jstring modelDir) {
    const char* dir = env->GetStringUTFChars(modelDir, nullptr);
    if (!dir) { env->ThrowNew(env->FindClass("java/lang/RuntimeException"), "OOM"); return 0L; }
    auto* ctx = new MyCtx();
    bool ok = ctx->load(dir);
    env->ReleaseStringUTFChars(modelDir, dir);
    if (!ok) { delete ctx; env->ThrowNew(..., ctx->error.c_str()); return 0L; }
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(ctx));
}

// Use — cast handle back
JNIEXPORT jbyteArray JNICALL Java_..._nativeSynthesize(JNIEnv* env, jobject, jlong handle, ...) {
    auto* ctx = reinterpret_cast<MyCtx*>(static_cast<uintptr_t>(handle));
    if (!ctx) { env->ThrowNew(..., "null handle"); return nullptr; }
    ...
}

// Free — delete and zero
JNIEXPORT void JNICALL Java_..._nativeFree(JNIEnv* env, jobject, jlong handle) {
    delete reinterpret_cast<MyCtx*>(static_cast<uintptr_t>(handle));
}
```

## Returning raw bytes to Kotlin

```cpp
// Native allocates the result
std::vector<float> audio = compute();
jbyteArray result = env->NewByteArray((jsize)(audio.size() * sizeof(float)));
if (!result) { env->ThrowNew(..., "OOM allocating result"); return nullptr; }
env->SetByteArrayRegion(result, 0, (jsize)(audio.size() * sizeof(float)),
    reinterpret_cast<const jbyte*>(audio.data()));
return result;
```

```kotlin
// Kotlin side reads float32 from ByteArray
val buf = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
val floats = FloatArray(bytes.size / 4) { buf.float }
```

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| `ReleaseStringUTFChars` skipped on error path | Memory leak per call | Release before every `return` |
| `GetByteArrayElements` with mode `0` on read-only data | JVM may write-back garbage | Use `JNI_ABORT` |
| `NewByteArray` return not null-checked | NPE crash on OOM | Check and throw before use |
| `reinterpret_cast` without `static_cast<uintptr_t>` intermediate | UB on platforms where `jlong` ≠ pointer width | Always double-cast |
| Throwing exception and then continuing | Undefined JNI state | `return` immediately after every `ThrowNew` |
