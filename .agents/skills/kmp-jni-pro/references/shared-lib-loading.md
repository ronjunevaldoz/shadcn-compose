# Shared Library Loading — JVM, RTLD_GLOBAL, and Symbol Conflicts

## How the JVM loads native libraries

```kotlin
System.loadLibrary("mylib")   // searches java.library.path
System.load("/abs/path/to/libmylib.so")  // absolute path
```

Both call `dlopen(path, RTLD_LAZY | RTLD_GLOBAL)` on Linux/macOS.

**`RTLD_GLOBAL`** exports every symbol from the loaded `.so` into the process-wide
global symbol namespace. This means:

1. Library A is loaded → its symbols (`foo`, `bar`) enter the global namespace.
2. Library B is loaded → the dynamic linker finds `foo` and `bar` already in the global
   namespace and binds B's references to A's implementations.
3. B never loads its own copy of `foo` / `bar`, even if it ships one.

## When this is safe

If A and B depend on the same library at the same version and ABI, sharing symbols is
fine — it saves memory and avoids duplicate global state.

## When this causes bugs

If A and B depend on the same library at **different versions** (e.g., both vendor a
copy of `libggml.so` at different commits), the second loaded library silently uses the
first's code. If internal struct layouts changed between versions, the result is:
- Crashes in `ggml_*` functions with no clear stack trace
- Silent data corruption (wrong tensor shapes, wrong strides)
- Intermittent failures that only appear after version bumps

## Detection

```bash
# Which symbols does lib_a export?
nm -D lib_a.so | grep ' T '

# Which symbols does lib_b need from the outside (undefined)?
nm -D lib_b.so | grep ' U '

# Overlap = conflict candidates
comm -12 \
  <(nm -D lib_a.so | grep ' T ' | awk '{print $3}' | sort) \
  <(nm -D lib_b.so | grep ' U ' | awk '{print $3}' | sort)
```

If the overlap is non-empty and the two libraries use different versions of the shared
dependency, you have a conflict.

## Resolution options (in order of preference)

### Option 1 — Single CMake invocation (preferred)

Build both JNI targets in the same CMake run. Both `add_subdirectory(lib_a_src)` and
`add_subdirectory(lib_b_src)` will see the same CMake targets. The shared dependency
is built once; both JNI `.so` files link against the same output.

```cmake
# One CMakeLists.txt, both targets ON
add_subdirectory(liba_src)   # defines target: ggml
add_subdirectory(libb_src)   # links against the same 'ggml' target
add_library(jni-a SHARED jni-a.cpp)
target_link_libraries(jni-a PRIVATE liba)
add_library(jni-b SHARED jni-b.cpp)
target_link_libraries(jni-b PRIVATE libb)
```

### Option 2 — Statically link the shared dependency

Build the common dependency as a static library and link it into each JNI `.so` with
`--whole-archive`. Each `.so` is self-contained. Symbols are not exported globally.

```cmake
set(BUILD_SHARED_LIBS OFF)
add_subdirectory(common_dep)
target_link_libraries(jni-b PRIVATE -Wl,--whole-archive common_dep -Wl,--no-whole-archive)
```

Downside: increases `.so` size; two copies of the dependency in memory.

### Option 3 — Rename symbols (nuclear option)

Use `objcopy --redefine-syms` or `--localize-hidden` to give the second library's
dependency symbols a unique prefix. Requires a symbol map file.

### Option 4 — Separate processes

Run each native engine in its own process and communicate via IPC (Unix socket, pipe,
shared memory). Zero risk of symbol conflict. Higher complexity and latency.

## Load order in Kotlin

If you must load two libraries that risk conflict, load the shared dependency
**explicitly first** under the name that both will resolve to:

```kotlin
// Force the shared dep to be the one you want, before either JNI lib loads
System.load("/app/lib/libggml.so")   // pin the version
System.loadLibrary("jni-a")
System.loadLibrary("jni-b")
```

This still uses a single copy; it just gives you control over which version wins.

## SONAME symlinks

The runtime linker on Linux looks for versioned names (e.g., `libggml.so.0`).
If the library was built without a SONAME, or the SONAME does not match what the
linker expects, the library fails to load with `cannot open shared object file`.

Fix: create symlinks in the deploy directory:
```bash
ln -s libggml.so libggml.so.0
ln -s libggml-base.so libggml-base.so.0
```

Or set SONAME at build time:
```cmake
set_target_properties(ggml PROPERTIES SOVERSION 0 VERSION 0.9.6)
```
