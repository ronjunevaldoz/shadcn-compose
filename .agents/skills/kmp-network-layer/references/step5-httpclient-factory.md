# Step 5: HttpClient factory

Part of `kmp-network-layer`. Load this file when working on: step 5: httpclient factory.

---

Create `src/commonMain/kotlin/GROUP_ID/core/network/NetworkClient.kt`:

```kotlin
package GROUP_ID.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates the shared Ktor HttpClient.
 *
 * @param baseUrl    Base URL applied to every request (e.g. "https://api.example.com")
 * @param tokenStorage   Used by the Auth plugin for Bearer token management.
 * @param onRefreshFailed Called when token refresh returns null — use to trigger logout.
 * @param enableLogging  Set false in release builds (use BuildKonfig.DEBUG).
 */
fun createHttpClient(
    baseUrl: String,
    tokenStorage: TokenStorage,
    onRefreshFailed: suspend () -> Unit = {},
    enableLogging: Boolean = false,
): HttpClient = HttpClient(platformEngine()) {

    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }

    // Ktor's built-in bearer plugin handles:
    // - Attaching the access token to every request
    // - Automatic refresh on 401 (race-condition-safe: only one refresh fires
    //   even when multiple requests fail simultaneously)
    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenStorage.getAccessToken() ?: return@loadTokens null
                val refresh = tokenStorage.getRefreshToken() ?: return@loadTokens null
                BearerTokens(access, refresh)
            }
            refreshTokens {
                val refreshToken = tokenStorage.getRefreshToken()
                    ?: run { onRefreshFailed(); return@refreshTokens null }

                // TODO: replace with your actual refresh endpoint call
                // val response = client.post("/auth/refresh") { ... }
                // val newTokens = response.body<TokenResponse>()
                // tokenStorage.saveTokens(newTokens.access, newTokens.refresh)
                // BearerTokens(newTokens.access, newTokens.refresh)

                // Placeholder — return null to trigger onRefreshFailed
                onRefreshFailed()
                null
            }
            sendWithoutRequest { request ->
                // Bypass auth for public endpoints (e.g. login, register)
                request.url.pathSegments.none { it == "auth" }
            }
        }
    }

    if (enableLogging) {
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    println("[Ktor] $message")
                }
            }
        }
    }
}
```

---

