---
name: kotlin-multiplatform-migration
description: >
  Incremental adoption guide for teams moving an existing Android or KMP project
  toward the kmm-agent-skills architecture. Covers assessment of current state,
  prioritized skill adoption order, migration paths from MVVM+LiveData to MVI,
  monolith to multi-module, and how to migrate without breaking a live app.
  Use this skill when a project already exists and the team wants to adopt KMP
  skills one feature or one layer at a time.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-28'
  keywords:
    - migration
    - incremental adoption
    - existing project
    - MVVM to MVI
    - LiveData to StateFlow
    - monolith to multi-module
    - legacy migration
    - refactor architecture
    - adopt clean architecture
    - retrofit existing project
    - migrate ViewModel
    - architecture migration
    - how to start
    - where to begin
    - old project adoption
    - brownfield
---

## When to Use This Skill

Use when:
- An existing Android or KMP project needs to adopt the 6-layer architecture incrementally
- A team is migrating from MVVM+LiveData to MVI+StateFlow screen by screen
- You need to split a monolithic module into `:presenter` / `:ui` without breaking everything
- You want a prioritized order for adopting skills in a project that has none yet

**Freshness rule:** migration paths reference specific skills (MVI, clean-architecture, DI).
Recheck those skills for updated patterns before executing a migration step — the target
pattern may have changed since this skill was written.

**Trigger keywords:** migrate existing project, adopt MVI, LiveData to StateFlow,
migrate to clean architecture, incremental adoption, where to start, existing project,
brownfield, refactor architecture, add layers to project, how to adopt skills,
migration path, legacy project, retrofit architecture, migrate ViewModel.

---

## Recommendation First

**Never migrate everything at once.** Migrate one screen or one feature at a time.
Keep the app shipping at every step — a half-migrated screen in production is acceptable;
a broken build is not.

Adoption order:
1. **Run the audit** — understand current state before touching code
2. **Adopt the architecture contract** — agree on layer rules, add Detekt gates
3. **Add MVI to one screen** — prove the pattern works, set the example
4. **Migrate the highest-traffic feature** — high leverage, high visibility
5. **Spread one layer at a time** — complete the pattern across screens before adding new layers

---

## Phase 1 — Assess Current State

Run the audit before writing any code:

```bash
# Project architecture audit
python3 .claude/skills/kotlin-multiplatform-audit/scripts/audit_project.py . --roadmap

# If audit_project.py is not installed, run the skills version scanner
python3 /path/to/kmm-agent-skills/skills/kotlin-multiplatform-audit/scripts/audit_project.py . --roadmap
```

The `--roadmap` flag outputs a prioritized adoption plan instead of just violations.

### What to look for manually

| Question | Where to look | What it tells you |
|---|---|---|
| What state management is in use? | ViewModel classes | LiveData → migrate to StateFlow; StateFlow → check for MVI contract |
| Are layers separated? | Module list in `settings.gradle.kts` | One module = monolith; `:feature:*` = already split |
| Is there a version catalog? | `gradle/libs.versions.toml` | Missing = high dependency drift risk |
| Are there tests? | `src/*/test/` directories | No tests = migrate with extra care; add tests first |
| Is Detekt configured? | `detekt.yml` or Gradle task | Missing = no architecture enforcement |
| Are there hardcoded colors / spacing? | `Color(0x...)`, `.dp` literals in UI | Design system not adopted |

### Current state scorecard

Score the project before planning:

| Dimension | 0 | 1 | 2 |
|---|---|---|---|
| **Module structure** | Single module | Some feature split | Full :feature:*:layer split |
| **State management** | LiveData / MutableState | StateFlow (no Contract) | MVI Contract + Channel |
| **Testing** | No tests | Some unit tests | ViewModel tests + Compose tests |
| **DI** | Manual instantiation / Hilt | Koin (manual) | Koin 4 (annotated) |
| **Enforcement** | Nothing | Detekt partial | Detekt + Gradle layer gates |

Score 0–2 per dimension. Lower score = higher priority to fix first.

---

## Phase 2 — Agree on the Contract First

Before migrating any code, add Detekt rules and document the layer contract.
This prevents new violations while you migrate old ones.

```bash
# Add detekt.yml with architecture rules (from kotlin-multiplatform-clean-architecture)
# Add the Gradle fitness function CI check
# Document the target structure in ARCHITECTURE.md or AGENTS.md
```

Why first: without gates, developers keep adding violations faster than you migrate them.

---

## Phase 3 — Prioritized Skill Adoption Order

Adopt skills in this order. Each builds on the previous.

### Tier 1 — Foundation (do these before anything else)

| Skill | Why first |
|---|---|
| `kotlin-multiplatform-clean-architecture` | Sets the contract all other migrations must satisfy |
| `kotlin-multiplatform-code-quality` | Detekt gates prevent new violations during migration |
| `kotlin-multiplatform-feature-scaffold` | Defines the module structure to migrate toward |

### Tier 2 — Screen layer (apply per screen as you migrate)

| Skill | When to apply |
|---|---|
| `kotlin-multiplatform-mvi` | When migrating a screen from MVVM to MVI |
| `kotlin-multiplatform-presenter-module` | When extracting `:presenter` from a monolithic module |
| `kotlin-multiplatform-unit-testing` | Immediately after migrating each ViewModel |

### Tier 3 — Infrastructure (after screen layer is stable)

| Skill | When to apply |
|---|---|
| `kotlin-multiplatform-network-layer` | When migrating from Retrofit/Volley to Ktor |
| `kotlin-multiplatform-repository-pattern` | When splitting data access from ViewModels |
| `kotlin-multiplatform-dependency-injection` | When migrating from Hilt/manual DI to Koin 4 |
| `kotlin-multiplatform-sqldelight-setup` | When migrating from Room to SQLDelight |

### Tier 4 — UI system (after infrastructure is stable)

| Skill | When to apply |
|---|---|
| `kotlin-multiplatform-design-system` | When consolidating hardcoded colors/spacing |
| `kotlin-multiplatform-navigation` | When migrating from Fragments to Compose Navigation |
| `kotlin-multiplatform-preview-driven-development` | After Screen/Content split is in place |

### Tier 5 — Platform expansion (last)

| Skill | When to apply |
|---|---|
| `kotlin-multiplatform-expect-actual` | When extracting platform-specific code to KMP |
| `kotlin-multiplatform-xcframework-spm` | When adding iOS target |
| `kotlin-multiplatform-ci-github-actions` | When the architecture is stable enough to gate on CI |

---

## Migration Path A — MVVM + LiveData → MVI + StateFlow

Migrate one screen at a time. Never migrate all screens in one PR.

### Step 1 — Convert state to StateFlow (no MVI yet)

```kotlin
// Before — LiveData
class AuthViewModel : ViewModel() {
    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
}

// After step 1 — StateFlow, still flat (no Contract yet)
class AuthViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
}
```

Keep `observeAsState()` → `collectAsStateWithLifecycle()` in the Composable. No other
UI changes needed at this step. Ship and verify before proceeding.

### Step 2 — Consolidate into a single State class

```kotlin
// After step 2 — single State, still no Contract object
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value) }
}
```

### Step 3 — Add Intent and Effect (full MVI)

```kotlin
// After step 3 — full Contract + MviViewModel
object AuthContract {
    data class State(val email: String = "", val isLoading: Boolean = false, val error: String? = null)
    sealed interface Intent { ... }
    sealed interface Effect { ... }
}

class AuthViewModel : MviViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
    AuthContract.State()
) {
    override suspend fun handleIntent(intent: AuthContract.Intent) { ... }
}
```

**Only do Step 3 when there are actual Effects to handle.** A screen with no navigation
or toasts can stay at Step 2 indefinitely — that is not a violation.

---

## Migration Path B — Monolith → Multi-Module

Split one layer at a time, starting with the layer that has the least dependencies.

### Extraction order (least to most dependent)

```
1. :model    — data classes, no deps, safe to extract first
2. :api      — interfaces that depend only on :model
3. :domain   — use cases that depend on :api
4. :data     — impls that depend on :api (Ktor/SQLDelight)
5. :presenter — ViewModels that depend on :domain
6. :ui       — Composables that depend on :presenter
```

### Step-by-step: extracting `:model`

```kotlin
// 1. Create the module
// settings.gradle.kts
include(":feature:auth:model")

// 2. Move data classes — zero logic changes
// src/commonMain/kotlin/GROUP_ID/feature/auth/model/User.kt
data class User(val id: String, val email: String)

// 3. Add dependency in the consuming module
// feature/auth/build.gradle.kts (the old monolith)
dependencies {
    api(projects.feature.auth.model)
}

// 4. Verify build passes — no logic changed
```

Repeat for each layer. Each extraction is a separate PR. Never move more than one layer
per PR — it makes rollback impossible and reviewers miserable.

### When NOT to split

Do not extract a layer if:
- The feature has fewer than 3 screens and no plans to grow
- The "domain layer" would be empty (no use cases)
- The deadline is in 2 weeks and the team has never done multi-module before

A medium-tier feature (`:presenter` + `:ui`) is a valid end state, not a migration step.

---

## Migration Path C — Hilt → Koin 4

Migrate one Hilt module at a time. Hilt and Koin can coexist in the same app during migration.

```kotlin
// 1. Add Koin alongside Hilt — both can run at the same time
// build.gradle.kts
implementation(libs.koin.android)

// 2. Convert one @Module at a time
// Before (Hilt)
@Module @InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository = AuthRepositoryImpl(api)
}

// After (Koin)
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

// 3. Remove @Inject from the ViewModel, switch to koinViewModel()
// 4. Remove @HiltViewModel, @AndroidEntryPoint from Activity/Fragment
// 5. Remove Hilt from the module after full migration
```

---

## Common Anti-Patterns During Migration

- migrating all screens in one PR — untestable, hard to review, breaks bisect
- adding empty modules "for the future" — adds Gradle overhead and signals false completeness
- migrating ViewModel before adding tests — you won't know if you broke behavior
- keeping `LiveData` alongside `StateFlow` in the same ViewModel — pick one per class
- introducing `MviViewModel` on a screen that has no Effects — Step 2 is enough
- migrating the DI framework at the same time as the architecture — one change at a time

---

## Testing During Migration

Add tests **before** migrating a ViewModel, not after.

```kotlin
// Write this test first, using the existing MVVM ViewModel
class AuthViewModelMigrationTest {
    @Test
    fun `login success navigates to home`() = runTest {
        val vm = AuthViewModel(FakeAuthRepository())
        val navigated = mutableListOf<String>()
        // capture current navigation behavior
        ...
    }
}

// Then migrate the ViewModel — the test must still pass
```

This gives you a safety net. If the test breaks after migration, you introduced a
behavior change. Fix it before the PR.

---

## Related Skills

- `kotlin-multiplatform-clean-architecture` — the contract all migrations must satisfy
- `kotlin-multiplatform-mvi` — the target pattern for screen-level migration
- `kotlin-multiplatform-feature-scaffold` — the module structure to migrate toward
- `kotlin-multiplatform-audit` — run first; `--roadmap` flag gives the adoption plan
- `kotlin-multiplatform-unit-testing` — write tests before migrating each component

---

## Output Style

When helping with a migration:
1. Run or ask for the audit output first
2. State which migration path applies (A, B, C, or combination)
3. Propose the next single step only — not the full roadmap
4. After each step: verify build passes, tests pass, then propose the next step

Never propose more than one migration step at a time. Migration stalls when the scope
is too large to finish in a single session.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-28 | Initial release — assessment phase, adoption order, migration paths A/B/C. |
