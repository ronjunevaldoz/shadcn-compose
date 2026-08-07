# Decision Trees

Part of `kmp-expert`. Load this file when working on: decision trees.

---

### "Where does this code go?"

```
Is it platform-specific behavior?
├── YES: Does it wrap a platform SDK or require a platform type?
│   ├── YES → expect/actual (kmp-expect-actual)
│   └── NO  → interface + Koin injection in platform sourcesets
└── NO:
    ├── Is it a domain type (data class, sealed, enum)?  → :feature:x:model
    ├── Is it a repository interface or nav contract?    → :feature:x:api
    ├── Is it network communication?     → :core:network + network-layer skill
    ├── Is it local persistence?         → :core:database + sqldelight-setup skill
    ├── Is it domain logic?              → :feature:x:domain use cases
    ├── Is it data fetching + mapping?   → :feature:x:data repository-pattern skill
    ├── Is it ViewModel / UiState?       → :feature:x:presenter (presenter-module skill)
    ├── Is it a Compose screen?          → :feature:x:ui (mvi skill, Content composable)
    ├── Is it a reusable UI component?   → :core:designsystem slot-api + state-hoisting skills
    └── Is it app-wide config?           → :core:common or flavor-environment skill
```

### "Which state container?"

```
Does the state involve async, IO, or repository calls?
├── YES → ViewModel (mvi skill)
└── NO:
    ├── Must survive rotation? YES
    │   ├── Bundle-safe type? → rememberSaveable {}
    │   └── Complex type?     → rememberSaveable(stateSaver = customSaver)
    └── Must survive rotation? NO → remember {}
    └── Shared with another screen? → ViewModel (graph-scoped)
```

Full survival matrix: see `kmp-compose-state-container`.

### "Which transport for a backend call?"

Before following the tree below, check by **content**, not by module name, whether a
Ktor client already exists anywhere in the project — a new server module or feature
with a different name is still the same transport concern. Real bug this fixed: an
agent found no module literally named `:core:network` and defaulted to a raw HTTP call
instead of the project's actual (differently-named) client:

```bash
grep -rl "HttpClient(\|safeRequest\|NetworkResult<" */src --include="*.kt"
```

If that finds matches, reuse whatever module they're in — never scaffold a second client
or write a raw platform HTTP call because the path didn't match an assumed name. See
`kmp-network-layer`'s Step 0 for the full detection procedure.

```
grep -r "RemoteService\|@Rpc\|withRpc\|KtorRPCClient\|rpcClient\|\.rpc(" */src --include="*.kt" -l

Results found?
├── YES (kRPC is in the project):
│   ├── Does an existing RPC service interface expose this operation?
│   │   ├── YES → call through the RPC client; do NOT add safeRequest
│   │   └── NO  → extend the service interface with a new method; do NOT add a parallel HTTP call
│   └── Is the call to a DIFFERENT backend (external REST API, third-party service)?
│       └── YES → safeRequest is correct; this is a separate network boundary
└── NO (kRPC not present):
    ├── Is the backend a Kotlin-first Ktor server you control?
    │   ├── YES → consider kRPC (kmp-kotlin-rpc skill) before adding HTTP
    │   └── NO  → use safeRequest (kmp-network-layer skill)
    └── Is the backend a third-party REST API?
        └── YES → safeRequest is correct
```

### "expect/actual or interface?"

```
Is it a pure behavior difference (same API, different platform behavior)?
→ Interface + Koin injection

Does it require a platform-specific constructor argument (Context, UIViewController)?
→ expect class / typealias actual

Does it wrap a platform SDK with no clean interface abstraction?
→ expect class (Category 3 in expect-actual skill)

Is it a stateless primitive with no constructor (UUID, currentTimeMillis)?
→ expect fun (Category 4 in expect-actual skill)
```

Full guide: see `kmp-expect-actual`.

### "What layer does this DTO/entity/model belong to?"

```
NetworkDto (from Ktor JSON)      → stays inside :feature:x:data/remote/dto/
DatabaseEntity (from SQLDelight) → stays inside :feature:x:data/local/
DomainModel (data class)         → lives in :feature:x:model/
RepositoryInterface              → lives in :feature:x:api/
UiState / UiIntent               → lives in :feature:x:presenter/
Composable screen                → lives in :feature:x:ui/
```

The rule: data flows **inward** through mappers. DTOs and entities never cross the `:data`
boundary. Domain types (in `:model`) are the lingua franca across `:api`, `:domain`, and `:presenter`.

### "Improve the performance of X" — where do I even look?

There is no single performance skill — routing depends entirely on what X names.
Never guess at a target; if X is unnamed or app-wide ("the app feels slow"), ask the
user to narrow it to one of the branches below before picking a skill.

```
What is X?
├── A specific composable re-rendering too often / UI feels janky?
│   → kmp-compose-state-container (wrong container, e.g. ViewModel
│     for ephemeral state) or kmp-compose-state-hoisting (state
│     buried too deep, forcing a wide recomposition scope)
├── Custom drawing (Canvas, graphicsLayer, drawBehind) is slow?
│   → kmp-compose-graphics-modifiers
├── A JNI/native bridge call?
│   → kmp-jni-pro (minimize boundary crossings, batch marshalling,
│     GPU sync tips already in the skill)
├── Database queries?
│   → kmp-sqldelight-setup (indices, Flow query batching)
├── Network calls / sync?
│   → kmp-network-layer or kmp-offline-first
│     (cache-first, avoid redundant refresh)
├── App startup time or binary/APK size?
│   → kmp-proguard-r8
├── Web/Wasm target: slow page load, dropped frames in the browser, or bundle size?
│   → kmp-compose-web-performance (live browser profiling via chrome-devtools-mcp —
│     distinct from kmp-benchmark, which measures a Kotlin function in isolation,
│     not the running browser's own load/render cost)
├── A specific function/class flagged as complex (long, many params, deep nesting)?
│   → kmp-code-quality (Detekt `complexity:` rules — LongMethod,
│     CyclomaticComplexMethod, LongParameterList)
├── Need a real number, not a guess (comparing two implementations, confirming a fix)?
│   → kmp-benchmark
└── Unnamed / whole-app / "it feels slow"?
    → STOP — do not pick a skill on a guess. Ask which of the above the user means,
      or profile first (Android Studio Profiler / Instruments, or
      kmp-benchmark for a specific function/class) to get a concrete
      target, then re-route through this tree.
```

### "How do I handle audit findings?"

```
Finding confirmed?
├── NO → keep it as a question and ask the user for clarification
└── YES:
    ├── Needs tracking in the repo? → draft a GitHub issue
    └── Needs design/product input?  → draft a GitHub question
```

Include the skill name in every draft so attribution stays visible.

---

