# Typed Domain Errors

Part of `kmp-clean-architecture`.

---

Typed errors let callers distinguish and handle failure cases without parsing strings.
They live in `:model` (if shared) or `:feature:*:model` (if feature-specific).

```kotlin
// :core:model or :feature:auth:model
sealed class AuthError {
    data object InvalidCredentials : AuthError()
    data object AccountLocked : AuthError()
    data class NetworkError(val cause: Throwable) : AuthError()
    data class Unknown(val cause: Throwable) : AuthError()
}
```

Repository interface in `:api` returns `Result<T>` wrapping the typed error:

```kotlin
// :feature:auth:api
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    // throws AuthError subtypes captured in Result.failure(...)
}
```

The `:data` impl maps HTTP/network errors to the sealed type:

```kotlin
override suspend fun login(email: String, password: String): Result<User> = runCatching {
    api.login(email, password).toDomain()
}.mapFailure { cause ->
    when {
        cause is HttpException && cause.code == 401 -> AuthError.InvalidCredentials
        cause is HttpException && cause.code == 423 -> AuthError.AccountLocked
        cause is IOException -> AuthError.NetworkError(cause)
        else -> AuthError.Unknown(cause)
    }
}
```

The `:presenter` maps `AuthError` to a `UiError` for display — domain errors never flow
to the UI layer as-is (see `kmp-mvi` for the `UiError` sealed type).

---

