---
name: kmp-network-layer
description: >
  Scaffolds a production-ready Ktor 3 network layer inside :core:network for a
  Kotlin Multiplatform project. Covers: platform engines (OkHttp/Darwin/CIO/Js)
  for Android, iOS, Desktop (JVM), and Web (JS + WasmJs), ContentNegotiation,
  Bearer auth with automatic token refresh (race-condition-safe), structured error
  mapping to a NetworkResult<T> sealed type, request/response logging, and Koin
  wiring. Assumes the project was scaffolded with kmp-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
  keywords:
    - Ktor 3
    - KMP networking
    - token refresh
    - Bearer auth
    - NetworkResult
    - Kotlin Multiplatform
    - OkHttp
    - Darwin
    - CIO
    - desktop
    - web
    - JS
    - WasmJs
---

## Overview

This skill populates `:core:network` with a complete, production-grade HTTP client setup:

```
:core:network
  commonMain
    HttpClient factory (Ktor plugins: Auth, ContentNegotiation, Logging, HttpTimeout)
    NetworkResult<T>          sealed type (Success / HttpError / NetworkError)
    NetworkError              typed error hierarchy
    TokenStorage              interface (platform-agnostic token persistence)
    safeRequest {}            extension — executes a call, maps to NetworkResult<T>
    di/NetworkModule          Koin module
  androidMain
    HttpClientEngineFactory   → OkHttp
  iosMain
    HttpClientEngineFactory   → Darwin
  jvmMain
    HttpClientEngineFactory   → CIO  (Desktop)
    jsMain
      HttpClientEngineFactory   → Js   (Web browser)
    wasmJsMain
      HttpClientEngineFactory   → Js   (Web/Wasm browser)
```

## When to Use This Skill

Use this skill when you need to:
- Add a shared Ktor HTTP client to a KMP project
- Model transport results with `NetworkResult<T>`
- Configure bearer auth with automatic token refresh
- Split platform engines across Android, iOS, Desktop, Web, and WasmJs
- Call a **third-party REST API** or any backend that is not a Kotlin-first Ktor server

**Do NOT use this skill** when:
- The project already has a kRPC transport for the backend you are calling — use
  `kmp-kotlin-rpc` and extend the existing service interface instead
  of adding a parallel `safeRequest` call to the same server
- Run the kRPC pre-check first:
  ```bash
  grep -r "RemoteService\|@Rpc\|withRpc\|KtorRPCClient\|rpcClient\|\.rpc(" \
    <project_root>/*/src --include="*.kt" -l
  ```
  If this returns matches, treat kRPC as the default transport for that backend.

**Trigger keywords:** network layer, Ktor client, HTTP client, bearer auth,
token refresh, NetworkResult, safeRequest, OkHttp, Darwin, CIO, JS engine,
API call, HTTP request, REST API, make a request, fetch data, network call,
call API, REST client, HTTP interceptor, API client setup, network error handling,
data fetching, content retrieval, remote data, backend integration, API integration,
data redundancy, content synchronization, remote content,
handle errors, error handling, error state, catch errors, HTTP error,
network error, request error, response error, retry, handle failure.

**Freshness rule:** Ktor changes quickly, so recheck the current client engine,
auth, and serialization docs before using or updating this skill.

---

## Recommendation First

Default to **Ktor CIO engine + `NetworkResult<T>` sealed type + `safeRequest` extension**.

Why:
- `NetworkResult` keeps error handling explicit at call sites — no uncaught exceptions leaking into UI
- `safeRequest` centralizes serialization, timeout, and HTTP error handling in one place
- CIO engine works on Android, Desktop, and Web; swap to Darwin engine for iOS if needed

Use a different approach only when an existing REST abstraction is already in the codebase and
migration cost outweighs the consistency benefit.

---

## Step 0 — Detect an existing network layer before scaffolding or writing raw HTTP

**Never assume `:core:network` is the module name.** Real projects name it differently
(`:core:api`, `:shared:networking`, `:data:remote`, a project-specific name from before
this skill was applied). Checking only for a module literally named `:core:network` and
falling through to a hand-written `HttpClient`/raw platform HTTP call when that exact
path doesn't exist is a real bug this skill has caused — a new feature or server module
with a different name should still reuse the project's existing Ktor client, not bypass
it.

Before scaffolding anything or writing a network call, grep the whole project for an
existing Ktor client **by content, not by path**:

```bash
grep -rl "HttpClient(\|io\.ktor\.client\.\|safeRequest\|NetworkResult<" \
  <project_root>/*/src --include="*.kt"
```

- **Matches found** → an established client already exists. Read that module's actual
  path and package, reuse its `HttpClient`/`safeRequest`/`NetworkResult<T>` for the new
  server module or feature, and do not scaffold a second client. A new server with a
  different module name is still the same transport concern — extend the existing setup
  (new base URL / new service interface), never a parallel raw `java.net.HttpURLConnection`,
  `NSURLSession`, platform `fetch()`, or a second unrelated `HttpClient(...)` instance.
- **No matches** → this is genuinely a fresh project; proceed with Step 1 below using
  `:core:network` as the default name, or the project's own established core-module
  naming convention if one is already visible in `settings.gradle.kts`.

This mirrors `kmp-compose-adaptive-layout`'s cross-session pattern-consistency
check — grep for the real signal before assuming a fixed path.

---

## Prerequisites

- Project scaffolded with `kmp-feature-scaffold`
- Step 0 above found no existing client — if one exists under a different name, extend
  it instead of following the rest of this skill
- `:core:network` module exists and applies `GROUP_ID.core` convention plugin (or
  whatever name Step 0's detection confirmed, or the project's own naming convention for
  a brand-new module)
- `libs.versions.toml` contains Ktor entries (see version reference below)

---

## Version Reference

```toml
ktor                 = "3.5.0"
```

Verify these libraries exist in `libs.versions.toml`:
- `ktor-client-core`, `ktor-client-android`, `ktor-client-darwin`
- `ktor-client-cio` (Desktop/JVM), `ktor-client-js` (Web/JS + WasmJs)
- `ktor-client-logging`, `ktor-client-contentNegotiation`
- `ktor-serialization-json`, `ktor-client-mock`
- Bundle: `ktor-common`

---

## Step 1: Update `:core:network/build.gradle.kts`

The `GROUP_ID.core` convention plugin already adds all targets. Confirm the module build file:

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.GROUP_ID.core)
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.core.network"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.android)   // OkHttp engine
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)    // URLSession engine
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)       // CIO engine (Desktop)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)        // Js engine (Web)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)        // Js engine (WasmJs)
        }
    }
}
```

---

## Step 2: NetworkResult sealed type

Create `src/commonMain/kotlin/GROUP_ID/core/network/NetworkResult.kt`:

```kotlin
package GROUP_ID.core.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val cause: Throwable) : NetworkResult<Nothing>()
}

inline fun <T> NetworkResult<T>.onSuccess(block: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) block(data)
    return this
}

inline fun <T> NetworkResult<T>.onHttpError(block: (code: Int, message: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.HttpError) block(code, message)
    return this
}

inline fun <T> NetworkResult<T>.onNetworkError(block: (Throwable) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.NetworkError) block(cause)
    return this
}

fun <T> NetworkResult<T>.getOrNull(): T? = (this as? NetworkResult.Success)?.data

fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.HttpError -> this
    is NetworkResult.NetworkError -> this
}
```

---

## Step 3: TokenStorage interface

Create `src/commonMain/kotlin/GROUP_ID/core/network/TokenStorage.kt`:

```kotlin
package GROUP_ID.core.network

interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}
```

> **Platform note**: Implement `TokenStorage` per platform:
> - Android: `EncryptedSharedPreferences` or `DataStore`
> - iOS: `NSUserDefaults` (dev) or `Keychain` (prod)
>
> Register the implementation in each platform's Koin module, not in `:core:network`.

---

## Step 4: Platform engine factory

Full content: `references/step4-platform-engine-factory.md`.

## Step 5: HttpClient factory

Full content: `references/step5-httpclient-factory.md`.

## Step 6: safeRequest extension

Create `src/commonMain/kotlin/GROUP_ID/core/network/SafeRequest.kt`:

```kotlin
package GROUP_ID.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request

/**
 * Executes an HTTP request and maps the result to [NetworkResult].
 * Catches Ktor's [ResponseException] for HTTP errors and all other
 * [Throwable]s as [NetworkResult.NetworkError].
 */
suspend inline fun <reified T> HttpClient.safeRequest(
    block: HttpRequestBuilder.() -> Unit
): NetworkResult<T> = try {
    val response = request(block)
    NetworkResult.Success(response.body<T>())
} catch (e: ResponseException) {
    NetworkResult.HttpError(
        code = e.response.status.value,
        message = e.response.status.description
    )
} catch (e: Exception) {
    NetworkResult.NetworkError(cause = e)
}
```

---

## Step 7: Koin module

Create `src/commonMain/kotlin/GROUP_ID/core/network/di/NetworkModule.kt`:

```kotlin
package GROUP_ID.core.network.di

import GROUP_ID.core.network.createHttpClient
import org.koin.dsl.module

/**
 * Provides the shared [HttpClient].
 *
 * Consumers must provide a [TokenStorage] binding — typically registered
 * in the Android/iOS platform module in :androidApp or the shared App module.
 *
 * Usage in :androidApp Koin setup:
 *   modules(networkModule(baseUrl = BuildKonfig.BASE_URL, enableLogging = BuildKonfig.DEBUG))
 */
fun networkModule(
    baseUrl: String,
    enableLogging: Boolean = false,
    onRefreshFailed: suspend () -> Unit = {},
) = module {
    single {
        createHttpClient(
            baseUrl = baseUrl,
            tokenStorage = get(),
            onRefreshFailed = onRefreshFailed,
            enableLogging = enableLogging,
        )
    }
}
```

---

## Step 8: Wire in :androidApp

In your `Application` class or Koin init:

```kotlin
startKoin {
    androidContext(this@App)
    modules(
        networkModule(
            baseUrl = BuildKonfig.BASE_URL,
            enableLogging = BuildKonfig.DEBUG,
            onRefreshFailed = { /* navigate to login */ }
        ),
        // platform TokenStorage impl
        module { single<TokenStorage> { DataStoreTokenStorage(get()) } },
        // feature modules...
    )
}
```

---

## Step 9: Usage in :feature:*:data

```kotlin
// In a repository implementation
class AuthRepositoryImpl(
    private val client: HttpClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): NetworkResult<User> =
        client.safeRequest {
            post("/auth/login")
            setBody(LoginRequest(email, password))
        }
}
```

---

## Step 10: Testing with mock engine

In `:feature:*:data` commonTest, use `MockEngine`:

```kotlin
@Test
fun `login returns Success on 200`() = runTest {
    val engine = MockEngine { request ->
        respond(
            content = """{"id":"1","name":"Ron"}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    val client = HttpClient(engine) {
        install(ContentNegotiation) { json() }
    }
    val result = client.safeRequest<User> { post("/auth/login") }
    assertTrue(result is NetworkResult.Success)
}
```

---

## Step 11: Server-Sent Events (SSE) for real-time server push

Full content: `references/step11-sse.md`.

## Guidelines

- Never use `GlobalScope` for Ktor requests — always call from a ViewModel or use case scope
- Never hardcode tokens in source — always use `TokenStorage`
- Use `BuildKonfig.BASE_URL` for base URL — never hardcode strings
- Keep `safeRequest` in `:core:network` only; feature modules use it but do not redefine it
- `sendWithoutRequest` must whitelist public paths — default is auth-required
- Disable logging in release: `enableLogging = BuildKonfig.DEBUG`

## Verification

1. `./gradlew :core:network:compileKotlinMetadata` — common source compiles
2. `./gradlew :core:network:compileDebugKotlinAndroid` — Android source compiles
3. `./gradlew :core:network:compileKotlinJvm` — Desktop (CIO) source compiles
4. `./gradlew :core:network:compileKotlinJs` — Web JS source compiles
5. `./gradlew :core:network:compileKotlinWasmJs` — WasmJs source compiles
6. `./gradlew :core:network:commonTest` — mock engine tests pass

---

## Common Anti-Patterns

- throwing exceptions from `safeRequest` instead of returning `NetworkResult.Error` — callers must handle errors explicitly
- creating a new `HttpClient` per request — always inject a single shared instance via Koin
- leaking `NetworkResult` into domain or UI state — map to domain types at the repository boundary
- putting auth token logic in route-level call sites — token refresh belongs in the HttpClient plugin
- skipping the mock engine in tests — test the `safeRequest` contract without a real server

If token refresh is unreliable, check that the auth plugin is installed on the `HttpClient` and not duplicated per request.

---

## Related Skills

- `kmp-feature-scaffold` — `:core:network` follows the same convention plugin pattern
- `kmp-repository-pattern` — repositories call `safeRequest` and map `NetworkResult` to domain types
- `kmp-datastore` — provides `TokenStorage` implementation via DataStore for token persistence
- `kmp-dependency-injection` — Koin wiring for the shared `HttpClient` singleton
- `kmp-unit-testing` — `MockEngine` for testing `safeRequest` contracts without a real server
- `kmp-kotlin-rpc` — use instead of SSE for bidirectional Kotlin-to-Kotlin streaming

---

## Output Style

When asked about the network layer or HTTP client setup, respond in this order:
1. recommendation (Ktor + NetworkResult<T> + safeRequest)
2. project structure (`:core:network` layout)
3. code snippet (one safeRequest call and its NetworkResult handler)
4. why that approach is preferred
5. main alternative (Retrofit, raw HttpClient)

Keep the snippet to one endpoint. Map to the user's actual base URL and response type when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split Step 4 (Platform engine factory), Step 5 (HttpClient factory), and Step 11 (SSE) out of SKILL.md into `references/*.md`. SKILL.md drops from 690 to 473 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as the other 9 skills fixed alongside it (KI-008). |
| 2026-07-31 | Added Step 11: Server-Sent Events (SSE) via Ktor's official multiplatform SSE client plugin (reconnection config, `Flow` mapping, no-compression caveat), plus an SSE vs kRPC vs WebSocket vs polling decision table. Real gap — this repo had zero SSE coverage before. |
| 2026-07-10 | **Fixed a real bug**: this skill only checked for a module literally named `:core:network` — a new server module or feature with a different name found no match, and an agent defaulted to a raw platform HTTP call instead of reusing the project's actual (differently-named) Ktor client. Added Step 0: detect an existing client by content (`HttpClient(`/`safeRequest`/`NetworkResult<`), not by path, before scaffolding or writing any network call. New `kmp-audit` detector `raw http bypasses established ktor client [HIGH]` catches the resulting anti-pattern. |
| 2026-06-06 | Initial release. |
