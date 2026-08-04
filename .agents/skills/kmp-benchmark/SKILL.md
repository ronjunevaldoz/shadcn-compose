---
name: kmp-benchmark
description: >
  Sets up kotlinx-benchmark for Kotlin Multiplatform performance measurement. Covers:
  Gradle plugin + allopen wiring, a separate benchmark source set (never mixed into
  commonMain/commonTest), @State/@Benchmark/@Setup/@TearDown conventions, per-target
  registration (JVM/Native/JS/Wasm), running benchmarks, and reading JSON/CSV output.
  Use this whenever a performance claim needs a real number instead of a guess — this is
  the "profile first" step the kmp-expert performance decision tree
  routes to when a request is unnamed or needs a concrete target before further routing.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-10'
  keywords:
    - kotlinx-benchmark
    - benchmark
    - microbenchmark
    - performance measurement
    - JMH
    - allopen
    - Benchmark annotation
    - profile performance
    - performance number
---

## When to Use This Skill

Use when you need to:
- Get a real, reproducible number before claiming a change "improves performance"
- Compare two implementations of the same function (e.g. two JSON parsers, two collection strategies)
- Track a regression across releases with a committed baseline
- Answer `kmp-expert`'s performance decision tree fallback: "profile first
  to get a concrete target"

**Trigger keywords:** benchmark, microbenchmark, kotlinx-benchmark, performance number,
measure performance, profile this, @Benchmark, JMH, is this faster, compare performance,
performance regression, benchmark baseline.

**Freshness rule:** `kotlinx-benchmark` version, the `allopen` annotation target, and
per-target registration syntax change between releases — recheck the
[kotlinx-benchmark docs](https://github.com/Kotlin/kotlinx-benchmark/tree/master/docs)
before pinning versions or copying setup code from this skill.

---

## Recommendation First

Default to a **dedicated `benchmark` source set, never commonMain or commonTest**, using
`kotlinx.benchmark.*` annotations (not `org.openjdk.jmh.annotations.*` directly) so the
same benchmark compiles across every registered target, not just JVM.

Why:
- A benchmark source set that `associateWith` the main compilation gets internal-API
  access without shipping benchmark code (or its JMH-generated subclasses) in the real
  artifact
- `kotlinx.benchmark.State`/`@Benchmark` are KMP-portable; `org.openjdk.jmh.annotations.*`
  only compiles on the JVM target — using JMH's own annotations silently locks the
  benchmark to one target
- Kotlin classes are `final` by default; JMH-style benchmarking needs the class open to
  generate subclasses — `allopen` handles this without hand-adding `open` everywhere

---

## Prerequisites

- Project scaffolded with `kmp-feature-scaffold`
- Kotlin 2.2.0+, Gradle 8+
- A concrete function/class to measure — never scaffold a benchmark module speculatively;
  see Common Anti-Patterns

---

## Step 1: Gradle plugin + allopen setup

### `libs.versions.toml`

```toml
[versions]
kotlinx-benchmark = "0.4.17"

[libraries]
kotlinx-benchmark-runtime = { module = "org.jetbrains.kotlinx:kotlinx-benchmark-runtime", version.ref = "kotlinx-benchmark" }

[plugins]
kotlinx-benchmark = { id = "org.jetbrains.kotlinx.benchmark", version.ref = "kotlinx-benchmark" }
kotlin-allopen = { id = "org.jetbrains.kotlin.plugin.allopen", version.ref = "kotlin" }
```

### Module `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    // Opens classes annotated with kotlinx.benchmark.State — required because Kotlin
    // classes are final by default and the benchmark runner needs to generate
    // subclasses. Use the kotlinx.benchmark annotation, not org.openjdk.jmh's, so this
    // works for every registered target, not just JVM.
    annotation("kotlinx.benchmark.State")
}

kotlin {
    jvm()
    // Add other targets you actually benchmark — see Step 3. Don't register a target
    // here "for completeness" if nothing runs there; see Common Anti-Patterns.

    sourceSets {
        commonMain.dependencies {
            // your library code
        }
        val commonBenchmark by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
            }
        }
        val jvmBenchmark by creating {
            dependsOn(commonBenchmark)
        }
    }
}

benchmark {
    targets {
        register("jvmBenchmark") // matches the target name; registers its compilation
    }
    configurations {
        named("main") {
            warmups = 10
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
        }
    }
}
```

---

## Step 2: Writing a benchmark

Place benchmark files in `src/commonBenchmark/kotlin/` (or the platform-specific
benchmark source set if the code under test is platform-specific).

```kotlin
// src/commonBenchmark/kotlin/GROUP_ID/JsonParseBenchmark.kt
package GROUP_ID

import kotlinx.benchmark.*

@State(Scope.Benchmark)
class JsonParseBenchmark {
    private lateinit var payload: String

    @Setup
    fun setup() {
        // Build fixture data ONCE per benchmark run, not per-iteration — @Setup runs
        // before the timed loop, so this cost is never measured.
        payload = buildLargeJsonFixture()
    }

    @Benchmark
    fun parseWithKotlinxSerialization(): User = Json.decodeFromString(payload)

    @Benchmark
    fun parseWithHandRolledParser(): User = HandRolledJsonParser.parse(payload)

    @TearDown
    fun tearDown() {
        // Release large fixtures explicitly if they'd otherwise skew GC pressure
        // across repeated iterations within the same JVM fork.
    }
}
```

**Rules:**
- `@State(Scope.Benchmark)` — one instance shared across all invocations in a benchmark
  run; use `Scope.Thread` only if the state must not be shared across benchmark threads
- `@Setup`/`@TearDown` — fixture cost never counts toward the measured result
- Name benchmark functions for what they measure (`parseWithKotlinxSerialization`), not
  generic names (`test1`, `benchmarkA`) — the report is only useful if the function name
  says what was measured

---

## Step 3: Multi-target registration

Register one `benchmark { targets { register(...) } }` entry per Kotlin target you
actually measure — the registered name must match an existing compilation:

```kotlin
kotlin {
    jvm()
    linuxX64()
    js(IR) { nodejs() }

    sourceSets {
        val commonBenchmark by creating { dependsOn(commonMain.get()) }
        val jvmBenchmark by creating { dependsOn(commonBenchmark) }
        val linuxX64Benchmark by creating { dependsOn(commonBenchmark) }
        val jsBenchmark by creating { dependsOn(commonBenchmark) }
    }
}

benchmark {
    targets {
        register("jvmBenchmark")
        register("linuxX64Benchmark")
        register("jsBenchmark")
    }
}
```

Native and JS results are **not directly comparable to JVM** — different runtimes, no
JIT warmup on Native, different GC (or none). Compare JVM-to-JVM and Native-to-Native;
use cross-target numbers only to confirm a target-specific regression, not to rank
targets against each other.

---

## Step 4: Running and reading results

```bash
./gradlew jvmBenchmarkBenchmark      # one target
./gradlew benchmark                  # all registered targets
```

Default output is JSON (JMH-compatible format) under
`build/reports/benchmarks/main/`. Configure `reportFormat = "csv"` in the
`configurations { named("main") { ... } }` block if you want CSV instead.

**Where results go in docs — do not paste raw output into a task note.** Per
`kmp-project-docs-maintainer`'s existing rule: write the canonical
comparison table in `docs/reference/benchmark-matrix.md` (or the nearest durable
`docs/reference/` page), and keep any task-note summary short with a link back. Commit
the benchmark source alongside the result so a future run can reproduce the number.

---

## Common Anti-Patterns

- Using `org.openjdk.jmh.annotations.*` directly instead of `kotlinx.benchmark.*` — compiles on JVM only, silently breaks Native/JS targets
- Putting benchmark code in `commonMain` or `commonTest` — ships benchmark code (and its JMH-generated subclasses on JVM) in the real artifact; always a dedicated benchmark source set
- Measuring fixture setup inside `@Benchmark` instead of `@Setup` — the timed result includes work that isn't the thing being measured
- Registering every possible target "for completeness" with nothing to actually measure there — extra compile time and CI cost for numbers nobody reads
- Comparing a JVM benchmark result directly against a Native or JS result — different runtimes; only comparable within the same target
- Pasting raw benchmark JSON/CSV into a task note instead of the canonical `docs/reference/benchmark-matrix.md` table
- Running a benchmark once and treating the number as final — rerun with the configured warmup/iteration counts; a single run is noise, not a result
- Scaffolding a benchmark module before there's a concrete function/claim to measure — benchmark the actual bottleneck found via profiling or a real performance complaint, not speculatively

---

## Testing

`kotlinx-benchmark` output is itself the test signal — there's no separate unit-test
layer for a benchmark module. Validate the setup, not the numbers:

- `./gradlew :module:jvmBenchmarkBenchmark` completes without a compile error and
  produces a JSON report under `build/reports/benchmarks/`
- Each `@Benchmark` function's name states what it measures
- `@Setup` fixture cost is excluded from the reported number (spot-check by comparing
  two runs with and without fixture-building moved into `@Benchmark` temporarily)
- The `docs/reference/benchmark-matrix.md` table is updated in the same change that adds
  or updates a benchmark

---

## Output Style

When asked to benchmark something, respond in this order:
1. Confirm there's a concrete function/claim to measure — ask if not (see Anti-Patterns)
2. Gradle setup (plugin, allopen, source set) — only the parts not already present
3. The `@Benchmark` class for the specific comparison requested
4. The run command and where the report lands
5. Where the result belongs in docs (`docs/reference/benchmark-matrix.md`)

---

## Related Skills

- `kmp-expert` — the "Improve the performance of X" decision tree routes
  here when X is unnamed or needs a concrete number before further routing
- `kmp-project-docs-maintainer` — owns the `docs/reference/benchmark-matrix.md`
  placement rule this skill's output follows
- `kmp-jni-pro` — JNI boundary-crossing cost claims should be backed by
  a benchmark from this skill, not an assumption
- `kmp-code-quality` — Detekt's complexity rules flag *style* smells;
  this skill answers whether a change is actually *faster*, a different question

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-10 | Initial release — kotlinx-benchmark Gradle/allopen setup, dedicated benchmark source set convention, @State/@Benchmark/@Setup/@TearDown usage, multi-target registration, JVM-vs-Native-vs-JS comparability caveat, docs/reference/benchmark-matrix.md placement wiring. Closes the "profile first" dead-end in kmp-expert's performance decision tree. |
