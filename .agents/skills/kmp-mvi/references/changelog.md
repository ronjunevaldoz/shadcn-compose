# Changelog

Part of `kmp-mvi`. Load this file when working on: changelog.

---

| Date | Change |
|---|---|
| 2026-08-04 | Split SKILL.md (1626 lines) into 9 `references/*.md` files, leaving pointer stubs under each heading plus a new References section. SKILL.md drops to 500 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as `kmp-compose-design-system`/`-extended` (KI-008). |
| 2026-07-26 | Real gap closed: this skill's own earlier changelog called the ViewModel-depends-only-on-`:domain` rule "bright-line and mechanically checkable," but nothing actually checked it — `_detect_module_layer_violation` can't, since `presenter -> api` is an allowed module-level edge. Added `kmp-audit`'s new `_detect_viewmodel_injects_repository`, a file-level check on the ViewModel's constructor param types. 1 new anti-pattern. |
| 2026-07-26 | Renamed the previous entry's section to "Field count alone isn't the test — Divergent Change is" and named the smell properly (Fowler's *Refactoring* catalog — same family as Long Parameter List/Primitive Obsession already named elsewhere in this collection) instead of an ad-hoc "relatedness litmus test" phrase, for a better search term. Added `Divergent Change`/`God State` to keywords and trigger keywords. |
| 2026-07-26 | Added "Field count alone isn't the test — relatedness is" — clarifies the existing ~8-field god-ViewModel symptom, which couldn't distinguish a cohesive multi-field `State` (`SearchState`) from an unrelated-concerns `State` (a real `ChatShellState` mixing chat/project/session). Gives a naming litmus test instead of a count. Deliberately not mechanically detected — same treatment as the Parameter Object regression in `kmp-code-quality`. |
| 2026-07-26 | Added two more god-ViewModel signals beyond line count: 15+ `Intent` variants, and 2+ exposed `StateFlow` properties beyond `state`. Backed by `kmp-audit`'s new `_detect_viewmodel_too_many_intents`/`_detect_viewmodel_multiple_stateflows` — real gap, since `_detect_viewmodel_size` alone can miss a terse-but-overloaded ViewModel. 2 new anti-patterns. |
| 2026-07-26 | Added a de-escalation guardrail: a Coordinator ViewModel (Option 2/3) is not exempt from `_detect_viewmodel_size`'s god-ViewModel threshold — a real gap where choosing a coordinator to escape a god composable could quietly relocate the same size problem instead of fixing it. If a coordinator keeps growing after delegating to State Holders/use cases, that's a signal to split back to Option 1. 1 new anti-pattern. |
| 2026-06-28 | Add @Stable/@Immutable rule for State types; CoroutineExceptionHandler in MviViewModel base class; rememberUpdatedState section with decision table. Three new anti-patterns.
| 2026-06-28 | Add multi-source state: combine(), WhileSubscribed(5_000) table, flatMapLatest, snapshotFlow with debounce example. Four new anti-patterns.
| 2026-06-28 | Add collectAsStateWithLifecycle vs collectAsState rule; LaunchedEffect vs DisposableEffect vs SideEffect decision table; SavedStateHandle + viewModelOf Koin wiring; four new anti-patterns.
| 2026-07-09 | Added Koin Compose ViewModel / AndroidX lifecycle compatibility note and Wasm verification reminder for MVI screens that use Koin-backed ViewModels. |
| 2026-06-28 | Add auth gate and back-stack anti-patterns. Two new anti-patterns: storing auth state in MVI State for nav, and Effect.NavigateBack without popUpTo contract. |
| 2026-06-28 | Add ViewModel size rule, god ViewModel symptoms, use case extraction guide, and ViewModel split patterns. Two new anti-patterns for monolithic ViewModels. |
| 2026-06-29 | Reworked feature-orchestration guidance into a decision order led by Option 1 (separate screens + NavHost + repository as source of truth) before any coordinator. Two hard rules (no VM-in-VM, share via repository only). Hardened rules section mapped to audit findings. |
| 2026-06-29 | Coordinator ViewModel section rewritten to State Holder pattern — a ViewModel must never take another ViewModel as a constructor param; demote sub-units to State Holders (plain class + injected scope) or use cases. New anti-pattern + audit detector for VM-in-VM constructor. |
| 2026-06-29 | Added Coordinator ViewModel section — fixes god composables that orchestrate multiple sub-ViewModels in the UI layer (state assembly, effect relays, persistence in LaunchedEffect). New "god composable" anti-pattern. Detected by audit_project.py. |
| 2026-06-28 | Added "When NOT to Use MviViewModel" with thin patterns (no-ViewModel, no-Contract). Updated Recommendation First to lead with start-thin principle. Added Nav Args as Initial State, In-flight Cancellation, Typed Errors in State, Shared ViewModel. |
| 2026-06-06 | Initial release. |
