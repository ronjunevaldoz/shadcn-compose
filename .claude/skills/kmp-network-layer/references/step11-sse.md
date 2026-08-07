# Step 11: Server-Sent Events (SSE) for real-time server push

Part of `kmp-network-layer`. Load this file when working on: step 11: server-sent events (sse) for real-time server push.

---

Ktor ships an official multiplatform SSE client plugin — works across all KMP targets
since Ktor Client is multiplatform. Use it for one-way server → client push (live
updates, progress streams, notifications) instead of polling REST or rolling a custom
WebSocket.

```kotlin
// build.gradle.kts — commonMain
implementation("io.ktor:ktor-client-sse:$ktorVersion")
```

```kotlin
val client = HttpClient {
    install(SSE) {
        // auto-reconnect on drop — tune per endpoint, not global
        reconnectionTime = 3.seconds
        maxReconnectionAttempts = 5
    }
}

fun observeOrderStatus(orderId: String): Flow<OrderStatus> = flow {
    client.sseSession(urlString = "$baseUrl/orders/$orderId/events") {
        // type-safe deserialization via ClientSSESessionWithDeserialization
        incoming.collect { event ->
            event.data?.let { emit(json.decodeFromString<OrderStatus>(it)) }
        }
    }
}
```

SSE maps directly onto `Flow` — backpressure comes for free, and `Flow.retry` plus
replaying the `Last-Event-ID` header covers reconnection without hand-rolled retry logic.

Constraints to know before reaching for it:
- **No compression** — the Compression plugin skips SSE responses by default; don't
  expect gzip savings on an SSE stream.
- **One-way only** — SSE is server → client. If the client also needs to push, use
  WebSocket or fall back to a normal request alongside the SSE stream.
- Server-side counterpart is Ktor's own SSE plugin (`install(SSE)` on the server) —
  pairs naturally when both ends are Ktor.

### SSE vs kRPC vs WebSocket vs polling

| Need | Use |
|---|---|
| One-way server push, Kotlin or non-Kotlin client, standard HTTP semantics | **SSE** (this section) |
| Bidirectional Kotlin-to-Kotlin streaming, both sides control the contract | **kRPC** — see `kmp-kotlin-rpc`'s streaming section |
| Bidirectional, non-Kotlin client, or low-level frame control needed | **WebSocket** |
| Infrequent updates, simplicity over latency, no persistent connection wanted | **Polling REST** with `safeRequest` |

If the backend is already exposed over kRPC and the need is one-way push to a
non-Kotlin client too, don't add SSE as a second transport — extend the RPC service
with a `Flow`-returning method instead (kRPC handles that natively).

---

