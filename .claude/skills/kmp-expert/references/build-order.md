# Build Order for a New Project

Part of `kmp-expert`. Load this file when working on: build order for a new project.

---

### Phase 1: Foundation (do once per project)
1. **`clean-architecture`** — read the layer contract before writing any code
2. **`feature-scaffold`** — create the project from Kotlin/kmp-wizard, 6-layer module structure
3. **`flavor-environment`** — set up dev/staging/prod before writing any API code
4. **`network-layer`** — Ktor client, `NetworkResult`, auth interceptor
5. **`sqldelight-setup`** — local database, platform drivers, Koin wiring
6. **`logging`** — structured logging wrapper setup before any feature adds log calls
7. **`ci-github-actions`** — CI before any feature merges
8. **`code-quality`** — Ktlint + Detekt as CI gates from day one

### Phase 2: iOS/Desktop Readiness (if shipping to those platforms)
9. **`xcframework-spm`** — SPM binary target for iOS team
10. **`expect-actual`** — platform-specific code (UUID, SecureStorage, dispatchers)

### Phase 3: First Feature (repeat for each feature)
11. **`design-system`** — tokens and core components (once per project, before first feature)
12. **`navigation`** — add the feature's routes to the nav graph
13. **`shared-resources`** — add strings/assets the feature needs
14. **`repository-pattern`** — wire `RemoteDataSource` + `LocalDataSource` → `FooRepository`
15. **`presenter-module`** — `FooViewModel` (no Compose dep) + `FooUiState`/`FooUiIntent`
16. **`mvi`** — `FooScreen`/`FooContent` split consuming the presenter
17. **`preview-driven-development`** — Desktop `@Preview` for all states before wiring logic
18. **`unit-testing`** — `runTest` + Turbine tests for the ViewModel before shipping

### Phase 4: Richer UI & Quality (as needed)
19. **`design-system-extended`** — pull in Dialog, Sheet, Toast etc. when the feature needs them
20. **`compose-slot-api`** — when designing reusable components for the design system
21. **`compose-state-hoisting`** — when a component hierarchy gets complex
22. **`compose-state-container`** — when debugging state survival across rotation/back-nav
23. **`roborazzi`** — screenshot golden tests once the UI is stable

---

