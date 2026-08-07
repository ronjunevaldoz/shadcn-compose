---
name: kmp-sqldelight-setup
description: >
  Sets up SQLDelight 2 in a Kotlin Multiplatform project. Covers: Gradle plugin
  configuration, schema (.sq) files, migrations, type adapters, platform-specific
  drivers via expect/actual (Android, iOS, Desktop/JVM, Web/JS), coroutines Flow
  queries, and Koin wiring inside :core:database. Assumes the project was scaffolded
  with kmp-feature-scaffold.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-09'
  keywords:
    - SQLDelight 2
    - KMP database
    - SQLite
    - Kotlin Multiplatform
    - coroutines
    - Flow
    - type adapters
    - migrations
    - Android
    - iOS
    - Desktop
    - Web
---

## Overview

This skill populates `:core:database` with a complete SQLDelight 2 setup:

```
:core:database
  commonMain
    Database schema (.sq files)
    DriverFactory          expect class — creates platform SqlDriver
    DatabaseFactory        creates and migrates the Database instance
    di/DatabaseModule      Koin module
  androidMain
    DriverFactory          → AndroidSqliteDriver
  iosMain / nativeMain
    DriverFactory          → NativeSqliteDriver
  jvmMain
    DriverFactory          → JdbcSqliteDriver  (Desktop)
  jsMain
    DriverFactory          → WebWorkerDriver   (Web/JS — async)
  wasmJsMain
    ⚠ Not supported by SQLDelight 2.x — see note below
```

> **WasmJs note**: SQLDelight 2.x has no WasmJs driver. For WasmJs targets, use an
> in-memory store or a different persistence strategy (e.g., `localStorage` via
> `kotlin-wrappers`). Track upstream: https://github.com/cashapp/sqldelight/issues

## When to Use This Skill

Use this skill when you need to:
- Add SQLDelight 2 to a KMP project
- Define schema files, migrations, or type adapters
- Wire platform drivers and Flow-based queries
- Recheck driver support before targeting a new platform

**Trigger keywords:** SQLDelight, database, SQLite, schema, migrations, type adapter,
Flow query, Android driver, Native driver, Desktop driver, Web driver,
local database KMP, Room alternative, offline storage, local persistence,
SQLite KMP, persist to database, store data locally, local data layer,
data storage, persistence layer, data redundancy, content storage, local cache,
database design, data schema, persistent storage, offline data.

**Freshness rule:** SQLDelight support changes across targets, so recheck the current
driver matrix and WasmJs status before copying setup code.

---

## Recommendation First

Default to **SQLDelight 2 with platform-specific drivers injected via Koin + Flow queries**.

Why:
- Flow queries emit automatically when the underlying table changes — no manual cache invalidation
- platform drivers (Android SqlDriver, JDBC, Web Worker) are injected, not hard-coded, so the
  database module stays in `commonMain`
- SQLDelight's compile-time SQL verification catches schema/query mismatches before runtime

Use Room only if the team is Android-only and the shared-module investment is not worth it.

---

## Prerequisites

- Project scaffolded with `kmp-feature-scaffold`
- `:core:database` module exists and applies `GROUP_ID.core` convention plugin
- `libs.versions.toml` has SQLDelight entries (see version reference below)

---

## Version Reference

```toml
[versions]
sqldelight = "2.3.2"

[libraries]
sqldelight-runtime           = { module = "app.cash.sqldelight:runtime",                   version.ref = "sqldelight" }
sqldelight-coroutines        = { module = "app.cash.sqldelight:coroutines-extensions",      version.ref = "sqldelight" }
sqldelight-android-driver    = { module = "app.cash.sqldelight:android-driver",             version.ref = "sqldelight" }
sqldelight-native-driver     = { module = "app.cash.sqldelight:native-driver",              version.ref = "sqldelight" }
sqldelight-sqlite-driver     = { module = "app.cash.sqldelight:sqlite-driver",              version.ref = "sqldelight" }
sqldelight-web-worker-driver = { module = "app.cash.sqldelight:web-worker-driver",          version.ref = "sqldelight" }
sqldelight-primitive-adapters = { module = "app.cash.sqldelight:primitive-adapters",        version.ref = "sqldelight" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

Add any missing entries to `gradle/libs.versions.toml`.

---

## Step 1: Update `:core:database/build.gradle.kts`

Full content: `references/step1-build-gradle.md`.

## Step 2: Create a schema file

Schema files live in `src/commonMain/sqldelight/GROUP_ID/core/database/`.

`src/commonMain/sqldelight/GROUP_ID/core/database/User.sq`:

```sql
CREATE TABLE IF NOT EXISTS user (
    id          TEXT    NOT NULL PRIMARY KEY,
    name        TEXT    NOT NULL,
    email       TEXT    NOT NULL,
    created_at  INTEGER NOT NULL   -- stored as epoch millis
);

-- Named queries (generate type-safe Kotlin functions):

insertUser:
INSERT OR REPLACE INTO user (id, name, email, created_at)
VALUES (?, ?, ?, ?);

selectAllUsers:
SELECT * FROM user
ORDER BY created_at DESC;

selectUserById:
SELECT * FROM user
WHERE id = ?;

deleteUserById:
DELETE FROM user
WHERE id = ?;

deleteAllUsers:
DELETE FROM user;
```

The SQLDelight Gradle plugin generates `UserQueries` from this file.

---

## Step 3: Migrations

Migrations live in `src/commonMain/sqldelight/migrations/`.

Name files as `<version>.sqm` (version = the schema version being migrated FROM):

**`src/commonMain/sqldelight/migrations/1.sqm`** — migrate from version 1 to 2:

```sql
ALTER TABLE user ADD COLUMN avatar_url TEXT;
```

Update `schemaVersion` in the `create("AppDatabase")` block:

```kotlin
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("GROUP_ID.core.database")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
            // Increment when you add a migration:
            // version = 2  ← SQLDelight infers from migration files
        }
    }
}
```

SQLDelight auto-detects the version from the number of `.sqm` files.

---

## Step 4: Type adapters

Use type adapters to map non-primitive Kotlin types to SQLite storage types.

`src/commonMain/kotlin/GROUP_ID/core/database/adapters/InstantAdapter.kt`:

```kotlin
package GROUP_ID.core.database.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

/**
 * Stores [Instant] as epoch milliseconds (INTEGER) in SQLite.
 */
val instantAdapter: ColumnAdapter<Instant, Long> = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}
```

For `List<String>` (comma-delimited):

```kotlin
val stringListAdapter: ColumnAdapter<List<String>, String> = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> =
        if (databaseValue.isEmpty()) emptyList() else databaseValue.split(",")

    override fun encode(value: List<String>): String = value.joinToString(",")
}
```

---

## Step 5: DriverFactory expect/actual

Full content: `references/step5-driverfactory-expect-actual.md`.

## Step 6: DatabaseFactory

`src/commonMain/kotlin/GROUP_ID/core/database/DatabaseFactory.kt`:

```kotlin
package GROUP_ID.core.database

import GROUP_ID.core.database.adapters.instantAdapter

fun createDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(
        driver = driver,
        // Wire adapters for each table that uses custom column types:
        userAdapter = User.Adapter(
            created_atAdapter = instantAdapter
        )
    )
}
```

For JS, wrap in a suspend function due to the async driver:

```kotlin
// jsMain only — if you need to initialise explicitly on Web
suspend fun createDatabaseAsync(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    AppDatabase.Schema.awaitCreate(driver)
    return AppDatabase(
        driver = driver,
        userAdapter = User.Adapter(created_atAdapter = instantAdapter)
    )
}
```

---

## Step 7: Using coroutines extensions (Flow queries)

SQLDelight's `coroutines-extensions` turns any named query into a `Flow`:

```kotlin
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers

// In a repository:
class UserLocalDataSource(private val db: AppDatabase) {

    fun observeAllUsers(): Flow<List<User>> =
        db.userQueries.selectAllUsers()
            .asFlow()
            .mapToList(Dispatchers.Default)

    fun observeUser(id: String): Flow<User?> =
        db.userQueries.selectUserById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)

    suspend fun upsertUser(user: User) {
        db.userQueries.insertUser(
            id        = user.id,
            name      = user.name,
            email     = user.email,
            created_at = user.createdAt.toEpochMilliseconds()
        )
    }

    suspend fun deleteUser(id: String) {
        db.userQueries.deleteUserById(id)
    }
}
```

---

## Step 8: Transactions

Wrap multi-statement operations in a transaction for atomicity and performance:

```kotlin
db.transaction {
    users.forEach { user ->
        db.userQueries.insertUser(user.id, user.name, user.email, user.createdAt.toEpochMilliseconds())
    }
}
```

Use `transactionWithResult` to return a value:

```kotlin
val count = db.transactionWithResult {
    db.userQueries.selectAllUsers().executeAsList().size
}
```

---

## Step 9: Koin module

`src/commonMain/kotlin/GROUP_ID/core/database/di/DatabaseModule.kt`:

```kotlin
package GROUP_ID.core.database.di

import GROUP_ID.core.database.DatabaseFactory
import GROUP_ID.core.database.UserLocalDataSource
import org.koin.dsl.module

/**
 * Consumers must provide a [DriverFactory] binding for their platform.
 * Android: module { single { DriverFactory(androidContext()) } }
 * iOS/Desktop: module { single { DriverFactory() } }
 */
val databaseModule = module {
    single { createDatabase(get()) }
    single { UserLocalDataSource(get()) }
}
```

Wire in `:androidApp`:

```kotlin
startKoin {
    androidContext(this@App)
    modules(
        module { single { DriverFactory(androidContext()) } },
        databaseModule,
        // other modules...
    )
}
```

---

## Step 10: Update `:core:database` in settings.gradle.kts

Ensure the module is included in the project:

```kotlin
include(":core:database")
```

And in the feature data module that needs local persistence:

```kotlin
// :feature:auth:data/build.gradle.kts
dependencies {
    implementation(projects.core.database)
}
```

---

## Guidelines

- Never call database queries on the main thread — always use `Dispatchers.IO` (Android) or `Dispatchers.Default` (common)
- Prefer `asFlow()` + `mapToList/mapToOneOrNull` over one-shot `executeAsList()` for reactive UI
- Keep `.sq` files focused — one file per table; caught by the audit's `combined sqldelight table file [MEDIUM]` if a file defines more than one `CREATE TABLE`
- Name all queries explicitly (avoids relying on generated `selectAll`, `insert`, etc.) for clarity
- Use `INTEGER` for booleans (0/1) and epoch millis for timestamps — SQLite has no native bool/datetime
- WasmJs: SQLDelight has no WasmJs driver; use in-memory state or `localStorage` via `kotlin-browser`

---

## Verification

1. `./gradlew :core:database:generateCommonMainAppDatabaseInterface` — schema compiles, Kotlin generated
2. `./gradlew :core:database:compileDebugKotlinAndroid` — Android driver wires up
3. `./gradlew :core:database:compileKotlinJvm` — Desktop (JDBC) driver wires up
4. `./gradlew :core:database:compileKotlinJs` — Web worker driver wires up
5. `./gradlew :core:database:verifySqlDelightMigration` — migrations are valid (if any)
6. `./gradlew :core:database:jvmTest` — run unit tests against JdbcSqliteDriver in-memory

---

## Testing

Full content: `references/testing.md`.

## Common Anti-Patterns

- running database queries on the main thread — always use `Dispatchers.IO` or the appropriate coroutine dispatcher
- exposing raw `Query<T>` objects from the repository — always wrap in `Flow` and map to domain types
- writing migration SQL inside `afterVersion` lambdas without test coverage — migration bugs cause data loss
- sharing a `DatabaseDriverFactory` across tests without clearing state — tests contaminate each other
- forgetting to call `.asFlow().mapToList()` — plain `executeAsList()` doesn't react to table changes

If Flow queries are not updating after a write, check that the write and the Flow share the same database instance.

---

## Related Skills

- `kmp-feature-scaffold` — `:core:database` follows the same convention plugin pattern
- `kmp-repository-pattern` — SQLDelight drives the cache layer in offline-first repositories
- `kmp-network-layer` — Ktor + SQLDelight together form the complete data layer
- `kmp-dependency-injection` — Koin binding for `DatabaseDriverFactory` per platform
- `kmp-code-quality` — file/type naming conventions for `.sq` files and generated Kotlin

---

## Output Style

When asked about SQLDelight setup or database queries, respond in this order:
1. recommendation (SQLDelight 2, platform drivers, Flow queries)
2. project structure (`:core:database` layout)
3. code snippet (one `.sq` query and its generated Kotlin call)
4. why SQLDelight is preferred over Room in KMP
5. main alternative (Realm, raw SQLite driver)

Keep the snippet to one table and one query. Map to the user's actual entity names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split Step 1 (build.gradle.kts), Step 5 (DriverFactory expect/actual), and Testing out of SKILL.md into `references/*.md`, leaving pointer stubs. SKILL.md drops from 630 to 459 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as the other 13 skills fixed alongside it (KI-008). |
| 2026-08-04 | Added `kmp-code-quality` to Related Skills — naming conventions existed but only `kmp-mvi` cross-referenced them. |
| 2026-07-09 | The "one file per table" guideline had no enforcement anywhere. New `kmp-audit` detector `combined sqldelight table file [MEDIUM]` flags any `.sq` file defining more than one `CREATE TABLE`. |
| 2026-06-06 | Initial release. |
