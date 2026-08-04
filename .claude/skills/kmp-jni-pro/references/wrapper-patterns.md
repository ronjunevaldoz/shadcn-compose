# C Wrapper Patterns

Concrete patterns for `*-wrapper.cpp` / `*-wrapper.h`. Every pattern has the same
constraint: **zero JNI types, zero algorithm code, zero reimplementation of library
functions.** The wrapper's only job is to own lifecycle and translate between the
Kotlin-facing API and the library's C++ API.

---

## Pattern 1 — Context lifecycle (create / run / free)

The baseline. Use for any stateful library (inference engines, codecs, encoders).

**`engine-wrapper.h`**
```cpp
#pragma once
#include "vendor/your-lib/include/your-lib.h"  // include here, not in jni.cpp

struct EngineCtx {
    your_lib_handle_t* handle = nullptr;
    std::string        last_error;
};

EngineCtx*  engine_create(const char* model_path);
int         engine_run(EngineCtx* ctx, const float* in, int in_len,
                       float* out, int out_len);
const char* engine_get_error(const EngineCtx* ctx);
void        engine_free(EngineCtx* ctx);
```

**`engine-wrapper.cpp`**
```cpp
#include "engine-wrapper.h"

EngineCtx* engine_create(const char* model_path) {
    auto* ctx = new EngineCtx{};
    ctx->handle = your_lib_init(model_path);         // ← library function
    if (!ctx->handle) {
        ctx->last_error = "your_lib_init failed: " + std::string(model_path);
    }
    return ctx;
}

int engine_run(EngineCtx* ctx, const float* in, int in_len,
               float* out, int out_len) {
    if (!ctx->handle) { ctx->last_error = "not initialised"; return -1; }
    int rc = your_lib_process(ctx->handle, in, in_len, out, out_len);  // ← library
    if (rc != YOUR_LIB_OK) ctx->last_error = your_lib_strerror(rc);   // ← library
    return rc;
}

const char* engine_get_error(const EngineCtx* ctx) {
    return ctx ? ctx->last_error.c_str() : "null context";
}

void engine_free(EngineCtx* ctx) {
    if (!ctx) return;
    if (ctx->handle) your_lib_free(ctx->handle);    // ← library
    delete ctx;
}
```

---

## Pattern 2 — Streaming / incremental output

Use when the library generates output incrementally (token-by-token, frame-by-frame).
The wrapper collects the stream; the JNI layer reads the collected result.

**`engine-wrapper.h`**
```cpp
#pragma once
#include "vendor/your-lib/include/your-lib.h"
#include <vector>

struct StreamCtx {
    your_lib_handle_t*    handle = nullptr;
    std::vector<float>    output_buf;
    std::string           last_error;
    bool                  done = false;
};

StreamCtx* stream_create(const char* model_path);
int        stream_push(StreamCtx* ctx, const float* input, int len);
int        stream_step(StreamCtx* ctx);        // advance one step; returns 1 if done
int        stream_output_len(const StreamCtx* ctx);
const float* stream_output_data(const StreamCtx* ctx);
const char*  stream_get_error(const StreamCtx* ctx);
void         stream_free(StreamCtx* ctx);
```

**`engine-wrapper.cpp`**
```cpp
#include "engine-wrapper.h"

StreamCtx* stream_create(const char* model_path) {
    auto* ctx = new StreamCtx{};
    ctx->handle = your_lib_stream_init(model_path);    // ← library
    if (!ctx->handle) ctx->last_error = "init failed";
    return ctx;
}

int stream_step(StreamCtx* ctx) {
    if (!ctx->handle || ctx->done) return -1;
    float sample = 0.0f;
    int rc = your_lib_stream_next(ctx->handle, &sample);  // ← library
    if (rc == YOUR_LIB_DONE) { ctx->done = true; return 1; }
    if (rc != YOUR_LIB_OK)   { ctx->last_error = your_lib_strerror(rc); return -1; }
    ctx->output_buf.push_back(sample);
    return 0;
}

int          stream_output_len(const StreamCtx* ctx)  { return (int)ctx->output_buf.size(); }
const float* stream_output_data(const StreamCtx* ctx) { return ctx->output_buf.data(); }

void stream_free(StreamCtx* ctx) {
    if (!ctx) return;
    if (ctx->handle) your_lib_stream_free(ctx->handle);  // ← library
    delete ctx;
}
```

---

## Pattern 3 — Callback / function pointer

Use when the library calls back into your code (progress callbacks, token callbacks,
frame-ready callbacks). Route the callback to a C-compatible function; never pass a
lambda or `std::function` directly across the boundary.

**`engine-wrapper.h`**
```cpp
#pragma once
#include "vendor/your-lib/include/your-lib.h"

// Callback signature the caller provides
typedef void (*TokenCallback)(const char* token, void* user_data);

struct CallbackCtx {
    your_lib_handle_t* handle    = nullptr;
    TokenCallback      on_token  = nullptr;
    void*              user_data = nullptr;
    std::string        last_error;
};

// C-compatible trampoline — passed to the library as a function pointer
static void token_trampoline(const char* token, void* user_data) {
    auto* ctx = static_cast<CallbackCtx*>(user_data);
    if (ctx->on_token) ctx->on_token(token, ctx->user_data);
}

CallbackCtx* callback_create(const char* model_path,
                              TokenCallback cb, void* user_data);
int          callback_generate(CallbackCtx* ctx, const char* prompt);
const char*  callback_get_error(const CallbackCtx* ctx);
void         callback_free(CallbackCtx* ctx);
```

**`engine-wrapper.cpp`**
```cpp
#include "engine-wrapper.h"

CallbackCtx* callback_create(const char* model_path,
                              TokenCallback cb, void* user_data) {
    auto* ctx      = new CallbackCtx{};
    ctx->handle    = your_lib_init(model_path);          // ← library
    ctx->on_token  = cb;
    ctx->user_data = user_data;
    if (!ctx->handle) ctx->last_error = "init failed";
    return ctx;
}

int callback_generate(CallbackCtx* ctx, const char* prompt) {
    if (!ctx->handle) { ctx->last_error = "not initialised"; return -1; }
    // Pass the trampoline and ctx as user_data — the library calls back into us
    return your_lib_generate(ctx->handle, prompt,
                             token_trampoline, ctx);     // ← library
}

void callback_free(CallbackCtx* ctx) {
    if (!ctx) return;
    if (ctx->handle) your_lib_free(ctx->handle);         // ← library
    delete ctx;
}
```

**JNI side** — store the JVM callback as a global ref; call back on the JNI thread:
```cpp
// engine-jni.cpp
static JavaVM* g_jvm = nullptr;
static jobject g_callback_obj = nullptr;
static jmethodID g_on_token_mid = nullptr;

static void jni_token_trampoline(const char* token, void* /*user_data*/) {
    JNIEnv* env = nullptr;
    g_jvm->AttachCurrentThread(&env, nullptr);
    jstring jtok = env->NewStringUTF(token);
    env->CallVoidMethod(g_callback_obj, g_on_token_mid, jtok);
    env->DeleteLocalRef(jtok);
    // Do NOT DetachCurrentThread if this thread was already attached (e.g. the JNI thread)
}

JNIEXPORT jlong JNICALL Java_..._nativeCreate(JNIEnv* env, jobject,
                                               jstring model, jobject callback) {
    env->GetJavaVM(&g_jvm);
    g_callback_obj = env->NewGlobalRef(callback);
    jclass cls = env->GetObjectClass(callback);
    g_on_token_mid = env->GetMethodID(cls, "onToken", "(Ljava/lang/String;)V");
    const char* path = env->GetStringUTFChars(model, nullptr);
    auto* ctx = callback_create(path, jni_token_trampoline, nullptr);
    env->ReleaseStringUTFChars(model, path);
    return reinterpret_cast<jlong>(ctx);
}
```

---

## Pattern 4 — Multi-step pipeline (two libraries composed)

Use when a task requires output from library A as input to library B (e.g. LLM → TTS).
Each library has its own wrapper. The pipeline wrapper composes them — no algorithm code.

**`pipeline-wrapper.h`**
```cpp
#pragma once
#include "llm-wrapper.h"
#include "tts-wrapper.h"

struct PipelineCtx {
    LlmCtx* llm = nullptr;
    TtsCtx* tts = nullptr;
    std::string last_error;
};

PipelineCtx* pipeline_create(const char* llm_model, const char* tts_model);
// Runs LLM then TTS; output_pcm is float32 PCM; returns sample count or -1
int          pipeline_run(PipelineCtx* ctx, const char* prompt,
                          float** output_pcm, int* sample_rate);
const char*  pipeline_get_error(const PipelineCtx* ctx);
void         pipeline_free(PipelineCtx* ctx);
```

**`pipeline-wrapper.cpp`**
```cpp
#include "pipeline-wrapper.h"

PipelineCtx* pipeline_create(const char* llm_model, const char* tts_model) {
    auto* ctx  = new PipelineCtx{};
    ctx->llm   = llm_create(llm_model);   // ← your wrapper (not the library directly)
    ctx->tts   = tts_create(tts_model);   // ← your wrapper
    if (!ctx->llm || llm_get_error(ctx->llm)[0]) {
        ctx->last_error = llm_get_error(ctx->llm);
    }
    return ctx;
}

int pipeline_run(PipelineCtx* ctx, const char* prompt,
                 float** output_pcm, int* sample_rate) {
    // Step 1: LLM → text
    const char* text = nullptr;
    if (llm_generate(ctx->llm, prompt, &text) != 0) {   // ← wrapper call
        ctx->last_error = llm_get_error(ctx->llm);
        return -1;
    }
    // Step 2: TTS → audio
    int n = tts_synthesize(ctx->tts, text, output_pcm, sample_rate);  // ← wrapper call
    if (n < 0) ctx->last_error = tts_get_error(ctx->tts);
    return n;
}

void pipeline_free(PipelineCtx* ctx) {
    if (!ctx) return;
    llm_free(ctx->llm);   // ← wrapper call
    tts_free(ctx->tts);   // ← wrapper call
    delete ctx;
}
```

---

## Anti-patterns (what NOT to do in a wrapper)

| Wrong | Right |
|---|---|
| Call `your_lib_internal_fn()` not in the public header | Only call functions declared in `include/*.h` |
| Copy the library's source into wrapper.cpp and edit it | Call the library as-is; adapt only input/output shapes |
| Put JNI types (`jstring`, `jbyteArray`) in wrapper.h | Wrapper API uses only plain C types (`const char*`, `float*`, `int`) |
| Return `std::vector` or `std::string` directly | Return raw pointers + length; caller manages lifetime |
| Allocate output inside the wrapper without a matching free | Every allocation exposed across the boundary needs a corresponding `_free_output()` function |
| Put the callback trampoline inside the JNI file | Trampoline lives in wrapper.h/cpp; JNI just provides the JVM-side function |
