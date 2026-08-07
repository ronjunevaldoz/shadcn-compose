# Testing

Part of `kmp-sqldelight-setup`. Load this file when working on: testing.

---

```kotlin
// Use JdbcSqliteDriver with in-memory database — no device or emulator needed
fun testDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AppDatabase.Schema.create(driver)
    return driver
}

@Test fun `insert and query round-trip`() = runTest {
    val db = AppDatabase(testDriver())
    db.userQueries.insertUser(id = "1", name = "Alice", email = "a@example.com", createdAt = 0L)
    val result = db.userQueries.selectUserById("1").executeAsOne()
    assertEquals("Alice", result.name)
}

@Test fun `delete removes row`() = runTest {
    val db = AppDatabase(testDriver())
    db.userQueries.insertUser(id = "1", name = "Alice", email = "a@example.com", createdAt = 0L)
    db.userQueries.deleteUserById("1")
    assertNull(db.userQueries.selectUserById("1").executeAsOneOrNull())
}

@Test fun `query emits updates via flow`() = runTest {
    val db = AppDatabase(testDriver())
    db.userQueries.selectAllUsers().asFlow().mapToList(coroutineContext).test {
        assertEquals(emptyList(), awaitItem())
        db.userQueries.insertUser(id = "1", name = "Alice", email = "a@example.com", createdAt = 0L)
        assertEquals(1, awaitItem().size)
        cancelAndIgnoreRemainingEvents()
    }
}
```

> Add `testImplementation("app.cash.sqldelight:sqlite-driver:<version>")` to the `jvmTest` source set only — the JDBC driver is JVM-only and must not appear in `commonMain`.

---

