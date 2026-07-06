---
name: kotlin-multiplatform-permissions
description: >-
  Runtime permission handling for Kotlin Multiplatform — a sealed PermissionState
  type in commonMain, expect/actual PermissionController, Android Activity-based
  launcher, iOS Info.plist keys and AVFoundation/CoreLocation usage, and
  cross-platform Compose UI for rationale dialogs.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - permissions
    - runtime permission
    - PermissionState
    - expect actual
    - Android permission
    - iOS permission
    - camera permission
    - location permission
    - permission controller
    - permission rationale
    - Accompanist permissions
    - KMP permissions
---

## When to Use This Skill

Use when:
- A feature requires a dangerous permission (camera, location, microphone, contacts, storage)
- The app must show a rationale dialog when the user has previously denied a permission
- Permission request logic must be testable in commonMain
- iOS requires a `NSCameraUsageDescription` (or equivalent) Info.plist key

**Trigger keywords:** permissions, runtime permission, PermissionState, camera permission,
location permission, microphone permission, permission denied, permission rationale,
request permission, permission controller, Accompanist permissions KMP, expect actual permission,
iOS Info.plist permission, permission flow.

**Freshness rule:** On Android, `ActivityResultContracts.RequestPermission` replaced the
deprecated `ActivityCompat.requestPermissions` — always use the modern launcher contract.
On iOS, every new permission category requires its own `NS*UsageDescription` key in `Info.plist`;
check Apple docs for the exact key name when adding a new permission type.

---

## Recommendation First

Define a `Permission` enum and `PermissionState` sealed class in `commonMain`. Use
`expect/actual` for `PermissionController` to keep platform-specific launcher code out of
shared logic. The ViewModel holds `PermissionState` in `UiState` and delegates the request
to the controller — it never calls platform APIs directly.

---

## Core types — commonMain

```kotlin
// :core:permissions:model — Permission.kt
enum class Permission {
    Camera,
    Location,
    LocationAlways,
    Microphone,
    Contacts,
    PushNotifications,
}

// :core:permissions:model — PermissionState.kt
sealed class PermissionState {
    object Unknown    : PermissionState()   // never requested or cold start
    object Granted    : PermissionState()
    object Denied     : PermissionState()   // denied once, may re-request
    object PermanentlyDenied : PermissionState()  // must open Settings
}
```

---

## PermissionController — expect/actual

```kotlin
// commonMain — PermissionController.kt
expect class PermissionController {
    suspend fun checkState(permission: Permission): PermissionState
    suspend fun requestPermission(permission: Permission): PermissionState
    fun openAppSettings()
}
```

---

## Android implementation

```kotlin
// androidMain — PermissionController.android.kt
actual class PermissionController(private val activity: ComponentActivity) {

    actual suspend fun checkState(permission: Permission): PermissionState {
        val androidPerm = permission.toAndroidPermission()
        return when {
            ContextCompat.checkSelfPermission(activity, androidPerm) ==
                    PackageManager.PERMISSION_GRANTED -> PermissionState.Granted
            ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPerm) ->
                PermissionState.Denied
            else -> PermissionState.Unknown
        }
    }

    actual suspend fun requestPermission(permission: Permission): PermissionState =
        suspendCoroutine { cont ->
            val launcher = activity.activityResultRegistry.register(
                "perm_${permission.name}", ActivityResultContracts.RequestPermission()
            ) { granted ->
                cont.resume(if (granted) PermissionState.Granted else PermissionState.Denied)
            }
            launcher.launch(permission.toAndroidPermission())
        }

    actual fun openAppSettings() {
        activity.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
        )
    }

    private fun Permission.toAndroidPermission(): String = when (this) {
        Permission.Camera           -> android.Manifest.permission.CAMERA
        Permission.Location         -> android.Manifest.permission.ACCESS_FINE_LOCATION
        Permission.LocationAlways   -> android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        Permission.Microphone       -> android.Manifest.permission.RECORD_AUDIO
        Permission.Contacts         -> android.Manifest.permission.READ_CONTACTS
        Permission.PushNotifications -> android.Manifest.permission.POST_NOTIFICATIONS
    }
}
```

---

## iOS implementation

```kotlin
// iosMain — PermissionController.ios.kt
actual class PermissionController {

    actual suspend fun checkState(permission: Permission): PermissionState =
        when (permission) {
            Permission.Camera -> checkCamera()
            Permission.Location, Permission.LocationAlways -> checkLocation()
            Permission.Microphone -> checkMicrophone()
            else -> PermissionState.Unknown
        }

    actual suspend fun requestPermission(permission: Permission): PermissionState =
        suspendCoroutine { cont ->
            when (permission) {
                Permission.Camera -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    cont.resume(if (granted) PermissionState.Granted else PermissionState.PermanentlyDenied)
                }
                Permission.Location -> CLLocationManager().requestWhenInUseAuthorization()
                    .also { cont.resume(PermissionState.Unknown) }   // result comes via delegate
                Permission.Microphone -> AVAudioSession.sharedInstance()
                    .requestRecordPermission { granted ->
                        cont.resume(if (granted) PermissionState.Granted else PermissionState.PermanentlyDenied)
                    }
                else -> cont.resume(PermissionState.Unknown)
            }
        }

    actual fun openAppSettings() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it, mapOf<Any?, Any>(), null)
        }
    }

    private fun checkCamera(): PermissionState =
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized   -> PermissionState.Granted
            AVAuthorizationStatusDenied,
            AVAuthorizationStatusRestricted   -> PermissionState.PermanentlyDenied
            else                              -> PermissionState.Unknown
        }

    private fun checkMicrophone(): PermissionState =
        when (AVAudioSession.sharedInstance().recordPermission()) {
            AVAudioSessionRecordPermissionGranted  -> PermissionState.Granted
            AVAudioSessionRecordPermissionDenied   -> PermissionState.PermanentlyDenied
            else                                  -> PermissionState.Unknown
        }

    private fun checkLocation(): PermissionState =
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.Granted
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted       -> PermissionState.PermanentlyDenied
            else                                   -> PermissionState.Unknown
        }
}
```

---

## iOS Info.plist keys

Add the relevant usage description to `iosApp/Info.plist`:

```xml
<!-- Camera -->
<key>NSCameraUsageDescription</key>
<string>This app needs camera access to scan QR codes.</string>

<!-- Location -->
<key>NSLocationWhenInUseUsageDescription</key>
<string>This app needs your location to show nearby items.</string>

<!-- Microphone -->
<key>NSMicrophoneUsageDescription</key>
<string>This app needs microphone access to record voice notes.</string>
```

---

## ViewModel

```kotlin
// :presenter — CameraViewModel.kt
class CameraViewModel(
    private val permissionController: PermissionController,
) : ViewModel() {

    private val _state = MutableStateFlow(CameraContract.State())
    val state: StateFlow<CameraContract.State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val current = permissionController.checkState(Permission.Camera)
            _state.update { it.copy(cameraPermission = current) }
        }
    }

    fun onIntent(intent: CameraContract.Intent) {
        when (intent) {
            CameraContract.Intent.RequestCameraPermission -> {
                viewModelScope.launch {
                    val result = permissionController.requestPermission(Permission.Camera)
                    _state.update { it.copy(cameraPermission = result) }
                }
            }
            CameraContract.Intent.OpenSettings ->
                permissionController.openAppSettings()
        }
    }
}
```

---

## UI — permission gate composable

```kotlin
// :ui — PermissionGate.kt
@Composable
fun CameraPermissionGate(
    permissionState: PermissionState,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable () -> Unit,
) {
    when (permissionState) {
        PermissionState.Granted  -> content()
        PermissionState.Unknown, PermissionState.Denied -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Camera access is needed", style = AppTheme.typography.titleMedium)
                Spacer(Modifier.height(AppTheme.spacing.sm))
                AppButton("Allow camera", onClick = onRequest)
            }
        }
        PermissionState.PermanentlyDenied -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Camera access was denied", style = AppTheme.typography.titleMedium)
                Spacer(Modifier.height(AppTheme.spacing.sm))
                AppButton("Open Settings", onClick = onOpenSettings)
            }
        }
    }
}
```

---

## Koin wiring

```kotlin
// androidMain
val permissionsModule = module {
    single { PermissionController(get<ComponentActivity>()) }
}

// iosMain
val permissionsModule = module {
    single { PermissionController() }
}
```

---

## Testing

```kotlin
class FakePermissionController(
    private var state: PermissionState = PermissionState.Unknown,
) : PermissionController() {   // or extract an interface in commonMain
    override suspend fun checkState(permission: Permission) = state
    override suspend fun requestPermission(permission: Permission): PermissionState {
        state = PermissionState.Granted
        return state
    }
    override fun openAppSettings() = Unit
}

@Test fun `viewmodel reflects granted state after request`() = runTest {
    val fake = FakePermissionController(PermissionState.Unknown)
    val vm = CameraViewModel(fake)
    vm.onIntent(CameraContract.Intent.RequestCameraPermission)
    assertEquals(PermissionState.Granted, vm.state.value.cameraPermission)
}
```

---

## Common Anti-Patterns

- **Calling `ContextCompat.checkSelfPermission` in a ViewModel** — ViewModel is in
  `commonMain`; platform API calls belong in the `actual` implementation; pass
  `PermissionState` into the ViewModel via the controller
- **Showing the rationale dialog before checking current state** — always check state
  first; if permission is already `Granted`, bypass the rationale entirely
- **Missing Info.plist key on iOS** — the app crashes with an unhandled exception at the
  first permission request if the usage description key is absent; add it proactively
- **Requesting `ACCESS_BACKGROUND_LOCATION` alongside foreground location in the same call** —
  Android 11+ requires requesting background location separately, after the foreground
  permission is granted
- **Not handling `PermanentlyDenied`** — the OS will silently do nothing on repeated
  requests; detect this state and direct the user to Settings instead

---

## Related Skills

- `kotlin-multiplatform-mvi` — `PermissionState` lives inside `UiState` as a field;
  permission request is an `Intent`
- `kotlin-multiplatform-dependency-injection` — `PermissionController` is a Koin single
  bound in platform modules because it requires a platform context or activity
- `kotlin-multiplatform-push-notifications` — push notification permissions on Android 13+
  use `Permission.PushNotifications` from this skill

---

## Output Style

When implementing permissions, respond in this order:
1. **Permission enum** — list only the permissions the feature needs
2. **PermissionState** — sealed class (no changes needed if already present)
3. **expect/actual PermissionController** — platform method bodies
4. **Info.plist keys** — iOS usage description strings for each permission
5. **ViewModel intent + state** — `checkState` in `init`, `requestPermission` intent
6. **PermissionGate composable** — screen-level gate with rationale and settings fallback
7. **Fake + test** — `FakePermissionController` and one ViewModel test

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
