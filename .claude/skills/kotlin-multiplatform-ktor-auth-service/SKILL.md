---
name: kotlin-multiplatform-ktor-auth-service
description: >
  Ktor-based auth service pattern for Kotlin Multiplatform full-stack apps. Covers:
  bearer and JWT auth, sessions when stateful browser-style sessions are a better fit,
  Ktor RPC when the client and server are both Kotlin-first, typed auth errors, route
  guards, refresh/logout flows, and a small scaffold script for repeated auth module
  setup. Use this for server-side auth, not shared UI auth state.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-13'
  keywords:
    - auth
    - authentication
    - authorization
    - Ktor auth
    - bearer token
    - JWT
    - sessions
    - Ktor RPC
    - login
    - refresh token
    - logout
    - KMP backend
    - server auth
---

## When to Use This Skill

Use this skill when you need to:
- Build or review a Ktor auth service for a KMP full-stack app
- Choose between bearer/JWT auth, sessions, or Ktor RPC
- Add login, refresh, logout, and protected routes
- Keep auth state and transport errors out of shared UI code
- Scaffold the repeated auth service files for a new project

**Recommended default:** bearer + JWT for API auth, sessions only when you need
stateful browser-style persistence, and Ktor RPC only when both sides are Kotlin-first.

**Trigger keywords:** auth, authentication, authorization, bearer token, JWT, sessions,
Ktor auth, Ktor RPC, login, refresh token, logout, protected route, auth service,
sign in, sign up, user login, access token, OAuth, token management,
secure endpoint, validate token, user session, authenticated request.

**Freshness rule:** Ktor auth plugin and JWT APIs change quickly — recheck the
[Ktor auth docs](https://ktor.io/docs/server-auth.html) before using or updating this skill.

---

## Recommendation First

Default to this stack:

1. **Bearer + JWT** for request authorization.
2. **Sessions** only when the product truly wants server-managed session state.
3. **Ktor RPC** only when the client and server are both Kotlin and the contract is
   better expressed as Kotlin procedures than REST resources.

Why:
- bearer/JWT is the most explicit boundary for APIs
- sessions are useful, but they add server state you should choose deliberately
- Ktor RPC is nice for Kotlin-first systems, but it is not the default for every app

---

## Project Structure

Keep auth code separate from the rest of the backend:

```text
server/
  auth/
    routes/AuthRoutes.kt
    service/AuthService.kt
    service/TokenService.kt
    model/AuthRequest.kt
    model/AuthResponse.kt
    model/AuthError.kt
    di/AuthModule.kt
  user/
    repository/UserRepository.kt
    data/UserRepositoryImpl.kt
```

Rules:
- routes stay thin
- business rules live in services
- token issuing and verification stay in one place
- user persistence sits behind a repository boundary

---

## Core Pattern

Use Ktor auth with typed auth errors and explicit route guards:

```kotlin
install(Authentication) {
    bearer("auth-bearer") {
        authenticate { tokenCredential ->
            authService.verifyAccessToken(tokenCredential.token)
        }
    }
    jwt("auth-jwt") {
        verifier(authService.jwtVerifier)
        validate { credential -> authService.validateClaims(credential) }
    }
}

routing {
    authRoutes()
    authenticate("auth-bearer") {
        protectedRoutes()
    }
}
```

For login and refresh:

```kotlin
val result = authService.login(email, password)
when (result) {
    is AuthResult.Success -> call.respond(result.body)
    is AuthResult.InvalidCredentials -> call.respond(HttpStatusCode.Unauthorized)
    is AuthResult.Locked -> call.respond(HttpStatusCode.Forbidden)
}
```

---

## Ktor RPC Guidance

Use Ktor RPC only when the boundary is Kotlin-to-Kotlin and the procedure style is a
better fit than REST resources.

Good fit:
- internal service calls
- Kotlin client and Kotlin server under one product
- strongly typed request/response pairs

Not a good fit:
- public APIs with many non-Kotlin consumers
- simple REST resources
- auth flows that need obvious HTTP semantics

---

## Scaffold Script

- `scripts/scaffold_auth_service.py` — creates a starter auth-service folder with route,
  service, model, and DI files.

---

## Related Skills

- `kotlin-multiplatform-kotlin-rpc` — auth guards the Ktor RPC route using bearer/JWT from this skill
- `kotlin-multiplatform-mongodb-database` — `UserRepository` used by `AuthService` for credential lookup
- `kotlin-multiplatform-network-layer` — client-side token injection is handled by the Ktor auth plugin
- `kotlin-multiplatform-feature-scaffold` — server modules live alongside the shared and client modules from this skill

---

## Testing

```kotlin
// Use Ktor MockEngine — never spin up a real server in unit tests
fun buildTestClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine) {
        engine { addHandler(handler) }
        install(ContentNegotiation) { json() }
    }

class FakeTokenStorage : TokenStorage {
    var stored: TokenPair? = null
    override suspend fun save(tokens: TokenPair) { stored = tokens }
    override suspend fun load(): TokenPair? = stored
    override suspend fun clear() { stored = null }
}

@Test fun `login returns token on 200`() = runTest {
    val client = buildTestClient {
        respond(
            content = """{"accessToken":"tok","refreshToken":"ref","expiresIn":3600}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }
    val storage = FakeTokenStorage()
    val result = AuthServiceImpl(client, storage).login("user", "pass")
    assertTrue(result is NetworkResult.Success)
    assertEquals("tok", storage.stored?.accessToken)
}

@Test fun `login returns Error on 401`() = runTest {
    val client = buildTestClient { respond("Unauthorized", HttpStatusCode.Unauthorized) }
    val result = AuthServiceImpl(client, FakeTokenStorage()).login("u", "wrong")
    assertTrue(result is NetworkResult.Error)
    assertEquals(401, (result as NetworkResult.Error).code)
}

@Test fun `refresh clears storage on 401`() = runTest {
    val storage = FakeTokenStorage().apply { stored = TokenPair("old", "refresh") }
    val client = buildTestClient { respond("Unauthorized", HttpStatusCode.Unauthorized) }
    AuthServiceImpl(client, storage).refresh()
    assertNull(storage.stored)
}
```

---

## Common Anti-Patterns

- validating tokens inside route handlers instead of in the `authenticate {}` block — bypasses Ktor's auth pipeline
- storing refresh tokens in memory — they must survive server restart; use a database or cache
- returning 200 with an error body on auth failure — always use `401 Unauthorized` or `403 Forbidden`
- mixing bearer and session auth on the same route without explicit guards — creates ambiguous behavior
- putting business logic in `AuthService` — it should only handle token issuance, verification, and revocation

If tokens are being rejected unexpectedly, check whether the `verifier` and `validate` blocks in the JWT install are consistent.

---

## Output Style

When asked about Ktor auth, respond in this order:
1. recommendation (bearer + JWT default; sessions or RPC only when explicitly needed)
2. project structure (auth routes, service, token service, DI module)
3. code snippet (install block + one route guard)
4. why that auth approach is preferred
5. main alternative

Keep the snippet to one install block and one guarded route. Map to the user's actual route names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-13 | Initial release. |
