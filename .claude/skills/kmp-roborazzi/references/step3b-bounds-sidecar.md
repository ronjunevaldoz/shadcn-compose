# Step 3b: Bounds Sidecar (exact position/size regression — no vision needed)

Part of `kmp-roborazzi`. Load this file when working on: step 3b: bounds sidecar (exact position/size regression — no vision needed).

---

A pixel diff on the golden PNG tells you *that* something changed, not *what*. Asking an
agent to read the diff image and estimate "did this move 8px or 12px?" from a screenshot
is unreliable — vision models aren't precise at exact pixel numbers. The fix isn't a
smarter image comparison; it's to stop deriving position/size from pixels at all.

`onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot` (already used in the layout
stability regression pattern above) gives exact position and size straight from the
semantics tree. Write it to a small JSON file next to the golden PNG, and a position/size
regression becomes a normal `git diff` line on a committed text file — exact numbers, zero
noise for nodes that didn't move, no image analysis required:

```kotlin
// :core:testing/src/jvmTest/kotlin/GROUP_ID/core/testing/BoundsSnapshot.kt
package GROUP_ID.core.testing

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithTag
import java.io.File

/**
 * Writes exact position/size for [tags] to a JSON sidecar next to the Roborazzi golden PNG
 * of the same name — a position/size regression then shows up as a plain git diff on the
 * sidecar instead of something a reviewer has to eyeball from two screenshots.
 * @receiver Must be called from inside `runDesktopComposeUiTest`, after `setContent` —
 * bounds are read from the live semantics tree at the point of the call.
 */
@OptIn(ExperimentalTestApi::class)
fun SemanticsNodeInteractionsProvider.captureBoundsSnapshot(
    fileName: String,
    vararg tags: String,
    outputDir: File = File("src/jvmTest/snapshots"),
) {
    val bounds = tags.associateWith { tag -> onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot }
    val json = bounds.entries.sortedBy { it.key }.joinToString(",\n", prefix = "{\n", postfix = "\n}") { (tag, r) ->
        "  \"$tag\": {\"left\": ${r.left}, \"top\": ${r.top}, \"width\": ${r.width}, \"height\": ${r.height}}"
    }
    outputDir.mkdirs()
    File(outputDir, fileName).writeText(json + "\n")
}
```

Call it in the same test that records the golden, using the real multiplatform-JVM
`captureRoboImage` entry point (`onRoot().captureRoboImage(...)` inside
`runDesktopComposeUiTest`, not the plain content-lambda form) so both come from the same
composition pass:

```kotlin
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import GROUP_ID.core.testing.captureBoundsSnapshot
import kotlin.test.Test

class AuthContentScreenshotTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun authContent_default() = runDesktopComposeUiTest {
        setContent {
            AppTheme {
                AuthContent(state = AuthContract.State(), onIntent = {})
            }
        }
        onRoot().captureRoboImage("auth_content_default.png")
        captureBoundsSnapshot(
            "auth_content_default.bounds.json",
            AuthTestTags.EMAIL_FIELD, AuthTestTags.PASSWORD_FIELD, AuthTestTags.LOGIN_BUTTON,
        )
    }
}
```

This writes `auth_content_default.bounds.json` next to `auth_content_default.png` in
`src/jvmTest/snapshots/` — commit both. On a PR, `git diff` on the `.bounds.json` file
shows the exact change (`"top": 120.0` → `"top": 128.0`), with nothing printed for tags
that didn't move.

**What this does not replace:** border width and corner radius are not screenshot
problems — they're literal values in `ButtonStyles.kt`/`CardStyles.kt`
(`RoundedCornerShape(appTheme.shapes.md)`, `borderWidth(1.dp)`). A regression there is
already a normal code diff on the Style file, partly caught statically by
`scan_design_violations.py`. Don't add pixel-based border/corner-radius detection — it
would be strictly less precise than the value that's already sitting in source.

---

