# Step 5: DriverFactory expect/actual

Part of `kmp-sqldelight-setup`. Load this file when working on: step 5: driverfactory expect/actual.

---

### `src/commonMain/kotlin/GROUP_ID/core/database/DriverFactory.kt`

```kotlin
package GROUP_ID.core.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}
```

### `src/androidMain/kotlin/GROUP_ID/core/database/DriverFactory.kt`

```kotlin
package GROUP_ID.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")
}
```

### `src/iosMain/kotlin/GROUP_ID/core/database/DriverFactory.kt`

```kotlin
package GROUP_ID.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AppDatabase.Schema, "app.db")
}
```

### `src/jvmMain/kotlin/GROUP_ID/core/database/DriverFactory.kt`

```kotlin
package GROUP_ID.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:app.db", Properties(), AppDatabase.Schema)
        return driver
    }
}
```

> For Desktop, the database file is created relative to the process working directory.
> Use `System.getProperty("user.home")` to store in a persistent location.

### `src/jsMain/kotlin/GROUP_ID/core/database/DriverFactory.kt`

```kotlin
package GROUP_ID.core.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

/**
 * JS driver is async — the schema creation is deferred to [DatabaseFactory].
 * [createDriver] returns the uninitialised driver; call [AppDatabase.Schema.awaitCreate] after.
 */
actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        WebWorkerDriver(
            Worker(js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""))
        )
}
```

> JS uses an async schema; call `AppDatabase.Schema.awaitCreate(driver)` before first query.

---

