# Feature Slice Checklist

Part of `kmp-expert`. Load this file when working on: feature slice checklist.

---

For every new feature module group (`:feature:x:model/:api/:domain/:data/:presenter/:ui`), verify:

**`:feature:x:model` (pure types)**
- [ ] Only `data class`, `sealed class`, `enum class` — no interfaces, no framework imports
- [ ] No dependency on any other module

**`:feature:x:api` (interfaces)**
- [ ] `FooRepository` interface returns domain types and `Flow<T>` / `Result<T>` only
- [ ] `sealed interface FooError` defined for typed error cases
- [ ] Depends only on `:model` — no logic, no framework deps

**`:feature:x:data` (implementation)**
- [ ] `FooRemoteDataSource` returns `NetworkResult<FooDto>`
- [ ] `FooLocalDataSource` returns `FooEntity` / `Flow<FooEntity?>`
- [ ] `FooRepositoryImpl` maps all types — no DTO or entity escapes to `:api`
- [ ] `FooDataModule` (Koin) wires both data sources and `FooRepository`

**`:feature:x:domain` (use cases, if complexity warrants)**
- [ ] Use cases have a single `invoke` operator
- [ ] Use cases depend only on `:api` — no `:data` imports

**`:feature:x:presenter` (ViewModel — no Compose)**
- [ ] `FooViewModel` has zero Compose imports
- [ ] `FooUiState` and `FooUiIntent` sealed classes defined here
- [ ] Exposes `StateFlow<FooUiState>` — no `SharedFlow` as state holder
- [ ] `_state.update { it.copy(...) }` — never `_state.value = _state.value.copy(...)`

**`:feature:x:ui` (Compose screens)**
- [ ] `FooScreen` wires ViewModel via `koinViewModel()` only
- [ ] `FooContent` is a stateless `@Composable` — accepts `FooUiState` as parameter
- [ ] `@Preview` functions cover Loading / Error / Empty / Success states
- [ ] No direct `:domain` or `:data` imports

---

