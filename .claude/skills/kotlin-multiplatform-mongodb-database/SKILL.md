---
name: kotlin-multiplatform-mongodb-database
description: >
  MongoDB Kotlin Coroutine Driver pattern for Kotlin Multiplatform full-stack apps.
  Covers: server-side MongoDB access, document mapping, repository boundaries,
  typed database errors, reactive reads with Flow, change streams, and a scaffold
  script for repeated database setup. Use this for the backend data layer, not
  shared client persistence.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - MongoDB
    - Kotlin coroutine driver
    - database
    - repository
    - Flow
    - change stream
    - server-side Kotlin
    - KMP backend
    - document mapping
    - collection
    - query
---

## When to Use This Skill

Use this skill when you need to:
- Build or review a MongoDB-backed backend data layer for a KMP app
- Keep document mapping and repository code cleanly separated
- Use Flow or change streams for reactive reads
- Model typed database failures instead of leaking driver exceptions upward
- Scaffold repeated database setup for a new service

**Recommended default:** keep MongoDB on the server side behind a repository boundary,
map documents to domain models at the edge, and expose typed errors to the caller.

**Trigger keywords:** MongoDB, database, collection, repository, document mapping,
Flow, change stream, server-side Kotlin, coroutine driver, typed errors, backend data.

**Freshness rule:** the MongoDB Kotlin coroutine driver evolves independently of the JVM
driver — recheck the [official docs](https://www.mongodb.com/docs/drivers/kotlin/coroutine/)
before upgrading or scaffolding.

---

## Recommendation First

Default to this pattern:

1. **Server-side MongoDB only** with the official Kotlin coroutine driver.
2. **Repository boundary** between collection access and the rest of the app.
3. **Typed document mapping** at the edge, not in route handlers.
4. **Flow / change streams** for reactive reads when the data should update live.

Why:
- the driver is officially positioned for server-side Kotlin coroutine apps
- repositories keep route handlers and services small
- typed mapping keeps BSON/documents from leaking through the app

---

## Project Structure

Keep the database layer separate from auth and routes:

```text
server/
  database/
    MongoClientFactory.kt
    di/DatabaseModule.kt
  user/
    data/UserCollection.kt
    data/UserDocument.kt
    repository/UserRepository.kt
    repository/UserRepositoryImpl.kt
```

Rules:
- database bootstrap lives in one place
- collection access is not spread across route handlers
- document mapping happens in the data layer
- repositories expose domain models, not BSON documents

---

## Core Pattern

Use the coroutine driver with a client + database + collection boundary:

```kotlin
class UserRepositoryImpl(
    private val collection: MongoCollection<UserDocument>,
) : UserRepository {

    override suspend fun findById(id: String): User? =
        collection.findOneById(id)?.toDomain()

    override fun watchUsers(): Flow<List<User>> =
        collection.watch().map { events -> events.map { it.fullDocument.toDomain() } }
}
```

Typed errors stay close to the repository:

```kotlin
sealed interface DatabaseError {
    data object NotFound : DatabaseError
    data object Unauthorized : DatabaseError
    data class Unknown(val message: String) : DatabaseError
}
```

---

## Reactive Reads

Use `Flow` when the UI or service should react to updates:
- live dashboards
- collections that change often
- admin views
- background sync listeners

Do not overuse change streams for simple CRUD if a plain repository call is enough.

---

## Scaffold Script

- `scripts/scaffold_mongodb_database.py` — creates a starter MongoDB data-layer folder
  with client, repository, document, and DI files.

---

## Related Skills

- `kotlin-multiplatform-ktor-auth-service` — auth service depends on the `UserRepository` built here
- `kotlin-multiplatform-kotlin-rpc` — RPC service implementations delegate to MongoDB repositories
- `kotlin-multiplatform-repository-pattern` — the same repository interface/implementation pattern applied server-side
- `kotlin-multiplatform-dependency-injection` — `MongoClient` and collection bindings are registered as Koin singletons

---

## Testing

MongoDB data layer tests operate at two levels. Both are required.

### Level 1 — Unit tests for services and use cases (FakeRepository)

Services and use cases that depend on `UserRepository` must never use the real
`UserRepositoryImpl` in tests. Use a fake that implements the interface:

```kotlin
// server/user/testing/FakeUserRepository.kt
class FakeUserRepository : UserRepository {

    val users = mutableMapOf<String, User>()
    var shouldThrow: Exception? = null

    override suspend fun findById(id: String): User? {
        shouldThrow?.let { throw it }
        return users[id]
    }

    override suspend fun findByEmail(email: String): User? =
        users.values.find { it.email == email }

    override suspend fun save(user: User): User {
        users[user.id] = user
        return user
    }

    override suspend fun delete(id: String): Boolean =
        users.remove(id) != null

    // Change stream test support — call emitUpdate() from test body
    private val _updates = MutableSharedFlow<List<User>>(extraBufferCapacity = 1)
    override fun watchUsers(): Flow<List<User>> = _updates

    suspend fun emitUpdate(list: List<User>) = _updates.emit(list)
}
```

Example service unit test:

```kotlin
class UserServiceTest {
    private val repo = FakeUserRepository()
    private val service = UserService(repo)

    @Test fun `findById returns user when found`() = runTest {
        repo.users["u1"] = User(id = "u1", name = "Alice", email = "alice@example.com")
        val result = service.findById("u1")
        assertEquals("Alice", result?.name)
    }

    @Test fun `findById returns null when not found`() = runTest {
        assertNull(service.findById("missing"))
    }

    @Test fun `save persists the user`() = runTest {
        val user = User(id = "u2", name = "Bob", email = "bob@example.com")
        service.save(user)
        assertEquals(user, repo.users["u2"])
    }

    @Test fun `watchUsers emits on change`() = runTest {
        val users = listOf(User("u1", "Alice", "alice@example.com"))
        service.watchUsers().test {
            repo.emitUpdate(users)
            assertEquals(users, awaitItem())
        }
    }
}
```

---

### Level 2 — Integration tests for the repository implementation (embedded MongoDB)

The repository implementation contains real MongoDB query logic — it must be tested
against an actual MongoDB instance. Use **Flapdoodle** for fast in-process tests (no
Docker required) or **TestContainers** for production-parity CI tests.

**`libs.versions.toml` additions:**

```toml
[versions]
flapdoodle = "4.14.0"     # recheck latest before pinning

[libraries]
flapdoodle-mongo = { module = "de.flapdoodle.embed:de.flapdoodle.embed.mongo", version.ref = "flapdoodle" }
```

In the server test sourceset:
```kotlin
testImplementation(libs.flapdoodle.mongo)
```

**Repository integration test pattern:**

```kotlin
// server/user/data/UserRepositoryImplTest.kt
class UserRepositoryImplTest {

    private lateinit var mongoClient: MongoClient
    private lateinit var collection: MongoCollection<UserDocument>
    private lateinit var repo: UserRepositoryImpl

    @BeforeTest fun setUp() {
        val mongod = MongodStarter.getDefaultInstance()
            .prepare(MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(Net(27017, Network.localhostIsIPv6()))
                .build())
            .start()

        mongoClient = MongoClient.create("mongodb://localhost:27017")
        collection = mongoClient
            .getDatabase("test_${System.currentTimeMillis()}")   // isolated DB per test class
            .getCollection<UserDocument>("users")
        repo = UserRepositoryImpl(collection)
    }

    @AfterTest fun tearDown() {
        collection.drop()   // clean up after each test class run
        mongoClient.close()
    }

    @Test fun `findById returns mapped domain model`() = runTest {
        val doc = UserDocument(id = "u1", name = "Alice", email = "alice@example.com")
        collection.insertOne(doc)

        val result = repo.findById("u1")

        assertNotNull(result)
        assertEquals("Alice", result.name)
        assertEquals("alice@example.com", result.email)
    }

    @Test fun `findById returns null when document absent`() = runTest {
        assertNull(repo.findById("not-there"))
    }

    @Test fun `save inserts then retrieves correctly`() = runTest {
        val user = User(id = "u2", name = "Bob", email = "bob@example.com")
        repo.save(user)
        val found = repo.findById("u2")
        assertEquals(user.name, found?.name)
    }

    @Test fun `delete removes the document`() = runTest {
        collection.insertOne(UserDocument(id = "u3", name = "Carol", email = "carol@example.com"))
        val deleted = repo.delete("u3")
        assertTrue(deleted)
        assertNull(repo.findById("u3"))
    }

    @Test fun `findById propagates typed error on driver exception`() = runTest {
        // Close the client to force a connection error
        mongoClient.close()
        assertFailsWith<DatabaseError.Unknown> { repo.findById("any") }
    }
}
```

---

### Testing change streams (Flow)

Change streams require a **replica set** — they do not work on a standalone Flapdoodle
instance. Options:
- Use a replica set via TestContainers (`MongoDBContainer` with replica set support)
- Or test the Flow contract via `FakeUserRepository.emitUpdate()` in unit tests (recommended
  for most cases — the MongoDB change-stream plumbing is driver-owned, not yours to test)

```kotlin
// TestContainers replica set — for full change-stream integration test
@Testcontainers
class UserWatchIntegrationTest {

    companion object {
        @Container
        val mongo = MongoDBContainer("mongo:7").apply {
            withCommand("--replSet", "rs0")
        }
    }

    @Test fun `watchUsers emits on insert`() = runTest {
        val client = MongoClient.create(mongo.connectionString)
        val collection = client.getDatabase("test").getCollection<UserDocument>("users")
        val repo = UserRepositoryImpl(collection)

        repo.watchUsers().test(timeout = 5.seconds) {
            collection.insertOne(UserDocument("u1", "Alice", "alice@example.com"))
            val update = awaitItem()
            assertTrue(update.any { it.name == "Alice" })
        }
    }
}
```

---

### Testing the document mapping (pure unit test — no DB needed)

Document ↔ domain mapping is pure Kotlin — test it without any database:

```kotlin
class UserDocumentMappingTest {
    @Test fun `toDomain maps all fields correctly`() {
        val doc = UserDocument(id = "u1", name = "Alice", email = "alice@example.com", role = "ADMIN")
        val user = doc.toDomain()
        assertEquals("u1", user.id)
        assertEquals("Alice", user.name)
        assertEquals("alice@example.com", user.email)
        assertEquals(UserRole.ADMIN, user.role)
    }

    @Test fun `toDocument round-trips through toDomain`() {
        val user = User(id = "u1", name = "Alice", email = "alice@example.com", role = UserRole.ADMIN)
        assertEquals(user, user.toDocument().toDomain())
    }
}
```

---

## Common Anti-Patterns

- accessing collections directly from route handlers — always go through a repository
- leaking `BsonDocument` or `MongoCollection` types out of the data layer — map to domain types at the boundary
- using change streams for every collection by default — use them only when live updates are needed
- constructing `MongoClient` in every call instead of sharing one instance via Koin — exhausts connection pools
- catching `MongoException` globally and swallowing it — return a typed `DatabaseError` instead
- **mocking `MongoCollection` directly in tests** — mocking driver internals (`collection.findOneById(...)` returns ...) is brittle; the mock breaks on driver upgrades and doesn't test query construction; use Flapdoodle/TestContainers for the repository impl, and `FakeUserRepository` for everything above it
- **only writing service tests (Level 1) and skipping repository integration tests** — service tests with fakes verify service logic, but the repository implementation contains actual MongoDB query logic (filter construction, sort, projection); that code only executes against a real driver
- **sharing a single embedded MongoDB database across tests** — concurrent or sequential tests that share collection state interfere with each other; use a unique database name per test class (e.g. `"test_${System.currentTimeMillis()}"`) and drop it in `@AfterTest`
- **testing change streams against a standalone MongoDB** — change streams require a replica set; a standalone instance silently returns no events; either use TestContainers with `--replSet rs0` or skip the live-stream test in favour of a fake Flow in unit tests

If the connection pool is exhausted or queries are slow, check that only one `MongoClient` is registered per DI scope.

---

## Output Style

When asked about MongoDB setup or data access, respond in this order:
1. recommendation (server-side only, repository boundary, typed errors)
2. project structure (client factory, collection, repository, DI module)
3. code snippet (repository impl with one `findById` and one Flow)
4. **FakeRepository** — for service/use case unit tests
5. **Flapdoodle integration test** — one test covering the happy path and one covering not-found
6. **Document mapping test** — pure Kotlin round-trip test
7. why that approach is preferred
8. main alternative (direct collection access, other drivers)

Never ship a MongoDB repository implementation without a matching Level 1 fake and a Level 2
Flapdoodle test. Both levels cover different bugs — do not treat one as sufficient.

Keep the snippet to one repository method. Map to the user's actual collection and document names when provided.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
