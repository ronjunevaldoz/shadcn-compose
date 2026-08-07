# Step 4: Platform engine factory

Part of `kmp-network-layer`. Load this file when working on: step 4: platform engine factory.

---

### `src/androidMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp
```

### `src/iosMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin
```

### `src/jvmMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

internal actual fun platformEngine(): HttpClientEngineFactory<*> = CIO
```

### `src/jsMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual fun platformEngine(): HttpClientEngineFactory<*> = Js
```

### `src/wasmJsMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual fun platformEngine(): HttpClientEngineFactory<*> = Js
```

### `src/commonMain/kotlin/GROUP_ID/core/network/HttpClientEngineFactory.kt`

```kotlin
package GROUP_ID.core.network

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun platformEngine(): HttpClientEngineFactory<*>
```

---

