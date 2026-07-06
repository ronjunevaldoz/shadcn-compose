---
name: kotlin-multiplatform-push-notifications
description: >-
  Push notification handling for Kotlin Multiplatform — a shared PushToken and
  NotificationPayload domain type in commonMain, Android FCM token registration
  and FirebaseMessagingService, iOS APNs token bridging to FCM, NotificationHandler
  expect/actual for foreground display and tap routing, and deep-link navigation
  from a notification tap.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - push notifications
    - FCM
    - APNs
    - Firebase Messaging
    - push token
    - NotificationHandler
    - FirebaseMessagingService
    - foreground notification
    - notification tap
    - deep link from notification
    - remote notification
    - KMP push notifications
    - notification payload
    - NotificationPayload
---

## When to Use This Skill

Use when:
- Registering for push notifications on Android (FCM) and iOS (APNs → FCM)
- Receiving the device push token and sending it to a backend
- Handling foreground notifications (show an in-app banner or dialog)
- Routing the user to a specific screen when they tap a notification
- Managing notification permission on Android 13+ (uses `Permission.PushNotifications`)

**Trigger keywords:** push notifications, FCM, APNs, Firebase Messaging, push token,
notification handler, FirebaseMessagingService, foreground notification, notification tap,
deep link from notification, remote notification, KMP push, NotificationPayload, token refresh.

**Freshness rule:** Firebase Messaging depends on the Firebase BoM — always use
`platform("com.google.firebase:firebase-bom:...")` and let BoM manage individual versions.
On iOS, APNs token must be bridged to FCM via `Messaging.messaging().apnsToken`. On
Android 13+, `POST_NOTIFICATIONS` is a dangerous permission (see `kotlin-multiplatform-permissions`
skill). FCM HTTP v1 API replaced the legacy API in 2024 — update backend senders accordingly.

---

## Recommendation First

Define `PushToken` and `NotificationPayload` in `commonMain`. Android handles everything
in `FirebaseMessagingService`; iOS bridges APNs to FCM in `AppDelegate`. Use
`expect/actual NotificationHandler` for showing foreground notifications — platform code
shows the notification channel (Android) or `UNUserNotificationCenter` (iOS). Deep-link
routing from a notification tap reuses the existing `DeepLinkParser`.

---

## Core types — commonMain

```kotlin
// :core:notifications:model — PushToken.kt
data class PushToken(val value: String, val platform: Platform) {
    enum class Platform { Android, iOS }
}

// :core:notifications:model — NotificationPayload.kt
data class NotificationPayload(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val data: Map<String, String> = emptyMap(),
)
```

---

## NotificationHandler — expect/actual

```kotlin
// commonMain — NotificationHandler.kt
expect class NotificationHandler {
    fun showForegroundNotification(payload: NotificationPayload)
    fun handleTap(payload: NotificationPayload)
}
```

---

## Android — FCM setup

**`libs.versions.toml`** (BoM manages individual versions):
```toml
firebase-bom      = { module = "com.google.firebase:firebase-bom",      version = "33.1.0" }
firebase-messaging = { module = "com.google.firebase:firebase-messaging" }
```

In `androidApp/build.gradle.kts`:
```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.messaging)
```

**AndroidManifest.xml**:
```xml
<service
    android:name=".AppFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**`AppFirebaseMessagingService.kt`**:
```kotlin
class AppFirebaseMessagingService : FirebaseMessagingService() {

    // Called when a new FCM token is generated
    override fun onNewToken(token: String) {
        val push = PushToken(token, PushToken.Platform.Android)
        // Send to backend via a WorkManager job to survive process death
        SendPushTokenWorker.enqueue(applicationContext, token)
    }

    // Called when a message arrives while app is in foreground
    override fun onMessageReceived(message: RemoteMessage) {
        val payload = NotificationPayload(
            title    = message.notification?.title.orEmpty(),
            body     = message.notification?.body.orEmpty(),
            deepLink = message.data["deep_link"],
            data     = message.data,
        )
        val handler = KoinAndroidApplication.get<NotificationHandler>()
        handler.showForegroundNotification(payload)
    }
}
```

**Android `NotificationHandler.android.kt`**:
```kotlin
actual class NotificationHandler(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "default_channel"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    actual fun showForegroundNotification(payload: NotificationPayload) {
        val intent = Intent(context, MainActivity::class.java).apply {
            payload.deepLink?.let { putExtra("deep_link", it) }
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    actual fun handleTap(payload: NotificationPayload) {
        payload.deepLink?.let { url ->
            val route = DeepLinkParser.parse(url).toRoute() ?: return
            // Post route to a shared event channel consumed by MainActivity
            DeepLinkEventBus.emit(route)
        }
    }
}
```

---

## Android — retrieve current FCM token on startup

```kotlin
// In Application.onCreate or a startup Initializer
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = PushToken(task.result, PushToken.Platform.Android)
        // send to backend
    }
}
```

---

## iOS — APNs bridge to FCM

In `AppDelegate.swift`:

```swift
import FirebaseMessaging
import UserNotifications

@main
class AppDelegate: UIResponder, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        // Request permission (iOS 10+)
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
        application.registerForRemoteNotifications()
        return true
    }

    // APNs token — bridge to FCM
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    // FCM token refresh
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        // Call Kotlin via KMP bridging: sendPushToken(token)
        PushTokenBridge.shared.onNewToken(token: token)
    }

    // Foreground notification
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                 willPresent notification: UNNotification,
                                 withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .badge])
    }

    // Notification tap
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                 didReceive response: UNNotificationResponse,
                                 withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        if let deepLink = userInfo["deep_link"] as? String {
            DeepLinkHandler.shared.emit(deepLink)
        }
        completionHandler()
    }
}
```

---

## Android 13+ notification permission

Use `Permission.PushNotifications` from the `kotlin-multiplatform-permissions` skill:

```kotlin
// In the onboarding or home ViewModel
viewModelScope.launch {
    val state = permissionController.checkState(Permission.PushNotifications)
    if (state != PermissionState.Granted) {
        permissionController.requestPermission(Permission.PushNotifications)
    }
}
```

---

## Testing

```kotlin
class FakeNotificationHandler : NotificationHandler {
    val tokens = mutableListOf<PushToken>()
    val received = mutableListOf<NotificationPayload>()

    override fun onTokenRefresh(token: PushToken) { tokens += token }
    override fun onNotificationReceived(payload: NotificationPayload) { received += payload }
}

@Test fun `token refresh stores new token`() = runTest {
    val handler = FakeNotificationHandler()
    handler.onTokenRefresh(PushToken("device-token-abc"))
    assertEquals(1, handler.tokens.size)
    assertEquals("device-token-abc", handler.tokens.first().value)
}

@Test fun `received notification forwarded correctly`() = runTest {
    val handler = FakeNotificationHandler()
    val payload = NotificationPayload(
        title = "New message",
        body = "You have a reply",
        data = mapOf("thread_id" to "42"),
    )
    handler.onNotificationReceived(payload)
    assertEquals(1, handler.received.size)
    assertEquals("New message", handler.received.first().title)
    assertEquals("42", handler.received.first().data["thread_id"])
}

@Test fun `multiple tokens only the latest is relevant`() = runTest {
    val handler = FakeNotificationHandler()
    handler.onTokenRefresh(PushToken("old-token"))
    handler.onTokenRefresh(PushToken("new-token"))
    assertEquals("new-token", handler.tokens.last().value)
}
```

> Platform registration (FCM `onNewToken`, APNs `didRegisterForRemoteNotificationsWithDeviceToken`)
> is tested via instrumented tests on Android and XCTest on iOS. The shared `NotificationHandler`
> interface covers the cross-platform logic testable in `commonTest`.

---

## Common Anti-Patterns

- **Sending the push token directly from `onNewToken` over the network** — `onNewToken`
  can be called when the network is unavailable; always enqueue a `WorkManager` one-time
  job to persist and retry the token upload
- **Using the legacy FCM HTTP API** — the legacy send API was deprecated in June 2023 and
  shut down; all backend senders must use the HTTP v1 API with OAuth 2.0 service account
- **Not bridging APNs token to FCM on iOS** — Firebase on iOS needs the raw APNs device
  token to generate an FCM registration token; set `Messaging.messaging().apnsToken` in
  `didRegisterForRemoteNotificationsWithDeviceToken`
- **Assuming `onMessageReceived` is called for all messages** — FCM only calls `onMessageReceived`
  for data-only messages and notification messages when the app is in the foreground;
  background notification messages bypass the service and go directly to the system tray
- **Hardcoded notification channel ID strings** — define the channel ID as a constant and
  use the same value in `NotificationChannel` creation and `NotificationCompat.Builder`

---

## Related Skills

- `kotlin-multiplatform-permissions` — Android 13+ `POST_NOTIFICATIONS` permission is a
  dangerous permission; request it using the permissions skill's `PermissionController`
- `kotlin-multiplatform-deep-linking` — notification taps route the user via `DeepLinkParser`;
  the same `DeepLink` sealed class and `toRoute()` extension are reused
- `kotlin-multiplatform-workmanager` — token upload should be a one-time `WorkManager`
  request so it survives process death and network unavailability

---

## Output Style

When implementing push notifications, respond in this order:
1. **PushToken + NotificationPayload** — domain types in commonMain
2. **Android setup** — BoM dependency, manifest, `FirebaseMessagingService` subclass
3. **iOS setup** — AppDelegate FCM configure, APNs bridge, `MessagingDelegate`
4. **NotificationHandler** — expect/actual: channel creation (Android), UNUserNotificationCenter (iOS)
5. **Token retrieval** — startup fetch + `onNewToken` → WorkManager enqueue
6. **Notification permission** — Android 13+ permission request
7. **Deep-link routing** — tap handler → `DeepLinkParser` → `DeepLinkEventBus`

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
