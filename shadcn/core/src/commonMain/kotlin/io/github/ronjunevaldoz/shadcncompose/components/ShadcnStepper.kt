package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One step in a [ShadcnStepper]: a numbered circle with a title and optional description. */
data class ShadcnStepperStep(val title: String, val description: String? = null)

/** Layout direction for [ShadcnStepper] -- [Horizontal] for wide screens, [Vertical] for narrow/mobile or long labels. */
enum class ShadcnStepperOrientation { Horizontal, Vertical }

/**
 * A multi-step progress indicator, matching shadcn/ui community "stepper" patterns (e.g.
 * shadcn-studio's stepper variants): numbered circles connected by a line, with each step's
 * title/description alongside. Steps before [currentStep] render as completed (filled), the step
 * at [currentStep] as active (filled + ring), the rest as upcoming (muted).
 *
 * [showLabels] = false renders a compact indicator with just circles and connectors -- no
 * title/description row -- for tight spaces (progress dots, condensed headers).
 *
 * This is the step *indicator* only -- Back/Next navigation and per-step content are left to the
 * caller (a `when (currentStep)` block plus [ShadcnButton]s, or a form built from
 * [ShadcnTextField]), matching how [ShadcnTabs] and [ShadcnAccordion] stay presentational
 * primitives rather than owning wizard state.
 *
 * Usage:
 * ```
 * var step by remember { mutableStateOf(0) }
 * ShadcnStepper(
 *     steps = listOf(
 *         ShadcnStepperStep("Details", "Enter the required details"),
 *         ShadcnStepperStep("Review", "Confirm your information"),
 *         ShadcnStepperStep("Done", "All set"),
 *     ),
 *     currentStep = step,
 * )
 * ```
 */
@Composable
fun ShadcnStepper(
    steps: List<ShadcnStepperStep>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    orientation: ShadcnStepperOrientation = ShadcnStepperOrientation.Horizontal,
    showLabels: Boolean = true,
) {
    when (orientation) {
        ShadcnStepperOrientation.Horizontal -> HorizontalStepper(steps, currentStep, showLabels, modifier)
        ShadcnStepperOrientation.Vertical -> VerticalStepper(steps, currentStep, showLabels, modifier)
    }
}

private val StepCircleSize = 32.dp

@Composable
private fun StepCircle(
    index: Int,
    currentStep: Int,
) {
    val theme = shadcnTheme
    val reached = index <= currentStep
    val active = index == currentStep
    // Real shadcn rings sit *outside* the shape with a gap (`ring-offset`), not flush against its
    // own edge -- a plain `border(...)` at the circle's own size draws inset on the boundary,
    // reading as a thin scratch on the fill rather than a ring. Sizing the bordered Box larger
    // than the filled circle (by ring.width on each side) and centering the circle inside it
    // reproduces the offset: Modifier.border always draws at *its own* Box's edge, which is now
    // further out than the circle underneath.
    Box(
        modifier =
            Modifier
                .size(StepCircleSize + if (active) theme.ring.width * 2 else 0.dp)
                .let {
                    if (active) {
                        it.border(
                            theme.ring.width,
                            theme.colors.borderFocus.copy(alpha = theme.ring.opacity),
                            CircleShape,
                        )
                    } else {
                        it
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(StepCircleSize)
                    .background(if (reached) theme.colors.primary else theme.colors.muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ShadcnText(
                (index + 1).toString(),
                style = ShadcnTextStyle.LabelLarge,
                color = if (reached) theme.colors.onPrimary else theme.colors.onMuted,
            )
        }
    }
}

@Composable
private fun HorizontalStepper(
    steps: List<ShadcnStepperStep>,
    currentStep: Int,
    showLabels: Boolean,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { index, _ ->
                StepCircle(index, currentStep)
                if (index != steps.lastIndex) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (index < currentStep) shadcnTheme.colors.primary else shadcnTheme.colors.border,
                                ),
                    )
                }
            }
        }
        if (showLabels) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = shadcnTheme.spacing.sm)) {
                steps.forEachIndexed { index, step ->
                    val upcoming = index > currentStep
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        ShadcnText(step.title, style = ShadcnTextStyle.LabelLarge, muted = upcoming)
                        step.description?.let { ShadcnText(it, style = ShadcnTextStyle.BodySmall, muted = true) }
                    }
                    if (index != steps.lastIndex) {
                        Box(modifier = Modifier.width(shadcnTheme.spacing.md))
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalStepper(
    steps: List<ShadcnStepperStep>,
    currentStep: Int,
    showLabels: Boolean,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        steps.forEachIndexed { index, step ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StepCircle(index, currentStep)
                    if (index != steps.lastIndex) {
                        val connectorColor =
                            if (index < currentStep) shadcnTheme.colors.primary else shadcnTheme.colors.border
                        Box(
                            modifier =
                                Modifier
                                    .width(2.dp)
                                    .height(if (showLabels) 40.dp else 16.dp)
                                    .background(connectorColor),
                        )
                    }
                }
                if (showLabels) {
                    val upcoming = index > currentStep
                    Column(
                        modifier = Modifier.padding(start = shadcnTheme.spacing.sm, top = shadcnTheme.spacing.xxs),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        ShadcnText(step.title, style = ShadcnTextStyle.LabelLarge, muted = upcoming)
                        step.description?.let { ShadcnText(it, style = ShadcnTextStyle.BodySmall, muted = true) }
                    }
                }
            }
        }
    }
}
