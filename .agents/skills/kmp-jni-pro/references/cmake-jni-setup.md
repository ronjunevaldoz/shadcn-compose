# CMake JNI Setup — Including 3rd Party Libraries Without Modifying Them

The correct way to configure a 3rd party library is through CMake variables and
compile definitions — never by editing the library's source or headers.

---

## Directory structure

```
project/
├── CMakeLists.txt               ← root: declares targets, links everything
├── src/
│   ├── engine-wrapper.cpp       ← YOUR code
│   ├── engine-wrapper.h         ← YOUR code
│   └── engine-jni.cpp           ← YOUR code
└── vendor/
    └── your-lib/                ← git submodule or FetchContent — READ ONLY
        ├── CMakeLists.txt
        ├── include/
        │   └── your-lib.h
        └── src/
            └── your-lib.cpp
```

`vendor/` and its contents are never edited. All project code lives in `src/`.

---

## Including a library: three options

### Option A — `add_subdirectory` (submodule or vendored copy)

Use when the library ships its own `CMakeLists.txt` and you've pinned it as a
git submodule or vendored copy.

```cmake
cmake_minimum_required(VERSION 3.22)
project(MyJniLib)

# Include the library as a subdirectory — its CMakeLists.txt runs,
# defining its own targets (e.g. target name: 'your_lib')
add_subdirectory(vendor/your-lib)

# Your JNI shared library — only YOUR source files
add_library(my-jni SHARED
    src/engine-wrapper.cpp
    src/engine-jni.cpp
)

# Link against the library's CMake target (not a file path)
target_link_libraries(my-jni PRIVATE your_lib)

# Include the library's public headers — no editing needed
target_include_directories(my-jni PRIVATE vendor/your-lib/include)
```

### Option B — `FetchContent` (download at configure time)

Use when you don't want to vendor the source, or need a specific version by tag.

```cmake
include(FetchContent)

FetchContent_Declare(
    your_lib
    GIT_REPOSITORY https://github.com/org/your-lib.git
    GIT_TAG        v2.1.0        # pin to a tag, never use main/HEAD
)

# Pass CMake options to the library WITHOUT editing its source
set(BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(your_lib)

add_library(my-jni SHARED src/engine-wrapper.cpp src/engine-jni.cpp)
target_link_libraries(my-jni PRIVATE your_lib)
```

### Option C — Prebuilt `.so` / `.a` (binary distribution)

Use when the library is distributed as a binary (e.g. ONNX Runtime, TensorFlow Lite).

```cmake
# Declare the prebuilt library as an imported target
add_library(your_lib SHARED IMPORTED)
set_target_properties(your_lib PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/arm64-v8a/libyour-lib.so
    INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/include
)

add_library(my-jni SHARED src/engine-wrapper.cpp src/engine-jni.cpp)
target_link_libraries(my-jni PRIVATE your_lib)
```

---

## Passing configuration to a library — the CMake-correct way

**Never** edit a `#define` in a library header to configure it.
Use CMake cache variables or compile definitions instead.

```cmake
# WRONG — editing vendor/your-lib/include/config.h to change a constant
# (don't do this — it modifies a 3rd party file)

# RIGHT — pass the flag via CMake before add_subdirectory:
set(YOUR_LIB_ENABLE_GPU ON CACHE BOOL "" FORCE)
set(YOUR_LIB_MAX_THREADS 4 CACHE INT "" FORCE)
add_subdirectory(vendor/your-lib)

# Or define a compile-time constant only in YOUR wrapper, not in the library:
target_compile_definitions(my-jni PRIVATE
    MY_WRAPPER_BATCH_SIZE=8
    MY_WRAPPER_LOG_LEVEL=2
)
```

---

## Turning features on/off without touching the library

Most libraries expose build-time options as CMake `option()` or `set(... CACHE ...)`.
Find them with:

```bash
grep -n "^option\|^set.*CACHE" vendor/your-lib/CMakeLists.txt
```

Set them before `add_subdirectory` / `FetchContent_MakeAvailable`:

```cmake
# Disable what you don't need to keep the .so small and avoid symbol conflicts
set(LLAMA_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
set(GGML_METAL           ON  CACHE BOOL "" FORCE)   # enable Metal GPU

add_subdirectory(vendor/llama.cpp)
```

---

## Avoiding Dockerfile / build-script omissions (EP-7 prevention)

After adding a new CMake target:

```bash
# Verify the flag is ON in the build script / Dockerfile
grep "BUILD_MY_JNI" Dockerfile scripts/build.sh CMakeLists.txt

# Verify the .so is actually COPY'd into the runtime image
grep "my-jni\|libmy-jni" Dockerfile
```

The pattern that causes EP-7 ("built with X=OFF"):

```dockerfile
# WRONG — new target is OFF by default and forgotten
RUN cmake .. \
    -DBUILD_EXISTING_JNI=ON \
    -DBUILD_MY_NEW_JNI=OFF       # ← someone added this default, forgot to flip it

# RIGHT — every JNI target has an explicit flag
RUN cmake .. \
    -DBUILD_EXISTING_JNI=ON \
    -DBUILD_MY_NEW_JNI=ON         # ← explicit; easy to audit
```

---

## Keeping project sources clearly separated from library sources

CMake's `target_sources` scoping enforces the boundary at build time:

```cmake
# Define your project's sources explicitly — never glob into vendor/
add_library(my-jni SHARED)
target_sources(my-jni PRIVATE
    src/engine-wrapper.cpp   # yours
    src/engine-jni.cpp       # yours
    # DO NOT add vendor/**/*.cpp here
)
```

If you find yourself needing to add a `vendor/` `.cpp` to your `target_sources`, that
is a sign you are trying to modify or extend library behaviour from inside the library.
Stop — write an adapter in `engine-wrapper.cpp` instead.

---

## Checklist before any CMake change

- [ ] Is every `vendor/` path in the CMakeLists.txt referenced only by `add_subdirectory`, `target_include_directories`, or `target_link_libraries`? Never by `target_sources`.
- [ ] Are all library feature flags set via CMake cache variables before `add_subdirectory`, not via `#define` in headers?
- [ ] Does the Dockerfile/build script have an explicit `-DBUILD_X=ON` for every new JNI target?
- [ ] Is the new `.so` copied into the runtime image with an explicit `COPY` instruction?
- [ ] Can the project build from a clean clone without any manual edits to vendor files?
