@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnStepper
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnStepperOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnStepperStep
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val stepperDoc =
    ComponentDoc(
        id = "stepper",
        title = "Stepper",
        description = "A numbered multi-step progress indicator for wizards, onboarding, and checkout flows.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnStepper(
                steps = listOf(
                    ShadcnStepperStep("Details", "Enter the required details"),
                    ShadcnStepperStep("Review", "Confirm your information"),
                    ShadcnStepperStep("Done", "All set"),
                ),
                currentStep = 0,
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var step by remember { mutableStateOf(0) }
                        Column {
                            ShadcnStepper(
                                steps = listOf(
                                    ShadcnStepperStep("Details", "Enter the required details"),
                                    ShadcnStepperStep("Review", "Confirm your information"),
                                    ShadcnStepperStep("Done", "All set"),
                                ),
                                currentStep = step,
                            )
                            Row {
                                ShadcnButton(onClick = { step = (step - 1).coerceAtLeast(0) }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(2) }) {
                                    ShadcnText("Next")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var step by remember { mutableStateOf(0) }
                        Column(modifier = Modifier.width(320.dp)) {
                            ShadcnStepper(
                                steps =
                                    listOf(
                                        ShadcnStepperStep("Details", "Enter the required details"),
                                        ShadcnStepperStep("Review", "Confirm your information"),
                                        ShadcnStepperStep("Done", "All set"),
                                    ),
                                currentStep = step,
                            )
                            Row(
                                modifier = Modifier.width(320.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ShadcnButton(
                                    onClick = { step = (step - 1).coerceAtLeast(0) },
                                    variant = ButtonVariant.Outline,
                                ) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(2) }) {
                                    ShadcnText("Next")
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Titles only",
                    code =
                        """
                        // No description string -- ShadcnStepperStep.description is nullable.
                        ShadcnStepper(
                            steps = listOf(
                                ShadcnStepperStep("Step 1"),
                                ShadcnStepperStep("Step 2"),
                                ShadcnStepperStep("Step 3"),
                            ),
                            currentStep = 1,
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnStepper(
                            steps = listOf(ShadcnStepperStep("Step 1"), ShadcnStepperStep("Step 2"), ShadcnStepperStep("Step 3")),
                            currentStep = 1,
                            modifier = Modifier.width(320.dp),
                        )
                    },
                ),
                ComponentExample(
                    title = "Compact",
                    code =
                        """
                        // showLabels = false -- just circles and connectors, for tight spaces.
                        ShadcnStepper(
                            steps = listOf(ShadcnStepperStep("1"), ShadcnStepperStep("2"), ShadcnStepperStep("3")),
                            currentStep = 1,
                            showLabels = false,
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnStepper(
                            steps = listOf(ShadcnStepperStep("1"), ShadcnStepperStep("2"), ShadcnStepperStep("3")),
                            currentStep = 1,
                            showLabels = false,
                            modifier = Modifier.width(320.dp),
                        )
                    },
                ),
                ComponentExample(
                    title = "Vertical",
                    code =
                        """
                        ShadcnStepper(
                            steps = listOf(
                                ShadcnStepperStep("Account", "Create an account"),
                                ShadcnStepperStep("Profile", "Set up your profile"),
                                ShadcnStepperStep("Complete", "Complete the setup"),
                            ),
                            currentStep = 1,
                            orientation = ShadcnStepperOrientation.Vertical,
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnStepper(
                            steps =
                                listOf(
                                    ShadcnStepperStep("Account", "Create an account"),
                                    ShadcnStepperStep("Profile", "Set up your profile"),
                                    ShadcnStepperStep("Complete", "Complete the setup"),
                                ),
                            currentStep = 1,
                            orientation = ShadcnStepperOrientation.Vertical,
                        )
                    },
                ),
                ComponentExample(
                    title = "Step content",
                    code =
                        """
                        var step by remember { mutableStateOf(0) }
                        val steps = listOf(
                            ShadcnStepperStep("Details", "Enter the required details for this step"),
                            ShadcnStepperStep("Review", "Confirm your information and choices"),
                            ShadcnStepperStep("Done", "All set. Review completed"),
                        )
                        Column {
                            ShadcnStepper(steps = steps, currentStep = step)
                            Box(modifier = Modifier.background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md))) {
                                ShadcnText("${'$'}{steps[step].title} content")
                            }
                            Row {
                                ShadcnButton(onClick = { step = (step - 1).coerceAtLeast(0) }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(steps.lastIndex) }) {
                                    ShadcnText("Next")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var step by remember { mutableStateOf(0) }
                        val steps =
                            listOf(
                                ShadcnStepperStep("Details", "Enter the required details for this step"),
                                ShadcnStepperStep("Review", "Confirm your information and choices"),
                                ShadcnStepperStep("Done", "All set. Review completed"),
                            )
                        Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                            ShadcnStepper(steps = steps, currentStep = step)
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md))
                                        .padding(shadcnTheme.spacing.md),
                            ) {
                                ShadcnText("${'$'}{steps[step].title} content", style = ShadcnTextStyle.BodySmall, muted = true)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnButton(
                                    onClick = { step = (step - 1).coerceAtLeast(0) },
                                    variant = ButtonVariant.Outline,
                                ) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(steps.lastIndex) }) {
                                    ShadcnText("Next")
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Mid-flow action",
                    code =
                        """
                        // A middle step that can be completed directly, without stepping through it.
                        var step by remember { mutableStateOf(1) }
                        val steps = listOf(ShadcnStepperStep("Step 1"), ShadcnStepperStep("Step 2"), ShadcnStepperStep("Step 3"))
                        Column {
                            ShadcnStepper(steps = steps, currentStep = step)
                            Row {
                                ShadcnButton(onClick = { step = (step - 1).coerceAtLeast(0) }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { }, variant = ButtonVariant.Secondary) { ShadcnText("Complete Step") }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(steps.lastIndex) }) { ShadcnText("Next") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var step by remember { mutableStateOf(1) }
                        val steps = listOf(ShadcnStepperStep("Step 1"), ShadcnStepperStep("Step 2"), ShadcnStepperStep("Step 3"))
                        Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                            ShadcnStepper(steps = steps, currentStep = step)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnButton(
                                    onClick = { step = (step - 1).coerceAtLeast(0) },
                                    variant = ButtonVariant.Outline,
                                ) {
                                    ShadcnText("Back")
                                }
                                ShadcnButton(onClick = { }, variant = ButtonVariant.Secondary) {
                                    ShadcnText("Complete Step")
                                }
                                ShadcnButton(onClick = { step = (step + 1).coerceAtMost(steps.lastIndex) }) {
                                    ShadcnText("Next")
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Form step",
                    code =
                        """
                        var firstName by remember { mutableStateOf("") }
                        var lastName by remember { mutableStateOf("") }
                        Column {
                            ShadcnStepper(
                                steps = listOf(
                                    ShadcnStepperStep("Personal Info"),
                                    ShadcnStepperStep("Contact Info"),
                                    ShadcnStepperStep("Address"),
                                ),
                                currentStep = 0,
                            )
                            ShadcnTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name")
                            ShadcnTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
                            ShadcnButton(onClick = {}) { ShadcnText("Next") }
                        }
                        """.trimIndent(),
                    preview = {
                        var firstName by remember { mutableStateOf("") }
                        var lastName by remember { mutableStateOf("") }
                        Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
                            ShadcnStepper(
                                steps =
                                    listOf(
                                        ShadcnStepperStep("Personal Info"),
                                        ShadcnStepperStep("Contact Info"),
                                        ShadcnStepperStep("Address"),
                                    ),
                                currentStep = 0,
                            )
                            ShadcnTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = "First Name",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            ShadcnTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = "Last Name",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            ShadcnButton(onClick = {}) { ShadcnText("Next") }
                        }
                    },
                ),
            ),
    )
