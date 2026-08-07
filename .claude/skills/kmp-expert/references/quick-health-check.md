# Quick Health Check for Existing Projects

Part of `kmp-expert`. Load this file when working on: quick health check for existing projects.

---

Run through these 6 questions for any KMP project audit:

1. **Dependency direction**: do `:ui` or `:domain` modules ever import from `:data`?
   If yes → architectural violation; data layer details are leaking.

2. **Presenter boundary**: does `:presenter` import `androidx.compose.*` or `org.jetbrains.compose.*`?
   If yes → ViewModels cannot be tested on JVM; move Compose to `:ui` only.

3. **Network/DB types at the boundary**: does any `UiState` contain a `Dto`, `Entity`,
   or `NetworkResult`? If yes → mapping is missing at the repository boundary.

4. **Effect delivery**: are effects `SharedFlow` or `StateFlow`? They should be `Channel<Effect>`.
   `SharedFlow` can replay effects (double navigation, double toast).

5. **State atomicity**: are there any `_state.value = _state.value.copy(...)` calls?
   They should be `_state.update { it.copy(...) }` to be thread-safe under concurrent intents.

6. **Expect/actual ratio**: what fraction of platform files have identical implementations?
   High ratio → probable over-use of expect/actual; move shared logic to `commonMain`.

