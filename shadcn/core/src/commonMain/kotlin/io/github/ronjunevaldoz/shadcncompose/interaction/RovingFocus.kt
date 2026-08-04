package io.github.ronjunevaldoz.shadcncompose.interaction

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Real shadcn/ui gets arrow-key/Home/End roving-focus navigation for free in every menu, tab
 * list, and accordion because they're all built on Radix's `RovingFocusGroup` primitive.
 * Compose Foundation has no equivalent, so this is the one shared replacement wired into every
 * list-of-focusable-rows component in this library (DropdownMenu family, Menubar,
 * NavigationMenu, Accordion, Select) instead of hand-rolling per-component key handling.
 *
 * Design: rather than tracking a separate index/[androidx.compose.ui.focus.FocusRequester] per
 * row -- which the free-form `content: @Composable ... -> Unit` slot APIs on
 * DropdownMenu/ContextMenu/Menubar can't enumerate ahead of time -- this rides Compose's own
 * built-in spatial focus search ([FocusManager.moveFocus]), the same mechanism that already
 * powers D-pad/keyboard 2D navigation. Every row in this library is already focusable for free
 * (`Modifier.clickable` installs `focusable()` internally), so no per-row wiring is needed
 * beyond applying this modifier to the *container*. This also means it stays correct if a
 * component's row count changes at runtime -- there's no separate registry to fall out of sync.
 *
 * - The arrow key matching [orientation]'s forward direction (Down for [RovingFocusOrientation.Vertical],
 *   Right for [RovingFocusOrientation.Horizontal]) moves to the next focusable row.
 * - The opposite arrow key moves to the previous row.
 * - Home/End jump to the first/last focusable row.
 * - Arrow keys wrap around at either end (`ArrowDown` on the last row moves to the first),
 *   matching Radix's default `loop` behavior in `Menu`/`Tabs`/`Accordion`.
 *
 * Not covered: typeahead (jump-to-row-by-typed-letter). Real Radix has it, but it needs a
 * registry of each row's label text that this label-agnostic, slot-based approach doesn't have
 * without new bookkeeping -- left as a follow-up, not required for this to be a real fix.
 *
 * [ShadcnResizableHandle][io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizableHandle]
 * deliberately does *not* use this: it's a single value-changing control (closer to a slider),
 * not a group of peer rows to move focus between, so it gets its own small, local key handler.
 */
@Composable
fun Modifier.rovingFocusGroup(orientation: RovingFocusOrientation = RovingFocusOrientation.Vertical): Modifier {
    val focusManager = LocalFocusManager.current
    return this
        .focusGroup()
        .onPreviewKeyEvent { event ->
            when (rovingFocusAction(event.key, event.type, orientation)) {
                RovingFocusAction.MoveForward -> {
                    if (!focusManager.moveFocus(orientation.forward)) {
                        moveFocusToBoundary(focusManager, orientation.backward)
                    }
                    true
                }
                RovingFocusAction.MoveBackward -> {
                    if (!focusManager.moveFocus(orientation.backward)) {
                        moveFocusToBoundary(focusManager, orientation.forward)
                    }
                    true
                }
                RovingFocusAction.JumpToFirst -> {
                    moveFocusToBoundary(focusManager, orientation.backward)
                    true
                }
                RovingFocusAction.JumpToLast -> {
                    moveFocusToBoundary(focusManager, orientation.forward)
                    true
                }
                RovingFocusAction.None -> false
            }
        }
}

enum class RovingFocusOrientation { Vertical, Horizontal }

private val RovingFocusOrientation.forward: FocusDirection
    get() = if (this == RovingFocusOrientation.Vertical) FocusDirection.Down else FocusDirection.Right

private val RovingFocusOrientation.backward: FocusDirection
    get() = if (this == RovingFocusOrientation.Vertical) FocusDirection.Up else FocusDirection.Left

// ponytail: naive bounded walk-to-the-edge instead of a registered FocusRequester list --
// fine for menu/tab/accordion-sized item counts (dozens, not thousands); if a list ever needs
// to roam past this cap, swap to an indexed FocusRequester registry instead of raising the cap.
private const val MAX_ROVING_FOCUS_STEPS = 256

private fun moveFocusToBoundary(
    focusManager: FocusManager,
    direction: FocusDirection,
) {
    var steps = 0
    while (steps < MAX_ROVING_FOCUS_STEPS && focusManager.moveFocus(direction)) steps++
}

/** The four things a key press can mean to a roving-focus group; pure and Compose-runtime-free. */
internal enum class RovingFocusAction { MoveForward, MoveBackward, JumpToFirst, JumpToLast, None }

/**
 * Pure key-to-action mapping, extracted from [rovingFocusGroup] so it's unit-testable without
 * standing up a Compose UI test -- [Key] and [KeyEventType] are plain value types.
 */
internal fun rovingFocusAction(
    key: Key,
    keyEventType: KeyEventType,
    orientation: RovingFocusOrientation,
): RovingFocusAction {
    if (keyEventType != KeyEventType.KeyDown) return RovingFocusAction.None
    return when (key) {
        orientation.forward.toKey() -> RovingFocusAction.MoveForward
        orientation.backward.toKey() -> RovingFocusAction.MoveBackward
        Key.MoveHome -> RovingFocusAction.JumpToFirst
        Key.MoveEnd -> RovingFocusAction.JumpToLast
        else -> RovingFocusAction.None
    }
}

private fun FocusDirection.toKey(): Key =
    when (this) {
        FocusDirection.Down -> Key.DirectionDown
        FocusDirection.Up -> Key.DirectionUp
        FocusDirection.Right -> Key.DirectionRight
        FocusDirection.Left -> Key.DirectionLeft
        else -> error("unreachable: RovingFocusOrientation only ever produces Up/Down/Left/Right")
    }

/**
 * Pure, wrap-around index math for components that track a *virtual* highlighted index instead
 * of moving real focus -- currently just [io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommand],
 * whose search field must keep real focus the whole time (see that component's own KDoc for
 * why it can't use [rovingFocusGroup]). Kept here rather than duplicated so both index-based and
 * real-focus-based components share one definition of "wrap at the edges." `count <= 0` returns
 * `-1` (no valid index) rather than throwing, so an empty list is a safe no-op for callers.
 */
object RovingFocusIndex {
    fun next(
        current: Int,
        count: Int,
    ): Int = if (count <= 0) -1 else (current + 1).mod(count)

    fun previous(
        current: Int,
        count: Int,
    ): Int = if (count <= 0) -1 else (current - 1).mod(count)

    fun first(count: Int): Int = if (count <= 0) -1 else 0

    fun last(count: Int): Int = count - 1
}
