---
name: kotlin-multiplatform-workmanager
description: >-
  Background task scheduling for Kotlin Multiplatform — WorkManager (Android) and
  BGTaskScheduler / BGProcessingTask (iOS) behind an expect/actual BackgroundScheduler.
  Covers one-time work, periodic work, retry with backoff, input/output data, and
  chained work on Android. iOS uses BGAppRefreshTask for short frequent tasks and
  BGProcessingTask for long network or maintenance jobs.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - WorkManager
    - background work
    - BackgroundScheduler
    - BGTaskScheduler
    - BGProcessingTask
    - BGAppRefreshTask
    - one-time work
    - periodic work
    - retry backoff
    - background task
    - WorkRequest
    - CoroutineWorker
    - expect actual background
    - KMP background tasks
    - background sync
---

## When to Use This Skill

Use when:
- Running a deferred task that must complete even if the app is backgrounded (push token upload, log flush)
- Scheduling periodic background syncs (hourly cache refresh, daily cleanup)
- Running a long-running network or DB task on iOS that needs more than the 30-second background time
- Retrying failed network operations with exponential backoff

**Trigger keywords:** WorkManager, background work, background task, BackgroundScheduler,
BGTaskScheduler, BGProcessingTask, BGAppRefreshTask, one-time work, periodic work, retry
backoff, CoroutineWorker, background sync, deferred work, background job, WorkRequest, KMP
background.

**Freshness rule:** WorkManager 2.9+ uses `CoroutineWorker` as the primary API — do not
use the deprecated `ListenableWorker` directly. On iOS, `BGTaskScheduler` identifiers must
be declared in `Info.plist` under `BGTaskSchedulerPermittedIdentifiers`; the scheduler
silently ignores tasks that are not declared. iOS background time for `BGAppRefreshTask`
is up to ~30 seconds; `BGProcessingTask` allows minutes but requires `requiresNetworkConnectivity`
or `requiresExternalPower` constraints.

---

## Recommendation First

Use `expect/actual BackgroundScheduler` with a minimal scheduling API in `commonMain`.
Android delegates to `WorkManager`; iOS delegates to `BGTaskScheduler`. Keep business
logic in a shared use case — `CoroutineWorker` on Android and the `BGTask` handler on iOS
both call the same use case. This keeps background logic testable without device hardware.

---

## Core types — commonMain

```kotlin
// :core:background:model — WorkTag.kt
enum class WorkTag(val id: String) {
    SyncData("com.example.app.sync_data"),
    UploadToken("com.example.app.upload_token"),
    CleanupCache("com.example.app.cleanup_cache"),
}

data class WorkConstraints(
    val requiresNetwork: Boolean = true,
    val requiresCharging: Boolean = false,
    val requiresIdle: Boolean = false,
)
```

---

## BackgroundScheduler — expect/actual

```kotlin
// commonMain — BackgroundScheduler.kt
expect class BackgroundScheduler {
    fun scheduleOneTime(
        tag: WorkTag,
        constraints: WorkConstraints = WorkConstraints(),
        initialDelay: Duration = Duration.ZERO,
        replaceIfRunning: Boolean = true,
    )

    fun schedulePeriodic(
        tag: WorkTag,
        interval: Duration,
        constraints: WorkConstraints = WorkConstraints(),
    )

    fun cancel(tag: WorkTag)
}
```

---

## Android implementation

**`libs.versions.toml`**:
```toml
[versions]
workmanager = "2.9.0"

[libraries]
work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "workmanager" }
```

```kotlin
// androidMain — BackgroundScheduler.android.kt
actual class BackgroundScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    actual fun scheduleOneTime(
        tag: WorkTag,
        constraints: WorkConstraints,
        initialDelay: Duration,
        replaceIfRunning: Boolean,
    ) {
        val request = OneTimeWorkRequestBuilder<AppWorker>()
            .setConstraints(constraints.toAndroidConstraints())
            .setInitialDelay(initialDelay.toLong(DurationUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("tag" to tag.id))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(tag.id)
            .build()

        val policy = if (replaceIfRunning)
            ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

        workManager.enqueueUniqueWork(tag.id, policy, request)
    }

    actual fun schedulePeriodic(
        tag: WorkTag,
        interval: Duration,
        constraints: WorkConstraints,
    ) {
        val request = PeriodicWorkRequestBuilder<AppWorker>(
            interval.toLong(DurationUnit.MINUTES), TimeUnit.MINUTES
        )
            .setConstraints(constraints.toAndroidConstraints())
            .setInputData(workDataOf("tag" to tag.id))
            .addTag(tag.id)
            .build()

        workManager.enqueueUniquePeriodicWork(
            tag.id,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    actual fun cancel(tag: WorkTag) {
        workManager.cancelUniqueWork(tag.id)
    }

    private fun WorkConstraints.toAndroidConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(if (requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
            .setRequiresCharging(requiresCharging)
            .setRequiresDeviceIdle(requiresIdle)
            .build()
}
```

**CoroutineWorker — dispatches to use cases**:
```kotlin
// androidMain — AppWorker.kt
class AppWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tag = inputData.getString("tag") ?: return Result.failure()
        return try {
            when (WorkTag.values().firstOrNull { it.id == tag }) {
                WorkTag.SyncData     -> get<SyncDataUseCase>().invoke()
                WorkTag.UploadToken  -> get<UploadPushTokenUseCase>().invoke()
                WorkTag.CleanupCache -> get<CleanupCacheUseCase>().invoke()
                null -> Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    // Access Koin from worker via koin-android-workmanager
    private inline fun <reified T : Any> get() = getKoin().get<T>()
}
```

---

## iOS implementation

**`Info.plist`** — declare task identifiers:
```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.example.app.sync_data</string>
    <string>com.example.app.upload_token</string>
    <string>com.example.app.cleanup_cache</string>
</array>
```

```kotlin
// iosMain — BackgroundScheduler.ios.kt
actual class BackgroundScheduler {

    actual fun scheduleOneTime(
        tag: WorkTag,
        constraints: WorkConstraints,
        initialDelay: Duration,
        replaceIfRunning: Boolean,
    ) {
        val request = BGProcessingTaskRequest(identifier = tag.id).apply {
            requiresNetworkConnectivity = constraints.requiresNetwork
            requiresExternalPower = constraints.requiresCharging
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(
                initialDelay.toDouble(DurationUnit.SECONDS)
            )
        }
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
        } catch (e: Exception) {
            // Task identifier not declared in Info.plist — log and no-op
        }
    }

    actual fun schedulePeriodic(
        tag: WorkTag,
        interval: Duration,
        constraints: WorkConstraints,
    ) {
        // iOS does not support true periodic tasks; re-schedule from the completion handler
        scheduleOneTime(tag, constraints)
    }

    actual fun cancel(tag: WorkTag) {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(tag.id)
    }
}
```

**Register handlers in AppDelegate**:
```swift
func application(_ application: UIApplication,
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    BGTaskScheduler.shared.register(
        forTaskWithIdentifier: "com.example.app.sync_data",
        using: nil
    ) { task in
        self.handleSyncData(task: task as! BGProcessingTask)
    }
    return true
}

func handleSyncData(task: BGProcessingTask) {
    // Reschedule for next run
    BackgroundBridge.shared.scheduleOneTime(tag: .syncData)
    task.expirationHandler = { task.setTaskCompleted(success: false) }
    Task {
        // Call Kotlin use case via KMP bridge
        let success = await SyncDataBridge.shared.execute()
        task.setTaskCompleted(success: success)
    }
}
```

---

## Chaining work (Android only)

```kotlin
// Chain: sync → upload → cleanup
workManager
    .beginUniqueWork("full_sync_chain", ExistingWorkPolicy.REPLACE, syncRequest)
    .then(uploadRequest)
    .then(cleanupRequest)
    .enqueue()
```

---

## Koin wiring

```kotlin
// androidMain
val backgroundModule = module {
    single { BackgroundScheduler(get<Context>()) }
}

// iosMain
val backgroundModule = module {
    single { BackgroundScheduler() }
}
```

---

## Testing

```kotlin
class FakeBackgroundScheduler : BackgroundScheduler() {
    val scheduled = mutableListOf<Pair<WorkTag, Boolean>>() // tag, isOneTime
    override fun scheduleOneTime(tag: WorkTag, constraints: WorkConstraints,
                                  initialDelay: Duration, replaceIfRunning: Boolean) {
        scheduled.add(tag to true)
    }
    override fun schedulePeriodic(tag: WorkTag, interval: Duration, constraints: WorkConstraints) {
        scheduled.add(tag to false)
    }
    override fun cancel(tag: WorkTag) = scheduled.removeIf { it.first == tag }.let { }
}

@Test fun `push token upload is scheduled on token refresh`() {
    val scheduler = FakeBackgroundScheduler()
    // simulate token refresh
    val handler = PushTokenRefreshHandler(scheduler)
    handler.onNewToken("new_token_value")
    assertTrue(scheduler.scheduled.any { it.first == WorkTag.UploadToken })
}
```

---

## Common Anti-Patterns

- **Calling WorkManager or BGTaskScheduler directly in a ViewModel** — ViewModel is in
  `commonMain`; WorkManager is Android-only; delegate scheduling to `BackgroundScheduler`
- **Not declaring BGTask identifiers in Info.plist** — `BGTaskScheduler.submitTaskRequest`
  silently fails if the identifier is not in `BGTaskSchedulerPermittedIdentifiers`; check
  the Xcode console for `Error submitting` warnings
- **Scheduling periodic tasks on iOS with a fixed interval** — iOS does not honor fixed
  intervals; use `BGAppRefreshTask` or reschedule from the completion handler and let iOS
  decide when to run based on battery and usage patterns
- **Putting large data in `WorkData`** — `WorkManager`'s `Data` has a 10 KB size limit;
  pass an ID and fetch the full payload from the repository inside the worker
- **Not retrying on transient errors** — return `Result.retry()` (not `Result.failure()`)
  for network timeouts so WorkManager applies the backoff policy

---

## Related Skills

- `kotlin-multiplatform-push-notifications` — push token upload after `onNewToken` is the
  primary use case; enqueue `WorkTag.UploadToken` from `FirebaseMessagingService`
- `kotlin-multiplatform-dependency-injection` — `BackgroundScheduler` is a Koin single
  in the platform module; `AppWorker` accesses use cases via `koin-android-workmanager`
- `kotlin-multiplatform-unit-testing` — use case logic called by the worker is tested
  independently; use `FakeBackgroundScheduler` for ViewModel tests that trigger scheduling

---

## Output Style

When implementing background tasks, respond in this order:
1. **WorkTag enum** — task identifiers matching Info.plist declarations
2. **BackgroundScheduler expect/actual** — `scheduleOneTime`, `schedulePeriodic`, `cancel`
3. **Android impl** — `BackgroundScheduler`, `CoroutineWorker`, Koin wiring
4. **iOS impl** — `BackgroundScheduler`, AppDelegate handler registration, Info.plist keys
5. **Chain** — Android-only chained work (if requested)
6. **FakeBackgroundScheduler + tests** — scheduling side effects verified in ViewModel tests

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
