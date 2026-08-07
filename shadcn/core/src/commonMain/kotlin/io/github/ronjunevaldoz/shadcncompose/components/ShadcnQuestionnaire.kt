@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A single question in a [ShadcnQuestionnaire]. Matches real shadcn/ui's
 * `Questionnaire.Item` definition shape -- [name] is the stable answer key,
 * [required] gates whether [ShadcnQuestionnaireSkip] is allowed, [disabled] excludes
 * the item from navigation entirely.
 */
data class ShadcnQuestionnaireItemDef(
    val name: String,
    val required: Boolean = false,
    val disabled: Boolean = false,
)

/** Matches real shadcn/ui's `QuestionnaireItemStatus`. */
enum class ShadcnQuestionnaireStatus { Unanswered, Answered, Skipped }

/**
 * Hoisted state for a [ShadcnQuestionnaire], created via [rememberShadcnQuestionnaireState].
 * Distilled from real shadcn/ui's `use-questionnaire-root`/`use-questionnaire-item` (verified
 * against `packages/react/src/questionnaire` in `shadcn-ui/ui`, since the published component
 * is a thin styling wrapper over a headless `@shadcn/react/questionnaire` primitive with no
 * Compose equivalent to wrap) -- reimplemented as plain Compose state rather than the real
 * version's DOM-registration/`FormData` model, which has no meaning outside a browser form.
 *
 * **Real, disclosed simplifications** -- not ported, and not silently dropped:
 * - No native HTML form validation / `FormData` collection (Compose has neither) -- [submit]
 *   returns the answers as a plain `Map` instead.
 * - No letter/number keyboard shortcuts per choice (`shortcuts` prop in the real API) -- arrow
 *   keys and text input still work through normal Compose focus traversal.
 * - No DOM `MutationObserver`-based reordering -- item order is fixed to [items]' order.
 */
class ShadcnQuestionnaireState(val items: List<ShadcnQuestionnaireItemDef>) {
    var currentIndex: Int by mutableStateOf(0)
        private set
    private val selectedValues = mutableStateMapOf<String, Set<String>>()
    private val inputValues = mutableStateMapOf<String, String>()
    private val skippedItems = mutableStateMapOf<String, Boolean>()
    private val validationAttempted = mutableStateMapOf<String, Boolean>()

    val total: Int get() = items.size
    val current: Int get() = currentIndex + 1
    val first: Boolean get() = currentIndex == 0
    val last: Boolean get() = currentIndex == items.lastIndex
    val activeItem: ShadcnQuestionnaireItemDef get() = items[currentIndex]

    fun selectedValues(name: String): Set<String> = selectedValues[name].orEmpty()

    fun inputValue(name: String): String = inputValues[name].orEmpty()

    /** Toggles [value] for item [name] -- replaces the selection if [multiple] is false. */
    fun toggleChoice(
        name: String,
        value: String,
        multiple: Boolean,
    ) {
        skippedItems[name] = false
        val current = selectedValues[name].orEmpty()
        selectedValues[name] =
            if (multiple) {
                if (value in current) current - value else current + value
            } else {
                setOf(value)
            }
    }

    fun setInputValue(
        name: String,
        value: String,
    ) {
        skippedItems[name] = false
        inputValues[name] = value
    }

    fun status(name: String): ShadcnQuestionnaireStatus =
        when {
            skippedItems[name] == true -> ShadcnQuestionnaireStatus.Skipped
            selectedValues[name].orEmpty().isNotEmpty() || inputValues[name].orEmpty().isNotBlank() ->
                ShadcnQuestionnaireStatus.Answered
            else -> ShadcnQuestionnaireStatus.Unanswered
        }

    private fun isValid(item: ShadcnQuestionnaireItemDef): Boolean =
        when {
            item.disabled -> true
            status(item.name) == ShadcnQuestionnaireStatus.Skipped -> !item.required
            else -> status(item.name) == ShadcnQuestionnaireStatus.Answered
        }

    /** Whether [name]'s error should render -- only after a failed [goNext]/[submit] attempt. */
    fun isInvalid(name: String): Boolean {
        val item = items.first { it.name == name }
        return validationAttempted[name] == true && !isValid(item)
    }

    /** Validates the active item and advances. Returns `false` (and marks it invalid) if it fails. */
    fun goNext(): Boolean {
        if (last) return false
        validationAttempted[activeItem.name] = true
        if (!isValid(activeItem)) return false
        currentIndex++
        return true
    }

    fun goPrevious() {
        if (!first) currentIndex--
    }

    /** No-op on a [ShadcnQuestionnaireItemDef.required] item, matching real shadcn's `skipCurrent`. */
    fun skip(onLastItemSkipped: () -> Unit = {}) {
        val item = activeItem
        if (item.required) return
        selectedValues.remove(item.name)
        inputValues.remove(item.name)
        skippedItems[item.name] = true
        if (!last) currentIndex++ else onLastItemSkipped()
    }

    /**
     * Validates every item, jumping to and marking the first invalid one if any fail --
     * matches real shadcn's `handleSubmit`. Returns the collected answers only once every
     * item validates.
     */
    fun submit(): Map<String, List<String>>? {
        for ((index, item) in items.withIndex()) {
            validationAttempted[item.name] = true
            if (!isValid(item)) {
                currentIndex = index
                return null
            }
        }
        return items.associate { item ->
            val choices = selectedValues(item.name).toList()
            val input = inputValue(item.name)
            item.name to (choices.ifEmpty { if (input.isNotBlank()) listOf(input) else emptyList() })
        }
    }
}

@Composable
fun rememberShadcnQuestionnaireState(items: List<ShadcnQuestionnaireItemDef>): ShadcnQuestionnaireState =
    remember(items) { ShadcnQuestionnaireState(items) }

/** The questionnaire's root container. [state] drives every child below it. */
@Composable
fun ShadcnQuestionnaire(
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
        content = content,
    )
}

/** A "current / total" indicator, matching real shadcn's `Questionnaire.Progress`. */
@Composable
fun ShadcnQuestionnaireProgress(
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
) {
    ShadcnText(
        "${state.current} / ${state.total}",
        style = ShadcnTextStyle.LabelSmall,
        muted = true,
        modifier = modifier,
    )
}

/**
 * One question's content. Renders [content] only while [item] is the active item -- matches
 * real shadcn's `hidden`/`inert` behavior on inactive items, simplified to not composing them
 * at all rather than composing-but-hiding.
 */
@Composable
fun ShadcnQuestionnaireItem(
    state: ShadcnQuestionnaireState,
    item: ShadcnQuestionnaireItemDef,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (state.activeItem != item) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md),
        content = content,
    )
}

/** A question's prompt, matching real shadcn's `Questionnaire.Title` (real `<legend>`). */
@Composable
fun ShadcnQuestionnaireTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.TitleMedium, modifier = modifier)
}

/** A question's helper text, matching real shadcn's `Questionnaire.Description`. */
@Composable
fun ShadcnQuestionnaireDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, muted = true, modifier = modifier)
}

/** Groups a question's [ShadcnQuestionnaireChoice]s (and optional [ShadcnQuestionnaireInput]). */
@Composable
fun ShadcnQuestionnaireChoices(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
        content = content,
    )
}

/**
 * One selectable answer. [multiple] picks a [ShadcnCheckbox] (multi-select) or
 * [ShadcnRadioButton] (single-select, exclusive) indicator -- matches real shadcn's
 * `data-[type=radio]`/`data-[type=checkbox]` split, driven by the same
 * [ShadcnQuestionnaireItemDef.multiple]-equivalent the caller passes as [multiple] here
 * (this library threads it per-call rather than through item context, since [ShadcnQuestionnaireItem]
 * has no `multiple` field of its own -- see [ShadcnQuestionnaireItemDef]).
 */
@Composable
fun ShadcnQuestionnaireChoice(
    state: ShadcnQuestionnaireState,
    itemName: String,
    value: String,
    modifier: Modifier = Modifier,
    multiple: Boolean = false,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val checked = value in state.selectedValues(itemName)
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                ) { state.toggleChoice(itemName, value, multiple) }
                .padding(vertical = shadcnTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        if (multiple) {
            ShadcnCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
        } else {
            ShadcnRadioButton(selected = checked, onClick = null, enabled = enabled)
        }
        content()
    }
}

/** A freeform text answer, matching real shadcn's `Questionnaire.Input` ("write your own"). */
@Composable
fun ShadcnQuestionnaireInput(
    state: ShadcnQuestionnaireState,
    itemName: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    ShadcnTextField(
        value = state.inputValue(itemName),
        onValueChange = { state.setInputValue(itemName, it) },
        placeholder = placeholder,
        modifier = modifier.fillMaxWidth(),
    )
}

/** A question's validation message, shown only once [ShadcnQuestionnaireState.isInvalid] is true. */
@Composable
fun ShadcnQuestionnaireError(
    state: ShadcnQuestionnaireState,
    itemName: String,
    modifier: Modifier = Modifier,
    text: String = "This question is required.",
) {
    if (!state.isInvalid(itemName)) return
    val theme = ShadcnTheme.LocalShadcnTheme.current
    ShadcnText(text, style = ShadcnTextStyle.BodySmall, color = theme.colors.error, modifier = modifier)
}

/** The Previous/Skip/Next/Submit button row, matching real shadcn's `Questionnaire.Actions`. */
@Composable
fun ShadcnQuestionnaireActions(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Hidden on the first item, matching real shadcn's `Questionnaire.Previous` visibility. */
@Composable
fun ShadcnQuestionnairePrevious(
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Outline,
    content: @Composable () -> Unit = { ShadcnText("Previous") },
) {
    if (state.first) return
    ShadcnButton(onClick = { state.goPrevious() }, modifier = modifier, variant = variant, content = content)
}

/** Hidden on a [ShadcnQuestionnaireItemDef.required] item, matching real shadcn's skip-blocking rule. */
@Composable
fun ShadcnQuestionnaireSkip(
    onSubmit: (Map<String, List<String>>) -> Unit,
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Ghost,
    content: @Composable () -> Unit = { ShadcnText("Skip") },
) {
    if (state.activeItem.required) return
    ShadcnButton(
        onClick = { state.skip(onLastItemSkipped = { state.submit()?.let(onSubmit) }) },
        modifier = modifier,
        variant = variant,
        content = content,
    )
}

/** Hidden on the last item (see [ShadcnQuestionnaireSubmit]), matching real shadcn's visibility split. */
@Composable
fun ShadcnQuestionnaireNext(
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    content: @Composable () -> Unit = { ShadcnText("Next") },
) {
    if (state.last) return
    ShadcnButton(onClick = { state.goNext() }, modifier = modifier, variant = variant, content = content)
}

/** Shown only on the last item; validates every item and calls [onSubmit] once all pass. */
@Composable
fun ShadcnQuestionnaireSubmit(
    onSubmit: (Map<String, List<String>>) -> Unit,
    state: ShadcnQuestionnaireState,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    content: @Composable () -> Unit = { ShadcnText("Submit") },
) {
    if (!state.last) return
    ShadcnButton(onClick = { state.submit()?.let(onSubmit) }, modifier = modifier, variant = variant, content = content)
}
