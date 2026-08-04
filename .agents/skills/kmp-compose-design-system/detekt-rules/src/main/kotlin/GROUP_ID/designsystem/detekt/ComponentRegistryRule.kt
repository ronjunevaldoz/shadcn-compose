package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags @Composable functions in feature modules whose names end with a known
 * design system component suffix AND whose body never actually calls the matching
 * design system component — these are likely reimplementations from raw primitives
 * rather than extensions of the design system.
 *
 * Configure `componentPrefix` in detekt-design-system.yml to match your project's
 * prefix (default: "App").
 *
 * NOT flagged:
 *   - Functions whose name starts with the componentPrefix (e.g. AppButton)
 *   - Functions in core/designsystem/ or files ending in Preview.kt
 *   - Functions with 0 parameters (likely a decorative/section wrapper, not a
 *     reimplementation of an interactive component)
 *   - Functions whose body calls the matching design system component
 *     (e.g. `fun ProductCard(title: String) { AppCard { AppText(title) } }` —
 *     this is composing the design system correctly, not reimplementing it)
 *
 * FLAGGED:
 *   - `fun MyButton(label: String) { Button(onClick = {}) { Text(label) } }` in
 *     feature/*/ui/ — ends in "Button", body never calls AppButton
 *   - `fun CustomCard(title: String) { Card { Text(title) } }` in feature/*/ —
 *     ends in "Card", body never calls AppCard
 */
class ComponentRegistryRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "ComponentRegistryViolation",
        severity = Severity.Warning,
        description = "Custom composable name matches a design system component, and its body never calls that component. Prefer using the design system component or extending it via the variant system.",
        debt = Debt.TWENTY_MINS,
    )

    private val componentPrefix: String by config.valueOrDefault("componentPrefix", "App")

    private val dsComponentSuffixes = setOf(
        "Button", "Card", "Text", "Badge", "Chip", "TextField",
        "Dialog", "Sheet", "Toast", "TopAppBar", "NavigationBar",
        "Checkbox", "RadioButton", "Switch", "Slider", "Tabs",
        "Icon", "IconButton", "Spinner", "Skeleton", "Progress",
        "Avatar", "Tooltip", "Popover", "Accordion", "Label", "Separator",
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val isComposable = function.annotationEntries
            .any { it.shortName?.asString() == "Composable" }
        if (!isComposable) return

        val filePath = function.containingKtFile.virtualFilePath
        if (filePath.contains("/designsystem/")) return
        if (filePath.endsWith("Preview.kt")) return

        if (function.valueParameters.isEmpty()) return  // likely a decorative/section wrapper

        val name = function.name ?: return
        if (name.startsWith(componentPrefix)) return  // proper DS component or Preview

        val matched = dsComponentSuffixes.firstOrNull { name.endsWith(it) } ?: return

        val expectedCall = "$componentPrefix$matched"
        val bodyCallsDsComponent = function.bodyExpression
            ?.text
            ?.let { body -> Regex("\\b${Regex.escape(expectedCall)}\\s*[(<{]").containsMatchIn(body) }
            ?: false
        if (bodyCallsDsComponent) return  // composes the design system correctly

        report(CodeSmell(issue, Entity.from(function),
            "'$name' reimplements a $expectedCall-shaped component from raw primitives outside " +
            "the design system — its body never calls '$expectedCall'. Use '$expectedCall' " +
            "directly, or add a new variant to the design system rather than creating a parallel " +
            "component."))
    }
}
