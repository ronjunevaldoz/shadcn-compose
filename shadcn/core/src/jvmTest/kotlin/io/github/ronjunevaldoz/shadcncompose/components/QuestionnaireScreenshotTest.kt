package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.remember
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class QuestionnaireScreenshotTest : ShadcnScreenshotTest() {
    private val items =
        listOf(
            ShadcnQuestionnaireItemDef(name = "direction", required = true),
            ShadcnQuestionnaireItemDef(name = "detail", required = false),
        )

    private fun firstQuestion(darkTheme: Boolean) {
        snapshot("questionnaire_first_question", darkTheme = darkTheme) {
            val state = rememberShadcnQuestionnaireState(items)
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
                ShadcnQuestionnaireActions {
                    ShadcnQuestionnairePrevious(state)
                    ShadcnQuestionnaireSkip(onSubmit = {}, state = state)
                    ShadcnQuestionnaireNext(state)
                    ShadcnQuestionnaireSubmit(onSubmit = {}, state = state)
                }
            }
        }
    }

    private fun invalidRequired(darkTheme: Boolean) {
        snapshot("questionnaire_invalid_required", darkTheme = darkTheme) {
            val state = rememberShadcnQuestionnaireState(items)
            remember(state) { state.goNext() }
            ShadcnQuestionnaire(state = state) {
                ShadcnQuestionnaireItem(state, items[0]) {
                    ShadcnQuestionnaireTitle("What should we prototype next?")
                    ShadcnQuestionnaireChoices {
                        ShadcnQuestionnaireChoice(state, "direction", "delegation") {
                            ShadcnText("Delegation")
                        }
                    }
                    ShadcnQuestionnaireError(state, "direction")
                }
            }
        }
    }

    @Test fun first_question_light() = firstQuestion(darkTheme = false)

    @Test fun first_question_dark() = firstQuestion(darkTheme = true)

    @Test fun invalid_required_light() = invalidRequired(darkTheme = false)
}
