# Required vs Optional Skills

Part of `kmp-expert`.

---

Classify every skill into one of five bands before recommending. Always cover the **Required core** first; pull in lower bands only when the task or the app's capabilities demand them.

### Required core (every KMP feature)
These implement the architecture contract — no proper feature ships without them.

| Skill | Why required |
|---|---|
| `clean-architecture` | The 6-layer contract — the rules everything else obeys |
| `feature-scaffold` | Module structure, build-logic, version catalog |
| `presenter-module` | Every feature has a no-Compose, JVM-testable ViewModel |
| `mvi` | The Screen/Content state pattern for every screen |
| `dependency-injection` | Koin wiring spans every layer |

### Conditionally required (depends on app capability)
Required **if** the app has that capability — most production apps do.

| Skill | Required when… |
|---|---|
| `network-layer` | App calls any backend/API |
| `sqldelight-setup` **or** `datastore` | App persists data (DB vs key-value) |
| `repository-pattern` | App has both network and local storage |
| `navigation` | App has more than one screen |
| `design-system` | App renders any custom UI |
| `shared-resources` | App needs localization / strings / assets |
| `expect-actual` | App needs platform-specific code |
| `xcframework-spm` | Shipping a shared framework to an iOS team |

### Strongly recommended (project health)
Optional in theory; skipping them costs quality and velocity.

`flavor-environment`, `ci-github-actions`, `code-quality`, `logging`, `unit-testing`, `preview-driven-development`

### Optional (feature-specific)
Pull in only when a feature explicitly needs it.

`design-system-extended`, `adaptive-layout`, `compose-slot-api`, `compose-state-hoisting`, `compose-state-container`, `compose-animation`, `graphics-modifiers`, `roborazzi`, `accessibility`, `paging`, `analytics`, `form-validation`, `image-loading`, `permissions`, `deep-linking`, `biometric-auth`, `push-notifications`, `workmanager`, `feature-flags`, `crash-reporting`, `ktor-auth-service`, `mongodb-database`, `kotlin-rpc`, `legal-docs`, `release`

### Opt-in (never auto-select — must be named explicitly)
- `offline-first` — only when the user names "offline-first", "background sync", or "conflict resolution". For plain caching or a local source of truth, use `repository-pattern` + `sqldelight-setup` instead. Offline-first layers `SyncManager` + `WorkManager`/`BGTaskScheduler` on top, which is overkill unless explicitly wanted.

### Meta (tooling, not app code)
`expert` (routing), `audit` (review), `kmp-jni-pro` (native bridge), `docs-maintainer`, `changelog`, `benchmark` (invoked on-demand for a specific performance claim — never scaffolded speculatively), `docs-site` (public developer guide — library-only, gated on real surface area, never scaffolded for an app or a trivial library)

---

