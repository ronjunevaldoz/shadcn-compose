# Confirmed JNI Bug Patterns — Never Reproduce

These were root-caused in real production pipelines. Each entry states what went wrong,
why it was hard to detect, and the rule that prevents it.

---

## EP-1 — Algorithm reimplementation with parameter drift

**What happened**: A C++ JNI wrapper reimplemented an ISTFT (audio reconstruction) by
copying the FFT parameters from the reference library's source but rewriting the logic.
The rewrite missed peak normalisation. Result: every output WAV was clipped to digital
full-scale (`max_volume = -0.0 dB` on every file), producing square-wave distortion.

**Why hard to detect**: The code compiled cleanly. The function returned audio samples.
The distortion required audio analysis (`ffprobe volumedetect`) to confirm — a casual
listen is not enough because clipped speech is still intelligible.

**Rule**: Before writing any algorithm in a JNI wrapper, `grep -r "function_name" <library_source>/`.
If it exists as a CLI tool or sample: port it verbatim with a comment citing the source file
and line numbers. Do not rewrite from first principles.

---

## EP-2 — Wrong Hann window type (symmetric vs periodic)

**What happened**: The same ISTFT reimplementation used a symmetric Hann window
(`denominator = n-1`) instead of a periodic one (`denominator = n`). The periodic window
satisfies the Constant Overlap-Add (COLA) condition at 25% hop. The symmetric window
does not — `norm[i]` is not constant across the output buffer, introducing amplitude
ripple.

**Why hard to detect**: The ripple is subtle (~0.1–0.5%). The audio sounds "slightly off"
but the defect is not obvious without an oscilloscope or spectrogram. The parameters
look nearly correct (`n` vs `n-1` is easy to miss in a code review).

**Rule**: When porting windowing functions, verify the denominator against the reference
implementation and the mathematical requirement (COLA for OLA, von Hann periodicity for
STFT). These are not interchangeable.

---

## EP-3 — Wrong constant default not verified against library header

**What happened**: A Kotlin engine class set `defaultLanguageId = 0` for a TTS codec.
The library's actual default for English was `2050`. ID `0` is not a valid language entry.
The codec silently produced garbled audio without throwing an exception, so the wrong
output reached the caller with no error signal.

**Why hard to detect**: No crash, no exception, no log. The output was audio — just wrong.
Required reading the library header (`qwen3_tts.h:42`) to discover the correct value.

**Rule**: Every constant that is passed to a native library must be verified against the
library's own header or source, not guessed or defaulted to 0/1/-1. Add a comment citing
the source: `// 2050 = English; see qwen3_tts.h:42`.

---

## EP-4 — `n_embd` vs `n_embd_out` stride mismatch

**What happened**: A wrapper called `model_n_embd(model)` (hidden/internal embedding dim)
to compute the per-frame stride when reading encoder output embeddings. The correct call
is `model_n_embd_out(model)` (the output embedding dim). Using the wrong value shifts
every frame by a fixed offset, silently corrupting the entire output.

**Why hard to detect**: The code produced audio (or image data) — just wrong. The shape
mismatch does not raise an error in the native library. The output looks structurally
valid (correct length, non-zero values) but the content is garbage.

**Rule**: For encoder models, always use `n_embd_out` when indexing into embeddings
returned by the library. Read the reference implementation's embedding loop to confirm
which dimension it uses.

---

## EP-5 — Missing GPU synchronise before reading output

**What happened**: A wrapper read inference output (logits or embeddings) immediately
after `llama_decode` / `llama_encode` without calling `llama_synchronize`. On CPU builds
this was fine. On Metal/CUDA builds, the GPU kernels had not finished writing. Result:
intermittent garbage output on GPU, correct output on CPU — a platform-dependent race.

**Why hard to detect**: Tests run on CPU pass. The bug only appears on GPU builds. The
output is not always wrong — only when the GPU falls behind, which is timing-dependent.

**Rule**: After any inference call that may execute on GPU, call the library's synchronise
function before reading any output. Check the reference CLI tool — it almost always does this.

---

## EP-6 — `ReleaseStringUTFChars` missing on error path

**What happened**: A JNI function acquired a `jstring` with `GetStringUTFChars`, then
returned early (throwing an exception) on a later error without releasing the string.
Each failed call leaked the string's native buffer.

**Why hard to detect**: Memory leaks are slow. The process ran fine under test load.
Under sustained production load the JVM heap grew until GC pressure became visible.
`GetStringUTFChars` is not tracked by JVM GC — it is native memory.

**Rule**: Treat every `GetStringUTFChars` like a file open. Use RAII or a goto-cleanup
pattern to guarantee release on every exit path including exception throws.

---

## EP-7 — JNI library built with `BUILD_X=OFF` in Docker, never copied

**What happened**: A CMakeLists.txt had a complete, correct `BUILD_QWEN3_TTS_JNI` block.
The Dockerfile passed `-DBUILD_QWEN3_TTS_JNI=OFF` on every CMake invocation and had no
COPY instruction for the output. At runtime `System.loadLibrary("qwen3-jni")` threw
`UnsatisfiedLinkError`. The server silently fell back to a secondary engine.

**Why hard to detect**: The fallback worked. No crash, no visible error in happy-path
testing. The wrong engine was used in production for an extended period.

**Rule**: After adding a CMake target, immediately check the Dockerfile (or build script)
for every `-DBUILD_X=OFF` flag and every COPY instruction. Confirm the new `.so` is
compiled AND copied into the runtime image. Add a startup log that prints which engine
loaded.

---

## EP-8 — RTLD_GLOBAL symbol conflict between two shared libraries

**What happened**: Two JNI libraries were loaded in the same JVM. Both dynamically
depended on `libggml.so` — but from different builds (different versions of the same
upstream library). `System.loadLibrary` calls `dlopen` with `RTLD_GLOBAL`, exporting the
first library's `ggml_*` symbols into the global namespace. The second library's
`ggml_*` references resolved to the first library's code. If the two ggml versions had
different internal struct layouts, the result was crashes or silent data corruption.

**Why hard to detect**: On matching versions, it works. The bug appears only after a
version bump of one of the libraries. Crash stacks point into ggml internals with no
indication of which caller caused the conflict.

**Rule**: Before adding a second JNI library that depends on the same underlying C library
as an existing one, check for symbol overlap: `nm -D lib_a.so | grep 'T symbol'` vs
`nm -D lib_b.so | grep 'U symbol'`. Resolve conflicts by: (1) building both against the
same CMake targets, (2) statically linking the dependency into the JNI `.so`, or
(3) running in separate processes.

---

## EP-9 — Editing 3rd party library `.cpp` or `.h` files directly

**What happened**: An agent editing a JNI wrapper also modified a header or source file
inside `third_party/` (e.g. added a field to a library struct, changed a `#define` in the
library's public header, or patched a function body in `vendor/lib.cpp`). The modification
was lost the next time the submodule was updated or the external project was re-fetched,
and the build broke silently on a fresh clone. In some cases the modification changed the
ABI of a struct shared between the JNI wrapper and the native library, producing crashes
that were extremely difficult to trace.

**Why hard to detect**: The change compiles and works locally. It only fails when:
(a) another developer clones and builds without the modification,
(b) the submodule is bumped,
(c) the CMake FetchContent re-downloads the source.
The connection between "submodule update" and "new crash" is non-obvious.

**Rule**: 3rd party files — any file under `vendor/`, `third_party/`, `external/`, or
managed by a git submodule / `ExternalProject_Add` — are **read-only**. All adaptations
go into project-owned `*-wrapper.cpp` / `*-wrapper.h`. If a `#define` or constant is
needed, define it in your own header before including the library header. If the library
has a genuine bug, document the workaround in the wrapper and file an upstream issue.
