---
name: kotlin-multiplatform-image-loading
description: >-
  Image loading for Kotlin Multiplatform using Coil 3 — AsyncImage, cache policy,
  placeholder and error states, circular/rounded clipping, local resource images,
  Koin wiring of ImageLoader, and memory/disk cache tuning. Coil 3 is KMP-ready
  for Android, iOS, Desktop, and WASM targets.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - image loading
    - Coil 3
    - AsyncImage
    - coil-compose
    - image cache
    - placeholder
    - error image
    - network image
    - KMP image
    - SubcomposeAsyncImage
    - ImageLoader
    - disk cache
    - memory cache
---

## When to Use This Skill

Use when:
- Loading images from a URL in a Compose Multiplatform screen
- Showing a placeholder while the image loads and an error state when it fails
- Displaying circular avatars, rounded thumbnails, or full-bleed hero images
- Tuning memory/disk cache size for image-heavy screens
- Loading local bundled images from `Res` alongside network images

**Trigger keywords:** image loading, Coil, Coil 3, AsyncImage, coil-compose, network image,
load image from URL, image placeholder, image error state, circular image, avatar image,
image cache, disk cache, SubcomposeAsyncImage, ImageLoader KMP, Kamel, image loading KMP.

**Freshness rule:** Coil 3 became KMP-ready in 3.x — verify the version in `libs.versions.toml`
lists `io.coil-kt.coil3` (not `io.coil-kt`). Coil 3 APIs differ significantly from Coil 2.
Recheck the [Coil 3 migration guide](https://coil-kt.github.io/coil/upgrading_to_coil3/) before
upgrading. iOS target requires the Kotlin/Native memory model.

---

## Recommendation First

Use `AsyncImage` for the common case. Use `SubcomposeAsyncImage` only when you need
to compose custom loading/error UI inline — it has higher recomposition cost.
Create a single `ImageLoader` instance via Koin and share it across the app; do not
create a new loader per composable.

---

## `libs.versions.toml` additions

```toml
[versions]
coil = "3.1.0"

[libraries]
coil-compose      = { module = "io.coil-kt.coil3:coil-compose",       version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor",  version.ref = "coil" }
```

In `commonMain`:
```kotlin
implementation(libs.coil.compose)
implementation(libs.coil.network.ktor)
```

---

## Koin wiring — single ImageLoader

```kotlin
// commonMain — imageLoaderModule
val imageLoaderModule = module {
    single<ImageLoader> {
        ImageLoader.Builder(get<android.content.Context>())   // Android
            // iOS / Desktop: use the platform context via expect/actual
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(0.25)   // 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(get<okio.FileSystem>().canonicalPath(okio.Path.of("image_cache")))
                    .maxSizeBytes(50L * 1024 * 1024)   // 50 MB
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory(get()))   // reuse the app's Ktor HttpClient
            }
            .build()
    }
}
```

For Android, the `Context` comes from Koin's Android setup. For iOS/Desktop use the
`LocalContext` equivalent or a no-context builder variant from Coil 3.

---

## Basic network image

```kotlin
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model             = url,
        contentDescription = contentDescription,
        contentScale      = contentScale,
        placeholder       = painterResource(Res.drawable.img_placeholder),
        error             = painterResource(Res.drawable.img_error),
        modifier          = modifier,
    )
}
```

---

## Circular avatar

```kotlin
@Composable
fun AvatarImage(
    url: String?,
    size: Dp = 40.dp,
    contentDescription: String? = null,
) {
    AsyncImage(
        model             = url,
        contentDescription = contentDescription,
        contentScale      = ContentScale.Crop,
        placeholder       = painterResource(Res.drawable.ic_avatar_placeholder),
        error             = painterResource(Res.drawable.ic_avatar_placeholder),
        modifier          = Modifier
            .size(size)
            .clip(CircleShape),
    )
}
```

---

## Custom loading/error UI (SubcomposeAsyncImage)

```kotlin
@Composable
fun HeroImage(url: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model    = url,
        contentDescription = null,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    Modifier.fillMaxSize().background(AppTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.primary)
                }
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    Modifier.fillMaxSize().background(AppTheme.colors.errorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(Icons.Default.BrokenImage, contentDescription = "Failed to load")
                }
            }
            else -> SubcomposeAsyncImageContent()
        }
    }
}
```

---

## Local bundled image alongside network

```kotlin
// Prefer painterResource for local — no loader needed
Image(
    painter            = painterResource(Res.drawable.img_onboarding),
    contentDescription = "Onboarding illustration",
    modifier           = Modifier.fillMaxWidth(),
)

// For dynamic choice between local and remote:
@Composable
fun SmartImage(source: ImageSource, modifier: Modifier = Modifier) {
    when (source) {
        is ImageSource.Remote -> AsyncImage(
            model = source.url, contentDescription = null, modifier = modifier
        )
        is ImageSource.Local  -> Image(
            painter = painterResource(source.resId), contentDescription = null, modifier = modifier
        )
    }
}

sealed class ImageSource {
    data class Remote(val url: String) : ImageSource()
    data class Local(val resId: DrawableResource) : ImageSource()
}
```

---

## Testing

```kotlin
// Coil 3 exposes ImageLoader as an interface — create a fake for unit/UI tests
class FakeImageLoader : ImageLoader {
    var requestCount = 0

    override suspend fun execute(request: ImageRequest): ImageResult {
        requestCount++
        return SuccessResult(
            drawable = ColorDrawable(android.graphics.Color.GRAY),
            request = request,
            dataSource = DataSource.MEMORY_CACHE,
        )
    }

    override fun enqueue(request: ImageRequest): Disposable {
        requestCount++
        return object : Disposable {
            override val job get() = CompletableDeferred(Unit as Any?)
            override val isDisposed = false
            override fun dispose() = Unit
        }
    }

    override fun newBuilder() = error("not needed in tests")
    override fun shutdown() = Unit
    override val components: ComponentRegistry get() = ComponentRegistry()
    override val defaults: DefaultRequestOptions get() = DefaultRequestOptions()
    override val diskCache: DiskCache? = null
    override val memoryCache: MemoryCache? = null
}

@get:Rule val composeRule = createComposeRule()

@Test fun `async image triggers a load request`() {
    val loader = FakeImageLoader()
    composeRule.setContent {
        CompositionLocalProvider(LocalImageLoader provides loader) {
            AsyncImage(
                model = "https://example.com/photo.jpg",
                contentDescription = null,
                modifier = Modifier.testTag("image"),
            )
        }
    }
    composeRule.onNodeWithTag("image").assertExists()
    assertTrue(loader.requestCount > 0)
}
```

---

## Common Anti-Patterns

- **Creating `ImageLoader` per composable** — each loader creates its own memory/disk cache;
  create exactly one via Koin and let Coil use it everywhere via `LocalImageLoader`
- **Using Coil 2 imports with Coil 3** — `io.coil-kt:coil-compose` (Coil 2) vs
  `io.coil-kt.coil3:coil-compose` (Coil 3) have different APIs; mixing them causes
  duplicate class errors
- **Hardcoded image size in `AsyncImage`** — always constrain image size via `Modifier.size()`
  or `fillMaxWidth()` before calling `AsyncImage`; an unconstrained image causes a
  layout measurement loop
- **Using `SubcomposeAsyncImage` everywhere** — it subcomposes the content on each frame
  during loading; use `AsyncImage` with `placeholder`/`error` painters for the common case
- **No `contentDescription` on meaningful images** — accessibility requires a description
  for images that convey meaning; pass `null` only for purely decorative images

---

## Related Skills

- `kotlin-multiplatform-design-system` — placeholder and error drawables should come from
  the design system's resource set via `Res.drawable.*`
- `kotlin-multiplatform-shared-resources` — bundled images live under `commonMain/composeResources`
  and are accessed via `Res.drawable.*`; wire them in before using `painterResource()`
- `kotlin-multiplatform-network-layer` — Coil's `KtorNetworkFetcherFactory` reuses the app's
  existing `HttpClient`; inject it via Koin rather than creating a second client

---

## Output Style

When implementing image loading, respond in this order:
1. **TOML additions** — Coil 3 versions
2. **Koin ImageLoader** — single instance with memory + disk cache
3. **Component** — `NetworkImage`, `AvatarImage`, or `HeroImage` depending on the use case
4. **Placeholder/error resources** — confirm they exist in `commonMain/composeResources`
5. **Local vs remote** — `SmartImage` wrapper only if needed

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
