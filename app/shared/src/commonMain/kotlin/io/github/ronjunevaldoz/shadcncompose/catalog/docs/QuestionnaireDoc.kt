@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaire
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireChoice
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireChoices
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireError
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireInput
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireItemDef
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireNext
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnairePrevious
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireProgress
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireSkip
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireSubmit
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnQuestionnaireTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.components.rememberShadcnQuestionnaireState

/**
 * Real shadcn/ui's published component is a thin styling wrapper over a headless
 * `@shadcn/react/questionnaire` primitive (verified directly against
 * `packages/react/src/questionnaire` in `shadcn-ui/ui`, not the docs page alone) --
 * this library reimplements the same state machine (validation, single/multi-select,
 * skip, submit) as plain hoisted Compose state via [ShadcnQuestionnaireState]. No native
 * HTML form validation, letter/number keyboard shortcuts, or DOM-order reordering --
 * see [ShadcnQuestionnaireState]'s own doc comment for the full, disclosed list.
 */
val questionnaireDoc =
    ComponentDoc(
        id = "questionnaire",
        referenceUrl = "https://ui.shadcn.com/docs/components/base/questionnaire",
        title = "Questionnaire",
        description =
            "A multi-step form for single-choice, multiple-choice, and freeform questions, " +
                "with progress tracking, required/skip semantics, and per-item validation.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            val items = listOf(
                ShadcnQuestionnaireItemDef(name = "direction", required = true),
                ShadcnQuestionnaireItemDef(name = "detail", required = false),
            )
            val state = rememberShadcnQuestionnaireState(items)

            ShadcnQuestionnaire(state = state) {
                ShadcnQuestionnaireProgress(state)
                ShadcnQuestionnaireItem(state, items[0]) {
                    ShadcnQuestionnaireTitle("What should we prototype next?")
                    ShadcnQuestionnaireChoices {
                        ShadcnQuestionnaireChoice(state, "direction", "delegation") {
                            ShadcnText("Delegation")
                        }
                    }
                    ShadcnQuestionnaireError(state, "direction")
                }
                ShadcnQuestionnaireActions {
                    ShadcnQuestionnairePrevious(state)
                    ShadcnQuestionnaireSkip(onSubmit = { }, state = state)
                    ShadcnQuestionnaireNext(state)
                    ShadcnQuestionnaireSubmit(onSubmit = { }, state = state)
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        val items = listOf(
                            ShadcnQuestionnaireItemDef(name = "direction", required = true),
                            ShadcnQuestionnaireItemDef(name = "detail", required = false),
                        )
                        val state = rememberShadcnQuestionnaireState(items)
                        var result by remember { mutableStateOf<String?>(null) }

                        ShadcnQuestionnaire(state = state) {
                            ShadcnQuestionnaireProgress(state)
                            ShadcnQuestionnaireItem(state, items[0]) {
                                ShadcnQuestionnaireTitle("What should we prototype next?")
                                ShadcnQuestionnaireDescription("Choose a direction or write your own.")
                                ShadcnQuestionnaireChoices {
                                    ShadcnQuestionnaireChoice(state, "direction", "delegation") {
                                        Column {
                                            ShadcnText("Delegation")
                                            ShadcnText(
                                                "Show how work moves to a specialist.",
                                                style = ShadcnTextStyle.BodySmall,
                                                muted = true,
                                            )
                                        }
                                    }
                                    ShadcnQuestionnaireChoice(state, "direction", "questions") {
                                        ShadcnText("Question prompts")
                                    }
                                    ShadcnQuestionnaireInput(state, "direction", placeholder = "Type another answer...")
                                }
                                ShadcnQuestionnaireError(state, "direction")
                            }
                            ShadcnQuestionnaireItem(state, items[1]) {
                                ShadcnQuestionnaireTitle("How much detail?")
                                ShadcnQuestionnaireDescription("Skip this if you are not sure yet.")
                                ShadcnQuestionnaireChoices {
                                    ShadcnQuestionnaireChoice(state, "detail", "focused") { ShadcnText("Focused") }
                                    ShadcnQuestionnaireChoice(state, "detail", "complete") { ShadcnText("Complete flow") }
                                }
                            }
                            ShadcnQuestionnaireActions {
                                ShadcnQuestionnairePrevious(state)
                                ShadcnQuestionnaireSkip(onSubmit = { result = it.toString() }, state = state)
                                ShadcnQuestionnaireNext(state)
                                ShadcnQuestionnaireSubmit(onSubmit = { result = it.toString() }, state = state)
                            }
                        }
                        result?.let { ShadcnText(it, style = ShadcnTextStyle.BodySmall, muted = true) }
                        """.trimIndent(),
                    preview = {
                        val items =
                            listOf(
                                ShadcnQuestionnaireItemDef(name = "direction", required = true),
                                ShadcnQuestionnaireItemDef(name = "detail", required = false),
                            )
                        val state = rememberShadcnQuestionnaireState(items)
                        var result by remember { mutableStateOf<String?>(null) }

                        Column(modifier = Modifier.width(360.dp)) {
                            ShadcnQuestionnaire(state = state) {
                                ShadcnQuestionnaireProgress(state)
                                ShadcnQuestionnaireItem(state, items[0]) {
                                    ShadcnQuestionnaireTitle("What should we prototype next?")
                                    ShadcnQuestionnaireDescription("Choose a direction or write your own.")
                                    ShadcnQuestionnaireChoices {
                                        ShadcnQuestionnaireChoice(state, "direction", "delegation") {
                                            Column {
                                                ShadcnText("Delegation")
                                                ShadcnText(
                                                    "Show how work moves to a specialist.",
                                                    style = ShadcnTextStyle.BodySmall,
                                                    muted = true,
                                                )
                                            }
                                        }
                                        ShadcnQuestionnaireChoice(state, "direction", "questions") {
                                            ShadcnText("Question prompts")
                                        }
                                        ShadcnQuestionnaireInput(
                                            state,
                                            "direction",
                                            placeholder = "Type another answer...",
                                        )
                                    }
                                    ShadcnQuestionnaireError(state, "direction")
                                }
                                ShadcnQuestionnaireItem(state, items[1]) {
                                    ShadcnQuestionnaireTitle("How much detail?")
                                    ShadcnQuestionnaireDescription("Skip this if you are not sure yet.")
                                    ShadcnQuestionnaireChoices {
                                        ShadcnQuestionnaireChoice(state, "detail", "focused") { ShadcnText("Focused") }
                                        ShadcnQuestionnaireChoice(
                                            state,
                                            "detail",
                                            "complete",
                                        ) { ShadcnText("Complete flow") }
                                    }
                                }
                                ShadcnQuestionnaireActions {
                                    ShadcnQuestionnairePrevious(state)
                                    ShadcnQuestionnaireSkip(onSubmit = { result = it.toString() }, state = state)
                                    ShadcnQuestionnaireNext(state)
                                    ShadcnQuestionnaireSubmit(onSubmit = { result = it.toString() }, state = state)
                                }
                            }
                            result?.let { ShadcnText(it, style = ShadcnTextStyle.BodySmall, muted = true) }
                        }
                    },
                ),
                ComponentExample(
                    title = "Multi-select",
                    code =
                        """
                        // multiple = true makes each ShadcnQuestionnaireChoice a checkbox instead
                        // of a radio, letting more than one value stay selected at once.
                        val items = listOf(ShadcnQuestionnaireItemDef(name = "tools", required = true))
                        val state = rememberShadcnQuestionnaireState(items)

                        ShadcnQuestionnaire(state = state) {
                            ShadcnQuestionnaireItem(state, items[0]) {
                                ShadcnQuestionnaireTitle("Which tools do you use daily?")
                                ShadcnQuestionnaireChoices {
                                    ShadcnQuestionnaireChoice(state, "tools", "figma", multiple = true) {
                                        ShadcnText("Figma")
                                    }
                                    ShadcnQuestionnaireChoice(state, "tools", "notion", multiple = true) {
                                        ShadcnText("Notion")
                                    }
                                    ShadcnQuestionnaireChoice(state, "tools", "linear", multiple = true) {
                                        ShadcnText("Linear")
                                    }
                                }
                                ShadcnQuestionnaireError(state, "tools")
                            }
                            ShadcnQuestionnaireActions {
                                ShadcnQuestionnairePrevious(state)
                                ShadcnQuestionnaireSkip(onSubmit = { }, state = state)
                                ShadcnQuestionnaireNext(state)
                                ShadcnQuestionnaireSubmit(onSubmit = { }, state = state)
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        val items = listOf(ShadcnQuestionnaireItemDef(name = "tools", required = true))
                        val state = rememberShadcnQuestionnaireState(items)

                        Column(modifier = Modifier.width(360.dp)) {
                            ShadcnQuestionnaire(state = state) {
                                ShadcnQuestionnaireItem(state, items[0]) {
                                    ShadcnQuestionnaireTitle("Which tools do you use daily?")
                                    ShadcnQuestionnaireChoices {
                                        ShadcnQuestionnaireChoice(state, "tools", "figma", multiple = true) {
                                            ShadcnText("Figma")
                                        }
                                        ShadcnQuestionnaireChoice(state, "tools", "notion", multiple = true) {
                                            ShadcnText("Notion")
                                        }
                                        ShadcnQuestionnaireChoice(state, "tools", "linear", multiple = true) {
                                            ShadcnText("Linear")
                                        }
                                    }
                                    ShadcnQuestionnaireError(state, "tools")
                                }
                                ShadcnQuestionnaireActions {
                                    ShadcnQuestionnairePrevious(state)
                                    ShadcnQuestionnaireSkip(onSubmit = { }, state = state)
                                    ShadcnQuestionnaireNext(state)
                                    ShadcnQuestionnaireSubmit(onSubmit = { }, state = state)
                                }
                            }
                        }
                    },
                ),
            ),
    )
